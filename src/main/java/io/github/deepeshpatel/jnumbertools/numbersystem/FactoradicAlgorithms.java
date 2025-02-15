/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides various algorithms for working with the Factoradic numeral system.
 * <p>
 * The Factoradic numeral system represents a nonnegative integer as a sum of factorials weighted by digits.
 * This class includes methods to convert between decimal and Factoradic representations, as well as utility
 * functions for ranking and unranking permutations. These algorithms are useful for directly computing
 * the nth permutation of a set.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class FactoradicAlgorithms {

    private FactoradicAlgorithms() {
        // Prevent instantiation of utility class.
    }

    /**
     * Converts a Factoradic representation to its decimal equivalent.
     * <p>
     * This method computes the decimal value by summing each digit multiplied by the factorial of its position.
     * <em>Note:</em> A new {@link Calculator} instance is created internally; consider refactoring to remove the static dependency.
     * </p>
     *
     * @param factoradic an array representing the Factoradic digits.
     * @return the decimal value corresponding to the given Factoradic representation.
     */
    public static int factoradicToInt(int[] factoradic) {
        // TODO: remove calculator dependency and make this method non-static.
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
        List<Integer> factoradic = new ArrayList<>();
        long d = 1;
        while (!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic.add(divideAndRemainder[1].intValue());
            k = divideAndRemainder[0];
            d++;
        }
        return factoradic.stream().mapToInt(Integer::intValue).toArray();
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
        int[] factoradic = new int[sizeOfFactoradic];
        if (k.equals(BigInteger.ZERO)) {
            return factoradic;
        }
        long d = 1;
        int i = sizeOfFactoradic - 1;
        while (!k.equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = k.divideAndRemainder(BigInteger.valueOf(d));
            factoradic[i--] = divideAndRemainder[1].intValue();
            k = divideAndRemainder[0];
            d++;
        }
        return factoradic;
    }

    /**
     * Converts a Factoradic representation to its corresponding permutation.
     * <p>
     * This method maps the Factoradic digits to positions to produce the permutation.
     * </p>
     *
     * @param factoradic the Factoradic representation to be converted.
     * @return an array representing the permutation corresponding to the given Factoradic representation.
     */
    public static int[] factoradicToMthPermutation(int[] factoradic) {
        int[] output = IntStream.range(0, factoradic.length).toArray();
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
     * Converts a given rank to the corresponding permutation of a specified size.
     * <p>
     * This method first converts the rank into its Factoradic representation with a known size,
     * and then maps that Factoradic representation to the permutation.
     * </p>
     *
     * @param rank the rank of the permutation.
     * @param size the size of the permutation.
     * @return an array representing the permutation corresponding to the given rank.
     */
    public static int[] unRank(BigInteger rank, int size) {
        return factoradicToMthPermutation(intToFactoradicKnownSize(rank, size));
    }
}
