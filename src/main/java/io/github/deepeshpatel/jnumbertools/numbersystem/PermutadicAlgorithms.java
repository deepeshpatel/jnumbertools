/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;

import java.math.BigInteger;
import java.util.*;

/**
 * Provides algorithms for operations related to the Permutadic number system, which is a mixed radix system based on permutations.
 * <p>
 * The class includes methods to convert between decimal values and Permutadic representations, compute mᵗʰ permutations,
 * and rank or un-rank permutations.
 * </p>
 *
 * <p>
 * <strong>Performance:</strong> each conversion picks one of three algorithms from the set size s and the number of
 * selected items k (thresholds measured with {@code OptimizationBenchmark}; they depend on the JVM and CPU):
 * <ul>
 *   <li>sparse, O(k²), independent of s, when k² is at most a few times s;</li>
 *   <li>Fenwick tree, O(s + k log s), otherwise;</li>
 *   <li>a plain ArrayList pool, O(s·k), only when turning digits back into a permutation for small s.</li>
 * </ul>
 *
 * <p>
 * Example usages:
 * <ul>
 * <li>Convert a decimal value to a Permutadic representation: {@link #toPermutadic(BigInteger, int)}</li>
 * <li>Convert a Permutadic representation to a decimal value: {@link #toDecimal(List, int)}</li>
 * <li>Compute the mᵗʰ permutation from a Permutadic representation: {@link #toMthPermutation(List, int, int)}</li>
 * <li>Convert an mᵗʰ permutation to Permutadic representation: {@link #mthPermutationToPermutadic(int[], int)}</li>
 * <li>Rank a permutation: {@link #rank(int, int...)}</li>
 * <li>Un-rank a permutation (no bound check): {@link #unRank(BigInteger, int, int)}</li>
 * </ul>
 *
 * <p>
 * <strong>Implementation class: intended to be used through {@link io.github.deepeshpatel.jnumbertools.base.RankOf} and
 * {@link io.github.deepeshpatel.jnumbertools.base.UnrankOf} (and the builders).</strong> Those validate their input (null, negative sizes, duplicate or
 * out-of-range values, ranks outside [0, nPk), ...) as described in {@code ValidationRules.md}. The methods here
 * assume valid input and do not check it, so that internal callers that already hold valid values pay nothing.
 * </p>
 * @author Deepesh Patel
 */
public final class PermutadicAlgorithms {

    /**
     * Below this set size, turning digits back into a permutation with an ArrayList pool is as fast as the
     * Fenwick tree (the two are within noise from about s = 100) and has the lower constant cost.
     */
    private static final int FENWICK_THRESHOLD = 100;

    /**
     * Computing digits from a permutation ({@link #rank}, {@link #mthPermutationToPermutadic}) uses the
     * sparse O(k²) loop while k² is at most this many times the set size; beyond that the Fenwick tree wins.
     * Measured crossover: about 1 to 2 for s ≥ 8,000 (larger, up to 4 to 8, for small s where the difference is a fraction of a microsecond).
     */
    private static final int SPARSE_DIGITS_MAX_K_SQUARED_PER_S = 2;

    /**
     * Turning digits into a permutation ({@link #unRank}, {@link #toMthPermutation}) uses the sparse
     * algorithm while k² is at most this many times the set size. The crossover is later than for computing
     * digits (about 8 to 16) because the BigInteger division in {@code unRank} dominates the cost.
     */
    private static final int SPARSE_SELECT_MAX_K_SQUARED_PER_S = 8;

    /**
     * Converts a decimal value to its Permutadic representation.
     *
     * @param decimalValue the decimal value to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a list of integers representing the Permutadic value.
     */
    public static List<Integer> toPermutadic(BigInteger decimalValue, int degree) {
        List<Integer> permutadicValues = new ArrayList<>(16);
        int radix = degree + 1;

        do {
            BigInteger[] dr = decimalValue.divideAndRemainder(BigInteger.valueOf(radix));
            permutadicValues.add(dr[1].intValue());
            decimalValue = dr[0];
            radix++;
        } while (decimalValue.signum() > 0);

        return List.copyOf(permutadicValues);
    }

    /**
     * Converts a Permutadic representation to its decimal value.
     *
     * @param permutadicValues the Permutadic representation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return the decimal value equivalent of the Permutadic representation.
     */
    public static BigInteger toDecimal(List<Integer> permutadicValues, int degree) {
        BigInteger sum = BigInteger.ZERO;
        BigInteger placeValue = BigInteger.ONE;

        int radix = degree;

        for (int digit : permutadicValues) {
            sum = sum.add(placeValue.multiply(BigInteger.valueOf(digit)));
            radix++;
            placeValue = placeValue.multiply(BigInteger.valueOf(radix));
        }

        return sum;
    }

