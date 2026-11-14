/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * Builder for generating subsets of elements within a specified size range.
 * <p>
 * A subset is a selection of 0 to n elements from a set, where order does not matter.
 * For a set of n elements, the total number of subsets is 2ⁿ. This builder allows
 * generating all subsets or restricting to a specific size range.
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Order doesn't matter</b> - Each subset is returned as a List&lt;T&gt;</li>
 *   <li><b>Size range</b> - Can generate all subsets or restrict to [from, to] range</li>
 *   <li><b>Generation order</b> - Subsets are generated in lexicographical order,
 *       first by size, then by combination order within each size</li>
 *   <li><b>Total count</b> - Sum of C(n,i) for i from 'from' to 'to'</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>All Subsets</h3>
 * <pre>
 * SubsetSelector&lt;String&gt; builder = new SubsetSelector&lt;&gt;(
 *     List.of("A", "B", "C"), calculator);
 *
 * // All 2³ = 8 subsets
 * builder.all()
 *        .lexOrder()
 *        .forEach(System.out::println);
 * // Output: [], [A], [B], [C], [A,B], [A,C], [B,C], [A,B,C]
 * </pre>
 *
 * <h3>Subsets in Size Range</h3>
 * <pre>
 * // Subsets of size 1 to 2 only
 * builder.inRange(1, 2)
 *        .lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A], [B], [C], [A,B], [A,C], [B,C]
 *
 * // Subsets of size exactly 2
 * builder.inRange(2, 2)
 *        .lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,B], [A,C], [B,C]
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Subsets of {0,1,2,3,4}
 * SubsetSelector&lt;Integer&gt; intBuilder = new SubsetSelector&lt;&gt;(5, calculator);
 *
 * // All subsets of size 2 to 4
 * intBuilder.inRange(2, 4)
 *           .lexOrder()
 *           .forEach(System.out::println);
 * </pre>
 *
 * <h3>Every mᵗʰ Subset</h3>
 * <pre>
 * // Every 3rd subset starting from rank 1
 * builder.all()
 *        .lexOrderMth(3, 1)
 *        .forEach(System.out::println);
 * // Output for [A,B,C]: ranks 1,4,7 → [A], [A,C], [A,B,C]
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 3 unique subsets without replacement
 * builder.inRange(1, 3)
 *        .sample(3)
 *        .forEach(System.out::println);
 *
 * // Sample 4 subsets with replacement (duplicates allowed)
 * builder.all()
 *        .choice(4)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get subsets at ranks 0, 3, and 6
 * builder.all()
 *        .byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(3),
 *                         BigInteger.valueOf(6)))
 *        .forEach(System.out::println);
 * // Output for [A,B,C]: [], [C], [B,C] (ranks 0,3,6)
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // Empty-set(∅) has one subset (the empty-set itself)
 * SubsetSelector&lt;String&gt; emptyBuilder =
 *     new SubsetSelector&lt;&gt;(Collections.emptyList(), calculator);
 * System.out.println(emptyBuilder.all().count()); // 1
 * emptyBuilder.all().lexOrder().forEach(System.out::println); // Prints: []
 *
 * // Range with from=0, to=0 produces only the empty subset
 * builder.inRange(0, 0)
 *        .lexOrder()
 *        .forEach(System.out::println); // Prints: []
 *
 * // Invalid ranges throw IllegalArgumentException
 * // builder.inRange(3, 1); // Exception: to must be ≥ from
 * // builder.inRange(5, 6); // Exception: to cannot exceed n
 * </pre>
 *
 * <h3>Mathematical Properties</h3>
 * <pre>
 * // Total subsets = 2ⁿ
 * SubsetSelector&lt;String&gt; builder4 = new SubsetSelector&lt;&gt;(List.of("A","B","C","D"), calculator);
 * System.out.println(builder4.all().count()); // 16
 *
 * // Sum of C(4,i) for i=0..4 = 16
 * // Sum of C(4,i) for i=2..3 = C(4,2)+C(4,3) = 6+4 = 10
 * System.out.println(builder4.inRange(2, 3).count()); // 10
 * </pre>
 *
 * <h3>Combining with Other Operations</h3>
 * <pre>
 * // Subsets can be used as dimensions in constrained Cartesian products
 * CartesianProduct cp = new CartesianProduct(calculator);
 * cp.constrainedProductOfDistinct(1, "X", "Y")
 *   .andInRange(1, 2, List.of("A", "B", "C"))  // Subsets as a dimension
 *   .lexOrder()
 *   .forEach(System.out::println);
 * </pre>
 *
 * <p>
 * A {@code SubsetBuilder} always has a size range: it is obtained from a {@link SubsetSelector}
 * ({@code subsets().of(...)}) by choosing {@code all()} or {@code inRange(from, to)}, so there is no
 * half-configured builder. The examples above call {@code all()} and {@code inRange(...)} on the
 * selector. This builder is immutable and thread-safe.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @see SubsetSelector
 * @see SubsetGenerator
 * @see SubsetGeneratorMth
 * @see SubsetGeneratorByRanks
 * @see <a href="https://en.wikipedia.org/wiki/Power_set">Power Set</a>
 * @author Deepesh Patel
 */
