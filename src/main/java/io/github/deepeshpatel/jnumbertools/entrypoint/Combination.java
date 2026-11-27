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

    public static UniqueCombinationBuilder<Integer> unique(int n, int r) {
        return factory().unique(n, r);
    }

    @SafeVarargs
    public static <T> UniqueCombinationBuilder<T> unique(int r, T... elements) {
        return factory().unique(r, elements);
    }

    public static <T> UniqueCombinationBuilder<T> unique(int r, List<T> elements) {
        return factory().unique(r, elements);
    }

    public static RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        return factory().repetitive(n, r);
    }

    @SafeVarargs
    public static <T> RepetitiveCombinationBuilder<T> repetitive(int r, T... elements) {
        return factory().repetitive(r, elements);
    }

    public static <T> RepetitiveCombinationBuilder<T> repetitive(int r, List<T> elements) {
        return factory().repetitive(r, elements);
    }

    public static <T> MultisetCombinationBuilder<T> multiset(LinkedHashMap<T, Integer> options, int r) {
        return factory().multiset(options, r);
    }
}
