/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.entrypoint;

public final class JNumberTools {

    private JNumberTools() {
    }

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
