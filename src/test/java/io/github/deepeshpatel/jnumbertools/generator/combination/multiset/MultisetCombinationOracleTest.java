/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Multiset combinations from lexOrder (array and frequency-vector iterators) and from ranks (long and BigInteger
 * tables) must equal a plain recursive enumeration, with the result map listing elements in option order.
 */
class MultisetCombinationOracleTest {

    private static LinkedHashMap<Integer, Integer> multiset(int... freq) {
        LinkedHashMap<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < freq.length; i++) m.put(i * 3 + 1, freq[i]); // keys are not 0..n-1 on purpose
        return m;
    }

    private static int[] repeat(int value, int times) {
        int[] a = new int[times];
        Arrays.fill(a, value);
        return a;
    }

    /** All combinations as count vectors, first type's count from high to low (the library's lexicographic order). */
    private static void enumerate(int[] freq, int i, int remaining, int[] cur, List<int[]> out, int limit) {
        if (out.size() >= limit) return;
        if (i == freq.length) { if (remaining == 0) out.add(cur.clone()); return; }
        int capacityAfter = 0;
        for (int j = i + 1; j < freq.length; j++) capacityAfter += freq[j];
        for (int x = Math.min(freq[i], remaining); x >= 0 && out.size() < limit; x--) {
            if (remaining - x > capacityAfter) break;
            cur[i] = x;
            enumerate(freq, i + 1, remaining - x, cur, out, limit);
        }
        cur[i] = 0;
    }

    private static Map<Integer, Integer> expectedMap(int[] counts) {
        Map<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < counts.length; i++) if (counts[i] > 0) m.put(i * 3 + 1, counts[i]);
        return m;
    }

    private static void assertSame(int[] counts, Map<Integer, Integer> actual, String message) {
        Map<Integer, Integer> expected = expectedMap(counts);
        assertEquals(expected, actual, message);
        assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()), message + " (key order)");
        assertThrows(UnsupportedOperationException.class, () -> actual.put(-1, 1), message + " (immutable)");
    }

    private static void check(int[] freq, int r, int limit) {
        var builder = JNumberTools.combinations().multiset(multiset(freq), r);
        List<int[]> expected = new ArrayList<>();
        enumerate(freq, 0, r, new int[freq.length], expected, limit);
        String label = "types=" + freq.length + " r=" + r;

        if (expected.size() < limit) assertEquals(BigInteger.valueOf(expected.size()), builder.count(), label + " count");

        Iterator<Map<Integer, Integer>> lex = builder.lexOrder().iterator();
        for (int i = 0; i < expected.size(); i++) assertSame(expected.get(i), lex.next(), label + " lexOrder " + i);

        List<BigInteger> ranks = new ArrayList<>();
        for (int i = 0; i < expected.size(); i++) ranks.add(BigInteger.valueOf(i));
        Iterator<Map<Integer, Integer>> byRanks = builder.byRanks(ranks).iterator();
        for (int i = 0; i < expected.size(); i++) assertSame(expected.get(i), byRanks.next(), label + " byRanks " + i);

        BigInteger total = builder.count();
        assertThrows(IllegalArgumentException.class, () -> builder.byRanks(List.of(total)).iterator().next(), label);
    }

    @Test
    void smallAndMediumMultisets() {
        int[][] cases = {{1}, {3}, {1, 1, 1, 1, 1}, {2, 1, 2}, {3, 3, 3}, {5, 1, 4, 2}, {1, 2, 3, 4, 5, 6}, {2, 2, 2, 2, 2, 2, 2, 2}, {7, 7}};
        for (int[] freq : cases) {
            int items = Arrays.stream(freq).sum();
            for (int r : new int[]{0, 1, 2, items / 2, items - 1, items}) {
                if (r >= 0 && r <= items) check(freq, r, 3000);
            }
        }
    }

    @Test
    void surveyShapeTenTypesOfThree() {
        check(repeat(3, 10), 8, 3000);
    }

    @Test
    void frequencyVectorIteratorRegionLargeR() {
        // r >= 150 with few types selects the frequency-vector iterator in lexOrder
        check(repeat(20, 10), 150, 500);
        check(repeat(20, 10), 199, 100);
        check(repeat(20, 10), 200, 100);
    }

    @Test
    void tableEntriesBeyondALongStillUnrankCorrectly() {
        // 40 types x 3, r = 119: only 40 combinations, but intermediate counts exceed a long, so the BigInteger table is used
        check(repeat(3, 40), 119, 100);
        check(repeat(3, 40), 118, 1000);
    }

    @Test
    void hugeSpaceByRanksAgreesWithLexOrderMth() {
        var big = JNumberTools.combinations().multiset(multiset(repeat(3, 40)), 60);
        BigInteger total = big.count();
        Random random = new Random(5);
        for (int i = 0; i < 100; i++) {
            BigInteger rank = new BigInteger(total.bitLength() + 8, random).mod(total);
            Map<Integer, Integer> a = big.byRanks(List.of(rank)).iterator().next();
            Map<Integer, Integer> b = big.lexOrderMth(BigInteger.ONE, rank).iterator().next();
            assertEquals(b, a, "rank " + rank);
            assertEquals(60, a.values().stream().mapToInt(Integer::intValue).sum());
        }
    }
}
