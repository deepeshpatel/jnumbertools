/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
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
 * <p>
 * <strong>Implementation class: intended to be used through {@link io.github.deepeshpatel.jnumbertools.base.RankOf} and
 * {@link io.github.deepeshpatel.jnumbertools.base.UnrankOf} (and the builders).</strong> Those validate their input (null, negative sizes, a
 * combination that is not strictly ascending within [0, n), ranks outside [0, nCr), ...) as described in
 * {@code ValidationRules.md}. The methods here assume valid input and do not check it.
 * </p>
 * @author Deepesh Patel
 */
public final class CombinadicAlgorithms {

    /**
     * Smallest combination size for which {@link #unRank} uses the monotone walk instead of the
     * per-position binary search. Below this the binary search is at least as fast.
     */
    private static final int WALK_MIN_R = 256;

    /**
     * The walk visits up to n candidates, so it is only chosen while n is at most this many times r.
     */
    private static final int WALK_MAX_N_PER_R = 1024;

    private final Calculator calculator;

    /**
     * Constructs an instance of {@link CombinadicAlgorithms} with the specified calculator.
     *
     * @param calculator the calculator used for combinatorial calculations
     */
    public CombinadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Computes the lexicographical rank of a given combination.
     * <p>
     * The rank is 0-based, meaning the first combination in lexicographical order
     * has rank 0. Uses combinadic representation for efficient computation.
     * </p>
     * <p>
     * Example: For n=5, r=3, the combination [0,1,2] has rank 0,
     * [0,1,3] has rank 1, [2,3,4] has rank 9, etc.
     * </p>
     *
     * @param n the total number of elements (n ≥ 0)
     * @param mthCombination the combination (sorted array of indices) whose rank is to be computed
     * @return the 0-based lexicographical rank of the combination
     * @see io.github.deepeshpatel.jnumbertools.base.RankOf

     */
    public BigInteger rank(int n, int[] mthCombination) {
        int r = mthCombination.length;
        BigInteger x = BigInteger.ZERO;

        for (int i = 0; i < r; i++) {
            x = x.add(calculator.nCr(n - 1 - mthCombination[i], r - i));
        }

        return calculator.nCr(n, r).subtract(x).subtract(BigInteger.ONE);
    }

    /**
     * Un-ranks the nth combination in lexicographical order using the
     * Combinatorial Number System (Combinadic) with Binary Search.
     *
     * Small and medium r use a binary search per position. Large r (see {@code WALK_MIN_R}) uses a single
     * monotone walk that updates one binomial coefficient incrementally instead of evaluating a new
     * C(mid, k) for every probe; both give identical results.
     *
     * @param rank the rank of the combination
     * @param total the total number of combinations (Cₙ,ᵣ)
     * @param n the total number of elements (nₙ)
     * @param r the size of the combination (rᵣ)
     * @return an int array of size r containing the combination in ascending order
     */
    public int[] unRank(BigInteger rank, BigInteger total, int n, int r) {
        if (r >= WALK_MIN_R && n <= (long) WALK_MAX_N_PER_R * r) {
            return unRankByWalk(rank, total, n, r);
        }
        return unRankBySearch(rank, total, n, r);
    }

    private int[] unRankBySearch(BigInteger rank, BigInteger total, int n, int r) {
        int[] result = new int[r];
        int index = 0;

        // To get lexicographical order, we unrank the "reversed" rank in co-lexicographical order
        //BigInteger total = calculator.nCr(n, r);
        BigInteger remaining = total.subtract(BigInteger.ONE).subtract(rank);

        for (int k = r; k >= 1; k--) {
            // BINARY SEARCH: Find the largest 'c' such that nCr(c, k) <= remaining
            int low = k - 1;
            int high = n - 1;
            int best = k - 1; // Fallback if remaining is 0

            while (low <= high) {
                int mid = low + (high - low) / 2;
                BigInteger val = calculator.nCr(mid, k);

                if (val.compareTo(remaining) <= 0) {
                    best = mid;      // This 'c' is valid, try to find a larger one
                    low = mid + 1;
                } else {
                    high = mid - 1;  // 'c' is too large, search lower
                }
            }

            // MAP TO LEXICOGRAPHICAL ORDER:
            // Because k decreases from r to 1, 'best' decreases,
            // so (n - 1 - best) naturally increases, filling the array in ascending order!
            result[index++] = n - 1 - best;

            // Subtract the combinations we just skipped
            remaining = remaining.subtract(calculator.nCr(best, k));
        }

        return result;
    }

    /**
     * Same result as {@code unRankBySearch}, computed by walking the candidate position c downwards once
     * while keeping v = C(c, k) up to date with O(1) big-number operations per step.
     */
    private static int[] unRankByWalk(BigInteger rank, BigInteger total, int n, int r) {
        BigInteger remaining = total.subtract(BigInteger.ONE).subtract(rank);
        int[] result = new int[r];
        int c = n - 1;
        int k = r;
        BigInteger v = total.multiply(BigInteger.valueOf(n - r)).divide(BigInteger.valueOf(n)); // C(n-1, r)

        for (int index = 0; k >= 1; index++, k--) {
            // v holds C(c, k). Step c down (C(c-1,k) = C(c,k)*(c-k)/c) until it no longer exceeds remaining.
            while (v.compareTo(remaining) > 0) {
                v = v.multiply(BigInteger.valueOf(c - k)).divide(BigInteger.valueOf(c));
                c--;
            }
            result[index] = n - 1 - c;
            remaining = remaining.subtract(v);
            if (k > 1) {
                // C(c, k-1) = C(c, k) * k / (c-k+1)
                int d = c - k + 1;
                v = d == 0 ? BigInteger.ONE : v.multiply(BigInteger.valueOf(k)).divide(BigInteger.valueOf(d));
            }
        }
        return result;
    }

    /**
     * Converts a combination to its Combinadic representation.
     * <p>
     * This is achieved by mapping each element of the combination as:
     * <pre>
     *     combinadic[i] = nₙ − 1 − mthCombination[i]
     * </pre>
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
