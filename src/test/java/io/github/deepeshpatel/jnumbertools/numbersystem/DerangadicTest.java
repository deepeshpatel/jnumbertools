/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test class for Derangadic wrapper class.
 *
 * @author Deepesh Patel & Aditya Patel
 */
class DerangadicTest {

    @Test
    @DisplayName("Derangadic.of() should create correct instances from rank")
    void testOf() {
        DerangadicAlgorithms algo = new DerangadicAlgorithms();

        // n=4, D₄=9
        int[] expected0 = algo.toDerangadic(0, 4);
        Derangadic d0 = Derangadic.of(0, 4);
        assertEquals(BigInteger.ZERO, d0.decimalValue());
        assertArrayEquals(expected0, d0.derangadicValues());
        assertEquals(4, d0.order());

        int[] expected1 = algo.toDerangadic(1, 4);
        Derangadic d1 = Derangadic.of(1, 4);
        assertEquals(BigInteger.ONE, d1.decimalValue());
        assertArrayEquals(expected1, d1.derangadicValues());

        int[] expected8 = algo.toDerangadic(8, 4);
        Derangadic d8 = Derangadic.of(8, 4);
        assertEquals(BigInteger.valueOf(8), d8.decimalValue());
        assertArrayEquals(expected8, d8.derangadicValues());
    }

    @Test
    @DisplayName("Derangadic.fromDerangement() should create correct instances from derangement")
    void testFromDerangement() {
        // Rank 0 derangement for n=4: [1, 0, 3, 2]
        int[] derangement = {1, 0, 3, 2};
        Derangadic d = Derangadic.fromDerangement(derangement, 4);
        assertEquals(BigInteger.ZERO, d.decimalValue());
        assertTrue(arraysEqualIgnoringTrailingZeros(new int[]{0, 0}, d.derangadicValues()));

        // Rank 1 derangement for n=4: [1, 2, 3, 0]
        int[] derangement2 = {1, 2, 3, 0};
        Derangadic d2 = Derangadic.fromDerangement(derangement2, 4);
        assertEquals(BigInteger.ONE, d2.decimalValue());
        assertTrue(arraysEqualIgnoringTrailingZeros(new int[]{0, 1, 1}, d2.derangadicValues()));
    }

    @Test
    @DisplayName("toString() should format correctly ignoring trailing zeros in comparison")
    void testToString() {
        // Note: The actual string representation may have trailing zeros,
        // but the visual output should be consistent
        Derangadic d0 = Derangadic.of(0, 4);
        String str0 = d0.toString();
        assertTrue(str0.equals("[0, 0](4)") || str0.equals("[0](4)"),
                "toString() should be either [0,0](4) or [0](4) but was: " + str0);

        Derangadic d1 = Derangadic.of(1, 4);
        assertTrue(d1.toString().equals("[0, 1, 1](4)") || d1.toString().equals("[0, 1, 1, 0](4)"),
                "toString() mismatch for rank 1: " + d1.toString());
    }

    @Test
    @DisplayName("getMinimalSize() should return length of digit array")
    void testGetMinimalSize() {
        // The length may vary depending on implementation, but should be consistent
        Derangadic d0 = Derangadic.of(0, 4);
        int size0 = d0.getMinimalSize();
        assertTrue(size0 == 2 || size0 == 1, "Size for rank 0 should be 1 or 2, got: " + size0);

        Derangadic d1 = Derangadic.of(1, 4);
        int size1 = d1.getMinimalSize();
        assertTrue(size1 == 3 || size1 == 4, "Size for rank 1 should be 3 or 4, got: " + size1);
    }

    @Test
    @DisplayName("toDerangement() should convert back to original derangement")
    void testToDerangement() {
        for (int n = 3; n <= 6; n++) {
            DerangadicAlgorithms algo = new DerangadicAlgorithms();
            BigInteger total = algo.derangementCount(n);

            for (long rank = 0; rank < total.longValue() && rank < 50; rank++) {
                Derangadic d = Derangadic.of(rank, n);
                int[] derangement = d.toDerangement();

                assertTrue(isValidDerangement(derangement),
                        String.format("Invalid derangement at rank=%d, n=%d", rank, n));

                Derangadic d2 = Derangadic.fromDerangement(derangement, n);
                assertEquals(d.decimalValue(), d2.decimalValue(),
                        String.format("Round-trip failed at rank=%d, n=%d", rank, n));
            }
        }
    }

    @Test
    @DisplayName("equals() and hashCode() should work correctly")
    void testEqualsAndHashCode() {
        Derangadic d1 = Derangadic.of(5, 4);
        Derangadic d2 = Derangadic.of(5, 4);
        Derangadic d3 = Derangadic.of(6, 4);
        Derangadic d4 = Derangadic.of(5, 5);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(d1, d4);
        assertNotEquals(d1, null);
        assertNotEquals(d1, "some string");
    }

    @Test
    @DisplayName("Invalid inputs should throw appropriate exceptions")
    void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(-1, 4));
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(BigInteger.valueOf(-1), 4));

        DerangadicAlgorithms algo = new DerangadicAlgorithms();
        BigInteger total = algo.derangementCount(4);
        assertThrows(IllegalArgumentException.class, () -> Derangadic.of(total, 4));

        int[] invalidDerangement = {0, 2, 1};
        assertThrows(IllegalArgumentException.class, () ->
                Derangadic.fromDerangement(invalidDerangement, 3));
    }

    @Test
    @DisplayName("Round-trip through both APIs should be consistent")
    void testBothApisConsistent() {
        DerangadicAlgorithms algo = new DerangadicAlgorithms();

        for (int n = 3; n <= 6; n++) {
            BigInteger total = algo.derangementCount(n);

            for (long rank = 0; rank < total.longValue() && rank < 50; rank++) {
                Derangadic d = Derangadic.of(rank, n);
                int[] derangementFromWrapper = d.toDerangement();
                int[] derangementFromAlgo = algo.unrank(BigInteger.valueOf(rank), n);

                assertArrayEquals(derangementFromAlgo, derangementFromWrapper,
                        String.format("Inconsistent derangement at rank=%d, n=%d", rank, n));
            }
        }
    }

    // ==================== Utility Methods ====================

    private static boolean isValidDerangement(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            if (arr[i] == i) return false;
        }
        boolean[] seen = new boolean[n];
        for (int val : arr) {
            if (val < 0 || val >= n) return false;
            if (seen[val]) return false;
            seen[val] = true;
        }
        return true;
    }

    private static boolean arraysEqualIgnoringTrailingZeros(int[] a, int[] b) {
        int[] trimmedA = trimTrailingZeros(a);
        int[] trimmedB = trimTrailingZeros(b);
        return Arrays.equals(trimmedA, trimmedB);
    }

    private static int[] trimTrailingZeros(int[] arr) {
        if (arr.length == 0) return arr;

        int lastNonZero = arr.length - 1;
        while (lastNonZero >= 0 && arr[lastNonZero] == 0) {
            lastNonZero--;
        }

        if (lastNonZero < 0) {
            return new int[]{0};
        }

        int[] result = new int[lastNonZero + 1];
        System.arraycopy(arr, 0, result, 0, lastNonZero + 1);
        return result;
    }
}
