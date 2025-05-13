/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder class for generating various types of unique permutations of input elements.
 * Provides multiple permutation generation strategies:
 * <ul>
 *   <li>Lexicographical order (all permutations)</li>
 *   <li>Every mᵗʰ permutation in lexicographical order</li>
 *   <li>Random sampling of permutations</li>
 *   <li>Permutations at specific ranks</li>
 *   <li>Single-swap permutations (Heap's algorithm)</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Generate all permutations of [A, B, C] in lex order
 * new UniquePermutationBuilder<>(calculator, Arrays.asList("A", "B", "C"))
 *     .lexOrder()
 *     .forEach(System.out::println);
 * }</pre>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 * @see UniquePermutation
 * @see UniquePermutationByRanks
 * @see UniquePermutationSingleSwap
 */
public final class UniquePermutationBuilder<T> implements Builder<T> {

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
     * @return a {@link UniquePermutationByRanks} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public UniquePermutationByRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ unique permutation in lexicographical order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link UniquePermutationByRanks} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public UniquePermutationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new EveryMthIterable(start, m, total), calculator);
    }


    /**
     * Generates a sample of unique permutations without replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive and ≤ n!
     * @return a {@link UniquePermutationByRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public UniquePermutationByRanks<T> sample(int sampleSize) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Generates a sample of unique permutations with replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive
     * @return a {@link UniquePermutationByRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public UniquePermutationByRanks<T> choice(int sampleSize) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new BigIntegerChoice(total, sampleSize), calculator);
    }

    /**
     * Generates permutations at specified lexicographical rank positions.
     * <p>
     * <b>Example for 3 elements:</b>
     * <pre>
     * Rank | Permutation
     * -----|------------
     * 0    | [A, B, C]
     * 1    | [A, C, B]
     * 2    | [B, A, C]
     * 3    | [B, C, A]
     * 4    | [C, A, B]
     * 5    | [C, B, A]
     *
     * byRanks([0, 2, 4]) → [A, B, C], [B, A, C], [C, A, B]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < n!)
     * @return Permutation generator for specified ranks
     * @throws IllegalArgumentException if any rank ≥ n!
     * @throws IllegalStateException if no generation strategy was selected
     */
    public UniquePermutationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        return new UniquePermutationByRanks<>(elements, ranks, calculator);
    }

    /**
     * Generates unique permutations using single swaps (Heap's algorithm).
     *
     * @return a {@link UniquePermutationSingleSwap} instance using single-swap generation
     */
    public UniquePermutationSingleSwap<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

    public BigInteger count() {
        return calculator.factorial(elements.size());
    }
}