/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.core.external.Calculator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * Builder for generating repetitive combinations (combinations with repetition allowed).
 * <p>
 * A repetitive combination (also called combination with replacement or multichoose)
 * selects r elements from a set of n distinct elements where:
 * <ul>
 *   <li>Elements can be selected multiple times (repetition allowed)</li>
 *   <li>Order does not matter ([A,A,B] is the same as [A,B,A])</li>
 *   <li>The total number of combinations is given by C(n+r-1, r) = (n+r-1)!/(r!·(n-1)!)</li>
 * </ul>
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Repetition allowed</b> - Elements can appear multiple times</li>
 *   <li><b>Order doesn't matter</b> - Combinations are sorted lexicographically</li>
 *   <li><b>r can exceed n</b> - Since repetition is allowed, r can be larger than the number of distinct elements</li>
 *   <li><b>Total count</b> = C(n+r-1, r) = (n+r-1 choose r)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * RepetitiveCombinationBuilder&lt;String&gt; builder =
 *     new RepetitiveCombinationBuilder&lt;&gt;(List.of("A", "B", "C"), 2, calculator);
 *
 * // All C(3+2-1,2) = C(4,2) = 6 combinations with repetition
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,A], [A,B], [A,C], [B,B], [B,C], [C,C]
 * </pre>
 *
 * <h3>When r exceeds n</h3>
 * <pre>
 * // Combinations of size 5 from [A, B] (C(2+5-1,5) = C(6,5) = 6)
 * RepetitiveCombinationBuilder&lt;String&gt; builder2 =
 *     new RepetitiveCombinationBuilder&lt;&gt;(List.of("A", "B"), 5, calculator);
 * builder2.lexOrder().forEach(System.out::println);
 * // Output: [A,A,A,A,A], [A,A,A,A,B], [A,A,A,B,B], [A,A,B,B,B], [A,B,B,B,B], [B,B,B,B,B]
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Combinations of size 3 from {0,1,2} with repetition (C(3+3-1,3) = C(5,3) = 10)
 * RepetitiveCombinationBuilder&lt;Integer&gt; intBuilder =
 *     new RepetitiveCombinationBuilder&lt;&gt;(3, 3, calculator);
 * intBuilder.lexOrder().forEach(System.out::println);
 * // Output: [0,0,0], [0,0,1], [0,0,2], [0,1,1], [0,1,2], [0,2,2], [1,1,1], [1,1,2], [1,2,2], [2,2,2]
 * </pre>
 *
 * <h3>Every mᵗʰ Combination</h3>
 * <pre>
 * // Every 2nd combination starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * // Output for [A,B,C] size 2: [A,B], [B,B], [C,C] (ranks 1,3,5)
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 4 unique combinations without replacement
 * builder.sample(4)
 *        .forEach(System.out::println);
 *
 * // Sample 5 combinations with replacement (duplicates allowed)
 * builder.choice(5)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get combinations at ranks 0, 2, and 4
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(2),
 *                         BigInteger.valueOf(4)))
 *        .forEach(System.out::println);
 * // Output for [A,B,C] size 2: [A,A], [A,C], [B,C] (ranks 0,2,4)
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // r = 0 produces one empty combination
 * RepetitiveCombinationBuilder&lt;String&gt; emptyBuilder =
 *     new RepetitiveCombinationBuilder&lt;&gt;(List.of("A", "B"), 0, calculator);
 * System.out.println(emptyBuilder.count()); // 1
 * emptyBuilder.lexOrder().forEach(System.out::println); // Prints: []
 *
 * // n = 1 produces all combinations with the single element
 * RepetitiveCombinationBuilder&lt;String&gt; singleBuilder =
 *     new RepetitiveCombinationBuilder&lt;&gt;(List.of("A"), 3, calculator);
 * System.out.println(singleBuilder.count()); // 1
 * singleBuilder.lexOrder().forEach(System.out::println); // Prints: [A,A,A]
 *
 * // n = 0, r > 0 produces no combinations [empty-set(∅)]
 * RepetitiveCombinationBuilder&lt;String&gt; zeroBuilder =
 *     new RepetitiveCombinationBuilder&lt;&gt;(Collections.emptyList(), 3, calculator);
 * System.out.println(zeroBuilder.count()); // 0
 * System.out.println(zeroBuilder.lexOrder().stream().count()); // 0
 * </pre>
 *
 * <h3>Relationship to Stars and Bars</h3>
 * <pre>
 * // Each combination corresponds to a stars-and-bars representation
 * // For n=3, r=5, the combination [A,A,B,C,C] represents:
 * // A appears 2 times, B appears 1 time, C appears 2 times
 * // This maps to stars: **|*|** (2 stars, 1 star, 2 stars)
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @see RepetitiveCombination
 * @see RepetitiveCombinationByRanks
 * @see <a href="https://en.wikipedia.org/wiki/Combination#Number_of_combinations_with_repetition">Combinations with Repetition</a>
 * @see <a href="https://en.wikipedia.org/wiki/Stars_and_bars_(combinatorics)">Stars and Bars</a>
 * @author Deepesh Patel
 */
