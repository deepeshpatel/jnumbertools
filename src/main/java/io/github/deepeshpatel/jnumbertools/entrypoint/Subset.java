/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.builders.SubsetsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetSelector;

import java.util.List;

/**
 * Static entry point for subset generation.
 *
 * <p>Use {@code all()} or {@code inRange(from, to)} on the returned selector
 * to choose which subset sizes to generate.</p>
 *
 * <p>The static factories use an internally managed {@link io.github.deepeshpatel.jnumbertools.datastructure.Calculator}
 * for convenience. Advanced users who want to share calculation caches across selectors can
 * instantiate {@link SubsetSelector} directly and pass the same calculator to multiple selectors.
 * For example:</p>
 *
 * <pre>{@code
 * import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
 * import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetSelector;
 * import java.util.List;
 *
 * Calculator calculator = new Calculator();
 * var first = new SubsetSelector<>(List.of("A", "B", "C"), calculator);
 * var second = new SubsetSelector<>(List.of("D", "E", "F"), calculator);
 * }</pre>
 *
 * <pre>{@code
 * Subset.of("A", "B", "C").all().lexOrder();
 * Subset.of("A", "B", "C").inRange(1, 2).lexOrder();
 * Subset.of(5).all().lexOrder();
 * }</pre>
 *
 * @see SubsetsBuilder
 * @author Deepesh Patel
 */
public final class Subset {

    private Subset() {
        // Static factory class.
    }

    private static SubsetsBuilder factory() {
        return new SubsetsBuilder();
    }

    @SafeVarargs
    public static <T> SubsetSelector<T> of(T... elements) {
        return factory().of(elements);
    }

    public static <T> SubsetSelector<T> of(List<T> elements) {
        return factory().of(elements);
    }

    public static SubsetSelector<Integer> of(int dataSize) {
        return factory().of(dataSize);
    }
}
