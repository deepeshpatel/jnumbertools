/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates subsets of a given set of elements.
 * <p>
 * A subset is a selection of 0 to n elements from a set, where order does not matter.
 * For a set of n elements, there are 2ⁿ possible subsets. This class creates a
 * SubsetBuilder to generate subsets in lexicographical order, output as lists,
 * supporting combinatorial analysis.
 * </p>
 * Example usage:
 * <pre>
 * Subsets subsets = new Subsets();
 * List<List<Integer>> result = subsets.of(Arrays.asList(1, 2, 3)).getAllSubsets();
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class Subsets {

    private final Calculator calculator;

    /**
     * Constructs a new Subsets instance with a default Calculator.
     */
    public Subsets() {
        this(new Calculator());
    }

    /**
     * Constructs a new Subsets instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public Subsets(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a SubsetBuilder for a varargs set of elements.
     * <p>
     * Generates all 2ⁿ subsets of the input set in lexicographical order, where n is
     * the number of elements, using a SubsetBuilder to configure output.
     * </p>
     *
     * @param elements the elements to build subsets from
     * @param <T> the type of the elements
     * @return a SubsetBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> SubsetBuilder<T> of(T... elements) {
        return of(List.of(elements));
    }

    /**
     * Creates a SubsetBuilder for a list of elements.
     * <p>
     * Generates all 2ⁿ subsets of the input set in lexicographical order, where n is
     * the number of elements, using a SubsetBuilder to configure output.
     * </p>
     *
     * @param elements the list of elements to build subsets from
     * @param <T> the type of the elements
     * @return a SubsetBuilder for the specified elements
     */
    public <T> SubsetBuilder<T> of(List<T> elements) {
        return new SubsetBuilder<>(elements, calculator);
    }

    /**
     * Creates a SubsetBuilder for a range of integers from 0 to n-1.
     * <p>
     * Generates all 2ⁿ subsets of the set {0, 1, ..., n-1} in lexicographical order,
     * where n is the dataSize, using a SubsetBuilder to configure output.
     * </p>
     *
     * @param dataSize the number of elements in the range (n)
     * @return a SubsetBuilder for the integer range
     */
    public SubsetBuilder<Integer> of(int dataSize) {
        var elements = IntStream.range(0, dataSize).boxed().toList();
        return new SubsetBuilder<>(elements, calculator);
    }
}