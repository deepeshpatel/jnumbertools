package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnrankOf Factory")
public class UnrankOfTest {

    @Nested
    @DisplayName("Unique permutations")
    class UniquePermutationTests {

        @Test
        @DisplayName("uniquePermutation(rank=23, n=4) = [3,2,1,0]")
        void correctPermutationForGivenRank() {
            int[] expected = {3, 2, 1, 0};
            int[] permutation = unrankOf.permutation().unique(BigInteger.valueOf(23), 4);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutation(rank=0, n=4) = [0,1,2,3]")
        void firstPermutation() {
            int[] expected = {0, 1, 2, 3};
            int[] permutation = unrankOf.permutation().unique(BigInteger.valueOf(0), 4);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutationMinimumSize(rank=5) = size 3 = [2,1,0]")
        void minimumSizeForRank() {
            BigInteger rank = BigInteger.valueOf(5);
            int[] permutation = unrankOf.permutation().uniqueMinimumSize(rank);
            assertEquals(3, permutation.length);
            int[] expected = {2, 1, 0};
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutationMinimumSize boundary cases: rank=0, rank=6, rank=1000")
        void minimumSizeBoundary() {
            // rank 0 has the empty permutation as its minimum-size representation
            int[] zeroPerm = unrankOf.permutation().uniqueMinimumSize(BigInteger.ZERO);
            assertArrayEquals(new int[0], zeroPerm);

            // rank 6 requires size 4 (3! = 6, so rank 6 needs 4!)
            BigInteger rank6 = BigInteger.valueOf(6);
            int[] perm6 = unrankOf.permutation().uniqueMinimumSize(rank6);
            assertEquals(4, perm6.length);

            // rank 1000 requires size 7 (6!=720, 7!=5040, so rank 1000 needs 7!)
            BigInteger rank1000 = BigInteger.valueOf(1000);
            int[] perm1000 = unrankOf.permutation().uniqueMinimumSize(rank1000);
            assertEquals(7, perm1000.length);
        }
    }

    @Nested
    @DisplayName("K-Permutations")
    class KPermutationTests {

        @Test
        @DisplayName("kPermutation(rank=5, n=10, k=3) = [0,1,7]")
        void correctKPermutation() {
            int[] expected = {0, 1, 7};
            int[] permutation = unrankOf.permutation().k(BigInteger.valueOf(5), 10, 3);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("kPermutation out-of-range throws ArithmeticException")
        void outOfRangeThrows() {
            int n = 4;
            int r = 2;
            long totalPermutations = calculator.nPr(n, r).longValue();

            Exception exception = assertThrows(ArithmeticException.class,
                    () -> JNumberTools.unrankOf().permutation().k(BigInteger.valueOf(totalPermutations), n, r));

            String output = String.format("≥ Permutation(%d,%d)", n, r);
            assertTrue(exception.getMessage().contains(output));
        }
    }

    @Nested
    @DisplayName("Unique combinations")
    class UniqueCombinationTests {

        @Test
        @DisplayName("uniqueCombination(rank=9, n=5, k=3) = [2,3,4], the last of 5C3 = 10 combinations")
        void correctCombination() {
            // rank 35 used to be accepted and silently returned the last combination; it is out of range
            // (see RankUnrankValidationRulesTest)
            assertArrayEquals(new int[]{0, 1, 2}, unrankOf.combination().unique(BigInteger.ZERO, 5, 3));
            assertArrayEquals(new int[]{0, 1, 3}, unrankOf.combination().unique(BigInteger.ONE, 5, 3));
            assertArrayEquals(new int[]{2, 3, 4}, unrankOf.combination().unique(BigInteger.valueOf(9), 5, 3));
        }

        @Test
        @DisplayName("large combinations (r >= 256) round-trip through rankOf, including first, last and middle ranks")
        void largeCombinationsRoundTrip() {
            java.util.Random random = new java.util.Random(42);
            int[][] shapes = {{256, 256}, {257, 256}, {300, 299}, {512, 256}, {1000, 500}, {1000, 999}, {400, 300}, {3000, 300}};
            for (int[] shape : shapes) {
                int n = shape[0];
                int r = shape[1];
                BigInteger total = calculator.nCr(n, r);
                java.util.List<BigInteger> ranks = new java.util.ArrayList<>(java.util.List.of(
                        BigInteger.ZERO, total.subtract(BigInteger.ONE), total.shiftRight(1)));
                for (int i = 0; i < 8; i++) {
                    ranks.add(new BigInteger(total.bitLength() + 8, random).mod(total));
                }
                for (BigInteger rank : ranks) {
                    int[] combination = unrankOf.combination().unique(rank, n, r);
                    assertEquals(r, combination.length, "n=" + n + " r=" + r);
                    for (int i = 0; i < r; i++) {
                        assertTrue(combination[i] >= 0 && combination[i] < n, "in range, n=" + n + " r=" + r);
                        if (i > 0) {
                            assertTrue(combination[i] > combination[i - 1], "strictly ascending, n=" + n + " r=" + r);
                        }
                    }
                    assertEquals(rank, rankOf.combination().unique(n, combination), "n=" + n + " r=" + r + " rank=" + rank);
                }
            }
        }

        @Test
        @DisplayName("large combinations: rank 0 is {0..r-1} and the last rank is {n-r..n-1}")
        void largeCombinationsAtTheEnds() {
            int n = 700;
            int r = 300;
            BigInteger total = calculator.nCr(n, r);
            int[] first = unrankOf.combination().unique(BigInteger.ZERO, n, r);
            int[] last = unrankOf.combination().unique(total.subtract(BigInteger.ONE), n, r);
            for (int i = 0; i < r; i++) {
                assertEquals(i, first[i]);
                assertEquals(n - r + i, last[i]);
            }
        }
    }

    @Nested
    @DisplayName("Derangements")
    class DerangementTests {

        @Test
        @DisplayName("derangement(rank=0, n=4) = [1,0,3,2]")
        void correctDerangement() {
            int[] expected = {1, 0, 3, 2};
            int[] actual = unrankOf.permutation().derangement(BigInteger.ZERO, 4);
            assertArrayEquals(expected, actual);
            // Verify long overload agrees
            assertArrayEquals(expected, unrankOf.permutation().derangement(0L, 4));
        }

        @Test
        @DisplayName("all derangements enumerated: valid permutations with no fixed points")
        void allDerangementsValid() {
            for (int n = 2; n <= 6; n++) {
                int total = calculator.subFactorial(n).intValue();
                var seen = new java.util.HashSet<String>();
                for (int rank = 0; rank < total; rank++) {
                    int[] d = unrankOf.permutation().derangement(rank, n);
                    assertEquals(n, d.length, "Derangement length mismatch for n=" + n + " rank=" + rank);
                    boolean[] used = new boolean[n];
                    for (int i = 0; i < n; i++) {
                        assertNotEquals(i, d[i], "Fixed point at i=" + i + " n=" + n + " rank=" + rank);
                        assertTrue(d[i] >= 0 && d[i] < n, "Value out of range for n=" + n);
                        assertFalse(used[d[i]], "Duplicate value at i=" + i);
                        used[d[i]] = true;
                    }
                    assertTrue(seen.add(java.util.Arrays.toString(d)),
                            "Duplicate derangement at n=" + n + " rank=" + rank);
                }
                assertEquals(total, seen.size(), "Total derangements mismatch for n=" + n);
            }
        }

        @Test
        @DisplayName("derangement rank out of range throws IllegalArgumentException")
        void outOfRangeThrows() {
            // D_4 = 9; rank 9 is out of range
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.permutation().derangement(BigInteger.valueOf(9), 4));
            // Negative ranks must also throw
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.permutation().derangement(BigInteger.valueOf(-1), 4));
        }

        @Test
        @DisplayName("derangement of n=1 throws IllegalArgumentException (D_1=0)")
        void n1Throws() {
            // D_1 = 0; no valid rank exists
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.permutation().derangement(BigInteger.ZERO, 1));
        }
    }

