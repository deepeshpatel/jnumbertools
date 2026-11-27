/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Combination;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every result map must list its elements in the order of the options the multiset was created with, on every
 * path, so that seeded output is reproducible (Map.copyOf iterates in an order that changes from JVM to JVM).
 */
class MultisetCombinationMapOrderTest {

    // Deliberately not alphabetical, so a sorted or hashed order cannot match by accident.
    private static final List<String> KEYS = List.of("D", "B", "E", "A", "C");
    private static final int R = 6;

    private static LinkedHashMap<String, Integer> options() {
        var options = new LinkedHashMap<String, Integer>();
        for (String key : KEYS) options.put(key, 3);
        return options;
    }

    private static int check(Iterable<Map<String, Integer>> results) {
        int count = 0;
        for (Map<String, Integer> result : results) {
            int previous = -1;
            for (String key : result.keySet()) {
                int index = KEYS.indexOf(key);
                assertTrue(index > previous, "keys out of option order: " + result);
                previous = index;
            }
            assertEquals(R, result.values().stream().mapToInt(Integer::intValue).sum());
            assertThrows(UnsupportedOperationException.class, () -> result.put("Z", 1));
            count++;
        }
        return count;
    }

    @Test
    void lexOrderKeepsOptionOrder() {
        assertTrue(check(Combination.multiset(options(), R).lexOrder()) > 0);
    }

    @Test
    void lexOrderMthKeepsOptionOrder() {
        assertTrue(check(Combination.multiset(options(), R)
                .lexOrderMth(BigInteger.valueOf(5), BigInteger.ZERO)) > 0);
    }

    @Test
    void choiceKeepsOptionOrder() {
        assertEquals(200, check(Combination.multiset(options(), R).choice(200, new Random(1))));
    }

    @Test
    void byRanksKeepsOptionOrder() {
        List<BigInteger> ranks = new ArrayList<>();
        for (int i = 0; i < 100; i++) ranks.add(BigInteger.valueOf(i));
        assertEquals(100, check(Combination.multiset(options(), R).byRanks(ranks)));
    }
}
