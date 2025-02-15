/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;

/**
 * A builder for creating subsets from a list of elements.
 * <p>
 * The {@code SubsetBuilder} class provides methods to define a range of subset sizes and to generate subsets
 * in lexicographical order. It supports creating all subsets or subsets within a specified size range, and it
 * can generate every mᵗʰ subset starting from a given position.
 * </p>
 * <p>
 * <strong>Note:</strong> This builder is intended to be used to construct subset generators, and its associated
 * generator classes (which extend {@code AbstractGenerator}) should be obtained via this builder.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @since 1.0.3
 * @author Deepesh Patel
 */
public class SubsetBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final Calculator calculator;
    private int from = -1;
    private int to = -1;
    private BigInteger count = BigInteger.valueOf(-1);

    /**
     * Constructs a {@code SubsetBuilder} with the specified elements and calculator.
     *
     * @param elements   the list of elements from which subsets will be generated
     * @param calculator a calculator used for computing subset counts
     */
    public SubsetBuilder(List<T> elements, Calculator calculator) {
        this.elements = elements;
        this.calculator = calculator;
    }

    /**
     * Configures the builder to generate all subsets of the given elements.
     *
     * @return this builder instance
     */
    public SubsetBuilder<T> all() {
        this.from = 0;
        this.to = elements.size();
        return this;
    }

    /**
     * Configures the builder to generate subsets within a specified size range.
     *
     * @param from the minimum size of subsets to generate (inclusive)
     * @param to   the maximum size of subsets to generate (inclusive)
     * @return this builder instance
     * @throws IllegalArgumentException if {@code to} is less than {@code from}
     */
    public SubsetBuilder<T> inRange(int from, int to) {
        if (to < from) {
            throw new IllegalArgumentException("parameter 'to' must be greater than or equal to parameter 'from'");
        }
        this.from = from;
        this.to = to;
        return this;
    }

    /**
     * Creates a {@link SubsetGenerator} that generates subsets in lexicographical order within the specified range.
     *
     * @return a {@code SubsetGenerator} instance
     * @throws IllegalArgumentException if the range is not specified using {@link #inRange(int, int)} or {@link #all()}
     */
    public SubsetGenerator<T> lexOrder() {
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Must specify range of subsets via method inRange() or all()");
        }
        return new SubsetGenerator<>(elements, from, to);
    }

    /**
     * Creates a {@link SubsetGeneratorMth} that generates every mᵗʰ subset in lexicographical order, starting from the specified position.
     *
     * @param m     the interval for selecting subsets (every mᵗʰ subset)
     * @param start the starting position for subsets
     * @return a {@code SubsetGeneratorMth} instance
     */
    public SubsetGeneratorMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a {@link SubsetGeneratorMth} that generates every mᵗʰ subset in lexicographical order, starting from the specified position.
     *
     * @param m     the interval for selecting subsets (every mᵗʰ subset) as a {@link BigInteger}
     * @param start the starting position for subsets as a {@link BigInteger}
     * @return a {@code SubsetGeneratorMth} instance
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    /**
     * Returns the total number of subsets in the specified range.
     * <p>
     * This method calculates the total number of subsets based on the range defined by {@link #inRange(int, int)}
     * or {@link #all()}.
     * </p>
     *
     * @return the count of subsets
     */
    public synchronized BigInteger count() {
        if (count.intValue() == -1) {
            count = calculator.totalSubsetsInRange(from, to, elements.size());
        }
        return count;
    }

    @Override
    public String toString() {
        return "SubsetBuilder{" +
                "elements=" + elements +
                ", from=" + from +
                ", to=" + to +
                ", count=" + count +
                '}';
    }
}
