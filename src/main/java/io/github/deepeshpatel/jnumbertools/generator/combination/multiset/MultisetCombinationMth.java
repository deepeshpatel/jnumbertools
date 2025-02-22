/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Generates every mᵗʰ multiset combination in lexicographic order, starting at a specified rank.
 * <p>
 * The multiset is defined by a map of distinct elements (in lex order) and their corresponding frequencies.
 * A combination is defined as a multiset selection of exactly r items (where r must be ≤ the total number of items).
 * Each combination is returned as a frequency map indicating how many times each element is selected.
 * </p>
 * <p>
 * The constructor takes a start rank and an increment (step). For example, if <code>start = 3</code> and <code>m = 2</code>,
 * then the iterator returns the 3rd, 5th, 7th, … combination (in lexicographic order). If <code>start = 0</code> and <code>m = 0</code>,
 * then m is interpreted as a default step of 5.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 */
public final class MultisetCombinationMth<T extends Comparable<T>> implements Iterable<Map<T, Integer>> {

    private final int[] frequencies;
    private final Map.Entry<T, Integer>[] entries;
    private final int r;
    private final long start;
    private long increment;
    private final BigInteger total;

    /**
     * Constructs a MultisetCombinationMth generator.
     *
     * @param options     a map of distinct items (keys) and their corresponding frequencies (values),
     *                    where each frequency must be a nonnegative integer
     * @param r           the number of items to select in each combination
     * @param start       the 0-indexed rank of the first combination to generate
     * @param increment   positive step size (every mᵗʰ combination is returned)
     * @throws IllegalArgumentException if r or start is negative, if increment is negative,
     *                                  or if the options map is empty or contains negative frequencies
     */
    public MultisetCombinationMth(Map<T, Integer> options, int r, long start, long increment) {

        if (r < 0 || start < 0 || increment <= 0 || options == null) {
            String msg = "Must follow constraint(s):  r >=0 and start >=0 and " +
                    "increment >0 and options must not null";
            throw new IllegalArgumentException(msg);
        }

        this.entries = Util.toLexOrderedMap(options);
        this.frequencies = new int[entries.length];
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = entries[i].getValue();
        }

        int totalAvailable = Arrays.stream(frequencies).sum();
        this.r = r > totalAvailable ? 0 : r;
        this.start = start;
        this.increment = (increment == 0) ? 5 : increment;
        this.total = Calculator.multisetCombinationsCount(r, frequencies); // Assuming this now returns BigInteger
    }

    public Stream<Map<T, Integer>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<Map<T, Integer>> iterator() {
        // If no combinations can be generated, return an empty iterator.
        return (r == 0 || entries.length == 0) ? Util.emptyMapIterator() : new Itr();
    }

    private class Itr implements Iterator<Map<T, Integer>> {
        private BigInteger currentRank;

        private Itr() {
            currentRank = BigInteger.valueOf(start);
        }

        @Override
        public boolean hasNext() {
            return currentRank.compareTo(total) < 0;
        }

        @Override
        public Map<T, Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            // Unrank the combination corresponding to currentRank.
            int[] countVector = unrankMultisetCombination(r, frequencies, currentRank.intValue());
            Map<T, Integer> combination = new HashMap<>();
            // Convert count vector to a frequency map.
            for (int i = 0; i < countVector.length; i++) {
                if (countVector[i] > 0) { // Only include elements with non-zero counts
                    combination.put(entries[i].getKey(), countVector[i]);
                }
            }
            currentRank = currentRank.add(BigInteger.valueOf(increment));
            return combination;
        }
    }

    /**
     * Unranks the multiset combination corresponding to the given rank (0-indexed), returning a count vector.
     * <p>
     * The count vector is an array of integers of the same length as {@code frequencies} whose sum is k,
     * where each element indicates how many items of that type are selected. This version iterates in descending
     * order for each item type so that the resulting multiset combination (after converting the count vector) is in
     * lexicographic order (smallest first).
     * </p>
     *
     * @param k           the total number of items to select
     * @param frequencies an array of nonnegative integers representing the available count for each item type
     * @param rank        the 0-indexed rank of the desired combination (in lex order of the resulting multisets)
     * @return an int array representing the count vector (its entries sum to k)
     */
    public int[] unrankMultisetCombination(int k, int[] frequencies, int rank) {
        int n = frequencies.length;
        int[] combination = new int[n];
        TwoLevelMap<Integer, Integer, Integer> memo = new TwoLevelMap<>();
        for (int i = 0; i < n; i++) {
            int maxForThisType = Math.min(frequencies[i], k);
            for (int x = maxForThisType; x >= 0; x--) {
                int ways = Calculator.multisetCombinationsCountStartingFromIndex(k - x, i + 1, frequencies).intValue();
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

    /**
     * A simple two-level map for memoization.
     *
     * @param <K1> the type of the first key
     * @param <K2> the type of the second key
     * @param <V>  the type of the value
     */
    public static class TwoLevelMap<K1, K2, V> extends ConcurrentHashMap<K1, Map<K2, V>> {
        public V get(K1 key1, K2 key2) {
            Map<K2, V> inner = get(key1);
            return inner == null ? null : inner.get(key2);
        }
        public V put(K1 key1, K2 key2, V value) {
            computeIfAbsent(key1, k -> new ConcurrentHashMap<>()).put(key2, value);
            return value;
        }
    }
}