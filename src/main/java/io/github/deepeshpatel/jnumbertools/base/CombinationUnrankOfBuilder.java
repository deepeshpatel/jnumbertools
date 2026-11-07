/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.*;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Unranks combinations in lexicographical order.
 */
public final class CombinationUnrankOfBuilder {

    private final Calculator calculator;

    /**
     * Constructs a new CombinationUnrankOfBuilder instance with a default Calculator.
     */
    public CombinationUnrankOfBuilder() {
        this(new Calculator());
    }

    /**
     * Constructs a new CombinationUnrankOfBuilder instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public CombinationUnrankOfBuilder(Calculator calculator) {
        this.calculator = calculator;
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
    public int[] unique(BigInteger rank, int n, int r) {
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
     * Determines the repetitive combination at a given lexicographical rank.
     * The unranking is delegated to the repetitive-combination builder's rank-based generator.
     *
     * @param rank the zero-based lexicographical rank
     * @param n the number of distinct element types
     * @param r the size of the combination
     * @return the nondecreasing combination as indices in {@code [0, n)}
     * @throws NullPointerException if rank is null
     * @throws IllegalArgumentException if n or r is negative, or rank is out of range
     */
    public int[] repetitive(BigInteger rank, int n, int r) {
        Objects.requireNonNull(rank, "rank must not be null");
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (r < 0) throw new IllegalArgumentException("r must be non-negative");
        List<Integer> result = new Combinations(calculator).repetitive(n, r)
                .byRanks(List.of(rank)).iterator().next();
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Determines the multiset combination at a given lexicographical rank. The returned
     * array is a count vector aligned with {@code frequencies}.
     * Unranking is delegated to the multiset-combination builder's rank-based generator.
     *
     * @param rank the zero-based lexicographical rank
     * @param frequencies available multiplicity of each type
     * @param r the number of items to select
     * @return selected multiplicity of each type
     * @throws NullPointerException if rank or frequencies is null
     * @throws IllegalArgumentException if frequencies or r are invalid, or rank is out of range
     */
    public int[] multiset(BigInteger rank, int[] frequencies, int r) {
        Objects.requireNonNull(rank, "rank must not be null");
        Objects.requireNonNull(frequencies, "frequencies must not be null");
        LinkedHashMap<Integer, Integer> options = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) options.put(i, frequencies[i]);
        Map<Integer, Integer> selected = new Combinations(calculator).multiset(options, r)
                .byRanks(List.of(rank)).iterator().next();
        int[] result = new int[frequencies.length];
        selected.forEach((type, count) -> result[type] = count);
        return result;
    }

}
