/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Cases where a different algorithm or data structure could be faster or slower depending on the input size, so
 * a change is kept only if it measurably wins here. Each case reads every result element by element.
 *
 * <ul>
 *   <li>{@code derangement-N-lex1000}: first 1,000 derangements of N items (availability set: Fenwick tree
 *       versus a 64-bit mask for small N)</li>
 *   <li>{@code multiset-perm-*}: multiset permutation unranking with {@code choice} and {@code lexOrderMth}
 *       (type search: linear scan versus Fenwick tree, for few and for many types)</li>
 *   <li>{@code subsets-200-choice100}: subsets of 200 items, where ranks need {@code BigInteger}</li>
 *   <li>{@code combination-30000C3-choice100}: few items from a very large list (long table size limit)</li>
 *   <li>{@code multiset-comb-lex1000}: result map of multiset combinations</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 8, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class OptimisationCandidatesBenchmark {

    @Param({
            "derangement-9-lex1000",
            "derangement-40-lex1000",
            "multiset-perm-4types-choice100",
            "multiset-perm-4types-mth100",
            "multiset-perm-4types-big-choice100",
            "multiset-perm-8types-choice100",
            "multiset-perm-16types-choice100",
            "multiset-perm-16types-mth100",
            "multiset-perm-64types-choice100",
            "multiset-perm-64types-mth100",
            "multiset-perm-256types-choice100",
            "multiset-perm-256types-mth100",
            "subsets-200-choice100",
            "combination-30000C3-choice100",
            "multiset-comb-lex1000"
    })
    public String scenario;

    private Supplier<Iterable<?>> source;
    private int limit;

    @Setup(Level.Trial)
    public void setup() {
        String[] parts = scenario.split("-");
        String op = parts[parts.length - 1];
        limit = op.endsWith("1000") ? 1000 : 100;
        source = switch (scenario) {
            case "derangement-9-lex1000" -> () -> JNumberTools.derangements().of(9).lexOrder();
            case "derangement-40-lex1000" -> () -> JNumberTools.derangements().of(40).lexOrder();
            case "multiset-perm-4types-choice100" -> multisetPerm(new int[]{3, 3, 3, 2}, true);
            case "multiset-perm-4types-mth100" -> multisetPerm(new int[]{3, 3, 3, 2}, false);
            case "multiset-perm-4types-big-choice100" -> multisetPerm(repeat(30, 4), true);
            case "multiset-perm-8types-choice100" -> multisetPerm(repeat(2, 8), true);
            case "multiset-perm-16types-choice100" -> multisetPerm(repeat(1, 16), true);
            case "multiset-perm-16types-mth100" -> multisetPerm(repeat(1, 16), false);
            case "multiset-perm-64types-choice100" -> multisetPerm(repeat(2, 64), true);
            case "multiset-perm-64types-mth100" -> multisetPerm(repeat(2, 64), false);
            case "multiset-perm-256types-choice100" -> multisetPerm(repeat(1, 256), true);
            case "multiset-perm-256types-mth100" -> multisetPerm(repeat(1, 256), false);
            case "subsets-200-choice100" -> () -> JNumberTools.subsets().of(200).all().choice(100, new Random(1));
            case "combination-30000C3-choice100" -> () -> JNumberTools.combinations().unique(30_000, 3).choice(100, new Random(1));
            case "multiset-comb-lex1000" -> () -> JNumberTools.combinations().multiset(map(repeat(3, 10)), 8).lexOrder();
            default -> throw new IllegalArgumentException(scenario);
        };
        int n = 0;
        for (Object ignored : source.get()) if (++n == limit) break;
        if (n != limit) throw new IllegalStateException(scenario + " yields only " + n);
    }

    private static Supplier<Iterable<?>> multisetPerm(int[] freq, boolean choice) {
        var builder = JNumberTools.permutations().multiset(map(freq));
        BigInteger step = builder.count().divide(BigInteger.valueOf(100)).max(BigInteger.ONE);
        return choice
                ? () -> builder.choice(100, new Random(1))
                : () -> builder.lexOrderMth(step, BigInteger.ZERO);
    }

    private static int[] repeat(int value, int times) {
        int[] a = new int[times];
        java.util.Arrays.fill(a, value);
        return a;
    }

    private static LinkedHashMap<Integer, Integer> map(int[] freq) {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < freq.length; i++) m.put(i, freq[i]);
        return m;
    }

    @Benchmark
    public long run() {
        long hash = 1;
        int n = 0;
        for (Object result : source.get()) {
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
