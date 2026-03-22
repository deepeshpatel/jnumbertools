/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates the Cartesian product of multiple lists in lexicographical order.
 * <p>
 * This class takes a list of lists, where each inner list represents a set of elements,
 * and generates the Cartesian product of these sets following strict mathematical rules.
 * </p>
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
 * <h2>Examples</h2>
 * <pre>
 * // Product of [A,B] × [1,2]
 * List<List<Object>> lists = List.of(List.of("A","B"), List.of(1,2));
 * SimpleProduct product = new SimpleProduct(lists);
 * // Output: [A,1], [A,2], [B,1], [B,2]
 *
 * // Single empty list
 * SimpleProduct empty = new SimpleProduct(List.of(List.of()));
 * // Output: [[]]
 *
 * // Multiple lists with an empty list
 * SimpleProduct withEmpty = new SimpleProduct(
 *     List.of(List.of("A","B"), List.of(), List.of("X","Y"))
 * );
 * // Output: [] (empty iterator)
 * </pre>
 *
 * @author Deepesh Patel
 * @see SimpleProductBuilder
 */
public class SimpleProduct implements Iterable<List<Object>> {

    private final List<List<Object>> allLists;

    /**
     * Constructs a SimpleProduct with the given list of lists.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link SimpleProductBuilder#lexOrder()}.
     * All parameter validation and dimension configuration is handled by the builder.
     * </p>
     *
     * @param allLists the list of lists representing the dimensions of the Cartesian product
     *                 (assumes all inner lists are non-null, may be empty)
     */
    public SimpleProduct(List<List<Object>> allLists) {
        this.allLists = allLists;
    }

    /**
     * Returns an iterator over the Cartesian product in lexicographical order.
     * <p>
     * The iterator follows these mathematical rules:
     * <ul>
     *   <li>If there are no lists → returns {@code [[]]}</li>
     *   <li>If there is one empty list → returns {@code [[]]}</li>
     *   <li>If there are multiple lists and any is empty → returns empty iterator</li>
     *   <li>Otherwise → generates full Cartesian product</li>
     * </ul>
     * </p>
     *
     * @return an iterator over the Cartesian product
     */
    @Override
    public Iterator<List<Object>> iterator() {
        return new SimpleProductIterator();
    }

    /**
     * Iterator implementation for Cartesian product generation.
     */
    private class SimpleProductIterator implements Iterator<List<Object>> {
        private final int[] indices;
        private boolean hasNext;

        /**
         * Constructs an iterator based on the input lists.
         */
        SimpleProductIterator() {
            indices = new int[allLists.size()];

            // Case 1: No lists → return [[]]
            if (allLists.isEmpty()) {
                hasNext = true;
                return;
            }

            // Case 2: Multiple lists with any empty → return empty iterator
            if (allLists.size() > 1 && allLists.stream().anyMatch(List::isEmpty)) {
                hasNext = false;
                return;
            }

            // Case 3: All remaining cases (single list, empty or not) → return [[]] or normal
            hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<Object> next() {
            if (!hasNext) {
                throw new NoSuchElementException("No more tuples available");
            }

            if (allLists.isEmpty()) {
                hasNext = false;
                return List.of();
            }

            if (allLists.size() == 1 && allLists.get(0).isEmpty()) {
                hasNext = false;
                return List.of();
            }

            // Normal case: build the current tuple
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < indices.length; i++) {
                result.add(allLists.get(i).get(indices[i]));
            }

            hasNext = advanceIndices();
            return List.copyOf(result);
        }

        /**
         * Advances the indices to the next combination in lexicographical order.
         *
         * @return {@code true} if there is a next combination, {@code false} otherwise
         */
        private boolean advanceIndices() {
            for (int i = indices.length - 1; i >= 0; i--) {
                int maxIndex = allLists.get(i).size() - 1;
                if (indices[i] < maxIndex) {
                    indices[i]++;
                    return true;
                } else {
                    indices[i] = 0;
                }
            }
            return false;
        }
    }

    /**
     * Returns a string representation of this product.
     *
     * @return a string describing the product configuration
     */
    @Override
    public String toString() {
        return String.format("SimpleProduct{dimensions=%d}", allLists.size());
    }
}
