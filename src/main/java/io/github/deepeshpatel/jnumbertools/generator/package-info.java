/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
/**
 * Base package for combinatorial generator classes.
 *
 * Contains subpackages (e.g., generator.permutation, generator.combination, generator.subset, generator.product)
 * with classes for generating combinatorial structures, used via builder classes in the base package.
 * Supports generation of:
 * - Unique permutations (`ⁿ!`), k-permutations (`ⁿPₖ`), repetitive permutations (`nᵣ`), and multiset permutations.
 * - Unique combinations (`ⁿCᵣ`) and multiset combinations.
 * - Subsets (`2ⁿ`).
 * - Simple and constrained Cartesian products.
 * - Ranking (RankOf) and unranking (UnrankOf) of permutations and combinations.
 *
 * Generators produce structures in lexicographical order, by specific ranks, or every mᵗʰ element.
 * Thread safety depends on the implementation; builders in the base package are typically thread-safe.
 *
 * Example usage:
 * ```
 * // Generate unique permutations via base package builder
 * Permutations permutations = new Permutations();
 * permutations.unique("A", "B", "C").lexOrder().forEach(System.out::println);
 *
 * // Generate multiset combinations via generator.combination subpackage
 * MultisetCombinationBuilder<String> builder = new MultisetCombinationBuilder<>(new int[]{2, 1}, List.of("A", "B"));
 * builder.lexOrder().forEach(System.out::println);
 * ```
 *
 * @see io.github.deepeshpatel.jnumbertools.base
 * @see io.github.deepeshpatel.jnumbertools.base.Permutations
 * @see io.github.deepeshpatel.jnumbertools.base.Combinations
 * @see io.github.deepeshpatel.jnumbertools.base.Subsets
 * @see io.github.deepeshpatel.jnumbertools.base.CartesianProduct
 * @see io.github.deepeshpatel.jnumbertools.base.RankOf
 * @see io.github.deepeshpatel.jnumbertools.base.UnrankOf
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 */
package io.github.deepeshpatel.jnumbertools.generator;