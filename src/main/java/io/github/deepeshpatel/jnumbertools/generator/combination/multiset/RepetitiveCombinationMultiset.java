/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility for generating combinations of multiset
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class RepetitiveCombinationMultiset<T> extends AbstractGenerator<T> {

    private final int[] multisetFreqArray;
    private final int r;

    /**
     * Constructs a {@code RepetitiveCombinationMultiset} with the specified elements, size of combinations, and frequency array.
     *
     * @param elements the list of N items
     * @param r the number of items in each combination
     * @param multisetFreqArray an array containing the number of times each element in the input can be repeated.
     *                          {@code multisetFreqArray[0]} contains the count for the 0th element in the input,
     *                          {@code multisetFreqArray[1]} contains the count for the 1st element in the input, and so on.
     *                          The count of every element must be &gt;= 1.
     */
    public RepetitiveCombinationMultiset(List<T> elements, int r, int[] multisetFreqArray) {
        super(elements);
        checkParamCombination(elements.size(), r, "multiset combination");
        checkParamMultisetFreqArray(elements.size(), multisetFreqArray, "combination");

        this.r = r;
        this.multisetFreqArray = multisetFreqArray;
    }

    /**
     * Returns an iterator over combinations of elements in lexicographical order.
     *
     * @return an iterator over the combinations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (r == 0 || elements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;

        private Itr() {
            indices = new int[r];
            indices[0] = -1;
            indices = nextRepetitiveCombinationOfMultiset(indices, multisetFreqArray);
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if there are more combinations, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Returns the next combination.
         *
         * @return the next combination
         * @throws NoSuchElementException if there are no more combinations
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextRepetitiveCombinationOfMultiset(indices, multisetFreqArray);
            return indicesToValues(old);
        }

        private int[] nextRepetitiveCombinationOfMultiset(int[] a, int[] availableCount) {

            int[] next = Arrays.copyOf(a, a.length);
            int i = next.length - 1;
            int maxSupportedValue = availableCount.length - 1;

            while (i >= 0 && next[i] == maxSupportedValue) {
                i--;
            }

            if (i == -1) {
                return new int[]{};
            }

            i = next[0] == -1 ? 0 : i; // Hack to indicate the first combination
            int fillValue = next[i] + 1;
            int k = i;

            while (k < next.length && fillValue < availableCount.length) {
                int availableFillValueCount = availableCount[fillValue];
                while (availableFillValueCount > 0 && k < next.length) {
                    next[k] = fillValue;
                    availableFillValueCount--;
                    k++;
                }

                fillValue = fillValue + 1;
            }

            if (k < next.length) {
                // Optimization potential: Check if the count of maxSupportedValue is reached
                // and previousValue is maxSupportedValue - 1.
                return nextRepetitiveCombinationOfMultiset(next, availableCount);
            }
            return next;
        }
    }
}
