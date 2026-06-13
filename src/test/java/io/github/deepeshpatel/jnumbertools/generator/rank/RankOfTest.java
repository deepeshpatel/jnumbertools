package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
