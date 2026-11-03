/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
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
 * <strong>Performance Optimization:</strong> For large permutations (s > 8000), this implementation automatically switches
 * to a Fenwick Tree-based algorithm that achieves O(k log s) time complexity instead of O(s·k). Benchmark results show:
 * <ul>
 *   <li>For s=20000, k=10000: Fenwick version is 3.4x faster than ArrayList version</li>
 *   <li>Crossover point where Fenwick becomes beneficial is around s=8000</li>
 *   <li>For smaller sizes, the standard ArrayList implementation is used for better performance</li>
 * </ul>
 * </p>
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
 * </p>
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
     * Threshold for switching to Fenwick Tree optimization.
     * Based on benchmarks, Fenwick becomes beneficial when s > 8000.
     */
    private static final int FENWICK_THRESHOLD = 8000;

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
     * For large permutations (s > {@value #FENWICK_THRESHOLD}), this method automatically switches
     * to an optimized Fenwick Tree implementation that achieves O(k log s) time complexity.
     * For smaller sizes, the standard ArrayList implementation (O(s·k)) is used for better
     * performance due to lower overhead and better cache locality.
     * </p>
     *
     * @param permutadic the Permutadic representation to be converted.
     * @param s the size of the set from which the permutation is selected.
     * @param k the number of items to be selected for the permutation.
     * @return an array representing the mᵗʰ permutation.
     */
    public static int[] toMthPermutation(List<Integer> permutadic, int s, int k) {
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

        FenwickTree ft = new FenwickTree(s);
        for (int i = 1; i <= s; i++) {
            ft.update(i, 1); // Mark all as available
        }

        int resultIdx = 0;
        for (int i = k - 1; i >= 0; i--) {
            int rank = (i < permutadic.size() ? permutadic.get(i) : 0) + 1; // Convert to 1-indexed

            int pos = ft.findKth(rank);
            result[resultIdx++] = pos - 1; // Convert back to 0-indexed

            ft.update(pos, -1); // Mark as taken
        }

        return result;
    }

    /**
     * Converts a permutation to its Permutadic representation.
     *
     * @param permutation the mᵗʰ permutation to be converted.
     * @param degree the degree of the Permutadic representation.
     * @return a list of integers representing the Permutadic value.
     */
    public static List<Integer> mthPermutationToPermutadic(int[] permutation, int degree) {
        int size = degree + permutation.length;

        Set<Integer> permutationSet = new HashSet<>(permutation.length);
        for (int v : permutation) {
            permutationSet.add(v);
        }

        List<Integer> pool = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (!permutationSet.contains(i)) {
                pool.add(i);
            }
        }

        List<Integer> result = new ArrayList<>(permutation.length);

        for (int i = permutation.length - 1; i >= 0; i--) {
            int value = permutation[i];
            int index = Collections.binarySearch(pool, value);
            index = -(index + 1);
            pool.add(index, value);
            result.add(index);
        }

        return List.copyOf(result);
    }

    /**
     * Computes the rank of a permutation in the Permutadic system.
     *
     * @param size the size of the set from which the permutation is selected.
     * @param mth_kPermutation the mᵗʰ permutation to be ranked.
     * @return the rank of the given permutation.
     * @see {@link io.github.deepeshpatel.jnumbertools.base.RankOf}

     */
    public static BigInteger rank(int size, int... mth_kPermutation) {
        int degree = size - mth_kPermutation.length;
        List<Integer> permutadic = mthPermutationToPermutadic(mth_kPermutation, degree);
        return toDecimal(permutadic, degree);
    }

    /**
     * Converts a rank to its corresponding k-permutation without bounds checking.
     * <p>
     * Directly computes the k-permutation at the given lexicographical rank using
     * permutadic representation. This method skips bounds validation for performance,
     * assuming the caller has verified that 0 ≤ rank < P(n,k).
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
     * @see {@link io.github.deepeshpatel.jnumbertools.base.UnrankOf}
     */
    public static int[] unRank(BigInteger rank, int size, int k) {
        List<Integer> permutadic = toPermutadic(rank, size - k);
        return toMthPermutation(permutadic, size, k);
    }
}
