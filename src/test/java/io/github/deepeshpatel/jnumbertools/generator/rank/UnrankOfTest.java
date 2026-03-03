package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

public class UnrankOfTest {

    @Test
    void shouldGenerateCorrectUniquePermutationForGivenRank() {
        int[] expected = {3, 2, 1, 0};
        int[] permutation = unrankOf.uniquePermutation(BigInteger.valueOf(23), 4);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectKPermutationForGivenRank() {
        int[] expected = {0, 1, 7};
        int[] permutation = unrankOf.kPermutation(BigInteger.valueOf(5), 10, 3);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectUniqueCombinationForGivenRank() {
        int[] expected = {2, 3, 4};
        int[] combination = unrankOf.uniqueCombination(BigInteger.valueOf(35), 5, 3);
        assertArrayEquals(expected, combination);
    }

    @Test
    void shouldGenerateCorrectRepetitivePermutationForGivenRank() {
        int[] expected = {0, 1, 2, 3};
        int[] permutation = unrankOf.uniquePermutation(BigInteger.valueOf(0), 4);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectRepetitivePermutationForLargeRank() {
        int[] expected = {3, 2, 1, 0};
        int[] permutation = unrankOf.uniquePermutation(BigInteger.valueOf(23), 4);
        assertArrayEquals(expected, permutation);
    }

    @Test
    void shouldGenerateCorrectRepetitiveCombinationForGivenRank() {
        int[] expected = {0, 3, 1, 2};
        int[] combination = unrankOf.uniquePermutation(BigInteger.valueOf(4), 4);
        assertArrayEquals(expected, combination);
    }

    @Test
    void shouldThrowExceptionWhileUnRankingOutOrRange() {
        int n = 4;
        int r = 2;
        long totalPermutations = calculator.nPr(n, r).longValue(); //from 0 to n-1

        Exception exception = assertThrows(ArithmeticException.class,
                () -> JNumberTools.unrankOf().kPermutation(BigInteger.valueOf(totalPermutations), n, r));

        String output = String.format(">= Permutation(%d,%d)", n, r);
        assertTrue(exception.getMessage().contains(output));
    }
}
