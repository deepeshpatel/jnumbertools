/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Builder for generating various types of combinations.
 * <p>
 * A combination is a selection of r items from a set where order does not matter. This class provides
 * factory methods to create builders for different combination types:
 * <ul>
 *   <li><b>Unique Combinations</b> (ⁿCᵣ): Select r elements from n distinct elements without repetition</li>
 *   <li><b>Repetitive Combinations</b> (ⁿ⁺ʳ⁻¹Cᵣ): Select r elements from n distinct elements with repetition allowed</li>
 *   <li><b>Multiset Combinations</b>: Select r elements from a multiset with specified multiplicities</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Unique Combinations</h3>
 * <pre>
 * Combinations comb = new Combinations();
 *
 * // All combinations of size 2 from [A, B, C, D]
 * comb.unique(2, "A", "B", "C", "D")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [A,B], [A,C], [A,D], [B,C], [B,D], [C,D]
 *
 * // Every 2nd combination starting from rank 0
 * comb.unique(5, 3)  // combinations of size 3 from {0,1,2,3,4}
 *     .lexOrderMth(2, 0)
 *     .forEach(System.out::println);
 *
 * // Random sample of 4 unique combinations without replacement
 * comb.unique(2, "A", "B", "C", "D", "E")
 *     .sample(4)
 *     .forEach(System.out::println);
 *
 * // Combinations at specific ranks
 * comb.unique(2, "A", "B", "C", "D")
 *     .byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(3)))
 *     .forEach(System.out::println);
 * // Output: [A,B], [B,D] (assuming ranks 0 and 3)
 * </pre>
 *
 * <h3>Repetitive Combinations (with repetition)</h3>
 * <pre>
 * // All combinations of size 3 from [A, B] with repetition
 * comb.repetitive(3, "A", "B")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [A,A,A], [A,A,B], [A,B,B], [B,B,B]
 *
 * // Every 2nd repetitive combination
 * comb.repetitive(4, 3)  // from {0,1,2} with size 4
 *     .lexOrderMth(2, 0)
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Multiset Combinations</h3>
 * <pre>
 * // Combinations from multiset with frequencies: A=3, B=2, C=1, size=3
 * LinkedHashMap&lt;String, Integer&gt; multiset = new LinkedHashMap&lt;&gt;();
 * multiset.put("A", 3);
 * multiset.put("B", 2);
 * multiset.put("C", 1);
 *
 * comb.multiset(multiset, 3)
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output frequency maps: {A=3}, {A=2,B=1}, {A=2,C=1}, {A=1,B=2}, {A=1,B=1,C=1}, {B=2,C=1}
 * </pre>
 *
 * <p>
 * This class is immutable and thread-safe. All methods return new builder instances.
 * </p>
 *
 * @see AllExamples
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
     * Creates a builder for unique combinations (ⁿCᵣ) from a range of integers.
     * <p>
     * Generates combinations of r elements from {0, 1, ..., n-1} without repetition,
     * in lexicographical order or by rank.
     * </p>
     * <p>
     * Example: unique(5, 3).lexOrder() generates all 10 combinations of size 3 from {0,1,2,3,4}.
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @param r the size of each combination (0 ≤ r ≤ n)
     * @return a UniqueCombinationBuilder for integer elements
     */
    public UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0, n).boxed().toList();
        return unique(r, elements);
    }

    /**
     * Creates a builder for unique combinations (ⁿCᵣ) from a varargs list of elements.
     * <p>
     * Generates combinations of r elements from the input set without repetition,
     * in lexicographical order or by rank. Elements are treated as distinct based on
     * their position in the input.
     * </p>
     *
     * @param r the size of each combination (0 ≤ r ≤ elements.length)
     * @param elements the elements to combine
     * @param <T> the type of elements
     * @return a UniqueCombinationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int r, T... elements) {
        return unique(r, List.of(elements));
    }

    /**
     * Creates a builder for unique combinations (ⁿCᵣ) from a list of elements.
     * <p>
     * Generates combinations of r elements from the input set without repetition,
     * in lexicographical order or by rank. Elements are treated as distinct based on
     * their position in the list.
     * </p>
     *
     * @param r the size of each combination (0 ≤ r ≤ elements.size())
     * @param elements the list of elements to combine
     * @param <T> the type of elements
     * @return a UniqueCombinationBuilder for the specified elements
     */
    public <T> UniqueCombinationBuilder<T> unique(int r, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, r, calculator);
    }

    /**
     * Creates a builder for repetitive combinations (ⁿ⁺ʳ⁻¹Cᵣ) from a range of integers.
     * <p>
     * Generates combinations of r elements from {0, 1, ..., n-1} with repetition allowed,
     * in lexicographical order or by rank. The total number of combinations is C(n+r-1, r).
     * </p>
     * <p>
     * Example: repetitive(3, 2).lexOrder() generates combinations of size 2 from {0,1,2} with repetition:
     * [0,0], [0,1], [0,2], [1,1], [1,2], [2,2]
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
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
     * Creates a builder for repetitive combinations (ⁿ⁺ʳ⁻¹Cᵣ) from a varargs list of elements.
     * <p>
     * Generates combinations of r elements from the input set with repetition allowed,
     * in lexicographical order or by rank.
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
     * Creates a builder for repetitive combinations (ⁿ⁺ʳ⁻¹Cᵣ) from a list of elements.
     * <p>
     * Generates combinations of r elements from the input set with repetition allowed,
     * in lexicographical order or by rank. Elements are treated as distinct based on
     * their position in the list.
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
     * where each element can appear up to its given frequency. Combinations are returned
     * as frequency maps (e.g., {A=2, B=1}) in lexicographical order based on the map's
     * key order.
     * </p>
     * <p>
     * Example: For multiset {A=3, B=2} and r=2, the combinations are:
     * {A=2}, {A=1,B=1}, {B=2}
     * </p>
     *
     * @param options a map of elements to their multiplicities (must not be null, and all frequencies must be positive)
     * @param r the size of each combination (r ≥ 0)
     * @param <T> the type of elements
     * @return a MultisetCombinationBuilder for the specified multiset
     * @throws IllegalArgumentException if options is null, contains non-positive frequencies, or r < 0
     */
    public <T> MultisetCombinationBuilder<T> multiset(LinkedHashMap<T, Integer> options, int r) {
        return new MultisetCombinationBuilder<>(options, r);
    }
}
