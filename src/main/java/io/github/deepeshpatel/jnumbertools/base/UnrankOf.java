/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

/**
 * Entry point for unranking combinatorial objects. Select the mathematical structure
 * first, then the specific kind of permutation or combination.
 *
 * @see PermutationUnrankOfBuilder
 * @see CombinationUnrankOfBuilder
 */
public final class UnrankOf {

    private final Calculator calculator;

    /** Creates an unranking entry point with a default calculator. */
    public UnrankOf() {
        this(new Calculator());
    }

    /**
     * Creates an unranking entry point with the specified calculator.
     *
     * @param calculator calculator used for combinatorial computations
     */
    public UnrankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    /** Returns unranking operations for permutations and derangements. */
    public PermutationUnrankOfBuilder permutation() {
        return new PermutationUnrankOfBuilder(calculator);
    }

    /** Returns unranking operations for combinations. */
    public CombinationUnrankOfBuilder combination() {
        return new CombinationUnrankOfBuilder(calculator);
    }
}