    @Nested
    @DisplayName("Integration with JNumberTools facade")
    class IntegrationTests {

        @Test
        @DisplayName("unrankOf() via facade returns same results as static instance")
        void viaJNumberToolsFacade() {
            int[] viaFacade = JNumberTools.unrankOf().permutation().derangement(BigInteger.ZERO, 4);
            int[] viaInstance = unrankOf.permutation().derangement(BigInteger.ZERO, 4);
            assertArrayEquals(viaInstance, viaFacade);
        }
    }

    @Nested
    @DisplayName("Invalid ranks (ValidationRules.md: rank / unrank helpers)")
    class InvalidRanks {
        // ValidationRules.md specifies the CONDITION (an error is signaled), not the exception type.


        @Test
        @DisplayName("uniquePermutation rejects a negative rank")
        void uniquePermutationRejectsNegativeRank() {
            assertThrows(RuntimeException.class, () -> unrankOf.permutation().unique(BigInteger.valueOf(-1), 3));
        }

        @Test
        @DisplayName("uniquePermutation rejects rank >= size!")
        void uniquePermutationRejectsRankBeyondSizeFactorial() {
            assertThrows(RuntimeException.class, () -> unrankOf.permutation().unique(BigInteger.valueOf(6), 3));
            assertThrows(RuntimeException.class, () -> unrankOf.permutation().unique(BigInteger.ONE, 0));
            assertDoesNotThrow(() -> unrankOf.permutation().unique(BigInteger.valueOf(5), 3));
        }