public class SubsetBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final Calculator calculator;
    private final int from;
    private final int to;

    /**
     * Creates a builder for an already validated, snapshotted element list and a size range.
     * Instances come from {@link SubsetSelector#all()} and {@link SubsetSelector#inRange(int, int)}, so
     * a builder always has a range.
     */
    SubsetBuilder(List<T> elements, Calculator calculator, int from, int to) {
        this.elements = elements;
        this.calculator = calculator;
        this.from = from;
        this.to = to;
    }

    /**
     * Creates a {@link SubsetGenerator} that generates subsets in lexicographical order within the specified range.
     *
     * @return a {@code SubsetGenerator} instance
     */
    public SubsetGenerator<T> lexOrder() {
        return new SubsetGenerator<>(elements, from, to);
    }

    /**
     * Creates a {@link SubsetGeneratorByRanks} that generates every mᵗʰ subset in lexicographical order, starting from the specified position.
     *
     * <p>
     * If the range contains no subsets ({@code count() == 0}, e.g. sizes 1..2 of an empty set) any
     * {@code start ≥ 0} is accepted and the generator yields nothing.
     * </p>
     *
     * @param m     the interval for selecting subsets (every mᵗʰ subset) as a {@link BigInteger}
     * @param start the starting position for subsets as a {@link BigInteger}
     * @return a {@code SubsetGeneratorMth} instance
     * @throws IllegalArgumentException if m ≤ 0, start &lt; 0, or start ≥ count() while count() &gt; 0
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        // Rules: m ≤ 0, start < 0, and (count > 0 and start ≥ count) are errors;
        // count == 0 accepts any start ≥ 0 and the generator is simply empty.
        Util.validateLexOrderMthParams(m, start, count());
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    /**
     * Generates subsets at specified lexicographical ranks.
     * <p>
     * Ranks are 0-based in lexicographical order.
     * </p>
     * <p>
     * Only a null {@code ranks} is rejected up front. The sequence is consumed lazily, one rank at a
     * time (it may be infinite or single-use), so a rank {@code < 0} or {@code ≥ count()} is reported
     * as an {@link IllegalArgumentException} during iteration, when that rank is reached.
     * </p>
     *
     * @param ranks the iterable of ranks (each rank must be ≥ 0 and &lt; total subsets)
     * @return a generator producing subsets at the given ranks
     * @throws IllegalArgumentException if {@code ranks} is null
     */
    public SubsetGeneratorByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new SubsetGeneratorByRanks<>(from, to, ranks, elements, calculator);
    }

    /**
     * Generates a random sample of subsets with replacement using custom random generator.
     * <p>
     * Each subset is chosen independently and uniformly at random from all possible subsets
     * in the configured range. The same subset may appear multiple times in the result.
     * </p>
     * <p>
     * This is equivalent to generating {@code sampleSize} random ranks in [0, count())
     * with replacement and returning the corresponding subsets.
     * </p>
     *
     * <p>
     * Sampling is uniform over the whole range {@code [0, count())}, however large {@code count()} is.
     * {@code sampleSize} may exceed {@code count()} (duplicates are then unavoidable), but the range
     * must contain at least one subset.
     * </p>
     *
     * @param sampleSize the number of subsets to generate (must be &gt; 0)
     * @param random the random generator to use
     * @return a {@code SubsetGeneratorByRanks} producing the random subsets
     * @throws IllegalArgumentException if {@code sampleSize ≤ 0} or {@code count() == 0}
     * @throws NullPointerException if {@code random} is null
     * @see #sample(int, Random)
     * @see BigIntegerChoice
     */
    @Override
    public SubsetGeneratorByRanks<T> choice(int sampleSize, Random random) {
        // BigIntegerChoice enforces: count > 0, sampleSize > 0, random != null
        return byRanks(new BigIntegerChoice(count(), sampleSize, random));
    }

    /**
     * Generates a random sample of unique subsets without replacement using custom random generator.
     * <p>
     * All returned subsets are distinct and chosen uniformly at random from all possible
     * subsets in the configured range. The order is random (not lexicographical).
     * </p>
     * <p>
     * This is equivalent to generating {@code sampleSize} distinct random ranks in [0, count())
     * and returning the corresponding subsets.
     * </p>
     *
     * @param sampleSize the number of unique subsets to generate (must be &gt; 0 and ≤ count())
     * @param random the random generator to use
     * @return a {@code SubsetGeneratorByRanks} producing the random unique subsets
     * @throws IllegalArgumentException if {@code sampleSize ≤ 0} or {@code sampleSize > count()}
     *         (including {@code count() == 0}: sampling is without replacement)
     * @throws NullPointerException if {@code random} is null
     * @see #choice(int, Random)
     * @see BigIntegerSample
     */
    @Override
    public SubsetGeneratorByRanks<T> sample(int sampleSize, Random random) {
        // BigIntegerSample enforces: count > 0, 0 < sampleSize ≤ count, random != null
        return byRanks(new BigIntegerSample(count(), sampleSize, random));
    }


    /**
     * Returns the total number of subsets in the specified range.
     * <p>
     * This method calculates the total number of subsets of the size range this builder was created with
     * (see {@link SubsetSelector}).
     * </p>
     *
     * @return the count of subsets
     */
    public BigInteger count() {
        // Empty list special cases
        if (elements.isEmpty()) {
            if (from == 0) {
                return BigInteger.ONE; // empty-set(∅) exists
            }
            return BigInteger.ZERO; // No non-empty subsets
        }

        return calculator.totalSubsetsInRange(from, to, elements.size());
    }

    @Override
    public boolean isEmpty() {
        // Empty if and only if the builder yields no results at all (count() == 0).
        // A builder whose only result is the empty subset ([[]]) has count() == 1 and is NOT empty.
        return count().signum() == 0;
    }

    @Override
    public String toString() {
        return "SubsetBuilder{" +
                "elements=" + elements +
                ", from=" + from +
                ", to=" + to +
                ", count=" + count() +
                '}';
    }
}