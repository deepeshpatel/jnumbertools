package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation rules for the "RANK / UNRANK HELPERS" that currently lack input validation:
 *   rank:   RankOf.uniqueCombination, RankOf.repeatedPermutation
 *   unrank: UnrankOf.uniqueCombination
 * (uniquePermutation, kPermutation and derangement are covered in RankOfTest / UnrankOfTest.)
 *
 * The rules: an array that is not a valid arrangement (duplicate value, or value outside [0, n)), a negative
 * size, a rank below 0 or at/above the number of arrangements all signal an error; null signals a null-input
 * error. As in RankOfTest the exception type is not asserted for the invalid-argument cases, only that an
 * error is signaled.
 *
 * The valid input of uniqueCombination is ascending (RankOf's Javadoc: "indices in lexicographical order"):
 * ranking the i-th combination of Combinations.unique(n, r).lexOrder() gives i. The guards at the end pin that,
 * independently of unrank, so adding validation cannot change any answer for a valid combination.
 *
 * NOT covered here, because it needs a decision: RankOfTest currently pins uniqueCombination(5, 2, 1, 0) == 2,
 * but {2,1,0} is not ascending and 2 is not the rank of {0,1,2} (that is 0); descending input gives
 * non-unique numbers ([2,1,0], [3,1,0], [4,1,0] all give 2). Likewise UnrankOfTest pins
 * uniqueCombination(rank=35, n=5, k=3) == [2,3,4] although 5C3 = 10. Both existing cases would have to change
 * (to (5, 0,1,2) == 0 and rank 9) if the rules below are implemented.
 */
class RankUnrankValidationRulesTest {

    private static BigInteger bi(long v) {
        return BigInteger.valueOf(v);
    }

    // ------------------------------------------------------------------------------------
    // RankOf.uniqueCombination(n, indices...)
    // ------------------------------------------------------------------------------------

    /** Duplicate indices are rejected. */
    @Test
    @DisplayName("rank: uniqueCombination rejects a duplicate index")
    void uniqueCombinationRejectsDuplicates() {
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(5, 1, 1, 2));
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(5, 0, 3, 3));
    }

    /** Indices outside [0, n) are rejected. */
    @Test
    @DisplayName("rank: uniqueCombination rejects indices outside [0, n)")
    void uniqueCombinationRejectsIndicesOutsideRange() {
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(5, 0, 1, 7));
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(5, -1, 0, 1));
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(5, 0, 1, 5));
    }

    /** A selection longer than n is rejected. */
    @Test
    @DisplayName("rank: uniqueCombination rejects a combination longer than n")
    void uniqueCombinationRejectsTooManyIndices() {
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(2, 0, 1, 2));
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(0, 0));
    }

    /** Negative n is rejected. */
    @Test
    @DisplayName("rank: uniqueCombination rejects a negative n")
    void uniqueCombinationRejectsNegativeN() {
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(-1));
        assertThrows(RuntimeException.class, () -> rankOf.uniqueCombination(-3, 0, 1));
    }

    /** Null array is a null-input error. */
    @Test
    @DisplayName("rank: uniqueCombination of a null array is a null-input error")
    void uniqueCombinationRejectsNull() {
        assertThrows(NullPointerException.class, () -> rankOf.uniqueCombination(5, (int[]) null));
    }

    // ------------------------------------------------------------------------------------
    // RankOf.repeatedPermutation(n, digits...)
    // ------------------------------------------------------------------------------------

    /** Values outside [0, n) are rejected. */
    @Test
    @DisplayName("rank: repeatedPermutation rejects values outside [0, n)")
    void repeatedPermutationRejectsValuesOutsideRange() {
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(3, 0, 3, 1));
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(3, 0, -1, 1));
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(3, 7));
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(1, 1));
    }

    /** Negative n is rejected. */
    @Test
    @DisplayName("rank: repeatedPermutation rejects a negative n")
    void repeatedPermutationRejectsNegativeN() {
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(-1, 0));
        assertThrows(RuntimeException.class, () -> rankOf.repeatedPermutation(-2));
    }

    /** Null array or null element is a null-input error. */
    @Test
    @DisplayName("rank: repeatedPermutation of a null array or null element is a null-input error")
    void repeatedPermutationRejectsNull() {
        assertThrows(NullPointerException.class, () -> rankOf.repeatedPermutation(3, (Integer[]) null));
        assertThrows(NullPointerException.class, () -> rankOf.repeatedPermutation(3, 0, null, 1));
    }

    // ------------------------------------------------------------------------------------
    // UnrankOf.uniqueCombination(rank, n, k)
    // ------------------------------------------------------------------------------------

    /** Negative rank is rejected. */
    @Test
    @DisplayName("unrank: uniqueCombination rejects a negative rank")
    void unrankCombinationRejectsNegativeRank() {
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(bi(-1), 5, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(bi(-1), 0, 0));
    }

    /** A rank >= nCr is rejected. */
    @Test
    @DisplayName("unrank: uniqueCombination rejects a rank >= nCr")
    void unrankCombinationRejectsRankBeyondTheLastCombination() {
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(bi(10), 5, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(bi(11), 5, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(bi(35), 5, 3));
        // a domain with exactly one combination (r == n, or r == 0): only rank 0 exists
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ONE, 3, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ONE, 3, 0));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ONE, 0, 0));
    }

    /** k > n is rejected (every rank is out of range). */
    @Test
    @DisplayName("unrank: uniqueCombination rejects k > n, where every rank is out of range")
    void unrankCombinationRejectsSelectionLargerThanTheSet() {
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ZERO, 3, 5));
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ZERO, 0, 1));
    }

    /** Negative n or k is rejected. */
    @Test
    @DisplayName("unrank: uniqueCombination rejects a negative n or k")
    void unrankCombinationRejectsNegativeSizes() {
        assertThrows(RuntimeException.class, () -> unrankOf.uniqueCombination(BigInteger.ZERO, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> unrankOf.uniqueCombination(BigInteger.ZERO, 5, -1));
    }

    /** Null rank is a null-input error. */
    @Test
    @DisplayName("unrank: uniqueCombination of a null rank is a null-input error")
    void unrankCombinationRejectsNullRank() {
        assertThrows(NullPointerException.class, () -> unrankOf.uniqueCombination(null, 5, 3));
    }

    // ------------------------------------------------------------------------------------
    // Valid input must keep working (regression guards)
    // ------------------------------------------------------------------------------------

    /** Ascending indices give the position in the real lexicographic enumeration (independent of unrank). */
    @Test
    @DisplayName("rank: uniqueCombination gives the lexicographic position of every ascending combination, n <= 7")
    void uniqueCombinationIsTheLexicographicPosition() {
        for (int n = 0; n <= 7; n++) {
            for (int r = 0; r <= n; r++) {
                int position = 0;
                for (List<Integer> combo : combination.unique(n, r).lexOrder()) {
                    int[] indices = combo.stream().mapToInt(Integer::intValue).toArray();
                    assertEquals(bi(position), rankOf.uniqueCombination(n, indices), "n=" + n + " r=" + r + " " + combo);
                    position++;
                }
            }
        }
    }

    /** unrank(i) is the i-th combination of the real enumeration, for every valid rank (independent of rank()). */
    @Test
    @DisplayName("unrank: uniqueCombination returns the i-th lexicographic combination for every valid rank, n <= 7")
    void unrankCombinationIsTheIthLexicographicCombination() {
        for (int n = 0; n <= 7; n++) {
            for (int r = 0; r <= n; r++) {
                List<int[]> expected = new ArrayList<>();
                for (List<Integer> combo : combination.unique(n, r).lexOrder()) {
                    expected.add(combo.stream().mapToInt(Integer::intValue).toArray());
                }
                assertEquals(calculator.nCr(n, r).intValueExact(), expected.size());
                for (int i = 0; i < expected.size(); i++) {
                    assertArrayEquals(expected.get(i), unrankOf.uniqueCombination(bi(i), n, r), "n=" + n + " r=" + r + " rank=" + i);
                }
            }
        }
    }

    /** Valid digits give the base-n value, most significant digit first (RankOfTest pins (4, 1,2,3) = 27). */
    @Test
    @DisplayName("rank: repeatedPermutation gives the base-n value of every valid digit string")
    void repeatedPermutationIsTheBaseNValue() {
        for (int n = 1; n <= 4; n++) {
            for (int a = 0; a < n; a++) {
                assertEquals(bi(a), rankOf.repeatedPermutation(n, a), "n=" + n + " [" + a + "]");
                for (int b = 0; b < n; b++) {
                    assertEquals(bi((long) a * n + b), rankOf.repeatedPermutation(n, a, b), "n=" + n + " [" + a + "," + b + "]");
                    for (int c = 0; c < n; c++) {
                        long expected = ((long) a * n + b) * n + c;
                        assertEquals(bi(expected), rankOf.repeatedPermutation(n, a, b, c), "n=" + n + " [" + a + "," + b + "," + c + "]");
                    }
                }
            }
        }
    }
}