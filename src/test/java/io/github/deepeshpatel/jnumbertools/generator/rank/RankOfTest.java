package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankOfTest {

    @Test
    void shouldGenerateCorrectRankOfUniquePermutation() {
        BigInteger rank = rankOf.uniquePermutation(3, 2, 1, 0);
        assertEquals(23, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfKPermutation() {
        BigInteger rank = rankOf.kPermutation(2, 1, 0);
        assertEquals(1, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfRepetitivePermutation() {
        int base = 4;
        int expected = 27;
        BigInteger rank = rankOf.repeatedPermutation(4, 1, 2, 3);
        assertEquals(expected, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfRepetitivePermutationForLargeBase() {
        int base = 4;
        int expected = 255;
        BigInteger rank = rankOf.repeatedPermutation(4, 3, 3, 3, 3);
        assertEquals(expected, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfUniqueCombination() {
        BigInteger rank = rankOf.uniqueCombination(5, 2, 1, 0);
        assertEquals(2, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfRepetitiveCombination() {
        BigInteger rank = rankOf.repeatedPermutation(4, 3, 1, 2, 3);
        assertEquals(219, rank.intValue());
    }
}
