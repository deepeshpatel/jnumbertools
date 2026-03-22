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
 * - Unique permutations (`n!`), k-permutations (`ⁿPₖ`), repetitive permutations (`nᵣ`), and multiset permutations.
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
 * JNumberTools.permutations().unique("A", "B", "C").lexOrder().forEach(System.out::println);
 *
 * // Generate multiset combinations via generator.combination subpackage
 * var elements = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
 * JNumberTools.combinations().multiset(elements, 2).lexOrder().forEach(System.out::println);
 * ```
 *
 * @see io.github.deepeshpatel.jnumbertools.base
 * @see io.github.deepeshpatel.jnumbertools.api.Permutations
 * @see io.github.deepeshpatel.jnumbertools.api.Combinations
 * @see io.github.deepeshpatel.jnumbertools.api.Subsets
 * @see io.github.deepeshpatel.jnumbertools.api.CartesianProduct
 * @see io.github.deepeshpatel.jnumbertools.api.RankOf
 * @see io.github.deepeshpatel.jnumbertools.api.UnrankOf
 * @see io.github.deepeshpatel.jnumbertools.api.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator;
