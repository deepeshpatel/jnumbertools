/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;

/**
 * Interface for building generators of combinatorial structures.
 * Defines methods to create generators for permutations (ⁿ!, ⁿPₖ), combinations (ⁿCᵣ),
 * multiset combinations, and other structures, in lexicographical order, every mᵗʰ element,
 * or based on specific ranks. Provides the total count of generatable structures.
 * Implementations are expected to be thread-safe where applicable, depending on the specific generator.
 * Example usage:
 * <pre>
 * // Example with a concrete builder (e.g., UniquePermutationBuilder)
 * Builder<String> builder = new UniquePermutationBuilder<>(List.of("A", "B", "C"));
 * builder.lexOrder().stream().forEach(System.out::println); // All permutations in lex order
 * builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ZERO).stream().forEach(System.out::println); // Every 2nd permutation
 * builder.byRanks(List.of(BigInteger.ZERO)).stream().forEach(System.out::println); // Permutations at specific ranks
 * </pre>
 *
 * @param <E> the type of elements in the generated structures
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public interface Builder<E> {

    /**
     * Creates a generator for all elements in lexicographical order.
     * Generates all structures (e.g., ⁿ! permutations or ⁿCᵣ combinations) in lexicographical order.
     * @return a StreamableIterable producing elements in lexicographical order
     */
    StreamableIterable<E> lexOrder();

    /**
     * Convenience method for lexOrderMth using long values.
     * @param m     the interval to select every mᵗʰ product
     * @param start the starting position
     * @return a CartesianProductByRanks for the specified intervals
     */
     default StreamableIterable<E> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
     }

    /**
     * Creates a generator for every mᵗʰ element in lexicographical order, starting from a given rank.
     * Generates structures (e.g., ⁿ! permutations or ⁿCᵣ combinations) at ranks start, start+m, start+2m, etc.
     * @param m the step size for generating elements (m > 0)
     * @param start the starting rank (start ≥ 0)
     * @return a StreamableIterable producing every mᵗʰ element in lexicographical order
     */
    StreamableIterable<E> lexOrderMth(BigInteger m, BigInteger start);

    /**
     * Creates a generator for elements at specified rank positions.
     * Generates structures (e.g., ⁿ! permutations or ⁿCᵣ combinations) corresponding to the provided ranks.
     * @param ranks an iterable of 0-based rank numbers
     * @return a StreamableIterable producing elements at the specified ranks
     */
    StreamableIterable<E> byRanks(Iterable<BigInteger> ranks);

    StreamableIterable<E> choice(int sampleSize);

    StreamableIterable<E> sample(int sampleSize);

    /**
     * Returns the total number of generatable structures.
     * Represents the count of structures (e.g., ⁿ! for permutations, ⁿCᵣ for combinations).
     * @return the total count as a BigInteger
     */
    BigInteger count();
}