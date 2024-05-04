package io.github.deepeshpatel.jnumbertools.generator.rank;

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
    public void shouldGenerateCorrectRankOfUniqueCombination() {
        BigInteger rank = tools.rankOf().uniqueCombination(8, 1,2,3,4);
        Assert.assertEquals(35, rank.intValue() );
    }

}
