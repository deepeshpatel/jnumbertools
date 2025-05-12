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
 * Builder for generating unique combinations of elements with various sampling strategies.
 * <p>
 * This builder provides methods to generate combinations in lexicographical order, sample combinations
 * randomly with or without replacement, or follow a custom sequence of ranks. It leverages a
 * {@link Calculator} to compute combination counts and supports flexible rank sequences via
 * {@link UniqueCombinationOfRanks}.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniqueCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a new builder for unique combinations.
     *
     * @param elements   the list of elements from which combinations are generated
     * @param size       the size of each combination (r); must be non-negative and ≤ elements.size()
     * @param calculator the calculator used for computing combination counts and ranks
     */
    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Creates an instance that generates all unique combinations in lexicographical order.
     *
     * @return a {@link UniqueCombination} instance for full lexicographical iteration
     */
    public UniqueCombination<T> lexOrder() {
        return new UniqueCombination<>(elements, size);
    }

    /**
     * Creates an instance that samples unique combinations randomly without replacement.
     * <p>
     * Uses {@link BigIntegerSample} to ensure each combination in the sample is distinct.
     * </p>
     *
     * @param sampleSize the number of combinations to generate; must be positive and ≤ nCr
     * @return a {@link UniqueCombinationOfRanks} instance for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize is invalid or exceeds total combinations
     */
    public UniqueCombinationOfRanks<T> sample(int sampleSize) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationOfRanks<>(elements, size, new BigIntegerSample(nCr, sampleSize), calculator);
    }

    /**
     * Creates an instance that samples unique combinations randomly with replacement.
     * <p>
     * Uses {@link BigIntegerChoice} to allow duplicate combinations across the sample.
     * </p>
     *
     * @param sampleSize the number of combinations to generate; must be positive
     * @return a {@link UniqueCombinationOfRanks} instance for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize is not positive
     */
    public UniqueCombinationOfRanks<T> choice(int sampleSize) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationOfRanks<>(elements, size, new BigIntegerChoice(nCr, sampleSize), calculator);
    }

    /**
     * Generates combinations at the specified combinatorial rank positions.
     * <p>
     * Example for C(3,2) (3 total combinations):
     * <pre>
     * atRanks([0, 2]) → [A,B], [A,C]
     * </pre>
     *
     * @throws IllegalArgumentException if rank ≥ C(n,k)
     */
    public UniqueCombinationOfRanks<T> ofRank(Iterable<BigInteger> iterable) {
        return new UniqueCombinationOfRanks<>(elements, size , iterable, calculator);
    }

    /**
     * Creates an instance that generates every mᵗʰ combination in lexicographical order starting from a given rank,
     * using long values for convenience.
     *
     * @param m      the increment between combination ranks; must be positive
     * @param start  the starting rank (inclusive); must be non-negative and less than nCr
     * @return a {@link UniqueCombinationOfRanks} instance for lexicographical sequence
     * @throws IllegalArgumentException if m or start is invalid
     * @see #lexOrderMth(BigInteger, BigInteger)
     */
    public UniqueCombinationOfRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates an instance that generates every mᵗʰ combination in lexicographical order starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce a sequence of ranks: start, start+m, start+2m, etc., up to nCr.
     * </p>
     *
     * @param m      the increment between combination ranks; must be positive
     * @param start  the starting rank (inclusive); must be non-negative and less than nCr
     * @return a {@link UniqueCombinationOfRanks} instance for lexicographical sequence
     * @throws IllegalArgumentException if m or start is invalid
     */
    public UniqueCombinationOfRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCr);
        return new UniqueCombinationOfRanks<>(elements, size, mthIterable, calculator);
    }

    /**
     * Creates an instance that generates combinations based on a custom sequence of ranks.
     *
     * @param ranks the iterable providing the sequence of ranks; each rank must be in [0, nCr)
     * @return a {@link UniqueCombinationOfRanks} instance for custom sequence
     * @throws IllegalArgumentException if ranks contains invalid values
     */
    public UniqueCombinationOfRanks<T> withSequence(Iterable<BigInteger> ranks) {
        return new UniqueCombinationOfRanks<>(elements, size, ranks, calculator);
    }

    /**
     * Returns the total number of possible unique combinations (nCr).
     * <p>
     * Computed using the combination formula, leveraging cached values in the calculator for efficiency.
     * </p>
     *
     * @return the total number of unique combinations as a {@link BigInteger}
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