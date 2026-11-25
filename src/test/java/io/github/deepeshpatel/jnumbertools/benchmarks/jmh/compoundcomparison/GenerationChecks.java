/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.compoundcomparison;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Correctness checks shared by the compound generation benchmarks.
 *
 * <p>A result is summarised by the number of tuples and two order-independent hashes, so sequential, parallel,
 * ordered and unordered traversals can be compared without relying on encounter order. The expected summary comes
 * from {@link #oracle}, which tests every tuple of the full product against a plain predicate and does not use
 * JNumberTools at all.</p>
 */
final class GenerationChecks {

    /** Tuple count plus two order-independent hashes of the tuples. */
    record Summary(long count, long sum, long xor) {
    }

    private GenerationChecks() {
    }

    /** Summarises a stream. The accumulator is a combiner-safe reduction, so a parallel stream gives the same result. */
    static Summary summarize(Stream<List<Object>> tuples) {
        long[] total = tuples.collect(
                () -> new long[3],
                (acc, tuple) -> add(acc, tuple.hashCode()),
                (left, right) -> {
                    left[0] += right[0];
                    left[1] += right[1];
                    left[2] ^= right[2];
                });
        return new Summary(total[0], total[1], total[2]);
    }

    /**
     * The expected summary, by testing every tuple of the product whose dimensions are {@code 0..sizes[i]-1}.
     * The hash of a tuple is the {@link List#hashCode()} of its values, which is what the library's tuples report.
     */
    static Summary oracle(int[] sizes, Predicate<int[]> valid) {
        long[] total = new long[3];
        int[] tuple = new int[sizes.length];
        while (true) {
            if (valid.test(tuple)) {
                int hash = 1;
                for (int value : tuple) hash = 31 * hash + value;
                add(total, hash);
            }
            int position = sizes.length - 1;
            while (position >= 0 && ++tuple[position] == sizes[position]) tuple[position--] = 0;
            if (position < 0) return new Summary(total[0], total[1], total[2]);
        }
    }

    /** The sum of the tuples' list hash codes, which is what the timed benchmark methods return. */
    static long hashSum(int[] sizes, Predicate<int[]> valid) {
        long sum = 0;
        int[] tuple = new int[sizes.length];
        while (true) {
            if (valid.test(tuple)) {
                int hash = 1;
                for (int value : tuple) hash = 31 * hash + value;
                sum += hash;
            }
            int position = sizes.length - 1;
            while (position >= 0 && ++tuple[position] == sizes[position]) tuple[position--] = 0;
            if (position < 0) return sum;
        }
    }

    static void require(String scenario, String label, Summary expected, Summary actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Wrong result for " + label + " in scenario " + scenario
                    + ": expected " + expected + " but got " + actual);
        }
    }

    static void require(String scenario, String label, long expected, long actual) {
        if (expected != actual) {
            throw new IllegalStateException("Wrong result for " + label + " in scenario " + scenario
                    + ": expected " + expected + " but got " + actual);
        }
    }

    private static void add(long[] total, int tupleHash) {
        long hash = mix64(tupleHash);
        total[0]++;
        total[1] += hash;
        total[2] ^= Long.rotateLeft(hash, (int) (hash & 63));
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
