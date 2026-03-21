/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.api.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Builder for generating unique combinations (ⁿCᵣ) of elements.
 * <p>
 * A unique combination is a selection of r elements from a set of n distinct elements
 * where order does not matter and each element can be selected at most once.
 * The total number of unique combinations is given by the binomial coefficient
 * C(n,r) = n!/(r!·(n−r)!).
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li>Elements are distinct (no repetitions)</li>
 *   <li>Order does not matter ([A,B] is the same as [B,A])</li>
 *   <li>Each combination is returned as a sorted list</li>
 *   <li>Total count = C(n,r) = n!/(r!·(n−r)!)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * UniqueCombinationBuilder&lt;String&gt; builder =
 *     new UniqueCombinationBuilder&lt;&gt;(List.of("A", "B", "C", "D"), 2, calculator);
 *
 * // All C(4,2) = 6 combinations of size 2
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,B], [A,C], [A,D], [B,C], [B,D], [C,D]
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Combinations of size 3 from {0,1,2,3,4}
 * UniqueCombinationBuilder&lt;Integer&gt; intBuilder =
 *     new UniqueCombinationBuilder&lt;&gt;(5, 3, calculator);
 * intBuilder.lexOrder().forEach(System.out::println);
 * // Output: [0,1,2], [0,1,3], [0,1,4], [0,2,3], [0,2,4], [0,3,4],
 * //         [1,2,3], [1,2,4], [1,3,4], [2,3,4]
 * </pre>
 *
 * <h3>Every mᵗʰ Combination</h3>
 * <pre>
 * // Every 2nd combination starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * // Output for 4 elements size 2: [A,C], [B,C], [C,D] (ranks 1,3,5)
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
 * // Output for 4 elements size 2: [A,B], [A,D], [B,D] (ranks 0,2,4)
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // r = 0 produces one empty combination
 * UniqueCombinationBuilder&lt;String&gt; emptyBuilder =
 *     new UniqueCombinationBuilder&lt;&gt;(List.of("A", "B", "C"), 0, calculator);
 * System.out.println(emptyBuilder.count()); // 1
 * emptyBuilder.lexOrder().forEach(System.out::println); // Prints: []
 *
 * // r = n produces one combination containing all elements
 * UniqueCombinationBuilder&lt;String&gt; fullBuilder =
 *     new UniqueCombinationBuilder&lt;&gt;(List.of("A", "B", "C"), 3, calculator);
 * System.out.println(fullBuilder.count()); // 1
 * fullBuilder.lexOrder().forEach(System.out::println); // Prints: [A,B,C]
 *
 * // r > n throws IllegalArgumentException
 * // new UniqueCombinationBuilder&lt;&gt;(List.of("A", "B"), 3, calculator); // Exception!
 * </pre>
 *
 * <h3>Mathematical Properties</h3>
 * <pre>
 * // Symmetry: C(n,r) = C(n,n-r)
 * UniqueCombinationBuilder&lt;String&gt; builder1 =
 *     new UniqueCombinationBuilder&lt;&gt;(5, 2, calculator);
 * UniqueCombinationBuilder&lt;String&gt; builder2 =
 *     new UniqueCombinationBuilder&lt;&gt;(5, 3, calculator);
 * System.out.println(builder1.count()); // 10
 * System.out.println(builder2.count()); // 10
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @see UniqueCombination
 * @see UniqueCombinationByRanks
 * @see <a href="https://en.wikipedia.org/wiki/Binomial_coefficient">Binomial Coefficient</a>
 * @author Deepesh Patel
 */
