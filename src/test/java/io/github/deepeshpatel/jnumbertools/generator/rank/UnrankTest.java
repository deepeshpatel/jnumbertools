package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UnrankTest {

    @Test
    public void shouldGenerateCorrectUniquePermutationForGivenRank() {

        int[] permutation = JNumberTools.unRankingOf().uniquePermutation(BigInteger.valueOf(23),4);
        Assert.assertEquals("[3, 2, 1, 0]", Arrays.toString(permutation));
    }

    @Test
    public void shouldGenerateCorrectKPermutationForGivenRank() {
        int[] permutation = JNumberTools.unRankingOf().kPermutation(BigInteger.valueOf(1000), 8,4);
        Assert.assertEquals("[4, 6, 2, 0]", Arrays.toString(permutation) );
    }

    @Test
    public void shouldGenerateCorrectUniqueCombinationForGivenRank() {
        int[] combination = JNumberTools.unRankingOf().uniqueCombination(BigInteger.valueOf(35),8,4);
        Assert.assertEquals("[1, 2, 3, 4]", Arrays.toString(combination) );
    }

    @Test
    public void shouldThrowExceptionWhileUnRankingOutOrRange() {
        int n=4;
        int r =2;
        long totalPermutations = calculator.nPr(n,r).longValue(); //from 0 to n-1

        Exception exception = assertThrows(ArithmeticException.class, () -> {
            JNumberTools.unRankingOf().kPermutation(BigInteger.valueOf(totalPermutations),n,r );
        });

        String output = String.format(">= Permutation(%d,%d)", n,r);
        assertTrue(exception.getMessage().contains(output));
    }

}
