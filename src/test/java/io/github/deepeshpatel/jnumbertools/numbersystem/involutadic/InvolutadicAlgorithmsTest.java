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
 * <p>Tests cover:
 * <ul>
 *   <li>Telephone-number counts</li>
 *   <li>Round-trip: rank ↔ digits (encode/decode)</li>
 *   <li>Round-trip: rank ↔ involution (unrank/rank)</li>
 *   <li>Round-trip: involution ↔ digits (fromInvolution/toInvolution)</li>
 *   <li>Exhaustive verification for small n (all T(n) involutions)</li>
 *   <li>Lexicographic order preservation</li>
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
    // Helper Utilities
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
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
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
        if (pos == n) {
            result.add(perm.clone());
            return;
        }
        if (used[pos]) {
            generateInvolutions(perm, used, pos + 1, n, result);
            return;
        }

        // Fixed point
        perm[pos] = pos;
        used[pos] = true;
        generateInvolutions(perm, used, pos + 1, n, result);
        used[pos] = false;

        // 2-cycle with each j > pos not yet used
        for (int j = pos + 1; j < n; j++) {
            if (!used[j]) {
                perm[pos] = j;
                perm[j] = pos;
                used[pos] = true;
                used[j] = true;
                generateInvolutions(perm, used, pos + 1, n, result);
                used[pos] = false;
                used[j] = false;
            }
        }
    }

    // =========================================================
    // 1. Telephone-number counts (using Calculator directly)
    // =========================================================

    @Nested
    @DisplayName("Telephone numbers T(n)")
    class TelephoneNumbers {

        @Test
        @DisplayName("T(0)=1, T(1)=1, T(2)=2, T(3)=4, T(4)=10, T(5)=26, T(6)=76")
        void smallValues() {
            long[] expected = {1, 1, 2, 4, 10, 26, 76, 232, 764, 2620};
            for (int n = 0; n < expected.length; n++) {
                assertEquals(BigInteger.valueOf(expected[n]), calculator.telephoneNumber(n),
                        "T(" + n + ")");
            }
        }

        @Test
        @DisplayName("Recurrence T(n) = T(n-1) + (n-1)*T(n-2)")
        void recurrenceSatisfied() {
            for (int n = 2; n <= 12; n++) {
                BigInteger expected = calculator.telephoneNumber(n - 1)
                        .add(BigInteger.valueOf(n - 1).multiply(calculator.telephoneNumber(n - 2)));
                assertEquals(expected, calculator.telephoneNumber(n),
                        "recurrence at n=" + n);
            }
        }
    }

    // =========================================================
    // 2. Round-trip: rank ↔ digits (encode/decode)
    // =========================================================

    @Nested
    @DisplayName("Round-trip: rank ↔ digits")
    class DigitsRoundTrip {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        void encodeDecodeRoundTrip(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] digits = alg.encode(rank, n);
                BigInteger recovered = alg.decode(digits);
                assertEquals(rank, recovered,
                        "n=" + n + " rank=" + rank + " digits=" + Arrays.toString(digits));
            }
        }

        @Test
        @DisplayName("Large rank round-trip for n=15")
        void largeN() {
            int n = 15;
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank : List.of(
                    BigInteger.ZERO,
                    total.subtract(BigInteger.ONE),
                    total.divide(BigInteger.TWO))) {
                int[] digits = alg.encode(rank, n);
                BigInteger recovered = alg.decode(digits);
                assertEquals(rank, recovered, "n=" + n + " rank=" + rank);
            }
        }
    }

    // =========================================================
    // 3. Round-trip: rank ↔ involution (unrank/rank)
    // =========================================================

    @Nested
    @DisplayName("Round-trip: rank ↔ involution")
    class InvolutionRoundTrip {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        void unrankRankRoundTrip(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] inv = alg.unrank(rank, n);
                assertTrue(isValidInvolution(inv), "n=" + n + " rank=" + rank + " invalid involution");
                BigInteger recovered = alg.rank(inv);
                assertEquals(rank, recovered,
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
    // 4. Round-trip: involution ↔ digits (fromInvolution/toInvolution)
    // =========================================================

    @Nested
    @DisplayName("Round-trip: involution ↔ digits")
    class InvolutionDigitsRoundTrip {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        void fromInvolutionToInvolutionRoundTrip(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] involution = alg.unrank(rank, n);
                int[] digits = alg.fromInvolution(involution);
                int[] recoveredInvolution = alg.toInvolution(digits);

                assertArrayEquals(involution, recoveredInvolution,
                        "n=" + n + " rank=" + rank + " involution mismatch");
            }
        }

        @Test
        @DisplayName("fromInvolution matches encode for same rank")
        void fromInvolutionVsEncode() {
            int n = 6;
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] involution = alg.unrank(rank, n);
                int[] digitsFromInv = alg.fromInvolution(involution);
                int[] digitsFromRank = alg.encode(rank, n);

                assertArrayEquals(digitsFromRank, digitsFromInv,
                        "n=" + n + " rank=" + rank + " digits mismatch");
            }
        }
    }

    // =========================================================
    // 5. Lexicographic order preservation
    // =========================================================

    @Nested
    @DisplayName("Lexicographic order")
    class LexOrder {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {3, 4, 5, 6, 7})
        void allInvolutionsAreInLexOrder(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            int[] prev = alg.unrank(0, n);
            for (BigInteger rank = BigInteger.ONE;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] curr = alg.unrank(rank, n);
                assertTrue(lexLessThan(prev, curr),
                        "n=" + n + " rank " + (rank.subtract(BigInteger.ONE)) + " not lex-before rank " + rank
                                + ": " + Arrays.toString(prev) + " vs " + Arrays.toString(curr));
                prev = curr;
            }
        }
    }

    // =========================================================
    // 6. Known Involutadic digit arrays (fixed-width format)
    // =========================================================

    @Nested
    @DisplayName("Known digit arrays (fixed-width format)")
    class KnownDigits {

        @Test
        @DisplayName("Digit arrays for n=4")
        void digitsN4() {
            int[][] expectedDigits = {
                    {0, 0, 0, 0},  // rank 0
                    {0, 0, 1, -1}, // rank 1
                    {0, 1, -1, 0}, // rank 2
                    {0, 2, 0, -1}, // rank 3
                    {1, -1, 0, 0}, // rank 4
                    {1, -1, 1, -1},// rank 5
                    {2, 0, -1, 0}, // rank 6
                    {2, 1, -1, -1},// rank 7
                    {3, 0, 0, -1}, // rank 8
                    {3, 1, -1, -1},// rank 9
            };
            for (int rank = 0; rank < expectedDigits.length; rank++) {
                assertArrayEquals(expectedDigits[rank], alg.encode(rank, 4),
                        "n=4 rank=" + rank);
            }
        }

        @Test
        @DisplayName("Digit arrays length is always n (fixed-width)")
        void digitLengthsN4() {
            for (int rank = 0; rank < 10; rank++) {
                int[] digits = alg.encode(rank, 4);
                assertEquals(4, digits.length,
                        "n=4 rank=" + rank + " length mismatch, expected 4 but got " + digits.length);
            }
        }
    }

    // =========================================================
    // 10. Boundary and exception tests
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
                BigInteger lastRank = calculator.telephoneNumber(n).subtract(BigInteger.ONE);
                int[] inv = alg.unrank(lastRank, n);
                int[] expected = new int[n];
                for (int i = 0; i < n; i++) expected[i] = n - 1 - i;
                assertArrayEquals(expected, inv, "n=" + n + " last rank should be reverse");
            }
        }

        @Test
        @DisplayName("Rank out of range throws IllegalArgumentException")
        void rankOutOfRange() {
            assertThrows(IllegalArgumentException.class, () -> alg.unrank(-1L, 4));
            assertThrows(IllegalArgumentException.class, () -> alg.unrank(10L, 4));
            assertThrows(IllegalArgumentException.class, () -> alg.encode(-1L, 4));
            assertThrows(IllegalArgumentException.class, () -> alg.encode(10L, 4));
        }

        @Test
        @DisplayName("Invalid involution throws IllegalArgumentException")
        void invalidInvolution() {
            assertThrows(IllegalArgumentException.class,
                    () -> alg.fromInvolution(new int[]{1, 2, 0}));
            assertThrows(IllegalArgumentException.class,
                    () -> alg.rank(new int[]{1, 2, 0}));
        }

        @Test
        @DisplayName("n=1: single involution (the identity)")
        void n1() {
            assertEquals(BigInteger.ONE, calculator.telephoneNumber(1));
            assertArrayEquals(new int[]{0}, alg.unrank(0, 1));
            assertEquals(BigInteger.ZERO, alg.rank(new int[]{0}));
        }

        @Test
        @DisplayName("n=2: two involutions [0,1] and [1,0]")
        void n2() {
            assertEquals(BigInteger.TWO, calculator.telephoneNumber(2));
            assertArrayEquals(new int[]{0, 1}, alg.unrank(0, 2));
            assertArrayEquals(new int[]{1, 0}, alg.unrank(1, 2));
        }

        @Test
        @DisplayName("Invalid digits array throws IllegalArgumentException")
        void invalidDigitsArray() {
            // -1 but position not consumed
            int[] invalidDigits1 = {-1, 0, 0, 0};
            assertThrows(IllegalArgumentException.class,
                    () -> alg.decode(invalidDigits1),
                    "digit -1 at position 0 but position was not consumed");

            // digit out of range (d > available positions)
            int[] invalidDigits2 = {2, -1, 0};  // n=3, at pos0 only 1 position to the right
            assertThrows(IllegalArgumentException.class,
                    () -> alg.decode(invalidDigits2),
                    "digit 2 exceeds available positions");

            // invalid digit value
            int[] invalidDigits3 = {-2, 0, 0};
            assertThrows(IllegalArgumentException.class,
                    () -> alg.decode(invalidDigits3),
                    "Invalid digit: -2");
        }
    }

    // =========================================================
    // 11. Involution structure properties
    // =========================================================

    @Nested
    @DisplayName("Involution structure properties")
    class StructureProperties {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {3, 4, 5, 6})
        void allUnrankedAreValidInvolutions(int n) {
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] inv = alg.unrank(rank, n);
                assertTrue(isValidInvolution(inv), "rank=" + rank + " produced invalid involution");
            }
        }

        @Test
        @DisplayName("Fixed-point count matches zero-digit count in digit array")
        void fixedPointCountMatchesDigits() {
            int n = 6;
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] inv = alg.unrank(rank, n);
                int[] digits = alg.encode(rank, n);

                int fixedPoints = 0;
                for (int i = 0; i < n; i++) if (inv[i] == i) fixedPoints++;

                int zeroDigits = 0;
                for (int d : digits) if (d == 0) zeroDigits++;

                assertEquals(fixedPoints, zeroDigits,
                        "rank=" + rank + " inv=" + Arrays.toString(inv)
                                + " digits=" + Arrays.toString(digits));
            }
        }

        @Test
        @DisplayName("Number of 2-cycles matches count of positive digits")
        void cycleCountMatchesPositiveDigits() {
            int n = 6;
            BigInteger total = calculator.telephoneNumber(n);
            for (BigInteger rank = BigInteger.ZERO;
                 rank.compareTo(total) < 0;
                 rank = rank.add(BigInteger.ONE)) {

                int[] digits = alg.encode(rank, n);

                int positiveDigits = 0;
                for (int d : digits) if (d > 0) positiveDigits++;

                int cycleCount = 0;
                boolean[] visited = new boolean[n];
                int[] inv = alg.unrank(rank, n);
                for (int i = 0; i < n; i++) {
                    if (!visited[i] && inv[i] != i) {
                        cycleCount++;
                        visited[i] = true;
                        visited[inv[i]] = true;
                    }
                }

                assertEquals(cycleCount, positiveDigits,
                        "rank=" + rank + " cycle count mismatch");
            }
        }
    }
}