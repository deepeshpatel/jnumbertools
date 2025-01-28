/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 * Utility for generating r-combinations of input = {1, 2, ..., n} with repetition allowed.
 * <p>
 * This class generates r-combinations from n = input. length items, with combinations generated
 * in lexicographical order based on the indices of items in a list. The class does not check for duplicate
 * values and treats all values differently based on their index.
 * <p>
 * Example of repetitive combinations of 3 items out of 2 {1, 2}:
 * <pre>
 * 111 112 122 222
 * </pre>
 * <p>
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> The type of elements in the combination.
 * @author Deepesh Patel
 */
public final class RepetitiveCombination <T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * Constructs a new {@code RepetitiveCombination} generator.
     *
     * @param elements List of N items from which combinations will be generated. N is the length of the input list.
     * @param r The number of items in each combination.
     */
    RepetitiveCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
    }

    /**
     * Returns an iterator over the repetitive combinations.
     *
     * @return an {@code Iterator} over lists of combinations.
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (r==0 || elements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices = new int[r];

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if there are more combinations, {@code false} otherwise.
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Generates the next combination in lexicographical order.
         *
         * @return the next combination as a list of elements.
         * @throws NoSuchElementException if no more combinations are available.
         */
        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextRepetitiveCombination(indices, elements.size());
            return indicesToValues(old);
        }

        /**
         * Generates the next combination of indices.
         *
         * @param a the current combination of indices.
         * @param n the number of elements in the input list.
         * @return the next combination of indices, or an empty array if no further combinations exist.
         */
        private int[] nextRepetitiveCombination(int []a, int n) {

            final int[] next = Arrays.copyOf(a, a.length);
            final int maxSupportedValue = n-1;
            int i=next.length-1;

            while( i>=0 && a[i] == maxSupportedValue) {
                i--;
            }
            if(i==-1) {
                return new int[]{};
            }

            int fillValue = a[i]+1;

            for(int k=i; k<next.length; k++) {
                next[k] = fillValue;
            }
            return next;
        }
    }
}

