/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.base;

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
 * Builder<String> builder = new UniquePermutationBuilder<>(List.of("A", "B", "C"));
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
     * @param start the starting rank (must be ≥ 0 and < total count when count > 0)
     * @return a generator for every mᵗʰ element in lexicographical order
     * @throws IllegalArgumentException if m ≤ 0, start < 0, or start ≥ count() when count() > 0
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

    boolean isEmpty();

    /**
     * Validates parameters commonly used in lexOrderMth(m, start) calls, including upper bound check.
     * <p>
     * Builders should call this method before creating a generator to ensure fail-fast validation.
     * This enhanced version also validates that the starting rank is within the valid range
     * of available elements.
     * </p>
     *
     * @param m     increment/step size (must be positive)
     * @param start starting rank (must be non-negative and less than count)
     * @param count total number of elements available (must be non-negative)
     * @throws IllegalArgumentException if:
     *         <ul>
     *           <li>m is null or ≤ 0</li>
     *           <li>start is null or < 0</li>
     *           <li>start is ≥ count (out of valid range)</li>
     *         </ul>
     */

    default void validateLexOrderMthParams(BigInteger m, BigInteger start, BigInteger count) {
        validateMthParams(m, start, count);
    }

    static void validateMthParams(BigInteger m, BigInteger start, BigInteger count) {
        if (m == null || m.signum() <= 0) {
            throw new IllegalArgumentException(String.format("Increment 'm' must be positive (m > 0). Found " + m));
        }

        if (start == null ||
                start.signum() < 0 ||
                (start.compareTo(count) >= 0 && count.signum()> 0 )) {
            var msg = String.format("Element should be in range [0,%s). Found %s", count, start);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Validates parameters commonly used in byRanks(ranks) calls.
     * <p>
     * Builders should call this method before creating a generator
     * to ensure fail-fast validation for null parameters only.
     * Invalid rank values (negative, out of bounds) will be caught
     * naturally during iteration without performance overhead.
     * </p>
     * @param ranks iterable of ranks to validate
     * @throws IllegalArgumentException if ranks is null
     */
    default void validateByRanksParams(Iterable<BigInteger> ranks) {
        validateRanks(ranks);
    }

    static void validateRanks(Iterable<BigInteger> ranks) {
        if (ranks == null) {
            throw new IllegalArgumentException("Ranks sequence cannot be null");
        }
    }
}
