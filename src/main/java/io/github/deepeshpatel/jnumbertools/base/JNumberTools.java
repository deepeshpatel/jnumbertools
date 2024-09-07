/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.base;

public final class JNumberTools {

    private JNumberTools() {
    }

    /**
     * utility method to generate permutations.
     * Examples:
     * <pre>
     *     all unique permutations in lex order
     *     JNumberTools.permutations().unique("A","B","C").lexOrder().forEach(System.out::println);
     *
     *     every mth unique permutation
     *     JNumberTools.permutations().unique("A","B","C").lexOrderMth(3,0).forEach(System.out::println);
     *
     *     all repetitive permutations
     *
     *
     * </pre>
     * @return Permutations A helper class to generate various types of permutations.
     *
     */
    public static Permutations permutations() {
        return new Permutations();
    }

    public static Combinations combinations() {
        return new Combinations();
    }

    public static Subsets subsets() {
        return new Subsets();
    }

    public static RankOf rankOf() {
        return new RankOf();
    }

    public static UnrankOf unRankingOf() {
        return new UnrankOf();
    }

    public static CartesianProduct cartesianProduct() {
        return new CartesianProduct(new Calculator());
    }



}
