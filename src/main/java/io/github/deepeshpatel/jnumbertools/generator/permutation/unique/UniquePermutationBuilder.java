/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Builder for generating unique permutations of input elements.
 * <p>
 * A unique permutation is an ordered arrangement of all n distinct elements,
 * where each element appears exactly once. The total number of unique permutations
 * is n! (n factorial).
 * </p>
 *
 * <h2>Generation Strategies</h2>
 * <p>
 * This builder provides multiple permutation generation strategies:
 * <ul>
 *   <li><b>Lexicographical Order</b> - {@link #lexOrder()}: Generates all n! permutations
 *       in lexicographical (dictionary) order based on element indices.</li>
 *   <li><b>Single-Swap Order</b> - {@link #singleSwap()}: Generates permutations using
 *       Heap's algorithm, where each successive permutation differs by a single swap.</li>
 *   <li><b>Every mᵗʰ Permutation</b> - {@link #lexOrderMth(BigInteger, BigInteger)}:
 *       Efficiently generates permutations at ranks start, start+m, start+2m, etc.</li>
 *   <li><b>Random Sampling</b> - {@link #sample(int)} and {@link #choice(int)}:
 *       Generate random samples with or without replacement.</li>
 *   <li><b>Rank-Based Access</b> - {@link #byRanks(Iterable)}: Generate permutations
 *       at specific lexicographical rank positions.</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * UniquePermutationBuilder&lt;String&gt; builder = new UniquePermutationBuilder&lt;&gt;(
 *     calculator, List.of("A", "B", "C"));
 *
 * // All 3! = 6 permutations in lexicographical order
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,B,C], [A,C,B], [B,A,C], [B,C,A], [C,A,B], [C,B,A]
 *
 * // Using Heap's algorithm (single-swap order)
 * builder.singleSwap()
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Every mᵗʰ Permutation</h3>
 * <pre>
 * // Every 2nd permutation starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * // Output for 3 elements: [A,C,B], [B,C,A], [C,B,A] (ranks 1,3,5)
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 4 unique permutations without replacement
 * builder.sample(4)
 *        .forEach(System.out::println);
 *
 * // Sample 5 permutations with replacement (duplicates allowed)
 * builder.choice(5)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get permutations at ranks 0, 2, and 4
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(2),
 *                         BigInteger.valueOf(4)))
 *        .forEach(System.out::println);
 * // Output for 3 elements: [A,B,C], [B,A,C], [C,A,B]
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Permutations of {0,1,2,3}
 * UniquePermutationBuilder&lt;Integer&gt; intBuilder =
 *     new UniquePermutationBuilder&lt;&gt;(calculator, 4);
 * intBuilder.lexOrder().forEach(System.out::println);
 * </pre>
 *
 * <h3>Empty Input</h3>
 * <pre>
 * // Empty input produces one empty permutation
 * UniquePermutationBuilder&lt;String&gt; emptyBuilder =
 *     new UniquePermutationBuilder&lt;&gt;(calculator, Collections.emptyList());
 * System.out.println(emptyBuilder.count()); // 1
 * emptyBuilder.lexOrder().forEach(System.out::println); // Prints: []
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @see UniquePermutation
 * @see UniquePermutationByRanks
 * @see UniquePermutationSingleSwap
 * @see <a href="https://en.wikipedia.org/wiki/Heap%27s_algorithm">Heap's Algorithm</a>
 * @author Deepesh Patel
 */
public final class UniquePermutationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a builder for unique permutations.
     *
     * @param calculator the calculator for permutation-related computations
     * @param elements   the list of elements to permute; null results in an empty list
     */
    public UniquePermutationBuilder(Calculator calculator, List<T> elements) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.calculator = calculator;
    }

    /**
     * Generates all unique permutations in lexicographical order.
     *
     * @return a {@link UniquePermutation} instance for all permutations
     */
    public UniquePermutation<T> lexOrder() {
        return new UniquePermutation<>(elements);
    }

    /**
     * Generates every mᵗʰ unique permutation in lexicographical order, starting from the specified rank.
     * <p>
     * For example, with elements [A, B, C] and m=2, start=0, this generates permutations at ranks 0, 2, 4:
     * [A,B,C], [B,A,C], [C,A,B] (assuming 6 total permutations).
     * </p>
     *
     * @param m the step size between selected permutations (must be > 0)
     * @param start the starting rank (0-based, must be ≥ 0 and < total permutations)
     * @return a generator for every mᵗʰ permutation in lexicographical order
     * @throws IllegalArgumentException if m ≤ 0, start < 0, or start ≥ total permutations
     */
    public UniquePermutationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m,start,count());
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new EveryMthIterable(start, m, total), calculator);
    }

    /**
     * Generates permutations at specified lexicographical rank positions.
     * <p>
     * <b>Example for 3 elements:</b>
     * <pre>
     * Rank | Permutation
     * -----|------------
     * 0    | [A, B, C]
     * 1    | [A, C, B]
     * 2    | [B, A, C]
     * 3    | [B, C, A]
     * 4    | [C, A, B]
     * 5    | [C, B, A]
     *
     * byRanks([0, 2, 4]) → [A, B, C], [B, A, C], [C, A, B]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < n!)
     * @return Permutation generator for specified ranks
     * @throws IllegalArgumentException if any rank ≥ n!
     * @throws IllegalStateException if no generation strategy was selected
     */
    public UniquePermutationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new UniquePermutationByRanks<>(elements, ranks, calculator);
    }

    /**
     * Generates a sample of unique permutations without replacement using custom random generator.
     *
     * @param sampleSize the number of permutations to sample; must be positive and ≤ n!
     * @param random the random generator to use
     * @return a {@link UniquePermutationByRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid or random is null
     */
    @Override
    public UniquePermutationByRanks<T> sample(int sampleSize, Random random) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new BigIntegerSample(total, sampleSize, random), calculator);
    }

    /**
     * Generates a sample of unique permutations with replacement using custom random generator.
     *
     * @param sampleSize the number of permutations to sample; must be positive
     * @param random the random generator to use
     * @return a {@link UniquePermutationByRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid or random is null
     */
    @Override
    public UniquePermutationByRanks<T> choice(int sampleSize, Random random) {
        BigInteger total = calculator.factorial(elements.size());
        return new UniquePermutationByRanks<>(elements, new BigIntegerChoice(total, sampleSize, random), calculator);
    }

    /**
     * Generates unique permutations using single swaps (Heap's algorithm).
     *
     * @return a {@link UniquePermutationSingleSwap} instance using single-swap generation
     */
    public UniquePermutationSingleSwap<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

    public BigInteger count() {
        return calculator.factorial(elements.size());
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public String toString() {
        return "UniquePermutationBuilder{" +
                "elements=" + elements +
                ", count=" + count() +
                '}';
    }
}
