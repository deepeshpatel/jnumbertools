/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates the Cartesian product of multiple lists in lexicographical order.
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌─────────────────────────────────┬─────────────────────────────┐
 * │ Input                           │ Output                      │
 * ├─────────────────────────────────┼─────────────────────────────┤
 * │ No lists (empty list)           │ [[]] (one empty tuple)      │
 * │ One non-empty list [A,B]        │ [A], [B]                    │
 * │ One empty list []               │ [[]] (one empty tuple)      │
 * │ Multiple lists, all non-empty   │ Full Cartesian product      │
 * │ Multiple lists, any empty       │ [] (empty iterator)         │
 * └─────────────────────────────────┴─────────────────────────────┘
 * </pre>
 *
 * @author Deepesh Patel
 */
public class SimpleProduct implements Iterable<List<Object>> {

    private final List<List<Object>> allLists;

    public SimpleProduct(List<List<Object>> allLists) {
        this.allLists = allLists;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new SimpleProductIterator();
    }

    private class SimpleProductIterator implements Iterator<List<Object>> {

        private final int[] indices;
        private boolean hasNext;

        SimpleProductIterator() {
            indices = new int[allLists.size()];

            if (allLists.isEmpty()) {
                hasNext = true;
                return;
            }
            if (allLists.size() > 1 && allLists.stream().anyMatch(List::isEmpty)) {
                hasNext = false;
                return;
            }
            hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<Object> next() {
            if (!hasNext) throw new NoSuchElementException();

            if (allLists.isEmpty() || (allLists.size() == 1 && allLists.get(0).isEmpty())) {
                hasNext = false;
                return List.of();
            }

            List<Object> result = new ArrayList<>(indices.length);
            for (int i = 0; i < indices.length; i++) {
                result.add(allLists.get(i).get(indices[i]));
            }
            hasNext = advanceIndices();
            return result;
        }

        private boolean advanceIndices() {
            for (int i = indices.length - 1; i >= 0; i--) {
                if (indices[i] < allLists.get(i).size() - 1) {
                    indices[i]++;
                    return true;
                }
                indices[i] = 0;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "SimpleProduct{dimensions=" + allLists.size() + "}";
    }
}