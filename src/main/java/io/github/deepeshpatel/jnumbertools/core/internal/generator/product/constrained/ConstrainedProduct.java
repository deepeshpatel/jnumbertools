/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.product.constrained;

import java.util.*;

/**
 * Generates the constrained Cartesian product of multiple dimensions in lexicographical order.
 * <p>
 * This class takes a list of, lists of lists, where each inner list represents a set of elements
 * (e.g., combinations or subsets), and generates the Cartesian product of these sets.
 * </p>
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌─────────────────────────────────┬─────────────────────────────┐
 * │ Input                           │ Output                      │
 * ├─────────────────────────────────┼─────────────────────────────┤
 * │ No dimensions                   │ [[]] (one empty tuple)      │
 * │ One dimension (any content)      │ elements of that dimension  │
 * │ Multiple dims, all non-empty     │ Full Cartesian product      │
 * │ Multiple dims, any empty         │ [] (empty iterator)         │
 * └─────────────────────────────────┴─────────────────────────────┘
 * </pre>
 *
 * @author Deepesh Patel
 */
public class ConstrainedProduct implements Iterable<List<Object>> {

    private final List<List<List<Object>>> all;

    /**
     * Constructs a ConstrainedProduct with the given dimensions.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link ConstrainedProductBuilder#lexOrder()}.
     * All parameter validation and dimension configuration is handled by the builder.
     * </p>
     *
     * @param all the list of dimensions, each dimension being a list of possible values
     *            (assumes all inner lists are non-null, may be empty)
     */
    public ConstrainedProduct(List<List<List<Object>>> all) {
        this.all = all;
    }

    @Override
    public Iterator<List<Object>> iterator() {
        return new ConstrainedProductIterator();
    }

    private class ConstrainedProductIterator implements Iterator<List<Object>> {
        private final int[] indices;
        private final int[] maxIndices;
        private boolean hasNext;

        ConstrainedProductIterator() {
            indices = new int[all.size()];
            maxIndices = new int[all.size()];

            for (int i = 0; i < all.size(); i++) {
                maxIndices[i] = all.get(i).size();
            }

            // Determine if there are any elements to iterate
            if (all.isEmpty()) {
                // No dimensions → one empty tuple
                hasNext = true;
            } else if (all.size() == 1) {
                // Single dimension → iterate its elements
                hasNext = !all.get(0).isEmpty();
            } else {
                // Multiple dimensions → product is empty if any dimension is empty
                hasNext = all.stream().noneMatch(List::isEmpty);
            }
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

            // no dimensions
            if (all.isEmpty()) {
                hasNext = false;
                return List.of();
            }

            // single dimension
            if (all.size() == 1) {
                List<Object> result = all.get(0).get(indices[0]);
                hasNext = ++indices[0] < maxIndices[0];
                return result;
            }

            // multiple dimensions
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < indices.length; i++) {
                result.addAll(all.get(i).get(indices[i]));
            }

            hasNext = advanceIndices();
            return Collections.unmodifiableList(result);
        }

        /**
         * Advances indices to the next combination.
         *
         * @return true if there is a next combination, false otherwise
         */
        private boolean advanceIndices() {
            for (int i = indices.length - 1; i >= 0; i--) {
                if (indices[i] < maxIndices[i] - 1) {
                    indices[i]++;
                    return true;
                } else {
                    indices[i] = 0;
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("ConstrainedProduct{dimensions=%d}", all.size());
    }
}
