/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.entrypoint;

public class JNumberTools {

    private JNumberTools() {
    }

    public static Permutations permutations() {
        return new Permutations(new Calculator());
    }

    public static Combinations combinations() {
        return new Combinations(new Calculator());
    }

    public static Subsets subsets() {
        return new Subsets();
    }

    public static RankOf rankOf() {
        return new RankOf(new Calculator());
    }

    public static UnrankOf unRankingOf() {
        return new UnrankOf(new Calculator());
    }

//    public static <T> ProductBuilder<T> productOf(int n, List<T> data ) {
//            return ProductBuilder.of(n, data, new Calculator());
//    }

    public static Product product() {
        return new Product(new Calculator());
    }



}
