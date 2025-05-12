/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

/**
 * Determines the permutation or combination at a given lexicographical rank.
 * <p>
 * Unranking finds the permutation or combination corresponding to a rank in its
 * lexicographical order. Permutations are order-dependent arrangements, while
 * combinations are order-independent selections. Outputs are arrays of indices.
 * This class supports:
 * <p>
 * - Unique Permutation (`ⁿ!`): Unranks a full permutation of n distinct elements.
 * - k-Permutation (`ⁿPₖ`): Unranks a k-permutation from n distinct elements.
 * - Unique Combination (`ⁿCᵣ`): Unranks a combination of r elements from n distinct elements.
 * </p>
 * Example usage:
 * <pre>
 * UnrankOf unrankOf = new UnrankOf();
 * int[] uniquePermutation = unrankOf.uniquePermutation(BigInteger.valueOf(10), 5);
 * int[] kPermutation = unrankOf.kPermutation(BigInteger.valueOf(5), 10, 3);
 * int[] uniqueCombination = unrankOf.uniqueCombination(BigInteger.valueOf(15), 5, 3);
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
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
     * Determines the unique permutation (`ⁿ!`) at a given rank.
     * <p>
     * Unranks a full permutation of n distinct elements, returning an array of n
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
     * Determines the unique permutation (`ⁿ!`) at a given rank with minimal size.
     * <p>
     * Unranks a full permutation, determining the smallest n where rank < n!, and
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
     * Unranks a k-permutation of k distinct elements from {0, 1, ..., n-1}, returning
     * an array of k distinct indices in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @param n the number of distinct elements
     * @param k the size of the permutation (k ≥ 0)
     * @return an array of k distinct indices
     */
    public int[] kPermutation(BigInteger rank, int n, int k) {
        return new PermutadicAlgorithms(calculator).unRankWithBoundCheck(rank, n, k);
    }

    /**
     * Determines the unique combination (`ⁿCᵣ`) at a given rank.
     * <p>
     * Unranks a combination of r distinct elements from {0, 1, ..., n-1} without
     * repetition, returning an array of r distinct indices in lexicographical order.
     * </p>
     *
     * @param rank the lexicographical rank
     * @param n the number of distinct elements
     * @param r the size of the combination (r ≥ 0)
     * @return an array of r distinct indices
     */
    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        return new CombinadicAlgorithms(calculator).unRank(rank, calculator.nCr(n, r), n, r);
    }
}