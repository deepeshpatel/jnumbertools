/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility to generate all permutations of given items and size where items can be repeated.
 * Permutations are generated in lexicographical order of indices of input values, considering value at each indices as unique.
 *
 * <pre>
 * Following code will generate all permutations of size 2
 * </pre>
 * <pre>
 *      new RepetitivePermutation&lt;&gt;(Arrays.asList("A","B","C"),2)
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
public class RepetitivePermutation<T>  extends AbstractGenerator<T> {

    private final int size;

    /**
     * Implements the iterable of repetitive permutations.
     * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
     * @param seed Input of size n from which repeated permutations of size 'size' will be generated.
     * @param size size of permutations. In contrast to KPermutation, size can even be even greater than
     *             n, because repetition is allowed
     */
    public RepetitivePermutation(Collection<T> seed, int size) {
        super(seed);
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
            List<T> result = AbstractGenerator.indicesToValuesReverse(currentIndices, seed);
            hasNext = nextRepetitivePermutation(currentIndices, seed.size());
            return result;
        }

        private boolean nextRepetitivePermutation(int[] indices, int base) {
            int j;
            for (j=0; j <indices.length ; j++) {
                if(indices[j] == base-1) {
                    indices[j] = 0;
                } else {
                    indices[j]++;
                    break;
                }
            }
            return (j != indices.length); //true if more number's are available
        }
    }
}