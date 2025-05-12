/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Builder for generating various types of combinations.
 * <p>
 * A combination is a selection of r items from a set where order does not matter. This class supports:
 * <p>
 * - Unique Combinations (`ⁿCᵣ`): Select r elements from n distinct elements without repetition, in lexicographical order or by rank.
 * - Repetitive Combinations (`ⁿ⁺ᵣ⁻¹Cᵣ`): Select r elements from n distinct elements with repetition, in lexicographical order or by rank.
 * - Multiset Combinations: Select r elements from a multiset with specified multiplicities, in lexicographical order or by rank, output as frequency maps.
 * </p>
 * Example usage:
 * <pre>
 * Combinations combinations = new Combinations();
 * // Select 3 out of 5 distinct elements without repetition
 * List<List<Integer>> result = combinations.unique(5, 3).lexOrder().stream().toList();
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class Combinations {

    private final Calculator calculator;

    /**
     * Constructs a new Combinations instance with a default Calculator.
     */
    public Combinations() {
        this(new Calculator());
    }

    /**
     * Constructs a new Combinations instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for unique combinations (`ⁿCᵣ`) from a range of elements.
     * <p>
     * Generates combinations of r elements from {0, 1, ..., n-1} without repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param n the number of distinct elements
     * @param r the size of each combination (r ≥ 0)
     * @return a UniqueCombinationBuilder for integer elements
     */
    public UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0, n).boxed().toList();
        return unique(r, elements);
    }

    /**
     * Creates a builder for unique combinations (`ⁿCᵣ`) from a varargs list of elements.
     * <p>
     * Generates combinations of r elements from the input set without repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each combination (r ≥ 0)
     * @param elements the elements to combine
     * @param <T> the type of elements
     * @return a UniqueCombinationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int r, T... elements) {
        return unique(r, List.of(elements));
    }

    /**
     * Creates a builder for unique combinations (`ⁿCᵣ`) from a list of elements.
     * <p>
     * Generates combinations of r elements from the input set without repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each combination (r ≥ 0)
     * @param elements the list of elements to combine
     * @param <T> the type of elements
     * @return a UniqueCombinationBuilder for the specified elements
     */
    public <T> UniqueCombinationBuilder<T> unique(int r, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, r, calculator);
    }

    /**
     * Creates a builder for repetitive combinations (`ⁿ⁺ᵣ⁻¹Cᵣ`) from a range of elements.
     * <p>
     * Generates combinations of r elements from {0, 1, ..., n-1} with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param n the number of distinct elements
     * @param r the size of each combination (r ≥ 0)
     * @return a RepetitiveCombinationBuilder for integer elements
     * @throws IllegalArgumentException if r < 0
     */
    public RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        if (r < 0) {
            throw new IllegalArgumentException("r should be >=0");
        }
        var elements = IntStream.range(0, n).boxed().toList();
        return repetitive(r, elements);
    }

    /**
     * Creates a builder for repetitive combinations (`ⁿ⁺ᵣ⁻¹Cᵣ`) from a varargs list of elements.
     * <p>
     * Generates combinations of r elements from the input set with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each combination (r ≥ 0)
     * @param elements the elements to combine
     * @param <T> the type of elements
     * @return a RepetitiveCombinationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int r, T... elements) {
        return repetitive(r, List.of(elements));
    }

    /**
     * Creates a builder for repetitive combinations (`ⁿ⁺ᵣ⁻¹Cᵣ`) from a list of elements.
     * <p>
     * Generates combinations of r elements from the input set with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each combination (r ≥ 0)
     * @param elements the list of elements to combine
     * @param <T> the type of elements
     * @return a RepetitiveCombinationBuilder for the specified elements
     */
    public <T> RepetitiveCombinationBuilder<T> repetitive(int r, List<T> elements) {
        return new RepetitiveCombinationBuilder<>(elements, r, calculator);
    }

    /**
     * Creates a builder for multiset combinations from a map of elements and multiplicities.
     * <p>
     * Generates combinations of r elements from a multiset with specified multiplicities,
     * output as frequency maps, in lexicographical order or by rank.
     * </p>
     *
     * @param options a map of elements to their multiplicities (must not be null or empty)
     * @param r the size of each combination (r ≥ 0)
     * @param <T> the type of elements
     * @return a MultisetCombinationBuilder for the specified multiset
     */
    public <T> MultisetCombinationBuilder<T> multiset(LinkedHashMap<T, Integer> options, int r) {
        return new MultisetCombinationBuilder<>(options, r);
    }
}