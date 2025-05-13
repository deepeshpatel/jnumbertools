/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for generating k-permutations of a list of elements.
 * <p>
 * This builder provides methods to generate k-permutations (ordered subsets of size kₖ from nₙ elements)
 * in lexicographical or combination order, as well as to retrieve specific mᵗʰ permutations or permutations
 * at specified ranks. The total number of k-permutations is given by Pₙ,ₖ = n!/(n−kₖ)!. For example, for
 * elements [A, B, C] and kₖ=2, permutations might include [A, B], [B, A], [A, C], etc., depending on the order.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Generate 2-length permutations of [A, B, C] in lex order
 * new KPermutationBuilder<>(Arrays.asList("A", "B", "C"), 2, calculator)
 *     .lexOrder()
 *     .iterator();
 * // Generate specific ranks in lex order
 * new KPermutationBuilder<>(Arrays.asList("A", "B", "C"), 2, calculator)
 *     .byRank(Arrays.asList(BigInteger.ZERO, BigInteger.valueOf(2)))
 *     .iterator();
 * }</pre>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public final class KPermutationBuilder<T> {

    private final List<T> elements;
    private final int k;
    private final Calculator calculator;

    /**
     * Constructs a builder for k-permutations.
     *
     * @param elements the list of elements to permute (e.g., [A, B, C])
     * @param k the size of each permutation (kₖ); must be non-negative and ≤ nₙ
     * @param calculator utility for combinatorial calculations
     * @return a new KPermutationBuilder instance
     * @throws IllegalArgumentException if kₖ is negative or exceeds nₙ
     */
    public KPermutationBuilder(List<T> elements, int k, Calculator calculator) {
        this.elements = elements;
        this.k = k;
        this.calculator = calculator;
    }

    /**
     * Generates all k-permutations in lexicographical order.
     *
     * @return a {@link KPermutationLexOrder} instance for lexicographical iteration
     */
    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(elements, k);
    }

    /**
     * Generates k-permutations randomly with replacement.
     * <p>
     * The total number of k-permutations is Pₙ,ₖ = n!/(n−kₖ)!.
     * </p>
     *
     * @param sampleSize the number of permutations to generate; must be positive
     * @return a {@link KPermutationByRanks} instance for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize is not positive
     */
    public KPermutationByRanks<T> choice(int sampleSize) {
        BigInteger max = calculator.nPr(elements.size(), k);
        var choice = new BigIntegerChoice(max, sampleSize);
        return new KPermutationByRanks<>(elements, k, choice, calculator);
    }

    /**
     * Generates k-permutations randomly without replacement.
     * <p>
     * The total number of k-permutations is Pₙ,ₖ = n!/(n−kₖ)!.
     * </p>
     *
     * @param sampleSize the number of permutations to generate; must be positive and ≤ Pₙ,ₖ
     * @return a {@link KPermutationByRanks} instance for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize is not positive or exceeds Pₙ,ₖ
     */
    public KPermutationByRanks<T> sample(int sampleSize) {
        BigInteger max = calculator.nPr(elements.size(), k);
        var sample = new BigIntegerSample(max, sampleSize);
        return new KPermutationByRanks<>(elements, k, sample, calculator);
    }

    /**
     * Generates k-permutations at specified lexicographical ranks.
     *
     * @param ranks an iterable of 0-based rank numbers (0 ≤ rank < Pₙ,ₖ)
     * @return a {@link KPermutationByRanks} instance for generating k-permutations at specified ranks
     * @throws IllegalArgumentException if any rank is negative or ≥ Pₙ,ₖ
     */
    public KPermutationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        return new KPermutationByRanks<>(elements, k, ranks, calculator);
    }

    /**
     * Generates all k-permutations in combination order.
     *
     * @return a {@link KPermutationCombinationOrder} instance for combination-order iteration
     */
    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(elements, k);
    }

    /**
     * Generates every mᵗʰ k-permutation in lexicographical order, using long values.
     *
     * @param m the step size for selecting every mᵗʰ permutation; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationByRanks} instance for mᵗʰ permutations in lex order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationByRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ k-permutation in lexicographical order.
     *
     * @param m the step size for selecting every mᵗʰ permutation; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationByRanks} instance for mᵗʰ permutations in lex order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        EveryMthIterable iterator = new EveryMthIterable(start, m, calculator.nPr(elements.size(), k));
        return new KPermutationByRanks<>(elements, k, iterator, calculator);
        //return new KPermutationLexOrderMth<>(elements, k, m, start, calculator);
    }

    /**
     * Generates every mᵗʰ k-permutation in combination order, using long values.
     *
     * @param m the step size for selecting every mᵗʰ permutation; must be positive
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
     * @param m the step size for selecting every mᵗʰ permutation; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link KPermutationCombinationOrderMth} instance for mᵗʰ permutations in combination order
     * @throws IllegalArgumentException if m or start is invalid
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationCombinationOrderMth<>(elements, k, m, start, calculator);
    }
}