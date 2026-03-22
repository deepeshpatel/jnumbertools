/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.external;

import java.math.BigInteger;

/**
 * Provides mathematical functions for combinatorial counting, permutations,
 * derangements, multinomial coefficients, multiset combinations, and related quantities.
 * <p>
 * All methods return results as {@link BigInteger} to support arbitrarily large values
 * without overflow. Implementations are expected to be thread-safe and may use
 * internal caching/memoization for performance.
 * </p>
 *
 * @author Deepesh Patel
 * @since 3.0
 */
public interface Calculator {

    /**
     * Calculates the number of combinations with repetition (ⁿ⁺ʳ⁻¹Cᵣ / stars and bars theorem).
     * Equivalent to choosing {@code r} items from {@code n} types where order does not matter
     * and repetition is allowed.
     *
     * @param n number of distinct types (≥ 0)
     * @param r number of items to choose (≥ 0)
     * @return the number of multi-combinations as a {@link BigInteger}
     */
    BigInteger nCrRepetitive(int n, int r);

    /**
     * Computes the binomial coefficient ⁿCᵣ ("n choose r").
     * Returns 0 if {@code r > n} or {@code r < 0}.
     *
     * @param n total number of items (≥ 0)
     * @param r number of items to choose (≥ 0)
     * @return ⁿCᵣ as a {@link BigInteger}
     */
    BigInteger nCr(int n, int r);

    /**
     * Finds the smallest integer {@code n ≥ r} such that ⁿCᵣ > {@code max}.
     *
     * @param r   fixed number of items to choose (≥ 0)
     * @param max threshold value (exclusive)
     * @return the smallest n where binomial(n, r) > max
     */
    int nCrUpperBound(int r, BigInteger max);

    /**
     * Computes the number of k-permutations ⁿPₖ (ordered selections without replacement).
     * Returns 0 if {@code r > n} or {@code r < 0}.
     *
     * @param n total number of distinct items (≥ 0)
     * @param r number of positions to fill (≥ 0)
     * @return ⁿPᵣ as a {@link BigInteger}
     */
    BigInteger nPr(int n, int r);

    /**
     * Finds the smallest integer {@code n} such that {@code n!} > {@code value}.
     *
     * @param value threshold value (must be non-negative)
     * @return smallest n where n! > value
     */
    int factorialUpperBound(BigInteger value);

    /**
     * Computes n! (factorial of n).
     *
     * @param n non-negative integer
     * @return n! as a {@link BigInteger}
     * @throws IllegalArgumentException if n < 0
     */
    BigInteger factorial(int n);

    /**
     * Computes !n (sub-factorial / number of derangements of n items).
     * Number of permutations of n elements with no element appearing in its original position.
     *
     * @param n non-negative integer
     * @return !n as a {@link BigInteger}
     * @throws IllegalArgumentException if n < 0
     */
    BigInteger subFactorial(int n);

    /**
     * Computes {@code base<sup>exponent</sup>} using efficient binary exponentiation.
     *
     * @param base     base number
     * @param exponent non-negative exponent
     * @return base<sup>exponent</sup> as a {@link BigInteger}
     * @throws IllegalArgumentException if exponent < 0
     */
    default BigInteger power(long base, long exponent) {
        return power(BigInteger.valueOf(base), BigInteger.valueOf(exponent));
    }

    /**
     * Computes {@code base<sup>exponent</sup>} using efficient binary exponentiation.
     *
     * @param base     base number
     * @param exponent non-negative exponent
     * @return base<sup>exponent</sup> as a {@link BigInteger}
     * @throws IllegalArgumentException if exponent < 0
     */
    BigInteger power(BigInteger base, BigInteger exponent);

    /**
     * Computes the partial sum of binomial coefficients:
     * ∑_{r=from}^{to} ⁿCᵣ
     * <p>
     * When from = 0 and to = n, this method may return 2ⁿ more efficiently.
     *
     * @param from lower bound (inclusive, ≥ 0)
     * @param to   upper bound (inclusive, ≤ n)
     * @param n    total number of elements
     * @return sum of binomial coefficients in the given range as {@link BigInteger}
     */
    BigInteger totalSubsetsInRange(int from, int to, int n);

    /**
     * Computes the multinomial coefficient:
     * n! / (k₁! × k₂! × … × kₘ!)   where n = k₁ + k₂ + … + kₘ
     *
     * @param counts non-negative integers representing group sizes/frequencies
     * @return the multinomial coefficient as a {@link BigInteger}
     * @throws IllegalArgumentException if any count is negative
     */
    BigInteger multinomial(int... counts);

    /**
     * Computes the Rencontres number D(n,k) — number of permutations of n elements
     * with exactly k fixed points.
     * Formula: D(n,k) = ⁿCₖ × !(n−k)
     *
     * @param n size of the set (≥ 0)
     * @param k number of fixed points (0 ≤ k ≤ n)
     * @return D(n,k) as a {@link BigInteger}, or ZERO if out of range
     */
    BigInteger rencontresNumber(int n, int k);

    /**
     * Clears any internal caches (if the implementation uses memoization).
     * After calling this method, subsequent calculations will recompute values.
     * <p>
     * Useful mainly for testing or memory-constrained environments.
     * </p>
     */
    void clearCaches();

    /**
     * Computes the number of ways to choose exactly s items from a multiset
     * for every s from 0 to floor(total/2), using bottom-up dynamic programming.
     * Due to symmetry, ways(total - s) = ways(s).
     *
     * @param frequencies array of non-negative frequencies/multiplicities
     * @return array dp where dp[s] = number of ways to choose exactly s items (s ≤ total/2)
     */
     int[] multisetCombinationsCountAll(int... frequencies);

    /**
     * Computes the number of ways to choose exactly k items from a multiset defined by counts.
     * @param k      number of items to choose (k ≥ 0)
     * @param counts non-negative multiplicities of each distinct type
     * @return number of ways to choose exactly k items as a BigInteger
     * @throws IllegalArgumentException if k < 0 or any count < 0
     */
    BigInteger multisetCombinationsCount(int k, int... counts);

    /**
     * Calculates the number of ways to select exactly k items from a multiset, starting from a given index.
     * The multiset is defined by multiplicities, considering item types from the specified index onward.
     * @param k the number of items to select (k ≥ 0)
     * @param index the starting index of item types to consider (0 ≤ index ≤ counts.length)
     * @param counts an array of non-negative integers representing the multiplicities of item types
     * @return the number of ways to select k items as a BigInteger
     * @throws IllegalArgumentException if k is negative, index is out of range, or any count is negative
     */
    BigInteger multisetCombinationsCountStartingFromIndex(int k, int index, int... counts);
}
