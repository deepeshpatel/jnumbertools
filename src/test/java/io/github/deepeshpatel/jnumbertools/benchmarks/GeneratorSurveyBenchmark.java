/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.MultisetBuilder;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Survey of every generator family with the same three operations, to show which one costs the most per
 * result and is worth optimising. All elements are {@code Integer}.
 *
 * <ul>
 *   <li>{@code lexOrder1000}: the first 1,000 results in lexicographic order (the iteration cost);
 *       divide the score by 1,000 for the cost per result</li>
 *   <li>{@code lexOrderMth100}: 100 results spread over the whole space with {@code lexOrderMth}
 *       (the unranking cost); divide by 100</li>
 *   <li>{@code choice100}: 100 random results with {@code choice} (unranking plus random ranks); divide by 100</li>
 * </ul>
 *
 * (Multiset combinations return a map of counts per result instead of a list; the map entries are read.)
 * Each result is read element by element into a checksum, so reading is part of the cost. Run with
 * {@code -prof gc} to see allocation per operation. The trial setup checks that the space has at least
 * 1,000 results and that each operation yields the expected number.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 8, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public class GeneratorSurveyBenchmark {

    private static final int FIRST_N = 1_000;
    private static final int SPREAD_N = 100;

    @State(Scope.Thread)
    public static class BenchmarkState {

        @Param({
                "permutation-unique",        // 10 items: 3,628,800
                "permutation-k",             // 12 P 4: 11,880
                "permutation-repetitive",    // 10 items, length 4: 10,000
                "permutation-multiset",      // 3,3,3,2 copies of four items
                "combination-unique",        // 20 C 8: 125,970
                "combination-repetitive",    // 10 items, size 6: 5,005
                "combination-multiset",      // 10 items, 3 copies each, size 8
                "derangement",               // 9 items: 133,496
                "subsets",                   // 15 items, every size: 32,768
                "product-simple",            // 3 x 10: 1,000 (reference)
                "product-constrained"        // 1 of 10 from each of 3 lists: 1,000 (reference)
        })
        public String generator;

        Source source;
        BigInteger step;

        @Setup(Level.Trial)
        public void setup() {
            source = create(generator);
            BigInteger count = source.count();
            if (count.compareTo(BigInteger.valueOf(FIRST_N)) < 0) {
                throw new IllegalStateException(generator + " has only " + count + " results");
            }
            step = count.divide(BigInteger.valueOf(SPREAD_N)).max(BigInteger.ONE);
            if (count(source.lexOrder(), FIRST_N) != FIRST_N
                    || count(source.lexOrderMth(step), SPREAD_N) != SPREAD_N
                    || count(source.choice(SPREAD_N, new Random(1)), SPREAD_N) != SPREAD_N) {
                throw new IllegalStateException(generator + " did not yield the expected number of results");
            }
        }

        private static int count(Iterable<?> results, int limit) {
            int n = 0;
            for (Object ignored : results) {
                if (++n == limit) break;
            }
            return n;
        }

        private static Source create(String name) {
            if (name.equals("combination-multiset")) {
                MultisetBuilder<Integer> multiset = JNumberTools.combinations()
                        .multiset(multiset(10, new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3}), 8);
                return new Source() {
                    public BigInteger count() { return multiset.count(); }
                    public Iterable<?> lexOrder() { return multiset.lexOrder(); }
                    public Iterable<?> lexOrderMth(BigInteger step) { return multiset.lexOrderMth(step, BigInteger.ZERO); }
                    public Iterable<?> choice(int n, Random random) { return multiset.choice(n, random); }
                };
            }
            Builder<?> builder = createBuilder(name);
            return new Source() {
                public BigInteger count() { return builder.count(); }
                public Iterable<?> lexOrder() { return builder.lexOrder(); }
                public Iterable<?> lexOrderMth(BigInteger step) { return builder.lexOrderMth(step, BigInteger.ZERO); }
                public Iterable<?> choice(int n, Random random) { return builder.choice(n, random); }
            };
        }

        private static Builder<?> createBuilder(String name) {
            return switch (name) {
                case "permutation-unique" -> JNumberTools.permutations().unique(10);
                case "permutation-k" -> JNumberTools.permutations().nPk(12, 4);
                case "permutation-repetitive" -> JNumberTools.permutations().repetitive(4, 10);
                case "permutation-multiset" -> JNumberTools.permutations().multiset(multiset(4, new int[]{3, 3, 3, 2}));
                case "combination-unique" -> JNumberTools.combinations().unique(20, 8);
                case "combination-repetitive" -> JNumberTools.combinations().repetitive(10, 6);
                case "derangement" -> JNumberTools.derangements().of(9);
                case "subsets" -> JNumberTools.subsets().of(15).all();
                case "product-simple" -> JNumberTools.cartesianProduct()
                        .simpleProductOf(range(10)).and(range(10)).and(range(10));
                case "product-constrained" -> JNumberTools.cartesianProduct()
                        .constrainedProductOfDistinct(1, range(10)).andDistinct(1, range(10)).andDistinct(1, range(10));
                default -> throw new IllegalArgumentException(name);
            };
        }

        private static LinkedHashMap<Integer, Integer> multiset(int items, int[] copies) {
            LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < items; i++) map.put(i, copies[i]);
            return map;
        }

        private static List<Integer> range(int n) {
            return java.util.stream.IntStream.range(0, n).boxed().toList();
        }
    }

    /** The three operations, for builders with different result types. */
    interface Source {
        BigInteger count();
        Iterable<?> lexOrder();
        Iterable<?> lexOrderMth(BigInteger step);
        Iterable<?> choice(int sampleSize, Random random);
    }

    @Benchmark
    public long lexOrder1000(BenchmarkState state) {
        return checksum(state.source.lexOrder(), FIRST_N);
    }

    @Benchmark
    public long lexOrderMth100(BenchmarkState state) {
        return checksum(state.source.lexOrderMth(state.step), SPREAD_N);
    }

    @Benchmark
    public long choice100(BenchmarkState state) {
        return checksum(state.source.choice(SPREAD_N, new Random(1)), SPREAD_N);
    }

    private static long checksum(Iterable<?> results, int limit) {
        long hash = 1;
        int n = 0;
        for (Object result : results) {
            if (result instanceof List<?> list) {
                for (Object value : list) hash = hash * 31 + (Integer) value;
            } else {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) result).entrySet()) {
                    hash = hash * 31 + (Integer) entry.getKey();
                    hash = hash * 31 + (Integer) entry.getValue();
                }
            }
            hash = hash * 31 + 1;
            if (++n == limit) break;
        }
        return hash;
    }
}