/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.datastructure.FreqVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FreqVector}, the internal compact representation used by
 * MultisetCombination for very large multisets. Maintains two parallel arrays:
 * <ul>
 *   <li>{@code freq[i]}    — current count of key {@code i}</li>
 *   <li>{@code prefix[i]}  — running sum over {@code freq[0..i]}</li>
 * </ul>
 * The {@code toString()} format is {@code "[freq...] [prefix...]"}.
 */
@DisplayName("FreqVector — backing store for large multisets")
class FreqVectorTest {

    @Test
    @DisplayName("add/remove/set roundtrip + asList mirrors expanded form")
    void addRemoveSetRoundtrip() {
        FreqVector freqVector = new FreqVector(5, 3);

        // Empty at beginning
        assertEquals("[0, 0, 0] [0, 0, 0]", freqVector.toString());
        assertEquals("[]", freqVector.asList().toString());

        freqVector.add(0);
        assertEquals("[1, 0, 0] [1, 1, 1]", freqVector.toString());
        assertEquals("[0]", freqVector.asList().toString());

        freqVector.add(2);
        assertEquals("[1, 0, 1] [1, 1, 2]", freqVector.toString());
        assertEquals("[0, 2]", freqVector.asList().toString());

        freqVector.add(2);
        assertEquals("[1, 0, 2] [1, 1, 3]", freqVector.toString());
        assertEquals("[0, 2, 2]", freqVector.asList().toString());

        freqVector.add(1);
        assertEquals("[1, 1, 2] [1, 2, 4]", freqVector.toString());
        assertEquals("[0, 1, 2, 2]", freqVector.asList().toString());

        freqVector.add(1);
        assertEquals("[1, 2, 2] [1, 3, 5]", freqVector.toString());
        assertEquals("[0, 1, 1, 2, 2]", freqVector.asList().toString());

        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(1),
                "adding beyond declared capacity must fail");

        freqVector.remove(1);
        assertEquals("[1, 1, 2] [1, 2, 4]", freqVector.toString());
        assertEquals("[0, 1, 2, 2]", freqVector.asList().toString());

