
/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Provides various algorithms for working with the Factoradic numeral system.
 * <p>
 * The Factoradic numeral system represents a non-negative integer as a sum of factorials weighted by digits.
 * This class includes methods to convert between decimal and Factoradic representations, as well as utility
 * functions for ranking and unranking permutations. These algorithms are useful for directly computing
 * the nᵗʰ permutation of a set.
 * </p>
 *
 * <p>
 * <strong>Implementation class: intended to be used through {@link io.github.deepeshpatel.jnumbertools.entrypoint.Unrank} and the builders.</strong>
 * Argument validation and its error signalling belong to those; this class assumes valid input. The few
 * checks that remain here are intrinsic to a conversion (for example a value that cannot be represented with the
 * requested number of digits) and cost a single comparison.
 * </p>
 * @author Deepesh Patel
 */
public final class FactoradicAlgorithms {

    private FactoradicAlgorithms() {
        // Prevent instantiation of utility class.
    }

    /**
     * Converts a Factoradic representation to its decimal equivalent.
     *
     * @param factoradic an array representing the Factoradic digits.
     * @return the decimal value corresponding to the given Factoradic representation.
     */
    public static int factoradicToInt(int[] factoradic) {
        Calculator calculator = new Calculator();
        int value = 0;
        for (int i = 0; i < factoradic.length; i++) {
            value += factoradic[i] * calculator.factorial(i).intValue();
        }
        return value;
    }

    /**
     * Converts a positive long value to its Factoradic representation.
     *
     * @param k the positive long value to be converted.
     * @return an array representing the Factoradic digits of the given number.
     */
    public static int[] intToFactoradic(long k) {
        return intToFactoradic(BigInteger.valueOf(k));
    }

    /**
     * Converts a positive integer to its Factoradic representation.
     * <p>
     * The conversion is performed by repeatedly dividing the number by increasing integers
     * and recording the remainders as the Factoradic digits.
     * </p>
     *
     * @param k the positive integer (as a {@link BigInteger}) to be converted.
     * @return an array representing the Factoradic digits of the given integer.
     */
    public static int[] intToFactoradic(BigInteger k) {
        if (k.equals(BigInteger.ZERO)) {
            return new int[]{0};
        }

        // The maximum number of factoradic digits for a positive BigInteger
        // is bounded by its bit length plus one.
        int[] digits = new int[k.bitLength() + 1];
        int size = 0;
        long divisor = 1;

        while (!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder =
                    k.divideAndRemainder(BigInteger.valueOf(divisor));

            digits[size++] = divideAndRemainder[1].intValue();
            k = divideAndRemainder[0];
            divisor++;
        }

        return Arrays.copyOf(digits, size);
    }

    /**
     * Converts a positive integer to its Factoradic representation with a specified size.
     * <p>
     * The resulting array will have a length of {@code sizeOfFactoradic}. If the number is too small,
     * the leading digits will be zeros.
     * </p>
     *
     * @param k the positive integer (as a {@link BigInteger}) to be converted.
     * @param sizeOfFactoradic the desired size of the Factoradic representation array.
     * @return an array representing the Factoradic digits of the given integer.
     */
    public static int[] intToFactoradicKnownSize(BigInteger k, int sizeOfFactoradic) {
        if (sizeOfFactoradic < 0) {
            throw new IllegalArgumentException("size must be ≥ 0: " + sizeOfFactoradic);
        }
        if (k.signum() < 0) {
            throw new IllegalArgumentException("value must be ≥ 0: " + k);
        }
        int[] factoradic = new int[sizeOfFactoradic];
        if (k.equals(BigInteger.ZERO)) {
            return factoradic;
        }
        if (k.bitLength() < 64) {
            // Same digits in primitive arithmetic (k fits in a long, and so does every quotient).
            long value = k.longValue();
            long divisor = 1;
            int index = sizeOfFactoradic - 1;
            while (value != 0) {
                if (index < 0) {
                    throw new IllegalArgumentException(
                            "value is too large to be represented with " + sizeOfFactoradic + " factoradic digits");
                }
                factoradic[index--] = (int) (value % divisor);
                value /= divisor;
                divisor++;
            }
            return factoradic;
        }
        fillDigitsMagnitude(k, factoradic);
        return factoradic;
    }

    /**
     * Digit extraction for values that do not fit in a long. The value is copied once into a mutable array of
     * 32-bit words and divided in place, so the whole conversion allocates one array instead of several
     * {@code BigInteger}s per digit. Consecutive divisors d, d+1, ... are multiplied together while the product
     * stays below 2<sup>31</sup>, so each pass over the words yields several digits at once: dividing by the
     * product leaves a remainder R whose mixed-radix digits (R mod d, then R / d mod (d+1), ...) are exactly
     * the digits that dividing by d, d+1, ... one at a time would give.
     */
    private static void fillDigitsMagnitude(BigInteger k, int[] factoradic) {
        byte[] bytes = k.toByteArray();
        int words = (bytes.length + 3) >>> 2;
        int[] mag = new int[words];
        for (int b = 0; b < bytes.length; b++) {
            int fromEnd = bytes.length - 1 - b;
            mag[words - 1 - (fromEnd >>> 2)] |= (bytes[b] & 0xFF) << ((fromEnd & 3) << 3);
        }
        int first = 0;
        while (first < words && mag[first] == 0) first++;

        int size = factoradic.length;
        int digitsDone = 0;
        while (digitsDone < size && first < words) {
            long product = digitsDone + 1L;
            int last = digitsDone + 1;
            while (last < size && product * (last + 1L) < (1L << 31)) {
                product *= ++last;
            }
            long remainder = 0;
            for (int w = first; w < words; w++) {
                long current = (remainder << 32) | (mag[w] & 0xFFFFFFFFL);
                mag[w] = (int) (current / product);
                remainder = current % product;
            }
            for (int d = digitsDone + 1; d <= last; d++) {
                factoradic[size - d] = (int) (remainder % d);
                remainder /= d;
            }
            digitsDone = last;
            while (first < words && mag[first] == 0) first++;
        }
        if (first < words) {
            throw new IllegalArgumentException(
                    "value is too large to be represented with " + size + " factoradic digits");
        }
    }

    /**
     * Converts a Factoradic representation to its corresponding permutation.
     *
     * @param factoradic the Factoradic representation to be converted.
     * @return an array representing the permutation corresponding to the given Factoradic representation.
     */
    public static int[] factoradicToMthPermutation(int[] factoradic) {
        int[] output = new int[factoradic.length];
        for (int i = 0; i < output.length; i++) output[i] = i;
        for (int i = 0; i < output.length; i++) {
            int index = factoradic[i] + i;
            if (index != i) {
                int temp = output[index];
                if (index - i >= 0) {
                    System.arraycopy(output, i, output, i + 1, index - i);
                }
                output[i] = temp;
            }
        }
        return output;
    }

    /**
     * Converts a rank to its corresponding permutation of specified size.
     *
     * @param rank the 0-based lexicographical rank of the desired permutation
     * @param size the size of the permutation (number of elements)
     * @return an array of indices representing the permutation at the given rank
     */
    public static int[] unRank(BigInteger rank, int size) {
        return factoradicToMthPermutation(intToFactoradicKnownSize(rank, size));
    }
}
