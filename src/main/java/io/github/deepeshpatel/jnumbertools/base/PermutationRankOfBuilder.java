/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.DerangadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Ranks permutations and derangements in lexicographical order.
 */
public final class PermutationRankOfBuilder {

    private final Calculator calculator;

    /**
     * Constructs a new PermutationRankOfBuilder instance with a default Calculator.
     */
    public PermutationRankOfBuilder() {
        this(new Calculator());
    }

    /**
     * Constructs a new PermutationRankOfBuilder instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public PermutationRankOfBuilder(Calculator calculator) {
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
    public BigInteger k(int n, int... permutation) {
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
    public BigInteger unique(int... permutation) {
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
    public BigInteger repetitive(int n, Integer... permutation) {
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
     * Calculates the lexicographical rank of a multiset permutation. Type indices in
     * {@code permutation} refer to positions in {@code frequencies}; their order defines
     * the lexicographical order of the types.
     *
     * @param frequencies multiplicity of each type
     * @param permutation sequence of type indices, using every type exactly its frequency
     * @return the zero-based lexicographical rank
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if the frequencies or permutation are invalid
     */
    public BigInteger multiset(int[] frequencies, int[] permutation) {
        Objects.requireNonNull(frequencies, "frequencies must not be null");
        Objects.requireNonNull(permutation, "permutation must not be null");
        int totalLength = validateFrequencies(frequencies);
        if (permutation.length != totalLength) {
            throw new IllegalArgumentException("Permutation length must equal the sum of frequencies");
        }

        int[] remaining = frequencies.clone();
        int[] actualCounts = new int[frequencies.length];
        for (int type : permutation) {
            if (type < 0 || type >= frequencies.length) {
                throw new IllegalArgumentException("Every type index must be in [0, " + frequencies.length + ")");
            }
            if (++actualCounts[type] > frequencies[type]) {
                throw new IllegalArgumentException("Permutation exceeds the available frequency for type " + type);
            }
        }
        if (!Arrays.equals(actualCounts, frequencies)) {
            throw new IllegalArgumentException("Permutation must use every type exactly its specified frequency");
        }

        BigInteger total = calculator.multinomial(frequencies);
        BigInteger rank = BigInteger.ZERO;
        int left = totalLength;
        for (int selected : permutation) {
            for (int type = 0; type < selected; type++) {
                if (remaining[type] > 0) {
                    rank = rank.add(total.multiply(BigInteger.valueOf(remaining[type]))
                            .divide(BigInteger.valueOf(left)));
                }
            }
            total = total.multiply(BigInteger.valueOf(remaining[selected]))
                    .divide(BigInteger.valueOf(left));
            remaining[selected]--;
            left--;
        }
        return rank;
    }

    private static int validateFrequencies(int[] frequencies) {
        long total = 0;
        for (int frequency : frequencies) {
            if (frequency < 0) throw new IllegalArgumentException("Frequencies must be non-negative");
            total += frequency;
            if (total > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Sum of frequencies exceeds integer capacity");
            }
        }
        return (int) total;
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

}
