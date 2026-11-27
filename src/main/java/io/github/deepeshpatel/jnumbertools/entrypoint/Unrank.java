package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.builders.CombinationUnrankOfBuilder;
import io.github.deepeshpatel.jnumbertools.builders.PermutationUnrankOfBuilder;

/** Static entry point for reconstructing permutations, derangements and combinations from zero-based ranks.
 * <p>Use {@link #permutation()} or {@link #combination()} to obtain the corresponding unrank builder.
 * An optional shared {@link io.github.deepeshpatel.jnumbertools.datastructure.Calculator} can be
 * supplied to reuse combinatorial caches.</p>
 */
public final class Unrank {

    private Unrank() { }

    /** Returns permutation and derangement unranking operations. */

    public static PermutationUnrankOfBuilder permutation() {
        return new PermutationUnrankOfBuilder(new Calculator());
    }

    /** Returns permutation and derangement unranking operations using a shared calculator. */
    public static PermutationUnrankOfBuilder permutation(Calculator sharedCalculator) {
        return new PermutationUnrankOfBuilder(sharedCalculator);
    }

    /** Returns unranking operations for combinations. */
    public static CombinationUnrankOfBuilder combination() {
        return new CombinationUnrankOfBuilder(new Calculator());
    }

    /** Returns combination unranking operations using a shared calculator. */
    public static CombinationUnrankOfBuilder combination(Calculator sharedCalculator) {
        return new CombinationUnrankOfBuilder(sharedCalculator);
    }
}
