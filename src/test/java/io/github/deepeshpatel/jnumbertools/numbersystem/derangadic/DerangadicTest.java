/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.TestBase.isLexLess;
import static io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Correctness tests for {@link Derangadic} public wrapper.
 * <p>
 * Tests verify that the public API produces correct derangements,
 * handles edge cases properly, and maintains consistency across
 * different construction methods.
 *
 * @author Deepesh Patel &amp; Aditya Patel
 * @version 3.0.2
 */
class DerangadicTest {

    private static final Calculator CALC = new Calculator();
    private static final DerangadicAlgorithms ALG = new DerangadicAlgorithms(CALC);

    // =========================================================================
    // 1. Construction and Basic Getters
    // =========================================================================

    @Test
    @DisplayName("Constructor with rank 0 produces correct derangement for n=4")
    void testConstructorWithRankZero() {
        Derangadic d = new Derangadic(4, BigInteger.ZERO, CALC);

        assertEquals(BigInteger.ZERO, d.rank());
        assertEquals(4, d.order());
        assertNotNull(d.derangement());
        assertEquals(4, d.derangement().length);

        // Verify it's the first derangement
        int[] expected = ALG.unrank(0, 4);
        assertArrayEquals(expected, d.derangement());
    }

    @Test
    @DisplayName("Constructor with non-zero rank produces correct derangement")
    void testConstructorWithNonZeroRank() {
        Derangadic d = new Derangadic(5, BigInteger.valueOf(10), CALC);

        assertEquals(BigInteger.valueOf(10), d.rank());
        int[] expected = ALG.unrank(10, 5);
        assertArrayEquals(expected, d.derangement());
    }

    @Test
    @DisplayName("encoded() returns digit array that matches toDerangadic")
    void testEncodedMatchesToDerangadic() {
        int n = 6;
        for (long rank = 0; rank < 20; rank++) {
            Derangadic d = new Derangadic(n, BigInteger.valueOf(rank), CALC);
            int[] encoded = d.encoded();
            int[] expected = ALG.encode(rank, n);
            assertArrayEquals(expected, encoded,
                    "Encoded mismatch at rank " + rank);
        }
    }

