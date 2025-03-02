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
 * A builder class for generating repetitive combinations of a specified size from a list of elements.
 * <p>
 * Given an input list of elements (e.g., elements₀, elements₁, …, elementsₙ₋₁), this class provides methods to:
 * <ul>
 *   <li>Generate all repetitive combinations in lexicographic order.</li>
 *   <li>Retrieve the mᵗʰ lexicographic repetitive combination (with m being 0-based).</li>
 *   <li>Count the total number of possible repetitive combinations.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of elements in the combinations
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class RepetitiveCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a {@code RepetitiveCombinationBuilder} with the specified elements, combination size, and calculator.
     *
     * @param elements   the list of elements (e.g., elements₀, elements₁, …, elementsₙ₋₁) from which combinations will be generated
     * @param size       the size (r) of the combinations to generate
     * @param calculator the calculator to use for computing combinatorial values
     */
    public RepetitiveCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Generates all repetitive combinations in lexicographic order.
     *
     * @return a {@code RepetitiveCombination} instance for iterating over all combinations
     */
    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }

    public RepetitiveCombinationForSequence<T> sample(int sampleSize) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationForSequence<>(elements, size, new BigIntegerSample(nCrRepetitive, sampleSize), calculator);
    }

    public RepetitiveCombinationForSequence<T> choice(int sampleSize) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationForSequence<>(elements, size, new BigIntegerChoice(nCrRepetitive, sampleSize), calculator);
    }

    public RepetitiveCombinationForSequence<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public RepetitiveCombinationForSequence<T> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCrRepetitive);
        return new RepetitiveCombinationForSequence<>(elements, size, mthIterable, calculator);
    }

    public RepetitiveCombinationForSequence<T> withSequence(Iterable<BigInteger> ranks) {
        return new RepetitiveCombinationForSequence<>(elements, size, ranks, calculator);
    }

    /**
     * Returns the total number of possible repetitive combinations.
     * <p>
     * The total is computed using the repetitive combination formula (multichoose), where n is the number
     * of elements in the input list and r is the size of each combination. The result is cached in the
     * calculator for efficiency.
     * </p>
     *
     * @return the total number of combinations as a {@link BigInteger}
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
