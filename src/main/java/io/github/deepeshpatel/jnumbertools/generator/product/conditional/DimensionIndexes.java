/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Lazily built lookup from a value to its positions in a dimension, shared by every builder derived from the
 * same dimensions (adding a rule or a dimension keeps the maps of the dimensions already built).
 *
 * <p>Compiling a rule such as {@code isOneOf(values)} then costs time in proportion to the number of values the
 * rule names, not to the size of the dimension, and allocates no per-rule hash sets. A dimension's map is built
 * at most a few times under a race and then reused; the dimension snapshots never change.</p>
 */
final class DimensionIndexes {
    private final List<List<?>> dimensions;
    private final AtomicReferenceArray<Map<Object, int[]>> maps;

    DimensionIndexes(List<List<?>> dimensions) {
        this(dimensions, new AtomicReferenceArray<>(dimensions.size()));
    }

    private DimensionIndexes(List<List<?>> dimensions, AtomicReferenceArray<Map<Object, int[]>> maps) {
        this.dimensions = dimensions;
        this.maps = maps;
    }

    /** The indexes for {@code extended}, which is these dimensions plus more at the end; built maps are kept. */
    DimensionIndexes extendedTo(List<List<?>> extended) {
        AtomicReferenceArray<Map<Object, int[]>> copy = new AtomicReferenceArray<>(extended.size());
        for (int i = 0; i < maps.length(); i++) copy.set(i, maps.get(i));
        return new DimensionIndexes(extended, copy);
    }

    /** Positions of each value of dimension {@code dim}; equal values share one entry. */
    Map<Object, int[]> of(int dim) {
        Map<Object, int[]> map = maps.get(dim);
        if (map == null) {
            List<?> list = dimensions.get(dim);
            map = new HashMap<>(Math.max(16, list.size() * 2));
            // Values that occur once keep a one-element array. The positions of a value that repeats are collected
            // in a growable buffer (amortized constant time per occurrence) and stored as one exact-length array
            // at the end, so a dimension made of many equal values is not copied once per occurrence.
            Map<Object, Positions> repeated = null;
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                int[] known = map.get(element);
                if (known == null) {
                    map.put(element, new int[]{i});
                } else {
                    if (repeated == null) repeated = new HashMap<>();
                    Positions positions = repeated.get(element);
                    if (positions == null) {
                        positions = new Positions(known[0]);
                        repeated.put(element, positions);
                    }
                    positions.add(i);
                }
            }
            if (repeated != null) {
                for (Map.Entry<Object, Positions> entry : repeated.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().toArray());
                }
            }
            maps.compareAndSet(dim, null, map);
            map = maps.get(dim);
        }
        return map;
    }

    /** Positions of one repeated value, appended in increasing order. */
    private static final class Positions {
        private int[] buffer = new int[4];
        private int size;

        Positions(int first) {
            buffer[size++] = first;
        }

        void add(int position) {
            if (size == buffer.length) buffer = Arrays.copyOf(buffer, size * 2);
            buffer[size++] = position;
        }

        int[] toArray() {
            return Arrays.copyOf(buffer, size);
        }
    }
}
