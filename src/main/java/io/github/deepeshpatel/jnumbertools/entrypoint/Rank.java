package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.builders.CombinationRankOfBuilder;
import io.github.deepeshpatel.jnumbertools.builders.PermutationRankOfBuilder;

public class Rank {
    /** Returns ranking operations for permutations and derangements. */
    public static PermutationRankOfBuilder permutation() {
        return new PermutationRankOfBuilder(new Calculator());
    }

    public static PermutationRankOfBuilder permutation(Calculator sharedCalculator) {
        return new PermutationRankOfBuilder(sharedCalculator);
    }


    /** Returns ranking operations for combinations. */
    public static CombinationRankOfBuilder combination() {
        return new CombinationRankOfBuilder(new Calculator());
    }

    public static CombinationRankOfBuilder combination(Calculator sharedCalculator) {
        return new CombinationRankOfBuilder(sharedCalculator);
    }
}
