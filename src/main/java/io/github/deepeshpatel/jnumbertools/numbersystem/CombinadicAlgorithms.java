/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Provides algorithms for converting between Combinadic and decimal representations,
 * and for computing ranks and un-ranks of combinations using the Combinadic number system.
 * <p>
 * The combinadic representation is a unique method to represent nonnegative integers as combinations,
 * enabling direct computation of the nth combination in lexicographical order without enumeration of
 * all preceding combinations. This class includes methods to convert a given combination to its combinadic
 * representation, to convert a combinadic representation back to a combination, and to convert between
 * decimal and combinadic formats.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class CombinadicAlgorithms {

    private final Calculator calculator;

    /**
     * Constructs an instance of {@link CombinadicAlgorithms} with the specified calculator.
     *
     * @param calculator the calculator used for combinatorial calculations.
     */
    public CombinadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Computes the rank of a given combination in the combinadic representation.
     * <p>
     * The rank is calculated by converting the provided combination into its combinadic form,
     * then into a decimal value, and finally computing the rank as:
     * <pre>
     *     rank = nCr - decimalValue - 1
     * </pre>
     * where nCr is the total number of combinations of the given size.
     * </p>
     *
     * @param n              the total number of elements.
     * @param mthCombination the combination whose rank is to be computed.
     * @return the rank of the given combination in the combinadic representation.
     */
    public BigInteger rank(int n, int[] mthCombination) {
        int[] combinadic = combinationToCombinadic(n, mthCombination);
        BigInteger x = combinadicToDecimal(combinadic);
        BigInteger nCr = calculator.nCr(n, mthCombination.length);
        return nCr.subtract(x).subtract(BigInteger.ONE);
    }

    /**
     * Computes the combination corresponding to a given rank in the combinadic representation.
     * <p>
     * This method computes:
     * <pre>
     *     x = nCr - rank - 1
     * </pre>
     * then converts x to a combinadic representation of degree r and transforms it back into a combination.
     * </p>
     *
     * @param rank the rank of the combination.
     * @param nCr  the total number of combinations.
     * @param n    the total number of elements.
     * @param r    the size of the combination.
     * @return the combination corresponding to the given rank.
     */
    public int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
        BigInteger x = nCr.subtract(rank).subtract(BigInteger.ONE);
        int[] a = decimalToCombinadic(x, r);
        return combinadicToCombination(a, n);
    }

    /**
     * Converts a combination to its combinadic representation.
     * <p>
     * This is achieved by mapping each element of the combination as:
     * <pre>
     *     combinadic[i] = n - 1 - mthCombination[i]
     * </pre>
     * </p>
     *
     * @param n              the total number of elements.
     * @param mthCombination the combination to be converted.
     * @return an array representing the combinadic values of the combination.
     */
    public static int[] combinationToCombinadic(int n, int[] mthCombination) {
        int[] combinadic = new int[mthCombination.length];
        for (int i = 0; i < combinadic.length; i++) {
            combinadic[i] = n - 1 - mthCombination[i];
        }
        return combinadic;
    }

    /**
     * Converts a combinadic representation to its corresponding combination.
     * <p>
     * This method reverses the transformation done by {@link #combinationToCombinadic(int, int[])}.
     * </p>
     *
     * @param combinadic the combinadic representation to convert.
     * @param n          the total number of elements.
     * @return an array representing the original combination.
     */
    public static int[] combinadicToCombination(int[] combinadic, int n) {
        int[] a = Arrays.copyOf(combinadic, combinadic.length);
        for (int i = 0; i < a.length; i++) {
            a[i] = n - 1 - a[i];
        }
        return a;
    }

    /**
     * Converts a combinadic representation to its decimal equivalent.
     * <p>
     * The decimal value is computed as the sum of binomial coefficients for each position in the combinadic array.
     * </p>
     *
     * @param combinadic the combinadic representation to convert.
     * @return the decimal value of the combinadic representation.
     */
    public BigInteger combinadicToDecimal(int[] combinadic) {
        BigInteger decimalValue = BigInteger.ZERO;
        int r = combinadic.length;
        for (int j : combinadic) {
            decimalValue = decimalValue.add(calculator.nCr(j, r));
            r--;
        }
        return decimalValue;
    }

    /**
     * Converts a decimal value to its combinadic representation.
     * <p>
     * The conversion is performed by repeatedly finding the largest integer n such that
     * the binomial coefficient C(n, r) is less than or equal to the remaining value.
     * </p>
     *
     * @param value  the decimal value to convert.
     * @param degree the degree (length) of the combinadic representation.
     * @return an array representing the combinadic values of the decimal number.
     */
    public int[] decimalToCombinadic(BigInteger value, int degree) {
        int[] combinadic = new int[degree];
        int r = degree;
        BigInteger max = value;
        for (int i = 0; r > 0; i++, r--) {
            int n = calculator.nCrUpperBound(r, max);
            combinadic[i] = n - 1;
            max = max.subtract(calculator.nCr(n - 1, r));
        }
        return combinadic;
    }

    /**
     * Computes the next combinadic value in the sequence.
     * <p>
     * This method returns a new combinadic number by incrementing the current combinadic representation.
     * The algorithm increases the rightmost element and resets it if necessary, propagating a carry as required.
     * </p>
     *
     * @param combinadic a list representing the current combinadic number.
     * @return an array representing the next combinadic number.
     */
    public static int[] nextCombinadic(List<Integer> combinadic) {
        int[] result = combinadic.stream().mapToInt(Integer::intValue).toArray();
        int k = 0;
        for (int i = result.length - 1; i > 0; i--) {
            result[i] = result[i] + 1;
            if (result[i] < result[i - 1]) {
                return result;
            }
            result[i] = k++;
        }
        result[0]++;
        return result;
    }

    // TODO: Add algorithm for nextMthCombinadic(int[] combinadic) without converting to decimal.
}
