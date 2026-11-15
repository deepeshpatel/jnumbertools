/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.AllocationMeter;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves that the in-place digit extraction {@link FactoradicAlgorithms} uses for ranks beyond a long gives exactly
 * the digits of the straightforward {@code BigInteger} algorithm, and allocates far less.
 * (Correctness of the whole unRank, on both paths, is also covered by {@code FactoradicLongPathTest}.)
 */
class FactoradicUnrankOptimisationTest {

    /** The algorithm used before the optimisation: one BigInteger division per digit. */
    private static int[] referenceDigits(BigInteger k, int size) {
        int[] digits = new int[size];
        if (k.signum() == 0) {
            return digits;
        }
        long d = 1;
        int i = size - 1;
        while (k.signum() != 0) {
            if (i < 0) {
                throw new IllegalArgumentException("value is too large");
            }
            BigInteger[] qr = k.divideAndRemainder(BigInteger.valueOf(d));
            digits[i--] = qr[1].intValue();
            k = qr[0];
            d++;
        }
        return digits;
    }

    private static BigInteger factorial(int n) {
        BigInteger f = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            f = f.multiply(BigInteger.valueOf(i));
        }
        return f;
    }

    @Test
    void digitsMatchReferenceForEverySizeUpTo130() {
        Random random = new Random(11);
        for (int n = 0; n <= 130; n++) {
            BigInteger total = factorial(n);
            List<BigInteger> values = new ArrayList<>(List.of(BigInteger.ZERO, total.subtract(BigInteger.ONE).max(BigInteger.ZERO)));
            for (int t = 0; t < 100; t++) {
                values.add(new BigInteger(total.bitLength() + 1, random).mod(total));
            }
            for (BigInteger value : values) {
                assertArrayEquals(referenceDigits(value, n), FactoradicAlgorithms.intToFactoradicKnownSize(value, n),
                        "n=" + n + " value=" + value);
            }
        }
    }

    @Test
    void digitsMatchReferenceAroundTheLongBoundary() {
        for (int shift : new int[]{62, 63, 64}) {
            BigInteger boundary = BigInteger.ONE.shiftLeft(shift);
            for (int n : new int[]{21, 22, 25, 60}) {
                for (int delta = -2; delta <= 2; delta++) {
                    BigInteger value = boundary.add(BigInteger.valueOf(delta));
                    assertArrayEquals(referenceDigits(value, n), FactoradicAlgorithms.intToFactoradicKnownSize(value, n),
                            "n=" + n + " value=" + value);
                }
            }
        }
    }

    @Test
    void valueTooLargeForTheSizeIsRejected() {
        for (int n : new int[]{0, 1, 5, 20, 21, 60, 100}) {
            BigInteger total = factorial(n);
            assertThrows(IllegalArgumentException.class, () -> FactoradicAlgorithms.intToFactoradicKnownSize(total, n), "n=" + n);
            assertThrows(IllegalArgumentException.class, () -> FactoradicAlgorithms.intToFactoradicKnownSize(total.multiply(BigInteger.TEN), n), "n=" + n);
            if (n > 0) {
                assertDoesNotThrow(() -> FactoradicAlgorithms.intToFactoradicKnownSize(total.subtract(BigInteger.ONE), n));
            }
        }
    }

    @Test
    void negativeValueAndNegativeSizeAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> FactoradicAlgorithms.intToFactoradicKnownSize(BigInteger.valueOf(-1), 3));
        assertThrows(IllegalArgumentException.class, () -> FactoradicAlgorithms.intToFactoradicKnownSize(BigInteger.ONE, -1));
    }

    @Test
    void allocatesFarLessThanTheBigIntegerAlgorithm() {
        Assumptions.assumeTrue(AllocationMeter.supported(), "per-thread allocation is not reported by this JVM");
        Random random = new Random(5);
        for (int n : new int[]{21, 30, 52, 200}) {
            BigInteger total = factorial(n);
            List<BigInteger> ranks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                // ranks beyond a long, which is where the optimised path applies
                BigInteger rank;
                do {
                    rank = new BigInteger(total.bitLength() + 8, random).mod(total);
                } while (rank.bitLength() < 64);
                ranks.add(rank);
            }
            int[] sink = new int[1];
            double reference = AllocationMeter.bytesPerCall(() -> {
                for (BigInteger rank : ranks) sink[0] += referenceDigits(rank, n)[0];
            }, 200, 100);
            double optimised = AllocationMeter.bytesPerCall(() -> {
                for (BigInteger rank : ranks) sink[0] += FactoradicAlgorithms.intToFactoradicKnownSize(rank, n)[0];
            }, 200, 100);
            // The measured reduction is about 10x or more; asking for 3x leaves a wide margin
            assertTrue(optimised * 3 < reference,
                    "n=" + n + ": optimised " + optimised + " B vs reference " + reference + " B per 100 ranks");
        }
    }
}
