/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/** Shared natural-order predicates for conditional-product rules. */
final class ComparisonPredicates {
    private ComparisonPredicates() {}

    static Predicate<Object> greaterThan(Object bound) { return comparison(bound, result -> result > 0); }
    static Predicate<Object> greaterThanOrEqual(Object bound) { return comparison(bound, result -> result >= 0); }
    static Predicate<Object> lessThan(Object bound) { return comparison(bound, result -> result < 0); }
    static Predicate<Object> lessThanOrEqual(Object bound) { return comparison(bound, result -> result <= 0); }

    private static Predicate<Object> comparison(Object bound, IntPredicate test) {
        Objects.requireNonNull(bound, "comparison value must not be null");
        if (!(bound instanceof Comparable<?>)) {
            throw new IllegalArgumentException("Comparison value must implement Comparable: "
                    + bound.getClass().getName());
        }
        return value -> {
            if (value == null) throw new IllegalArgumentException("Cannot compare a null dimension value");
            if (!(value instanceof Comparable<?> comparable)) {
                throw new IllegalArgumentException("Dimension value does not implement Comparable: "
                        + value.getClass().getName());
            }
            try {
                @SuppressWarnings("unchecked")
                int result = ((Comparable<Object>) comparable).compareTo(bound);
                return test.test(result);
            } catch (ClassCastException exception) {
                throw new IllegalArgumentException("Dimension value of type " + value.getClass().getName()
                        + " cannot be compared with " + bound.getClass().getName(), exception);
            }
        };
    }
}
