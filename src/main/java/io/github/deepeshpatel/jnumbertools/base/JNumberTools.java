/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

/**
 * Central entry point for ranking, unranking, and number-system operations.
 *
 * <p>Dedicated static entry points provide generators for each combinatorial
 * family: {@link Permutation}, {@link Combination}, {@link Derangement},
 * {@link Subset}, and {@link Product}.</p>
 *
 * <h2>Ranking and Unranking</h2>
 * <pre>{@code
 * BigInteger rank = JNumberTools.rankOf()
 *     .permutation()
 *     .unique(2, 0, 1);
 *
 * int[] permutation = JNumberTools.unrankOf()
 *     .permutation()
 *     .unique(BigInteger.valueOf(5), 3);
 *
 * BigInteger combinationRank = JNumberTools.rankOf()
 *     .combination()
 *     .unique(4, 1, 3);
 * }</pre>
 *
 * @see Permutation
 * @see Combination
 * @see Derangement
 * @see Subset
 * @see Product
 * @author Deepesh Patel
 */
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.numbersystem.Combinadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.Permutadic;

/**
 * Factory for generating permutations, combinations, subsets, and other combinatorial structures.
 * <p>
 * JNumberTools provides utility methods to create instances for various combinatorial operations,
 * including permutations (n!, ⁿPₖ, nʳ), combinations (ⁿCᵣ, ⁿ⁺ʳ⁻¹Cᵣ), subsets (2ⁿ), ranking, unranking,
 * Cartesian products (n₁ × n₂ × ...), and number system conversions (permutadic, combinadic, factoradic).
 * </p>
 * <p>
 * This class is immutable and thread-safe. All methods return new builder instances.
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Permutations</h3>
 * <pre>
 * // All unique permutations in lexicographical order
 * JNumberTools.permutations()
 *     .unique("A", "B", "C")
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // 3rd unique permutation (0-based rank)
 * JNumberTools.permutations()
 *     .unique("A", "B", "C")
 *     .lexOrderMth(3, 0)
 *     .forEach(System.out::println);
 *
 * // k-permutations of size 2 from 4 elements
 * JNumberTools.permutations()
 *     .nPk(2, 0, 1, 2, 3)
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // Repetitive permutations (with replacement)
 * JNumberTools.permutations()
 *     .repetitive(3, "A", "B")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Combinations</h3>
 * <pre>
 * // All unique combinations of size 2 from 4 elements
 * JNumberTools.combinations()
 *     .unique(2, "A", "B", "C", "D")
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // Repetitive combinations (with repetition)
 * JNumberTools.combinations()
 *     .repetitive(3, "A", "B")
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Subsets</h3>
 * <pre>
 * // All subsets of [A, B, C]
 * JNumberTools.subsets()
 *     .of("A", "B", "C")
 *     .all()
 *     .lexOrder()
 *     .forEach(System.out::println);
 *
 * // Subsets of size 1 to 2
 * JNumberTools.subsets()
 *     .of("A", "B", "C")
 *     .inRange(1, 2)
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Cartesian Products</h3>
 * <pre>
 * // Simple product of multiple sets
 * JNumberTools.cartesianProduct()
 *     .simpleProductOf(List.of("A", "B"), List.of("X", "Y"), List.of(1, 2))
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Ranking and Unranking</h3>
 * <pre>
 * // Rank a unique permutation of three elements
 * BigInteger rank = JNumberTools.rankOf()
 *     .permutation()
 *     .unique(2, 0, 1);
 *
 * // Retrieve the unique permutation at rank 5
 * int[] permutation = JNumberTools.unrankOf()
 *     .permutation()
 *     .unique(BigInteger.valueOf(5), 3);
 *
 * // Rank a unique combination of size 2 from 4 elements
 * BigInteger combinationRank = JNumberTools.rankOf()
 *     .combination()
 *     .unique(4, 1, 3);
 * </pre>
 *
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class JNumberTools {

    private JNumberTools() {
        // Prevent instantiation
    }

    /**
     * Creates a RankOf instance for ranking permutations and combinations.
     * <p>
     * Supports unique, k-, repetitive, and multiset permutations; derangements; and
     * unique, repetitive, and multiset combinations in lexicographical order.
     * </p>
     *
     * @return a RankOf instance for ranking operations
     * @see RankOf
     */
    public static RankOf rankOf() {
        return new RankOf();
    }

    /**
     * Creates an UnrankOf instance for unranking permutations and combinations.
     * <p>
     * Supports unique, k-, repetitive, and multiset permutations; derangements; and
     * unique, repetitive, and multiset combinations at a given lexicographical rank.
     * </p>
     *
     * @return an UnrankOf instance for unranking operations
     * @see UnrankOf
     */
    public static UnrankOf unrankOf() {
        return new UnrankOf();
    }

    /**
     * Creates a NumberSystem instance for converting numbers to specialized number systems.
     * <p>
     * Supports conversions to permutadic (based on ⁿPₖ), combinadic (based on ⁿCᵣ),
     * and factoradic (based on n!) number systems for ranking and unranking permutations
     * and combinations.
     * </p>
     *
     * @return a NumberSystem instance for number system conversions
     * @see NumberSystem
     * @see Permutadic
     * @see Combinadic
     * @see Factoradic
     */
    public static NumberSystem numberSystem() {
        return new NumberSystem(new Calculator());
    }
}
