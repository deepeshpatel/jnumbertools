/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Resolves rule dimensions by caller-list identity and compiles value predicates into dimension-index bit sets.
 */
final class RuleCompiler {
    private final List<List<?>> dimensions;
    private final List<List<?>> originals;

    RuleCompiler(List<List<?>> dimensions, List<List<?>> originals) {
        this.dimensions = dimensions;
        this.originals = originals;
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

    /** Indexes of the values of dimension {@code dim} that equal one of {@code values} (or the others). */
    BitSet matching(int dim, Collection<?> values, boolean negate) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
        Set<Object> wanted = new HashSet<>();
        for (Object value : values) {
            wanted.add(Objects.requireNonNull(value, "a rule value must not be null"));
        }
        List<?> list = dimensions.get(dim);
        BitSet result = new BitSet(list.size());
        Set<Object> seen = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (wanted.contains(element)) {
                result.set(i);
                seen.add(element);
            }
        }
        if (seen.size() < wanted.size()) {
            Set<Object> missing = new HashSet<>(wanted);
            missing.removeAll(seen);
            throw new IllegalArgumentException("Not a value of that dimension: " + missing);
        }
        if (negate) {
            result.flip(0, list.size());
        }
        return result;
    }
}
