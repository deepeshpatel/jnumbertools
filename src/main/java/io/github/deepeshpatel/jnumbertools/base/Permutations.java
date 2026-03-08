/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
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
 * A permutation is an order-dependent arrangement of elements. This class provides factory methods
 * to create builders for different permutation types:
 * <ul>
 *   <li><b>Unique Permutations</b> (n!): All permutations of n distinct elements</li>
 *   <li><b>k-Permutations</b> (ⁿPₖ): Permutations of k elements from n distinct elements</li>
 *   <li><b>Repetitive Permutations</b> (nʳ): Permutations of r elements from n distinct elements with repetition</li>
 *   <li><b>Multiset Permutations</b>: Permutations of a multiset with specified multiplicities</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Unique Permutations</h3>
 * <pre>
 * Permutations perm = new Permutations();
 *
 * // All permutations of [A, B, C] in lexicographical order
 * perm.unique("A", "B", "C")
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // Every 2nd permutation starting from rank 1
 * perm.unique(4)  // permutations of [0,1,2,3]
 *     .lexOrderMth(2, 1)
 *     .forEach(System.out::println);
 *
 * // Random sample of 3 unique permutations without replacement
 * perm.unique("A", "B", "C", "D")
 *     .sample(3)
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>k-Permutations</h3>
 * <pre>
 * // All 2-permutations of [A, B, C, D] in lexicographical order
 * perm.nPk(2, "A", "B", "C", "D")
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // In combination order (grouped by underlying combination)
 * perm.nPk(2, "A", "B", "C")
 *     .combinationOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Repetitive Permutations</h3>
 * <pre>
 * // All 3-length permutations of [A, B] with repetition
 * perm.repetitive(3, "A", "B")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [A,A,A], [A,A,B], [A,B,A], [A,B,B], [B,A,A], [B,A,B], [B,B,A], [B,B,B]
 * </pre>
 *
 * <h3>Multiset Permutations</h3>
 * <pre>
 * // Permutations of multiset with frequencies: A=2, B=1
 * LinkedHashMap&lt;String, Integer&gt; multiset = new LinkedHashMap&lt;&gt;();
 * multiset.put("A", 2);
 * multiset.put("B", 1);
 *
 * perm.multiset(multiset)
 *     .lexOrder()
 *     .forEach(System.out::println);
 * // Output: [A,A,B], [A,B,A], [B,A,A]
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
     * Creates a builder for unique permutations (n!) of a range of integers.
     * <p>
     * Generates all permutations of {0, 1, ..., n-1} in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank.
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @return a UniquePermutationBuilder for integer elements
     */
    public UniquePermutationBuilder<Integer> unique(int n) {
        if(n < 0) throw new IllegalArgumentException("n should be ≥ 0 for unique permutation generation");
        return unique(IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for unique permutations (n!) of a list of elements.
     * <p>
     * Generates all permutations of the input list in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank. Elements are treated as distinct based on
     * their position in the list.
     * </p>
     *
     * @param list the list of distinct elements to permute (must not be null)
     * @param <T> the type of elements
     * @return a UniquePermutationBuilder for the specified elements
     * @throws NullPointerException if the input list is null
     */
    public <T> UniquePermutationBuilder<T> unique(List<T> list) {
        Util.validateInput(list);
        return new UniquePermutationBuilder<>(calculator, list);
    }

    /**
     * Creates a builder for unique permutations (n!) of a varargs list of elements.
     * <p>
     * Generates all permutations of the input elements in lexicographical or single-swap
     * (Heap's algorithm) order, or by rank.
     * </p>
     * <p>
     * <b>Note:</b> For a single character input (e.g., 'a'), this method may invoke
     * {@link #unique(int)} with the ASCII value (97 for 'a'). For single elements,
     * consider using {@link #unique(List)} with a single-element list.
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
     * Creates a builder for k-permutations (ⁿPₖ) from a range of integers.
     * <p>
     * Generates permutations of k distinct elements from {0, 1, ..., n-1} in
     * lexicographical or combination order, or by rank.
     * </p>
     *
     * @param n the number of distinct elements (n ≥ 0)
     * @param k the size of each permutation (0 ≤ k ≤ n)
     * @return a KPermutationBuilder for integer elements
     * @throws IllegalArgumentException if k < 0 or k > n
     */
    public KPermutationBuilder<Integer> nPk(int n, int k) {
        Util.validateNK(n,k);
        return nPk(k, IntStream.range(0, n).boxed().toList());
    }

    /**
     * Creates a builder for k-permutations (ⁿPₖ) from a list of elements.
     * <p>
     * Generates permutations of k distinct elements from the input list in
     * lexicographical or combination order, or by rank. Elements are treated as
     * distinct based on their position in the list.
     * </p>
     *
     * @param k the size of each permutation (0 ≤ k ≤ elements.size())
     * @param elements the list of distinct elements
     * @param <T> the type of elements
     * @return a KPermutationBuilder for the specified elements
     */
    public <T> KPermutationBuilder<T> nPk(int k, List<T> elements) {
        Util.validateInput(elements);
        Util.validateNK(elements.size(), k);
        return new KPermutationBuilder<>(elements, k, calculator);
    }

    /**
     * Creates a builder for k-permutations (ⁿPₖ) from a varargs list of elements.
     * <p>
     * Generates permutations of k distinct elements from the input elements in
     * lexicographical or combination order, or by rank.
     * </p>
     *
     * @param k the size of each permutation (0 ≤ k ≤ allElements.length)
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
     * Generates all distinct permutations of a multiset with specified multiplicities,
     * where the total number of permutations is n!/(n₁!·n₂!·...·nₖ!). Elements are returned
     * as lists preserving the order of the input map's keys.
     * </p>
     * <p>
     * Example: For multiset {A=2, B=1}, the 3!/(2!·1!) = 3 permutations are:
     * [A,A,B], [A,B,A], [B,A,A]
     * </p>
     *
     * @param options a map of elements to their multiplicities (must not be null or empty)
     * @param <T> the type of elements
     * @return a MultisetPermutationBuilder for the specified multiset
     * @throws IllegalArgumentException if options is null, or contains non-positive frequencies
     * (empty map is allowed, treated as ∅)
     */
    public <T> MultisetPermutationBuilder<T> multiset(LinkedHashMap<T, Integer> options) {
        Util.validateMapOptions(options);
        return new MultisetPermutationBuilder<>(options, calculator);
    }

    /**
     * Creates a builder for repetitive permutations (nʳ) from a list of elements.
     * <p>
     * Generates permutations of length r from the input list where elements can repeat.
     * The total number of permutations is {@code elements.size()}^r.
     * </p>
     * <p>
     * Example: For elements [A, B] and r=2, the 2² = 4 permutations are:
     * [A,A], [A,B], [B,A], [B,B]
     * </p>
     *
     * @param r the length of each permutation (r ≥ 0)
     * @param elements the list of distinct elements to permute
     * @param <T> the type of elements
     * @return a RepetitivePermutationBuilder for the specified elements
     * @throws IllegalArgumentException if r < 0 or elements is null
     */
    public <T> RepetitivePermutationBuilder<T> repetitive(int r, List<T> elements) {
        Util.validateInput(elements);
        if(r<0) {
            throw new IllegalArgumentException("Width (r) cannot be negative for repetitive permutation generation");
        }
        return new RepetitivePermutationBuilder<>(r, elements, calculator);
    }

    /**
     * Creates a builder for repetitive permutations (nʳ) from a varargs list of elements.
     * <p>
     * Generates permutations of length r from the input elements where elements can repeat.
     * </p>
     *
     * @param r the length of each permutation (r ≥ 0)
     * @param elements the elements to permute
     * @param <T> the type of elements
     * @return a RepetitivePermutationBuilder for the specified elements
     * @throws IllegalArgumentException if r < 0 or elements is null
     */
    @SafeVarargs
    public final <T> RepetitivePermutationBuilder<T> repetitive(int r, T... elements) {
        return repetitive(r, List.of(elements));
    }

    /**
     * Creates a builder for repetitive permutations (nʳ) from a range of integers.
     * <p>
     * Generates permutations of length r from the set {0, 1, ..., n-1} where elements can repeat.
     * The total number of permutations is n^r.
     * </p>
     *
     * @param r the length of each permutation (r ≥ 0)
     * @param n the number of distinct elements (n ≥ 1)
     * @return a RepetitivePermutationBuilder for integer elements
     * @throws IllegalArgumentException if n ≤ 0 or r < 0
     */
    public RepetitivePermutationBuilder<Integer> repetitive(int r, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Repetitive permutation generation requires at least 1 element/symbol");
        }
        var symbols = IntStream.range(0, n).boxed().toList();
        return repetitive(r, symbols);
    }
}
