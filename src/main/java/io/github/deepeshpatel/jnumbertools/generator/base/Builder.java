/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.Random;

/**
 * Interface for building generators of combinatorial structures.
 * <p>
 * Defines methods to create generators for permutations (n!, ⁿPₖ), combinations (ⁿCᵣ),
 * multiset combinations, and other structures, in lexicographical order, every mᵗʰ element,
 * or based on specific ranks. Provides the total count of generatable structures.
 * </p>
 * <p>
 * Implementations are expected to be immutable and thread-safe. Configuration methods should return new instances.
 * </p>
 * Example usage:
 * <pre>
 * // Example with a concrete builder (e.g., UniquePermutationBuilder)
 * Builder&lt;String&gt; builder = new UniquePermutationBuilder&lt;&gt;(List.of("A", "B", "C"));
 * builder.lexOrder().stream().forEach(System.out::println); // All permutations in lex order
 * builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ZERO).stream().forEach(System.out::println); // Every 2nd permutation
 * builder.byRanks(List.of(BigInteger.ZERO)).stream().forEach(System.out::println); // Permutations at specific ranks
 * </pre>
 *
 * @param <E> the type of elements in the generated structures
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public interface Builder<E> {

    /**
     * Creates a generator for all elements in lexicographical order.
     * Generates all structures (e.g., n! permutations or ⁿCᵣ combinations) in lexicographical order.
     * @return a StreamableIterable producing elements in lexicographical order
     */
    StreamableIterable<E> lexOrder();

    /**
     * Convenience method for lexOrderMth using long values.
     *
     * @param m     the interval to select every mᵗʰ element (must be > 0)
     * @param start the starting rank (must be ≥ 0 and &lt; total count when count > 0)
     * @return a generator for every mᵗʰ element in lexicographical order
     * @throws IllegalArgumentException if m ≤ 0, start &lt; 0, or start ≥ count() when count() > 0
     */
    default StreamableIterable<E> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a generator for every mᵗʰ element in lexicographical order, starting from a given rank.
     * Generates structures (e.g., n! permutations or ⁿCᵣ combinations) at ranks start, start+m, start+2m, etc.
     * @param m the step size for generating elements (m > 0)
     * @param start the starting rank (start ≥ 0)
     * @return a StreamableIterable producing every mᵗʰ element in lexicographical order
     */
    StreamableIterable<E> lexOrderMth(BigInteger m, BigInteger start);

    /**
     * Creates a generator for elements at specified rank positions.
     * Generates structures (e.g., n! permutations or ⁿCᵣ combinations) corresponding to the provided ranks.
     * @param ranks an iterable of 0-based rank numbers
     * @return a StreamableIterable producing elements at the specified ranks
     */
    StreamableIterable<E> byRanks(Iterable<BigInteger> ranks);

    /**
     * Generates a random sample of elements with replacement using custom random generator.
     *
     * @param sampleSize the number of elements to generate
     * @param random the random generator to use
     * @return a generator producing random elements (duplicates allowed)
     * @throws IllegalArgumentException if sampleSize is negative or random is null
     */
    StreamableIterable<E> choice(int sampleSize, Random random);

    /**
     * Generates a random sample of unique elements without replacement using custom random generator.
     *
     * @param sampleSize the number of unique elements to generate
     * @param random the random generator to use
     * @return a generator producing unique random elements
     * @throws IllegalArgumentException if sampleSize is negative, exceeds total count, or random is null
     */
    StreamableIterable<E> sample(int sampleSize, Random random);

    /**
     * Returns the total number of generatable structures.
     * Represents the count of structures (e.g., n! for permutations, ⁿCᵣ for combinations).
     * @return the total count as a BigInteger
     */
    BigInteger count();

    /**
     * Returns {@code true} if and only if this builder yields no results at all,
     * i.e. {@code count().signum() == 0}.
     * <p>
     * A builder whose single result is the empty list ({@code [[]]}, e.g. the permutations of an
     * empty set, or a selection of size 0) is <b>not</b> empty: it has {@code count() == 1}.
     * See {@code ValidationRules.md}.
     * </p>
     *
     * @return true if {@code count()} is zero
     */
    boolean isEmpty();
}