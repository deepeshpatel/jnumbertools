/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.*;

class DerangadicIncrementTest {

    private static final DerangadicIncrement  INC = new DerangadicIncrement();
    private static final DerangadicAlgorithms ALG = new DerangadicAlgorithms();

    // =========================================================================
    // 1. Digit sequence matches toDerangadic exactly for every rank
    // =========================================================================

    @Test
    @DisplayName("Digit sequence matches toDerangadic for even n=4 (all 9 ranks)")
    void testDigitSequenceN4() { assertDigitSequenceMatches(4); }

    @Test
    @DisplayName("Digit sequence matches toDerangadic for odd n=5 (all 44 ranks)")
    void testDigitSequenceN5() { assertDigitSequenceMatches(5); }

    @Test
    @DisplayName("Digit sequence matches toDerangadic for even n=6 (all 265 ranks)")
    void testDigitSequenceN6() { assertDigitSequenceMatches(6); }

    @Test
    @DisplayName("Digit sequence matches toDerangadic for odd n=7 (all 1854 ranks)")
    void testDigitSequenceN7() { assertDigitSequenceMatches(7); }

    @Test
    @DisplayName("Digit sequence matches toDerangadic for even n=8 (all 14833 ranks)")
    void testDigitSequenceN8() { assertDigitSequenceMatches(8); }

    @Test
    @DisplayName("Digit sequence matches toDerangadic for odd n=9 (all 133496 ranks)")
    void testDigitSequenceN9() { assertDigitSequenceMatches(9); }

    private void assertDigitSequenceMatches(int n) {
        BigInteger total = ALG.derangementCount(n);
        DerangadicIncrement.DerangadicState state = INC.initialState(n);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] expected = ALG.toDerangadic(rank, n);
            int[] actual   = state.getDigits();

            assertTrue(arraysEqualIgnoringTrailingZeros(expected, actual),
                    String.format("Digit mismatch at n=%d rank=%d%n  expected: %s%n  actual:   %s",
                            n, rank, Arrays.toString(expected), Arrays.toString(actual)));

