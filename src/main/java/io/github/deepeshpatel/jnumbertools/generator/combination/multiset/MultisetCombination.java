/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 * Utility for generating combinations from a multiset.
 * <p>
 * An instance of this class is intended to be created via a builder; therefore, although the constructor is public,
 * it is recommended to use the provided builder for instantiation.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetCombination<T> extends AbstractGenerator<T> {

    private final int[] multisetFreqArray;
    private final int r;

    /**
     * Constructs a {@code MultisetCombination} with the specified elements, combination size, and frequency array.
     * <p>
     * If the provided combination size {@code r} is greater than the total available count (i.e. the sum of all frequencies),
     * it is reset to 0, resulting in an iterator that produces an empty combination.
     * </p>
     *
     * @param elements the list of items from which combinations are generated
     * @param r the number of items in each combination
     * @param multisetFreqArray an array containing the number of times each element in the input can be repeated.
     *                          For example, {@code multisetFreqArray₀} contains the count for the 0ᵗʰ element in the input,
     *                          {@code multisetFreqArray₁} contains the count for the 1ˢᵗ element in the input, and so on.
     *                          The count for every element must be &gt;= 1.
     */
    public MultisetCombination(List<T> elements, int r, int[] multisetFreqArray) {
        super(elements);
        checkParamMultisetFreqArray(elements.size(), multisetFreqArray, "combination");
        r = r > Arrays.stream(multisetFreqArray).sum() ? 0 : r;

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
            // Initialize indices to the first valid combination.
            indices = nextRepetitiveCombinationOfMultiset(indices, multisetFreqArray);
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if additional combinations exist, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Returns the next combination.
         *
         * @return the next combination as a {@link List} of elements
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

        /**
         * Computes the next repetitive combination for the multiset based on the current indices and available frequencies.
         *
         * @param a the current combination represented as an array of indices
         * @param availableCount the array representing the available frequency for each element
         * @return the next combination as an array of indices, or an empty array if no further combination exists
         */
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

            // Use a hack to indicate the first combination if necessary.
            i = next[0] == -1 ? 0 : i;
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
                // and if the previous value is maxSupportedValue - 1.
                return nextRepetitiveCombinationOfMultiset(next, availableCount);
            }
            return next;
        }
    }
}
