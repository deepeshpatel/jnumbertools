/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for generating repetitive combinations of size r from n items.
 * <p>
 * A repetitive combination allows items to be selected multiple times, with the total number of
 * combinations given by ⁿ⁺ᵣ⁻¹Cᵣ = (n+r-1)! / (r! * (n-1)!). Order does not matter, and elements
 * are treated as distinct based on their indices in the input list. This builder supports generating
 * all combinations in lexicographical order, sampling randomly with or without replacement, selecting
 * every mᵗʰ combination, or using custom rank sequences.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class RepetitiveCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a builder for repetitive combinations.
     *
     * @param elements   the list of n items to generate combinations from (must not be null or empty)
     * @param size       the size of each combination (r ≥ 0)
     * @param calculator the calculator for computing combinatorial values
     * @throws IllegalArgumentException if r < 0 or elements is null/empty
     */
    public RepetitiveCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Creates a generator for all repetitive combinations in lexicographical order.
     *
     * @return a {@link RepetitiveCombination} for iterating all ⁿ⁺ᵣ⁻¹Cᵣ combinations
     */
    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }

    /**
     * Creates a generator that samples repetitive combinations randomly without replacement.
     * <p>
     * Uses {@link BigIntegerSample} to ensure each combination in the sample is distinct.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ ⁿ⁺ᵣ⁻¹Cᵣ)
     * @return a {@link RepetitiveCombinationOfRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or sampleSize > ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationOfRanks<T> sample(int sampleSize) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationOfRanks<>(elements, size, new BigIntegerSample(nCrRepetitive, sampleSize), calculator);
    }

    /**
     * Creates a generator that samples repetitive combinations randomly with replacement.
     * <p>
     * Uses {@link BigIntegerChoice} to allow duplicate combinations in the sample.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @return a {@link RepetitiveCombinationOfRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0
     */
    public RepetitiveCombinationOfRanks<T> choice(int sampleSize) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationOfRanks<>(elements, size, new BigIntegerChoice(nCrRepetitive, sampleSize), calculator);
    }

    /**
     * Creates a generator for every mᵗʰ repetitive combination in lexicographical order, starting from a given rank.
     * <p>
     * Generates ranks: start, start+m, start+2m, ..., up to ⁿ⁺ᵣ⁻¹Cᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿ⁺ᵣ⁻¹Cᵣ)
     * @return a {@link RepetitiveCombinationOfRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationOfRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a generator for every mᵗʰ repetitive combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to ⁿ⁺ᵣ⁻¹Cᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿ⁺ᵣ⁻¹Cᵣ)
     * @return a {@link RepetitiveCombinationOfRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationOfRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCrRepetitive);
        return new RepetitiveCombinationOfRanks<>(elements, size, mthIterable, calculator);
    }

    /**
     * Creates a generator for repetitive combinations based on a custom sequence of ranks.
     *
     * @param ranks the iterable of ranks (each rank in [0, ⁿ⁺ᵣ⁻¹Cᵣ))
     * @return a {@link RepetitiveCombinationOfRanks} for the custom sequence
     * @throws IllegalArgumentException if any rank < 0 or rank ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationOfRanks<T> withSequence(Iterable<BigInteger> ranks) {
        return new RepetitiveCombinationOfRanks<>(elements, size, ranks, calculator);
    }

    /**
     * Returns the total number of repetitive combinations.
     *
     * @return the value of ⁿ⁺ᵣ⁻¹Cᵣ as a {@link BigInteger}
     */
    public BigInteger count() {
        // No need to cache the count here as nCr values are cached in the calculator.
        return calculator.nCrRepetitive(elements.size(), size);
    }

    @Override
    public String toString() {
        return "RepetitiveCombinationBuilder{" +
                "elements=" + elements +
                ", size=" + size +
                ", count=" + count() +
                '}';
    }
}