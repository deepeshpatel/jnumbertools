package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.entrypoint.Permutations;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.tools;

public class RankTest {

    @Test
    public void shouldGenerateCorrectRankOfUniquePermutation() {

        BigInteger rank = tools.rankOf().uniquePermutation(3,2,1,0);
        Assert.assertEquals(23, rank.intValue() );
    }

    @Test
    public void shouldGenerateCorrectRankOfKPermutation() {
        BigInteger rank = tools.rankOf().kPermutation(8,4,6,2,0);
        Assert.assertEquals(1000, rank.intValue() );
    }

    @Test
    public void shouldGenerateCorrectRankOfRepetitivePermutation() {

        int base = 4;
        int expected = 0;
        int size = 6;
        var lists = new Permutations().repetitive(size, base).lexOrder().stream().toList();

        for(var list: lists) {
            BigInteger result = tools.rankOf().repeatedPermutation(base, list.toArray(new Integer[size]));
            Assert.assertEquals(expected++, result.intValue());
        }
    }

    @Test
    public void shouldGenerateCorrectRankOfUniqueCombination() {
        BigInteger rank = tools.rankOf().uniqueCombination(8, 1,2,3,4);
        Assert.assertEquals(35, rank.intValue() );
    }

}
