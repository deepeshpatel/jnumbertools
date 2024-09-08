/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 * Utility to generate all permutations of given items and size where items can be repeated.
 * <p>
 * Permutations are generated in lexicographical order of indices of input values, considering value at each index as unique.
 * </p>
 * <p>
 * Example:
 * <pre>
 *     new RepetitivePermutation&lt;&gt;(List.of("A","B","C"), 2)
 *         .forEach(System.out::println);
 *     or
 *     JNumberTools.permutationsOf("A","B","C")
 *         .repetitive(2)
 *         .forEach(System.out::println);
 * </pre>
 * This will generate the following permutations:
 * <pre>
 * [A, A]
 * [A, B]
 * [A, C]
 * [B, A]
 * [B, B]
 * [B, C]
 * [C, A]
 * [C, B]
 * [C, C]
 * </pre>
 *
 * @param <T> the type of elements to permute
 */
public final class RepetitivePermutation<T> extends AbstractGenerator<T> {

    private final int size;

    /**
     * Constructs a RepetitivePermutation with the specified elements and size.
     *
     * @param elements the list of elements to permute
     * @param size the size of each permutation. This can be greater than the number of elements due to repetition.
     */
    RepetitivePermutation(List<T> elements, int size) {
        super(elements);
        this.size = size;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new NumberIterator();
    }

    private class NumberIterator implements Iterator<List<T>> {

        private final int[] currentIndices = new int[size];
        private boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            List<T> result = indicesToValues(currentIndices);
            hasNext = nextRepetitivePermutation(currentIndices, elements.size());
            return result;
        }

        /**
         * Updates the indices array to the next permutation in lexicographical order.
         *
         * @param indices the current indices
         * @param base the size of the base set (number of unique elements)
         * @return true if there are more permutations, false if the end is reached
         */
        private boolean nextRepetitivePermutation(int[] indices, int base) {
            for (int i = 0, j = indices.length - 1; j >= 0; j--, i++) {
                if (indices[j] == base - 1) {
                    indices[j] = 0;
                } else {
                    indices[j]++;
                    return true;
                }
            }
            return false;
        }
    }
}
