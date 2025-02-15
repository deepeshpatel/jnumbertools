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
 * Provides methods for determining the permutation or combination corresponding to a given rank.
 * This class uses factoradic, permutadic, and combinadic algorithms to unrank permutations and combinations.
 *
 * <p>
 * The class supports:
 * <ul>
 *     <li><strong>Unique Permutation Unranking:</strong> Determines the permutation corresponding to a given rank in unique permutations.</li>
 *     <li><strong>K-Permutation Unranking:</strong> Determines the permutation corresponding to a given rank in k-permutations.</li>
 *     <li><strong>Unique Combination Unranking:</strong> Determines the combination corresponding to a given rank in unique combinations.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * <pre>
 * UnrankOf unrankOf = new UnrankOf();
 * int[] uniquePerm = unrankOf.uniquePermutation(BigInteger.valueOf(10), 5);
 * int[] kPerm = unrankOf.kPermutation(BigInteger.valueOf(5), 10, 3);
 * int[] uniqueComb = unrankOf.uniqueCombination(BigInteger.valueOf(15), 5, 3);
 * </pre>
 * @author Deepesh Patel
 */
public final class UnrankOf {

    private final Calculator calculator;

    /**
     * Constructs a new {@code UnrankOf} instance with a default {@code Calculator}.
     */
    public UnrankOf() {
        this(new Calculator());
    }

    /**
     * Constructs a new {@code UnrankOf} instance with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to use for unranking calculations.
     */
    public UnrankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Determines the unique permutation corresponding to a given rank.
     *
     * @param rank The rank of the permutation.
     * @param size The size of the permutation.
     * @return An array representing the unique permutation at the given rank.
     */
    public int[] uniquePermutation(BigInteger rank, int size) {
        return FactoradicAlgorithms.unRank(rank, size);
    }

    public int[] uniquePermutationMinimumSize(BigInteger rank) {
        int size = calculator.factorialUpperBound(rank);
        return FactoradicAlgorithms.unRank(rank, size);
    }

    /**
     * Determines the k-permutation corresponding to a given rank.
     *
     * @param rank The rank of the permutation.
     * @param n The total number of distinct objects.
     * @param k The size of the permutation.
     * @return An array representing the k-permutation at the given rank.
     */
    public int[] kPermutation(BigInteger rank, int n, int k) {
        return new PermutadicAlgorithms(calculator).unRankWithBoundCheck(rank, n, k);
    }

    /**
     * Determines the unique combination corresponding to a given rank.
     *
     * @param rank The rank of the combination.
     * @param n The total number of distinct objects.
     * @param r The size of the combination.
     * @return An array representing the unique combination at the given rank.
     */
    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        return new CombinadicAlgorithms(calculator).unRank(rank, calculator.nCr(n, r), n, r);
    }
}