public final class RepetitiveCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a builder for repetitive combinations.
     *
     * @param elements   the list of n items to generate combinations from (must not be null or empty)
     * @param size       the size of each combination (r ≥ 0)
     * @param calculator the calculator for computing combinatorial values
     * @throws IllegalArgumentException if r < 0 or elements is null/empty
     */
    public RepetitiveCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    /**
     * Creates a generator for all repetitive combinations in lexicographical order.
     *
     * @return a {@link RepetitiveCombination} for iterating all ⁿ⁺ᵣ⁻¹Cᵣ combinations
     */
    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }

    /**
     * Creates a generator for every mᵗʰ repetitive combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to ⁿ⁺ᵣ⁻¹Cᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿ⁺ᵣ⁻¹Cᵣ)
     * @return a {@link RepetitiveCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        validateLexOrderMthParams(m,start, count());
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCrRepetitive);
        return new RepetitiveCombinationByRanks<>(elements, size, mthIterable, calculator);
    }

    /**
     * Creates a generator that samples repetitive combinations randomly without replacement using custom random generator.
     * <p>
     * Uses {@link BigIntegerSample} to ensure each combination in the sample is distinct.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ ⁿ⁺ᵣ⁻¹Cᵣ)
     * @param random the random generator to use
     * @return a {@link RepetitiveCombinationByRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or sampleSize > ⁿ⁺ᵣ⁻¹Cᵣ or random is null
     */
    @Override
    public RepetitiveCombinationByRanks<T> sample(int sampleSize, Random random) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationByRanks<>(elements, size, new BigIntegerSample(nCrRepetitive, sampleSize, random), calculator);
    }

    /**
     * Creates a generator that samples repetitive combinations randomly with replacement using custom random generator.
     * <p>
     * Uses {@link BigIntegerChoice} to allow duplicate combinations in the sample.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @param random the random generator to use
     * @return a {@link RepetitiveCombinationByRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or random is null
     */
    @Override
    public RepetitiveCombinationByRanks<T> choice(int sampleSize, Random random) {
        BigInteger nCrRepetitive = calculator.nCrRepetitive(elements.size(), size);
        return new RepetitiveCombinationByRanks<>(elements, size, new BigIntegerChoice(nCrRepetitive, sampleSize, random), calculator);
    }

    /**
     * Creates a generator for repetitive combinations based on a custom sequence of ranks.
     * <p>
     * Ranks are 0-based in lexicographical order.
     * </p>
     *
     * @param ranks the iterable of ranks (each rank in [0, ⁿ⁺ᵣ⁻¹Cᵣ))
     * @return a {@link RepetitiveCombinationByRanks} for the custom sequence
     * @throws IllegalArgumentException if any rank < 0 or rank ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        validateByRanksParams(ranks);
        return new RepetitiveCombinationByRanks<>(elements, size, ranks, calculator);
    }

    /**
     * Returns the total number of repetitive combinations.
     *
     * @return the value of ⁿ⁺ᵣ⁻¹Cᵣ as a {@link BigInteger}
     */
    public BigInteger count() {
        return calculator.nCrRepetitive(elements.size(), size);
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty() || size == 0;
    }

    @Override
    public String toString() {
        return "RepetitiveCombinationBuilder{" +
                "elements=" + elements +
                ", size=" + size +
                ", count=" + count() +
                '}';
    }
}