            if (rank < total.longValue() - 1) {
                assertTrue(INC.increment(state),
                        "increment() returned false before last rank at n=" + n + ", rank=" + rank);
            }
        }

        assertFalse(INC.increment(state),
                "increment() should return false after last rank for n=" + n);
    }

    // =========================================================================
    // 2. Derangements are valid and lexicographically ordered
    // =========================================================================

    @Test
    @DisplayName("Derangements are valid and in lexicographic order for n=4..9")
    void testDerangementsValidAndOrdered() {
        for (int n = 4; n <= 9; n++) {
            BigInteger total = ALG.derangementCount(n);
            DerangadicIncrement.DerangadicState state = INC.initialState(n);

            int[] previous = null;
            for (long rank = 0; rank < total.longValue(); rank++) {
                int[] d = ALG.toDerangement(state.getDigits(), n);

                assertTrue(isValidDerangement(d),
                        String.format("Invalid derangement at n=%d rank=%d: %s",
                                n, rank, Arrays.toString(d)));

                if (previous != null) {
                    assertTrue(isLexLess(previous, d),
                            String.format("Order violation at n=%d rank=%d", n, rank));
                }

                previous = d;
                if (rank < total.longValue() - 1) INC.increment(state);
            }
        }
    }

    // =========================================================================
    // 3. Length boundary crossings at correct ranks
    // =========================================================================

    @Test
    @DisplayName("Length boundary crossings for even n=8: at ranks 1, 9, 265")
    void testBoundaryCrossingsEvenN8() {
        int n = 8;
        DerangadicIncrement.DerangadicState state = INC.initialState(n);
        assertEquals(2, state.getActualN(), "rank 0: actualN should be 2");

        INC.increment(state);
        assertEquals(4, state.getActualN(), "rank 1: actualN should be 4");

        for (int i = 0; i < 8; i++) INC.increment(state);
        assertEquals(6, state.getActualN(), "rank 9: actualN should be 6");

        for (int i = 0; i < 256; i++) INC.increment(state);
        assertEquals(8, state.getActualN(), "rank 265: actualN should be 8");
    }

    @Test
    @DisplayName("Length boundary crossings for odd n=7: at ranks 2, 44")
    void testBoundaryCrossingsOddN7() {
        int n = 7;
        DerangadicIncrement.DerangadicState state = INC.initialState(n);
        assertEquals(3, state.getActualN(), "rank 0: actualN should be 3");

        INC.increment(state);
        assertEquals(3, state.getActualN(), "rank 1: actualN should still be 3");

        INC.increment(state);
        assertEquals(5, state.getActualN(), "rank 2: actualN should be 5");

        for (int i = 0; i < 42; i++) INC.increment(state);
        assertEquals(7, state.getActualN(), "rank 44: actualN should be 7");
    }

    // =========================================================================
    // 4. getDigits() returns an independent copy
    // =========================================================================

    @Test
    @DisplayName("getDigits() mutation does not affect internal state")
    void testGetDigitsReturnsCopy() {
        DerangadicIncrement.DerangadicState state = INC.initialState(4);
        int[] copy = state.getDigits();
        copy[0] = 99;
        assertNotEquals(99, state.getDigits()[0], "getDigits() must return a defensive copy");
    }

    // =========================================================================
    // 5. All derangements via increment match direct unrank
    // =========================================================================

    @Test
    @DisplayName("All derangements via increment match ALG.unrank for n=4..7")
    void testAllDerangementsMatchUnrank() {
        for (int n = 4; n <= 7; n++) {
            BigInteger total = ALG.derangementCount(n);
            List<int[]> byIncrement = new ArrayList<>();
            DerangadicIncrement.DerangadicState state = INC.initialState(n);

            for (long rank = 0; rank < total.longValue(); rank++) {
                byIncrement.add(ALG.toDerangement(state.getDigits(), n));
                if (rank < total.longValue() - 1) INC.increment(state);
            }

            for (long rank = 0; rank < total.longValue(); rank++) {
                int[] expected = ALG.toDerangement(ALG.toDerangadic(rank, n), n);
                assertArrayEquals(expected, byIncrement.get((int) rank),
                        "Derangement mismatch at n=" + n + " rank=" + rank);
            }
        }
    }

    // =========================================================================
    // 6. No digit ever exceeds its maxDigit
    // =========================================================================

    @Test
    @DisplayName("No digit exceeds maxDigit for n=6 (all 265 ranks)")
    void testNoDigitExceedsMax() {
        int n = 6;
        BigInteger total = ALG.derangementCount(n);
        DerangadicIncrement.DerangadicState state = INC.initialState(n);

        for (long rank = 0; rank < total.longValue(); rank++) {
            for (int i = 0; i < state.getActualN(); i++) {
                assertTrue(state.digits[i] <= state.maxDigit[i],
                        String.format("digits[%d]=%d > maxDigit[%d]=%d at n=%d rank=%d",
                                i, state.digits[i], i, state.maxDigit[i], n, rank));
            }
            if (rank < total.longValue() - 1) INC.increment(state);
        }
    }

    // =========================================================================
    // 7. Pinned output from Scrap2 program
    // =========================================================================

    @Test
    @DisplayName("Pinned output: even n=12, ranks 0..20 match Scrap2 exactly")
    void testKnownOutputEvenN12() {
        int[][] expected = {
                {0, 0},               // rank 0
                {0, 1, 1, 0},         // rank 1
                {0, 0, 2, 0},         // rank 2
                {0, 1, 0, 1},         // rank 3
                {0, 0, 1, 1},         // rank 4
                {0, 1, 1, 1},         // rank 5
                {0, 0, 0, 2},         // rank 6
                {0, 0, 1, 2},         // rank 7
                {0, 1, 1, 2},         // rank 8
                {0, 1, 0, 0, 1, 0},   // rank 9
                {0, 0, 1, 0, 1, 0},   // rank 10
                {0, 0, 0, 1, 1, 0},   // rank 11
                {0, 1, 1, 1, 1, 0},   // rank 12
                {0, 0, 2, 1, 1, 0},   // rank 13
                {0, 1, 0, 2, 1, 0},   // rank 14
                {0, 0, 1, 2, 1, 0},   // rank 15
                {0, 1, 1, 2, 1, 0},   // rank 16
                {0, 0, 0, 3, 1, 0},   // rank 17
                {0, 0, 1, 3, 1, 0},   // rank 18
                {0, 1, 1, 3, 1, 0},   // rank 19
                {0, 0, 0, 0, 2, 0},   // rank 20
        };
        DerangadicIncrement.DerangadicState state = INC.initialState(12);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], state.getDigits()),
                    String.format("n=12 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(state.getDigits())));
            if (rank < 20) INC.increment(state);
        }
    }

    @Test
    @DisplayName("Pinned output: odd n=13, ranks 0..20 match Scrap2 exactly")
    void testKnownOutputOddN13() {
        int[][] expected = {
                {0, 1, 0},             // rank 0
                {0, 0, 1},             // rank 1
                {0, 0, 0, 1, 0},       // rank 2
                {0, 1, 1, 1, 0},       // rank 3
                {0, 0, 2, 1, 0},       // rank 4
                {0, 1, 0, 2, 0},       // rank 5
                {0, 0, 1, 2, 0},       // rank 6
                {0, 1, 1, 2, 0},       // rank 7
                {0, 0, 0, 3, 0},       // rank 8
                {0, 0, 1, 3, 0},       // rank 9
                {0, 1, 1, 3, 0},       // rank 10
                {0, 0, 0, 0, 1},       // rank 11
                {0, 1, 1, 0, 1},       // rank 12
                {0, 0, 2, 0, 1},       // rank 13
                {0, 1, 0, 1, 1},       // rank 14
                {0, 1, 1, 1, 1},       // rank 15
                {0, 0, 2, 1, 1},       // rank 16
                {0, 1, 2, 1, 1},       // rank 17
                {0, 0, 0, 2, 1},       // rank 18
                {0, 0, 1, 2, 1},       // rank 19
                {0, 0, 2, 2, 1},       // rank 20
        };
        DerangadicIncrement.DerangadicState state = INC.initialState(13);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], state.getDigits()),
                    String.format("n=13 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(state.getDigits())));
            if (rank < 20) INC.increment(state);
        }
    }

    // =========================================================================
    // 8. Large-n smoke tests
    // =========================================================================

    @Test
    @DisplayName("First 10 000 increments for n=100 produce valid derangements")
    void testSmokeTestN100() {
        int n = 100;
        DerangadicIncrement.DerangadicState state = INC.initialState(n);
        for (int i = 0; i < 10_000; i++) {
            int[] d = ALG.toDerangement(state.getDigits(), n);
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=100 iteration " + i);
            INC.increment(state);
        }
    }

    @Test
    @DisplayName("First 500 increments for n=5000 produce valid derangements")
    void testSmokeTestN5000() {
        int n = 5000;
        DerangadicIncrement.DerangadicState state = INC.initialState(n);
        for (int i = 0; i < 500; i++) {
            int[] d = ALG.toDerangement(state.getDigits(), n);
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=5000 iteration " + i);
            INC.increment(state);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static boolean arraysEqualIgnoringTrailingZeros(int[] a, int[] b) {
        return Arrays.equals(trimTrailingZeros(a), trimTrailingZeros(b));
    }

    private static int[] trimTrailingZeros(int[] arr) {
        int last = arr.length - 1;
        while (last > 0 && arr[last] == 0) last--;
        return Arrays.copyOf(arr, last + 1);
    }

    private static boolean isLexLess(int[] a, int[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            if (a[i] != b[i]) return a[i] < b[i];
        }
        return a.length < b.length;
    }

    @Test
    @DisplayName("Verify first 20 M ranks for even (n=12) and odd (n=13) elements")
    void testFirstMillionRanksEvenAndOdd() {
        int maxRanks = 20_000_000;

        // 1. Test Even N (n=12)
        int nEven = 12;
        DerangadicIncrement.DerangadicState stateEven = INC.initialState(nEven);
        System.out.println("Starting " + maxRanks + "  increment verifications for even n=" + nEven + "...");

        java.util.stream.LongStream.range(0, maxRanks).forEach(rank -> {
            int[] expectedDigits = ALG.toDerangadic(rank, nEven);
            int[] actualDigits = stateEven.getDigits();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at even n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nEven, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            // Validate that the produced derangement remains sound
            int[] derangement = ALG.toDerangement(actualDigits, nEven);
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at even n=%d rank=%d: %s",
                        nEven, rank, Arrays.toString(derangement)));
            }

            INC.increment(stateEven);
        });

        // 2. Test Odd N (n=13)
        int nOdd = 13;
        DerangadicIncrement.DerangadicState stateOdd = INC.initialState(nOdd);
        System.out.println("Starting " + maxRanks + "  increment verifications for odd n=" + nOdd + "...");

        java.util.stream.LongStream.range(0, maxRanks).forEach(rank -> {
            int[] expectedDigits = ALG.toDerangadic(rank, nOdd);
            int[] actualDigits = stateOdd.getDigits();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at odd n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nOdd, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            int[] derangement = ALG.toDerangement(actualDigits, nOdd);
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at odd n=%d rank=%d: %s",
                        nOdd, rank, Arrays.toString(derangement)));
            }

            INC.increment(stateOdd);
        });

        System.out.println("Successfully verified " + maxRanks + " unique states!");
    }
}