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
 * Generates unique permutations of input elements using Heap's algorithm which minimizes
 * movement by generating each permutation from the previous one by swapping two elements.
 * This implementation is memory-efficient as it generates permutations on-demand.
 *
 * <p><b>Note:</b> This does not generate permutations in lexicographic order. For lexicographic
 * order, use {@link UniquePermutation} instead. The time complexity is O(n!) and space
 * complexity is O(n) where n is the number of elements.
 *
 * <p>Example:
 * <pre>{@code
 * // Generates all permutations of [1, 2, 3] (not in lex order)
 * new UniquePermutationSingleSwap<>(Arrays.asList(1, 2, 3))
 *     .stream()
 *     .forEach(System.out::println);
 * }</pre>
 *
 * @param <T> the type of elements to be permuted
 * @author Deepesh Patel
 * @see UniquePermutation
 * @see UniquePermutationBuilder
 */
public final class UniquePermutationSingleSwap<T> extends AbstractGenerator<T> {

    /**
     * Constructs a permutation generator for the given items.
     *
     * @param elements the list of items to permute (must not be null or empty)
     * @throws IllegalArgumentException if items is null or empty
     */
    UniquePermutationSingleSwap(List<T> elements) {
        super(elements);
    }

    /**
     * Returns an iterator over all unique permutations of the input elements.
     * The iterator uses Heap's algorithm to generate permutations on-demand.
     *
     * @return an iterator that generates permutations using single-swap approach
     *         (not in lexicographic order)
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