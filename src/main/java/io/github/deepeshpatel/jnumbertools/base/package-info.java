/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
/**
 * Contains builder classes for the major public APIs in JNumberTools.
 *
 * This package provides the entry point for creating generators of combinatorial structures, such as
 * permutations (`ⁿ!`, `ⁿPₖ`, `nᵣ`, multiset), combinations (`ⁿCᵣ`, multiset), subsets (`2ⁿ`), Cartesian
 * products, and number system conversions (permutadic, combinadic, factoradic). Key classes include
 * JNumberTools, Calculator, Permutations, Combinations, Subsets, RankOf, UnrankOf, CartesianProduct,
 * and NumberSystem. These classes support generating structures in lexicographical order, by rank, or
 * every mᵗʰ element, with thread-safe operations unless specified otherwise.
 *
 * Example usage:
 * ```
 * // Generate all unique permutations of {A, B, C}
 * Permutations permutations = new Permutations();
 * permutations.unique("A", "B", "C").lexOrder().forEach(System.out::println);
 *
 * // Generate all unique combinations of 2 elements from {A, B, C}
 * Combinations combinations = new Combinations();
 * combinations.unique(2, "A", "B", "C").lexOrder().forEach(System.out::println);
 * ```
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 */
package io.github.deepeshpatel.jnumbertools.base;