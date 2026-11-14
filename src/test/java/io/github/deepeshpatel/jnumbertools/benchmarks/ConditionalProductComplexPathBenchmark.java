/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * JMH benchmark for conditional Cartesian products whose rules form <em>complex paths</em>.
 *
 * <p>{@code ConditionalProductBenchmark} uses a chain of adjacent rules checked as soon as the next value is
 * chosen. A search that prunes at every step never reaches a dead end there, which is the best case for
 * hand-written backtracking. This benchmark adds the shapes where that stops being true:</p>
 *
 * <ul>
 *   <li><b>CHAIN</b>: control, same shape as the existing benchmark.</li>
 *   <li><b>STAR_FIRST</b>: one hub declared first; every other dimension depends on it.</li>
 *   <li><b>STAR_LAST</b>: one hub declared last that depends on all the others. A prefix is refuted only when the
 *       hub is finally chosen, so a plain search meets many dead ends.</li>
 *   <li><b>SHUFFLED_TREE</b>: a random tree with randomly ordered dimension labels, so dimensions depend on
 *       several earlier ones and early choices are refuted late.</li>
 *   <li><b>CYCLE</b> and <b>CYCLE_TAIL</b>: rules linked in a loop, which the builder solves as one block, with and
 *       without a chain of rules after it.</li>
 * </ul>
 *
 * <p>Scenario names read {@code SHAPE_NxS_dP}: N dimensions of S values, and every rule allows about P percent of the
 * second dimension's values.</p>
 *
 * <h2>Operations</h2>
 * <ul>
 *   <li>{@code builderFirstN}, {@code imperativeBacktracking}, {@code imperativeChecksumOnly},
 *       {@code javaStreamFilter}: the first {@value #FIRST_N} valid tuples in lexicographic order.
 *       {@code imperativeBacktracking} builds the same {@link Tuple} per result as the builder, so it is the fair
 *       baseline; {@code imperativeChecksumOnly} allocates nothing and shows the floor.</li>
 *   <li>{@code builderBuildAndCount} against {@code imperativeCount}: applying every rule and counting.</li>
 *   <li>{@code builderUnrank} and {@code builderSample100}: the tuple at a random rank and 100 distinct random
 *       tuples. No enumeration-based baseline is included, because for these spaces it would take far longer than
 *       the budget; see the note printed during setup.</li>
 * </ul>
 *
 * <h2>Work budget</h2>
 * <p>The baselines stop after {@value #MAX_EXPANSIONS} steps so that a hopeless scenario cannot stall the run. A
 * capped baseline returns the sentinel {@link Long#MIN_VALUE} and its time is a <em>lower bound</em>. The setup of
 * each scenario prints which baselines complete and which are capped.</p>
 *
 * <p>Correctness is verified in setup, outside the timed methods: the builder and every baseline that completes
 * must agree on the first {@value #FIRST_N} tuples and on the count, and a sample of ranks must unrank to valid
 * tuples in strictly increasing order.</p>
 *
 * <p>Run (all scenarios, about half an hour with the defaults):</p>
 * <pre>
 *   org.openjdk.jmh.Main ConditionalProductComplexPathBenchmark
 *   org.openjdk.jmh.Main ConditionalProductComplexPathBenchmark -p scenarioName=STAR_LAST_7x15_d20 -prof gc
 * </pre>
 *
 * @author Deepesh Patel
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductComplexPathBenchmark {

    static final int FIRST_N = 1_000;
    static final long MAX_EXPANSIONS = 30_000_000L;
    static final long CAPPED = Long.MIN_VALUE;

    // -------------------------------------------------------------------------
    // JMH state
    // -------------------------------------------------------------------------

    @State(Scope.Thread)
    public static class BenchmarkState {

        @Param({
                "CHAIN_5x30_d30",
                "STAR_FIRST_5x30_d30",
                "STAR_LAST_5x30_d30",
                "STAR_LAST_7x15_d20",
                "SHUFFLED_TREE_8x12_d25",
                "SHUFFLED_TREE_12x10_d15",
                "CYCLE_3x60_d50",
                "CYCLE_TAIL_6x20_d40",
                "SHUFFLED_TREE_30x8_d30"
        })
        public String scenarioName;

        Scenario scenario;
        ConditionalProductBuilder builder;
        BigInteger count;
        BigInteger[] ranks;
        int nextRank;

        @Setup(Level.Trial)
        public void setup() {
            scenario = Scenario.parse(scenarioName);
            builder = scenario.builder();
            count = builder.count();
            if (count.signum() == 0) {
                throw new IllegalStateException(scenarioName + " has no valid tuples; change the seed or density");
            }

            Random random = new Random(1);
            ranks = new BigInteger[256];
            for (int i = 0; i < ranks.length; i++) {
                ranks[i] = new BigInteger(count.bitLength() + 8, random).mod(count);
            }

            verify();
        }

        private void verify() {
            Checksum fromBuilder = new Checksum();
            builder.lexOrder().stream().limit(FIRST_N).forEach(fromBuilder::accept);

            Checksum noAlloc = imperativeFirstN(scenario, false, new Budget());
            Checksum withTuples = imperativeFirstN(scenario, true, new Budget());
            Checksum naive = naiveFirstN(scenario, new Budget());
            Long imperativeCount = imperativeCount(scenario, new Budget());

            requireSame("imperative (no objects)", fromBuilder, noAlloc);
            requireSame("imperative (Tuple)", fromBuilder, withTuples);
            requireSame("naive stream filter", fromBuilder, naive);
            if (imperativeCount != null && !BigInteger.valueOf(imperativeCount).equals(count)) {
                throw new IllegalStateException(scenarioName + ": count mismatch, builder " + count
                        + " vs backtracking " + imperativeCount);
            }

            // ranks must map to valid tuples, in strictly increasing lexicographic order
            BigInteger[] sorted = ranks.clone();
            java.util.Arrays.sort(sorted);
            int[] previous = null;
            for (int i = 0; i < 40; i++) {
                if (i > 0 && sorted[i].equals(sorted[i - 1])) continue;
                List<Object> tuple = builder.byRanks(List.of(sorted[i])).stream().findFirst().orElseThrow();
                if (!scenario.valid(tuple)) {
                    throw new IllegalStateException(scenarioName + ": rank " + sorted[i] + " is not a valid tuple");
                }
                int[] current = new int[tuple.size()];
                for (int d = 0; d < current.length; d++) current[d] = (Integer) tuple.get(d);
                if (previous != null && java.util.Arrays.compare(previous, current) >= 0) {
                    throw new IllegalStateException(scenarioName + ": ranks are not in lexicographic order");
                }
                previous = current;
            }

            BigInteger full = scenario.fullProduct();
            double share = new java.math.BigDecimal(count)
                    .divide(new java.math.BigDecimal(full), 12, java.math.RoundingMode.HALF_UP).doubleValue() * 100;
            System.out.printf("%n[ComplexPath] %s | full product %s | valid %s (%.6f%%) | first %d: imperative %s,"
                            + " naive filter %s | count: backtracking %s%n",
                    scenarioName, full, count, share, FIRST_N,
                    noAlloc == null ? "CAPPED" : "completes",
                    naive == null ? "CAPPED" : "completes",
                    imperativeCount == null ? "CAPPED" : "completes");
        }

        private void requireSame(String name, Checksum expected, Checksum actual) {
            if (actual != null && !expected.equals(actual)) {
                throw new IllegalStateException(scenarioName + ": " + name + " disagrees with the builder. Expected "
                        + expected + " but got " + actual);
            }
        }
    }

    // -------------------------------------------------------------------------
    // First N valid tuples, in order
    // -------------------------------------------------------------------------

    @Benchmark
    public long builderFirstN(BenchmarkState state) {
        Checksum checksum = new Checksum();
        state.builder.lexOrder().stream().limit(FIRST_N).forEach(checksum::accept);
        return checksum.value();
    }

    @Benchmark
    public long imperativeBacktracking(BenchmarkState state) {
        Checksum checksum = imperativeFirstN(state.scenario, true, new Budget());
        return checksum == null ? CAPPED : checksum.value();
    }

    @Benchmark
    public long imperativeChecksumOnly(BenchmarkState state) {
        Checksum checksum = imperativeFirstN(state.scenario, false, new Budget());
        return checksum == null ? CAPPED : checksum.value();
    }

    @Benchmark
    public long javaStreamFilter(BenchmarkState state) {
        Checksum checksum = naiveFirstN(state.scenario, new Budget());
        return checksum == null ? CAPPED : checksum.value();
    }

    // -------------------------------------------------------------------------
    // Count
    // -------------------------------------------------------------------------

    @Benchmark
    public BigInteger builderBuildAndCount(BenchmarkState state) {
        return state.scenario.builder().count();
    }

    @Benchmark
    public long imperativeCount(BenchmarkState state) {
        Long count = imperativeCount(state.scenario, new Budget());
        return count == null ? CAPPED : count;
    }

    // -------------------------------------------------------------------------
    // Rank access and sampling (no enumeration-based baseline)
    // -------------------------------------------------------------------------

    @Benchmark
    public long builderUnrank(BenchmarkState state) {
        BigInteger rank = state.ranks[state.nextRank++ & 255];
        Checksum checksum = new Checksum();
        state.builder.byRanks(List.of(rank)).stream().forEach(checksum::accept);
        return checksum.value();
    }

    @Benchmark
    public long builderSample100(BenchmarkState state) {
        Checksum checksum = new Checksum();
        state.builder.sample(100, new Random(5)).stream().forEach(checksum::accept);
        return checksum.value();
    }

    // -------------------------------------------------------------------------
    // Baselines
    // -------------------------------------------------------------------------

    /** Hand-written backtracking, pruning at the earliest dimension where a rule can be checked. */
    static Checksum imperativeFirstN(Scenario sc, boolean materialise, Budget budget) {
        Checksum checksum = new Checksum();
        try {
            search(sc, 0, new int[sc.n], materialise, checksum, budget);
        } catch (WorkLimitExceeded capped) {
            return null;
        }
        return checksum;
    }

    private static boolean search(Scenario sc, int d, int[] t, boolean materialise, Checksum checksum, Budget budget) {
        if (d == sc.n) {
            if (materialise) {
                Object[] items = new Object[sc.n];
                for (int i = 0; i < sc.n; i++) items[i] = sc.lists.get(i).get(t[i]);
                checksum.accept(Tuple.wrap(items));
            } else {
                checksum.accept(t);
            }
            return checksum.count >= FIRST_N;
        }
        outer:
        for (int v = 0; v < sc.s; v++) {
            budget.spend();
            for (int e : sc.below[d]) {
                if (!sc.ok[e][t[sc.ea[e]]][v]) continue outer;
            }
            t[d] = v;
            if (search(sc, d + 1, t, materialise, checksum, budget)) return true;
        }
        return false;
    }

    /** Counts valid tuples by backtracking; null if the work budget is exceeded. */
    static Long imperativeCount(Scenario sc, Budget budget) {
        try {
            return countFrom(sc, 0, new int[sc.n], budget);
        } catch (WorkLimitExceeded capped) {
            return null;
        }
    }

    private static long countFrom(Scenario sc, int d, int[] t, Budget budget) {
        if (d == sc.n) return 1;
        long total = 0;
        outer:
        for (int v = 0; v < sc.s; v++) {
            budget.spend();
            for (int e : sc.below[d]) {
                if (!sc.ok[e][t[sc.ea[e]]][v]) continue outer;
            }
            t[d] = v;
            total += countFrom(sc, d + 1, t, budget);
        }
        return total;
    }

    /**
     * The usual stream.filter approach: every tuple of the full product in lexicographic order, then filter, then
     * limit. Tuples are produced one at a time (no intermediate lists), so this is the cheapest version of it.
     */
    static Checksum naiveFirstN(Scenario sc, Budget budget) {
        Checksum checksum = new Checksum();
        try {
            Iterator<List<Integer>> all = new FullProduct(sc, budget);
            Stream<List<Integer>> stream = StreamSupport.stream(
                    java.util.Spliterators.spliteratorUnknownSize(all, java.util.Spliterator.ORDERED), false);
            stream.filter(sc::valid).limit(FIRST_N).forEach(checksum::accept);
        } catch (WorkLimitExceeded capped) {
            return null;
        }
        return checksum;
    }

    /** Odometer over the full product; each step counts against the work budget. */
    private static final class FullProduct implements Iterator<List<Integer>> {
        private final Scenario sc;
        private final Budget budget;
        private final int[] t;
        private boolean started;
        private boolean done;

        FullProduct(Scenario sc, Budget budget) {
            this.sc = sc;
            this.budget = budget;
            this.t = new int[sc.n];
        }

        @Override
        public boolean hasNext() {
            return !done;
        }

        @Override
        public List<Integer> next() {
            if (done) throw new NoSuchElementException();
            if (started) {
                int d = sc.n - 1;
                while (d >= 0 && ++t[d] == sc.s) t[d--] = 0;
                if (d < 0) {
                    done = true;
                    throw new NoSuchElementException();
                }
            }
            started = true;
            budget.spend();
            Object[] items = new Object[sc.n];
            for (int i = 0; i < sc.n; i++) items[i] = sc.lists.get(i).get(t[i]);
            return Tuple.wrap(items);
        }
    }

    // -------------------------------------------------------------------------
    // Scenario: reproducible rules, available as a builder and as a predicate
    // -------------------------------------------------------------------------

    static final class Scenario {

        final int n;
        final int s;
        final List<List<Integer>> lists = new ArrayList<>();
        final int[] ea;
        final int[] eb;
        final boolean[][][] ok;      // ok[edge][x][y]: value y of dimension eb allowed when dimension ea has x
        final int[][] below;         // below[d]: edges whose later dimension is d

        private Scenario(String shape, int n, int s, double density, long seed) {
            this.n = n;
            this.s = s;
            for (int d = 0; d < n; d++) {
                List<Integer> values = new ArrayList<>(s);
                for (int i = 0; i < s; i++) values.add(i);
                lists.add(values);               // distinct instances: rules identify a dimension by its list
            }

            List<int[]> edges = new ArrayList<>();
            switch (shape) {
                case "CHAIN" -> {
                    for (int i = 0; i + 1 < n; i++) edges.add(new int[]{i, i + 1});
                }
                case "STAR_FIRST" -> {
                    for (int i = 1; i < n; i++) edges.add(new int[]{0, i});
                }
                case "STAR_LAST" -> {
                    for (int i = 0; i + 1 < n; i++) edges.add(new int[]{i, n - 1});
                }
                case "SHUFFLED_TREE" -> {
                    Random random = new Random(seed);
                    int[] label = new int[n];
                    for (int i = 0; i < n; i++) label[i] = i;
                    for (int i = n - 1; i > 0; i--) {
                        int j = random.nextInt(i + 1);
                        int tmp = label[i];
                        label[i] = label[j];
                        label[j] = tmp;
                    }
                    for (int i = 1; i < n; i++) {
                        int parent = random.nextInt(i);
                        edges.add(new int[]{Math.min(label[i], label[parent]), Math.max(label[i], label[parent])});
                    }
                }
                case "CYCLE" -> {
                    for (int i = 0; i + 1 < n; i++) edges.add(new int[]{i, i + 1});
                    edges.add(new int[]{0, n - 1});
                }
                case "CYCLE_TAIL" -> {
                    edges.add(new int[]{0, 1});
                    edges.add(new int[]{1, 2});
                    edges.add(new int[]{0, 2});
                    for (int i = 2; i + 1 < n; i++) edges.add(new int[]{i, i + 1});
                }
                default -> throw new IllegalArgumentException("Unknown shape " + shape);
            }

            ea = new int[edges.size()];
            eb = new int[edges.size()];
            ok = new boolean[edges.size()][s][s];
            for (int e = 0; e < edges.size(); e++) {
                ea[e] = edges.get(e)[0];
                eb[e] = edges.get(e)[1];
                Random random = new Random(seed * 1_000 + e);
                for (int x = 0; x < s; x++) {
                    boolean any = false;
                    for (int y = 0; y < s; y++) {
                        ok[e][x][y] = random.nextDouble() < density;
                        any |= ok[e][x][y];
                    }
                    if (!any) ok[e][x][random.nextInt(s)] = true;   // a rule must allow at least one value
                }
            }

            below = new int[n][];
            for (int d = 0; d < n; d++) {
                List<Integer> list = new ArrayList<>();
                for (int e = 0; e < ea.length; e++) if (eb[e] == d) list.add(e);
                below[d] = list.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        /** Parses names such as {@code STAR_LAST_7x15_d20}. */
        static Scenario parse(String name) {
            int last = name.lastIndexOf('_');
            int middle = name.lastIndexOf('_', last - 1);
            String shape = name.substring(0, middle);
            String[] size = name.substring(middle + 1, last).split("x");
            int density = Integer.parseInt(name.substring(last + 2));
            return new Scenario(shape, Integer.parseInt(size[0]), Integer.parseInt(size[1]), density / 100.0, 42);
        }

        /** The rules through the public API: one rule per (linked pair, value of the first dimension). */
        ConditionalProductBuilder builder() {
            ConditionalProductBuilder b = JNumberTools.cartesianProduct().conditionalProductOf(lists.get(0));
            for (int d = 1; d < n; d++) b = b.and(lists.get(d));
            for (int e = 0; e < ea.length; e++) {
                for (int x = 0; x < s; x++) {
                    List<Integer> allowed = new ArrayList<>();
                    for (int y = 0; y < s; y++) if (ok[e][x][y]) allowed.add(y);
                    if (allowed.size() == s) continue;           // allows everything: no rule needed
                    b = b.when(lists.get(ea[e])).is(x).then(lists.get(eb[e])).isOneOf(allowed);
                }
            }
            return b;
        }

        boolean valid(List<?> tuple) {
            for (int e = 0; e < ea.length; e++) {
                if (!ok[e][(Integer) tuple.get(ea[e])][(Integer) tuple.get(eb[e])]) return false;
            }
            return true;
        }

        BigInteger fullProduct() {
            return BigInteger.valueOf(s).pow(n);
        }
    }

    // -------------------------------------------------------------------------
    // Support types
    // -------------------------------------------------------------------------

    /** Order-sensitive checksum, so a wrong tuple or a wrong order changes the result. */
    static final class Checksum {
        long hash = 1;
        long count;

        void accept(List<?> tuple) {
            long h = 0;
            for (Object value : tuple) h = h * 31 + (Integer) value;
            mix(h);
        }

        void accept(int[] tuple) {
            long h = 0;
            for (int value : tuple) h = h * 31 + value;
            mix(h);
        }

        private void mix(long tupleHash) {
            hash = hash * 1_000_003 + tupleHash;
            count++;
        }

        long value() {
            return hash ^ count;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Checksum that && hash == that.hash && count == that.count;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(hash) * 31 + Long.hashCode(count);
        }

        @Override
        public String toString() {
            return "tuples=" + count + ", hash=" + hash;
        }
    }

    static final class Budget {
        private long left = MAX_EXPANSIONS;

        void spend() {
            if (--left < 0) throw new WorkLimitExceeded();
        }
    }

    static final class WorkLimitExceeded extends RuntimeException {
        private static final long serialVersionUID = 1L;

        WorkLimitExceeded() {
            super(null, null, false, false);       // no stack trace: this is control flow
        }
    }

    /** Convenience launcher: runs only this benchmark unless other arguments are given. */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{ConditionalProductComplexPathBenchmark.class.getSimpleName()};
        }
        org.openjdk.jmh.Main.main(args);
    }
}