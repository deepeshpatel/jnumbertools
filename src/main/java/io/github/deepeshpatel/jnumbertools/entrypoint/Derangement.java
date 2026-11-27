/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.builders.DerangementsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.derangement.DerangementBuilder;

import java.util.List;

/**
 * Static entry point for derangement generation.
 *
 * <p>A derangement is a permutation in which no element remains in its
 * original position.</p>
 *
 * <p>The static factories use an internally managed {@link io.github.deepeshpatel.jnumbertools.datastructure.Calculator}
 * for convenience. Advanced users who want to share calculation caches across generators can
 * instantiate {@link DerangementBuilder} directly and pass the same calculator to multiple
 * builders. For example:</p>
 *
 * <pre>{@code
 * Calculator calculator = new Calculator();
 * var first = new DerangementBuilder<>(calculator, List.of("A", "B", "C"));
 * var second = new DerangementBuilder<>(calculator, List.of("D", "E", "F"));
 * }</pre>
 *
 * <p>Import {@code Calculator} from {@code io.github.deepeshpatel.jnumbertools.datastructure}
 * and {@code DerangementBuilder} from
 * {@code io.github.deepeshpatel.jnumbertools.generator.permutation.derangement}.</p>
 *
 * <pre>{@code
 * Derangement.of(4).lexOrder();
 * Derangement.of("A", "B", "C", "D").lexOrder();
 * }</pre>
 *
 * @see DerangementsBuilder
 * @author Deepesh Patel &amp; Aditya Patel
 */
public final class Derangement {

    private Derangement() {
        // Static factory class.
    }

    private static DerangementsBuilder factory() {
        return new DerangementsBuilder();
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
