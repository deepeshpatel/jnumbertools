/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for generating unique combinations of size r from n items.
 * <p>
 * A unique combination is a selection of r items from n distinct items where order does not matter,
 * with the total number of combinations given by ⁿCᵣ = n! / (r! * (n-r)!).
 * This builder supports generating combinations in lexicographical order, sampling combinations
 * randomly with or without replacement, or following a custom sequence of ranks.
 * It uses a {@link Calculator} for computing combination counts and ranks.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class UniqueCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a builder for unique combinations.
     *
     * @param elements   the list of items to generate combinations from (must not be null or empty)
     * @param size       the size of each combination (0 ≤ r ≤ n)
     * @param calculator the calculator for computing combination counts and ranks
     * @throws IllegalArgumentException if size < 0, size > n, or elements is null/empty
     */
    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Creates a generator for all unique combinations in lexicographical order.
     *
     * @return a {@link UniqueCombination} for iterating all ⁿCᵣ combinations
     */
    public UniqueCombination<T> lexOrder() {
        return new UniqueCombination<>(elements, size);
    }

    /**
     * Creates a generator that samples unique combinations randomly without replacement.
     * <p>
     * Uses {@link BigIntegerSample} to ensure each combination in the sample is distinct.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ ⁿCᵣ)
     * @return a {@link UniqueCombinationByRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or sampleSize > ⁿCᵣ
     */
    public UniqueCombinationByRanks<T> sample(int sampleSize) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationByRanks<>(elements, size, new BigIntegerSample(nCr, sampleSize), calculator);
    }

    /**
     * Creates a generator that samples unique combinations randomly with replacement.
     * <p>
     * Uses {@link BigIntegerChoice} to allow duplicate combinations in the sample.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @return a {@link UniqueCombinationByRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0
     */
    public UniqueCombinationByRanks<T> choice(int sampleSize) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationByRanks<>(elements, size, new BigIntegerChoice(nCr, sampleSize), calculator);
    }

    /**
     * Creates a generator for combinations at specified ranks.
     * <p>
     * For example, for ⁿCᵣ with n=3, r=2, ranks [0, 2] might yield [A, B], [B, C].
     * </p>
     *
     * @param ranks the iterable of ranks (each rank in [0, ⁿCᵣ))
     * @return a {@link UniqueCombinationByRanks} for the specified ranks
     * @throws IllegalArgumentException if any rank < 0 or rank ≥ ⁿCᵣ
     */
    public UniqueCombinationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        return new UniqueCombinationByRanks<>(elements, size, ranks, calculator);
    }

    /**
     * Creates a generator for every mᵗʰ combination in lexicographical order, starting from a given rank.
     * <p>
     * Generates ranks: start, start+m, start+2m, ..., up to ⁿCᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿCᵣ)
     * @return a {@link UniqueCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿCᵣ
     */
    public UniqueCombinationByRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a generator for every mᵗʰ combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to ⁿCᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿCᵣ)
     * @return a {@link UniqueCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿCᵣ
     */
    public UniqueCombinationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCr);
        return new UniqueCombinationByRanks<>(elements, size, mthIterable, calculator);
    }

    /**
     * Returns the total number of unique combinations.
     *
     * @return the value of ⁿCᵣ as a {@link BigInteger}
     */
    public BigInteger count() {
        return calculator.nCr(elements.size(), size);
    }

    @Override
    public String toString() {
        return "UniqueCombinationBuilder{" +
                "elements=" + elements +
                ", size=" + size +
                ", count=" + count() +
                '}';
    }
}