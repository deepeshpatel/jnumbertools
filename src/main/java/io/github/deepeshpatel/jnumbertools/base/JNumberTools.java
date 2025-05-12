/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

/**
 * Factory for generating permutations, combinations, subsets, and other combinatorial structures.
 * <p>
 * JNumberTools provides utility methods to create instances for various combinatorial operations,
 * including permutations (`ⁿ!`, `ⁿPₖ`, `nᵣ`), combinations (`ⁿCᵣ`, `ⁿ⁺ᵣ⁻¹Cᵣ`), subsets (`2ⁿ`),
 * ranking, unranking, Cartesian products (`n₁ × n₂ × ...`), and number system conversions
 * (permutadic, combinadic, factoradic). Each method returns a specialized builder or helper class.
 * </p>
 * Example usage:
 * <pre>
 * // All unique permutations in lexicographical order
 * JNumberTools.permutations().unique("A", "B", "C").lexOrder().forEach(System.out::println);
 *
 * // 3rd unique permutation (0-based rank)
 * JNumberTools.permutations().unique("A", "B", "C").lexOrderMth(3, 0).forEach(System.out::println);
 *
 * // All unique combinations of 2 elements from 3
 * JNumberTools.combinations().unique(2, "A", "B", "C").lexOrder().forEach(System.out::println);
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class JNumberTools {

    /**
     * Private constructor to prevent instantiation.
     */
    private JNumberTools() {
        // Prevent instantiation
    }

    /**
     * Creates a Permutations instance for generating permutations.
     * <p>
     * Supports unique permutations (`ⁿ!`), k-permutations (`ⁿPₖ`), repetitive permutations
     * (`nᵣ`), and multiset permutations, in lexicographical, single-swap, or combination order,
     * or by rank.
     * </p>
     *
     * @return a Permutations instance for permutation generation
     */
    public static Permutations permutations() {
        return new Permutations();
    }

    /**
     * Creates a Combinations instance for generating combinations.
     * <p>
     * Supports unique combinations (`ⁿCᵣ`), repetitive combinations (`ⁿ⁺ᵣ⁻¹Cᵣ`), and multiset
     * combinations, in lexicographical order or by rank.
     * </p>
     *
     * @return a Combinations instance for combination generation
     */
    public static Combinations combinations() {
        return new Combinations();
    }

    /**
     * Creates a Subsets instance for generating subsets.
     * <p>
     * Generates all 2ⁿ subsets of a set in lexicographical order.
     * </p>
     *
     * @return a Subsets instance for subset generation
     */
    public static Subsets subsets() {
        return new Subsets();
    }

    /**
     * Creates a RankOf instance for ranking permutations and combinations.
     * <p>
     * Supports ranking of unique permutations (`ⁿ!`), k-permutations (`ⁿPₖ`), repetitive
     * permutations (`nᵣ`), and unique combinations (`ⁿCᵣ`) in lexicographical order.
     * </p>
     *
     * @return a RankOf instance for ranking operations
     */
    public static RankOf rankOf() {
        return new RankOf();
    }

    /**
     * Creates an UnrankOf instance for unranking permutations and combinations.
     * <p>
     * Supports unranking of unique permutations (`ⁿ!`), k-permutations (`ⁿPₖ`), and unique
     * combinations (`ⁿCᵣ`) to retrieve the structure at a given lexicographical rank.
     * </p>
     *
     * @return an UnrankOf instance for unranking operations
     */
    public static UnrankOf unrankOf() {
        return new UnrankOf();
    }

    /**
     * Creates a CartesianProduct instance for generating Cartesian products.
     * <p>
     * Generates the Cartesian product (`n₁ × n₂ × ...`) of multiple sets, representing all
     * possible tuples.
     * </p>
     *
     * @return a CartesianProduct instance for Cartesian product generation
     */
    public static CartesianProduct cartesianProduct() {
        return new CartesianProduct(new Calculator());
    }

    /**
     * Creates a NumberSystem instance for converting numbers to specialized number systems.
     * <p>
     * Supports conversions to permutadic (`ⁿPₖ`), combinadic (`ⁿCᵣ`), and factoradic (`ⁿ!`)
     * number systems for ranking and unranking permutations and combinations.
     * </p>
     *
     * @return a NumberSystem instance for number system conversions
     */
    public static NumberSystem numberSystem() {
        return new NumberSystem(new Calculator());
    }
}