/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Builder class to create various types of combinations of elements.
 *
 * <p>
 * Supports creation of following 6 types of combinations -
 * <ul>
 *     <li><strong>Unique combination lex order:</strong> </li>A selection of r elements out of n without repetition
 *     <li><strong>Mth unique combination lex order:</strong> </li>generating mth combination directly
 *     <li><strong>Repetitive combination lex order:</strong> </li>A selection of r elements out of n with repetition
 *     <li><strong>Mth repetitive combination lex order:</strong> </li>generating mth combination directly
 *     <li><strong>Multiset combination lex order:</strong> </li>combination with given quantity of each element
 *     <li><strong>Mth multiset combination lex order:</strong> </li>generating mth combination directly
 * </ul>
 *
 * Example usage:
 * <pre>
 * <code>
 * Combinations combinations = new Combinations();
 *
 * //selecting 3 out of 5 without repetition
 * List&lt;List&lt;Integer&gt;&gt; result = combinations.unique(5,3).lexOrder().stream().toList();
 * </code>
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for more detailed examples and usage scenarios.
 * @author Deepesh Patel
 */

public final class Combinations {

    private final Calculator calculator;

    /**
     * Constructs a new {@code Combinations} instance with a default {@code Calculator}.
     */
    public Combinations() {
        this(new Calculator());
    }

    /**
     * Constructs a new {@code Combinations} instance with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to use for generating combinations.
     */
    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for unique combinations of a specified size from a range of elements.
     *
     * @param n The range of elements.
     * @param r The size of combinations.
     * @return A builder for unique combinations.
     */
    public UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return unique(r, elements);
    }

    /**
     * Creates a builder for unique combinations of a specified size from a varargs list of elements.
     *
     * @param size The size of combinations.
     * @param elements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for unique combinations.
     */
    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int size, T... elements) {
        return unique(size, List.of(elements));
    }

    /**
     * Creates a builder for unique combinations of a specified size from a list of elements.
     *
     * @param size The size of combinations.
     * @param elements The list of elements.
     * @param <T> The type of elements.
     * @return A builder for unique combinations.
     */
    public <T> UniqueCombinationBuilder<T> unique(int size, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, size, calculator);
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a range of elements.
     *
     * @param n The range of elements.
     * @param r The size of combinations.
     * @return A builder for repetitive combinations.
     * @throws IllegalArgumentException if r &lt;  0
     */
    public RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        if(r < 0 ) {
            throw new IllegalArgumentException("r should be >=0");
        }
        var elements = IntStream.range(0,n).boxed().toList();
        return repetitive(r, elements);
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a varargs list of elements.
     *
     * @param size The size of combinations.
     * @param elements The varargs list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive combinations.
     */
    @SafeVarargs
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int size, T... elements) {
        return  repetitive(size, List.of(elements));
    }

    /**
     * Creates a builder for repetitive combinations of a specified size from a list of elements.
     *
     * @param size The size of combinations.
     * @param elements The list of elements.
     * @param <T> The type of elements.
     * @return A builder for repetitive combinations.
     */
    public  <T> RepetitiveCombinationBuilder<T> repetitive(int size, List<T> elements) {
        return new RepetitiveCombinationBuilder<>(elements, size, calculator);
    }

    /**
     * Creates a builder for multiset combinations of elements with specified frequencies and size.
     *
     * @param options A map where keys are the elements and values are their corresponding frequencies.
     * @param size The size of combinations.
     * @param <T> The type of elements.
     * @return A builder for multiset combinations.
     */
    public <T extends Comparable<T>> MultisetCombinationBuilder<T> multiset(Map<T, Integer> options, int size) {
        return new MultisetCombinationBuilder<>(options, size);
    }
}
