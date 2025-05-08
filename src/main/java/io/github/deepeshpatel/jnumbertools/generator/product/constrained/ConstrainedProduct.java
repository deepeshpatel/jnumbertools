/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A class that generates the Cartesian product of a list of lists of lists.
 * <p>
 * This class takes a list of lists of lists, where each inner list represents a set of elements
 * (e.g., combinations or subsets), and generates the Cartesian product of these sets in lexicographical order.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class ConstrainedProduct implements Iterable<List<Object>> {

    private final List<List<List<Object>>> all;

    /**
     * Constructs a ConstrainedProduct with the given list of lists of lists.
     *
     * @param all the list of lists of lists to generate the Cartesian product from
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
            hasNext = !all.isEmpty() && all.stream().noneMatch(List::isEmpty);
            if (!hasNext && !all.isEmpty()) {
                hasNext = true; // Handle empty input case
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<Object> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }

            List<Object> result = new ArrayList<>();
            for (int i = 0; i < indices.length; i++) {
                if (!all.get(i).isEmpty()) {
                    result.addAll(all.get(i).get(indices[i]));
                }
            }

            hasNext = createNext(indices, maxIndices);
            return result;
        }

        private boolean createNext(int[] current, int[] maxIndices) {
            for (int i = 0, j = current.length - 1; j >= 0; j--, i++) {
                if (current[j] == maxIndices[j] - 1) {
                    current[j] = 0;
                } else {
                    current[j]++;
                    return true;
                }
            }
            return false;
        }
    }
}