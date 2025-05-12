/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for generating repetitive permutations of a specified width from a list of elements.
 * <p>
 * This builder creates {@link RepetitivePermutationOfRanks} instances for lexicographical order (all or mᵗʰ),
 * sampling with/without replacement, and custom rank sequences. For example:
 * <pre>
 * List<String> elements = Arrays.asList("A", "B");
 * RepetitivePermutationBuilder<String> builder = new RepetitivePermutationBuilder<>(2, elements, calculator);
 * List<List<String>> perms = builder.lexOrder().stream().toList();
 * // Generates: [[A, A], [A, B], [B, A], [B, B]]
 * </pre>
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 */
public final class RepetitivePermutationBuilder<T> {

    private final int width;
    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a builder for repetitive permutations.
     *
     * @param width      the length of each permutation; must be non-negative
     * @param elements   the list of elements to permute; null results in an empty list
     * @param calculator utility for permutation computations
     */
    public RepetitivePermutationBuilder(int width, List<T> elements, Calculator calculator) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.width = width;
        this.calculator = calculator;
    }

    /**
     * Generates all repetitive permutations in lexicographical order.
     *
     * @return a {@link RepetitivePermutation} instance for all permutations
     */
    public RepetitivePermutation<T> lexOrder() {
        return new RepetitivePermutation<>(elements, width);
    }

    /**
     * Generates every mᵗʰ repetitive permutation in lexicographical order, using long values.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link RepetitivePermutationMth} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public RepetitivePermutationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ repetitive permutation in lexicographical order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link RepetitivePermutationMth} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public RepetitivePermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new RepetitivePermutationMth<>(elements, width, m, start);
    }

    /**
     * Generates a sample of repetitive permutations without replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive and ≤ nᵂ
     * @return a {@link RepetitivePermutationOfRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public RepetitivePermutationOfRanks<T> sample(int sampleSize) {
        BigInteger total = calculator.power(elements.size(), width);
        return new RepetitivePermutationOfRanks<>(elements, width, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Generates a sample of repetitive permutations with replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive
     * @return a {@link RepetitivePermutationOfRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public RepetitivePermutationOfRanks<T> choice(int sampleSize) {
        BigInteger total = calculator.power(elements.size(), width);
        return new RepetitivePermutationOfRanks<>(elements, width, new BigIntegerChoice(total, sampleSize), calculator);
    }

    /**
     * Generates permutations with repetition at specified rank positions.
     * <p>
     * Each rank corresponds to a unique base-n number representation where
     * n is the input size and k is the permutation length.
     * </p>
     * <p>
     * <b>Example for [A, B], k=3:</b>
     * <pre>
     * Rank | Base-2 | Permutation
     * -----|--------|------------
     * 0    | 000    | [A, A, A]
     * 1    | 001    | [A, A, B]
     * 2    | 010    | [A, B, A]
     * 3    | 011    | [A, B, B]
     * 4    | 100    | [B, A, A]
     * 5    | 101    | [B, A, B]
     * 6    | 110    | [B, B, A]
     * 7    | 111    | [B, B, B]
     *
     * atRanks([0, 7]) → [A, A, A], [B, B, B]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < nᵏ)
     * @return Permutation generator for specified ranks
     * @throws IllegalArgumentException if any rank ≥ nᵏ
     * @throws IllegalStateException if length k was not configured
     */
    public RepetitivePermutationOfRanks<T> ofRanks(Iterable<BigInteger> ranks) {
        return new RepetitivePermutationOfRanks<>(elements, width, ranks, calculator);
    }
}