package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationRules.md "RANK / UNRANK HELPERS" for k-permutations, through the public helpers only
 * (RankOf / UnrankOf). The arithmetic classes in the numbersystem package perform no validation of their own;
 * these helpers own it, so this is where it is tested.
 *
 * ValidationRules.md specifies the CONDITION (an error is signaled), not the exception type.
 */
class KPermutationRankUnrankValidationTest {

    private static BigInteger bi(long v) {
        return BigInteger.valueOf(v);
    }

    @Test
    @DisplayName("rank: kPermutation rejects a value equal to or above the set size, and a negative set size")
    void rankRejectsValueAtOrAboveSizeAndNegativeSize() {
        assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, 5, 0, 1));
        assertThrows(RuntimeException.class, () -> rankOf.permutation().k(5, 0, 1, 99));
        assertThrows(RuntimeException.class, () -> rankOf.permutation().k(-1));
        assertThrows(RuntimeException.class, () -> rankOf.permutation().k(-2, 0));
        assertThrows(RuntimeException.class, () -> rankOf.permutation().k(0, 0));     // longer than the set
    }

    @Test
    @DisplayName("unrank: kPermutation accepts exactly the ranks 0 .. nPk-1 (5P3 = 60)")
    void unrankAcceptsExactlyTheValidRanks() {
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(-1), 5, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(60), 5, 3));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(10_000), 5, 3));
        assertArrayEquals(new int[]{0, 1, 2}, unrankOf.permutation().k(bi(0), 5, 3));
        assertArrayEquals(new int[]{4, 3, 2}, unrankOf.permutation().k(bi(59), 5, 3));
        // k == 0 has exactly one (empty) arrangement
        assertArrayEquals(new int[]{}, unrankOf.permutation().k(bi(0), 5, 0));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(1), 5, 0));
    }

    @Test
    @DisplayName("unrank: kPermutation rejects a negative n or k, k > n, and a null rank")
    void unrankRejectsBadSizes() {
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(0), -1, 0));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(0), 3, -1));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(0), 3, 5));
        assertThrows(RuntimeException.class, () -> unrankOf.permutation().k(bi(0), 0, 1));
        assertThrows(NullPointerException.class, () -> unrankOf.permutation().k(null, 5, 3));
    }

    @Test
    @DisplayName("rank/unrank: kPermutation round trip in both directions for every valid rank, n <= 5")
    void roundTripForEveryValidRank() {
        for (int n = 0; n <= 5; n++) {
            for (int k = 0; k <= n; k++) {
                BigInteger total = calculator.nPr(n, k);
                for (BigInteger r = BigInteger.ZERO; r.compareTo(total) < 0; r = r.add(BigInteger.ONE)) {
                    int[] arrangement = unrankOf.permutation().k(r, n, k);
                    assertEquals(k, arrangement.length);
                    assertEquals(r, rankOf.permutation().k(n, arrangement), "n=" + n + " k=" + k + " r=" + r);
                    assertArrayEquals(arrangement, unrankOf.permutation().k(rankOf.permutation().k(n, arrangement), n, k));
                }
            }
        }
    }
}
