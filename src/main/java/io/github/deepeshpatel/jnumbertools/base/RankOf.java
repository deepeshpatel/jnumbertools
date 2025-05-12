/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

/**
 * Calculates the lexicographical rank of permutations and combinations.
 * <p>
 * The rank is the position of a permutation or combination in its lexicographical order.
 * Permutations are order-dependent arrangements, while combinations are order-independent
 * selections. This class supports:
 * <p>
 * - k-Permutation (`ⁿPₖ`): Ranks a k-permutation of n distinct elements.
 * - Unique Permutation (`ⁿ!`): Ranks a full permutation of n distinct elements.
 * - Repeated Permutation (`nᵣ`): Ranks a permutation of r elements from n distinct elements with repetition.
 * - Unique Combination (`ⁿCᵣ`): Ranks a combination of r elements from n distinct elements without repetition.
 * </p>
 * Example usage:
 * <pre>
 * RankOf rankOf = new RankOf();
 * BigInteger kPermutationRank = rankOf.kPermutation(5, 2, 1, 0);
 * BigInteger uniquePermutationRank = rankOf.uniquePermutation(3, 2, 1, 0);
 * BigInteger repeatedPermutationRank = rankOf.repeatedPermutation(4, 2, 1, 0);
 * BigInteger uniqueCombinationRank = rankOf.uniqueCombination(5, 2, 1, 0);
 * </pre>
 *
 * @author Deepesh Patel
 */
public final class RankOf {

    private final Calculator calculator;

    /**
     * Constructs a new RankOf instance with a default Calculator.
     */
    public RankOf() {
        this(new Calculator());
    }

    /**
     * Constructs a new RankOf instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public RankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Calculates the lexicographical rank of a k-permutation (`ⁿPₖ`).
     * <p>
     * Ranks a permutation of k distinct elements selected from {0, 1, ..., n-1}, where
     * the input array contains k distinct indices in lexicographical order.
     * </p>
     *
     * @param k the size of the permutation (k ≥ 0)
     * @param permutation the array of k distinct indices
     * @return the rank of the k-permutation
     */
    public BigInteger kPermutation(int k, int... permutation) {
        return PermutadicAlgorithms.rank(k, permutation);
    }

    /**
     * Calculates the lexicographical rank of a unique permutation (`ⁿ!`).
     * <p>
     * Ranks a full permutation of n distinct elements, where the input array contains
     * n distinct indices from {0, 1, ..., n-1} in lexicographical order.
     * </p>
     *
     * @param permutation the array of n distinct indices
     * @return the rank of the unique permutation
     */
    public BigInteger uniquePermutation(int... permutation) {
        return PermutadicAlgorithms.rank(permutation.length, permutation);
    }

    /**
     * Calculates the lexicographical rank of a repeated permutation (`nᵣ`).
     * <p>
     * Ranks a permutation of r elements selected from {0, 1, ..., n-1} with repetition,
     * where the input array contains r indices (each 0 to n-1) in lexicographical order.
     * </p>
     *
     * @param n the number of distinct elements
     * @param permutation the array of r indices with possible repetition
     * @return the rank of the repeated permutation
     */
    public BigInteger repeatedPermutation(int n, Integer... permutation) {
        BigInteger result = BigInteger.ZERO;
        long power = 1;
        for (int i = permutation.length - 1; i >= 0; i--) {
            long placeValue = permutation[i] * power;
            result = result.add(BigInteger.valueOf(placeValue));
            power *= n;
        }
        return result;
    }

    /**
     * Calculates the lexicographical rank of a unique combination (`ⁿCᵣ`).
     * <p>
     * Ranks a combination of r distinct elements selected from {0, 1, ..., n-1} without
     * repetition, where the input array contains r distinct indices in lexicographical order.
     * </p>
     *
     * @param n the number of distinct elements
     * @param combination the array of r distinct indices
     * @return the rank of the unique combination
     */
    public BigInteger uniqueCombination(int n, int... combination) {
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }
}