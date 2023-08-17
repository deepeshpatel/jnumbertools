/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.algos.RankOf;
import io.github.deepeshpatel.jnumbertools.algos.UnRankOf;

public class JNumberTools {

    private JNumberTools() { }

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

    public static UnRankOf unRankingOf() {
        return new UnRankOf();
    }

}
