/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

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
 * // Rank of 2-permutation [1,0] from a set of size 3
 * BigInteger rank = rankOf.kPermutation(3, 1, 0);
 * System.out.println(rank); // 2 (the third permutation in lexicographical order)
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
     * Ranks a permutation of {@code permutation.length} distinct elements selected from
     * {@code {0, 1, ..., n-1}}, where {@code n} is the size of the source set.
     * The input array contains the selected indices in lexicographical order.
     * </p>
     *
     * @param n the size of the source set (n ≥ 0)
     * @param permutation the array of k distinct indices
     * @return the rank of the k-permutation
     * @throws NullPointerException     if the permutation is null
     * @throws IllegalArgumentException if n is negative, the permutation is longer than n, or it contains a
     *                                  value outside [0, n) or a duplicate value
     */
    public BigInteger kPermutation(int n, int... permutation) {
        validateArrangement(n, permutation);
        return PermutadicAlgorithms.rank(n, permutation);
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
     * @throws NullPointerException     if the permutation is null
     * @throws IllegalArgumentException if it contains a value outside [0, n) or a duplicate value
     */
    public BigInteger uniquePermutation(int... permutation) {
        Objects.requireNonNull(permutation, "permutation must not be null");
        validateArrangement(permutation.length, permutation);
        return PermutadicAlgorithms.rank(permutation.length, permutation);
    }

    /**
     * Calculates the lexicographical rank of a repeated permutation (`nᵣ`).
     * <p>
     * Ranks a permutation of r elements selected from {0, 1, ..., n-1} with repetition,
     * where the input array contains r indices (each 0 to n-1) in lexicographical order.
     * The rank is the base-n value of the digit string, most significant digit first, and is exact however
     * long the string is.
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @param permutation the array of r indices with possible repetition
     * @return the rank of the repeated permutation
     * @throws NullPointerException     if the array or any of its elements is null
     * @throws IllegalArgumentException if n is negative or any value is outside [0, n)
     */
    public BigInteger repeatedPermutation(int n, Integer... permutation) {
        Objects.requireNonNull(permutation, "permutation must not be null");
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, found " + n);
        }
        BigInteger base = BigInteger.valueOf(n);
        BigInteger result = BigInteger.ZERO;
        for (Integer digit : permutation) {
            Objects.requireNonNull(digit, "permutation must not contain null");
            if (digit < 0 || digit >= n) {
                throw new IllegalArgumentException("Every value must be in [0, " + n + "), found " + digit);
            }
            result = result.multiply(base).add(BigInteger.valueOf(digit));   // Horner: no power to overflow
        }
        return result;
    }

    /**
     * Calculates the lexicographical rank of a unique combination (`ⁿCᵣ`).
     * <p>
     * Ranks a combination of r distinct elements selected from {0, 1, ..., n-1} without
     * repetition, where the input array contains r distinct indices in lexicographical order
     * (strictly ascending).
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @param combination the array of r distinct, strictly ascending indices
     * @return the rank of the unique combination
     * @throws NullPointerException     if the combination is null
     * @throws IllegalArgumentException if n is negative, the combination is longer than n, or its values are
     *                                  not strictly ascending values in [0, n)
     */
    public BigInteger uniqueCombination(int n, int... combination) {
        validateCombination(n, combination);
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }

    /**
     * Calculates the lexicographical rank of a derangement (`!n`).
     *
     * @param derangement the derangement array (full size n, no fixed points)
     * @return the 0-based lexicographical rank of the derangement
     * @throws IllegalArgumentException if {@code derangement} is not a valid derangement
     */
    public BigInteger derangement(int... derangement) {
        return new DerangadicAlgorithms(calculator).rank(derangement, derangement.length);
    }

    // ── Input validation ────────────────────────────────────────────────────────────────────────
    // These helpers are the user-facing entry points, so they own the validation described in
    // ValidationRules.md ("RANK / UNRANK HELPERS"). The numbersystem classes they delegate to assume valid input.

    /**
     * Rejects anything that is not a k-permutation of {0, ..., size-1}. Works on a sorted copy, so it costs
     * O(k log k) time and O(k) memory whatever the size of the set.
     */
    private static void validateArrangement(int size, int[] arrangement) {
        Objects.requireNonNull(arrangement, "arrangement must not be null");
        if (size < 0) {
            throw new IllegalArgumentException("size must be >= 0, found " + size);
        }
        if (arrangement.length > size) {
            throw new IllegalArgumentException("An arrangement of " + arrangement.length
                    + " values cannot be selected from a set of size " + size);
        }
        int[] sorted = arrangement.clone();
        Arrays.sort(sorted);
        if (sorted.length > 0 && (sorted[0] < 0 || sorted[sorted.length - 1] >= size)) {
            throw new IllegalArgumentException("Every value must be in [0, " + size + "), found "
                    + (sorted[0] < 0 ? sorted[0] : sorted[sorted.length - 1]));
        }
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] == sorted[i - 1]) {
                throw new IllegalArgumentException("Duplicate value in arrangement: " + sorted[i]);
            }
        }
    }

    /**
     * Rejects anything that is not a combination of {0, ..., n-1} in canonical (strictly ascending) form.
     * A single O(r) pass, with no copy: ascending order makes the duplicate and the range checks trivial.
     */
    private static void validateCombination(int n, int[] combination) {
        Objects.requireNonNull(combination, "combination must not be null");
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, found " + n);
        }
        if (combination.length > n) {
            throw new IllegalArgumentException("A combination of " + combination.length
                    + " values cannot be selected from a set of size " + n);
        }
        for (int i = 0; i < combination.length; i++) {
            int value = combination[i];
            if (value < 0 || value >= n) {
                throw new IllegalArgumentException("Every value must be in [0, " + n + "), found " + value);
            }
            if (i > 0 && value <= combination[i - 1]) {
                throw new IllegalArgumentException(value == combination[i - 1]
                        ? "Duplicate value in combination: " + value
                        : "Combination must be in ascending order, but " + value + " follows " + combination[i - 1]);
            }
        }
    }
}
