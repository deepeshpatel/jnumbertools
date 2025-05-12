/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive.RepetitivePermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

/**
 * Builder for generating various types of permutations.
 * <p>
 * A permutation is an order-dependent arrangement of elements. This class supports:
 * <p>
 * - Unique Permutations (`ⁿ!`): All permutations of n distinct elements, in lexicographical or single-swap (Heap's algorithm) order, or by rank.
 * - k-Permutations (`ⁿPₖ`): Permutations of k elements from n distinct elements, in lexicographical or combination order, or by rank.
 * - Repetitive Permutations (`nᵣ`): Permutations of r elements from n distinct elements with repetition, in lexicographical order or by rank.
 * - Multiset Permutations: Permutations of a multiset with specified multiplicities, output as frequency maps, in lexicographical order or by rank.
 * </p>
 * Outputs are lists of elements, except for multiset permutations (frequency maps).
 * Example usage:
 * <pre>
 * Permutations perm = new Permutations();
 * List<List<Integer>> result = perm.nPk(3, 2).lexOrder().stream().toList();
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class Permutations {

    private final Calculator calculator;

    /**
     * Constructs a new Permutations instance with a default Calculator.
     */
    public Permutations() {
        this(new Calculator());
    }

    /**
     * Constructs a new Permutations instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public Permutations(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for unique permutations (`ⁿ!`) of a range of integers.
     * <p>
     * Generates all permutations of {0, 1, ..., n-1} in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank.
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @return a UniquePermutationBuilder for integer elements
     */
    public UniquePermutationBuilder<Integer> unique(int n) {
        return unique(IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for unique permutations (`ⁿ!`) of a list of elements.
     * <p>
     * Generates all permutations of the input list in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank.
     * </p>
     *
     * @param list the list of distinct elements to permute
     * @param <T> the type of elements
     * @return a UniquePermutationBuilder for the specified elements
     */
    public <T> UniquePermutationBuilder<T> unique(List<T> list) {
        return new UniquePermutationBuilder<>(calculator, list);
    }

    /**
     * Creates a builder for unique permutations (`ⁿ!`) of a varargs list of elements.
     * <p>
     * Generates all permutations of the input elements in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank. Caution: For a single character input (e.g., 'a'),
     * this may invoke unique(int n) with the ASCII value (e.g., 97 for 'a').
     * </p>
     *
     * @param array the elements to permute
     * @param <T> the type of elements
     * @return a UniquePermutationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> UniquePermutationBuilder<T> unique(T... array) {
        return unique(List.of(array));
    }

    /**
     * Creates a builder for k-permutations (`ⁿPₖ`) from a range of integers.
     * <p>
     * Generates permutations of k distinct elements from {0, 1, ..., n-1} in
     * lexicographical or combination order, or by rank.
     * </p>
     *
     * @param n the number of distinct elements
     * @param k the size of each permutation (k ≥ 0)
     * @return a KPermutationBuilder for integer elements
     */
    public KPermutationBuilder<Integer> nPk(int n, int k) {
        return nPk(k, IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for k-permutations (`ⁿPₖ`) from a list of elements.
     * <p>
     * Generates permutations of k distinct elements from the input list in
     * lexicographical or combination order, or by rank.
     * </p>
     *
     * @param k the size of each permutation (k ≥ 0)
     * @param allElements the list of distinct elements
     * @param <T> the type of elements
     * @return a KPermutationBuilder for the specified elements
     */
    public <T> KPermutationBuilder<T> nPk(int k, List<T> allElements) {
        return new KPermutationBuilder<>(allElements, k, calculator);
    }

    /**
     * Creates a builder for k-permutations (`ⁿPₖ`) from a varargs list of elements.
     * <p>
     * Generates permutations of k distinct elements from the input elements in
     * lexicographical or combination order, or by rank.
     * </p>
     *
     * @param k the size of each permutation (k ≥ 0)
     * @param allElements the elements to permute
     * @param <T> the type of elements
     * @return a KPermutationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> KPermutationBuilder<T> nPk(int k, T... allElements) {
        return nPk(k, asList(allElements));
    }

    /**
     * Creates a builder for multiset permutations.
     * <p>
     * Generates all permutations of a multiset with specified multiplicities, output as
     * frequency maps, in lexicographical order or by rank. The number of permutations is
     * n!/(f₁!·f₂!·...), where fᵢ are the multiplicities.
     * </p>
     *
     * @param options a map of elements to their multiplicities (must not be null or empty)
     * @param <T> the type of elements
     * @return a MultisetPermutationBuilder for the specified multiset
     */
    public <T> MultisetPermutationBuilder<T> multiset(LinkedHashMap<T, Integer> options) {
        return new MultisetPermutationBuilder<>(options, calculator);
    }

    /**
     * Creates a builder for repetitive permutations (`nᵣ`) from a list of elements.
     * <p>
     * Generates permutations of r elements from the input list with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each permutation (r ≥ 0)
     * @param elements the list of distinct elements
     * @param <T> the type of elements
     * @return a RepetitivePermutationBuilder for the specified elements
     */
    public <T> RepetitivePermutationBuilder<T> repetitive(int r, List<T> elements) {
        return new RepetitivePermutationBuilder<>(r, elements, calculator);
    }

    /**
     * Creates a builder for repetitive permutations (`nᵣ`) from a varargs list of elements.
     * <p>
     * Generates permutations of r elements from the input elements with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each permutation (r ≥ 0)
     * @param elements the elements to permute
     * @param <T> the type of elements
     * @return a RepetitivePermutationBuilder for the specified elements
     */
    @SafeVarargs
    public final <T> RepetitivePermutationBuilder<T> repetitive(int r, T... elements) {
        return repetitive(r, List.of(elements));
    }

    /**
     * Creates a builder for repetitive permutations (`nᵣ`) from a range of integers.
     * <p>
     * Generates permutations of r elements from {0, 1, ..., n-1} with repetition, in
     * lexicographical order or by rank.
     * </p>
     *
     * @param r the size of each permutation (r ≥ 0)
     * @param n the number of distinct elements (n ≥ 1)
     * @return a RepetitivePermutationBuilder for integer elements
     * @throws IllegalArgumentException if n ≤ 0
     */
    public RepetitivePermutationBuilder<Integer> repetitive(int r, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Repetitive permutation generation required at least 1 element/symbol");
        }
        var symbols = IntStream.range(0, n).boxed().toList();
        return repetitive(r, symbols);
    }
}