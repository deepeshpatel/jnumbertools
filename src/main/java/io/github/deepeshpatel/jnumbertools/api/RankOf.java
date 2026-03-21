/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api;

import io.github.deepeshpatel.jnumbertools.base.CalculatorImpl;
import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
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
 * - k-Permutation (ⁿPₖ): Ranks a k-permutation of n distinct elements.
 * - Unique Permutation (n!): Ranks a full permutation of n distinct elements.
 * - Repeated Permutation (nʳ): Ranks a permutation of r elements from n distinct elements with repetition.
 * - Unique Combination (ⁿCᵣ): Ranks a combination of r elements from n distinct elements without repetition.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Ranking k-Permutations</h3>
 * <pre>
 * RankOf rankOf = new RankOf();
 *
 * // Rank of 2-permutation [1,0] from set of size 3
 * BigInteger rank = rankOf.kPermutation(2, 1, 0);
 * System.out.println(rank); // Rank of [1,0] among 3P2 = 6 permutations
 * </pre>
 *
 * <h3>Ranking Unique Permutations</h3>
 * <pre>
 * // Rank of full permutation [2,0,1] of 3 elements
 * BigInteger rank = rankOf.uniquePermutation(2, 0, 1);
 * System.out.println(rank); // 3 (since [2,0,1] is the 4th permutation of [0,1,2])
 * </pre>
 *
 * <h3>Ranking Repeated Permutations</h3>
 * <pre>
 * // Rank of repeated permutation [1,0,1] from binary set {0,1} with length 3
 * BigInteger rank = rankOf.repeatedPermutation(2, 1, 0, 1);
 * System.out.println(rank); // Rank among 2³ = 8 permutations
 * </pre>
 *
 * <h3>Ranking Unique Combinations</h3>
 * <pre>
 * // Rank of combination [1,3,4] from set of 5 elements
 * BigInteger rank = rankOf.uniqueCombination(5, 1, 3, 4);
 * System.out.println(rank); // Rank among C(5,3) = 10 combinations
 * </pre>
 *
 * <p>
 * This class is immutable and thread-safe. All methods are stateless and can be safely shared across threads.
 * </p>
 *
 * @see AllExamples
 * @see UnrankOf
 * @see NumberSystem
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class RankOf {

    private final Calculator calculator;

    /**
     * Constructs a new RankOf instance with a default Calculator.
     */
    public RankOf() {
        this(new CalculatorImpl());
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
     * Calculates the lexicographical rank of a unique permutation (`n!`).
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
