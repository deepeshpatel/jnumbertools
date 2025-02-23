/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.TwoLevelMap;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Generates every mᵗʰ multiset combination in the specified order, starting at a given rank.
 * <p>
 * The multiset is defined by a map of distinct elements and their frequencies, ordered by the specified Order:
 * <ul>
 *   <li>LEX: Ascending key order (e.g., A→J).</li>
 *   <li>REVERSE_LEX: Descending key order (e.g., J→A).</li>
 *   <li>INPUT: Preserves map insertion order.</li>
 * </ul>
 * A combination is a selection of exactly r items, returned as an immutable frequency map.
 * </p>
 */
public final class MultisetCombinationMth<T extends Comparable<T>> extends AbstractMultisetCombination<T> {

    private final long start;
    private final long increment;
    private final BigInteger total;
    private final TwoLevelMap<Integer, Integer, BigInteger> combinationsCountMemo;

    public MultisetCombinationMth(Map<T, Integer> options, int r, long start, long increment, Order order) {
        super(options, r, order);
        this.start = start;
        this.increment = increment <= 0 ? 5 : increment;
        this.combinationsCountMemo = new TwoLevelMap<>();
        this.total = Calculator.multisetCombinationsCount(this.r, frequencies);
        this.combinationsCountMemo.put(this.r, 0, total);
        if (start >= total.longValueExact()) {
            throw new IllegalArgumentException("Start rank " + start + " exceeds total combinations " + total);
        }
    }

    public MultisetCombinationMth(Map<T, Integer> options, int r, long start, long increment) {
        this(options, r, start, increment, Order.LEX);
    }

    @Override
    public Iterator<Map<T, Integer>> iterator() {
        return (r == 0 || options.length == 0) ? Util.emptyMapIterator() : new Itr();
    }

    private class Itr implements Iterator<Map<T, Integer>> {
        private BigInteger currentRank;
        private final Map<T, Integer> outputMap = new HashMap<>();

        private Itr() {
            currentRank = BigInteger.valueOf(start);
        }

        @Override
        public boolean hasNext() {
            return currentRank.compareTo(total) < 0;
        }

        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            int[] countVector = unrankMultisetCombination(r, currentRank.intValue());
            outputMap.clear();
            for (int i = 0; i < countVector.length; i++) {
                if (countVector[i] > 0) {
                    outputMap.put(options[i].getKey(), countVector[i]);
                }
            }
            currentRank = currentRank.add(BigInteger.valueOf(increment));
            return Map.copyOf(outputMap);
        }
    }

    private long getCombinationCount(int k, int index) {
        var count = combinationsCountMemo.get(k, index);
        if (count == null) {
            count = Calculator.multisetCombinationsCountStartingFromIndex(k, index, frequencies);
            combinationsCountMemo.put(k, index, count);
        }
        return count.longValue();
    }

    private int[] unrankMultisetCombination(int k, int rank) {
        int[] combination = new int[frequencies.length];

        for (int i = 0; i < frequencies.length; i++) {
            int maxForThisType = Math.min(frequencies[i], k);
            for (int x = maxForThisType; x >= 0; x--) {
                long ways = getCombinationCount(k - x, i + 1);
                if (rank < ways) {
                    combination[i] = x;
                    k -= x;
                    break;
                } else {
                    rank -= ways;
                }
            }
        }
        return combination;
    }
}