    /**
     * Computes the mᵗʰ permutation from the given Permutadic representation.
     * <p>
     * When k is small relative to s (k² ≤ {@value #SPARSE_SELECT_MAX_K_SQUARED_PER_S}·s) a sparse O(k²) algorithm that never builds the
     * pool of s values is used. Otherwise a Fenwick tree (O(s + k log s)) is used, except for small sets
     * (s ≤ {@value #FENWICK_THRESHOLD}) where an ArrayList pool is as fast.
     * A digit outside its radix throws {@link IndexOutOfBoundsException} on every path.
     * </p>
     *
     * @param permutadic the Permutadic representation to be converted.
     * @param s the size of the set from which the permutation is selected.
     * @param k the number of items to be selected for the permutation.
     * @return an array representing the mᵗʰ permutation.
     */
    public static int[] toMthPermutation(List<Integer> permutadic, int s, int k) {
        if (useSparseSelect(s, k)) {
            return toMthPermutationSparse(permutadic, s, k);
        }
        if (s > FENWICK_THRESHOLD) {
            return toMthPermutationFenwick(permutadic, s, k);
        } else {
            return toMthPermutationArrayList(permutadic, s, k);
        }
    }

    /**
     * Standard ArrayList implementation for smaller permutations.
     * Time complexity: O(s·k) but with excellent constant factors due to cache locality.
     */
    private static int[] toMthPermutationArrayList(List<Integer> permutadic, int s, int k) {
        int[] result = new int[k];

        List<Integer> pool = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            pool.add(i);
        }

        int j = 0;

        for (int i = k - 1; i >= 0; i--) {
            int index = i < permutadic.size() ? permutadic.get(i) : 0;
            result[j++] = pool.remove(index);
        }

