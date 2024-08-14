/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.product.Product;

import java.util.List;

public class JNumberTools {

    private final Calculator calculator;

    public JNumberTools() {
       this(new Calculator());
    }

    public JNumberTools(Calculator calculator) {
        this.calculator = calculator;
    }

    public Permutations permutations() {
        return new Permutations(calculator);
    }

    public Combinations combinations() {
        return new Combinations(calculator);
    }

    public static Subsets subsets() {
        return new Subsets();
    }

    public RankOf rankOf() {
        return new RankOf(calculator);
    }

    public UnrankOf unRankingOf() {
        return new UnrankOf(calculator);
    }

    public static <T> Product<T> productOf(int n, List<T> data ) {
            return Product.of(n, data);
    }

}