        freqVector.set(0, 2);
        assertEquals("[0, 1, 3] [0, 1, 4]", freqVector.toString());
        assertEquals("[1, 2, 2, 2]", freqVector.asList().toString());
    }

    @Test
    @DisplayName("Zero keys + zero capacity: empty, fixed")
    void emptyKeyCount() {
        FreqVector freqVector = new FreqVector(0, 0);
        assertEquals("[] []", freqVector.toString());
        assertTrue(freqVector.isEmpty());
        assertEquals(-1, freqVector.findValueAtIndex(0));
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(0));
    }

    @Test
    @DisplayName("Zero capacity but positive key count: cannot add")
    void sizeZero() {
        FreqVector freqVector = new FreqVector(0, 3);
        assertEquals("[0, 0, 0] [0, 0, 0]", freqVector.toString());
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(0));
        assertEquals(-1, freqVector.findValueAtIndex(0));
    }

    @Test
    @DisplayName("Full capacity: refuses additional adds")
    void fullCapacity() {
        FreqVector freqVector = new FreqVector(3, 2);
        freqVector.add(0);
        freqVector.add(0);
        freqVector.add(1);
        assertEquals("[2, 1] [2, 3]", freqVector.toString());
        assertEquals("[0, 0, 1]", freqVector.asList().toString());
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(1));
    }

    @Test
    @DisplayName("Removing more than present returns false; never throws")
    void removeAll() {
        FreqVector freqVector = new FreqVector(3, 2);
        freqVector.add(0);
        freqVector.add(1);
        freqVector.add(0);
        assertEquals("[2, 1] [2, 3]", freqVector.toString());

        assertTrue(freqVector.remove(0));
        assertEquals("[1, 1] [1, 2]", freqVector.toString());
        assertTrue(freqVector.remove(0));
        assertEquals("[0, 1] [0, 1]", freqVector.toString());
        assertTrue(freqVector.remove(1));
        assertEquals("[0, 0] [0, 0]", freqVector.toString());
        assertFalse(freqVector.remove(1), "already zero — returns false");
        assertTrue(freqVector.isEmpty());
    }

    @Test
    @DisplayName("findValueAtIndex returns -1 out of range, never throws")
    void findValueAtIndex() {
        FreqVector freqVector = new FreqVector(5, 3);
        freqVector.add(0);
        freqVector.add(1);
        freqVector.add(2);
        assertEquals(0, freqVector.findValueAtIndex(0));
        assertEquals(1, freqVector.findValueAtIndex(1));
        assertEquals(2, freqVector.findValueAtIndex(2));
        assertEquals(-1, freqVector.findValueAtIndex(3));
        assertEquals(-1, freqVector.findValueAtIndex(-1));
    }

    @Test
    @DisplayName("set beyond current size acts as add")
    void setBeyondCurrentSize() {
        FreqVector freqVector = new FreqVector(3, 2);
        freqVector.add(0);
        freqVector.set(1, 1);
        assertEquals("[1, 1] [1, 2]", freqVector.toString());
        assertEquals("[0, 1]", freqVector.asList().toString());
    }

    @Test
    @DisplayName("Invalid key index throws on add, returns false on remove")
    void invalidKey() {
        FreqVector freqVector = new FreqVector(3, 2);
        assertThrows(IllegalArgumentException.class, () -> freqVector.add(-1));
        assertThrows(IllegalArgumentException.class, () -> freqVector.add(2));
        assertFalse(freqVector.remove(2));
    }

    // ------------------------------------------------------------------
    // Backend equivalence: FreqVector switches its index structure at 128
    // distinct keys. Behaviour must be identical on both sides of that line.
    // ------------------------------------------------------------------

    @Test
    @DisplayName("behaves identically on both sides of the 128-key backend switch")
    void behaviourIsIdenticalAcrossTheBackendSwitch() {
        for (int n : new int[]{1, 2, 127, 128, 129, 300, 1000}) {
            java.util.Random random = new java.util.Random(n);
            int r = Math.min(40, 3 * n);
            FreqVector vector = new FreqVector(r, n);
            java.util.List<Integer> model = new java.util.ArrayList<>(); // sorted expansion

            for (int step = 0; step < 600; step++) {
                int key = random.nextInt(n);
                switch (random.nextInt(4)) {
                    case 0, 1 -> {
                        if (model.size() < r) {
                            vector.add(key);
                            model.add(key);
                            java.util.Collections.sort(model);
                        } else {
                            assertThrows(IndexOutOfBoundsException.class, () -> vector.add(key));
                        }
                    }
                    case 2 -> {
                        boolean expected = model.remove((Integer) key);
                        assertEquals(expected, vector.remove(key), "remove n=" + n);
                    }
                    default -> {
                        int index = random.nextInt(r);
                        if (index < model.size()) {
                            model.remove(index);
                            model.add(key);
                            java.util.Collections.sort(model);
                            vector.set(index, key);
                        } else if (model.size() < r) {
                            vector.set(index, key);
                            model.add(key);
                            java.util.Collections.sort(model);
                        }
                    }
                }

                assertEquals(model, vector.asList(), "asList n=" + n + " step=" + step);
                assertEquals(model.size(), vector.getTotal());
                assertEquals(model.isEmpty(), vector.isEmpty());
                assertEquals(-1, vector.findValueAtIndex(-1));
                assertEquals(-1, vector.findValueAtIndex(model.size()));
                for (int i = 0; i < model.size(); i++) {
                    assertEquals(model.get(i), vector.findValueAtIndex(i), "find n=" + n + " i=" + i);
                }
            }
        }
    }

    @Test
    @DisplayName("copy constructor is independent of the original on both backends")
    void copyIsIndependent() {
        for (int n : new int[]{127, 128, 129}) {
            FreqVector original = new FreqVector(4, n);
            original.add(0);
            original.add(n - 1);

            FreqVector copy = new FreqVector(original);
            copy.add(n / 2);
            copy.remove(0);

            assertEquals(java.util.List.of(0, n - 1), original.asList(), "original unchanged, n=" + n);
            assertEquals(java.util.List.of(n / 2, n - 1), copy.asList(), "copy updated, n=" + n);
            assertEquals(0, original.findValueAtIndex(0), "original index must not see the copy's changes, n=" + n);
            assertEquals(n - 1, original.findValueAtIndex(1));
            assertEquals(n / 2, copy.findValueAtIndex(0));
        }
    }

    @Test
    @DisplayName("set with an out-of-range key is rejected and leaves the vector unchanged")
    void setWithInvalidKeyLeavesVectorUnchanged() {
        for (int n : new int[]{3, 127, 128, 129}) {
            FreqVector vector = new FreqVector(3, n);
            vector.add(1);
            vector.add(2);
            java.util.List<Integer> before = vector.asList();

            assertThrows(IllegalArgumentException.class, () -> vector.set(0, -1));
            assertThrows(IllegalArgumentException.class, () -> vector.set(0, n));

            assertEquals(before, vector.asList(), "n=" + n);
            assertEquals(1, vector.findValueAtIndex(0));
            assertEquals(2, vector.findValueAtIndex(1));
        }
    }
}
