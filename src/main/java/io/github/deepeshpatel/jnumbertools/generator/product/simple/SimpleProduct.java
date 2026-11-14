/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates the Cartesian product of multiple lists in lexicographical order
 * (the rightmost dimension varies fastest).
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌─────────────────────────────────┬─────────────────────────────┐
 * │ Input                           │ Output                      │
 * ├─────────────────────────────────┼─────────────────────────────┤
 * │ No lists                        │ [[]] (one empty tuple)      │
 * │ One non-empty list [A,B]        │ [A], [B]                    │
 * │ Any list empty (one or many)    │ [] (empty iterator)         │
 * │ Multiple lists, all non-empty   │ Full Cartesian product      │
 * └─────────────────────────────────┴─────────────────────────────┘
 * </pre>
 * <p>
 * A single empty list is an empty dimension like any other, so it yields no tuples
 * (see "SIMPLE CARTESIAN PRODUCT" in {@code ValidationRules.md}).
 * </p>
 * <p>
 * Every call to {@link #iterator()} starts a fresh, independent traversal and every tuple it returns
 * is immutable. This class does not copy the lists it is given; the owning
 * {@link SimpleProductBuilder} takes the snapshot.
 * </p>
 *
 * @author Deepesh Patel
 */
public class SimpleProduct implements Iterable<List<Object>> {

    private final List<? extends List<?>> allLists;

    /**
     * Intended for internal use; create instances via
     * {@link SimpleProductBuilder#lexOrder()}, which validates and snapshots the input.
     *
     * @param allLists the dimensions (assumed non-null, without null elements; may be empty lists)
     */
    public SimpleProduct(List<? extends List<?>> allLists) {
        this.allLists = allLists;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new SimpleProductIterator();
    }

    private class SimpleProductIterator implements Iterator<List<Object>> {

        private final int[] indices = new int[allLists.size()];
        // The current tuple. Advancing rewrites only the positions that changed, and next() copies the array.
        private final Object[] current = new Object[allLists.size()];
        private boolean hasNext;

        SimpleProductIterator() {
            // No dimensions: one empty tuple. Any empty dimension: no tuples at all.
            hasNext = allLists.stream().noneMatch(List::isEmpty);
            if (hasNext) {
                for (int i = 0; i < current.length; i++) current[i] = allLists.get(i).get(0);
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<Object> next() {
            if (!hasNext) throw new NoSuchElementException();

            Object[] tuple = current.clone();
            hasNext = advanceIndices();
            // The array is private to this call, so wrapping it (no copy) is safe and immutable to callers.
            return Tuple.wrap(tuple);
        }

        private boolean advanceIndices() {
            for (int i = indices.length - 1; i >= 0; i--) {
                List<?> dimension = allLists.get(i);
                if (indices[i] < dimension.size() - 1) {
                    indices[i]++;
                    current[i] = dimension.get(indices[i]);
                    return true;
                }
                indices[i] = 0;
                current[i] = dimension.get(0);
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "SimpleProduct{dimensions=" + allLists.size() + "}";
    }
}
