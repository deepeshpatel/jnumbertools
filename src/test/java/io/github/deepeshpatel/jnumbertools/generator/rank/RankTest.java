package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankTest {

    @Test
    void shouldGenerateCorrectRankOfUniquePermutation() {

        BigInteger rank = JNumberTools.rankOf().uniquePermutation(3, 2, 1, 0);
        assertEquals(23, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfKPermutation() {
        BigInteger rank = JNumberTools.rankOf().kPermutation(8, 4, 6, 2, 0);
        assertEquals(1000, rank.intValue());
    }

    @Test
    void shouldGenerateCorrectRankOfRepetitivePermutation() {

        int base = 4;
        int expected = 0;
        int size = 6;
        var lists = new Permutations().repetitive(size, base).lexOrder().stream().toList();

        for (var list : lists) {
            BigInteger result = JNumberTools.rankOf().repeatedPermutation(base, list.toArray(new Integer[size]));
            assertEquals(expected++, result.intValue());
        }
    }

    @Test
    void shouldGenerateCorrectRankOfUniqueCombination() {
        BigInteger rank = JNumberTools.rankOf().uniqueCombination(8, 1, 2, 3, 4);
        assertEquals(35, rank.intValue());
    }

}
