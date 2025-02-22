/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.deepeshpatel.jnumbertools.generator.base.Util.emptyMapIterator;

/**
 * Utility for generating combinations from a multiset in lex order of keys.
 * <p>
 * An instance of this class is intended to be created via a builder; therefore, although the constructor is public,
 * it is recommended to use the provided builder for instantiation.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class MultisetCombination<T extends Comparable<T>> implements Iterable<Map<T, Integer>> {

    private final Map.Entry<T, Integer>[]  options;
    private final int r;

    /**
     * Constructs a {@code MultisetCombination} with the specified options map and combination size.
     * MultisetCombination is used to generate an iterator for multiset combination in lex order of items
     * <p>
     * If the provided combination size {@code r} is greater than the total available
     * count (i.e., the sum of all frequencies),
     * it is reset to 0, resulting in an iterator that produces an empty combination.
     * All frequencies in the options map must be positive.
     * </p>
     *
     * @param options a map of elements to their frequency counts, where each frequency must be greater than 0
     * @param r the number of items in each combination
     * @throws IllegalArgumentException if the options map is null, empty, or contains non-positive frequencies
     */
    public MultisetCombination(Map<T,Integer> options, int r) {
        checkParameters(options, r);

        this.options = Util.toLexOrderedMap(options);
        this.r = r;
    }

    private void checkParameters(Map<T,Integer> options, int r) {
        if(r<0) {
            throw new IllegalArgumentException("parameter r (no. of items in each combination) should be>=0 ");
        }

        if (options == null) {
            throw new IllegalArgumentException("Options map cannot be null");
        }

        // Check for positive frequencies
        for (Map.Entry<T, Integer> entry : options.entrySet()) {
            Integer freq = entry.getValue();
            if (freq == null || freq <= 0) {
                throw new IllegalArgumentException("All frequencies must be positive: found "
                        + freq + " for element " + entry.getKey());
            }
        }
    }

    public Stream<Map<T, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Returns an iterator over combinations of elements in lexicographical order of keys
     * @return an iterator over the combinations
     */
    @Override
    public Iterator<Map<T,Integer>> iterator() {
        return (r == 0 || options.length==0) ? emptyMapIterator() : new Itr();
    }

    private class Itr implements Iterator<Map<T,Integer>> {

        int[] indices;

        private Itr() {
            indices = new int[r];
            indices[0] = -1;
            // Initialize indices to the first valid combination.
            indices = nextMultisetCombination(indices);
        }

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
        public Map<T,Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextMultisetCombination(indices);
            return Util.createFrequencyMap(options, old);
        }

        /**
         * Computes the next multiset combination for the current indices.
         * <p>
         * This method uses the instance's {@code availableCount} to determine the available frequencies for each element.
         * </p>
         *
         * @param a the current combination represented as an array of indices
         * @return the next combination as an array of indices, or an empty array if no further combination exists
         */
        private int[] nextMultisetCombination(int[] a) {
            int[] next = Arrays.copyOf(a, a.length);
            int maxSupportedValue = options.length - 1;

            // Step 1: Find the rightmost position to increment
            int i = next.length - 1;
            while (i >= 0 && next[i] == maxSupportedValue) {
                i--;
            }

            // If no position can be incremented, we're done
            if (i == -1) {
                return new int[]{};
            }

            // Handle the first combination case (initially -1)
            if (next[0] == -1) {
                i = 0;
            }

            // Step 2: Increment the value at position i and fill the rest
            int fillValue = next[i] + 1;
            int k = i;

            // Step 3: Iteratively fill remaining positions
            while (k < next.length) {
                // If we've run out of valid fill values, reset and try again
                if (fillValue >= options.length) {
                    if (i == 0) {
                        return new int[]{}; // No more combinations possible
                    }
                    // Backtrack: Move i left and increment again
                    i--;
                    while (i >= 0 && next[i] == maxSupportedValue) {
                        i--;
                    }
                    if (i < 0) {
                        return new int[]{};
                    }
                    fillValue = next[i] + 1;
                    k = i;
                }

                // Fill slots with the current fillValue up to its frequency limit
                int availableFillValueCount = options[fillValue].getValue();
                while (availableFillValueCount > 0 && k < next.length) {
                    next[k] = fillValue;
                    availableFillValueCount--;
                    k++;
                }

                // Move to the next value if we’ve used up the current one
                if (k < next.length && availableFillValueCount == 0) {
                    fillValue++;
                }
            }

            return next;
        }
    }
}
