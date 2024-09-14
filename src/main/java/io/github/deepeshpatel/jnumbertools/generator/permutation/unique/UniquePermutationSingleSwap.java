/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Generates a sequence of unique permutations using Heap's algorithm named after B. R. Heap.
 * <p>
 * Heap's algorithm is used to generate all possible permutations of n elements.
 * This implementation generates permutations by performing a single swap at a time.
 * </p>
 * <p>
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 * </p>
 * @author Deepesh Patel
 * @param <T> the type of elements to permute
 */
public final class UniquePermutationSingleSwap<T> extends AbstractGenerator<T> implements Iterable<List<T>> {

    /**
     * Constructs a UniquePermutationSingleSwap with the specified list of elements.
     *
     * @param elements the list of elements to generate permutations from
     */
    UniquePermutationSingleSwap(List<T> elements) {
        super(elements);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int i = 1;
        int[] indices = IntStream.range(0, elements.size()).toArray();
        final int[] c = new int[indices.length];

        /**
         * Generates the next permutation using Heap's algorithm.
         * This method performs the permutation generation logic by swapping elements.
         */
        public void generate() {
            while (i < indices.length) {
                if (c[i] < i) {
                    if (i % 2 == 0) {
                        swap(indices, 0, i);
                    } else {
                        swap(indices, c[i], i);
                    }
                    c[i] += 1;
                    i = 1;
                    return;

                } else {
                    c[i] = 0;
                    i += 1;
                }
            }
            indices = null;
        }

        private static void swap(int[] a, int x, int y) {
            int temp = a[x];
            a[x] = a[y];
            a[y] = temp;
        }

        @Override
        public boolean hasNext() {
            return indices != null;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            var result = indicesToValues(indices);
            generate();
            return result;
        }
    }
}
