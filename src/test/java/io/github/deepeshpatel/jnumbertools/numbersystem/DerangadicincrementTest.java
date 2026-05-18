/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.numbersystem.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.*;

class DerangadicIncrementTest {

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
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] expected = ALG.toDerangadic(rank, n);
            int[] actual = state.getDigits();

            assertTrue(arraysEqualIgnoringTrailingZeros(expected, actual),
                    String.format("Digit mismatch at n=%d rank=%d%n  expected: %s%n  actual:   %s",
                            n, rank, Arrays.toString(expected), Arrays.toString(actual)));

            if (rank < total.longValue() - 1) {
                assertTrue(inc.increment(state),
                        "next() returned false before last rank at n=" + n + ", rank=" + rank);
            }
        }

        assertFalse(inc.increment(state),
                "next() should return false after last rank for n=" + n);
    }

    // =========================================================================
    // 2. Derangements are valid and lexicographically ordered
    // =========================================================================

    @Test
    @DisplayName("Derangements are valid and in lexicographic order for n=4..9")
    void testDerangementsValidAndOrdered() {
        for (int n = 4; n <= 9; n++) {
            BigInteger total = ALG.derangementCount(n);
            DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
            DerangadicIncrement.DerangadicState state = inc.initialState(n);

            int[] previous = null;
            for (long rank = 0; rank < total.longValue(); rank++) {
                int[] d = state.currentDerangement().clone();

                assertTrue(isValidDerangement(d),
                        String.format("Invalid derangement at n=%d rank=%d: %s",
                                n, rank, Arrays.toString(d)));

                if (previous != null) {
                    assertTrue(isLexLess(previous, d),
                            String.format("Order violation at n=%d rank=%d", n, rank));
                }

                previous = d;
                if (rank < total.longValue() - 1) inc.increment(state);
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
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);
        assertEquals(2, state.getDigits().length, "rank 0: digit length should be 2");

        inc.increment(state);
        assertEquals(4, state.getDigits().length, "rank 1: digit length should be 4");

        for (int i = 0; i < 8; i++) inc.increment(state);
        assertEquals(6, state.getDigits().length, "rank 9: digit length should be 6");

        for (int i = 0; i < 256; i++) inc.increment(state);
        assertEquals(8, state.getDigits().length, "rank 265: digit length should be 8");
    }

    @Test
    @DisplayName("Length boundary crossings for odd n=7: at ranks 2, 44")
    void testBoundaryCrossingsOddN7() {
        int n = 7;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);
        assertEquals(3, state.getDigits().length, "rank 0: digit length should be 3");

        inc.increment(state);
        assertEquals(3, state.getDigits().length, "rank 1: digit length should still be 3");

        inc.increment(state);
        assertEquals(5, state.getDigits().length, "rank 2: digit length should be 5");

        for (int i = 0; i < 42; i++) inc.increment(state);
        assertEquals(7, state.getDigits().length, "rank 44: digit length should be 7");
    }

    // =========================================================================
    // 4. current() returns a defensive copy
    // =========================================================================

    @Test
    @DisplayName("current() mutation does not affect internal state")
    void testCurrentReturnsCopy() {
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(4);
        int[] copy = state.getDigits();
        copy[0] = 99;
        assertNotEquals(99, state.getDigits()[0], "current() must return a defensive copy");
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
            DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
            DerangadicIncrement.DerangadicState state = inc.initialState(n);

            for (long rank = 0; rank < total.longValue(); rank++) {
                byIncrement.add(state.currentDerangement().clone());
                if (rank < total.longValue() - 1) inc.increment(state);
            }

            for (long rank = 0; rank < total.longValue(); rank++) {
                int[] expected = ALG.toDerangement(ALG.toDerangadic(rank, n), n);
                assertArrayEquals(expected, byIncrement.get((int) rank),
                        "Derangement mismatch at n=" + n + " rank=" + rank);
            }
        }
    }

    @Test
    @DisplayName("Constructor with rank seeds digits and live derangement at non-zero ranks")
    void testConstructorWithNonZeroRank() {
        int[] sizes = {6, 7, 12, 13, 1000};
        long[] starts = {1, 2, 8, 9, 20, 1_000, 50_000};

        for (int n : sizes) {
            BigInteger total = ALG.derangementCount(n);
            for (long start : starts) {
                if (BigInteger.valueOf(start + 10).compareTo(total) >= 0) continue;

                DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
                DerangadicIncrement.DerangadicState state = inc.initialState(n, BigInteger.valueOf(start));
                for (long rank = start; rank < start + 10; rank++) {
                    assertTrue(arraysEqualIgnoringTrailingZeros(ALG.toDerangadic(rank, n), state.getDigits()),
                            String.format("Digit mismatch at seeded n=%d rank=%d", n, rank));
                    assertArrayEquals(ALG.unrank(rank, n), state.currentDerangement(),
                            String.format("Live derangement mismatch at seeded n=%d rank=%d", n, rank));
                    if (rank < start + 9) inc.increment(state);
                }
            }
        }
    }

    // =========================================================================
    // 6. Reset functionality
    // =========================================================================

    @Test
    @DisplayName("Reset should correctly reposition the incrementor")
    void testReset() {
        int n = 8;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);

        // Advance to rank 5
        for (int i = 0; i < 5; i++) inc.increment(state);
        int[] after5 = state.getDigits();

        // Reset to rank 0 and advance to rank 5 again
        state = inc.initialState(n);
        for (int i = 0; i < 5; i++) inc.increment(state);
        int[] afterReset = state.getDigits();

        assertArrayEquals(after5, afterReset, "Reset should produce same state");

        // Reset to rank 10 directly
        state = inc.initialState(n, BigInteger.TEN);
        int[] at10 = state.getDigits();
        assertTrue(arraysEqualIgnoringTrailingZeros(ALG.toDerangadic(10, n), at10),
                "Reset to specific rank should match direct toDerangadic");
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
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(12);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], state.getDigits()),
                    String.format("n=12 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(state.getDigits())));
            if (rank < 20) inc.increment(state);
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
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(13);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], state.getDigits()),
                    String.format("n=13 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(state.getDigits())));
            if (rank < 20) inc.increment(state);
        }
    }

    // =========================================================================
    // 8. Large-n smoke tests
    // =========================================================================

    @Test
    @DisplayName("First 10 000 increments for n=100 produce valid derangements")
    void testSmokeTestN100() {
        int n = 100;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);
        for (int i = 0; i < 10_000; i++) {
            int[] d = state.currentDerangement();
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=100 iteration " + i);
            inc.increment(state);
        }
    }

    @Test
    @DisplayName("First 500 increments for n=5000 produce valid derangements")
    void testSmokeTestN5000() {
        int n = 5000;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);
        for (int i = 0; i < 500; i++) {
            int[] d = state.currentDerangement();
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=5000 iteration " + i);
            inc.increment(state);
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
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    void testFirstMillionRanksEvenAndOdd() {
        int maxRanks = 20_000_000;

        // 1. Test Even N (n=12)
        int nEven = 12;
        DerangadicIncrement incEven = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState stateEven = incEven.initialState(nEven);
        System.out.println("Starting " + maxRanks + " increment verifications for even n=" + nEven + "...");

        for (long rank = 0; rank < maxRanks; rank++) {
            int[] expectedDigits = ALG.toDerangadic(rank, nEven);
            int[] actualDigits = stateEven.getDigits();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at even n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nEven, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            int[] derangement = stateEven.currentDerangement();
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at even n=%d rank=%d: %s",
                        nEven, rank, Arrays.toString(derangement)));
            }

            incEven.increment(stateEven);
        }

        // 2. Test Odd N (n=13)
        int nOdd = 13;
        DerangadicIncrement incOdd = new DerangadicIncrement(calculator);
        DerangadicIncrement.DerangadicState stateOdd = incOdd.initialState(nOdd);
        System.out.println("Starting " + maxRanks + " increment verifications for odd n=" + nOdd + "...");

        for (long rank = 0; rank < maxRanks; rank++) {
            int[] expectedDigits = ALG.toDerangadic(rank, nOdd);
            int[] actualDigits = stateOdd.getDigits();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at odd n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nOdd, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            int[] derangement = stateOdd.currentDerangement();
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at odd n=%d rank=%d: %s",
                        nOdd, rank, Arrays.toString(derangement)));
            }

            incOdd.increment(stateOdd);
        }

        System.out.println("Successfully verified " + maxRanks + " unique states!");
    }

    @Test
    @DisplayName("Three-level API: incrementEncoded + encodedToDerangement works correctly")
    void testThreeLevelApi() {
        int n = 6;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);

        for (long rank = 0; rank < 265; rank++) {
            // Get derangement via encodedToDerangement (should be current)
            int[] derFromEncoded = inc.encodedToDerangement(state);
            int[] expected = ALG.unrank(rank, n);
            assertArrayEquals(expected, derFromEncoded,
                    "encodedToDerangement should return current derangement at rank " + rank);

            // Advance using incrementEncoded (not increment)
            if (rank < 264) {
                assertTrue(inc.incrementEncoded(state),
                        "incrementEncoded should succeed at rank " + rank);
            }
        }
    }

    @Test
    @DisplayName("increment() and incrementEncoded() produce identical results")
    void testIncrementAndIncrementEncodedAreEquivalent() {
        int n = 6;
        DerangadicIncrement inc1 = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state1 = inc1.initialState(n);

        DerangadicIncrement inc2 = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state2 = inc2.initialState(n);

        for (int i = 0; i < 100; i++) {
            assertArrayEquals(state1.getDigits(), state2.getDigits(),
                    "States should be equal before increment");
            assertArrayEquals(state1.currentDerangement(), state2.currentDerangement(),
                    "Derangements should be equal before increment");

            inc1.increment(state1);
            inc2.incrementEncoded(state2);

            assertArrayEquals(state1.getDigits(), state2.getDigits(),
                    "Digits should be equal after increment " + i);
            assertArrayEquals(state1.currentDerangement(), state2.currentDerangement(),
                    "Derangements should be equal after increment " + i);
        }
    }

    @Test
    @DisplayName("encodedToDerangement returns the live array (same reference)")
    void testEncodedToDerangementReturnsLiveArray() {
        int n = 6;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);

        int[] der1 = inc.encodedToDerangement(state);
        int[] der2 = inc.encodedToDerangement(state);

        assertSame(der1, der2, "encodedToDerangement should return the same array reference");
        assertSame(state.currentDerangement(), der1,
                "Should match state.currentDerangement()");
    }

    @Test
    @DisplayName("incrementEncoded keeps derangement current without calling encodedToDerangement")
    void testIncrementEncodedKeepsDerangementCurrent() {
        int n = 6;
        DerangadicIncrement inc = new DerangadicIncrement(new Calculator());
        DerangadicIncrement.DerangadicState state = inc.initialState(n);

        for (long rank = 0; rank < 100; rank++) {
            int[] derBefore = state.currentDerangement().clone();
            int[] expected = ALG.unrank(rank, n);
            assertArrayEquals(expected, derBefore, "Derangement at rank " + rank);

            if (rank < 99) {
                inc.incrementEncoded(state);
                // derAfter should be rank+1 without calling encodedToDerangement
                int[] derAfter = state.currentDerangement();
                int[] expectedNext = ALG.unrank(rank + 1, n);
                assertArrayEquals(expectedNext, derAfter,
                        "incrementEncoded should update derangement to rank " + (rank + 1));
            }
        }
    }
}
