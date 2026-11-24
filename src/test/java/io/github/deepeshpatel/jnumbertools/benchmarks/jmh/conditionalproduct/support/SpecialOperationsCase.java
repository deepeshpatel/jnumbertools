/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.support;

import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.baseline.ImperativeBacktrackingBaseline;
import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario.Scenario;
import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.scenario.Scenario.ScenarioData;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * One workload for the special operations: {@code count}, {@code isEmpty}, {@code lexOrderMth}, {@code byRanks},
 * {@code choice} and {@code sample}. Each measured call includes builder and rule construction, like the full
 * traversal benchmark.
 *
 * <p>{@code LIBRARY} calls the builder. {@code ENUMERATION} is the best a caller can do without the library: a
 * non-allocating imperative enumeration (materialising every tuple first where sampling needs them). Enumeration is
 * only feasible for bounded workloads; the {@code LARGE} workloads (up to about 1e40 tuples) are library-only.</p>
 */
public final class SpecialOperationsCase {
    public static final String LIBRARY = "LIBRARY";
    public static final String ENUMERATION = "ENUMERATION";
    public static final List<String> OPERATIONS =
            List.of("count", "isEmpty", "lexOrderMth", "byRanks", "choice", "sample");

    /** Tuples requested by every rank and sampling operation. */
    static final int K = 50;
    private static final long SEED = 20_260_618L;

    private final String caseId;
    private final ScenarioData scenario;
    private final List<ConditionalProductCaseSupport.PreparedRule> preparedRules;
    private final boolean bounded;
    private final BigInteger count;
    private final BigInteger every;
    private final List<BigInteger> ranks;

    public SpecialOperationsCase(String scenarioName) {
        int separator = scenarioName.lastIndexOf('-');
        String topology = scenarioName.substring(0, separator);
        String shape = scenarioName.substring(separator + 1);
        if (topology.equals("DEAD_END")) topology = "CONTROLLED_DEAD_END";
        this.caseId = "phase-e/" + topology + "/" + shape;
        this.scenario = Scenario.create(caseId);
        this.preparedRules = ConditionalProductCaseSupport.prepareRules(scenario);
        this.bounded = !topology.equals("LARGE");
        this.count = freshBuilder().count();
        this.every = count.divide(BigInteger.valueOf(K)).max(BigInteger.ONE);
        List<BigInteger> r = new ArrayList<>();
        BigInteger step = count.divide(BigInteger.valueOf(K));
        for (int i = 0; i < K && count.signum() > 0; i++) {
            BigInteger rank = step.signum() == 0 ? BigInteger.valueOf(i) : step.multiply(BigInteger.valueOf(i));
            if (rank.compareTo(count) < 0) r.add(rank);
        }
        this.ranks = List.copyOf(r);
    }

    private ConditionalProductBuilder freshBuilder() {
        return ConditionalProductCaseSupport.buildBuilder(scenario, preparedRules);
    }

    public boolean bounded() {
        return bounded;
    }

    /** Runs one operation end to end and returns a value derived from the result (so it cannot be optimised away). */
    public long run(String operation, String implementation) {
        return implementation.equals(LIBRARY) ? library(operation) : enumeration(operation);
    }

