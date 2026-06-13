package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnrankOf Factory")
public class UnrankOfTest {

    @Nested
    @DisplayName("Unique permutations")
    class UniquePermutationTests {

        @Test
        @DisplayName("uniquePermutation(rank=23, n=4) = [3,2,1,0]")
        void correctPermutationForGivenRank() {
            int[] expected = {3, 2, 1, 0};
            int[] permutation = unrankOf.uniquePermutation(BigInteger.valueOf(23), 4);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutation(rank=0, n=4) = [0,1,2,3]")
        void firstPermutation() {
            int[] expected = {0, 1, 2, 3};
            int[] permutation = unrankOf.uniquePermutation(BigInteger.valueOf(0), 4);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutationMinimumSize(rank=5) = size 3 = [2,1,0]")
        void minimumSizeForRank() {
            BigInteger rank = BigInteger.valueOf(5);
            int[] permutation = unrankOf.uniquePermutationMinimumSize(rank);
            assertEquals(3, permutation.length);
            int[] expected = {2, 1, 0};
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("uniquePermutationMinimumSize boundary cases: rank=0, rank=6, rank=1000")
        void minimumSizeBoundary() {
            // rank 0
            int[] zeroPerm = unrankOf.uniquePermutationMinimumSize(BigInteger.ZERO);
            assertEquals(1, zeroPerm.length);
            assertArrayEquals(new int[]{0}, zeroPerm);

            // rank 6 requires size 4 (3! = 6, so rank 6 needs 4!)
            BigInteger rank6 = BigInteger.valueOf(6);
            int[] perm6 = unrankOf.uniquePermutationMinimumSize(rank6);
            assertEquals(4, perm6.length);

            // rank 1000 requires size 7 (6!=720, 7!=5040, so rank 1000 needs 7!)
            BigInteger rank1000 = BigInteger.valueOf(1000);
            int[] perm1000 = unrankOf.uniquePermutationMinimumSize(rank1000);
            assertEquals(7, perm1000.length);
        }
    }

    @Nested
    @DisplayName("K-Permutations")
    class KPermutationTests {

        @Test
        @DisplayName("kPermutation(rank=5, n=10, k=3) = [0,1,7]")
        void correctKPermutation() {
            int[] expected = {0, 1, 7};
            int[] permutation = unrankOf.kPermutation(BigInteger.valueOf(5), 10, 3);
            assertArrayEquals(expected, permutation);
        }

        @Test
        @DisplayName("kPermutation out-of-range throws ArithmeticException")
        void outOfRangeThrows() {
            int n = 4;
            int r = 2;
            long totalPermutations = calculator.nPr(n, r).longValue();

            Exception exception = assertThrows(ArithmeticException.class,
                    () -> JNumberTools.unrankOf().kPermutation(BigInteger.valueOf(totalPermutations), n, r));

            String output = String.format("≥ Permutation(%d,%d)", n, r);
            assertTrue(exception.getMessage().contains(output));
        }
    }

    @Nested
    @DisplayName("Unique combinations")
    class UniqueCombinationTests {

        @Test
        @DisplayName("uniqueCombination(rank=35, n=5, k=3) = [2,3,4]")
        void correctCombination() {
            int[] expected = {2, 3, 4};
            int[] combination = unrankOf.uniqueCombination(BigInteger.valueOf(35), 5, 3);
            assertArrayEquals(expected, combination);
        }
    }

    @Nested
    @DisplayName("Derangements")
    class DerangementTests {

        @Test
        @DisplayName("derangement(rank=0, n=4) = [1,0,3,2]")
        void correctDerangement() {
            int[] expected = {1, 0, 3, 2};
            int[] actual = unrankOf.derangement(BigInteger.ZERO, 4);
            assertArrayEquals(expected, actual);
            // Verify long overload agrees
            assertArrayEquals(expected, unrankOf.derangement(0L, 4));
        }

        @Test
        @DisplayName("all derangements enumerated: valid permutations with no fixed points")
        void allDerangementsValid() {
            for (int n = 2; n <= 6; n++) {
                int total = calculator.subFactorial(n).intValue();
                var seen = new java.util.HashSet<String>();
                for (int rank = 0; rank < total; rank++) {
                    int[] d = unrankOf.derangement(rank, n);
                    assertEquals(n, d.length, "Derangement length mismatch for n=" + n + " rank=" + rank);
                    boolean[] used = new boolean[n];
                    for (int i = 0; i < n; i++) {
                        assertNotEquals(i, d[i], "Fixed point at i=" + i + " n=" + n + " rank=" + rank);
                        assertTrue(d[i] >= 0 && d[i] < n, "Value out of range for n=" + n);
                        assertFalse(used[d[i]], "Duplicate value at i=" + i);
                        used[d[i]] = true;
                    }
                    assertTrue(seen.add(java.util.Arrays.toString(d)),
                            "Duplicate derangement at n=" + n + " rank=" + rank);
                }
                assertEquals(total, seen.size(), "Total derangements mismatch for n=" + n);
            }
        }

        @Test
        @DisplayName("derangement rank out of range throws IllegalArgumentException")
        void outOfRangeThrows() {
            // D_4 = 9; rank 9 is out of range
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.derangement(BigInteger.valueOf(9), 4));
            // Negative ranks must also throw
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.derangement(BigInteger.valueOf(-1), 4));
        }

        @Test
        @DisplayName("derangement of n=1 throws IllegalArgumentException (D_1=0)")
        void n1Throws() {
            // D_1 = 0; no valid rank exists
            assertThrows(IllegalArgumentException.class,
                    () -> unrankOf.derangement(BigInteger.ZERO, 1));
        }
    }

    @Nested
    @DisplayName("Integration with JNumberTools facade")
    class IntegrationTests {

        @Test
        @DisplayName("unrankOf() via facade returns same results as static instance")
        void viaJNumberToolsFacade() {
            int[] viaFacade = JNumberTools.unrankOf().derangement(BigInteger.ZERO, 4);
            int[] viaInstance = unrankOf.derangement(BigInteger.ZERO, 4);
            assertArrayEquals(viaInstance, viaFacade);
        }
    }
}
