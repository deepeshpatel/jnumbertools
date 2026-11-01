package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RankOf Factory")
public class RankOfTest {

    @Nested
    @DisplayName("Permutation ranks")
    class PermutationRanksTests {

        @Test
        @DisplayName("uniquePermutation(3,2,1,0) = rank 23")
        void uniquePermutationRank() {
            BigInteger rank = rankOf.uniquePermutation(3, 2, 1, 0);
            assertEquals(23, rank.intValue());
        }

        @Test
        @DisplayName("kPermutation(2,1,0) = rank 1")
        void kPermutationRank() {
            BigInteger rank = rankOf.kPermutation(2, 1, 0);
            assertEquals(1, rank.intValue());
        }

        @Test
        @DisplayName("repeatedPermutation with base 4")
        void repeatedPermutationRank() {
            BigInteger rank1 = rankOf.repeatedPermutation(4, 1, 2, 3);
            assertEquals(27, rank1.intValue());

            BigInteger rank2 = rankOf.repeatedPermutation(4, 3, 1, 2, 3);
            assertEquals(219, rank2.intValue());
        }

        @Test
        @DisplayName("repeatedPermutation with large base: (4,3,3,3,3) = rank 255")
        void repeatedPermutationRankLargeBase() {
            BigInteger rank = rankOf.repeatedPermutation(4, 3, 3, 3, 3);
            assertEquals(255, rank.intValue());
        }
    }

    @Nested
    @DisplayName("Combination ranks")
    class CombinationRanksTests {

        @Test
        @DisplayName("uniqueCombination(5,2,1,0) = rank 2")
        void uniqueCombinationRank() {
            BigInteger rank = rankOf.uniqueCombination(5, 2, 1, 0);
            assertEquals(2, rank.intValue());
        }
    }

    @Nested
    @DisplayName("Derangement ranks")
    class DerangementRanksTests {

        @Test
        @DisplayName("derangement round-trip: unrank then rank returns same index")
        void derangementRankRoundTrip() {
            int[] derangementAtRank5ForN4 = unrankOf.derangement(5, 4);
            BigInteger rank = rankOf.derangement(derangementAtRank5ForN4);
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
            assertThrows(RuntimeException.class, () -> rankOf.uniquePermutation(1, 1, 2));
            assertThrows(RuntimeException.class, () -> rankOf.uniquePermutation(0, 2));
            assertThrows(RuntimeException.class, () -> rankOf.uniquePermutation(-1, 0));
        }

        @Test
        @DisplayName("kPermutation rejects duplicates, values outside [0, n) and a too long arrangement")
        void kPermutationRejectsInvalidArrangements() {
            assertThrows(RuntimeException.class, () -> rankOf.kPermutation(5, 1, 1, 2));
            assertThrows(RuntimeException.class, () -> rankOf.kPermutation(5, 7, 0, 1));
            assertThrows(RuntimeException.class, () -> rankOf.kPermutation(5, -1, 0, 1));
            assertThrows(RuntimeException.class, () -> rankOf.kPermutation(2, 0, 1, 2));
        }

        @Test
        @DisplayName("null arrangement is a null-input error")
        void nullArrangementIsRejected() {
            assertThrows(RuntimeException.class, () -> rankOf.uniquePermutation((int[]) null));
            assertThrows(RuntimeException.class, () -> rankOf.kPermutation(5, (int[]) null));
        }

        @Test
        @DisplayName("rank(unrank(r)) == r for every valid unique permutation, n <= 5")
        void uniquePermutationRoundTrip() {
            for (int n = 0; n <= 5; n++) {
                BigInteger total = calculator.factorial(n);
                for (BigInteger r = BigInteger.ZERO; r.compareTo(total) < 0; r = r.add(BigInteger.ONE)) {
                    assertEquals(r, rankOf.uniquePermutation(unrankOf.uniquePermutation(r, n)), "n=" + n + " r=" + r);
                }
            }
        }
    }
}