        return result;
    }

    /**
     * Optimized Fenwick Tree implementation for large permutations.
     * Time complexity: O(k log s)
     */
    private static int[] toMthPermutationFenwick(List<Integer> permutadic, int s, int k) {
        int[] result = new int[k];

        FenwickTree ft = FenwickTree.ofOnes(s);

        int resultIdx = 0;
        for (int i = k - 1; i >= 0; i--) {
            int digit = i < permutadic.size() ? permutadic.get(i) : 0;
            if (digit < 0 || digit >= s - resultIdx) {
                throw new IndexOutOfBoundsException("Permutadic digit " + digit + " at place " + i
                        + " is outside [0, " + (s - resultIdx) + ")");
            }
            int rank = digit + 1; // Convert to 1-indexed

            int pos = ft.findKth(rank);
            result[resultIdx++] = pos - 1; // Convert back to 0-indexed

            ft.update(pos, -1); // Mark as taken
        }

        return result;
    }

    /**
     * Converts a permutation to its Permutadic representation.
     * <p>
     * When k is small relative to the set size (k² ≤ {@value #SPARSE_DIGITS_MAX_K_SQUARED_PER_S}·s) the digits are computed from the k values alone in
     * O(k²), without building the pool of s values; otherwise a Fenwick tree is used (O(s + k log s)).
     * </p>
     *
     * @param permutation the mᵗʰ permutation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a list of integers representing the Permutadic value.
     */
    public static List<Integer> mthPermutationToPermutadic(int[] permutation, int degree) {
        int[] digits = digitsOf(permutation, degree + permutation.length);
        Integer[] boxed = new Integer[digits.length];
        for (int j = 0; j < digits.length; j++) {
            boxed[j] = digits[j];
        }
        return List.of(boxed);
    }

    /**
     * Ranks a permutation in lexicographical order.
     * <p>
     * Computes the digits with the sparse O(k²) loop when k is small relative to the set size, and with a Fenwick
     * tree otherwise; the rank is then the digits accumulated in the mixed radix.
     * </p>
     *
     * @param size the size of the set from which the permutation is selected.
     * @param mth_kPermutation the permutation to be ranked.
     * @return the zero-based rank of the permutation.
     * @see io.github.deepeshpatel.jnumbertools.base.RankOf
     */
    public static BigInteger rank(int size, int... mth_kPermutation) {
        int degree = size - mth_kPermutation.length;
        return accumulate(digitsOf(mth_kPermutation, size), degree);
    }

    private static boolean useSparseDigits(int size, int k) {
        return (long) k * k <= (long) SPARSE_DIGITS_MAX_K_SQUARED_PER_S * size;
    }

    private static boolean useSparseSelect(int size, int k) {
        return (long) k * k <= (long) SPARSE_SELECT_MAX_K_SQUARED_PER_S * size;
    }

    /**
     * Permutadic digits of a k-permutation of {0..size-1}, for positions i = k-1 down to 0. The digit for
     * position i is the index of p[i] among the values not used by positions before it.
     */
    private static int[] digitsOf(int[] p, int size) {
        return useSparseDigits(size, p.length) ? digitsSparse(p) : digitsFenwick(p, size);
    }

    /** O(k²), independent of the set size. */
    private static int[] digitsSparse(int[] p) {
        int k = p.length;
        int[] digits = new int[k];
        for (int i = k - 1, j = 0; i >= 0; i--, j++) {
            int pi = p[i];
            int smaller = 0;
            for (int x = 0; x < i; x++) {
                if (p[x] < pi) {
                    smaller++;
                }
            }
            digits[j] = pi - smaller;
        }
        return digits;
    }

    /** O(size + k log size). */
    private static int[] digitsFenwick(int[] p, int size) {
        int k = p.length;
        FenwickTree ft = FenwickTree.ofOnes(size);
        for (int value : p) {
            ft.update(value + 1, -1); // remove the chosen values; what is left is the pool of unused values
        }
        int[] digits = new int[k];
        for (int i = k - 1, j = 0; i >= 0; i--, j++) {
            int value = p[i];
            digits[j] = ft.rsq(value);  // unused values smaller than value
            ft.update(value + 1, 1);    // value is unused for the positions before i
        }
        return digits;
    }

    /** Sum of digits[j] * (degree+1) * (degree+2) * ... * (degree+j), the mixed-radix value. */
    private static BigInteger accumulate(int[] digits, int degree) {
        BigInteger rank = BigInteger.ZERO;
        BigInteger placeValue = BigInteger.ONE;
        int radix = degree;
        for (int digit : digits) {
            if (digit != 0) {
                rank = rank.add(placeValue.multiply(BigInteger.valueOf(digit)));
            }
            radix++;
            placeValue = placeValue.multiply(BigInteger.valueOf(radix));
        }
        return rank;
    }

    /**
     * Un-rank computed without materialising the pool of s values: O(k²) independent of the set size.
     * Each position picks the d-th smallest value not yet chosen, found by shifting d past the (sorted)
     * values already chosen.
     */
    private static int[] unRankSparse(BigInteger rank, int size, int k) {
        int degree = size - k;
        int[] digit = new int[k];
        BigInteger quotient = rank;
        for (int j = 0; j < k && quotient.signum() > 0; j++) {
            BigInteger[] dr = quotient.divideAndRemainder(BigInteger.valueOf(degree + 1L + j));
            digit[j] = dr[1].intValue();
            quotient = dr[0];
        }

        return selectFromDigits(digit, k);
    }

    /**
     * Turns permutadic digits (digit[j] has radix degree + 1 + j) into the k selected values, without
     * materialising the pool: position p takes the digit[k - 1 - p]-th smallest value not chosen yet.
     */
    private static int[] selectFromDigits(int[] digit, int k) {
        int[] result = new int[k];
        int[] chosen = new int[k]; // sorted copy of result[0..m)
        int m = 0;
        for (int position = 0; position < k; position++) {
            int value = digit[k - 1 - position];
            for (int i = 0; i < m && chosen[i] <= value; i++) {
                value++;
            }
            result[position] = value;

            int at = m;
            while (at > 0 && chosen[at - 1] > value) {
                chosen[at] = chosen[at - 1];
                at--;
            }
            chosen[at] = value;
            m++;
        }
        return result;
    }

    private static int[] toMthPermutationSparse(List<Integer> permutadic, int s, int k) {
        int degree = s - k;
        int[] digit = new int[k];
        for (int j = 0; j < k && j < permutadic.size(); j++) {
            int d = permutadic.get(j);
            if (d < 0 || d >= degree + 1L + j) {
                throw new IndexOutOfBoundsException("Permutadic digit " + d + " at place " + j
                        + " is outside [0, " + (degree + 1L + j) + ")");
            }
            digit[j] = d;
        }
        return selectFromDigits(digit, k);
    }

    /**
     * Converts a rank to its corresponding k-permutation without bounds checking.
     * <p>
     * Directly computes the k-permutation at the given lexicographical rank using
     * permutadic representation. This method skips bounds validation for performance,
     * assuming the caller has verified that 0 ≤ rank &lt; P(n,k).
     * </p>
     * <p>
     * Example: For size=5, k=3, rank 0 returns [0,1,2], rank 1 returns [0,1,3],
     * etc., selecting 3 elements from the set {0,1,2,3,4}.
     * </p>
     *
     * @param rank the 0-based lexicographical rank of the desired k-permutation
     * @param size the total number of elements (n)
     * @param k the number of elements to select for the permutation
     * @return an array of length k representing the k-permutation at the specified rank
     * @see io.github.deepeshpatel.jnumbertools.base.UnrankOf
     */
    public static int[] unRank(BigInteger rank, int size, int k) {
        if (useSparseSelect(size, k)) {
            return unRankSparse(rank, size, k);
        }
        List<Integer> permutadic = toPermutadic(rank, size - k);
        return toMthPermutation(permutadic, size, k);
    }
}
