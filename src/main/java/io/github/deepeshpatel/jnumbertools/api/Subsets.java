/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api;

import io.github.deepeshpatel.jnumbertools.base.CalculatorImpl;
import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGeneratorByRanks;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGeneratorMth;

import java.util.List;
import java.util.stream.IntStream;
/**
 * Generates subsets of a given set of elements.
 * <p>
 * A subset is a selection of 0 to n elements from a set, where order does not matter.
 * For a set of n elements, there are 2ⁿ possible subsets. This class provides builders
 * for generating subsets in lexicographical order, with support for size ranges,
 * random sampling, and rank-based access.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>All Subsets</h3>
 * <pre>
 * Subsets subs = new Subsets();
 *
 * // All subsets of [A, B, C]
 * subs.of("A", "B", "C")
 *     .all()
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [], [A], [B], [C], [A,B], [A,C], [B,C], [A,B,C]
 * </pre>
 *
 * <h3>Subsets in Size Range</h3>
 * <pre>
 * // Subsets of size 1 to 2 from [A, B, C, D]
 * subs.of("A", "B", "C", "D")
 *     .inRange(1, 2)
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [A], [B], [C], [D], [A,B], [A,C], [A,D], [B,C], [B,D], [C,D]
 * </pre>
 *
 * <h3>Every mᵗʰ Subset</h3>
 * <pre>
 * // Every 3rd subset of [A, B, C, D] starting from rank 1
 * subs.of("A", "B", "C", "D")
 *     .all()
 *     .lexOrderMth(3, 1)
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Random sample of 3 subsets without replacement
 * subs.of(1, 2, 3, 4, 5)
 *     .inRange(2, 4)
 *     .sample(3)
 *     .forEach(System.out::println);
 *
 * // Random sample of 5 subsets with replacement (duplicates allowed)
 * subs.of("A", "B", "C")
 *     .all()
 *     .choice(5)
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Subsets at Specific Ranks</h3>
 * <pre>
 * // Get subsets at ranks 0, 3, and 7
 * subs.of("A", "B", "C", "D")
 *     .all()
 *     .byRanks(List.of(BigInteger.ZERO, BigInteger.valueOf(3), BigInteger.valueOf(7)))
 *     .forEach(System.out::println);
 * // Output: [], [C], [A,B,C] (depending on lexicographical order)
 * </pre>
 *
 * <h3>Integer Range Subsets</h3>
 * <pre>
 * // All subsets of {0, 1, 2, 3, 4}
 * subs.of(5)
 *     .all()
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <p>
 * This class is immutable and thread-safe. All methods return new builder instances.
 * </p>
 *
 * @see AllExamples
 * @see SubsetBuilder
 * @see SubsetGenerator
 * @see SubsetGeneratorMth
 * @see SubsetGeneratorByRanks
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class Subsets {

    private final Calculator calculator;

    /**
     * Constructs a new Subsets instance with a default Calculator.
     */
    public Subsets() {
        this(new CalculatorImpl());
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
     * Generates all 2ⁿ subsets of the input set, where n is the number of elements.
     * Use the returned builder to configure range, sampling, or rank-based access.
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
     * Generates all 2ⁿ subsets of the input set, where n = elements.size().
     * Elements are treated as distinct based on their position in the list.
     * </p>
     *
     * @param elements the list of elements to build subsets from
     * @param <T> the type of the elements
     * @return a SubsetBuilder for the specified elements
     */
    public <T> SubsetBuilder<T> of(List<T> elements) {
        Util.validateInput(elements);
        return new SubsetBuilder<>(elements, calculator);
    }

    /**
     * Creates a SubsetBuilder for a range of integers from 0 to n-1.
     * <p>
     * Generates all 2ⁿ subsets of the set {0, 1, ..., n-1}. Useful for generating
     * subsets of indices without creating explicit element lists.
     * </p>
     *
     * @param dataSize the number of elements in the range (n)
     * @return a SubsetBuilder for the integer range
     */
    public SubsetBuilder<Integer> of(int dataSize) {
        if(dataSize < 0) {
            throw new IllegalArgumentException("dataSize should be ≥ 0 to generate subsets");
        }
        var elements = IntStream.range(0, dataSize).boxed().toList();
        return new SubsetBuilder<>(elements, calculator);
    }
}
