/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Resolves rule dimensions by caller-list identity and compiles value predicates into dimension-index bit sets.
 *
 * <p>The caller-list-to-positions map is built lazily on the first {@link #indexOf(List)} call, so a
 * builder that is constructed but never gains a rule pays nothing for it. Callers that do add rules
 * pay for one build per builder instance (identity-map sized to the dimension count), and each rule
 * dimension lookup is then O(1) instead of the linear scan that would otherwise run per rule.</p>
 */
final class RuleCompiler {
    private final List<List<?>> dimensions;
    private final List<List<?>> originals;                          // kept only to build `positions` lazily
    private final DimensionIndexes indexes;
    private volatile IdentityHashMap<List<?>, int[]> positions;     // built on first indexOf()

    RuleCompiler(List<List<?>> dimensions, List<List<?>> originals, DimensionIndexes indexes) {
        this.dimensions = dimensions;
        this.originals = originals;
        this.indexes = indexes;
    }

    int indexOf(List<?> dimension) {
        Objects.requireNonNull(dimension, "dimension must not be null");
        IdentityHashMap<List<?>, int[]> map = positions;
        if (map == null) {
            synchronized (this) {
                map = positions;
                if (map == null) {
                    map = buildPositions();
                    positions = map;
                }
            }
        }
        int[] at = map.get(dimension);
        if (at == null) {
            throw new IllegalArgumentException("The list is not a dimension of this product; pass the same "
                    + "List instance that was given to conditionalProductOf(...) or and(...)");
        }
        if (at.length > 1) {
            throw new IllegalArgumentException("The same list instance is used for more than one "
                    + "dimension (positions " + at[0] + " and " + at[1] + "), so the rule is ambiguous; "
                    + "pass distinct list instances");
        }
        return at[0];
    }

    /**
     * Builds the caller-list-to-positions map. Deferred until the first rule is added, so a builder that
     * never gains a rule (for example one constructed only to call {@code lexOrder()} or {@code count()})
     * pays nothing for this map. Called at most once per instance under a race; the result is the same.
     */
    private IdentityHashMap<List<?>, int[]> buildPositions() {
        IdentityHashMap<List<?>, int[]> map = new IdentityHashMap<>(originals.size() * 2);
        for (int i = 0; i < originals.size(); i++) {
            List<?> list = originals.get(i);
            int[] at = map.get(list);
            if (at == null) {
                map.put(list, new int[]{i});
            } else {
                int[] grown = Arrays.copyOf(at, at.length + 1);
                grown[at.length] = i;
                map.put(list, grown);
            }
        }
        return map;
    }

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

    /**
     * Returns the indexes of values in dimension {@code dim} that match
     * {@code values}. If {@code negate} is true, returns the complement:
     * indexes of values that do not match any of the supplied values.
     */
    BitSet matching(int dim, Collection<?> values, boolean negate) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
        Map<Object, int[]> valueToPositions = indexes.of(dim);
        int size = dimensions.get(dim).size();
        BitSet result = new BitSet(size);
        Set<Object> missing = null;
        for (Object value : values) {
            Objects.requireNonNull(value, "a rule value must not be null");
            int[] at = valueToPositions.get(value);
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