/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Resolves rule dimensions by caller-list identity and compiles value predicates into dimension-index bit sets.
 */
final class RuleCompiler {
    private final List<List<?>> dimensions;
    private final List<List<?>> originals;
    private final DimensionIndexes indexes;

    RuleCompiler(List<List<?>> dimensions, List<List<?>> originals, DimensionIndexes indexes) {
        this.dimensions = dimensions;
        this.originals = originals;
        this.indexes = indexes;
    }

    int indexOf(List<?> dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null");
        int found = -1;
        for (int i = 0; i < originals.size(); i++) {
            if (originals.get(i) == dimension) {
                if (found >= 0) {
                    throw new IllegalArgumentException("The same list instance is used for more than one "
                            + "dimension (positions " + found + " and " + i + "), so the rule is ambiguous; "
                            + "pass distinct list instances");
                }
                found = i;
            }
        }
        if (found < 0) {
            throw new IllegalArgumentException("The list is not a dimension of this product; pass the same "
                    + "List instance that was given to conditionalProductOf(...) or and(...)");
        }
        return found;
    }

    /**
     * Returns the indexes of values in dimension {@code dim} that match
     * {@code values}. If {@code negate} is true, returns the complement:
     * indexes of values that do not match any of the supplied values.
     */
    /**
     * Returns the positions in dimension {@code dim} accepted by {@code predicate}.
     * The predicate is evaluated once per position during rule compilation, never during generation.
     */
    BitSet matching(int dim, Predicate<Object> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        List<?> values = dimensions.get(dim);
        BitSet result = new BitSet(values.size());
        for (int position = 0; position < values.size(); position++) {
            if (predicate.test(values.get(position))) {
                result.set(position);
            }
        }
        return result;
    }

    BitSet matching(int dim, Collection<?> values, boolean negate) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
        Map<Object, int[]> positions = indexes.of(dim);
        int size = dimensions.get(dim).size();
        BitSet result = new BitSet(size);
        Set<Object> missing = null;
        for (Object value : values) {
            Objects.requireNonNull(value, "a rule value must not be null");
            int[] at = positions.get(value);
            if (at == null) {
                if (missing == null) missing = new LinkedHashSet<>();
                missing.add(value);
            } else {
                for (int position : at) result.set(position);
            }
        }
        if (missing != null) {
            throw new IllegalArgumentException("Not a value of that dimension: " + missing);
        }
        if (negate) {
            result.flip(0, size);
        }
        return result;
    }
}
