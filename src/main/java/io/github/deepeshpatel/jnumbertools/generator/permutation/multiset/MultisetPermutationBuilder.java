/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.LinkedHashMap;

/**
 * Builder for generating distinct permutations of a multiset.
 * <p>
 * A multiset permutation is an ordered arrangement of elements where some elements
 * may be identical. Given frequencies f₁, f₂, ..., fₖ for k distinct element types,
 * the total number of distinct permutations is n!/(f₁!·f₂!·...·fₖ!) where n = ∑fᵢ.
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Duplicate elements</b> - Elements can appear multiple times based on frequencies</li>
 *   <li><b>Order matters</b> - Different arrangements are considered distinct permutations</li>
 *   <li><b>Output format</b> - Each permutation is a List&lt;T&gt; of length n = sum of frequencies</li>
 *   <li><b>Total count</b> - Multinomial coefficient n!/(f₁!·f₂!·...·fₖ!)</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Generation</h3>
 * <pre>
 * // Define multiset: A appears 2 times, B appears 1 time
 * LinkedHashMap&lt;String, Integer&gt; multiset = new LinkedHashMap&lt;&gt;();
 * multiset.put("A", 2);
 * multiset.put("B", 1);
 *
 * MultisetPermutationBuilder&lt;String&gt; builder =
 *     new MultisetPermutationBuilder&lt;&gt;(multiset, calculator);
 *
 * // All 3!/(2!·1!) = 3 distinct permutations
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A, A, B], [A, B, A], [B, A, A]
 * </pre>
 *
 * <h3>Larger Example</h3>
 * <pre>
 * // Mississippi example: M=1, i=4, s=4, p=2 (total 11 letters)
 * LinkedHashMap&lt;String, Integer&gt; mississippi = new LinkedHashMap&lt;&gt;();
 * mississippi.put("M", 1);
 * mississippi.put("i", 4);
 * mississippi.put("s", 4);
 * mississippi.put("p", 2);
 *
 * MultisetPermutationBuilder&lt;String&gt; msBuilder =
 *     new MultisetPermutationBuilder&lt;&gt;(mississippi, calculator);
 * System.out.println("Total permutations: " + msBuilder.count());
 * // Output: 11!/(1!·4!·4!·2!) = 34,650
 * </pre>
 *
 * <h3>Integer Range Input</h3>
 * <pre>
 * // Multiset: 0 appears 2 times, 1 appears 1 time, 2 appears 1 time
 * LinkedHashMap&lt;Integer, Integer&gt; intMultiset = new LinkedHashMap&lt;&gt;();
 * intMultiset.put(0, 2);
 * intMultiset.put(1, 1);
 * intMultiset.put(2, 1);
 *
 * MultisetPermutationBuilder&lt;Integer&gt; intBuilder =
 *     new MultisetPermutationBuilder&lt;&gt;(intMultiset, calculator);
 *
 * intBuilder.lexOrder().forEach(System.out::println);
 * // Output: [0,0,1,2], [0,0,2,1], [0,1,0,2], [0,1,2,0], [0,2,0,1], [0,2,1,0],
 * //         [1,0,0,2], [1,0,2,0], [1,2,0,0], [2,0,0,1], [2,0,1,0], [2,1,0,0]
 * </pre>
 *
 * <h3>Every mᵗʰ Permutation</h3>
 * <pre>
 * // Every 2nd permutation starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * // Output for [A=2,B=1]: [A,B,A], [B,A,A] (ranks 1 and 2)
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 2 unique permutations without replacement
 * builder.sample(2)
 *        .forEach(System.out::println);
 *
 * // Sample 4 permutations with replacement (duplicates allowed)
 * builder.choice(4)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get permutations at ranks 0, 1, and 2
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.ONE,
 *                         BigInteger.valueOf(2)))
 *        .forEach(System.out::println);
 * // Output: [A,A,B], [A,B,A], [B,A,A]
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // Single element type with frequency f
 * LinkedHashMap&lt;String, Integer&gt; single = new LinkedHashMap&lt;&gt;();
 * single.put("X", 3);
 * MultisetPermutationBuilder&lt;String&gt; singleBuilder =
 *     new MultisetPermutationBuilder&lt;&gt;(single, calculator);
 * System.out.println(singleBuilder.count()); // 1
 * singleBuilder.lexOrder().forEach(System.out::println); // Prints: [X, X, X]
 *
 * // Zero frequencies not allowed (throws IllegalArgumentException)
 * LinkedHashMap&lt;String, Integer&gt; invalid = new LinkedHashMap&lt;&gt;();
 * invalid.put("A", 0);
 * // new MultisetPermutationBuilder&lt;&gt;(invalid, calculator); // Exception!
 * </pre>
 *
 * <h3>Performance Note</h3>
 * <pre>
 * // This implementation flattens frequencies into an initial index array.
 * // For extremely large frequencies (e.g., 10⁹⁹), this approach may be inefficient,
 * // but such cases would generate astronomically large numbers of permutations
 * // that cannot be enumerated in practice anyway.
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @see MultisetPermutation
 * @see MultisetPermutationMth
 * @see MultisetPermutationByRanks
 * @see <a href="https://en.wikipedia.org/wiki/Multinomial_coefficient">Multinomial Coefficient</a>
 * @see <a href="https://en.wikipedia.org/wiki/Permutation#Permutations_of_multisets">Permutations of Multisets</a>
 * @author Deepesh Patel
 */
public final class MultisetPermutationBuilder<T> implements Builder<T> {

    private final LinkedHashMap<T, Integer> options;
    private final Calculator calculator;

