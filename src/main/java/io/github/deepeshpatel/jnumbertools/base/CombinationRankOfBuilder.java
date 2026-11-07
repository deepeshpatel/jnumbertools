/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Ranks combinations in lexicographical order.
 */
public final class CombinationRankOfBuilder {

    private final Calculator calculator;

    /**
     * Constructs a new CombinationRankOfBuilder instance with a default Calculator.
     */
    public CombinationRankOfBuilder() {
        this(new Calculator());
    }

    /**
     * Constructs a new CombinationRankOfBuilder instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public CombinationRankOfBuilder(Calculator calculator) {
        this.calculator = calculator;
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
    public BigInteger unique(int n, int... combination) {
        validateCombination(n, combination);
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }

    /**
     * Calculates the lexicographical rank of a repetitive combination (multicombination).
     * The combination must be nondecreasing and contain indices in {@code [0, n)}.
     *
     * @param n the number of distinct element types
     * @param combination the nondecreasing indices, with repetition allowed
     * @return the zero-based lexicographical rank
     * @throws NullPointerException if the combination is null
     * @throws IllegalArgumentException if n is negative or an index is out of range or out of order
     */
    public BigInteger repetitive(int n, int... combination) {
        Objects.requireNonNull(combination, "combination must not be null");
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        int previous = -1;
        for (int value : combination) {
            if (value < 0 || value >= n || value < previous) {
                throw new IllegalArgumentException("Combination must be nondecreasing with values in [0, " + n + ")");
            }
            previous = value;
        }
        BigInteger rank = BigInteger.ZERO;
        int current = 0;
        for (int i = 0; i < combination.length; i++) {
            for (int candidate = current; candidate < combination[i]; candidate++) {
                rank = rank.add(calculator.nCrRepetitive(n - candidate, combination.length - i - 1));
            }
            current = combination[i];
        }
        return rank;
    }

    /**
     * Calculates the lexicographical rank of a multiset combination. The combination is
     * represented by a count vector aligned with {@code frequencies}. The ordering matches
     * {@code MultisetCombinationBuilder.byRanks}: larger counts of earlier types come first.
     *
     * @param frequencies available multiplicity of each type
     * @param combination selected multiplicity of each type
     * @return the zero-based lexicographical rank
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if the vectors are incompatible or contain invalid counts
     */
    public BigInteger multiset(int[] frequencies, int[] combination) {
        Objects.requireNonNull(frequencies, "frequencies must not be null");
        Objects.requireNonNull(combination, "combination must not be null");
        validateFrequencies(frequencies);
        if (combination.length != frequencies.length) {
            throw new IllegalArgumentException("Combination count vector length must equal frequencies length");
        }
        int r = 0;
        for (int i = 0; i < combination.length; i++) {
            if (combination[i] < 0 || combination[i] > frequencies[i]) {
                throw new IllegalArgumentException("Combination count at index " + i
                        + " must be in [0, " + frequencies[i] + "]");
            }
            r = Math.addExact(r, combination[i]);
        }

        BigInteger[][] ways = buildMultisetCombinationWays(frequencies, r);
        BigInteger rank = BigInteger.ZERO;
        int remaining = r;
        for (int i = 0; i < frequencies.length; i++) {
            int actual = combination[i];
            int max = Math.min(frequencies[i], remaining);
            for (int x = max; x > actual; x--) {
                int need = remaining - x;
                BigInteger block = need < ways[i + 1].length ? ways[i + 1][need] : BigInteger.ZERO;
                rank = rank.add(block);
            }
            remaining -= actual;
        }
        if (remaining != 0) throw new IllegalArgumentException("Combination count vector is invalid");
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

    private static BigInteger[][] buildMultisetCombinationWays(int[] frequencies, int r) {
        BigInteger[][] ways = new BigInteger[frequencies.length + 1][];
        ways[frequencies.length] = new BigInteger[]{BigInteger.ONE};
        long suffixSum = 0;
        for (int i = frequencies.length - 1; i >= 0; i--) {
            suffixSum += frequencies[i];
            int length = (int) Math.min((long) r, suffixSum) + 1;
            BigInteger[] below = ways[i + 1];
            BigInteger[] row = new BigInteger[length];
            BigInteger window = BigInteger.ZERO;
            int frequency = frequencies[i];
            for (int j = 0; j < length; j++) {
                if (j < below.length) window = window.add(below[j]);
                int drop = j - frequency - 1;
                if (drop >= 0 && drop < below.length) window = window.subtract(below[drop]);
                row[j] = window;
            }
            ways[i] = row;
        }
        return ways;
    }

    // ── Input validation ────────────────────────────────────────────────────────────────────────
    // These helpers are the user-facing entry points, so they own the validation described in
    // ValidationRules.md ("RANK / UNRANK HELPERS"). The numbersystem classes they delegate to assume valid input.

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
