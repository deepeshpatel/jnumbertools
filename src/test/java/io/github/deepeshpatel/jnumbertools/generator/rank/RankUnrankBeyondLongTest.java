package io.github.deepeshpatel.jnumbertools.generator.rank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.rankOf;
import static io.github.deepeshpatel.jnumbertools.TestBase.unrankOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ranks are BigInteger, so the helpers must be correct beyond the range of a long.
 *
 * PREDICTED on the current code (to be confirmed by running):
 *   FAIL  the repeatedPermutation tests: the rank is accumulated in a long, so valid digit strings whose value
 *         is >= 2^63 silently return a wrong number (for example 64 ones in base 2 returns -1)
 *   PASS  the combination tests (regression guards: validation added to uniqueCombination must stay cheap and
 *         must not reject any valid large input)
 */
class RankUnrankBeyondLongTest {

    /** Independent reference: the base-n value, most significant digit first, computed with BigInteger. */
    private static BigInteger baseNValue(int n, Integer[] digits) {
        BigInteger value = BigInteger.ZERO;
        for (Integer digit : digits) {
            value = value.multiply(BigInteger.valueOf(n)).add(BigInteger.valueOf(digit));
        }
        return value;
    }

    private static Integer[] filled(int length, int digit) {
        Integer[] digits = new Integer[length];
        Arrays.fill(digits, digit);
        return digits;
    }

    @Test
    @DisplayName("rank: repeatedPermutation, base 2 with 64 one-digits is 2^64 - 1")
    void repeatedPermutationAllOnesBase2() {
        assertEquals(BigInteger.TWO.pow(64).subtract(BigInteger.ONE), rankOf.repeatedPermutation(2, filled(64, 1)));
    }

    @Test
    @DisplayName("rank: repeatedPermutation, a 1 followed by 24 zeros in base 10 is 10^24")
    void repeatedPermutationPowerOfTen() {
        Integer[] digits = filled(25, 0);
        digits[0] = 1;
        assertEquals(BigInteger.TEN.pow(24), rankOf.repeatedPermutation(10, digits));
    }

    @Test
    @DisplayName("rank: repeatedPermutation, a 1 followed by 69 zeros in base 2 is 2^69 (not 0)")
    void repeatedPermutationLongBinary() {
        Integer[] digits = filled(70, 0);
        digits[0] = 1;
        assertEquals(BigInteger.TWO.pow(69), rankOf.repeatedPermutation(2, digits));
    }

    @Test
    @DisplayName("rank: repeatedPermutation matches the BigInteger reference for many bases and lengths")
    void repeatedPermutationMatchesReference() {
        for (int n : new int[]{2, 3, 7, 10, 36, 1000, 100_000}) {
            for (int length : new int[]{1, 5, 12, 25, 64, 130}) {
                Integer[] digits = new Integer[length];
                for (int i = 0; i < length; i++) {
                    digits[i] = (i * 31 + n) % n;          // deterministic, covers the whole digit range
                }
                digits[0] = n - 1;                          // largest leading digit
                assertEquals(baseNValue(n, digits), rankOf.repeatedPermutation(n, digits), "n=" + n + " length=" + length);
            }
        }
    }

    @Test
    @DisplayName("rank/unrank: uniqueCombination handles the first and last combination of C(100,50)")
    void uniqueCombinationBeyondLong() {
        int n = 100, r = 50;
        BigInteger count = calculator.nCr(n, r);
        assertTrue(count.bitLength() > 63);

        int[] first = new int[r];
        int[] last = new int[r];
        for (int i = 0; i < r; i++) {
            first[i] = i;
            last[i] = n - r + i;
        }
        assertEquals(BigInteger.ZERO, rankOf.uniqueCombination(n, first));
        assertEquals(count.subtract(BigInteger.ONE), rankOf.uniqueCombination(n, last));
        assertArrayEquals(first, unrankOf.uniqueCombination(BigInteger.ZERO, n, r));
        assertArrayEquals(last, unrankOf.uniqueCombination(count.subtract(BigInteger.ONE), n, r));

        // and a rank in the middle round-trips
        BigInteger middle = count.divide(BigInteger.valueOf(3));
        assertEquals(middle, rankOf.uniqueCombination(n, unrankOf.uniqueCombination(middle, n, r)));
    }
}
