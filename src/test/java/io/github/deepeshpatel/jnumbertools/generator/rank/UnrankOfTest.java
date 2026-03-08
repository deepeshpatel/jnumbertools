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

        String output = String.format("≥ Permutation(%d,%d)", n, r);
        assertTrue(exception.getMessage().contains(output));
    }

    @Test
    void shouldGenerateUniquePermutationWithMinimumSize() {
        // Test rank 5 - smallest n with n! > 5 is 3 (3! = 6)
        BigInteger rank = BigInteger.valueOf(5);
        int[] permutation = unrankOf.uniquePermutationMinimumSize(rank);
        assertEquals(3, permutation.length);

        // Verify it's the correct permutation at rank 5 for size 3
        // All permutations of size 3 in lex order:
        // 0:[0,1,2], 1:[0,2,1], 2:[1,0,2], 3:[1,2,0], 4:[2,0,1], 5:[2,1,0]
        int[] expected = {2, 1, 0};
        assertArrayEquals(expected, permutation);

        // Test rank 0 (special case)
        int[] zeroPerm = unrankOf.uniquePermutationMinimumSize(BigInteger.ZERO);
        assertEquals(1, zeroPerm.length);
        assertArrayEquals(new int[]{0}, zeroPerm);

        // Test boundary: rank 5 (within 3!)
        // rank 6 would require size 4 since 3! = 6 and rank must be < n!
        BigInteger rank6 = BigInteger.valueOf(6);
        int[] perm6 = unrankOf.uniquePermutationMinimumSize(rank6);
        assertEquals(4, perm6.length);

        // Test large rank
        BigInteger rank1000 = BigInteger.valueOf(1000);
        int[] perm1000 = unrankOf.uniquePermutationMinimumSize(rank1000);
        // 6! = 720, 7! = 5040, so rank 1000 requires size 7
        assertEquals(7, perm1000.length);
    }
}