    // =========================================================================
    // 2. Derangement Correctness
    // =========================================================================

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5, 6, 7, 8})
    @DisplayName("All derangements from rank 0 to count-1 are valid")
    void testAllDerangementsAreValid(int n) {
        BigInteger total = ALG.derangementCount(n);

        // Start at rank 0
        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] derangement = d.derangement();
            assertTrue(isValidDerangement(derangement),
                    String.format("Invalid derangement at n=%d rank=%d: %s",
                            n, rank, Arrays.toString(derangement)));

            if (rank < total.longValue() - 1) {
                d.next();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7})
    @DisplayName("Derangements from constructor match unrank reference")
    void testDerangementsMatchUnrankReference(int n) {
        BigInteger total = ALG.derangementCount(n);

        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] fromDerangadic = d.derangement();
            int[] expected = ALG.unrank(rank, n);

            assertArrayEquals(expected, fromDerangadic,
                    String.format("Mismatch at n=%d rank=%d", n, rank));

            if (rank < total.longValue() - 1) {
                d.next();
            }
        }
    }

    // =========================================================================
    // 3. next() Method Correctness
    // =========================================================================

    @Test
    @DisplayName("next() advances rank by 1")
    void testNextAdvancesRank() {
        Derangadic d = new Derangadic(6, BigInteger.valueOf(100), CALC);
        BigInteger originalRank = d.rank();
        d.next();
        assertEquals(originalRank.add(BigInteger.ONE), d.rank());
    }

    @Test
    @DisplayName("next() produces lexicographically increasing derangements")
    void testNextProducesIncreasingDerangements() {
        int n = 6;
        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        int[] prev = d.derangement().clone();
        BigInteger total = ALG.derangementCount(n);

        for (long rank = 1; rank < total.longValue(); rank++) {
            d.next();
            int[] curr = d.derangement().clone();
            assertTrue(isLexLess(prev, curr),
                    String.format("Order violation at rank transition %d->%d", rank - 1, rank));
            prev = curr;
        }
    }

    @Test
    @DisplayName("next() throws NoSuchElementException at last rank")
    void testNextThrowsAtLastRank() {
        int n = 4; // !4 = 9
        Derangadic d = new Derangadic(n, ALG.derangementCount(n).subtract(BigInteger.ONE), CALC);
        assertNotNull(d.derangement(),"Last rank should still have a valid derangement");
        assertThrows(NoSuchElementException.class, d::next, " next() should throw exp at last rank");
    }

    @Test
    @DisplayName("Sequential next() calls produce all derangements")
    void testSequentialNextProducesAllDerangements() {
        int n = 5;
        BigInteger total = ALG.derangementCount(n);

        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] fromDerangadic = d.derangement();
            int[] expected = ALG.unrank(rank, n);
            assertArrayEquals(expected, fromDerangadic,
                    "Mismatch at sequential step " + rank);

            if (rank < total.longValue() - 1) {
                d.next();
            }
        }
    }

    // =========================================================================
    // 4. add() Method Correctness
    // =========================================================================

    @Test
    @DisplayName("add() creates new instance with incremented rank")
    void testAddCreatesNewInstance() {
        Derangadic d = new Derangadic(5, BigInteger.valueOf(10), CALC);
        Derangadic added = d.add(BigInteger.valueOf(5));

        assertNotSame(d, added);
        assertEquals(BigInteger.valueOf(10), d.rank());
        assertEquals(BigInteger.valueOf(15), added.rank());
        assertEquals(d.order(), added.order());
    }

    @Test
    @DisplayName("add() produces same result as constructing with incremented rank")
    void testAddMatchesDirectConstruction() {
        int n = 5;
        long rank = 10;
        long increment = 7;

        Derangadic d = new Derangadic(n, BigInteger.valueOf(rank), CALC);
        Derangadic added = d.add(BigInteger.valueOf(increment));

        Derangadic direct = new Derangadic(n, BigInteger.valueOf(rank + increment), CALC);

        assertArrayEquals(direct.derangement(), added.derangement());
        assertArrayEquals(direct.encoded(), added.encoded());
    }

    // =========================================================================
    // 5. Edge Cases
    // =========================================================================

    @Test
    @DisplayName("Smallest n=2 works correctly")
    void testSmallestN2() {
        Derangadic d = new Derangadic(2, BigInteger.ZERO, CALC);

        // Only one derangement for n=2: [1, 0]
        int[] derangement = d.derangement();
        assertTrue(isValidDerangement(derangement));
        assertArrayEquals(new int[]{1, 0}, derangement);

        // No next derangement
        assertThrows(NoSuchElementException.class, d::next);
    }

    @Test
    @DisplayName("n=3 works correctly")
    void testN3() {
        // !3 = 2 derangements: rank 0: [1,2,0], rank 1: [2,0,1]
        Derangadic d0 = new Derangadic(3, BigInteger.ZERO, CALC);
        assertArrayEquals(new int[]{1, 2, 0}, d0.derangement());

        d0.next();
        assertArrayEquals(new int[]{2, 0, 1}, d0.derangement());
        assertThrows(NoSuchElementException.class, d0::next);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 8",      // last rank of even n=4: !4 = 9, so rank 8 is last
            "5, 43",     // last rank of odd n=5: !5 = 44, so rank 43 is last
            "6, 264"     // last rank of even n=6: !6 = 265, so rank 264 is last
    })
    @DisplayName("Last rank produces correct derangement")
    void testLastRank(int n, long lastRank) {
        Derangadic d = new Derangadic(n, BigInteger.valueOf(lastRank), CALC);
        int[] derangement = d.derangement();
        assertTrue(isValidDerangement(derangement));

        // Compare with unrank reference
        int[] expected = ALG.unrank(lastRank, n);
        assertArrayEquals(expected, derangement);
    }

    @Test
    @DisplayName("count() returns correct subfactorial")
    void testCount() {
        Derangadic d4 = new Derangadic(4, BigInteger.ZERO, CALC);
        assertEquals(BigInteger.valueOf(9), d4.count());

        Derangadic d5 = new Derangadic(5, BigInteger.ZERO, CALC);
        assertEquals(BigInteger.valueOf(44), d5.count());

        Derangadic d6 = new Derangadic(6, BigInteger.ZERO, CALC);
        assertEquals(BigInteger.valueOf(265), d6.count());
    }

    // =========================================================================
    // 6. Consistency Across Methods
    // =========================================================================

    @Test
    @DisplayName("Same rank produces same derangement regardless of construction")
    void testConsistencyAcrossConstruction() {
        int n = 7;
        long rank = 100;

        // Construction via rank
        Derangadic d1 = new Derangadic(n, BigInteger.valueOf(rank), CALC);

        // Build via encoded to verify consistency
        Derangadic d2 = new Derangadic(n, BigInteger.valueOf(rank), CALC);

        assertArrayEquals(d1.derangement(), d2.derangement());
        assertArrayEquals(d1.encoded(), d2.encoded());
        assertEquals(d1.rank(), d2.rank());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7, 8})
    @DisplayName("encoded() trailing zeros preserved consistently")
    void testEncodedTrailingZerosConsistent(int n) {
        BigInteger total = ALG.derangementCount(n);

        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        for (long rank = 0; rank < total.longValue() && rank < 100; rank++) {
            int[] encoded = d.encoded();
            int[] expected = ALG.encode(rank, n);

            // Compare ignoring trailing zeros (should be equal)
            assertTrue(arraysEqualIgnoringTrailingZeros(expected, encoded),
                    String.format("Encoded mismatch at n=%d rank=%d", n, rank));

            if (rank < total.longValue() - 1 && rank < 99) {
                d.next();
            }
        }
    }

    // =========================================================================
    // 7. Large n Smoke Tests
    // =========================================================================

    @Test
    @DisplayName("Large n=1000 construction and first increment works")
    void testLargeN1000() {
        int n = 1000;
        Derangadic d = new Derangadic(n, BigInteger.ZERO, CALC);

        int[] derangement = d.derangement();
        assertTrue(isValidDerangement(derangement));

        // First increment should work
        d.next();
        assertTrue(isValidDerangement(d.derangement()));
        assertEquals(BigInteger.ONE, d.rank());
    }

    @Test
    @DisplayName("Large n=5000 with mid-range rank works")
    void testLargeN5000MidRank() {
        int n = 5000;
        long rank = 1_000_000L;
        Derangadic d = new Derangadic(n, BigInteger.valueOf(rank), CALC);

        int[] derangement = d.derangement();
        assertTrue(isValidDerangement(derangement));
        assertEquals(BigInteger.valueOf(rank), d.rank());
    }

    // =========================================================================
    // 8. String Representation (basic)
    // =========================================================================


    // =========================================================================
    // Helper Methods
    // =========================================================================

    private static boolean arraysEqualIgnoringTrailingZeros(int[] a, int[] b) {
        return Arrays.equals(trimTrailingZeros(a), trimTrailingZeros(b));
    }

    private static int[] trimTrailingZeros(int[] arr) {
        int last = arr.length - 1;
        while (last > 0 && arr[last] == 0) last--;
        return Arrays.copyOf(arr, last + 1);
    }
}