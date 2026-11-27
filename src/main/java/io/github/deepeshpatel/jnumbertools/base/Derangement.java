/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.permutation.derangement.DerangementBuilder;

import java.util.List;

/**
 * Static entry point for derangement generation.
 *
 * <p>A derangement is a permutation in which no element remains in its
 * original position.</p>
 *
 * <pre>{@code
 * Derangement.of(4).lexOrder();
 * Derangement.of("A", "B", "C", "D").lexOrder();
 * }</pre>
 *
 * @see Derangements
 * @author Deepesh Patel &amp; Aditya Patel
 */
public final class Derangement {

    private Derangement() {
        // Static factory class.
    }

    private static Derangements factory() {
        return new Derangements();
    }

    public static DerangementBuilder<Integer> of(int n) {
        return factory().of(n);
    }

    public static <T> DerangementBuilder<T> of(List<T> elements) {
        return factory().of(elements);
    }

    @SafeVarargs
    public static <T> DerangementBuilder<T> of(T... elements) {
        return factory().of(elements);
    }
}
