/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * FactoradicAlgorithms.unRank has a primitive-long path (ranks below 2^63) and a BigInteger path. Both must agree with
 * an independent BigInteger reference, including around the 20!/21! boundary where the path changes, and both must
 * reject a rank that needs more digits than n.
 */
class FactoradicLongPathTest {

    @Test
    void everyRankForSmallSizes() {
        for (int n = 0; n <= 6; n++) {
            BigInteger total = factorial(n);
            for (BigInteger rank = BigInteger.ZERO; rank.compareTo(total) < 0; rank = rank.add(BigInteger.ONE)) {
                assertArrayEquals(reference(rank, n), FactoradicAlgorithms.unRank(rank, n), "n=" + n + " rank=" + rank);
            }
        }
    }

    @Test
    void sampledRanksAroundTheLongBoundary() {
        Random random = new Random(21);
        for (int n : new int[]{10, 19, 20, 21, 22, 30}) {
            BigInteger total = factorial(n);
            check(n, BigInteger.ZERO);
            check(n, total.subtract(BigInteger.ONE));
            for (int i = 0; i < 300; i++) check(n, new BigInteger(total.bitLength() + 8, random).mod(total));
        }
    }

    @Test
    void ranksAtTheExactLimitOfALong() {
        // 2^63 - 1 is the largest rank the long path accepts; 2^63 must go through the BigInteger path unchanged.
        BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
        check(21, maxLong);
        check(21, maxLong.add(BigInteger.ONE));
        check(25, maxLong.add(BigInteger.TWO));
    }

    @Test
    void rankNeedingMoreDigitsThanNIsRejectedOnBothPaths() {
        for (int n : new int[]{0, 1, 3, 12, 20, 21, 25}) {
            BigInteger total = factorial(n);
            for (BigInteger tooBig : new BigInteger[]{total, total.add(BigInteger.valueOf(7)), total.multiply(BigInteger.TEN)}) {
                assertThrows(IllegalArgumentException.class, () -> FactoradicAlgorithms.unRank(tooBig, n),
                        "n=" + n + " rank=" + tooBig);
            }
        }
    }

    private static void check(int n, BigInteger rank) {
        assertArrayEquals(reference(rank, n), FactoradicAlgorithms.unRank(rank, n), "n=" + n + " rank=" + rank + " got "
                + Arrays.toString(FactoradicAlgorithms.unRank(rank, n)));
    }

    private static BigInteger factorial(int n) {
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= n; i++) f = f.multiply(BigInteger.valueOf(i));
        return f;
    }

    /** Plain BigInteger factorial-number-system unranking, written independently of the production code. */
    private static int[] reference(BigInteger rank, int n) {
        int[] digits = new int[n];
        BigInteger k = rank;
        for (int i = n - 1, d = 1; i >= 0; i--, d++) {
            BigInteger[] qr = k.divideAndRemainder(BigInteger.valueOf(d));
            digits[i] = qr[1].intValue();
            k = qr[0];
        }
        if (k.signum() != 0) throw new IllegalArgumentException("too large");
        int[] output = IntStream.range(0, n).toArray();
        for (int i = 0; i < n; i++) {
            int index = digits[i] + i;
            int value = output[index];
            System.arraycopy(output, i, output, i + 1, index - i);
            output[i] = value;
        }
        return output;
    }
}
