/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicIncrementStateMachine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.isLexLess;
import static io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithmsTest.isValidDerangement;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DerangadicIncrementStateMachine}.
 * <p>
 * Verifies correctness of the stateful increment machine against the
 * stateless DerangadicAlgorithms reference implementation.
 *
 * @author Deepesh Patel & Aditya Patel
 * @version 3.0.2
 */
public class DerangadicIncrementStateMachineTest {

    private static final Calculator CALC = new Calculator();
    private static final DerangadicAlgorithms ALG = new DerangadicAlgorithms(CALC);


    // =========================================================================
    // 1. Digit sequence matches toDerangadic exactly for every rank
    // =========================================================================

    @ParameterizedTest(name = "n={0}, all {1} ranks")
    @CsvSource({
            "4, 9",
            "5, 44",
            "6, 265",
            "7, 1854",
            "8, 14833",
            "9, 133496"
    })
    @DisplayName("Digit sequence matches toDerangadic for complete small domains")
    void testDigitSequenceMatchesReference(int n, long expectedCount) {
        assertEquals(expectedCount, ALG.derangementCount(n).longValue(), "Unexpected derangement count for n=" + n);
        assertDigitSequenceMatches(n);
    }

    private void assertDigitSequenceMatches(int n) {
        BigInteger total = ALG.derangementCount(n);
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);

        for (long rank = 0; rank < total.longValue(); rank++) {
            int[] expected = ALG.toDerangadic(rank, n);
            int[] actual = machine.encoded();

            assertTrue(arraysEqualIgnoringTrailingZeros(expected, actual),
                    String.format("Digit mismatch at n=%d rank=%d%n  expected: %s%n  actual:   %s",
                            n, rank, Arrays.toString(expected), Arrays.toString(actual)));

            if (rank < total.longValue() - 1) {
                assertTrue(machine.increment(),
                        "increment() returned false before last rank at n=" + n + ", rank=" + rank);
            }
        }

        assertFalse(machine.increment(),
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
            DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);

            int[] previous = null;
            for (long rank = 0; rank < total.longValue(); rank++) {
                int[] d = machine.derangement().clone();

                assertTrue(isValidDerangement(d),
                        String.format("Invalid derangement at n=%d rank=%d: %s",
                                n, rank, Arrays.toString(d)));

                if (previous != null) {
                    assertTrue(isLexLess(previous, d),
                            String.format("Order violation at n=%d rank=%d", n, rank));
                }

                previous = d;
                if (rank < total.longValue() - 1) machine.increment();
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
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        assertEquals(2, machine.encoded().length, "rank 0: digit length should be 2");

        machine.increment();
        assertEquals(4, machine.encoded().length, "rank 1: digit length should be 4");

        for (int i = 0; i < 8; i++) machine.increment();
        assertEquals(6, machine.encoded().length, "rank 9: digit length should be 6");

        for (int i = 0; i < 256; i++) machine.increment();
        assertEquals(8, machine.encoded().length, "rank 265: digit length should be 8");
    }

    @Test
    @DisplayName("Length boundary crossings for odd n=7: at ranks 2, 44")
    void testBoundaryCrossingsOddN7() {
        int n = 7;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        assertEquals(3, machine.encoded().length, "rank 0: digit length should be 3");

        machine.increment();
        assertEquals(3, machine.encoded().length, "rank 1: digit length should still be 3");

        machine.increment();
        assertEquals(5, machine.encoded().length, "rank 2: digit length should be 5");

        for (int i = 0; i < 42; i++) machine.increment();
        assertEquals(7, machine.encoded().length, "rank 44: digit length should be 7");
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
            DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);

