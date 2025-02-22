package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.FreqVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FreqVectorTest {

    @Test
    void generalTestForMapList() {
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

        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(1));

        freqVector.remove(1);
        assertEquals("[1, 1, 2] [1, 2, 4]", freqVector.toString());
        assertEquals("[0, 1, 2, 2]", freqVector.asList().toString());

        freqVector.set(0, 2);
        assertEquals("[0, 1, 3] [0, 1, 4]", freqVector.toString());
        assertEquals("[1, 2, 2, 2]", freqVector.asList().toString());
    }

    @Test
    void testEmptyKeyCount() {
        FreqVector freqVector = new FreqVector(0, 0);
        assertEquals("[] []", freqVector.toString());
        assertTrue(freqVector.isEmpty());
        assertEquals(-1, freqVector.findValueAtIndex(0)); // Beyond empty list
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(0));
    }

    @Test
    void testSizeZero() {
        FreqVector freqVector = new FreqVector(0, 3);
        assertEquals("[0, 0, 0] [0, 0, 0]", freqVector.toString());
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(0));
        assertEquals(-1, freqVector.findValueAtIndex(0));
    }

    @Test
    void testFullCapacity() {
        FreqVector freqVector = new FreqVector(3, 2);
        freqVector.add(0);
        freqVector.add(0);
        freqVector.add(1);
        assertEquals("[2, 1] [2, 3]", freqVector.toString());
        assertEquals("[0, 0, 1]", freqVector.asList().toString());
        assertThrows(IndexOutOfBoundsException.class, () -> freqVector.add(1));
    }

    @Test
    void testRemoveAll() {
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
        assertFalse(freqVector.remove(1)); // Already zero
        assertTrue(freqVector.isEmpty());
    }

    @Test
    void testFindValueAtIndex() {
        FreqVector freqVector = new FreqVector(5, 3);
        freqVector.add(0);
        freqVector.add(1);
        freqVector.add(2);
        assertEquals(0, freqVector.findValueAtIndex(0));
        assertEquals(1, freqVector.findValueAtIndex(1));
        assertEquals(2, freqVector.findValueAtIndex(2));
        assertEquals(-1, freqVector.findValueAtIndex(3)); // Beyond current size
        assertEquals(-1, freqVector.findValueAtIndex(-1)); // Negative index
    }

    @Test
    void testSetBeyondCurrentSize() {
        FreqVector freqVector = new FreqVector(3, 2);
        freqVector.add(0);
        freqVector.set(1, 1); // Should add 1 since index 1 is beyond current size
        assertEquals("[1, 1] [1, 2]", freqVector.toString());
        assertEquals("[0, 1]", freqVector.asList().toString());
    }

    @Test
    void testInvalidKey() {
        FreqVector freqVector = new FreqVector(3, 2);
        assertThrows(IllegalArgumentException.class, () -> freqVector.add(-1));
        assertThrows(IllegalArgumentException.class, () -> freqVector.add(2));
        assertFalse(freqVector.remove(2)); // Invalid key
    }
}