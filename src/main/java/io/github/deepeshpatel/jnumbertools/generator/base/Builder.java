/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.List;

/**
 * Interface for building generators of combinatorial structures.
 *
 * Defines methods to create generators for permutations (`ⁿ!`, `ⁿPₖ`), combinations (`ⁿCᵣ`),
 * multiset combinations, and other structures, in lexicographical order or every mᵗʰ element.
 * Provides the total count of generatable structures. Implementations are expected to be thread-safe
 * where applicable, depending on the specific generator.
 *
 * Example usage:
 * ```
 * // Example with a concrete builder (e.g., UniquePermutationBuilder)
 * Builder<String> builder = new UniquePermutationBuilder<>(List.of("A", "B", "C"));
 * builder.lexOrder().forEach(System.out::println); // All permutations in lex order
 * builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ZERO).forEach(System.out::println); // Every 2nd permutation
 * ```
 *
 * @param <E> the type of elements in the generated structures
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public interface Builder<E> {

    /**
     * Creates a generator for all elements in lexicographical order.
     *
     * Generates all structures (e.g., `ⁿ!` permutations or `ⁿCᵣ` combinations) in lexicographical order.
     *
     * @return an AbstractGenerator producing elements in lexicographical order
     */
    AbstractGenerator<E> lexOrder();

    /**
     * Creates a generator for every mᵗʰ element in lexicographical order, starting from a given rank.
     *
     * Generates structures (e.g., `ⁿ!` permutations or `ⁿCᵣ` combinations) at ranks start, start+m, start+2m, etc.
     *
     * @param m the step size for generating elements (m > 0)
     * @param start the starting rank (start ≥ 0)
     * @return an Iterable producing every mᵗʰ element in lexicographical order
     */
    Iterable<List<E>> lexOrderMth(BigInteger m, BigInteger start);

    /**
     * Returns the total number of generatable structures.
     *
     * Represents the count of structures (e.g., `ⁿ!` for permutations, `ⁿCᵣ` for combinations).
     *
     * @return the total count as a BigInteger
     */
    BigInteger count();
}