        @Test
        @DisplayName("uniquePermutation rejects a null rank")
        void uniquePermutationRejectsNullRank() {
            assertThrows(NullPointerException.class, () -> unrankOf.permutation().unique(null, 3));
        }

        @Test
        @DisplayName("uniquePermutation rejects a negative size")
        void uniquePermutationRejectsNegativeSize() {
            assertThrows(RuntimeException.class, () -> unrankOf.permutation().unique(BigInteger.ZERO, -1));
        }

        @Test
        @DisplayName("uniqueMinimumSize rejects null and negative ranks")
        void uniqueMinimumSizeRejectsInvalidRanks() {
            assertThrows(NullPointerException.class, () -> unrankOf.permutation().uniqueMinimumSize(null));
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.permutation().uniqueMinimumSize(BigInteger.valueOf(-1)));
        }

        @Test
        @DisplayName("kPermutation rejects a negative rank")
        void kPermutationRejectsNegativeRank() {
            assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(BigInteger.valueOf(-1), 5, 3));
        }
    }

    @Nested
    @DisplayName("Repetitive unranking")
    class RepetitiveUnrankingTests {

        @Test
        @DisplayName("repetitive permutation unranking agrees with builder byRanks")
        void repetitivePermutationUnrankMatchesBuilder() {
            for (int n = 0; n <= 4; n++) {
                for (int width = 0; width <= 4; width++) {
                    BigInteger total = calculator.power(n, width);
                    for (BigInteger rank = BigInteger.ZERO; rank.compareTo(total) < 0;
                         rank = rank.add(BigInteger.ONE)) {
                        List<Integer> expected = permutation.repetitive(width, n)
                                .byRanks(List.of(rank)).iterator().next();
                        assertArrayEquals(expected.stream().mapToInt(Integer::intValue).toArray(),
                                unrankOf.permutation().repetitive(rank, n, width));
                    }
                }
            }
            assertThrows(RuntimeException.class,
                    () -> unrankOf.permutation().repetitive(BigInteger.ZERO, 0, 1));
            assertThrows(RuntimeException.class,
                    () -> unrankOf.permutation().repetitive(BigInteger.valueOf(-1), 2, 2));
        }

        @Test
        @DisplayName("repetitive combination unranking agrees with builder byRanks")
        void repetitiveCombinationUnrankMatchesBuilder() {
            for (int n = 0; n <= 5; n++) {
                for (int r = 0; r <= 5; r++) {
                    BigInteger total = calculator.nCrRepetitive(n, r);
                    for (BigInteger rank = BigInteger.ZERO; rank.compareTo(total) < 0;
                         rank = rank.add(BigInteger.ONE)) {
                        List<Integer> expected = combination.repetitive(n, r)
                                .byRanks(List.of(rank)).iterator().next();
                        assertArrayEquals(expected.stream().mapToInt(Integer::intValue).toArray(),
                                unrankOf.combination().repetitive(rank, n, r));
                    }
                }
            }
            assertThrows(RuntimeException.class,
                    () -> unrankOf.combination().repetitive(BigInteger.valueOf(6), 3, 2));
        }
    }

    @Nested
    @DisplayName("Multiset unranking")
    class MultisetUnrankingTests {

        @Test
        @DisplayName("multiset permutation unranking agrees with builder byRanks")
        void multisetPermutationUnrankMatchesBuilder() {
            int[][] frequencyCases = {{}, {1}, {2}, {2, 1}, {1, 2, 1}, {2, 2, 1}, {0, 2, 1}};
            for (int[] frequencies : frequencyCases) {
                LinkedHashMap<Integer, Integer> options = asFrequencyMap(frequencies);
                List<List<Integer>> all = permutation.multiset(options).lexOrder().stream().toList();
                for (int i = 0; i < all.size(); i++) {
                    BigInteger rank = BigInteger.valueOf(i);
                    int[] expected = all.get(i).stream().mapToInt(Integer::intValue).toArray();
                    assertArrayEquals(expected, unrankOf.permutation().multiset(rank, frequencies));
                    List<Integer> byRank = permutation.multiset(options).byRanks(List.of(rank)).iterator().next();
                    assertArrayEquals(expected, byRank.stream().mapToInt(Integer::intValue).toArray());
                }
            }
            assertThrows(RuntimeException.class,
                    () -> unrankOf.permutation().multiset(BigInteger.valueOf(3), 2, 1));
        }

        @Test
        @DisplayName("multiset combination unranking agrees with builder byRanks")
        void multisetCombinationUnrankMatchesBuilder() {
            int[][] frequencyCases = {{}, {1}, {2}, {2, 1}, {1, 2, 1}, {2, 2, 1}, {0, 2, 1}};
            for (int[] frequencies : frequencyCases) {
                LinkedHashMap<Integer, Integer> options = asFrequencyMap(frequencies);
                int totalItems = java.util.Arrays.stream(frequencies).sum();
                for (int r = 0; r <= totalItems + 1; r++) {
                    List<Map<Integer, Integer>> all = combination.multiset(options, r).lexOrder().stream().toList();
                    for (int i = 0; i < all.size(); i++) {
                        int[] expected = new int[frequencies.length];
                        all.get(i).forEach((type, count) -> expected[type] = count);
                        BigInteger rank = BigInteger.valueOf(i);
                        assertArrayEquals(expected, unrankOf.combination().multiset(rank, frequencies, r));
                        Map<Integer, Integer> byRank = combination.multiset(options, r)
                                .byRanks(List.of(rank)).iterator().next();
                        int[] actual = new int[frequencies.length];
                        byRank.forEach((type, count) -> actual[type] = count);
                        assertArrayEquals(expected, actual);
                    }
                }
            }
            assertThrows(RuntimeException.class,
                    () -> unrankOf.combination().multiset(BigInteger.ZERO, new int[]{1, 1}, 3));
        }
    }

    private static LinkedHashMap<Integer, Integer> asFrequencyMap(int[] frequencies) {
        LinkedHashMap<Integer, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) result.put(i, frequencies[i]);
        return result;
    }

}
