/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Generates the constrained Cartesian product of multiple dimensions in lexicographical order
 * (the rightmost dimension varies fastest).
 * <p>
 * Each dimension is a re-iterable source of tuples (e.g., the combinations or subsets of one set), and
 * the product concatenates one tuple from each dimension.
 * </p>
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌─────────────────────────────────┬─────────────────────────────┐
 * │ Input                           │ Output                      │
 * ├─────────────────────────────────┼─────────────────────────────┤
 * │ No dimensions                   │ [[]] (one empty tuple)      │
 * │ One dimension (any content)     │ elements of that dimension  │
 * │ Multiple dims, all non-empty    │ Full Cartesian product      │
 * │ Any dimension without tuples    │ [] (empty iterator)         │
 * └─────────────────────────────────┴─────────────────────────────┘
 * </pre>
 *
 * <h2>Laziness</h2>
 * <p>
 * Dimensions are <em>never</em> materialized. The iterator keeps one cursor per dimension (an odometer):
 * advancing restarts only the dimensions to the right of the one that moved. Memory use is
 * O(number of dimensions) however many tuples a dimension has, so the first tuples of a product with a
 * dimension such as {@code C(40,20)} are available immediately.
 * </p>
 * <p>
 * Every call to {@link #iterator()} starts a fresh, independent traversal, and every tuple returned is
 * immutable.
 * </p>
 *
 * @author Deepesh Patel
 */
public class ConstrainedProduct implements Iterable<List<Object>> {

    private final List<? extends Iterable<List<Object>>> dimensions;

    /**
     * Constructs a ConstrainedProduct with the given dimensions.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder#lexOrder()}.
     * All parameter validation and dimension configuration is handled by the builder.
     * </p>
     *
     * @param dimensions the dimensions; each must be re-iterable (every {@code iterator()} call starts over
     *                   and yields the same tuples). A plain {@code List<List<Object>>} qualifies.
     *                   Assumes non-null; a dimension may be empty.
     */
    public ConstrainedProduct(List<? extends Iterable<List<Object>>> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new ConstrainedProductIterator();
    }

    private class ConstrainedProductIterator implements Iterator<List<Object>> {

        private final int n = dimensions.size();
        private final Iterator<List<Object>>[] cursors;
        private final List<Object>[] current;   // the tuple each dimension currently contributes
        // The concatenation of the current tuples. Only the part from the dimension that moved onwards is
        // rewritten, and next() copies the finished array, so a tuple costs one array copy instead of one
        // pass over every dimension's elements.
        private Object[] buffer = new Object[16];
        private final int[] start = new int[n + 1];   // start[k]: where dimension k's elements begin in buffer
        private boolean hasNext;

        @SuppressWarnings("unchecked")
        ConstrainedProductIterator() {
            cursors = (Iterator<List<Object>>[]) new Iterator<?>[n];
            current = (List<Object>[]) new List<?>[n];

            // No dimensions: one empty tuple. Any dimension without tuples: no tuples at all.
            boolean ok = true;
            for (int i = 0; i < n && ok; i++) {
                cursors[i] = dimensions.get(i).iterator();
                if (cursors[i].hasNext()) {
                    current[i] = cursors[i].next();
                } else {
                    ok = false;
                }
            }
            hasNext = ok;
            if (ok) fillFrom(0);
        }

        /** Rewrites buffer from dimension {@code from} onwards; the dimensions before it are unchanged. */
        private void fillFrom(int from) {
            int pos = start[from];
            for (int d = from; d < n; d++) {
                start[d] = pos;
                List<Object> part = current[d];
                int size = part.size();
                if (pos + size > buffer.length) {
                    buffer = Arrays.copyOf(buffer, Math.max(buffer.length * 2, pos + size));
                }
                if (part instanceof RandomAccess) {
                    for (int k = 0; k < size; k++) buffer[pos + k] = part.get(k);
                } else {
                    int k = pos;
                    for (Object element : part) buffer[k++] = element;
                }
                pos += size;
            }
            start[n] = pos;
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

            Object[] tuple = Arrays.copyOf(buffer, start[n]);

            advance();
            // The array is private to this call, so wrapping it (no copy) is safe; clients cannot modify it.
            return Tuple.wrap(tuple);
        }

        /** Moves the rightmost dimension that still has tuples and restarts every dimension to its right. */
        private void advance() {
            for (int i = n - 1; i >= 0; i--) {
                if (cursors[i].hasNext()) {
                    current[i] = cursors[i].next();
                    for (int j = i + 1; j < n; j++) {
                        cursors[j] = dimensions.get(j).iterator();
                        current[j] = cursors[j].next();   // non-empty: it produced a tuple when first iterated
                    }
                    fillFrom(i);
                    return;
                }
            }
            hasNext = false;
        }
    }

    @Override
    public String toString() {
        return String.format("ConstrainedProduct{dimensions=%d}", dimensions.size());
    }
}