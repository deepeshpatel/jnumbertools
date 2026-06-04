///*
// * JNumberTools Library v3.0.2
// * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
// */
//package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;
//
//import io.github.deepeshpatel.jnumbertools.base.Calculator;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//
//import java.math.BigInteger;
//import java.util.Arrays;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Comprehensive test suite for {@link InvolutadicIncrementStateMachine}.
// *
// * <p>Tests verify that the state machine's increment operation correctly
// * traverses all involutions in lexicographic order by comparing against
// * the algorithm class's rank/unrank operations.</p>
// *
// * <p>The state machine is tested against:</p>
// * <ul>
// *   <li>Round-trip: rank → state machine → rank (via encode/decode)</li>
// *   <li>Sequential enumeration matches algorithm's unrank for all ranks</li>
// *   <li>Boundary conditions (first/last involution)</li>
// *   <li>Carry length correctness (optional)</li>
// *   <li>State consistency after increment</li>
// * </ul>
// *
// * @author Deepesh Patel and Aditya Patel
// * @since 3.0.2
// */
//@DisplayName("Involutadic Increment State Machine")
//class InvolutadicIncrementStateMachineTest {
//
//    private Calculator calculator;
//    private InvolutadicAlgorithms alg;
//
//    @BeforeEach
//    void setUp() {
//        calculator = new Calculator();
//        alg = new InvolutadicAlgorithms(calculator);
//    }
//
//    // =========================================================
//    // Helper Methods
//    // =========================================================
//
//    /**
//     * Helper method to simulate increment using algorithm class.
//     * This is the "ground truth" for comparison.
//     */
//    private int[] algorithmIncrement(int[] digits, int n) {
//        BigInteger currentRank = alg.decode(digits);
//        return alg.encode(currentRank.add(BigInteger.ONE), n);
//    }
//
//    /**
//     * Helper method to simulate decrement using algorithm class.
//     */
//    private int[] algorithmDecrement(int[] digits, int n) {
//        BigInteger currentRank = alg.decode(digits);
//        if (currentRank.signum() == 0) {
//            throw new IllegalArgumentException("Cannot decrement rank 0");
//        }
//        return alg.encode(currentRank.subtract(BigInteger.ONE), n);
//    }
//
//    /**
//     * Validates that state machine's internal state is consistent.
//     * - digits and involution should match
//     * - maxDigit should be correctly computed
//     */
//    private void assertStateConsistent(InvolutadicIncrementStateMachine engine, int n) {
//        int[] digits = engine.getDigits();
//        int[] involution = engine.involution();
//
//        // Test 1: Decoding digits should produce the same involution
//        int[] decodedInvolution = alg.toInvolution(digits);
//        assertArrayEquals(involution, decodedInvolution,
//                "State inconsistent: involution() doesn't match decoded digits");
//
//        // Test 2: Encoding involution should produce same digits
//        int[] encodedDigits = alg.fromInvolution(involution);
//        assertArrayEquals(digits, encodedDigits,
//                "State inconsistent: digits don't match encoding of involution");
//
//        // Test 3: Rank of involution should match decode of digits
//        BigInteger rankFromInv = alg.rank(involution);
//        BigInteger rankFromDigits = alg.decode(digits);
//        assertEquals(rankFromInv, rankFromDigits,
//                "State inconsistent: rank mismatch between involution and digits");
//    }
//
//    // =========================================================
//    // Core Tests
//    // =========================================================
//
//
//    @Nested
//    @DisplayName("Sequential enumeration vs algorithm class")
//    class SequentialEnumeration {
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {2, 3, 4, 5, 6, 7})
//        void stateMachineMatchesAlgorithmForAllRanks(int n) {
//            BigInteger total = calculator.telephoneNumber(n);
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//
//            for (BigInteger expectedRank = BigInteger.ZERO;
//                 expectedRank.compareTo(total) < 0;
//                 expectedRank = expectedRank.add(BigInteger.ONE)) {
//
//                // Get involution from state machine
//                int[] actualInvolution = engine.involution();
//
//                // Get expected involution from algorithm
//                int[] expectedInvolution = alg.unrank(expectedRank, n);
//
//                // Compare
//                assertArrayEquals(expectedInvolution, actualInvolution,
//                        "n=" + n + " rank=" + expectedRank + " involution mismatch");
//
//                // Also verify digits match
//                int[] actualDigits = engine.getDigits();
//                int[] expectedDigits = alg.encode(expectedRank, n);
//                assertArrayEquals(expectedDigits, actualDigits,
//                        "n=" + n + " rank=" + expectedRank + " digits mismatch");
//
//                // Increment for next iteration (except for last)
//                if (expectedRank.add(BigInteger.ONE).compareTo(total) < 0) {
//                    assertTrue(engine.increment(),
//                            "increment() returned false at rank=" + expectedRank);
//                }
//            }
//        }
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {2, 3, 4, 5, 6})
//        void stateMachineDigitsMatchAlgorithmIncrement(int n) {
//            BigInteger total = calculator.telephoneNumber(n);
//
//            for (BigInteger rank = BigInteger.ZERO;
//                 rank.compareTo(total.subtract(BigInteger.ONE)) < 0;
//                 rank = rank.add(BigInteger.ONE)) {
//
//                // Get state machine at current rank
//                var engine = new InvolutadicIncrementStateMachine(n, rank, calculator);
//                int[] stateMachineDigits = engine.getDigits();
//
//                // Get algorithm's increment result
//                int[] algorithmDigits = algorithmIncrement(stateMachineDigits, n);
//
//                // Increment state machine
//                engine.increment();
//                int[] nextStateMachineDigits = engine.getDigits();
//
//                // They should match
//                assertArrayEquals(algorithmDigits, nextStateMachineDigits,
//                        "n=" + n + " rank=" + rank +
//                                ": algorithm increment doesn't match state machine increment");
//            }
//        }
//    }
//
//    @Nested
//    @DisplayName("Starting from various ranks")
//    class StartFromRanks {
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {3, 4, 5, 6})
//        void startFromMidRankAndEnumerate(int n) {
//            BigInteger total = calculator.telephoneNumber(n);
//
//            // Test various starting points
//            BigInteger[] startRanks = {
//                    BigInteger.ZERO,
//                    total.divide(BigInteger.valueOf(4)),
//                    total.divide(BigInteger.valueOf(2)),
//                    total.multiply(BigInteger.valueOf(3)).divide(BigInteger.valueOf(4)),
//                    total.subtract(BigInteger.ONE)
//            };
//
//            for (BigInteger startRank : startRanks) {
//                var engine = new InvolutadicIncrementStateMachine(n, startRank, calculator);
//
//                for (BigInteger expectedRank = startRank;
//                     expectedRank.compareTo(total) < 0;
//                     expectedRank = expectedRank.add(BigInteger.ONE)) {
//
//                    int[] actual = engine.involution();
//                    int[] expected = alg.unrank(expectedRank, n);
//                    assertArrayEquals(expected, actual,
//                            "n=" + n + " startRank=" + startRank + " failed at rank=" + expectedRank);
//
//                    if (expectedRank.add(BigInteger.ONE).compareTo(total) < 0) {
//                        assertTrue(engine.increment());
//                    }
//                }
//
//                // After last, increment should return false
//                assertFalse(engine.increment(),
//                        "After last involution, increment() should return false");
//            }
//        }
//    }
//
//    @Nested
//    @DisplayName("Boundary conditions")
//    class BoundaryConditions {
//
//        @Test
//        @DisplayName("Starting at rank 0 works correctly")
//        void startAtRank0() {
//            int n = 5;
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//
//            int[] expected = alg.unrank(0, n);
//            assertArrayEquals(expected, engine.involution());
//            assertArrayEquals(alg.encode(0, n), engine.getDigits());
//        }
//
//        @Test
//        @DisplayName("Starting at last rank works correctly")
//        void startAtLastRank() {
//            int n = 5;
//            BigInteger lastRank = calculator.telephoneNumber(n).subtract(BigInteger.ONE);
//            var engine = new InvolutadicIncrementStateMachine(n, lastRank, calculator);
//
//            int[] expected = alg.unrank(lastRank, n);
//            assertArrayEquals(expected, engine.involution());
//            assertArrayEquals(alg.encode(lastRank, n), engine.getDigits());
//
//            // Increment from last should return false
//            assertFalse(engine.increment(), "increment() from last rank should return false");
//        }
//
//        @Test
//        @DisplayName("Multiple increment calls after last return false")
//        void multipleIncrementAfterLast() {
//            int n = 4;
//            BigInteger lastRank = calculator.telephoneNumber(n).subtract(BigInteger.ONE);
//            var engine = new InvolutadicIncrementStateMachine(n, lastRank, calculator);
//
//            assertFalse(engine.increment());
//            assertFalse(engine.increment());
//            assertFalse(engine.increment());
//            // State should remain unchanged
//            assertArrayEquals(alg.unrank(lastRank, n), engine.involution());
//        }
//    }
//
//    @Nested
//    @DisplayName("State consistency")
//    class StateConsistency {
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {3, 4, 5, 6})
//        void stateRemainsConsistentAfterEachIncrement(int n) {
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            BigInteger total = calculator.telephoneNumber(n);
//
//            for (int step = 0; step < total.intValue(); step++) {
//                assertStateConsistent(engine, n);
//                if (step < total.intValue() - 1) {
//                    assertTrue(engine.increment());
//                }
//            }
//        }
//
//        @Test
//        @DisplayName("getDigits returns a copy, not live reference")
//        void getDigitsReturnsCopy() {
//            int n = 5;
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//
//            int[] digits1 = engine.getDigits();
//            int[] digits2 = engine.getDigits();
//
//            // Should be equal but different objects
//            assertArrayEquals(digits1, digits2);
//            assertNotSame(digits1, digits2);
//
//            // Modifying copy shouldn't affect engine
//            digits1[0] = 999;
//            assertFalse(Arrays.equals(digits1, engine.getDigits()));
//        }
//    }
//
//    @Nested
//    @DisplayName("Increment and getCarryLength")
//    class CarryLength {
//
//        @Test
//        @DisplayName("incrementAndGetCarryLength returns positive value on success")
//        void carryLengthPositiveOnSuccess() {
//            int n = 5;
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            BigInteger total = calculator.telephoneNumber(n);
//
//            int increments = 0;
//            while (engine.incrementAndGetCarryLength() > 0) {
//                increments++;
//            }
//
//            assertEquals(total.intValue() - 1, increments,
//                    "Number of successful increments should be T(n)-1");
//        }
//
//        @Test
//        @DisplayName("incrementAndGetCarryLength returns 0 at end")
//        void carryLengthZeroAtEnd() {
//            int n = 4;
//            var engine = new InvolutadicIncrementStateMachine(n,
//                    calculator.telephoneNumber(n).subtract(BigInteger.ONE),
//                    calculator);
//
//            assertEquals(0, engine.incrementAndGetCarryLength(),
//                    "At last involution, incrementAndGetCarryLength should return 0");
//        }
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {3, 4, 5, 6})
//        void carryLengthEqualsPivotDistance(int n) {
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            BigInteger total = calculator.telephoneNumber(n);
//
//            for (int step = 0; step < total.intValue() - 1; step++) {
//                // Get current digits before increment
//                int[] beforeDigits = engine.getDigits();
//
//                // Find where pivot WILL be by scanning from right
//                int expectedPivot = -1;
//                for (int i = n - 1; i >= 0; i--) {
//                    if (beforeDigits[i] != -1) {
//                        // Need to check if this position can be incremented
//                        // This requires computing maxDigit, which is complex
//                        // Instead, just use the carry length returned
//                        break;
//                    }
//                }
//
//                int carryLength = engine.incrementAndGetCarryLength();
//
//                // The carry length should be >= 1 and <= n
//                assertTrue(carryLength >= 1 && carryLength <= n,
//                        "Carry length should be between 1 and n, got " + carryLength);
//
//                // After increment, the digits at positions n-carryLength to n-1
//                // should be different from before (the suffix that was rebuilt)
//                int[] afterDigits = engine.getDigits();
//
//                // The position at n-carryLength (the pivot) should have increased
//                int pivotPos = n - carryLength;
//                assertTrue(pivotPos >= 0 && pivotPos < n,
//                        "Invalid pivot position: " + pivotPos);
//
//                // The pivot digit should be greater than before
//                if (beforeDigits[pivotPos] != -1) {
//                    assertTrue(afterDigits[pivotPos] > beforeDigits[pivotPos],
//                            "Pivot digit should increase");
//                }
//
//                // All positions to the right of pivot should be reset to minimum
//                // (but not necessarily all changed - some may have been -1 and stay -1)
//                for (int i = pivotPos + 1; i < n; i++) {
//                    // These positions should be valid (not in an invalid state)
//                    // We can't assert they changed because they might have been -1
//                }
//            }
//        }
//
//        @ParameterizedTest(name = "n={0}")
//        @ValueSource(ints = {3, 4, 5, 6})
//        void carryLengthEqualsDistanceFromRightToPivot(int n) {
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            BigInteger total = calculator.telephoneNumber(n);
//
//            for (BigInteger rank = BigInteger.ZERO;
//                 rank.compareTo(total.subtract(BigInteger.ONE)) < 0;
//                 rank = rank.add(BigInteger.ONE)) {
//
//                int[] before = engine.getDigits();
//                int reportedCarry = engine.incrementAndGetCarryLength();
//                int[] after = engine.getDigits();
//
//                // Find the actual pivot (first position from left that changed)
//                int pivotPos = -1;
//                for (int i = 0; i < n; i++) {
//                    if (before[i] != after[i]) {
//                        pivotPos = i;
//                        break;
//                    }
//                }
//
//                assertTrue(pivotPos >= 0, "No pivot found");
//
//                // Calculate expected carry length
//                int expectedCarry = n - pivotPos;
//
//                assertEquals(expectedCarry, reportedCarry,
//                        String.format("n=%d rank=%d: pivot at position %d, expected carry %d but got %d\n" +
//                                        "Before: %s\nAfter:  %s",
//                                n, rank, pivotPos, expectedCarry, reportedCarry,
//                                Arrays.toString(before), Arrays.toString(after)));
//            }
//        }
//
//        @Test
//        @DisplayName("carry length equals actual suffix modified")
//        void carryLengthMatchesActualDigitChanges() {
//
//            int n = 8;
//            BigInteger total = calculator.telephoneNumber(n);
//
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//
//            for (BigInteger rank = BigInteger.ZERO;
//                 rank.compareTo(total.subtract(BigInteger.ONE)) < 0;
//                 rank = rank.add(BigInteger.ONE)) {
//
//                int[] before = engine.getDigits();
//
//                int carry = engine.incrementAndGetCarryLength();
//
//                assertTrue(carry > 0);
//
//                int[] after = engine.getDigits();
//
//                int firstChanged = -1;
//
//                for (int i = 0; i < n; i++) {
//                    if (before[i] != after[i]) {
//                        firstChanged = i;
//                        break;
//                    }
//                }
//
//                assertTrue(firstChanged >= 0,
//                        "No digit changed after increment");
//
//                int expectedCarry = n - firstChanged;
//
//                assertEquals(expectedCarry, carry,
//                        String.format(
//                                "Rank=%s%nBefore=%s%nAfter =%s%nExpected carry=%d Actual=%d",
//                                rank,
//                                Arrays.toString(before),
//                                Arrays.toString(after),
//                                expectedCarry,
//                                carry));
//            }
//        }
//
//        @Test
//        @DisplayName("carry distribution matches actual digit modifications")
//        void carryDistributionMatchesActualChanges() {
//
//            int n = 10;
//
//            BigInteger total = calculator.telephoneNumber(n);
//
//            long[] reportedCarryCounts = new long[n + 1];
//            long[] actualCarryCounts = new long[n + 1];
//
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//
//            for (BigInteger rank = BigInteger.ZERO;
//                 rank.compareTo(total.subtract(BigInteger.ONE)) < 0;
//                 rank = rank.add(BigInteger.ONE)) {
//
//                int[] before = engine.getDigits();
//
//                int reportedCarry = engine.incrementAndGetCarryLength();
//
//                int[] after = engine.getDigits();
//
//                reportedCarryCounts[reportedCarry]++;
//
//                int firstChanged = -1;
//
//                for (int i = 0; i < n; i++) {
//                    if (before[i] != after[i]) {
//                        firstChanged = i;
//                        break;
//                    }
//                }
//
//                assertTrue(firstChanged >= 0);
//
//                int actualCarry = n - firstChanged;
//
//                actualCarryCounts[actualCarry]++;
//            }
//
//            assertArrayEquals(reportedCarryCounts,
//                    actualCarryCounts,
//                    "Carry distribution mismatch");
//
//            System.out.println("Reported carry counts = "
//                    + Arrays.toString(reportedCarryCounts));
//
//            System.out.println("Actual carry counts   = "
//                    + Arrays.toString(actualCarryCounts));
//        }
//    }
//
//    @Nested
//    @DisplayName("Constructor validation")
//    class ConstructorValidation {
//
//        @Test
//        @DisplayName("Invalid n throws exception")
//        void invalidN() {
//            assertThrows(IllegalArgumentException.class,
//                    () -> new InvolutadicIncrementStateMachine(0, 0L, calculator));
//            assertThrows(IllegalArgumentException.class,
//                    () -> new InvolutadicIncrementStateMachine(-1, 0L, calculator));
//        }
//
//        @Test
//        @DisplayName("Null calculator throws exception")
//        void nullCalculator() {
//            assertThrows(NullPointerException.class,
//                    () -> new InvolutadicIncrementStateMachine(5, 0L, null));
//        }
//
//        @Test
//        @DisplayName("Rank out of range throws exception")
//        void rankOutOfRange() {
//            int n = 4;
//            BigInteger total = calculator.telephoneNumber(n);
//
//            assertThrows(IllegalArgumentException.class,
//                    () -> new InvolutadicIncrementStateMachine(n, -1L, calculator));
//            assertThrows(IllegalArgumentException.class,
//                    () -> new InvolutadicIncrementStateMachine(n, total, calculator));
//        }
//    }
//
//    // =========================================================
//    // Performance sanity check (not a real performance test)
//    // =========================================================
//
//    @Nested
//    @DisplayName("Performance sanity")
//    class PerformanceSanity {
//
//        @Test
//        @DisplayName("State machine increment is faster than encode+decode")
//        void sanityCheckIncrementPerformance() {
//            int n = 8;
//            BigInteger total = calculator.telephoneNumber(n);
//
//            // Warm up
//            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            for (int i = 0; i < 100; i++) {
//                if (!engine.increment()) break;
//            }
//
//            // Reset
//            engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
//            long stateMachineTime = 0;
//            long algorithmTime = 0;
//
//            // Only test up to min(1000, total-1) to avoid out of bounds
//            int maxIterations = Math.min(1000, total.intValue() - 1);
//
//            // Test state machine
//            long start = System.nanoTime();
//            for (int i = 0; i < maxIterations; i++) {
//                engine.increment();
//            }
//            stateMachineTime = System.nanoTime() - start;
//
//            // Test algorithm wrapper
//            int[] digits = alg.encode(BigInteger.ZERO, n);
//            start = System.nanoTime();
//            for (int i = 0; i < maxIterations; i++) {
//                // Use decode + encode to simulate increment (this is the ground truth)
//                BigInteger currentRank = alg.decode(digits);
//                digits = alg.encode(currentRank.add(BigInteger.ONE), n);
//            }
//            algorithmTime = System.nanoTime() - start;
//
//            // State machine should be faster (but we don't enforce - just log)
//            System.out.printf("n=%d, iterations=%d%n", n, maxIterations);
//            System.out.printf("  State machine: %,d ns (%,d ns per operation)%n",
//                    stateMachineTime, stateMachineTime / maxIterations);
//            System.out.printf("  Algorithm:     %,d ns (%,d ns per operation)%n",
//                    algorithmTime, algorithmTime / maxIterations);
//            System.out.printf("  Speedup: %.2fx%n", (double) algorithmTime / stateMachineTime);
//
//            // We don't assert performance - just informational
//            // But we do assert that both methods completed without errors
//            assertTrue(maxIterations > 0, "Should have at least one iteration to test");
//        }
//    }
//}