/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates an iterable sequence of multiset combinations based on a provided sequence of ranks.
 * <p>
 * This class generates combinations of size {@code r} from a multiset defined by a {@code LinkedHashMap}
 * of elements and their frequencies (e.g., {A=2, B=1} allows combinations like {A=2}, {A=1, B=1}).
 * The combinations are determined by ranks from the provided {@code Iterable<BigInteger>}, mapped to
 * combinations using a multiset un-ranking algorithm. Each combination is returned as an immutable
 * frequency map. The order of elements in the combinations is controlled by the specified {@code Order}
 * parameter: {@code LEX} (ascending), {@code REVERSE_LEX} (descending), or {@code INSERTION} (as per
 * the map's insertion order).
 * </p>
 *
 * @param <T> the type of elements in the combinations; must be comparable for LEX/REVERSE_LEX ordering
 * @author Deepesh Patel
 * @version 3.0.1
 */
public class MultisetCombinationForSequence<T> extends AbstractMultisetCombination<T> {

    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a new MultisetCombinationForSequence instance.
     *
     * @param options    the {@code LinkedHashMap} of distinct elements and their frequencies defining the multiset
     * @param r          the size of each combination (r); must be non-negative
     * @param ranks      the iterable providing the sequence of ranks; each rank must be in [0, total combinations)
     * @throws IllegalArgumentException if r is negative or options/frequencies are invalid
     */
    public MultisetCombinationForSequence(LinkedHashMap<T, Integer> options, int r, Iterable<BigInteger> ranks) {
        super(options, r);
        this.ranks = ranks;
    }

    /**
     * Returns an iterator that generates multiset combinations based on the provided rank sequence.
     * <p>
     * Each combination is returned as an immutable frequency map, respecting the frequency constraints
     * of the multiset and the specified order (LEX, REVERSE_LEX, or INSERTION).
     * </p>
     *
     * @return an iterator over immutable frequency maps representing multiset combinations
     */
    @Override
    public Iterator<Map<T, Integer>> iterator() {
        return new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<Map<T, Integer>> {
        private final Iterator<BigInteger> rankIterator;
        private final Map<T, Integer> outputMap = new HashMap<>();

        /**
         * Constructs a new sequence iterator.
         */
        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public Map<T, Integer> next() {
            BigInteger m = rankIterator.next();
            int[] countVector = unrankMultisetCombination(r, m.intValue());
            outputMap.clear();
            for (int i = 0; i < countVector.length; i++) {
                if (countVector[i] > 0) {
                    outputMap.put(options[i].getKey(), countVector[i]);
                }
            }
            return Map.copyOf(outputMap);
        }

        private int[] unrankMultisetCombination(int k, int rank) {
            int[] combination = new int[frequencies.length];
            int remainingK = k;
            int remainingRank = rank;

            for (int i = 0; i < frequencies.length; i++) {
                int maxForThisType = Math.min(frequencies[i], remainingK);
                for (int x = maxForThisType; x >= 0; x--) {
                    long ways = Calculator.multisetCombinationsCountStartingFromIndex(remainingK - x, i + 1, frequencies).longValue();
                    if (remainingRank < ways) {
                        combination[i] = x;
                        remainingK -= x;
                        break;
                    } else {
                        remainingRank -= ways;
                    }
                }
            }
            return combination;
        }
    }
}