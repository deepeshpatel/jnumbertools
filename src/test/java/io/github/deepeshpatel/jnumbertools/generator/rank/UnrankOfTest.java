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

    @Test
    void shouldGenerateCorrectDerangementForGivenRank() {
        // For n=4, D_4 = 9. Rank 0 derangement of [0,1,2,3] is [1,0,3,2].
        int[] expected = {1, 0, 3, 2};
        int[] actual = unrankOf.derangement(BigInteger.ZERO, 4);
        assertArrayEquals(expected, actual);

        // long overload should agree
        assertArrayEquals(expected, unrankOf.derangement(0L, 4));
    }

    @Test
    void shouldGenerateAllDerangementsByUnrankingEveryRank() {
        // For each n, enumerate all D_n ranks and verify each output is a valid
        // derangement (no fixed point, values are a permutation of [0, n)).
        for (int n = 2; n <= 6; n++) {
            int total = calculator.subFactorial(n).intValue();
            var seen = new java.util.HashSet<String>();
            for (int rank = 0; rank < total; rank++) {
                int[] d = unrankOf.derangement(rank, n);
                assertEquals(n, d.length);
                boolean[] used = new boolean[n];
                for (int i = 0; i < n; i++) {
                    assertNotEquals(i, d[i], "Fixed point at i=" + i + " n=" + n + " rank=" + rank);
                    assertTrue(d[i] >= 0 && d[i] < n);
                    assertFalse(used[d[i]], "Duplicate value at i=" + i);
                    used[d[i]] = true;
                }
                assertTrue(seen.add(java.util.Arrays.toString(d)),
                        "Duplicate derangement at n=" + n + " rank=" + rank);
            }
            assertEquals(total, seen.size());
        }
    }

    @Test
    void shouldThrowForDerangementRankOutOfRange() {
        // D_4 = 9; rank 9 is out of range
        assertThrows(IllegalArgumentException.class,
                () -> unrankOf.derangement(BigInteger.valueOf(9), 4));
        // Negative ranks must also throw
        assertThrows(IllegalArgumentException.class,
                () -> unrankOf.derangement(BigInteger.valueOf(-1), 4));
    }

    @Test
    void shouldThrowForDerangementOfN1() {
        // D_1 = 0; no valid rank exists
        assertThrows(IllegalArgumentException.class,
                () -> unrankOf.derangement(BigInteger.ZERO, 1));
    }

    @Test
    void shouldUnrankDerangementViaJNumberToolsFacade() {
        int[] viaFacade = JNumberTools.unrankOf().derangement(BigInteger.ZERO, 4);
        int[] viaInstance = unrankOf.derangement(BigInteger.ZERO, 4);
        assertArrayEquals(viaInstance, viaFacade);
    }
}
