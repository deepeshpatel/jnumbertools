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
 * A class that generates the Cartesian product of a list of lists.
 * <p>
 * This class takes a list of lists, where each inner list represents a set of elements,
 * and generates the Cartesian product of these sets in lexicographical order.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class SimpleProduct implements Iterable<List<Object>> {

    private final List<List<Object>> allLists;

    /**
     * Constructs a SimpleProduct with the given list of lists.
     *
     * @param allLists the list of lists to generate the Cartesian product from
     */
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
            hasNext = !allLists.isEmpty() && allLists.stream().noneMatch(List::isEmpty);
            if (!hasNext && !allLists.isEmpty()) {
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
                if (!allLists.get(i).isEmpty()) {
                    result.add(allLists.get(i).get(indices[i]));
                }
            }

            hasNext = createNext(indices, allLists);
            return result;
        }

        private boolean createNext(int[] current, List<List<Object>> elements) {
            for (int i = 0, j = current.length - 1; j >= 0; j--, i++) {
                if (current[j] == elements.get(j).size() - 1) {
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