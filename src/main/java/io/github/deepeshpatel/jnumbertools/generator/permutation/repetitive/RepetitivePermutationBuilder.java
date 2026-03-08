/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for generating repetitive permutations (permutations with repetition allowed).
 * <p>
 * A repetitive permutation selects r elements from a set of n distinct elements,
 * where elements can be repeated and order matters. The total number of such permutations
 * is nʳ (n raised to the power r).
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li>Elements can appear multiple times in a single permutation</li>
 *   <li>Order matters (different orders produce different permutations)</li>
 *   <li>The permutation length r can be greater than the number of distinct elements n</li>
 *   <li>Total count = nʳ</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * RepetitivePermutationBuilder&lt;String&gt; builder =
 *     new RepetitivePermutationBuilder&lt;&gt;(2, List.of("A", "B"), calculator);
 *
 * // All 2² = 4 permutations of length 2
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,A], [A,B], [B,A], [B,B]
 *
 * // Permutations of length 3 from binary set (2³ = 8)
 * RepetitivePermutationBuilder&lt;Integer&gt; binaryBuilder =
 *     new RepetitivePermutationBuilder&lt;&gt;(3, 2, calculator);
 * binaryBuilder.lexOrder().forEach(System.out::println);
 * // Output: [0,0,0], [0,0,1], [0,1,0], [0,1,1], [1,0,0], [1,0,1], [1,1,0], [1,1,1]
 * </pre>
 *
 * <h3>Every mᵗʰ Permutation</h3>
 * <pre>
 * // Every 3rd permutation of length 2 from [A,B] starting at rank 1
 * builder.lexOrderMth(3, 1)
 *        .forEach(System.out::println);
 * // Output: [A,B], [B,B] (ranks 1 and 4)
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 3 unique permutations without replacement
 * builder.sample(3)
 *        .forEach(System.out::println);
 *
 * // Sample 5 permutations with replacement (duplicates allowed)
 * builder.choice(5)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get permutations at ranks 0, 2, and 5
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(2),
 *                         BigInteger.valueOf(5)))
 *        .forEach(System.out::println);
 * // Output for length 2 from [A,B]: [A,A], [B,A], [B,B] (ranks 0,2,5)
 * </pre>
 *
 * <h3>Mathematical Interpretation</h3>
 * <pre>
 * // Each permutation corresponds to a base-n number with r digits
 * // For n=2, r=3: rank 5 (binary 101) → [1,0,1] → [B,A,B]
 * int rank = 5;
 * var perm = builder.lexOrderMth(1, rank).iterator().next();
 * System.out.println(perm); // [B,A,B] for [A,B] input
 * </pre>
 *
 * <h3>Empty Input Handling</h3>
 * <pre>
 * // Empty-set(∅) with r>0 produces no permutations (count = 0)
 * RepetitivePermutationBuilder&lt;String&gt; emptyBuilder =
 *     new RepetitivePermutationBuilder&lt;&gt;(2, Collections.emptyList(), calculator);
 * System.out.println(emptyBuilder.count()); // 0
 * System.out.println(emptyBuilder.lexOrder().stream().count()); // 0
 *
 * // r = 0 always produces one empty permutation
 * RepetitivePermutationBuilder&lt;String&gt; zeroBuilder =
 *     new RepetitivePermutationBuilder&lt;&gt;(0, List.of("A", "B"), calculator);
 * System.out.println(zeroBuilder.count()); // 1
 * zeroBuilder.lexOrder().forEach(System.out::println); // Prints: []
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @see RepetitivePermutation
 * @see RepetitivePermutationMth
 * @see RepetitivePermutationByRanks
 * @author Deepesh Patel
 */
public final class RepetitivePermutationBuilder<T> implements Builder<T> {

