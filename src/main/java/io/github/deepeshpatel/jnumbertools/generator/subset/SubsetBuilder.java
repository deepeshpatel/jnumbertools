/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
 * SubsetBuilder&lt;String&gt; builder = new SubsetBuilder&lt;&gt;(
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
 * SubsetBuilder&lt;Integer&gt; intBuilder = new SubsetBuilder&lt;&gt;(5, calculator);
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
 * SubsetBuilder&lt;String&gt; emptyBuilder =
 *     new SubsetBuilder&lt;&gt;(Collections.emptyList(), calculator);
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
 * SubsetBuilder&lt;String&gt; builder4 = new SubsetBuilder&lt;&gt;(List.of("A","B","C","D"), calculator);
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
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements in the subsets
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
     * Constructs a {@code SubsetBuilder} with the specified elements and calculator.
     *
     * @param elements   the list of elements from which subsets will be generated
     * @param calculator a calculator used for computing subset counts
     */
    public SubsetBuilder(List<T> elements, Calculator calculator) {
        this.elements = elements != null ? elements : Collections.emptyList();
        this.calculator = calculator;
        this.from = -1;
        this.to = -1;
    }

    /**
     * Private constructor for creating new instances with range specified.
     */
    private SubsetBuilder(List<T> elements, Calculator calculator, int from, int to) {
        this.elements = elements;
        this.calculator = calculator;
        this.from = from;
        this.to = to;
    }

    /**
     * Configures the builder to generate all subsets of the given elements.
     *
     * @return a new SubsetBuilder instance configured for all subsets
     */
    public SubsetBuilder<T> all() {
        return new SubsetBuilder<>(elements, calculator, 0, elements.size());
    }

    /**
     * Restricts subset generation to sizes within the specified range.
     * <p>
     * Only subsets whose size is between {@code from} and {@code to} (inclusive) will be generated.
     * Subsets are produced in lexicographical order, first by size, then by combination order within each size.
     * </p>
     *
     * @param from the minimum subset size (inclusive, must be ≥ 0)
     * @param to the maximum subset size (inclusive, must be ≥ from and ≤ elements.size())
     * @return a new SubsetBuilder configured for the specified size range
     * @throws IllegalArgumentException if to < from, from < 0, or to > elements.size()
     */
    public SubsetBuilder<T> inRange(int from, int to) {
        if (from < 0 || to < from) {
            throw new IllegalArgumentException(
                    String.format("Invalid range: from=%d, to=%d - must satisfy 0 ≤ from ≤ to", from, to)
            );
            //throw new IllegalArgumentException("Invalid range: from must be ≥ 0 and to must be ≥ from");
        }

        // Empty list special cases
        if (elements.isEmpty()) {
            // All ranges with from ≥ 0 are valid, but will produce:
            // - from=0 → count=1, returns [[]]
            // - from>0 → count=0, returns []
            return new SubsetBuilder<>(elements, calculator, from, to);
        }

        // For non-empty list, validate bounds
        if (to > elements.size()) {
            throw new IllegalArgumentException("Invalid range: to cannot exceed " + elements.size());
        }

        return new SubsetBuilder<>(elements, calculator, from, to);
    }

    /**
     * Creates a {@link SubsetGenerator} that generates subsets in lexicographical order within the specified range.
     *
     * @return a {@code SubsetGenerator} instance
     * @throws IllegalArgumentException if the range is not specified using {@link #inRange(int, int)} or {@link #all()}
     */
    public SubsetGenerator<T> lexOrder() {
        if (from < 0 || to < 0) {
            throw new IllegalArgumentException("Must specify range of subsets via method inRange() or all()");
        }
        return new SubsetGenerator<>(elements, from, to);
    }

    /**
     * Creates a {@link SubsetGeneratorByRanks} that generates every mᵗʰ subset in lexicographical order, starting from the specified position.
     *
     * @param m     the interval for selecting subsets (every mᵗʰ subset) as a {@link BigInteger}
     * @param start the starting position for subsets as a {@link BigInteger}
     * @return a {@code SubsetGeneratorMth} instance
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, count());

        // When count is zero, any start should return empty generator
        if (count().equals(BigInteger.ZERO)) {
            return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
        }

        if(start.compareTo(count()) >= 0 ) {
            throw new IllegalArgumentException("start must be < total subsets in range (0-based)");
        }
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    /**
     * Generates subsets at specified lexicographical ranks.
     * <p>
     * Ranks are 0-based in lexicographical order.
     * </p>
     *
     * @param ranks the iterable of ranks (each rank must be ≥ 0 and < total subsets)
     * @return a generator producing subsets at the given ranks
     */
    public SubsetGeneratorByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        if (from < 0 || to < 0) {
            throw new IllegalStateException("Must specify range via inRange() or all()");
        }
        Util.validateByRanksParams(ranks);
        return new SubsetGeneratorByRanks<>(from, to, ranks, elements, calculator);
    }

    /**
     * Generates a random sample of subsets with replacement.
     * <p>
     * Each subset is chosen independently and uniformly at random from all possible subsets
     * in the configured range. The same subset may appear multiple times in the result.
     * </p>
     * <p>
     * This is equivalent to generating {@code sampleSize} random ranks in [0, count())
     * with replacement and returning the corresponding subsets.
     * </p>
     *
     * @param sampleSize the number of subsets to generate
     * @return a {@code SubsetGeneratorByRanks} producing the random subsets
     * @throws IllegalArgumentException if {@code sampleSize} is negative
     * @see #sample(int)
     * @see BigIntegerChoice
     */
    public SubsetGeneratorByRanks<T> choice(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger total = count();
        Iterable<BigInteger> ranks = new BigIntegerChoice(total, sampleSize);
        return byRanks(ranks);
    }

    /**
     * Generates a random sample of unique subsets without replacement.
     * <p>
     * All returned subsets are distinct and chosen uniformly at random from all possible
     * subsets in the configured range. The order is random (not lexicographical).
     * </p>
     * <p>
     * This is equivalent to generating {@code sampleSize} distinct random ranks in [0, count())
     * and returning the corresponding subsets.
     * </p>
     *
     * @param sampleSize the number of unique subsets to generate
     * @return a {@code SubsetGeneratorByRanks} producing the random unique subsets
     * @throws IllegalArgumentException if {@code sampleSize} is negative or exceeds the total number of subsets in range
     * @see #choice(int)
     * @see BigIntegerSample
     */
    public SubsetGeneratorByRanks<T> sample(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger total = count();
        if (BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException(
                    "Sample size cannot exceed total subsets in range: " + total);
        }
        Iterable<BigInteger> ranks = new BigIntegerSample(total, sampleSize);
        return byRanks(ranks);
    }


    /**
     * Returns the total number of subsets in the specified range.
     * <p>
     * This method calculates the total number of subsets based on the range defined by {@link #inRange(int, int)}
     * or {@link #all()}.
     * </p>
     *
     * @return the count of subsets
     */
    public BigInteger count() {
        if (from < 0 || to < 0) {
            return BigInteger.ZERO; // Not configured yet
        }

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
        // A builder is empty only if it produces NO elements
        // For empty list with range [0,0], it produces one element ([[]]), so NOT empty
        if (elements.isEmpty() && from == 0) {
            return false;  // empty-set(∅) exists, produces [[]]
        }
        return count().equals(BigInteger.ZERO);
    }

    @Override
    public String toString() {
        return "SubsetBuilder{" +
                "elements=" + elements +
                ", from=" + from +
                ", to=" + to +
                ", count=" + (from >= 0 && to >= 0 ? count() : "unspecified") +
                '}';
    }
}