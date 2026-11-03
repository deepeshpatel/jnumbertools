/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.numbersystem.*;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Determines the permutation or combination at a given lexicographical rank.
 * <p>
 * Un-ranking finds the permutation or combination corresponding to a rank in its
 * lexicographical order. Permutations are order-dependent arrangements, while
 * combinations are order-independent selections. Outputs are arrays of indices.
 * This class supports:
 * <p>
 * - Unique Permutation (`n!`): Un-ranks a full permutation of n distinct elements.
 * - k-Permutation (`ⁿPₖ`): Un-ranks a k-permutation from n distinct elements.
 * - Unique Combination (`ⁿCᵣ`): Un-ranks a combination of r elements from n distinct elements.
 * </p>
 * <p>
 * This class is immutable and thread-safe. All methods are stateless and can be safely shared across threads.
 * </p>
 * Example usage:
 * <pre>
 * UnrankOf unrankOf = new UnrankOf();
 * int[] uniquePermutation = unrankOf.uniquePermutation(BigInteger.valueOf(10), 5);
 * int[] kPermutation = unrankOf.kPermutation(BigInteger.valueOf(5), 10, 3);
 * int[] uniqueCombination = unrankOf.uniqueCombination(BigInteger.valueOf(15), 5, 3);
 * </pre>
 *
 * @see AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class UnrankOf {

    private final Calculator calculator;

    /**
     * Constructs a new UnrankOf instance with a default Calculator.
     */
    public UnrankOf() {
        this(new Calculator());
    }

    /**
     * Constructs a new UnrankOf instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public UnrankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Determines the unique permutation (`n!`) at a given rank.
     * <p>
     * Un-ranks a full permutation of n distinct elements, returning an array of n
     * distinct indices from {0, 1, ..., n-1} in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @param size the size of the permutation (n)
     * @return an array of n distinct indices
     */
    public int[] uniquePermutation(BigInteger rank, int size) {
        return FactoradicAlgorithms.unRank(rank, size);
    }

    /**
     * Determines the unique permutation (`n!`) at a given rank with minimal size.
     * <p>
     * Un-ranks a full permutation, determining the smallest n where rank < n!, and
     * returns an array of n distinct indices from {0, 1, ..., n-1} in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @return an array of n distinct indices
     */
    public int[] uniquePermutationMinimumSize(BigInteger rank) {
        int size = calculator.factorialUpperBound(rank);
        return FactoradicAlgorithms.unRank(rank, size);
    }

    /**
     * Determines the k-permutation (`ⁿPₖ`) at a given rank.
     * <p>
     * Un-ranks a k-permutation of k distinct elements from {0, 1, ..., n-1}, returning
     * an array of k distinct indices in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @param n the number of distinct elements
     * @param k the size of the permutation (k ≥ 0)
     * @return an array of k distinct indices
     * @throws NullPointerException if the rank is null
     * @throws ArithmeticException  if the rank is negative or not below ⁿPₖ (this includes a negative n or k,
     *                              and k &gt; n, where no k-permutation exists)
     */
    public int[] kPermutation(BigInteger rank, int n, int k) {
        BigInteger max = calculator.nPr(n, k);
        if (rank.signum() < 0 || rank.compareTo(max) >= 0) {
            throw new ArithmeticException(
                    String.format("Out of range. Can't decode %d to mᵗʰ permutation as it is < 0 or ≥ Permutation(%d,%d)",
                            rank, n, k)
            );
        }
        return PermutadicAlgorithms.unRank(rank, n, k);
    }

    /**
     * Determines the unique combination (`ⁿCᵣ`) at a given rank.
     * <p>
     * Un-ranks a combination of r distinct elements from {0, 1, ..., n-1} without
     * repetition, returning an array of r distinct indices in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank (0 ≤ rank &lt; ⁿCᵣ)
     * @param n the number of distinct elements (n ≥ 0)
     * @param r the size of the combination (r ≥ 0)
     * @return an array of r distinct indices
     * @throws NullPointerException     if the rank is null
     * @throws IllegalArgumentException if n or r is negative, or the rank is negative or not below ⁿCᵣ
     *                                  (every rank is out of range when r &gt; n)
     */
    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        Objects.requireNonNull(rank, "rank must not be null");
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, found " + n);
        }
        if (r < 0) {
            throw new IllegalArgumentException("r must be >= 0, found " + r);
        }
        BigInteger nCr = calculator.nCr(n, r);
        if (rank.signum() < 0 || rank.compareTo(nCr) >= 0) {
            throw new IllegalArgumentException(String.format(
                    "Out of range. Can't decode %d to a combination as it is < 0 or >= Combination(%d,%d) = %d",
                    rank, n, r, nCr));
        }
        return new CombinadicAlgorithms(calculator).unRank(rank, nCr, n, r);
    }

    /**
     * Determines the derangement (`!n`) at a given rank.
     * <p>
     * Un-ranks a derangement of {@code n} distinct elements, returning an array
     * of {@code n} distinct indices in {@code [0, n)} such that no index equals
     * its position (i.e., a fixed-point-free permutation) in lexicographical
     * order.
     * </p>
     *
     * @param rank the lexicographical rank ({@code 0 ≤ rank < D_n})
     * @param n    the order of the derangement ({@code n ≥ 0})
     * @return an array of {@code n} distinct indices forming the derangement
     * @throws IllegalArgumentException if {@code rank} is out of range
     */
    public int[] derangement(BigInteger rank, int n) {
        return new DerangadicAlgorithms(calculator).unrank(rank, n);
    }

    /**
     * Convenience overload of {@link #derangement(BigInteger, int)} for
     * {@code long} ranks.
     *
     * @param rank the lexicographical rank
     * @param n    the order of the derangement
     * @return an array of {@code n} distinct indices forming the derangement
     */
    public int[] derangement(long rank, int n) {
        return derangement(BigInteger.valueOf(rank), n);
    }
}
