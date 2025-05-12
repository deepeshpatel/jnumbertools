/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Generates all unique permutations using Heap's single-swap algorithm.
 * <p>
 * This class implements Heap's algorithm, which generates permutations by performing a single swap per step,
 * operating on indices (e.g., elements₀, elements₁, ...) and mapping them to the input elements. It’s efficient
 * for sequential generation but not order-preserving (not lexicographical). Instances should be created via
 * a builder.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutationSingleSwap<T> extends AbstractGenerator<T> {

    /**
     * Constructs a generator using Heap's algorithm for the specified elements.
     *
     * @param elements the list of elements to permute; may be empty
     */
    UniquePermutationSingleSwap(List<T> elements) {
        super(elements);
    }

    /**
     * Returns an iterator over all unique permutations using single swaps.
     *
     * @return an iterator over lists representing permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? Util.emptyListIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {
        private int i = 1;
        private int[] indices = IntStream.range(0, elements.size()).toArray();
        private final int[] c = new int[indices.length];

        @Override
        public boolean hasNext() {
            return indices != null;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available");
            }
            List<T> result = indicesToValues(indices);
            generateNext();
            return result;
        }

        private void generateNext() {
            while (i < indices.length) {
                if (c[i] < i) {
                    if (i % 2 == 0) {
                        swap(indices, 0, i);
                    } else {
                        swap(indices, c[i], i);
                    }
                    c[i]++;
                    i = 1;
                    return;
                } else {
                    c[i] = 0;
                    i++;
                }
            }
            indices = null;
        }

        private static void swap(int[] a, int x, int y) {
            int temp = a[x];
            a[x] = a[y];
            a[y] = temp;
        }
    }
}