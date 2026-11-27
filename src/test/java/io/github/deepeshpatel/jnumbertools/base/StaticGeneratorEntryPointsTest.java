/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticGeneratorEntryPointsTest {

    @Test
    void permutationEntryPointDelegatesAllPermutationFactories() {
        assertEquals(BigInteger.valueOf(6), Permutation.unique(3).count());
        assertEquals(BigInteger.valueOf(6), Permutation.unique(List.of("A", "B", "C")).count());
        assertEquals(BigInteger.valueOf(6), Permutation.unique("A", "B", "C").count());

        assertEquals(BigInteger.valueOf(6), Permutation.nPk(3, 2).count());
        assertEquals(BigInteger.valueOf(6), Permutation.nPk(2, List.of("A", "B", "C")).count());
        assertEquals(BigInteger.valueOf(6), Permutation.nPk(2, "A", "B", "C").count());

        assertEquals(BigInteger.valueOf(8), Permutation.repetitive(3, 2).count());
        assertEquals(BigInteger.valueOf(8), Permutation.repetitive(3, List.of("A", "B")).count());
        assertEquals(BigInteger.valueOf(8), Permutation.repetitive(3, "A", "B").count());

        LinkedHashMap<String, Integer> multiset = new LinkedHashMap<>();
        multiset.put("A", 2);
        multiset.put("B", 1);
        assertEquals(BigInteger.valueOf(3), Permutation.multiset(multiset).count());
    }

    @Test
    void combinationEntryPointDelegatesAllCombinationFactories() {
        assertEquals(BigInteger.valueOf(6), Combination.unique(4, 2).count());
        assertEquals(BigInteger.valueOf(3), Combination.unique(2, "A", "B", "C").count());
        assertEquals(BigInteger.valueOf(3), Combination.unique(2, List.of("A", "B", "C")).count());

        assertEquals(BigInteger.valueOf(4), Combination.repetitive(2, 3).count());
        assertEquals(BigInteger.valueOf(4), Combination.repetitive(3, "A", "B").count());
        assertEquals(BigInteger.valueOf(4), Combination.repetitive(3, List.of("A", "B")).count());

        LinkedHashMap<String, Integer> multiset = new LinkedHashMap<>();
        multiset.put("A", 2);
        multiset.put("B", 1);
        assertEquals(BigInteger.valueOf(2), Combination.multiset(multiset, 2).count());
    }

    @Test
    void derangementEntryPointDelegatesAllInputForms() {
        assertEquals(BigInteger.valueOf(9), Derangement.of(4).count());
        assertEquals(BigInteger.valueOf(9), Derangement.of(List.of("A", "B", "C", "D")).count());
        assertEquals(BigInteger.valueOf(9), Derangement.of("A", "B", "C", "D").count());
    }

    @Test
    void subsetEntryPointDelegatesAllInputForms() {
        assertEquals(BigInteger.valueOf(8), Subset.of(3).all().count());
        assertEquals(BigInteger.valueOf(8), Subset.of(List.of("A", "B", "C")).all().count());
        assertEquals(BigInteger.valueOf(8), Subset.of("A", "B", "C").all().count());
        assertEquals(BigInteger.valueOf(6), Subset.of("A", "B", "C").inRange(1, 2).count());
    }

    @Test
    void jNumberToolsRetainsRankingAndNumberSystemEntryPoints() {
        // Keep the central facade focused on ranking, unranking, and number systems.
        assertEquals(BigInteger.valueOf(2), JNumberTools.rankOf().permutation().unique(1, 0));
        assertEquals(2, JNumberTools.unrankOf().permutation().unique(BigInteger.valueOf(2), 3).length);
        assertEquals(0, JNumberTools.numberSystem().factoradic(BigInteger.ZERO, 1).length);
    }
}
