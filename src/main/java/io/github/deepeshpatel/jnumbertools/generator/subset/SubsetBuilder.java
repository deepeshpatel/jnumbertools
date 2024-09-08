/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;

/**
 * A builder for creating subsets from a list of elements.
 * <p>
 * The {@link SubsetBuilder} class provides methods to define a range of subset sizes and generate subsets in lexicographical order.
 * It supports creating subsets of all sizes or subsets within a specific range, and it can generate every m<sup>th</sup> subset starting from a given position.
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
    private long count = -1;

    /**
     * Constructs a SubsetBuilder with the specified elements and calculator.
     *
     * @param elements the list of elements to generate subsets from
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
     * Configures the builder to generate subsets within a specified range.
     *
     * @param from the minimum size of subsets to generate (inclusive)
     * @param to the maximum size of subsets to generate (inclusive)
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
     * @return a SubsetGenerator instance
     * @throws IllegalArgumentException if range is not specified using {@link #inRange(int, int)} or {@link #all()}
     */
    public SubsetGenerator<T> lexOrder() {
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Must specify range of subsets via method inRange() or all()");
        }
        return new SubsetGenerator<>(elements, from, to);
    }

    /**
     * Creates a {@link SubsetGeneratorMth} that generates every m<sup>th</sup> subset in lexicographical order, starting from the specified position.
     *
     * @param m the interval for selecting subsets (every m<sup>th</sup> subset)
     * @param start the starting position for subsets
     * @return a SubsetGeneratorMth instance
     */
    public SubsetGeneratorMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a {@link SubsetGeneratorMth} that generates every m<sup>th</sup> subset in lexicographical order, starting from the specified position.
     *
     * @param m the interval for selecting subsets (every m<sup>th</sup> subset)
     * @param start the starting position for subsets
     * @return a SubsetGeneratorMth instance
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    /**
     * Returns the total number of subsets in the specified range.
     * <p>
     * This method calculates the total number of subsets based on the range defined by {@link #inRange(int, int)} or {@link #all()}.
     * </p>
     *
     * @return the count of subsets
     */
    public synchronized long count() {
        if (count == -1) {
            count = calculator.totalSubsetsInRange(from, to, elements.size()).longValue();
        }
        return count;
    }
}