            for (long rank = 0; rank < total.longValue(); rank++) {
                byIncrement.add(machine.derangement().clone());
                if (rank < total.longValue() - 1) machine.increment();
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

                DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.valueOf(start), CALC);
                for (long rank = start; rank < start + 10; rank++) {
                    assertTrue(arraysEqualIgnoringTrailingZeros(ALG.toDerangadic(rank, n), machine.encoded()),
                            String.format("Digit mismatch at seeded n=%d rank=%d", n, rank));
                    assertArrayEquals(ALG.unrank(rank, n), machine.derangement(),
                            String.format("Live derangement mismatch at seeded n=%d rank=%d", n, rank));
                    if (rank < start + 9) machine.increment();
                }
            }
        }
    }

    // =========================================================================
    // 6. Seeded construction equivalence
    // =========================================================================

    @Test
    @DisplayName("New machine seeded at a rank matches an incremented machine at that same rank")
    void testSeededMachineMatchesIncrementedMachine() {
        int n = 8;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);

        // Advance to rank 5
        for (int i = 0; i < 5; i++) machine.increment();
        int[] after5 = machine.encoded();

        // Reset to rank 0 and advance to rank 5 again
        DerangadicIncrementStateMachine resetMachine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        for (int i = 0; i < 5; i++) resetMachine.increment();
        int[] afterReset = resetMachine.encoded();

        assertArrayEquals(after5, afterReset, "Fresh machine advanced to rank 5 should produce same encoded state");

        // Reset to rank 10 directly
        DerangadicIncrementStateMachine rank10Machine = new DerangadicIncrementStateMachine(n, BigInteger.TEN, CALC);
        int[] at10 = rank10Machine.encoded();
        assertTrue(arraysEqualIgnoringTrailingZeros(ALG.toDerangadic(10, n), at10),
                "Direct construction at rank 10 should match direct toDerangadic");
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
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(12, BigInteger.ZERO, CALC);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], machine.encoded()),
                    String.format("n=12 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(machine.encoded())));
            if (rank < 20) machine.increment();
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
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(13, BigInteger.ZERO, CALC);
        for (int rank = 0; rank <= 20; rank++) {
            assertTrue(arraysEqualIgnoringTrailingZeros(expected[rank], machine.encoded()),
                    String.format("n=13 rank=%d%n  expected: %s%n  actual:   %s",
                            rank, Arrays.toString(expected[rank]), Arrays.toString(machine.encoded())));
            if (rank < 20) machine.increment();
        }
    }

    // =========================================================================
    // 8. Large-n smoke tests
    // =========================================================================

    @Test
    @DisplayName("First 10 000 increments for n=100 produce valid derangements")
    void testSmokeTestN100() {
        int n = 100;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        for (int i = 0; i < 10_000; i++) {
            int[] d = machine.derangement();
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=100 iteration " + i);
            machine.increment();
        }
    }

    @Test
    @DisplayName("First 500 increments for n=5000 produce valid derangements")
    void testSmokeTestN5000() {
        int n = 5000;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        for (int i = 0; i < 500; i++) {
            int[] d = machine.derangement();
            assertTrue(isValidDerangement(d),
                    "Invalid derangement at n=5000 iteration " + i);
            machine.increment();
        }
    }

    // =========================================================================
    // 9. incrementAndGetCarryLength tests (new for state machine)
    // =========================================================================

    @Test
    @DisplayName("incrementAndGetCarryLength advances full state and returns 0 after last rank")
    void testIncrementAndGetCarryLength() {
        int n = 6;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
        int lastRank = ALG.derangementCount(n).intValue() - 1;

        for (int rank = 1; rank <= lastRank; rank++) {
            int carry = machine.incrementAndGetCarryLength();
            assertTrue(carry > 0, "Carry length should be positive at rank " + rank);
            assertTrue(arraysEqualIgnoringTrailingZeros(ALG.toDerangadic(rank, n), machine.encoded()),
                    "Encoded mismatch at rank " + rank);
            assertArrayEquals(ALG.unrank(rank, n), machine.derangement(),
                    "Derangement mismatch at rank " + rank);
        }

        assertEquals(0, machine.incrementAndGetCarryLength(), "Final carry length should be 0");
    }

