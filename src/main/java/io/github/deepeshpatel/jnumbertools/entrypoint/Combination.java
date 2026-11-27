/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.builders.CombinationsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Static entry point for combination generation.
 *
 * <p>Supports unique combinations, combinations with repetition, and
 * multiset combinations. The returned builders provide the traversal,
 * ranking, and sampling operations available for each combination type.</p>
 *
 * <p>The unique and repetitive factories use an internally managed
 * {@link io.github.deepeshpatel.jnumbertools.datastructure.Calculator}. The multiset factory does not
 * require a calculator. Advanced users who want to share calculation caches across unique or
 * repetitive combination generators can instantiate the corresponding concrete builder directly
 * and pass the same calculator to each builder. For example:</p>
 *
 * <pre>{@code
 * import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
 * import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;
 * import java.util.List;
 *
 * Calculator calculator = new Calculator();
 * var first = new UniqueCombinationBuilder<>(List.of("A", "B", "C"), 2, calculator);
 * var second = new UniqueCombinationBuilder<>(List.of("D", "E", "F"), 2, calculator);
 * }</pre>
 *
 * <pre>{@code
 * Combination.unique(2, "A", "B", "C").lexOrder();
 * Combination.repetitive(3, "A", "B").lexOrder();
 * Combination.multiset(multiset, 2).lexOrder();
 * }</pre>
 *
 * @see CombinationsBuilder
 * @author Deepesh Patel
 */
public final class Combination {

    private Combination() {
        // Static factory class.
    }

    private static CombinationsBuilder factory() {
        return new CombinationsBuilder();
    }

    /** Creates unique r-combinations from the integer range {@code [0, n)}. */
    public static UniqueCombinationBuilder<Integer> unique(int n, int r) {
        return factory().unique(n, r);
    }

    /** Creates unique r-combinations from the supplied elements. */
    @SafeVarargs
    public static <T> UniqueCombinationBuilder<T> unique(int r, T... elements) {
        return factory().unique(r, elements);
    }

    /** Creates unique r-combinations from the supplied list. */
    public static <T> UniqueCombinationBuilder<T> unique(int r, List<T> elements) {
        return factory().unique(r, elements);
    }

    /** Creates repetitive r-combinations from the integer range {@code [0, n)}. */
    public static RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        return factory().repetitive(n, r);
    }

    /** Creates repetitive r-combinations from the supplied elements. */
    @SafeVarargs
    public static <T> RepetitiveCombinationBuilder<T> repetitive(int r, T... elements) {
        return factory().repetitive(r, elements);
    }

    /** Creates repetitive r-combinations from the supplied list. */
    public static <T> RepetitiveCombinationBuilder<T> repetitive(int r, List<T> elements) {
        return factory().repetitive(r, elements);
    }

    /** Creates r-combinations from the supplied multiset frequencies. */
    public static <T> MultisetCombinationBuilder<T> multiset(LinkedHashMap<T, Integer> options, int r) {
        return factory().multiset(options, r);
    }
}
