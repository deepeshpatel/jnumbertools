/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

/**
 * Entry point for ranking combinatorial objects. Select the mathematical structure
 * first, then the specific kind of permutation or combination.
 *
 * @see PermutationRankOfBuilder
 * @see CombinationRankOfBuilder
 */
public final class RankOf {

    private final Calculator calculator;

    /** Creates a ranking entry point with a default calculator. */
    public RankOf() {
        this(new Calculator());
    }

    /**
     * Creates a ranking entry point with the specified calculator.
     *
     * @param calculator calculator used for combinatorial computations
     */
    public RankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /** Returns ranking operations for permutations and derangements. */
    public PermutationRankOfBuilder permutation() {
        return new PermutationRankOfBuilder(calculator);
    }

    /** Returns ranking operations for combinations. */
    public CombinationRankOfBuilder combination() {
        return new CombinationRankOfBuilder(calculator);
    }
}
