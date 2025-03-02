/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.generator.numbers.NumberToBigIntegerAdapter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for generating unique permutations of a list of elements.
 * <p>
 * This builder supports generating all permutations in lexicographical order, specific mᵗʰ permutations,
 * permutations via single swaps (Heap's algorithm), and a placeholder for a fast unordered method.
 * Instances of permutation generators should be created through this builder.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutationBuilder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a builder for unique permutations.
     *
     * @param calculator the calculator for permutation-related computations
     * @param elements   the list of elements to permute; null results in an empty list
     */
    public UniquePermutationBuilder(Calculator calculator, List<T> elements) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.calculator = calculator;
    }

    /**
     * Generates all unique permutations in lexicographical order.
     *
     * @return a {@link UniquePermutation} instance for all permutations
     */
    public UniquePermutation<T> lexOrder() {
        return new UniquePermutation<>(elements);
    }

    /**
     * Generates every mᵗʰ unique permutation in lexicographical order, using long values.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link UniquePermutationForSequence} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public UniquePermutationForSequence<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ unique permutation in lexicographical order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link UniquePermutationForSequence} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public UniquePermutationForSequence<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationForSequence<>(elements, new EveryMthIterable(start, m, total), calculator);
    }


    /**
     * Generates a sample of unique permutations without replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive and ≤ n!
     * @return a {@link UniquePermutationForSequence} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public UniquePermutationForSequence<T> sample(int sampleSize) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationForSequence<>(elements, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Generates a sample of unique permutations with replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive
     * @return a {@link UniquePermutationForSequence} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public UniquePermutationForSequence<T> choice(int sampleSize) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationForSequence<>(elements, new BigIntegerChoice(total, sampleSize), calculator);
    }

    public UniquePermutationForSequence<T> fromSequence(Iterable<? extends Number> iterable) {
        Iterable<BigInteger> adapter = new NumberToBigIntegerAdapter(iterable);
        return new UniquePermutationForSequence<>(elements, adapter, calculator);
    }

    /**
     * Generates unique permutations using single swaps (Heap's algorithm).
     *
     * @return a {@link UniquePermutationSingleSwap} instance using single-swap generation
     */
    public UniquePermutationSingleSwap<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

}