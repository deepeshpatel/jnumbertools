/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigInteger;
import java.util.*;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class DerangadicAlgorithmsTest {

    private final DerangadicAlgorithms DERANGADIC = new DerangadicAlgorithms(new Calculator());
    private final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Test
    @DisplayName("Round-trip conversion should work for n=4 (D_4=9)")
    void testRoundTripN4() {
        testRoundTrip(4, 9);
    }

    @Test
    @DisplayName("Round-trip conversion should work for n=5 (D_5=44)")
    void testRoundTripN5() {
        testRoundTrip(5, 44);
    }

    private void testRoundTrip(int n, int expectedCount) {
        BigInteger total = DERANGADIC.derangementCount(n);
        assertEquals(expectedCount, total.intValue(),
                String.format("D_%d should be %d", n, expectedCount));

        for (long m = 0; m < total.longValue(); m++) {
            int[] digits = DERANGADIC.toDerangadic(m, n);
            assertNotNull(digits);

            BigInteger mBack = DERANGADIC.fromDerangadic(digits, n);
            assertEquals(BigInteger.valueOf(m), mBack,
                    String.format("Rank mismatch at m=%d", m));

            int[] derangement = DERANGADIC.toDerangement(digits, n);
            assertTrue(isValidDerangement(derangement),
                    String.format("Invalid derangement at m=%d", m));

            int[] digitsBack = DERANGADIC.fromDerangement(derangement, n);

            // Compare ignoring trailing zeros
            assertTrue(arraysEqualIgnoringTrailingZeros(digits, digitsBack),
                    String.format("Digits mismatch at m=%d\nExpected: %s\nActual:   %s",
                            m, Arrays.toString(digits), Arrays.toString(digitsBack)));
        }
    }

    /**
     * Compares two arrays ignoring trailing zeros.
     * For example, [2,3,0] equals [2,3] equals [2,3,0,0]
     */
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

    // ==================== Large N Edge Cases ====================

    @Test
    @DisplayName("Large n edge cases should work")
    void testLargeN() {
        int[] testSizes = {10, 20, 30, 50};

        for (int n : testSizes) {
            BigInteger count = DERANGADIC.derangementCount(n);

            // Test rank 0
            int[] zeroRank = DERANGADIC.unrank(0,n);
            assertTrue(isValidDerangement(zeroRank),
                    String.format("Rank 0 derangement invalid for n=%d", n));

            // Test rank 100 (if within range)
            if (BigInteger.valueOf(100).compareTo(count) < 0) {
                int[] rank100 = DERANGADIC.unrank(100, n);
                assertTrue(isValidDerangement(rank100),
                        String.format("Rank 100 derangement invalid for n=%d", n));
            }

            // Test middle rank
            BigInteger midRank = count.divide(BigInteger.valueOf(2));
            int[] midDerangement = DERANGADIC.unrank(midRank, n);
            assertTrue(isValidDerangement(midDerangement),
                    String.format("Middle rank derangement invalid for n=%d", n));

            // Test last rank
            BigInteger lastRank = count.subtract(BigInteger.ONE);
            int[] lastDerangement = DERANGADIC.unrank(lastRank, n);
            assertTrue(isValidDerangement(lastDerangement),
                    String.format("Last rank derangement invalid for n=%d", n));
        }
    }


    // ==================== Brute Force Validation ====================

    @Test
    @DisplayName("Brute force validation against JNumberTools should pass for n=3..7")
    void testBruteForceValidation() {
        int[] testSizes = {3, 4, 5, 6, 7};

        for (int n : testSizes) {
            List<int[]> bruteForce = generateDerangementsByBruteForce(n);
            int expectedCount = bruteForce.size();

            BigInteger actualCount = DERANGADIC.derangementCount(n);
            assertEquals(expectedCount, actualCount.intValue(),
                    String.format("Count mismatch for n=%d", n));

            int comparisons = Math.min(100, expectedCount);
            for (int rank = 0; rank < comparisons; rank++) {
                int[] expected = bruteForce.get(rank);
                int[] actual = DERANGADIC.unrank(BigInteger.valueOf(rank), n);
                assertArrayEquals(expected, actual,
                        String.format("Derangement mismatch at n=%d, rank=%d", n, rank));
            }
        }
    }

    // ==================== Random Sampling Validation ====================

    @Test
    @DisplayName("Random sampling validation should pass for n=10..50")
    void testRandomSampling() {
        int[] testSizes = {10, 12, 15, 20, 25, 30, 35, 40, 45, 50};

        for (int n : testSizes) {
            BigInteger total = DERANGADIC.derangementCount(n);

            // Test first 10 ranks
            for (long rank = 0; rank < 10 && BigInteger.valueOf(rank).compareTo(total) < 0; rank++) {
                int[] derangement = DERANGADIC.unrank(BigInteger.valueOf(rank), n);
                assertTrue(isValidDerangement(derangement),
                        String.format("Invalid derangement at n=%d, rank=%d", n, rank));
            }

            // Test random BigInteger ranks
            for (int i = 0; i < 50; i++) {
                BigInteger rank = generateRandomBigIntegerRank(total);
                int[] derangement = DERANGADIC.unrank(rank, n);
                assertTrue(isValidDerangement(derangement),
                        String.format("Invalid derangement at n=%d, random rank", n));

                // Verify round-trip
                int[] digits = DERANGADIC.toDerangadic(rank, n);
                BigInteger rankBack = DERANGADIC.fromDerangadic(digits, n);
                assertEquals(rank, rankBack,
                        String.format("Rank mismatch at n=%d", n));
            }

            // Test last rank
            BigInteger lastRank = total.subtract(BigInteger.ONE);
            int[] lastDerangement = DERANGADIC.unrank(lastRank, n);
            assertTrue(isValidDerangement(lastDerangement),
                    String.format("Invalid derangement at n=%d, last rank", n));

        }
    }

    // ==================== Lexicographic Order Validation ====================

    @Test
    @DisplayName("Derangements should be in lexicographic order")
    void testLexicographicOrder() {
        int n = 9;
        BigInteger total = DERANGADIC.derangementCount(n);
        long step = 1;

        int[] previous = null;
        long previousRank = -1;

        for (long rank = 0; rank < total.longValue(); rank += step) {
            int[] current = DERANGADIC.unrank(BigInteger.valueOf(rank), n);

            assertTrue(isValidDerangement(current),
                    String.format("Invalid derangement at rank %d", rank));

            if (previous != null) {
                assertTrue(isLexicographicallyLess(previous, current),
                        String.format("Order violation: rank %d >= rank %d", previousRank, rank));
            }

            previous = current;
            previousRank = rank;
        }
    }

    @Test
    @DisplayName("Derangements should be in lexicographic order for very large n")
    void testLexicographicOrderForVeryLarge() {
        int n = 5000;

        BigInteger step = BigInteger.TEN.pow(50);

        int[] previous = null;
        BigInteger rank = BigInteger.TEN.pow(100);

        int iterations = 200;
        for (int i = 0; i < iterations; i++) {

            int[] current = DERANGADIC.unrank(rank, n);

            assertTrue(isValidDerangement(current),
                    String.format("Invalid derangement at rank %s", rank));

            if (previous != null) {
                assertTrue(isLexicographicallyLess(previous, current),
                        String.format("Order violation: previous rank %s, current rank %s",
                                rank.subtract(step), rank));
            }

            previous = current;
            rank = rank.add(step);  // Fixed: assign result back to rank
        }
    }

    @Test
    @DisplayName("Contiguous block of derangements should be in correct order")
    void testContiguousBlockOrder() {
        int n = 12;
        int startRank = 1000;
        int blockSize = 100;

        int[] previous = null;

        for (int i = 0; i < blockSize; i++) {
            long rank = startRank + i;
            int[] current = DERANGADIC.unrank(BigInteger.valueOf(rank), n);

            assertTrue(isValidDerangement(current),
                    String.format("Invalid at rank %d", rank));

            if (previous != null) {
                assertTrue(isLexicographicallyLess(previous, current),
                        String.format("Order violation at rank %d", rank));
            }

            previous = current;
        }
    }

    // ==================== Very Large N Validation ====================

    @Test
    @DisplayName("Very large n validation (n=100)")
    void testVeryLargeN() {
        int n = 100;
        BigInteger total = DERANGADIC.derangementCount(n);

        // Test first 5 ranks
        for (long rank = 0; rank < 5; rank++) {
            int[] derangement = DERANGADIC.unrank(BigInteger.valueOf(rank), n);
            assertTrue(isValidDerangement(derangement));
        }

        // Test random ranks
        for (int i = 0; i < 20; i++) {
            BigInteger rank = generateRandomBigIntegerRank(total);
            int[] derangement = DERANGADIC.unrank(rank, n);
            assertTrue(isValidDerangement(derangement));

            // Verify round-trip
            int[] digits = DERANGADIC.toDerangadic(rank, n);
            BigInteger rankBack = DERANGADIC.fromDerangadic(digits, n);
            assertEquals(rank, rankBack);
        }
    }


    @Test
    @DisplayName("Array and Fenwick implementations should produce identical derangements")
    void testArrayVsFenwickConsistency() {

        // Test various n and rank combinations
        int[] testCases = {
                10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000
        };

        for (int n : testCases) {
            //BigInteger total = DERANGADIC.derangementCount(n);

            // Test first 100 ranks (or all if less than 100)
            int maxRank = 100;//Math.min(100, total.intValue());

            for (int rank = 0; rank < maxRank; rank++) {
                // Get digits (same for both implementations)
                int[] digits = DERANGADIC.toDerangadic(rank, n);

                // Force array implementation
                int[] resultArray = DERANGADIC.toDerangementArray(digits, n);

                // Force Fenwick implementation
                int[] resultFenwick = DERANGADIC.toDerangementFenwick(digits, n);

                assertArrayEquals(resultArray, resultFenwick,
                        String.format("Mismatch at n=%d, rank=%d%nArray: %s%nFenwick: %s",
                                n, rank, Arrays.toString(resultArray), Arrays.toString(resultFenwick)));
            }
        }
    }

    @Test
    @DisplayName("Random ranks should produce identical results for array and Fenwick")
    void testRandomRanksConsistency() {
        Random random = new Random(42);  // Fixed seed for reproducibility

        int[] nValues = {1000, 2000, 5000, 10000, 20000, 50000};
        int testsPerN = 10;

        for (int n : nValues) {

            for (int i = 0; i < testsPerN; i++) {
                // Generate random rank
                BigInteger rank;

                long randomRank = random.nextLong(50000, 1000000);
                rank = BigInteger.valueOf(randomRank);// new BigInteger(total.bitLength(), random);

                int[] digits = DERANGADIC.toDerangadic(rank, n);
                int[] resultArray = DERANGADIC.toDerangementArray(digits, n);
                int[] resultFenwick = DERANGADIC.toDerangementFenwick(digits, n);

                assertArrayEquals(resultArray, resultFenwick,
                        String.format("Mismatch at n=%d, rank=%s", n, rank));

            }
        }
    }

    @Test
    @DisplayName("Edge cases: rank=0 and max rank")
    void testEdgeCasesConsistency() {
        int[] nValues = {100, 200, 5000, 10000};

        int[] startIndices= {0, 5000, 20000, 50000, 100000};
        int batchSize = 1000;

        for (int n : nValues) {

            for(var startIndex: startIndices) {
                for (int i = startIndex; i <= batchSize + startIndex ; i++) {
                    int[] digits = DERANGADIC.toDerangadic(i, n);
                    int[] array = DERANGADIC.toDerangementArray(digits, n);
                    int[] fenwick = DERANGADIC.toDerangementFenwick(digits, n);
                    assertArrayEquals(array, fenwick, "Mismatch at n=" + n + ", rank=0");
                }
            }
        }
    }

    // ==================== Invalid Input Tests ====================

    @Test
    @DisplayName("Invalid inputs should throw appropriate exceptions")
    void testInvalidInputs() {
        // Test negative rank
        assertThrows(IllegalArgumentException.class, () ->
                DERANGADIC.toDerangadic(BigInteger.valueOf(-1), 4));

        // Test rank >= D_n
        BigInteger total = DERANGADIC.derangementCount(4);
        assertThrows(IllegalArgumentException.class, () ->
                DERANGADIC.toDerangadic(total, 4));

        // Test n=1 (no derangements - returns 0, not exception)
        BigInteger countForN1 = DERANGADIC.derangementCount(1);
        assertEquals(BigInteger.ZERO, countForN1,
                "D_1 should be 0 (no derangements)");

        // Test invalid derangement (with fixed point)
        int[] invalidDerangement = {0, 2, 1};
        assertThrows(IllegalArgumentException.class, () ->
                DERANGADIC.fromDerangement(invalidDerangement, 3));
    }

    // ==================== Random Generation Test ====================

    @RepeatedTest(3)
    @DisplayName("Random derangement generation should produce valid results")
    void testRandomDerangements() {
        int[] ns = {5, 8, 10, 12, 15, 20, 25, 30};
        int n = ns[new Random().nextInt(ns.length)];
        BigInteger total = DERANGADIC.derangementCount(n);

        for (int i = 0; i < 10; i++) {
            BigInteger rank = generateRandomBigIntegerRank(total);

            int[] digits = DERANGADIC.toDerangadic(rank, n);
            int[] derangement = DERANGADIC.toDerangement(digits, n);
            assertTrue(isValidDerangement(derangement));

            BigInteger rankBack = DERANGADIC.fromDerangadic(digits, n);
            assertEquals(rank, rankBack,
                    String.format("Rank mismatch for n=%d", n));

            // Verify no fixed points
            for (int pos = 0; pos < n; pos++) {
                assertNotEquals(pos, derangement[pos],
                        String.format("Fixed point at position %d for n=%d", pos, n));
            }
        }
    }



    // ==================== Utility Methods ====================


    private BigInteger generateRandomBigIntegerRank(BigInteger total) {
        BigInteger rank;
        do {
            rank = new BigInteger(total.bitLength(), SECURE_RANDOM);
        } while (rank.compareTo(total) >= 0);
        return rank;
    }

    static boolean isValidDerangement(int[] arr) {
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

    private static boolean isLexicographicallyLess(int[] a, int[] b) {
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            if (a[i] != b[i]) {
                return a[i] < b[i];
            }
        }
        return a.length < b.length;
    }

    private static List<int[]> generateDerangementsByBruteForce(int n) {
        List<int[]> derangements = new ArrayList<>();

        JNumberTools.permutations()
                .unique(n)
                .lexOrder()
                .stream()
                .forEach(perm -> {
                    int[] arr = perm.stream().mapToInt(Integer::intValue).toArray();
                    boolean isDerangement = true;
                    for (int i = 0; i < n; i++) {
                        if (arr[i] == i) {
                            isDerangement = false;
                            break;
                        }
                    }
                    if (isDerangement) {
                        derangements.add(arr);
                    }
                });

        return derangements;
    }
}