//    @Test
//    @DisplayName("incrementAndGetCarryLength matches manual carry counting")
//    void testIncrementAndGetCarryLengthMatchesManualCount() {
//        int n = 8;
//        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
//
//        for (int i = 0; i < 500; i++) {
//            int[] digitsBefore = machine.getEncoded();
//            int reportedCarry = machine.incrementAndGetCarryLength();
//
//            // Manually compute carry length by finding pivot
//            int actualCarry = 1;
//            for (int idx = 1; idx < digitsBefore.length; idx++) {
//                if (digitsBefore[idx] == 0) break;
//                actualCarry++;
//            }
//            // Adjust for LSD-encoding vs pivot logic
//            // The reported carry should be >= 2
//            assertTrue(reportedCarry >= 2, "Reported carry too small");
//        }
//    }

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

    @Test
    @DisplayName("Verify first 1 M ranks for even (n=12) and odd (n=13) elements")
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    void testFirstMillionRanksEvenAndOdd() {
        int maxRanks = 1_000_000;

        // 1. Test Even N (n=12)
        int nEven = 12;
        DerangadicIncrementStateMachine machineEven = new DerangadicIncrementStateMachine(nEven, BigInteger.ZERO, CALC);
        System.out.println("Starting " + maxRanks + " increment verifications for even n=" + nEven + "...");

        for (long rank = 0; rank < maxRanks; rank++) {
            int[] expectedDigits = ALG.toDerangadic(rank, nEven);
            int[] actualDigits = machineEven.encoded();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at even n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nEven, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            int[] derangement = machineEven.derangement();
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at even n=%d rank=%d: %s",
                        nEven, rank, Arrays.toString(derangement)));
            }

            machineEven.increment();
        }

        // 2. Test Odd N (n=13)
        int nOdd = 13;
        DerangadicIncrementStateMachine machineOdd = new DerangadicIncrementStateMachine(nOdd, BigInteger.ZERO, CALC);
        System.out.println("Starting " + maxRanks + " increment verifications for odd n=" + nOdd + "...");

        for (long rank = 0; rank < maxRanks; rank++) {
            int[] expectedDigits = ALG.toDerangadic(rank, nOdd);
            int[] actualDigits = machineOdd.encoded();

            if (!arraysEqualIgnoringTrailingZeros(expectedDigits, actualDigits)) {
                fail(String.format("Digit mismatch at odd n=%d rank=%d%n  expected: %s%n  actual:   %s",
                        nOdd, rank, Arrays.toString(expectedDigits), Arrays.toString(actualDigits)));
            }

            int[] derangement = machineOdd.derangement();
            if (!isValidDerangement(derangement)) {
                fail(String.format("Invalid derangement structurally broken at odd n=%d rank=%d: %s",
                        nOdd, rank, Arrays.toString(derangement)));
            }

            machineOdd.increment();
        }

        System.out.println("Successfully verified " + maxRanks + " unique states!");
    }

    // =========================================================================
    // Additional tests specific to state machine API
    // =========================================================================

    @Test
    @DisplayName("derangement() returns the live array (same reference)")
    void testDerangementReturnsLiveArray() {
        int n = 6;
        DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);

        int[] der1 = machine.derangement();
        int[] der2 = machine.derangement();

        assertSame(der1, der2, "derangement() should return the same array reference");
    }


    @Test
    @DisplayName("Consecutive encoded states decode to consecutive ranks")
    void testConsecutiveEncodedStatesDecodeToConsecutiveRanks() {
        int[] evenAndOddN = { 12,13 };

        for (int n : evenAndOddN) {
            DerangadicIncrementStateMachine machine = new DerangadicIncrementStateMachine(n, BigInteger.ZERO, CALC);
            for (int rank = 0; rank < 500; rank++) {
                assertEquals(BigInteger.valueOf(rank), ALG.fromDerangadic(machine.encoded(), n),
                        "Encoded state should decode to current rank for n=" + n + ", rank=" + rank);
                if (rank < 499) {
                    assertTrue(machine.increment());
                }
            }
        }
    }


    @Test
    @DisplayName("In-place increment keeps sub-microsecond average step time for representative n values")
    void testPerformanceOfInPlaceDerangement() {
        int[] nValues = {50, 100, 500, 1000, 5000, 8000};
        int iterations = 1000;
        long startRank = 0;//9_000_000_000L;

        long totalTime = 0;

        Calculator CALC = new Calculator();

        for (int n : nValues) {
            long incTime = measureFullIncrement(n, startRank, iterations ,CALC);
            totalTime+= incTime;
        }
        long averageNanos = totalTime/nValues.length;
        assertTrue(averageNanos < 1_000,
                "Average time per increment across tested n values should be under 1 microsecond, but was "
                        + averageNanos + " ns");
    }

    private long measureFullIncrement(int n, long startRank, int iterations, Calculator calculator) {

        var statMachine = new DerangadicIncrementStateMachine(n,BigInteger.valueOf(startRank), calculator);
        int sink = 0;
        // Warmup
        for (int i = 0; i < 200000; i++) {
            statMachine.increment();
            sink+= statMachine.derangement()[0]; // Prevent dead code elimination
        }

        //System.gc();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            statMachine.increment();
            sink+= statMachine.derangement()[0]; // Prevent dead code elimination
        }
        return (System.nanoTime() - startTime) / iterations;
    }
}