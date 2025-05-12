/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;

/**
 * Generates multiset combinations of size r from a multiset of n distinct elements with specified multiplicities.
 * <p>
 * A multiset combination selects r items from a multiset (e.g., {A:2, B:1}), where each element can be selected
 * up to its multiplicity, and order does not matter. The total number of combinations is determined by the
 * multichoose formula, constrained by element frequencies. Combinations are output as frequency maps (e.g.,
 * {A:1, B:2}) and generated in lexicographical order based on element indices.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class MultisetCombination<T> extends AbstractMultisetCombination<T> {

    private static final int CROSSOVER_THRESHOLD = 1000;

    /**
     * Constructs a generator for multiset combinations.
     *
     * @param map a map of n distinct elements to their multiplicities (must not be null or empty)
     * @param r   the size of each combination (r ≥ 0)
     * @throws IllegalArgumentException if r < 0 or map is null/empty
     */
    MultisetCombination(LinkedHashMap<T, Integer> map, int r) {
        super(map, r);
    }

    /**
     * Returns an iterator over multiset combinations in lexicographical order.
     * <p>
     * Uses {@link ArrayIterator} for small r (r < 1000) for simplicity, and {@link FreqVectorIterator}
     * for large r to optimize performance.
     * </p>
     *
     * @return an iterator of maps, each representing a combination as element frequencies
     */
    @Override
    public Iterator<Map<T, Integer>> iterator() {
        if (r == 0 || options.length == 0) return Util.emptyMapIterator();
        return (r < CROSSOVER_THRESHOLD) ? new ArrayIterator() : new FreqVectorIterator();
    }

    /**
     * Iterator for generating multiset combinations using an array-based approach.
     * <p>
     * Suitable for small combination sizes (r < 1000), this iterator generates combinations by
     * incrementing indices lexicographically, respecting element multiplicities.
     * </p>
     */
    public class ArrayIterator implements Iterator<Map<T, Integer>> {
        private int[] indices;

        public ArrayIterator() {
            indices = new int[r];
            indices[0] = -1;
            indices = nextMultisetCombination(indices);
        }

        /**
         * Checks if more combinations are available.
         *
         * @return true if further combinations exist, false otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Returns the next multiset combination as a frequency map.
         *
         * @return a map of elements to their frequencies in the combination
         * @throws NoSuchElementException if no further combinations are available
         */
        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            int[] old = indices;
            indices = nextMultisetCombination(indices);
            return createFrequencyMap(old);
        }

        /**
         * Computes the next multiset combination in lexicographical order.
         * <p>
         * Given the current combination of indices [a₀, a₁, ..., aᵣ₋₁], this method finds the
         * rightmost index i where aᵢ can be incremented (aᵢ < n-1) while respecting element
         * multiplicities, increments it, and fills subsequent positions with valid indices.
         * Returns an empty array if no further combination exists.
         *
         * @param a the current combination of indices
         * @return the next combination of indices, or an empty array if none exists
         */
        private int[] nextMultisetCombination(int[] a) {
            int[] next = Arrays.copyOf(a, a.length);
            int maxSupportedValue = options.length - 1;

            int i = next.length - 1;
            while (i >= 0 && next[i] == maxSupportedValue) i--;
            if (i == -1) return new int[]{};

            if (next[0] == -1) i = 0;

            int fillValue = next[i] + 1;
            int k = i;

            while (k < next.length) {
                if (fillValue >= options.length) {
                    if (i == 0) return new int[]{};
                    i--;
                    while (i >= 0 && next[i] == maxSupportedValue) i--;
                    if (i < 0) return new int[]{};
                    fillValue = next[i] + 1;
                    k = i;
                }

                int availableFillValueCount = options[fillValue].getValue();
                while (availableFillValueCount > 0 && k < next.length) {
                    next[k] = fillValue;
                    availableFillValueCount--;
                    k++;
                }
                if (k < next.length && availableFillValueCount == 0) fillValue++;
            }

            return next;
        }

        /**
         * Creates a frequency map from a sorted array of indices.
         * <p>
         * Converts the array of indices into a map where each key is an element from the multiset
         * and its value is the frequency of that element in the combination, preserving the order
         * of options.
         *
         * @param sortedArray the sorted array of indices
         * @return a map of elements to their frequencies
         */
        public Map<T, Integer> createFrequencyMap(int[] sortedArray) {
            var freqMap = new LinkedHashMap<T, Integer>();

            if (sortedArray.length == 0) {
                return freqMap;
            }

            int currentNum = sortedArray[0];
            int currentCount = 1;

            for (int i = 1; i < sortedArray.length; i++) {
                if (sortedArray[i] == currentNum) {
                    currentCount++;
                } else {
                    freqMap.put(options[currentNum].getKey(), currentCount);
                    currentNum = sortedArray[i];
                    currentCount = 1;
                }
            }

            freqMap.put(options[currentNum].getKey(), currentCount);

            return freqMap;
        }
    }

    /**
     * Iterator for generating multiset combinations using a frequency vector approach.
     * <p>
     * Suitable for large combination sizes (r ≥ 1000), this iterator generates combinations by
     * manipulating a frequency vector, optimizing for performance while respecting element
     * multiplicities.
     * </p>
     */
    public class FreqVectorIterator implements Iterator<Map<T, Integer>> {
        private FreqVector indices;
        private final Map<T, Integer> outputMap = new LinkedHashMap<>();

        public FreqVectorIterator() {
            indices = nextMultisetCombination(new FreqVector(r, options.length));
        }

        /**
         * Checks if more combinations are available.
         *
         * @return true if further combinations exist, false otherwise
         */
        @Override
        public boolean hasNext() {
            return indices != null;
        }

        /**
         * Returns the next multiset combination as a frequency map.
         *
         * @return a map of elements to their frequencies in the combination
         * @throws NoSuchElementException if no further combinations are available
         */
        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            FreqVector previous = new FreqVector(indices);
            indices = nextMultisetCombination(indices);
            return mapListToMap(previous);
        }

        /**
         * Converts a frequency vector to a frequency map.
         * <p>
         * Maps non-zero frequencies in the vector to their corresponding elements in the multiset,
         * preserving the order of options.
         *
         * @param freqVector the frequency vector
         * @return a map of elements to their frequencies
         */
        private Map<T, Integer> mapListToMap(FreqVector freqVector) {
            outputMap.clear();
            for (int i = 0; i < options.length; i++) {
                if (freqVector.frequencies[i] != 0) {
                    outputMap.put(options[i].getKey(), freqVector.frequencies[i]);
                }
            }
            return Map.copyOf(outputMap);
        }

        /**
         * Computes the next multiset combination in lexicographical order.
         * <p>
         * Given the current frequency vector, this method finds the rightmost index i where the
         * frequency can be incremented (respecting element multiplicities), adjusts the vector,
         * and fills subsequent positions with valid frequencies. Returns null if no further
         * combination exists.
         *
         * @param current the current frequency vector
         * @return the next frequency vector, or null if none exists
         */
        private FreqVector nextMultisetCombination(FreqVector current) {
            int maxSupportedValue = options.length - 1;

            int i = current.size() - 1;
            while (i >= 0 && current.findValueAtIndex(i) == maxSupportedValue) i--;
            if (i < 0) return null;

            int initialValue = current.findValueAtIndex(0);
            if (initialValue == -1) {
                int k = 0;
                int fillValue = 0;
                while (k < current.size() && fillValue < options.length) {
                    int availableFillValueCount = options[fillValue].getValue();
                    while (availableFillValueCount > 0 && k < current.size()) {
                        current.set(k, fillValue);
                        availableFillValueCount--;
                        k++;
                    }
                    fillValue++;
                }
                return k == current.size() ? current : null;
            }

            int fillValue = current.findValueAtIndex(i) + 1;
            int k = i;

            while (k < current.size()) {
                if (fillValue >= options.length) {
                    if (i == 0) return null;
                    i--;
                    while (i >= 0 && current.findValueAtIndex(i) == maxSupportedValue) i--;
                    if (i < 0) return null;
                    fillValue = current.findValueAtIndex(i) + 1;
                    k = i;
                }

                int availableFillValueCount = options[fillValue].getValue();
                while (availableFillValueCount > 0 && k < current.size()) {
                    current.set(k, fillValue);
                    availableFillValueCount--;
                    k++;
                }
                if (k < current.size() && availableFillValueCount == 0) fillValue++;
            }

            return current;
        }
    }
}