    private long library(String operation) {
        ConditionalProductBuilder b = freshBuilder();
        return switch (operation) {
            case "count" -> b.count().hashCode();
            case "isEmpty" -> b.isEmpty() ? 1 : 0;
            case "lexOrderMth" -> checksum(b.lexOrderMth(every, BigInteger.ZERO));
            case "byRanks" -> checksum(b.byRanks(ranks));
            case "choice" -> checksum(b.choice(K, new Random(SEED)));
            case "sample" -> checksum(b.sample(K, new Random(SEED)));
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private long enumeration(String operation) {
        if (!bounded) throw new IllegalStateException("Enumeration is not feasible for " + caseId);
        ImperativeBacktrackingBaseline.Plan plan = ImperativeBacktrackingBaseline.compile(scenario);
        switch (operation) {
            case "count":
                return ImperativeBacktrackingBaseline.count(plan);
            case "isEmpty":
                return ImperativeBacktrackingBaseline.isEmpty(plan) ? 1 : 0;
            case "lexOrderMth":
                return ImperativeBacktrackingBaseline.everyMth(plan, every.longValueExact(), 0);
            case "byRanks":
                return ImperativeBacktrackingBaseline.atRanks(plan, ranks.stream().mapToLong(BigInteger::longValueExact).toArray());
            case "choice": {
                int[][] all = ImperativeBacktrackingBaseline.materialize(plan);
                Random random = new Random(SEED);
                long hash = 1;
                for (int i = 0; i < K; i++) hash = ImperativeBacktrackingBaseline.mix(hash, all[random.nextInt(all.length)]);
                return hash;
            }
            case "sample": {
                int[][] all = ImperativeBacktrackingBaseline.materialize(plan);
                Random random = new Random(SEED);
                long hash = 1;
                int picks = Math.min(K, all.length);
                for (int i = 0; i < picks; i++) {   // partial Fisher-Yates: k distinct tuples
                    int j = i + random.nextInt(all.length - i);
                    int[] chosen = all[j];
                    all[j] = all[i];
                    all[i] = chosen;
                    hash = ImperativeBacktrackingBaseline.mix(hash, chosen);
                }
                return hash;
            }
            default:
                throw new IllegalArgumentException(operation);
        }
    }

    /**
     * Checks the library result for one operation before measuring. Bounded workloads are compared with the
     * enumeration baseline; every workload is checked for valid tuples, expected counts, ascending order for the
     * rank operations, and distinct tuples for {@code sample}.
     */
    public void verify(String operation) {
        ConditionalProductBuilder b = freshBuilder();
        if (bounded) {
            ImperativeBacktrackingBaseline.Plan plan = ImperativeBacktrackingBaseline.compile(scenario);
            long expected = ImperativeBacktrackingBaseline.count(plan);
            require(count.equals(BigInteger.valueOf(expected)), "count differs from enumeration");
            switch (operation) {
                case "isEmpty" -> require(b.isEmpty() == ImperativeBacktrackingBaseline.isEmpty(plan), "isEmpty differs");
                case "lexOrderMth" -> require(library(operation) == enumeration(operation), "lexOrderMth differs");
                case "byRanks" -> require(library(operation) == enumeration(operation), "byRanks differs");
                default -> { }
            }
        } else {
            BigInteger expected = largeCount();
            require(count.equals(expected), "count differs from the closed form " + expected);
        }
        switch (operation) {
            case "lexOrderMth" -> checkValidAscending(b.lexOrderMth(every, BigInteger.ZERO), -1);
            case "byRanks" -> checkValidAscending(b.byRanks(ranks), ranks.size());
            case "choice" -> checkValid(b.choice(K, new Random(SEED)), K, false);
            case "sample" -> checkValid(b.sample(K, new Random(SEED)), Math.min(K, count.min(BigInteger.valueOf(K)).intValue()), true);
            default -> { }
        }
    }

    /** {@code size * (size - 1) ^ (dimensions - 1)} for the LARGE chain. */
    private BigInteger largeCount() {
        int n = scenario.dimensions().size();
        BigInteger size = BigInteger.valueOf(scenario.dimensions().get(0).size());
        return size.multiply(size.subtract(BigInteger.ONE).pow(n - 1));
    }

    private void checkValid(Iterable<?> tuples, int expectedSize, boolean distinct) {
        Set<List<?>> seen = new HashSet<>();
        int n = 0;
        for (Object t : tuples) {
            List<?> list = (List<?>) t;
            require(isValid(list), "invalid tuple " + list);
            seen.add(list);
            n++;
        }
        require(n == expectedSize, "expected " + expectedSize + " tuples but got " + n);
        if (distinct) require(seen.size() == n, "sample returned duplicates");
    }

    private void checkValidAscending(Iterable<?> tuples, int expectedSize) {
        List<?> previous = null;
        int n = 0;
        for (Object t : tuples) {
            List<?> list = (List<?>) t;
            require(isValid(list), "invalid tuple " + list);
            if (previous != null) require(compare(previous, list) < 0, "tuples are not ascending");
            previous = list;
            n++;
        }
        require(n > 0, "no tuples returned");
        if (expectedSize >= 0) require(n == expectedSize, "expected " + expectedSize + " tuples but got " + n);
    }

    private static int compare(List<?> a, List<?> b) {
        for (int i = 0; i < a.size(); i++) {
            int c = Integer.compare((Integer) a.get(i), (Integer) b.get(i));
            if (c != 0) return c;
        }
        return 0;
    }

    private boolean isValid(List<?> tuple) {
        if (tuple.size() != scenario.dimensions().size()) return false;
        for (Rule rule : scenario.rules()) {
            int trigger = (Integer) tuple.get(rule.triggerDimension());
            int target = (Integer) tuple.get(rule.targetDimension());
            if (rule.triggerValues().get(trigger) && !rule.allowedValues().get(target)) return false;
        }
        return true;
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(caseId + ": " + message);
    }

    private static long checksum(Iterable<?> tuples) {
        long hash = 1;
        for (Object tuple : tuples) {
            for (Object value : (List<?>) tuple) hash = hash * 31 + (Integer) value;
            hash = hash * 31 + 1;
        }
        return hash;
    }
}
