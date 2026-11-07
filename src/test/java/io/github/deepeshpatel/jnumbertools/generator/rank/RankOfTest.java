package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RankOf Factory")
public class RankOfTest {

    @Nested
    @DisplayName("Permutation ranks")
    class PermutationRanksTests {

        @Test
        @DisplayName("uniquePermutation(3,2,1,0) = rank 23")
        void uniquePermutationRank() {
            BigInteger rank = rankOf.permutation().unique(3, 2, 1, 0);
            assertEquals(23, rank.intValue());
        }

        @Test
        @DisplayName("kPermutation(2,1,0) = rank 1")
        void kPermutationRank() {
            BigInteger rank = rankOf.permutation().k(2, 1, 0);
            assertEquals(1, rank.intValue());
        }

        @Test
        @DisplayName("repeatedPermutation with base 4")
        void repeatedPermutationRank() {
            BigInteger rank1 = rankOf.permutation().repetitive(4, 1, 2, 3);
            assertEquals(27, rank1.intValue());

            BigInteger rank2 = rankOf.permutation().repetitive(4, 3, 1, 2, 3);
            assertEquals(219, rank2.intValue());
        }

        @Test
        @DisplayName("repeatedPermutation with large base: (4,3,3,3,3) = rank 255")
        void repeatedPermutationRankLargeBase() {
            BigInteger rank = rankOf.permutation().repetitive(4, 3, 3, 3, 3);
            assertEquals(255, rank.intValue());
        }
    }

    @Nested
    @DisplayName("Combination ranks")
    class CombinationRanksTests {

        @Test
        @DisplayName("uniqueCombination(5, {0,1,2}) = rank 0, {0,1,3} = 1, {2,3,4} = 9 (5C3 = 10)")
        void uniqueCombinationRank() {
            // the combination is the ascending list of indices (a descending {2,1,0} is rejected, see
            // RankUnrankValidationRulesTest)
            assertEquals(0, rankOf.combination().unique(5, 0, 1, 2).intValue());
            assertEquals(1, rankOf.combination().unique(5, 0, 1, 3).intValue());
            assertEquals(9, rankOf.combination().unique(5, 2, 3, 4).intValue());
        }
    }

    @Nested
    @DisplayName("Derangement ranks")
    class DerangementRanksTests {

        @Test
        @DisplayName("derangement round-trip: unrank then rank returns same index")
        void derangementRankRoundTrip() {
            int[] derangementAtRank5ForN4 = unrankOf.permutation().derangement(5, 4);
            BigInteger rank = rankOf.permutation().derangement(derangementAtRank5ForN4);
            assertEquals(BigInteger.valueOf(5), rank);
        }
    }

    @Nested
    @DisplayName("Invalid arrangements (ValidationRules.md: rank / unrank helpers)")
    class InvalidArrangements {
        // ValidationRules.md specifies the CONDITION (an error is signaled), not the exception type.


        @Test
        @DisplayName("uniquePermutation rejects duplicates and values outside [0, n)")
        void uniquePermutationRejectsInvalidArrangements() {
            assertThrows(RuntimeException.class, () -> rankOf.permutation().unique(1, 1, 2));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().unique(0, 2));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().unique(-1, 0));
        }

        @Test
        @DisplayName("kPermutation rejects duplicates, values outside [0, n) and a too long arrangement")
        void kPermutationRejectsInvalidArrangements() {
            assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, 1, 1, 2));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, 7, 0, 1));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, -1, 0, 1));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().k(2, 0, 1, 2));
        }

        @Test
        @DisplayName("null arrangement is a null-input error")
        void nullArrangementIsRejected() {
            assertThrows(RuntimeException.class, () -> rankOf.permutation().unique((int[]) null));
            assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, (int[]) null));
        }

        @Test
        @DisplayName("rank(unrank(r)) == r for every valid unique permutation, n <= 5")
        void uniquePermutationRoundTrip() {
            for (int n = 0; n <= 5; n++) {
                BigInteger total = calculator.factorial(n);
                for (BigInteger r = BigInteger.ZERO; r.compareTo(total) < 0; r = r.add(BigInteger.ONE)) {
                    assertEquals(r, rankOf.permutation().unique(unrankOf.permutation().unique(r, n)), "n=" + n + " r=" + r);
                }
            }
        }
    }

    @Nested
    @DisplayName("Repetitive combination ranks")
    class RepetitiveCombinationRankTests {

        @Test
        @DisplayName("repetitiveCombination ranks agree with builder byRanks ordering")
        void repetitiveCombinationRankMatchesBuilder() {
            for (int n = 0; n <= 5; n++) {
                for (int r = 0; r <= 5; r++) {
                    BigInteger total = calculator.nCrRepetitive(n, r);
                    for (BigInteger rank = BigInteger.ZERO; rank.compareTo(total) < 0;
                         rank = rank.add(BigInteger.ONE)) {
                        var expected = combination.repetitive(n, r)
                                .byRanks(java.util.List.of(rank)).iterator().next();
                        int[] indices = expected.stream().mapToInt(Integer::intValue).toArray();
                        assertEquals(rank, rankOf.combination().repetitive(n, indices));
                    }
                }
            }
            assertThrows(RuntimeException.class, () -> rankOf.combination().repetitive(3, 1, 0));
        }
    }

    @Nested
    @DisplayName("Multiset ranks")
    class MultisetRankTests {

        @Test
        @DisplayName("multiset permutation ranks agree with builder lexicographical order")
        void multisetPermutationRankMatchesBuilder() {
            int[][] frequencyCases = {{}, {1}, {2}, {2, 1}, {1, 2, 1}, {2, 2, 1}, {0, 2, 1}};
            for (int[] frequencies : frequencyCases) {
                java.util.LinkedHashMap<Integer, Integer> options = asFrequencyMap(frequencies);
                var all = permutation.multiset(options).lexOrder().stream().toList();
                for (int i = 0; i < all.size(); i++) {
                    int[] values = all.get(i).stream().mapToInt(Integer::intValue).toArray();
                    assertEquals(BigInteger.valueOf(i), rankOf.permutation().multiset(frequencies, values));
                }
            }
            assertThrows(RuntimeException.class,
                    () -> rankOf.permutation().multiset(new int[]{2, 1}, new int[]{0, 0, 0}));
        }

        @Test
        @DisplayName("multiset combination ranks agree with builder lexicographical order")
        void multisetCombinationRankMatchesBuilder() {
            int[][] frequencyCases = {{}, {1}, {2}, {2, 1}, {1, 2, 1}, {2, 2, 1}, {0, 2, 1}};
            for (int[] frequencies : frequencyCases) {
                java.util.LinkedHashMap<Integer, Integer> options = asFrequencyMap(frequencies);
                int totalItems = java.util.Arrays.stream(frequencies).sum();
                for (int r = 0; r <= totalItems + 1; r++) {
                    var all = combination.multiset(options, r).lexOrder().stream().toList();
                    for (int i = 0; i < all.size(); i++) {
                        int[] counts = new int[frequencies.length];
                        all.get(i).forEach((type, count) -> counts[type] = count);
                        assertEquals(BigInteger.valueOf(i), rankOf.combination().multiset(frequencies, counts));
                    }
                }
            }
            assertThrows(RuntimeException.class,
                    () -> rankOf.combination().multiset(new int[]{2, 1}, new int[]{2, 2}));
        }
    }

    private static java.util.LinkedHashMap<Integer, Integer> asFrequencyMap(int[] frequencies) {
        java.util.LinkedHashMap<Integer, Integer> result = new java.util.LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) result.put(i, frequencies[i]);
        return result;
    }

}
