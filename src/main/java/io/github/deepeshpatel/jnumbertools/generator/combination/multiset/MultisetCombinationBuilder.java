/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.api.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.MultisetBuilder;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Builder for generating combinations from a multiset.
 * <p>
 * A multiset combination selects r items from a collection where each element type
 * has a specified maximum multiplicity (frequency). Unlike unique combinations where
 * each element can appear at most once, or repetitive combinations where elements can
 * appear unlimited times, multiset combinations respect given frequency constraints.
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Frequency constraints</b> - Each element type has a maximum allowed count</li>
 *   <li><b>Order doesn't matter</b> - Combinations are returned as frequency maps</li>
 *   <li><b>Output format</b> - Each combination is a Map&lt;T, Integer&gt; showing how many of each element are selected</li>
 *   <li><b>Total count</b> - Computed via dynamic programming respecting all frequency constraints</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * // Define multiset: A available 2 times, B available 1 time, C available 1 time
 * LinkedHashMap&lt;String, Integer&gt; multiset = new LinkedHashMap&lt;&gt;();
 * multiset.put("A", 2);
 * multiset.put("B", 1);
 * multiset.put("C", 1);
 *
 * MultisetCombinationBuilder&lt;String&gt; builder =
 *     new MultisetCombinationBuilder&lt;&gt;(multiset, 2);
 *
 * // All combinations of size 2 respecting frequencies
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: {A=2}, {A=1, B=1}, {A=1, C=1}, {B=1, C=1}
 * </pre>
 *
 * <h3>Larger Example</h3>
 * <pre>
 * // Multiset: A=3, B=2, C=1, D=1, selecting 3 items
 * LinkedHashMap&lt;String, Integer&gt; fruits = new LinkedHashMap&lt;&gt;();
 * fruits.put("Apple", 3);
 * fruits.put("Banana", 2);
 * fruits.put("Cherry", 1);
 * fruits.put("Date", 1);
 *
 * MultisetCombinationBuilder&lt;String&gt; fruitBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(fruits, 3);
 *
 * fruitBuilder.lexOrder().forEach(System.out::println);
 * // Output includes: {Apple=3}, {Apple=2, Banana=1}, {Apple=2, Cherry=1},
 * // {Apple=2, Date=1}, {Apple=1, Banana=2}, {Apple=1, Banana=1, Cherry=1},
 * // {Apple=1, Banana=1, Date=1}, {Banana=2, Cherry=1}, etc.
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Multiset: 0 available 3 times, 1 available 2 times, selecting 2 items
 * LinkedHashMap&lt;Integer, Integer&gt; intMultiset = new LinkedHashMap&lt;&gt;();
 * intMultiset.put(0, 3);
 * intMultiset.put(1, 2);
 *
 * MultisetCombinationBuilder&lt;Integer&gt; intBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(intMultiset, 2);
 *
 * intBuilder.lexOrder().forEach(System.out::println);
 * // Output: {0=2}, {0=1, 1=1}, {1=2}
 * </pre>
 *
 * <h3>Every mᵗʰ Combination</h3>
 * <pre>
 * // Every 2nd combination starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 3 unique combinations without replacement
 * builder.sample(3)
 *        .forEach(System.out::println);
 *
 * // Sample 4 combinations with replacement (duplicates allowed)
 * builder.choice(4)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get combinations at ranks 0, 2, and 3
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(2),
 *                         BigInteger.valueOf(3)))
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // r = 0 produces one empty combination (empty map)
 * MultisetCombinationBuilder&lt;String&gt; emptyBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(multiset, 0);
 * System.out.println(emptyBuilder.count()); // 1
 * emptyBuilder.lexOrder().forEach(System.out::println); // Prints: {}
 *
 * // r exceeding total available items yields no combinations
 * MultisetCombinationBuilder&lt;String&gt; tooLargeBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(multiset, 5);
 * System.out.println(tooLargeBuilder.count()); // 0
 * System.out.println(tooLargeBuilder.lexOrder().stream().count()); // 0
 *
 * // Single element type with frequency f
 * LinkedHashMap&lt;String, Integer&gt; single = new LinkedHashMap&lt;&gt;();
 * single.put("X", 4);
 * MultisetCombinationBuilder&lt;String&gt; singleBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(single, 2);
 * System.out.println(singleBuilder.count()); // 1
 * singleBuilder.lexOrder().forEach(System.out::println); // Prints: {X=2}
 * </pre>
 *
 * <h3>Performance Considerations</h3>
 * <pre>
 * // For large combinations, the builder automatically switches to
 * // an optimized frequency-vector based implementation when r ≥ 1000
 * LinkedHashMap&lt;String, Integer&gt; largeMultiset = new LinkedHashMap&lt;&gt;();
 * largeMultiset.put("A", 500);
 * largeMultiset.put("B", 500);
 *
 * // Uses FreqVectorIterator internally for efficiency
 * MultisetCombinationBuilder&lt;String&gt; largeBuilder =
 *     new MultisetCombinationBuilder&lt;&gt;(largeMultiset, 800);
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @see MultisetCombination
 * @see MultisetCombinationByRanks
 * @see MultisetBuilder
 * @see <a href="https://en.wikipedia.org/wiki/Multiset">Multiset</a>
 * @author Deepesh Patel
 */
