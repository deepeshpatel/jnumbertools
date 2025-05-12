/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.base;

/**
 * Interface for generating the mᵗʰ element in combinatorial sequences.
 *
 * Defines the contract for generators that produce specific ranks (mᵗʰ elements) of permutations
 * (`ⁿ!`, `ⁿPₖ`), combinations (`ⁿCᵣ`), multiset combinations, or other structures in lexicographical
 * order. Acts as a marker or base interface for rank-based generators, with implementations providing
 * methods to retrieve elements at given ranks. Thread safety depends on the implementation; generators
 * may not be thread-safe unless specified.
 *
 * Example usage (hypothetical implementation):
 * ```
 * MthElementGenerator<String> generator = new SomeMthGenerator<>(List.of("A", "B", "C"));
 * // Assuming a method getRank(BigInteger rank)
 * List<String> permutation = generator.getRank(BigInteger.valueOf(2)); // e.g., [A, C, B]
 * ```
 *
 * @param <E> the type of elements in the generated structures
 * @see io.github.deepeshpatel.jnumbertools.generator.base.Builder
 * @see io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public interface MthElementGenerator<E> {

}