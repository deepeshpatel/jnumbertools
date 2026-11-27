package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.builders.CombinationUnrankOfBuilder;
import io.github.deepeshpatel.jnumbertools.builders.PermutationUnrankOfBuilder;

public class Unrank {

    public static PermutationUnrankOfBuilder permutation() {
        return new PermutationUnrankOfBuilder(new Calculator());
    }

    public static PermutationUnrankOfBuilder permutation(Calculator sharedCalculator) {
        return new PermutationUnrankOfBuilder(sharedCalculator);
    }

    /** Returns unranking operations for combinations. */
    public static CombinationUnrankOfBuilder combination() {
        return new CombinationUnrankOfBuilder(new Calculator());
    }

    public static CombinationUnrankOfBuilder combination(Calculator sharedCalculator) {
        return new CombinationUnrankOfBuilder(sharedCalculator);
    }
}
