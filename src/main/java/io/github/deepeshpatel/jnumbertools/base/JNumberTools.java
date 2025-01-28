/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.base;

/**
 * Provides utility methods for generating permutations, combinations, subsets, and more.
 * The {@code JNumberTools} class acts as a factory for various combinatorial operations
 * and is designed to simplify the creation and management of these operations.
 *
 * <p>
 * Examples:
 * <pre>
 *     // All unique permutations in lexicographic order
 *     JNumberTools.permutations().unique("A", "B", "C").lexOrder().forEach(System.out::println);
 *
 *     // Every mth unique permutation
 *     JNumberTools.permutations().unique("A", "B", "C").lexOrderMth(3, 0).forEach(System.out::println);
 *
 *     // All repetitive permutations
 *     // (example usage not provided in the original comment)
 * </pre>
 */
public final class JNumberTools {

    private JNumberTools() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns a {@code Permutations} instance to generate various types of permutations.
     *
     * @return A helper class to generate permutations.
     */
    public static Permutations permutations() {
        return new Permutations();
    }

    /**
     * Returns a {@code Combinations} instance to generate various types of combinations.
     *
     * @return A helper class to generate combinations.
     */
    public static Combinations combinations() {
        return new Combinations();
    }

    /**
     * Returns a {@code Subsets} instance to generate subsets.
     *
     * @return A helper class to generate subsets.
     */
    public static Subsets subsets() {
        return new Subsets();
    }

    /**
     * Returns a {@code RankOf} instance for operations related to ranking.
     *
     * @return A helper class to determine rank-related operations.
     */
    public static RankOf rankOf() {
        return new RankOf();
    }

    /**
     * Returns a {@code UnrankOf} instance for operations related to unRanking.
     *
     * @return A helper class to determine unranked operations.
     */
    public static UnrankOf unRankingOf() {
        return new UnrankOf();
    }

    /**
     * Returns a {@code CartesianProduct} instance to generate Cartesian products of elements.
     *
     * @return A helper class to generate Cartesian products.
     */
    public static CartesianProduct cartesianProduct() {
        return new CartesianProduct(new Calculator());
    }

    public static NumberSystem numberSystem() {
        return new NumberSystem(new Calculator());
    }
}