public final class UniqueCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    /**
     * Constructs a builder for unique combinations.
     *
     * @param elements   the list of items to generate combinations from (must not be null or empty)
     * @param size       the size of each combination (0 ≤ r ≤ n)
     * @param calculator the calculator for computing combination counts and ranks
     * @throws IllegalArgumentException if size < 0, size > n, or elements is null/empty
     */
    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    public static UniqueCombinationBuilder<Integer> newInstance(int n, int r, Calculator calculator) {
        Util.validateNK(n,r);
        var elements = IntStream.range(0, n).boxed().toList();
        return new UniqueCombinationBuilder<>(elements, r, calculator);
    }

    /**
     * Creates a generator for all unique combinations in lexicographical order.
     *
     * @return a {@link UniqueCombination} for iterating all ⁿCᵣ combinations
     */
    public UniqueCombination<T> lexOrder() {

        return new UniqueCombination<>(elements, size);
    }

    /**
     * Creates a generator for every mᵗʰ combination in lexicographical order, starting from a given rank.
     * <p>
     * Uses {@link EveryMthIterable} to produce ranks: start, start+m, start+2m, ..., up to ⁿCᵣ - 1.
     * </p>
     *
     * @param m     the increment between ranks (m > 0)
     * @param start the starting rank (0 ≤ start < ⁿCᵣ)
     * @return a {@link UniqueCombinationByRanks} for the sequence
     * @throws IllegalArgumentException if m ≤ 0 or start < 0 or start ≥ ⁿCᵣ
     */
    public UniqueCombinationByRanks<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, count());
        BigInteger nCr = calculator.nCr(elements.size(), size);
        Iterable<BigInteger> mthIterable = new EveryMthIterable(start, m, nCr);
        return new UniqueCombinationByRanks<>(elements, size, mthIterable, calculator);
    }

    /**
     * Creates a generator for combinations at specified ranks.
     * <p>
     * For example, for ⁿCᵣ with n=3, r=2, ranks [0, 2] might yield [A, B], [B, C].
     * </p>
     * <p>
     * <strong>Validation:</strong> Only validates that the ranks parameter is not null.
     * Invalid rank values (negative, out of bounds, null values in iterable) will be caught
     * naturally during iteration. This design avoids performance overhead for large or infinite streams.
     * </p>
     * <p>
     * <strong>Range Validation:</strong> During iteration, each rank is validated to be in [0, ⁿCᵣ).
     * Invalid ranks will throw {@link IllegalArgumentException} with the specific rank and valid range.
     * </p>
     *
     * @param ranks the iterable of ranks (each rank in [0, ⁿCᵣ))
     * @return a {@link UniqueCombinationByRanks} for the specified ranks
     * @throws IllegalArgumentException if ranks is null or if any rank is out of bounds during iteration
     * @throws NullPointerException if null values are encountered during iteration
     * @throws ArithmeticException or other algorithm-specific exceptions for invalid ranks during generation
     */
    public UniqueCombinationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new UniqueCombinationByRanks<>(elements, size, ranks, calculator);
    }

    /**
     * Creates a generator that samples unique combinations randomly without replacement using custom random generator.
     * <p>
     * Uses {@link BigIntegerSample} to ensure each combination in the sample is distinct.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (1 ≤ sampleSize ≤ ⁿCᵣ)
     * @param random the random generator to use
     * @return a {@link UniqueCombinationByRanks} for random sampling without replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or sampleSize > ⁿCᵣ or random is null
     */
    @Override
    public UniqueCombinationByRanks<T> sample(int sampleSize, Random random) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationByRanks<>(elements, size, new BigIntegerSample(nCr, sampleSize, random), calculator);
    }

    /**
     * Creates a generator that samples unique combinations randomly with replacement using custom random generator.
     * <p>
     * Uses {@link BigIntegerChoice} to allow duplicate combinations in the sample.
     * </p>
     *
     * @param sampleSize the number of combinations to sample (sampleSize ≥ 1)
     * @param random the random generator to use
     * @return a {@link UniqueCombinationByRanks} for random sampling with replacement
     * @throws IllegalArgumentException if sampleSize ≤ 0 or random is null
     */
    @Override
    public UniqueCombinationByRanks<T> choice(int sampleSize, Random random) {
        BigInteger nCr = calculator.nCr(elements.size(), size);
        return new UniqueCombinationByRanks<>(elements, size, new BigIntegerChoice(nCr, sampleSize, random), calculator);
    }

    /**
     * Returns the total number of unique combinations.
     *
     * @return the value of ⁿCᵣ as a {@link BigInteger}
     */
    public BigInteger count() {
        return calculator.nCr(elements.size(), size);
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty() || size == 0;
    }

    @Override
    public String toString() {
        return "UniqueCombinationBuilder{" +
                "elements=" + elements +
                ", size=" + size +
                ", count=" + count() +
                '}';
    }
}