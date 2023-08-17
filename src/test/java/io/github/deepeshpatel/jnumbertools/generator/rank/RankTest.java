package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class RankTest {

    @Test
    public void shouldGenerateCorrectRankOfUniquePermutation() {

        BigInteger rank = JNumberTools.rankOf().uniquePermutation(3,2,1,0);
        Assert.assertEquals(23, rank.intValue() );
    }

    @Test
    public void shouldGenerateCorrectRankOfKPermutation() {
        BigInteger rank = JNumberTools.rankOf().kPermutation(8,4,6,2,0);
        Assert.assertEquals(1000, rank.intValue() );
    }

    @Test
    public void shouldGenerateCorrectRankOfUniqueCombination() {
        BigInteger rank = JNumberTools.rankOf().uniqueCombination(8, 1,2,3,4);
        Assert.assertEquals(35, rank.intValue() );
    }

}
