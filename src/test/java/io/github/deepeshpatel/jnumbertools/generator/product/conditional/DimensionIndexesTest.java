/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Position lookup of equal values in a dimension, including dimensions made of many equal values. */
class DimensionIndexesTest {

    @Test
    void equalValuesShareOneEntryWithAllTheirPositionsInOrder() {
        List<String> dimension = List.of("a", "b", "a", "c", "a", "b");
        Map<Object, int[]> positions = new DimensionIndexes(List.<List<?>>of(dimension)).of(0);

        assertEquals(3, positions.size());
        assertArrayEquals(new int[]{0, 2, 4}, positions.get("a"));
        assertArrayEquals(new int[]{1, 5}, positions.get("b"));
        assertArrayEquals(new int[]{3}, positions.get("c"));
    }

    @Test
    void uniqueValuesEachHaveOnePosition() {
        List<Integer> dimension = new ArrayList<>();
        for (int i = 0; i < 1000; i++) dimension.add(i);
        Map<Object, int[]> positions = new DimensionIndexes(List.<List<?>>of(dimension)).of(0);

        assertEquals(1000, positions.size());
        for (int i = 0; i < 1000; i++) assertArrayEquals(new int[]{i}, positions.get(i));
    }

    @Test
    void aDimensionOfManyEqualValuesIsIndexedInLinearTime() {
        int size = 400_000;
        List<String> dimension = new ArrayList<>(size);
        for (int i = 0; i < size; i++) dimension.add("same");
        DimensionIndexes indexes = new DimensionIndexes(List.<List<?>>of(dimension));

        // Growing the position array by one on every duplicate copies about size^2 / 2 integers, which takes
        // far longer than this bound; building it in one pass takes milliseconds.
        Map<Object, int[]> positions = assertTimeout(Duration.ofSeconds(5), () -> { return indexes.of(0); });

        int[] all = positions.get("same");
        assertEquals(size, all.length);
        for (int i = 0; i < size; i++) {
            if (all[i] != i) fail("position " + i + " was " + all[i]);
        }
    }

    @Test
    void mixedDuplicatesKeepEveryPositionAfterTheBuffersGrow() {
        List<Integer> dimension = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) dimension.add(i % 3);          // 0,1,2,0,1,2,...
        Map<Object, int[]> positions = new DimensionIndexes(List.<List<?>>of(dimension)).of(0);

        for (int value = 0; value < 3; value++) {
            int[] found = positions.get(value);
            int expectedLength = (10_000 - value + 2) / 3;
            assertEquals(expectedLength, found.length);
            for (int k = 0; k < found.length; k++) assertEquals(value + 3 * k, found[k]);
        }
    }
}
