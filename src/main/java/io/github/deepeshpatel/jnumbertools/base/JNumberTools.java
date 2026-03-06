/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;
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
 *     .simpleProductOf("A", "B")
 *     .and("X", "Y")
 *     .and(1, 2)
 *     .lexOrder()
 *     .forEach(System.out::println);
 * </pre>
 *
 * <h3>Ranking and Unranking</h3>
 * <pre>
 * // Get rank of a permutation
 * BigInteger rank = JNumberTools.rankOf()
 *     .permutation(List.of(1, 2, 0))
 *     .outOf(3);
 *
 * // Get permutation at rank 5
 * List<Integer> perm = JNumberTools.unrankOf()
 *     .permutation(5)
 *     .outOf(3);
 * </pre>
 *
 * @see AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class JNumberTools {

    private JNumberTools() {
        // Prevent instantiation
    }

    /**
     * Creates a Permutations instance for generating permutations.
     * <p>
     * Supports unique permutations (n!), k-permutations (ⁿPₖ), repetitive permutations (nʳ),
     * and multiset permutations, in lexicographical, single-swap, or combination order,
     * or by rank.
     * </p>
     *
     * @return a Permutations instance for permutation generation
     * @see Permutations
     */
    public static Permutations permutations() {
        return new Permutations();
    }

    /**
     * Creates a Combinations instance for generating combinations.
     * <p>
     * Supports unique combinations (ⁿCᵣ), repetitive combinations (ⁿ⁺ʳ⁻¹Cᵣ), and multiset
     * combinations, in lexicographical order or by rank.
     * </p>
     *
     * @return a Combinations instance for combination generation
     * @see Combinations
     */
    public static Combinations combinations() {
        return new Combinations();
    }

    /**
     * Creates a Subsets instance for generating subsets.
     * <p>
     * Generates all 2ⁿ subsets of a set in lexicographical order, with support for
     * size ranges, random sampling, and rank-based access.
     * </p>
     *
     * @return a Subsets instance for subset generation
     * @see Subsets
     */
    public static Subsets subsets() {
        return new Subsets();
    }

    /**
     * Creates a RankOf instance for ranking permutations and combinations.
     * <p>
     * Supports ranking of unique permutations (n!), k-permutations (ⁿPₖ), repetitive
     * permutations (nʳ), and unique combinations (ⁿCᵣ) in lexicographical order.
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
     * Supports unranking of unique permutations (n!), k-permutations (ⁿPₖ), and unique
     * combinations (ⁿCᵣ) to retrieve the structure at a given lexicographical rank.
     * </p>
     *
     * @return an UnrankOf instance for unranking operations
     * @see UnrankOf
     */
    public static UnrankOf unrankOf() {
        return new UnrankOf();
    }

    /**
     * Creates a CartesianProduct instance for generating Cartesian products.
     * <p>
     * Supports both simple products (Aⁿ) and constrained products with combinations,
     * subsets, and multi-select dimensions. Generates all possible tuples in
     * lexicographical order or by rank.
     * </p>
     *
     * @return a CartesianProduct instance for Cartesian product generation
     * @see CartesianProduct
     * @see SimpleProductBuilder
     * @see ConstrainedProductBuilder
     */
    public static CartesianProduct cartesianProduct() {
        return new CartesianProduct(new Calculator());
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