    private final int width;
    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a builder for repetitive permutations.
     *
     * @param width      the length of each permutation; must be non-negative
     * @param elements   the list of elements to permute; null results in an empty list
     * @param calculator utility for permutation computations
     */
    public RepetitivePermutationBuilder(int width, List<T> elements, Calculator calculator) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.width = width;
        this.calculator = calculator;
    }

    /**
     * Generates all repetitive permutations in lexicographical order.
     *
     * @return a {@link RepetitivePermutation} instance for all permutations
     */
    public RepetitivePermutation<T> lexOrder() {
        return new RepetitivePermutation<>(elements, width);
    }

    /**
     * Generates every mᵗʰ repetitive permutation in lexicographical order.
     *
     * @param m     the increment between permutations; must be positive
     * @param start the starting rank (0-based); must be non-negative
     * @return a {@link RepetitivePermutationMth} instance for mᵗʰ permutations
     * @throws IllegalArgumentException if m or start is invalid
     */
    public RepetitivePermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m,start, count());
        return new RepetitivePermutationMth<>(elements, width, m, start);
    }

    /**
     * Generates a random sample of unique repetitive permutations without replacement.
     * <p>
     * All returned permutations are distinct and chosen uniformly at random from all possible
     * nʳ permutations. The order is random (not lexicographical).
     * </p>
     * <p>
     * Example: For elements [A, B] with width=2, total permutations = 4 ([A,A], [A,B], [B,A], [B,B]).
     * sample(2) might return [B,A] and [A,A] in random order.
     * </p>
     *
     * @param sampleSize the number of unique permutations to generate (must be > 0 and ≤ nʳ)
     * @return a generator producing unique random permutations without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or sampleSize > total permutations
     */
    public RepetitivePermutationByRanks<T> sample(int sampleSize) {
        BigInteger total = calculator.power(elements.size(), width);
        return new RepetitivePermutationByRanks<>(elements, width, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Generates a sample of repetitive permutations with replacement.
     *
     * @param sampleSize the number of permutations to sample; must be positive
     * @return a {@link RepetitivePermutationByRanks} instance for sampled permutations
     * @throws IllegalArgumentException if sampleSize is invalid
     */
    public RepetitivePermutationByRanks<T> choice(int sampleSize) {
        BigInteger total = calculator.power(elements.size(), width);
        return new RepetitivePermutationByRanks<>(elements, width, new BigIntegerChoice(total, sampleSize), calculator);
    }

    /**
     * Generates permutations with repetition at specified rank positions.
     * <p>
     * Each rank corresponds to a unique base-n number representation where
     * n is the input size and k is the permutation length.
     * </p>
     * <p>
     * <b>Example for [A, B], k=3:</b>
     * <pre>
     * Rank | Base-2 | Permutation
     * -----|--------|------------
     * 0    | 000    | [A, A, A]
     * 1    | 001    | [A, A, B]
     * 2    | 010    | [A, B, A]
     * 3    | 011    | [A, B, B]
     * 4    | 100    | [B, A, A]
     * 5    | 101    | [B, A, B]
     * 6    | 110    | [B, B, A]
     * 7    | 111    | [B, B, B]
     *
     * byRanks([0, 7]) → [A, A, A], [B, B, B]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < nᵏ)
     * @return Permutation generator for specified ranks
     * @throws IllegalArgumentException if any rank ≥ nᵏ
     * @throws IllegalStateException if length k was not configured
     */
    public RepetitivePermutationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new RepetitivePermutationByRanks<>(elements, width, ranks, calculator);
    }

    /**
     * Returns the total number of possible repetitive permutations.
     *
     * @return the count of permutations as a BigInteger
     */
    @Override
    public BigInteger count() {
        // In context of combinatorics 0⁰ = 1, 0⁰ = 1  and 0ʳ = 0
        if(width==0) return BigInteger.ONE;
        if(elements.isEmpty()) return BigInteger.ZERO;
        return calculator.power(elements.size(), width);
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty() || width == 0;
    }

    @Override
    public String toString() {
        return "RepetitivePermutationBuilder{" +
                "width=" + width +
                ", elements=" + elements +
                ", count=" + count() +
                '}';
    }
}
