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
 * The Combinadic representation is a unique method to represent non-negative integers as combinations,
 * enabling direct computation of the nᵗʰ combination in lexicographical order without enumeration of
 * all preceding combinations. This class includes methods to convert a given combination to its Combinadic
 * representation, to convert a Combinadic representation back to a combination, and to convert between
 * decimal and Combinadic formats.
 * </p>
 *
 * @author Deepesh Patel
 */
public final class CombinadicAlgorithms {

    private final Calculator calculator;

    /**
     * Constructs an instance of {@link CombinadicAlgorithms} with the specified calculator.
     *
     * @param calculator the calculator used for combinatorial calculations
     * @return a new instance of CombinadicAlgorithms initialized with the provided calculator
     */
    public CombinadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Computes the rank of a given combination in the Combinadic representation.
     * <p>
     * The rank is calculated by converting the provided combination into its Combinadic form,
     * then into a decimal value, and finally computing the rank as:
     * <pre>
     *     rank = Cₙ,ᵣ − decimalValue − 1
     * </pre>
     * where Cₙ,ᵣ is the total number of combinations of the given size.
     * </p>
     *
     * @param n the total number of elements (nₙ)
     * @param mthCombination the combination (array of indices) whose rank is to be computed
     * @return the rank of the given combination in the Combinadic representation
     */
    public BigInteger rank(int n, int[] mthCombination) {
        int[] combinadic = combinationToCombinadic(n, mthCombination);
        BigInteger x = combinadicToDecimal(combinadic);
        BigInteger nCr = calculator.nCr(n, mthCombination.length);
        return nCr.subtract(x).subtract(BigInteger.ONE);
    }

    /**
     * Computes the combination corresponding to a given rank in the Combinadic representation.
     * <p>
     * This method computes:
     * <pre>
     *     x = Cₙ,ᵣ − rank − 1
     * </pre>
     * then converts x to a Combinadic representation of size rᵣ and transforms it back into a combination.
     * </p>
     *
     * @param rank the rank of the combination
     * @param nCr the total number of combinations (Cₙ,ᵣ)
     * @param n the total number of elements (nₙ)
     * @param r the size of the combination (rᵣ)
     * @return the combination corresponding to the given rank
     */
    public int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
        BigInteger x = nCr.subtract(rank).subtract(BigInteger.ONE);
        int[] a = decimalToCombinadic(x, r);
        return combinadicToCombination(a, n);
    }

    /**
     * Converts a combination to its Combinadic representation.
     * <p>
     * This is achieved by mapping each element of the combination as:
     * <pre>
     *     combinadic[i] = nₙ − 1 − mthCombination[i]
     * </pre>
     * </p>
     *
     * @param n the total number of elements (nₙ)
     * @param mthCombination the combination (array of indices) to be converted
     * @return an array representing the Combinadic values of the combination
     */
    public static int[] combinationToCombinadic(int n, int[] mthCombination) {
        int[] combinadic = new int[mthCombination.length];
        for (int i = 0; i < combinadic.length; i++) {
            combinadic[i] = n - 1 - mthCombination[i];
        }
        return combinadic;
    }

    /**
     * Converts a Combinadic representation to its corresponding combination.
     * <p>
     * This method reverses the transformation done by {@link #combinationToCombinadic(int, int[])}.
     * </p>
     *
     * @param combinadic the Combinadic representation to convert
     * @param n the total number of elements (nₙ)
     * @return an array representing the original combination
     */
    public static int[] combinadicToCombination(int[] combinadic, int n) {
        int[] a = Arrays.copyOf(combinadic, combinadic.length);
        for (int i = 0; i < a.length; i++) {
            a[i] = n - 1 - a[i];
        }
        return a;
    }

    /**
     * Converts a Combinadic representation to its decimal equivalent.
     * <p>
     * The decimal value is computed as the sum of binomial coefficients for each position in the Combinadic array.
     * </p>
     *
     * @param combinadic the Combinadic representation to convert
     * @return the decimal value of the Combinadic representation
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
     * Converts a decimal value to its Combinadic representation.
     * <p>
     * The conversion is performed by repeatedly finding the largest integer n such that
     * the binomial coefficient C(n, rᵣ) is less than or equal to the remaining value.
     * </p>
     *
     * @param value the decimal value to convert
     * @param degree the size of the combination (rᵣ)
     * @return an array representing the Combinadic values of the decimal number
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
     * Computes the next Combinadic value in the sequence.
     * <p>
     * This method returns a new Combinadic number by incrementing the current Combinadic representation.
     * The algorithm increases the rightmost element and resets it if necessary, propagating a carry as required.
     * </p>
     *
     * @param combinadic a list representing the current Combinadic number
     * @return an array representing the next Combinadic number
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
}