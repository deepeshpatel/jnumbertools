/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 *
 * Utility for generating r-combinations of input = {1, 2 . . ., n}
 * <p>
 * Generates r combinations from n=input.length items.
 * combinations are generated in Lexicographic order
 * of indices of items in a list. This class will not check for duplicate values and
 * treats all values differently based on the index
 * <p>
 * Repetitive combinations of 3 items out of 2{1, 2} are -
 * <pre>
 * 111 112 122 222
 * </pre>
 * <pre>
 * Code example:
 *
 *     new RepetitiveCombination&lt;&gt;(List.of("A","B"),3)
 *           .forEach(System.out::println);
 *
 *     or
 *
 *     JNumberTools.of("A","B")
 *           .repetitive(3)
 *           .forEach(System.out::println);
 *
 * will generate following -
 * [A, A, A]
 * [A, A, B]
 * [A, B, B]
 * [B, B, B]
 * </pre>
 *
 * @author Deepesh Patel
 */
public class RepetitiveCombination <T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * @param input List of N items. N is the length of input
     * @param r number of combinations from N items.
     */
    public RepetitiveCombination(List<T> input, int r) {
        super(input);
        this.r = r;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return (r==0 || seedElements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices = new int[r];

        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextRepetitiveCombination(indices, seedElements.size());
            return indicesToValues(old, seedElements);
        }

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

