/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Combination;

import io.github.deepeshpatel.jnumbertools.AllocationMeter;
import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves that unranking a combination in a space too large for a long (estimate from a logarithm table, then exact
 * confirmation) gives exactly the combination the plain binary search over exact binomials gives, and needs far less
 * work to do it.
 */
class CombinadicEstimateUnrankTest {

    private final Calculator calculator = new Calculator();
    private final CombinadicAlgorithms algorithms = new CombinadicAlgorithms(calculator);

    // Spaces beyond 62 bits, so the public unRank takes the estimate path, plus ones that are not so large.
    private static final int[][] SHAPES = {
            {70, 30}, {100, 40}, {200, 50}, {500, 100}, {1000, 20}, {4000, 12}, {4096, 30}, {300, 255}, {90, 45}, {64, 32}, {2000, 7}
    };

    private int[] viaEstimate(BigInteger rank, BigInteger total, int n, int r) {
        return algorithms.unRankByEstimate(rank, total, n, r);
    }

    private int[] viaSearch(BigInteger rank, BigInteger total, int n, int r) {
        return algorithms.unRankBySearch(rank, total, n, r);
    }

    @Test
    void everyRankOfSmallSpacesMatchesTheSearchAndAnIndependentEnumeration() {
        for (int[] shape : new int[][]{{1, 1}, {5, 0}, {5, 5}, {6, 1}, {7, 3}, {10, 4}, {12, 6}, {14, 13}, {16, 8}}) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            List<int[]> lexicographic = new ArrayList<>();
            enumerate(n, r, 0, new int[r], 0, lexicographic);
            assertEquals(total.intValueExact(), lexicographic.size(), n + "C" + r);
            for (int rank = 0; rank < lexicographic.size(); rank++) {
                BigInteger bigRank = BigInteger.valueOf(rank);
                assertArrayEquals(lexicographic.get(rank), viaEstimate(bigRank, total, n, r), n + "C" + r + " rank " + rank);
                assertArrayEquals(viaSearch(bigRank, total, n, r), viaEstimate(bigRank, total, n, r), n + "C" + r + " rank " + rank);
            }
        }
    }

    private static void enumerate(int n, int r, int from, int[] current, int depth, List<int[]> out) {
        if (depth == r) {
            out.add(current.clone());
            return;
        }
        for (int v = from; v <= n - (r - depth); v++) {
            current[depth] = v;
            enumerate(n, r, v + 1, current, depth + 1, out);
        }
    }

    @Test
    void randomRanksMatchTheSearchOnLargeSpaces() {
        Random random = new Random(42);
        for (int[] shape : SHAPES) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            for (int i = 0; i < 300; i++) {
                BigInteger rank = new BigInteger(total.bitLength() + 8, random).mod(total);
                assertArrayEquals(viaSearch(rank, total, n, r), viaEstimate(rank, total, n, r), n + "C" + r + " rank " + rank);
            }
        }
    }

    @Test
    void firstLastAndMiddleRanksMatch() {
        for (int[] shape : SHAPES) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            for (BigInteger rank : new BigInteger[]{BigInteger.ZERO, BigInteger.ONE, total.shiftRight(1),
                    total.subtract(BigInteger.TWO), total.subtract(BigInteger.ONE)}) {
                assertArrayEquals(viaSearch(rank, total, n, r), viaEstimate(rank, total, n, r), n + "C" + r + " rank " + rank);
            }
        }
    }

    @Test
    void ranksOnTheExactBoundaryOfABinomialMatch() {
        // The estimate is a float; the exact check must settle the cases where remaining is C(c,k) or C(c,k) - 1.
        Random random = new Random(7);
        for (int[] shape : SHAPES) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            for (int i = 0; i < 150; i++) {
                int k = 1 + random.nextInt(r);
                int c = k + random.nextInt(n - k);
                BigInteger binomial = calculator.nCr(c, k);
                // remaining = total - 1 - rank, so these ranks give remaining = C(c,k) and C(c,k) - 1
                for (BigInteger remaining : new BigInteger[]{binomial, binomial.subtract(BigInteger.ONE), binomial.add(BigInteger.ONE)}) {
                    BigInteger rank = total.subtract(BigInteger.ONE).subtract(remaining);
                    if (rank.signum() < 0 || rank.compareTo(total) >= 0) continue;
                    assertArrayEquals(viaSearch(rank, total, n, r), viaEstimate(rank, total, n, r),
                            n + "C" + r + " remaining " + remaining + " (c=" + c + ", k=" + k + ")");
                }
            }
        }
    }

    @Test
    void publicApiRoundTripsOnSpacesBeyondALong() {
        Random random = new Random(3);
        for (int[] shape : SHAPES) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            if (total.bitLength() <= 62 || r >= 256) continue;    // those use the long path and the walk
            for (int i = 0; i < 100; i++) {
                BigInteger rank = new BigInteger(total.bitLength() + 8, random).mod(total);
                int[] combination = JNumberTools.unrankOf().combination().unique(rank, n, r);
                assertArrayEquals(viaSearch(rank, total, n, r), combination, n + "C" + r);
                assertEquals(rank, JNumberTools.rankOf().combination().unique(n, combination), n + "C" + r);
            }
        }
    }

    @Test
    void generatorByRanksAgreesWithLexOrderMthOnALargeSpace() {
        var builder = Combination.unique(200, 50);
        BigInteger total = builder.count();
        Random random = new Random(9);
        for (int i = 0; i < 50; i++) {
            BigInteger rank = new BigInteger(total.bitLength() + 8, random).mod(total);
            List<Integer> byRanks = builder.byRanks(List.of(rank)).iterator().next();
            List<Integer> mth = builder.lexOrderMth(BigInteger.ONE, rank).iterator().next();
            assertEquals(mth, byRanks);
            assertEquals(50, byRanks.size());
            assertTrue(byRanks.stream().distinct().count() == 50);
        }
    }

    @Test
    void sizesAtAndAroundTheTableLimitAreHandled() {
        for (int n : new int[]{LogFactorials.MAX_N - 1, LogFactorials.MAX_N, LogFactorials.MAX_N + 1, LogFactorials.MAX_N + 50}) {
            int r = 10;
            BigInteger total = calculator.nCr(n, r);
            Random random = new Random(n);
            for (int i = 0; i < 50; i++) {
                BigInteger rank = new BigInteger(total.bitLength() + 8, random).mod(total);
                // the public method must pick the estimate up to the limit and the search above it, with one answer
                assertArrayEquals(viaSearch(rank, total, n, r), algorithms.unRank(rank, total, n, r), "n=" + n);
            }
        }
    }

    @Test
    void logFactorialTableHoldsTheLogarithmsOfFactorials() {
        double[] table = LogFactorials.upTo(300);
        assertTrue(table.length > 300);
        BigInteger factorial = BigInteger.ONE;
        for (int i = 1; i <= 300; i++) {
            factorial = factorial.multiply(BigInteger.valueOf(i));
            int shift = Math.max(0, factorial.bitLength() - 900);
            double expected = Math.log(factorial.shiftRight(shift).doubleValue()) + shift * Math.log(2);
            assertEquals(expected, table[i], 1e-9 * Math.max(1, expected), "ln(" + i + "!)");
        }
        assertEquals(0.0, table[0]);
        assertEquals(0.0, table[1]);
    }

    @Test
    void logFactorialTableCanBeGrownFromSeveralThreadsAtOnce() throws Exception {
        double[] reference = LogFactorials.upTo(LogFactorials.MAX_N);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                final int seed = t;
                results.add(pool.submit(() -> {
                    Random random = new Random(seed);
                    for (int i = 0; i < 2000; i++) {
                        int n = random.nextInt(LogFactorials.MAX_N + 1);
                        double[] table = LogFactorials.upTo(n);
                        if (table.length <= n || table[n] != reference[n]) return false;
                    }
                    return true;
                }));
            }
            for (Future<Boolean> result : results) assertTrue(result.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void allocatesLessThanTheExactSearch() {
        Assumptions.assumeTrue(AllocationMeter.supported(), "per-thread allocation is not reported by this JVM");
        Random random = new Random(5);
        for (int[] shape : new int[][]{{200, 50}, {1000, 20}, {500, 100}, {4000, 12}}) {
            int n = shape[0], r = shape[1];
            BigInteger total = calculator.nCr(n, r);
            List<BigInteger> ranks = new ArrayList<>();
            for (int i = 0; i < 100; i++) ranks.add(new BigInteger(total.bitLength() + 8, random).mod(total));
            int[] sink = new int[1];
            double search = AllocationMeter.bytesPerCall(() -> {
                for (BigInteger rank : ranks) sink[0] += viaSearch(rank, total, n, r)[0];
            }, 100, 20);
            double estimate = AllocationMeter.bytesPerCall(() -> {
                for (BigInteger rank : ranks) sink[0] += viaEstimate(rank, total, n, r)[0];
            }, 100, 20);
            // measured 2x to 6x less; the margin absorbs JIT and cache-state variation between runs
            assertTrue(estimate * 1.5 < search,
                    n + "C" + r + ": estimate " + estimate + " B vs search " + search + " B per 100 unranks");
        }
    }
}
