/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * rank/unrank of k-permutations across the shapes that take different internal paths:
 * small k relative to n (cost independent of n) and large k (pool based). Results must always be the
 * lexicographic k-permutation and must round-trip.
 */
@DisplayName("k-permutation rank/unrank: lexicographic order and round trip")
class KPermutationRankUnrankRoundTripTest {

    @Test
    @DisplayName("every rank of small (n, k) equals the independently enumerated lexicographic k-permutation")
    void matchesBruteForceEnumeration() {
        int[][] shapes = {{1, 0}, {1, 1}, {4, 4}, {5, 3}, {6, 2}, {6, 6}, {7, 4}, {8, 3}};
        for (int[] shape : shapes) {
            int n = shape[0];
            int k = shape[1];
            List<int[]> expected = new ArrayList<>();
            enumerate(n, k, new int[k], new boolean[n], 0, expected);

            assertEquals(calculator.nPr(n, k), BigInteger.valueOf(expected.size()), "count n=" + n + " k=" + k);
            for (int i = 0; i < expected.size(); i++) {
                BigInteger rank = BigInteger.valueOf(i);
                assertArrayEquals(expected.get(i), unrankOf.permutation().k(rank, n, k), "unrank n=" + n + " k=" + k + " rank=" + i);
                assertEquals(rank, rankOf.permutation().k(n, expected.get(i)), "rank n=" + n + " k=" + k + " i=" + i);
            }
        }
    }

    private static void enumerate(int n, int k, int[] current, boolean[] used, int depth, List<int[]> out) {
        if (depth == k) {
            out.add(current.clone());
            return;
        }
        for (int v = 0; v < n; v++) {
            if (!used[v]) {
                used[v] = true;
                current[depth] = v;
                enumerate(n, k, current, used, depth + 1, out);
                used[v] = false;
            }
        }
    }

    @Test
    @DisplayName("larger shapes round-trip, are valid, and the first and last ranks are the lexicographic extremes")
    void largerShapesRoundTrip() {
        Random random = new Random(5);
        // small k relative to n, around the boundary where k*k stops being small, and k close to n
        int[][] shapes = {{100, 5}, {100, 28}, {100, 60}, {1000, 90}, {1000, 400}, {9000, 3}, {9000, 250}, {9000, 400},
                {20000, 141}, {100000, 7}, {100000, 316}, {1000000, 5}, {300, 300}};
        for (int[] shape : shapes) {
            int n = shape[0];
            int k = shape[1];
            BigInteger total = calculator.nPr(n, k);

            List<BigInteger> ranks = new ArrayList<>(List.of(BigInteger.ZERO, total.subtract(BigInteger.ONE), total.shiftRight(1)));
            for (int i = 0; i < 6; i++) {
                ranks.add(new BigInteger(total.bitLength() + 8, random).mod(total));
            }
            for (BigInteger rank : ranks) {
                int[] permutation = unrankOf.permutation().k(rank, n, k);
                assertEquals(k, permutation.length, "n=" + n + " k=" + k);
                boolean[] seen = new boolean[n];
                for (int value : permutation) {
                    assertTrue(value >= 0 && value < n, "in range, n=" + n + " k=" + k);
                    assertFalse(seen[value], "distinct, n=" + n + " k=" + k);
                    seen[value] = true;
                }
                assertEquals(rank, rankOf.permutation().k(n, permutation), "round trip n=" + n + " k=" + k + " rank=" + rank);
            }

            int[] first = unrankOf.permutation().k(BigInteger.ZERO, n, k);
            int[] last = unrankOf.permutation().k(total.subtract(BigInteger.ONE), n, k);
            for (int i = 0; i < k; i++) {
                assertEquals(i, first[i], "first n=" + n + " k=" + k);
                assertEquals(n - 1 - i, last[i], "last n=" + n + " k=" + k);
            }
        }
    }
}
