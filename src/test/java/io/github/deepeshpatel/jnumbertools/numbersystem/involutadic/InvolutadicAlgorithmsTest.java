/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.involutadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Involutadic Number System.
 *
 * <p>Tests mirror the structure of {@code DerangadicAlgorithmsTest} and cover:
 * <ul>
 *   <li>Telephone-number counts</li>
 *   <li>Round-trip: rank → digits → rank</li>
 *   <li>Round-trip: rank → involution → rank</li>
 *   <li>Exhaustive verification for small n (all T(n) involutions)</li>
 *   <li>Lexicographic order preservation</li>
 *   <li>Increment machine correctness</li>
 *   <li>Increment machine structural consistency (digit-array length varies)</li>
 *   <li>Boundary conditions (rank 0, rank T(n)-1)</li>
 * </ul>
 *
 * @author Deepesh Patel and Aditya Patel
 */
@DisplayName("Involutadic Number System")
class InvolutadicAlgorithmsTest {

    private Calculator calculator;
    private InvolutadicAlgorithms alg;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
        alg = new InvolutadicAlgorithms(calculator);
    }

    // =========================================================
    // 1. Telephone-number counts
    // =========================================================

    @Nested
    @DisplayName("Telephone numbers T(n)")
    class TelephoneNumbers {

        @Test
        @DisplayName("T(0)=1, T(1)=1, T(2)=2, T(3)=4, T(4)=10, T(5)=26, T(6)=76")
        void smallValues() {
            long[] expected = {1, 1, 2, 4, 10, 26, 76, 232, 764, 2620};
            for (int n = 0; n < expected.length; n++) {
                assertEquals(BigInteger.valueOf(expected[n]), alg.involutionCount(n),
                        "T(" + n + ")");
            }
        }

        @Test
        @DisplayName("Recurrence T(n) = T(n-1) + (n-1)*T(n-2)")
        void recurrenceSatisfied() {
            for (int n = 2; n <= 12; n++) {
                BigInteger expected = alg.involutionCount(n - 1)
                        .add(BigInteger.valueOf(n - 1).multiply(alg.involutionCount(n - 2)));
                assertEquals(expected, alg.involutionCount(n),
                        "recurrence at n=" + n);
            }
        }
    }

    // =========================================================
    // 2. Round-trips: rank ↔ digits ↔ involution
    // =========================================================

    @Nested
    @DisplayName("Round-trip: rank ↔ digits")
    class DigitsRoundTrip {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        void rankToDigitsAndBack(int n) {
            int total = alg.involutionCount(n).intValueExact();
            for (int rank = 0; rank < total; rank++) {
                int[] digits = alg.toInvolutadic(rank, n);
                BigInteger recovered = alg.fromInvolutadic(digits, n);
                assertEquals(BigInteger.valueOf(rank), recovered,
                        "n=" + n + " rank=" + rank + " digits=" + Arrays.toString(digits));
            }
        }

        @Test
        @DisplayName("Large rank round-trip for n=15")
        void largeN() {
            int n = 15;
            BigInteger total = alg.involutionCount(n);
            for (BigInteger rank : List.of(
                    BigInteger.ZERO,
                    total.subtract(BigInteger.ONE),
                    total.divide(BigInteger.TWO))) {
                int[] digits = alg.toInvolutadic(rank, n);
                BigInteger recovered = alg.fromInvolutadic(digits, n);
                assertEquals(rank, recovered, "n=" + n + " rank=" + rank);
            }
        }
    }

    @Nested
    @DisplayName("Round-trip: rank ↔ involution")
    class InvolutionRoundTrip {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        void rankToInvolutionAndBack(int n) {
            int total = alg.involutionCount(n).intValueExact();
            for (int rank = 0; rank < total; rank++) {
                int[] inv = alg.unrank(rank, n);
                assertTrue(isValidInvolution(inv), "n=" + n + " rank=" + rank + " invalid involution");
                BigInteger recovered = alg.rank(inv, n);
                assertEquals(BigInteger.valueOf(rank), recovered,
                        "n=" + n + " rank=" + rank);
            }
        }

        @Test
        @DisplayName("Known involutions for n=4")
        void knownN4() {
            int[][] expected = {
                    {0, 1, 2, 3}, {0, 1, 3, 2}, {0, 2, 1, 3}, {0, 3, 2, 1},
                    {1, 0, 2, 3}, {1, 0, 3, 2}, {2, 1, 0, 3}, {2, 3, 0, 1},
                    {3, 1, 2, 0}, {3, 2, 1, 0},
            };
            for (int rank = 0; rank < expected.length; rank++) {
                assertArrayEquals(expected[rank], alg.unrank(rank, 4), "n=4 rank=" + rank);
            }
        }

        @Test
        @DisplayName("Known involutions for n=5")
        void knownN5() {
            int[][] expectedFirst = {
                    {0, 1, 2, 3, 4}, {0, 1, 2, 4, 3}, {0, 1, 3, 2, 4},
                    {0, 1, 4, 3, 2}, {0, 2, 1, 3, 4},
            };
            int[][] expectedLast = {
                    {4, 1, 2, 3, 0}, {4, 1, 3, 2, 0}, {4, 2, 1, 3, 0}, {4, 3, 2, 1, 0},
            };
            for (int rank = 0; rank < expectedFirst.length; rank++) {
                assertArrayEquals(expectedFirst[rank], alg.unrank(rank, 5), "n=5 rank=" + rank);
            }
            for (int i = 0; i < expectedLast.length; i++) {
                assertArrayEquals(expectedLast[i], alg.unrank(22 + i, 5), "n=5 rank=" + (22 + i));
            }
        }
    }

    // =========================================================
    // 3. Lexicographic order preservation
    // =========================================================

    @Nested
    @DisplayName("Lexicographic order")
    class LexOrder {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {3, 4, 5, 6, 7})
        void allInvolutionsAreInLexOrder(int n) {
            int total = alg.involutionCount(n).intValueExact();
            int[] prev = alg.unrank(0, n);
            for (int rank = 1; rank < total; rank++) {
                int[] curr = alg.unrank(rank, n);
                assertTrue(lexLessThan(prev, curr),
                        "n=" + n + " rank " + (rank - 1) + " not lex-before rank " + rank
                                + ": " + Arrays.toString(prev) + " vs " + Arrays.toString(curr));
                prev = curr;
            }
        }
    }

    // =========================================================
    // 4. Known Involutadic digit arrays
    // =========================================================

    @Nested
    @DisplayName("Known digit arrays")
    class KnownDigits {

        @Test
        @DisplayName("Digit arrays for n=4 (LSD-first)")
        void digitsN4() {
            int[][] expectedDigits = {
                    {0, 0, 0, 0}, // rank 0
                    {1, 0, 0},    // rank 1
                    {0, 1, 0},    // rank 2
                    {0, 2, 0},    // rank 3
                    {0, 0, 1},    // rank 4
                    {1, 1},       // rank 5
                    {0, 0, 2},    // rank 6
                    {1, 2},       // rank 7
                    {0, 0, 3},    // rank 8
                    {1, 3},       // rank 9
            };
            for (int rank = 0; rank < expectedDigits.length; rank++) {
                assertArrayEquals(expectedDigits[rank], alg.toInvolutadic(rank, 4),
                        "n=4 rank=" + rank);
            }
        }

        @Test
        @DisplayName("Digit array lengths for n=4")
        void digitLengthsN4() {
            int[] expectedLengths = {4, 3, 3, 3, 3, 2, 3, 2, 3, 2};
            for (int rank = 0; rank < expectedLengths.length; rank++) {
                int[] digits = alg.toInvolutadic(rank, 4);
                assertEquals(expectedLengths[rank], digits.length,
                        "n=4 rank=" + rank + " length mismatch");
            }
        }
    }

    // =========================================================
    // 5. Increment machine: correctness
    // =========================================================

    @Nested
    @DisplayName("Increment machine: correctness")
    class IncrementMachine {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {2, 3, 4, 5, 6, 7})
        void exhaustiveIncrementMatchesBruteForce(int n) {
            List<int[]> expected = allInvolutionsLex(n);
            int total = expected.size();
            assertEquals(alg.involutionCount(n).intValue(), total);

            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
            int step = 0;
            do {
                int[] actual = engine.involution();
                assertArrayEquals(expected.get(step), actual,
                        "n=" + n + " step=" + step
                                + " expected=" + Arrays.toString(expected.get(step))
                                + " actual="  + Arrays.toString(actual));
                step++;
            } while (engine.increment());

            assertEquals(total, step, "n=" + n + " total steps mismatch");
        }

        @Test
        @DisplayName("Starting from mid-rank enumerates the correct suffix")
        void startFromMidRank() {
            int n = 6;
            int startRank = 30;
            int total = alg.involutionCount(n).intValue();
            List<int[]> all = allInvolutionsLex(n);

            var engine = new InvolutadicIncrementStateMachine(n, startRank, calculator);
            for (int rank = startRank; rank < total; rank++) {
                assertArrayEquals(all.get(rank), engine.involution(),
                        "n=" + n + " rank=" + rank);
                if (rank < total - 1) {
                    assertTrue(engine.increment(), "increment returned false early at rank=" + rank);
                }
            }
            assertFalse(engine.increment(), "increment should return false after last");
        }

        @Test
        @DisplayName("Increment returns false exactly once after last involution")
        void returnsFalseAtEnd() {
            int n = 4;
            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
            int steps = 0;
            while (engine.increment()) steps++;
            assertEquals(alg.involutionCount(n).intValue() - 1, steps,
                    "n=4: should take T(4)-1=9 successful increments");
        }

        @Test
        @DisplayName("Each produced array is a valid involution")
        void allProducedAreValidInvolutions() {
            int n = 7;
            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
            int count = 0;
            do {
                int[] inv = engine.involution();
                assertTrue(isValidInvolution(inv),
                        "step " + count + " produced invalid involution: " + Arrays.toString(inv));
                count++;
            } while (engine.increment());
            assertEquals(alg.involutionCount(n).intValue(), count);
        }
    }

    // =========================================================
    // 6. Increment machine: structural consistency
    // =========================================================

    @Nested
    @DisplayName("Increment machine: structural consistency")
    class IncrementStructure {

        @Test
        @DisplayName("Digit array length is consistent with involution structure")
        void digitLengthConsistency() {
            int n = 6;
            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
            do {
                int[] inv    = engine.involution();
                int[] digits = engine.getDigits();
                assertEquals(countDecisions(inv, n), digits.length,
                        "involution=" + Arrays.toString(inv)
                                + " digits=" + Arrays.toString(digits));
            } while (engine.increment());
        }

        @Test
        @DisplayName("decisionCount matches expected number of decisions for each involution")
        void decisionCountMatchesInvolution() {
            int n = 5;
            var engine = new InvolutadicIncrementStateMachine(n, 0L, calculator);
            do {
                int[] inv = engine.involution();
                assertEquals(countDecisions(inv, n), engine.decisionCount(),
                        "decisionCount mismatch at involution " + Arrays.toString(inv));
            } while (engine.increment());
        }
    }

    // =========================================================
    // 7. Boundary and exception tests
    // =========================================================

    @Nested
    @DisplayName("Boundary conditions and exceptions")
    class Boundaries {

        @Test
        @DisplayName("Rank 0 produces the identity (all fixed points)")
        void rank0IsIdentity() {
            for (int n = 1; n <= 8; n++) {
                int[] inv = alg.unrank(0, n);
                int[] identity = new int[n];
                for (int i = 0; i < n; i++) identity[i] = i;
                assertArrayEquals(identity, inv, "n=" + n);
            }
        }

        @Test
        @DisplayName("Rank T(n)-1 produces the reverse involution for even n")
        void lastRankIsReverse() {
            for (int n : new int[]{2, 4, 6}) {
                BigInteger lastRank = alg.involutionCount(n).subtract(BigInteger.ONE);
                int[] inv = alg.unrank(lastRank, n);
                int[] expected = new int[n];
                for (int i = 0; i < n; i++) expected[i] = n - 1 - i;
                assertArrayEquals(expected, inv, "n=" + n + " last rank should be reverse");
            }
        }

        @Test
        @DisplayName("Rank out of range throws IllegalArgumentException")
        void rankOutOfRange() {
            assertThrows(IllegalArgumentException.class, () -> alg.toInvolutadic(-1L, 4));
            assertThrows(IllegalArgumentException.class, () -> alg.toInvolutadic(10L, 4));
        }

        @Test
        @DisplayName("Invalid involution throws IllegalArgumentException")
        void invalidInvolution() {
            assertThrows(IllegalArgumentException.class,
                    () -> alg.fromInvolution(new int[]{1, 2, 0}, 3));
        }

        @Test
        @DisplayName("n=1: single involution (the identity)")
        void n1() {
            assertEquals(BigInteger.ONE, alg.involutionCount(1));
            assertArrayEquals(new int[]{0}, alg.unrank(0, 1));
            assertEquals(BigInteger.ZERO, alg.rank(new int[]{0}, 1));
        }

        @Test
        @DisplayName("n=2: two involutions [0,1] and [1,0]")
        void n2() {
            assertEquals(BigInteger.TWO, alg.involutionCount(2));
            assertArrayEquals(new int[]{0, 1}, alg.unrank(0, 2));
            assertArrayEquals(new int[]{1, 0}, alg.unrank(1, 2));
        }
    }

    // =========================================================
    // 8. Involution structure properties
    // =========================================================

    @Nested
    @DisplayName("Involution structure properties")
    class StructureProperties {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {3, 4, 5, 6})
        void allUnrankedAreValidInvolutions(int n) {
            int total = alg.involutionCount(n).intValueExact();
            for (int rank = 0; rank < total; rank++) {
                int[] inv = alg.unrank(rank, n);
                assertTrue(isValidInvolution(inv), "rank=" + rank + " produced invalid involution");
            }
        }

        @Test
        @DisplayName("Fixed-point count matches zero-digit count in digit array")
        void fixedPointCountMatchesDigits() {
            int n = 6;
            int total = alg.involutionCount(n).intValueExact();
            for (int rank = 0; rank < total; rank++) {
                int[] inv    = alg.unrank(rank, n);
                int[] digits = alg.toInvolutadic(rank, n);
                int fps = 0;
                for (int i = 0; i < n; i++) if (inv[i] == i) fps++;
                int zeroDigits = 0;
                for (int d : digits) if (d == 0) zeroDigits++;
                assertEquals(fps, zeroDigits,
                        "rank=" + rank + " inv=" + Arrays.toString(inv)
                                + " digits=" + Arrays.toString(digits));
            }
        }
    }

    // =========================================================
    // Helper utilities
    // =========================================================

    private static boolean isValidInvolution(int[] pi) {
        int n = pi.length;
        for (int i = 0; i < n; i++) {
            if (pi[i] < 0 || pi[i] >= n) return false;
            if (pi[pi[i]] != i) return false;
        }
        return true;
    }

    private static boolean lexLessThan(int[] a, int[] b) {
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            if (a[i] < b[i]) return true;
            if (a[i] > b[i]) return false;
        }
        return a.length < b.length;
    }

    private static List<int[]> allInvolutionsLex(int n) {
        List<int[]> result = new ArrayList<>();
        generateInvolutions(new int[n], new boolean[n], 0, n, result);
        result.sort((a, b) -> {
            for (int i = 0; i < n; i++) {
                if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
            }
            return 0;
        });
        return result;
    }

    private static void generateInvolutions(
            int[] perm, boolean[] used, int pos, int n, List<int[]> result) {
        if (pos == n) { result.add(perm.clone()); return; }
        if (used[pos]) { generateInvolutions(perm, used, pos + 1, n, result); return; }

        // Fixed point
        perm[pos] = pos; used[pos] = true;
        generateInvolutions(perm, used, pos + 1, n, result);
        used[pos] = false;

        // 2-cycle with each j > pos not yet used
        for (int j = pos + 1; j < n; j++) {
            if (!used[j]) {
                perm[pos] = j; perm[j] = pos;
                used[pos] = true; used[j] = true;
                generateInvolutions(perm, used, pos + 1, n, result);
                used[pos] = false; used[j] = false;
            }
        }
    }

    private static int countDecisions(int[] pi, int n) {
        boolean[] placed = new boolean[n];
        int count = 0;
        for (int pos = 0; pos < n; pos++) {
            if (placed[pos]) continue;
            count++;
            placed[pos] = true;
            if (pi[pos] != pos) placed[pi[pos]] = true;
        }
        return count;
    }
}