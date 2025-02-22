package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.deepeshpatel.jnumbertools.generator.base.Util.emptyMapIterator;

public final class MultisetCombinationForHighFrequency<T extends Comparable<T>> implements Iterable<Map<T, Integer>> {
    private final Map.Entry<T, Integer>[] options;
    private final int r;

    public MultisetCombinationForHighFrequency(Map<T, Integer> options, int r) {
        checkParameters(options, r);
        this.options = Util.toLexOrderedMap(options);
        this.r = r;
    }

    private void checkParameters(Map<T, Integer> options, int r) {
        if (r < 0) throw new IllegalArgumentException("parameter r (no. of items in each combination) should be>=0 ");
        if (options == null) throw new IllegalArgumentException("Options map cannot be null");
        for (Map.Entry<T, Integer> entry : options.entrySet()) {
            Integer freq = entry.getValue();
            if (freq == null || freq <= 0) throw new IllegalArgumentException("All frequencies must be positive: found " + freq + " for element " + entry.getKey());
        }
    }

    public Stream<Map<T, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<Map<T, Integer>> iterator() {
        return (r == 0 || options.length == 0) ? emptyMapIterator() : new Itr();
    }

    private class Itr implements Iterator<Map<T, Integer>> {
        FreqVector indices;
        private final Map<T, Integer> outputMap = new LinkedHashMap<>(); // Reusable map

        private Itr() {
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
            return outputMap;
        }

        private FreqVector nextMultisetCombination(FreqVector a) {
            FreqVector next = a;
            int maxSupportedValue = options.length - 1;

            int i = next.size() - 1;
            while (i >= 0 && next.findValueAtIndex(i) == maxSupportedValue) {
                i--;
            }
            if (i < 0) return null;

            int initialValue = next.findValueAtIndex(0);
            if (initialValue == -1) {
                int k = 0;
                int fillValue = 0;
                while (k < next.size() && fillValue < options.length) {
                    int availableFillValueCount = options[fillValue].getValue();
                    while (availableFillValueCount > 0 && k < next.size()) {
                        next.set(k, fillValue);
                        availableFillValueCount--;
                        k++;
                    }
                    fillValue++;
                }
                return k == next.size() ? next : null;
            }

            int fillValue = next.findValueAtIndex(i) + 1;
            int k = i;

            while (k < next.size()) {
                if (fillValue >= options.length) {
                    if (i == 0) return null;
                    i--;
                    while (i >= 0 && next.findValueAtIndex(i) == maxSupportedValue) {
                        i--;
                    }
                    if (i < 0) return null;
                    fillValue = next.findValueAtIndex(i) + 1;
                    k = i;
                }

                int availableFillValueCount = options[fillValue].getValue();
                while (availableFillValueCount > 0 && k < next.size()) {
                    next.set(k, fillValue);
                    availableFillValueCount--;
                    k++;
                }
                if (k < next.size() && availableFillValueCount == 0) {
                    fillValue++;
                }
            }

            return next;
        }
    }
}