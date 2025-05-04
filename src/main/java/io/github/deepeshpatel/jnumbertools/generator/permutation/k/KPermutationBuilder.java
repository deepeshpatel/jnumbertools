/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for generating k-permutations of a list of elements.
 * <p>
 * This builder provides methods to generate k-permutations in lexicographical or combination order,
 * as well as to retrieve specific mᵗʰ permutations in either order. For example, for elements [A, B, C]
 * and k=2, permutations might include [A, B], [B, A], [A, C], etc., depending on the order.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationBuilder<T> {

    private final List<T> elements;
    private final int r;
    private final Calculator calculator;

    /**
     * Constructs a builder for k-permutations.
     *
     * @param elements   the list of elements to permute (e.g., [A, B, C])
     * @param r          the size of each permutation (k); must be non-negative and ≤ elements.size()
     * @param calculator utility for combinatorial calculations
     */
    public KPermutationBuilder(List<T> elements, int r, Calculator calculator) {
        this.elements = elements;
        this.r = r;
        this.calculator = calculator;
    }

    /**
     * Generates all k-permutations in lexicographical order.
     *
     * @return a {@link KPermutationLexOrder} instance for lexicographical iteration
     */
    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(elements, r);
    }

    public KPermutationForSequence<T> choice(int sampleSize) {
        BigInteger max = calculator.nPr(elements.size(), r);
        var choice = new BigIntegerChoice(max, sampleSize);
        return new KPermutationForSequence<>(elements, r, choice, calculator);
    }

    public KPermutationForSequence<T> sample(int sampleSize) {
        BigInteger max = calculator.nPr(elements.size(), r);
        var sample = new BigIntegerSample(max, sampleSize);
        return new KPermutationForSequence<>(elements, r, sample, calculator);
    }

    public KPermutationForSequence<T>  fromSequence(Iterable<BigInteger> iterable) {
        return new KPermutationForSequence<>(elements ,r ,iterable, calculator);
    }

    /**
     * Generates all k-permutations in combination order.
     *
     * @return a {@link KPermutationCombinationOrder} instance for combination-order iteration
     */
    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(elements, r);
    }

    /**
     * Generates every mᵗʰ k-permutation in lexicographical order, using long values.
     *
     * @param m     the increment between permutations (e.g., m=2 for every 2nd permutation); must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationForSequence} instance for mᵗʰ permutations in lex order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationForSequence<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ k-permutation in lexicographical order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationForSequence} instance for mᵗʰ permutations in lex order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationForSequence<T> lexOrderMth(BigInteger m, BigInteger start) {
        EveryMthIterable iterator = new EveryMthIterable(start, m, calculator.nPr(elements.size(), r));
        return new KPermutationForSequence<>(elements, r, iterator, calculator);
        //return new KPermutationLexOrderMth<>(elements, r, m, start, calculator);
    }

    /**
     * Generates every mᵗʰ k-permutation in combination order, using long values.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationCombinationOrderMth} instance for mᵗʰ permutations in combination order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m, long start) {
        return combinationOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ k-permutation in combination order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationCombinationOrderMth} instance for mᵗʰ permutations in combination order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationCombinationOrderMth<>(elements, r, m, start, calculator);
    }
}