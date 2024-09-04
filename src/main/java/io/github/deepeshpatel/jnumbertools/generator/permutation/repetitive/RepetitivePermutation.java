/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 * Utility to generate all permutations of given items and size where items can be repeated.
 * Permutations are generated in lexicographical order of indices of input values, considering value at each index as unique.
 *
 * <pre>
 * Following code will generate all permutations of size 2
 * </pre>
 * <pre>
 *      new RepetitivePermutation&lt;&gt;(List.of("A","B","C"),2)
 *           .forEach(System.out::println);
 *      or
 *
 *      JNumberTools.permutationsOf("A","B","C")
 *            .repetitive(2)
 *            .forEach(System.out::println);
 *
 * will generate following -
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
 * @author Deepesh Patel
 */
public final class RepetitivePermutation<T>  extends AbstractGenerator<T> {

    private final int size;

    /**
     * Implements the iterable of repetitive permutations.
     * Permutations are generated in lex order of indices of input values, considering value at each index as unique.
     * @param elements Input of size n from which repeated permutations of size 'size' will be generated.
     * @param size size of permutations. In contrast to KPermutation, size can even be greater than
     *             n, because repetition is allowed
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
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            List<T> result = indicesToValues(currentIndices);
            hasNext = nextRepetitivePermutation(currentIndices, elements.size());
            return result;
        }

        private boolean nextRepetitivePermutation(int[] indices, int base) {

            for (int i=0, j=indices.length-1; j >= 0 ; j--, i++) {
                if(indices[j] == base-1) {
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