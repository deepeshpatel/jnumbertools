/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides methods for generating subsets of elements.
 * This class includes functionality related to creating unique subsets from a given set of elements.
 * It is useful for various combinatorial and mathematical analyses involving subsets.
 *
 * <p>The class supports:</p>
 * <ul>
 *     <li><strong>Subsets:</strong> Generating specific subsets from a list of elements.</li>
 *     <li><strong>Combinatorial Analysis:</strong> Methods to aid in analyzing different combinations and properties of subsets.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * Subsets subsets = new Subsets();
 * List&lt;List&lt;Integer&gt;&gt; result = subsets.of(Arrays.asList(1, 2, 3)).getAllSubsets();
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @author Deepesh Patel
 */
public final class Subsets {

    private final Calculator calculator;

    /**
     * Constructs a Subsets instance with a default Calculator.
     */
    public Subsets() {
        this(new Calculator());
    }

    /**
     * Constructs a Subsets instance with the specified Calculator.
     *
     * @param calculator the Calculator to be used for computations
     */
    public Subsets(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a SubsetBuilder for the specified elements.
     *
     * @param elements the elements to build subsets from
     * @param <T>      the type of the elements
     * @return a SubsetBuilder instance for the specified elements
     */
    @SafeVarargs
    public final <T> SubsetBuilder<T> of(T... elements) {
        return of(List.of(elements));
    }

    /**
     * Creates a SubsetBuilder for the specified list of elements.
     *
     * @param elements the list of elements to build subsets from
     * @param <T>      the type of the elements
     * @return a SubsetBuilder instance for the specified list of elements
     */
    public <T> SubsetBuilder<T> of(List<T> elements) {
        return new SubsetBuilder<>(elements, calculator);
    }

    /**
     * Creates a SubsetBuilder for a range of integer elements from 0 to the specified size.
     *
     * @param dataSize the size of the range
     * @return a SubsetBuilder instance for the integer range
     */
    public SubsetBuilder<Integer> of(int dataSize) {
        var elements = IntStream.range(0, dataSize).boxed().toList();
        return new SubsetBuilder<>(elements, calculator);
    }
}
