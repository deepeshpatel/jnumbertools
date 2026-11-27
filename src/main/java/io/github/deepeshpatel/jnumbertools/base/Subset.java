/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import java.util.List;

/**
 * Static entry point for subset generation.
 *
 * <p>Use {@code all()} or {@code inRange(from, to)} on the returned selector
 * to choose which subset sizes to generate.</p>
 *
 * <pre>{@code
 * Subset.of("A", "B", "C").all().lexOrder();
 * Subset.of("A", "B", "C").inRange(1, 2).lexOrder();
 * Subset.of(5).all().lexOrder();
 * }</pre>
 *
 * @see Subsets
 * @author Deepesh Patel
 */
public final class Subset {

    private Subset() {
        // Static factory class.
    }

    private static Subsets factory() {
        return new Subsets();
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
