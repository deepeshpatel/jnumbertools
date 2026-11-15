/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.datastructure.FrequencyMap;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates an iterable sequence of multiset combinations based on a provided sequence of ranks.
 * <p>
 * This class generates combinations of size rᵣ from a multiset defined by a {@code LinkedHashMap}
 * of nₙ elements and their frequencies (e.g., {A=2, B=1} allows combinations like {A=2}, {A=1, B=1}).
 * The total number of combinations is given by the multiset combination formula C(nₙ, rᵣ) = Σ C(n₁, r₁)C(n₂, r₂)…,
 * where nₙ is the sum of frequencies, r₁+r₂+…=rᵣ, and nᵢ are individual frequencies. Combinations are
 * determined by ranks (e.g., 0ᵗʰ, 2ⁿᵈ) from the provided {@code Iterable<BigInteger>}, mapped to
 * combinations using a multiset un-ranking algorithm. Each combination is returned as an immutable
 * frequency map. The order of elements is controlled by the specified {@code Order} parameter:
 * {@code LEX} (ascending), {@code REVERSE_LEX} (descending), or {@code INSERTION} (map's insertion order).
 * </p>
 * <p>
 * Ranks are 0-based in lexicographical order.
 * </p>
 *
 * @param <T> the type of elements in the combinations; must be comparable for LEX/REVERSE_LEX ordering
 * @author Deepesh Patel
 */
public class MultisetCombinationByRanks<T> extends AbstractMultisetCombination<T>  {

    /**
     * The iterable sequence of rank numbers to generate combinations.
     */
    private final Iterable<BigInteger> ranks;
    private final BigInteger totalCombinations;

    /**
     * Constructs a new MultisetCombinationByRanks instance.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder#byRanks(Iterable)}.
     * All parameter validation (null check, non-negative frequencies, zero-frequency filtering, r ≥ 0)
     * is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param options a {@code LinkedHashMap} of distinct elements and their frequencies (assumes zero frequencies filtered)
     * @param r the size of each combination (assumed r ≥ 0)
     * @param ranks an iterable of 0-based rank numbers
     * @param totalCombinations pre-computed total for validation (assumed correct)
     */
    public MultisetCombinationByRanks(LinkedHashMap<T, Integer> options, int r, Iterable<BigInteger> ranks, BigInteger totalCombinations) {
        super(options, r);
        this.ranks = ranks;
        this.totalCombinations = totalCombinations;
    }

    /**
     * Returns an iterator over multiset combinations at specified ranks.
     * <p>
     * Each combination is an immutable frequency map, respecting the multiset's frequency constraints
     * and the specified order (LEX, REVERSE_LEX, or INSERTION).
     * </p>
     *
     * @return an iterator over immutable frequency maps representing multiset combinations
     */
    @Override
    public Iterator<Map<T, Integer>> iterator() {
        return new SequenceIterator();
    }


    /**
     * Iterator for generating multiset combinations at specified ranks.
     */
    private class SequenceIterator implements Iterator<Map<T, Integer>> {
        private final Iterator<BigInteger> rankIterator;

        /**
         * ways[i][j] = number of ways to choose j items from the types i..end. Built bottom-up once, on the first
         * rank that needs it, and reused for every later rank (no recursion, so any number of types is fine).
         */
        private BigInteger[][] ways;

        /**
         * The same table in primitive longs, used when the whole space fits in 62 bits and every table entry fits a
         * long; null otherwise, and then {@link #ways} is used.
         */
        private long[][] longWays;
        private boolean prepared;

        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        private long[][] buildLongWays() {
            if (totalCombinations.bitLength() > 62) return null;
            int n = frequencies.length;
            long[][] table = new long[n + 1][];
            table[n] = new long[]{1};
            long suffixSum = 0;
            try {
                for (int i = n - 1; i >= 0; i--) {
                    suffixSum += frequencies[i];
                    int length = (int) Math.min(r, suffixSum) + 1;
                    int c = frequencies[i];
                    long[] below = table[i + 1];
                    long[] row = new long[length];
                    long window = 0; // sum of below[j-c .. j]
                    for (int j = 0; j < length; j++) {
                        if (j < below.length) window = Math.addExact(window, below[j]);
                        int drop = j - c - 1;
                        if (drop >= 0 && drop < below.length) window -= below[drop];
                        row[j] = window;
                    }
                    table[i] = row;
                }
            } catch (ArithmeticException tooBigForLong) {
                return null; // an intermediate count is larger than the total itself: use BigInteger
            }
            return table;
        }

        private int[] unrankLong(int r, long rank) {
            int[] combination = new int[frequencies.length];
            int remainingR = r;
            long remainingRank = rank;
            for (int i = 0; i < frequencies.length; i++) {
                long[] rest = longWays[i + 1];
                for (int x = Math.min(frequencies[i], remainingR); x >= 0; x--) {
                    int need = remainingR - x;
                    long waysForRest = need < rest.length ? rest[need] : 0;
                    if (remainingRank < waysForRest) {
                        combination[i] = x;
                        remainingR -= x;
                        break;
                    }
                    remainingRank -= waysForRest;
                }
            }
            return combination;
        }

        private BigInteger[][] buildWays() {
            int n = frequencies.length;
            BigInteger[][] table = new BigInteger[n + 1][];
            table[n] = new BigInteger[]{BigInteger.ONE};
            long suffixSum = 0;
            for (int i = n - 1; i >= 0; i--) {
                suffixSum += frequencies[i];
                int length = (int) Math.min(r, suffixSum) + 1;
                int c = frequencies[i];
                BigInteger[] below = table[i + 1];
                BigInteger[] row = new BigInteger[length];
                BigInteger window = BigInteger.ZERO; // sum of below[j-c .. j]
                for (int j = 0; j < length; j++) {
                    if (j < below.length) {
                        window = window.add(below[j]);
                    }
                    int drop = j - c - 1;
                    if (drop >= 0 && drop < below.length) {
                        window = window.subtract(below[drop]);
                    }
                    row[j] = window;
                }
                table[i] = row;
            }
            return table;
        }

        @Override
        public boolean hasNext() { return rankIterator.hasNext(); }

        /**
         * Returns the next multiset combination for the current rank.
         * <p>
         * Maps the mᵗʰ rank to a frequency count vector using a multiset un-ranking algorithm,
         * then converts it to an immutable frequency map of elements.
         * </p>
         *
         * @return an immutable frequency map representing the next combination
         * @throws java.util.NoSuchElementException if no more ranks are available
         */
        @Override
        public Map<T, Integer> next() {
            BigInteger m = rankIterator.next();
            if (m.signum() < 0 || m.compareTo(totalCombinations) >= 0) {
                throw new IllegalArgumentException("Rank " + m + " out of bounds [0, " + totalCombinations + ")");
            }

            if (!prepared) {
                prepared = true;
                longWays = buildLongWays();
                if (longWays == null) ways = buildWays();
            }

            // m < totalCombinations, so when the long table exists the rank fits in a long too
            int[] combination = longWays != null ? unrankLong(r, m.longValue()) : unrankMultisetCombination(r, m);

            // Fresh arrays per result, so they can be handed over to the immutable map without copying.
            int distinct = 0;
            for (int c : combination) {
                if (c > 0) distinct++;
            }
            Object[] keys = new Object[distinct];
            int[] counts = new int[distinct];
            for (int i = 0, d = 0; i < combination.length; i++) {
                if (combination[i] > 0) {
                    keys[d] = options[i].getKey();
                    counts[d++] = combination[i];
                }
            }
            return FrequencyMap.wrap(keys, counts);
        }

        private int[] unrankMultisetCombination(int r, BigInteger rank) {
            int[] combination = new int[frequencies.length];
            int remainingR = r;
            BigInteger remainingRank = rank; // Use BigInteger directly

            for (int i = 0; i < frequencies.length; i++) {
                int maxForThisType = Math.min(frequencies[i], remainingR);

                // Iterate x from MAX down to 0 (standard unranking)
                for (int x = maxForThisType; x >= 0; x--) {
                    BigInteger[] rest = ways[i + 1];
                    int need = remainingR - x;
                    BigInteger waysForRest = need < rest.length ? rest[need] : BigInteger.ZERO;

                    // Compare BigIntegers
                    if (remainingRank.compareTo(waysForRest) < 0) {
                        combination[i] = x;
                        remainingR -= x;
                        break; // Found the count for this type, move to next type
                    } else {
                        remainingRank = remainingRank.subtract(waysForRest);
                    }
                }
            }
            return combination;
        }
    }

}
