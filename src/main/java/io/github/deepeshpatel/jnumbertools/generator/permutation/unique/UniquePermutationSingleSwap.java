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
 * Generates a sequence of unique permutations using Heap's algorithm.
 * <p>
 * Heap's algorithm is an efficient method for generating all possible permutations of <em>n</em> elements
 * by performing a single swap at a time. This implementation generates permutations by operating on
 * the indices of the input values (treating each element at a given index as unique, e.g. elements₀, elements₁, …).
 * </p>
 * <p>
 * Note: Instances of this class are intended to be created via a builder; hence, it does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 * @version 3.0.1
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
        return elements.isEmpty() ? Util.emptyListIterator() : new Itr();
    }

    /**
     * Inner iterator class that implements Heap's algorithm to generate permutations using single swaps.
     */
    private class Itr implements Iterator<List<T>> {

        int i = 1;
        int[] indices = IntStream.range(0, elements.size()).toArray();
        final int[] c = new int[indices.length];

        /**
         * Generates the next permutation using Heap's algorithm.
         * <p>
         * The algorithm repeatedly swaps elements according to the state in array {@code c} until
         * a new permutation is produced. This process continues until all permutations have been generated.
         * </p>
         */
        public void generate() {
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

        /**
         * Swaps the elements at the specified indices in the array.
         *
         * @param a the array in which to swap elements
         * @param x the first index
         * @param y the second index
         */
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
                throw new NoSuchElementException();
            }
            List<T> result = indicesToValues(indices);
            generate();
            return result;
        }
    }
}
