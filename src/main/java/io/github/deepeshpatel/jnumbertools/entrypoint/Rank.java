package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.builders.CombinationRankOfBuilder;
import io.github.deepeshpatel.jnumbertools.builders.PermutationRankOfBuilder;

/** Static entry point for zero-based lexicographical ranking of permutations, derangements and combinations.
 * <p>Use {@link #permutation()} or {@link #combination()} to obtain the corresponding rank builder.
 * An optional shared {@link io.github.deepeshpatel.jnumbertools.datastructure.Calculator} can be
 * supplied to reuse combinatorial caches.</p>
 */
public final class Rank {
    private Rank() { }
    /** Returns ranking operations for permutations and derangements. */
    public static PermutationRankOfBuilder permutation() {
        return new PermutationRankOfBuilder(new Calculator());
    }

    /** Returns permutation and derangement ranking operations using a shared calculator. */
    public static PermutationRankOfBuilder permutation(Calculator sharedCalculator) {
        return new PermutationRankOfBuilder(sharedCalculator);
    }


    /** Returns ranking operations for combinations. */
    public static CombinationRankOfBuilder combination() {
        return new CombinationRankOfBuilder(new Calculator());
    }

    /** Returns combination ranking operations using a shared calculator. */
    public static CombinationRankOfBuilder combination(Calculator sharedCalculator) {
        return new CombinationRankOfBuilder(sharedCalculator);
    }
}