public class MultisetCombinationBuilder<T>  implements MultisetBuilder<T> {

    private final LinkedHashMap<T, Integer> options;
    private final int r;
    private final Calculator calculator;

    /**
     * Constructs a builder for multiset combinations.
     *
     * @param options a map of n distinct elements to their multiplicities (must not be null or empty)
     * @param r       the size of each combination (r ≥ 0)
     * @throws IllegalArgumentException if options is null, empty, contains non-positive multiplicities,
     *         or if r < 0
     */
    public MultisetCombinationBuilder(LinkedHashMap<T, Integer> options, int r, Calculator calculator) {
        this.options = options;
        this.r = r;
        this.calculator = calculator;
    }

    /**
     * Generates all multiset combinations in lexicographical order.
     * <p>
     * The order is determined by the keys of the input {@link LinkedHashMap}, producing all
     * possible combinations (totaling the multichoose value, constrained by multiplicities).
     * </p>
     *
     * @return a {@link MultisetCombination} for iterating all combinations
     */
    public MultisetCombination<T> lexOrder() {
        return new MultisetCombination<>(options, r);
    }

    /**
     * Generates a random sample of multiset combinations without replacement.
     * <p>
     * Uses {@link BigIntegerSample} to select distinct combinations, up to the total number
     * of combinations (multichoose value, constrained by multiplicities).
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ total combinations)
     * @param random the random generator to use
     * @return a {@link MultisetCombinationByRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or exceeds total combinations
     */
    public MultisetCombinationByRanks<T> sample(int sampleSize, Random random) {
        BigInteger total = calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        if (sampleSize <= 0 || BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException("Sample size must be positive and not exceed total combinations");
        }
        return new MultisetCombinationByRanks<>(options, r, new BigIntegerSample(total, sampleSize, random), calculator);
    }

    /**
     * Generates a random sample of multiset combinations with replacement.
     * <p>
     * Uses {@link BigIntegerChoice} to select combinations, allowing duplicates, based on the
     * total number of combinations (multichoose value, constrained by multiplicities).
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @param random the random generator to use
     * @return a {@link MultisetCombinationByRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0
     */
    public MultisetCombinationByRanks<T> choice(int sampleSize, Random random) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        BigInteger total = calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        return new MultisetCombinationByRanks<>(options, r, new BigIntegerChoice(total, sampleSize, random), calculator);
    }

    /**
     * Generates every mᵗʰ multiset combination in lexicographical order, starting from a given rank.
     * <p>
     * Produces ranks: start, start+m, start+2m, ..., up to the total number of combinations
     * (multichoose value, constrained by multiplicities). The order is determined by the keys
     * of the input {@link LinkedHashMap}.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < total combinations)
     * @return a {@link MultisetCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0
     */
    public MultisetCombinationByRanks<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates every mᵗʰ multiset combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to the
     * total number of combinations (multichoose value, constrained by multiplicities). The order
     * is determined by the keys of the input {@link LinkedHashMap}.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < total combinations)
     * @return a {@link MultisetCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0
     */
    public MultisetCombinationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, count());
        BigInteger total = calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, total);
        return new MultisetCombinationByRanks<>(options, r, mthIterable, calculator);
    }

    /**
     * Generates multiset combinations at the specified lexicographical rank positions.
     * <p>
     * Example: For multiset {A=2, B=1} and r=2, total combinations are:
     * Rank 0: {A=2}, Rank 1: {A=1, B=1}
     * byRanks([0, 1]) would generate both combinations in that order.
     * </p>
     *
     * @param ranks iterable of 0-based rank numbers (each rank must be ≥ 0 and < total combinations)
     * @return a generator producing combinations at the specified ranks
     * @throws IllegalArgumentException if ranks is null or contains invalid ranks during iteration
     */
    public MultisetCombinationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new MultisetCombinationByRanks<>(options, r, ranks, calculator);
    }

    @Override
    public BigInteger count() {
        return calculator.multisetCombinationsCount(r, options.values().stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public String toString() {
        return "MultisetCombinationBuilder{" +
                "options=" + options +
                ", r=" + r +
                ", count=" + count() +
                '}';
    }
}