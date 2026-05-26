/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

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
}