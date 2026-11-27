/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.builders.PermutationsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive.RepetitivePermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationBuilder;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Static entry point for permutation generation.
 *
 * <p>Supports unique permutations, k-permutations, repetitive permutations,
 * and multiset permutations. The returned builders provide the traversal,
 * ranking, and sampling operations available for each permutation type.</p>
 *
 * <pre>{@code
 * Permutation.unique("A", "B", "C").lexOrder();
 * Permutation.nPk(2, "A", "B", "C").lexOrder();
 * Permutation.repetitive(3, "A", "B").lexOrder();
 * Permutation.multiset(multiset).lexOrder();
 * }</pre>
 *
 * @see PermutationsBuilder
 * @author Deepesh Patel
 */
public final class Permutation {

    private Permutation() {
        // Static factory class.
    }

    private static PermutationsBuilder factory() {
        return new PermutationsBuilder();
    }

    public static UniquePermutationBuilder<Integer> unique(int n) {
        return factory().unique(n);
    }

    public static <T> UniquePermutationBuilder<T> unique(List<T> elements) {
        return factory().unique(elements);
    }

    @SafeVarargs
    public static <T> UniquePermutationBuilder<T> unique(T... elements) {
        return factory().unique(elements);
    }

    public static KPermutationBuilder<Integer> nPk(int n, int k) {
        return factory().nPk(n, k);
    }

    public static <T> KPermutationBuilder<T> nPk(int k, List<T> elements) {
        return factory().nPk(k, elements);
    }

    @SafeVarargs
    public static <T> KPermutationBuilder<T> nPk(int k, T... elements) {
        return factory().nPk(k, elements);
    }

    public static <T> MultisetPermutationBuilder<T> multiset(LinkedHashMap<T, Integer> options) {
        return factory().multiset(options);
    }

    public static <T> RepetitivePermutationBuilder<T> repetitive(int r, List<T> elements) {
        return factory().repetitive(r, elements);
    }

    @SafeVarargs
    public static <T> RepetitivePermutationBuilder<T> repetitive(int r, T... elements) {
        return factory().repetitive(r, elements);
    }

    public static RepetitivePermutationBuilder<Integer> repetitive(int r, int n) {
        return factory().repetitive(r, n);
    }
}
