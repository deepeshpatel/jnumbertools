/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;

public final class MultisetCombination<T extends Comparable<T>> extends AbstractMultisetCombination<T> {

    private static final int CROSSOVER_THRESHOLD = 1000;

    public MultisetCombination(Map<T, Integer> options, int r, Order order) {
        super(options, r, order);
    }

    public MultisetCombination(Map<T, Integer> options, int r) {
        super(options, r, Order.LEX);
    }

    @Override
    public Iterator<Map<T, Integer>> iterator() {
        if (r == 0 || options.length == 0) return Util.emptyMapIterator();
        return (r < CROSSOVER_THRESHOLD) ? new ArrayIterator() : new FreqVectorIterator();
    }

    public class ArrayIterator implements Iterator<Map<T, Integer>> {
        private int[] indices;

        public ArrayIterator() {
            indices = new int[r];
            indices[0] = -1;
            indices = nextMultisetCombination(indices);
        }

        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            int[] old = indices;
            indices = nextMultisetCombination(indices);
            return createFrequencyMap(old);
        }

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

        public Map<T, Integer> createFrequencyMap(int[] sortedArray) {
            // Creates a frequency map from indices in sortedArray, preserving order of options
            var freqMap = new LinkedHashMap<T, Integer>();

            if (sortedArray.length == 0) {
                return freqMap;
            }

            // Count frequencies in one pass through sorted intArray
            int currentNum = sortedArray[0];
            int currentCount = 1;

            for (int i = 1; i < sortedArray.length; i++) {
                if (sortedArray[i] == currentNum) {
                    currentCount++;
                } else {
                    freqMap.put(options[currentNum].getKey(), currentCount); // Use List.get() instead of array access
                    currentNum = sortedArray[i];
                    currentCount = 1;
                }
            }

            // Handle the last group
            freqMap.put(options[currentNum].getKey(), currentCount); // Use List.get() for last group

            return freqMap;
        }
    }

    public class FreqVectorIterator implements Iterator<Map<T, Integer>> {
        private FreqVector indices;
        private final Map<T, Integer> outputMap = new LinkedHashMap<>();

        public FreqVectorIterator() {
            indices = nextMultisetCombination(new FreqVector(r, options.length));
        }

        @Override
        public boolean hasNext() {
            return indices != null;
        }

        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            FreqVector previous = new FreqVector(indices);
            indices = nextMultisetCombination(indices);
            return mapListToMap(previous);
        }

        private Map<T, Integer> mapListToMap(FreqVector freqVector) {
            outputMap.clear();
            for (int i = 0; i < options.length; i++) {
                if (freqVector.frequencies[i] != 0) {
                    outputMap.put(options[i].getKey(), freqVector.frequencies[i]);
                }
            }
            return Map.copyOf(outputMap);
        }

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