    /**
     * Constructs a builder for generating multiset permutations.
     *
     * @param options a {@link LinkedHashMap} containing elements as keys and their frequencies as values
     * @param calculator an instance of {@link Calculator} for performing combinatorial computations
     * @throws IllegalArgumentException if {@code options} is null, empty, or contains non-positive frequencies
     */
    public MultisetPermutationBuilder(LinkedHashMap<T, Integer> options, Calculator calculator) {
        if (options == null || options.isEmpty() || options.values().stream().anyMatch(f -> f <= 0)) {
            throw new IllegalArgumentException("Options must be non-null, non-empty, and contain positive frequencies");
        }
        this.options = options;
        this.calculator = calculator;
    }

    /**
     * Creates an instance of {@link MultisetPermutation} to generate all permutations in lexicographical order.
     * <p>
     * The permutations are generated based on the lexicographical order of {@code options.keySet()}.
     * </p>
     *
     * @return a new {@link MultisetPermutation} instance for generating permutations
     */
    public MultisetPermutation<T> lexOrder() {
        return new MultisetPermutation<>(options, calculator);
    }

    /**
     * Creates an instance of {@link MultisetPermutationMth} to generate the mᵗʰ permutation directly.
     * <p>
     * This method retrieves a specific permutation without generating all prior permutations, based on the
     * lexicographical order of {@code options.keySet()}. The {@code m} parameter specifies the 0-based offset
     * from the {@code start} index to select the desired permutation.
     * </p>
     *
     * @param m the 0-based offset from the start index for the desired permutation
     * @param start the starting index for permutation generation
     * @return a new {@link MultisetPermutationMth} instance for generating the mᵗʰ permutation
     * @throws IllegalArgumentException if {@code m} or {@code start} is negative
     */
    public MultisetPermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        EveryMthIterable.validateLexOrderMthParams(m,start, count());
        return new MultisetPermutationMth<>(options, m, start, calculator);
    }

    /**
     * Creates an instance of {@link MultisetPermutationMth} to generate the mᵗʰ permutation directly, using long values.
     * <p>
     * This method retrieves a specific permutation without generating all prior permutations, based on the
     * lexicographical order of {@code options.keySet()}. The {@code m} parameter specifies the 0-based offset
     * from the {@code start} index to select the desired permutation.
     * </p>
     *
     * @param m the 0-based offset from the start index for the desired permutation
     * @param start the starting index for permutation generation
     * @return a new {@link MultisetPermutationMth} instance for generating the mᵗʰ permutation
     * @throws IllegalArgumentException if {@code m} or {@code start} is negative
     */
    public MultisetPermutationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates an instance that samples multiset permutations randomly without replacement.
     * <p>
     * The total number of unique permutations is given by the multinomial coefficient n! / (m₁!·m₂!·…),
     * where mᵢ are the frequencies of the elements.
     * </p>
     *
     * @param sampleSize the number of permutations to generate; must be positive and ≤ n! / (m₁!·m₂!·…)
     * @return a new {@link MultisetPermutationByRanks} instance for random sampling
     * @throws IllegalArgumentException if {@code sampleSize} is not positive or exceeds total permutations
     */
    public MultisetPermutationByRanks<T> sample(int sampleSize) {
        BigInteger total = calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray());
        if (sampleSize <= 0 || BigInteger.valueOf(sampleSize).compareTo(total) > 0) {
            throw new IllegalArgumentException("Sample size must be positive and not exceed total permutations");
        }
        return new MultisetPermutationByRanks<>(options, new BigIntegerSample(total, sampleSize), calculator);
    }

    /**
     * Creates an instance that samples multiset permutations randomly with replacement.
     * <p>
     * The total number of unique permutations is given by the multinomial coefficient n! / (m₁!·m₂!·…),
     * where mᵢ are the frequencies of the elements.
     * </p>
     *
     * @param sampleSize the number of permutations to generate; must be positive
     * @return a new {@link MultisetPermutationByRanks} instance for random sampling with replacement
     * @throws IllegalArgumentException if {@code sampleSize} is not positive
     */
    public MultisetPermutationByRanks<T> choice(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive");
        }
        BigInteger total = calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray());
        return new MultisetPermutationByRanks<>(options, new BigIntegerChoice(total, sampleSize), calculator);
    }

    /**
     * Generates unique multiset permutations at specified lexicographical rank positions.
     * <p>
     * <b>Example for [A, A, B, C]:</b>
     * <pre>
     * Rank | Permutation    | Note
     * -----|---------------|------
     * 0    | [A, A, B, C]  |
     * 1    | [A, A, C, B]  |
     * 2    | [A, B, A, C]  |
     * ...  | ...           | (12 total unique permutations)
     * 11   | [C, B, A, A]  |
     *
     * byRanks([0, 2, 11]) → [A,A,B,C], [A,B,A,C], [C,B,A,A]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < n! / (m₁!·m₂!·…)), where mᵢ are duplicate counts
     * @return a new {@link MultisetPermutationByRanks} instance for generating permutations at specified ranks
     * @throws IllegalArgumentException if any rank ≥ n! / (m₁!·m₂!·…)
     * @throws IllegalStateException if no generation strategy was selected
     */
    public MultisetPermutationByRanks<T> byRanks(Iterable<BigInteger> ranks) {
        EveryMthIterable.validateByRanksParams(ranks);
        return new MultisetPermutationByRanks<>(options, ranks, calculator);
    }

    /**
     * Returns the total number of unique multiset permutations.
     *
     * @return the total count as a {@link BigInteger}
     */
    public BigInteger count() {
        return calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public String toString() {
        return "MultisetPermutationBuilder{" +
                "options=" + options +
                ", count=" + count() +
                '}';
    }
}