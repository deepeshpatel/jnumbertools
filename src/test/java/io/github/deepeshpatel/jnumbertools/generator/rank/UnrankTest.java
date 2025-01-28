package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.*;

public class UnrankTest {

    @Test
    void shouldGenerateCorrectUniquePermutationForGivenRank() {
        int[] expected = {3, 2, 1, 0};
        int[] permutation = JNumberTools.unRankingOf().uniquePermutation(BigInteger.valueOf(23), 4);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectKPermutationForGivenRank() {
        int[] expected = {4, 6, 2, 0};
        int[] permutation = JNumberTools.unRankingOf().kPermutation(BigInteger.valueOf(1000), 8, 4);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectUniqueCombinationForGivenRank() {
        int[] expected = {1, 2, 3, 4};
        int[] combination = JNumberTools.unRankingOf().uniqueCombination(BigInteger.valueOf(35), 8, 4);
        assertArrayEquals(expected, combination);
    }

    @Test
    void shouldThrowExceptionWhileUnRankingOutOrRange() {
        int n = 4;
        int r = 2;
        long totalPermutations = calculator.nPr(n, r).longValue(); //from 0 to n-1

        Exception exception = assertThrows(ArithmeticException.class,
                () -> JNumberTools.unRankingOf().kPermutation(BigInteger.valueOf(totalPermutations), n, r));

        String output = String.format(">= Permutation(%d,%d)", n, r);
        assertTrue(exception.getMessage().contains(output));
    }

}
