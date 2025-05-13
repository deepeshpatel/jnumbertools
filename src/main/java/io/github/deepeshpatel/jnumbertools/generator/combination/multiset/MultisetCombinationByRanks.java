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
 * This class generates combinations of size rᵣ from a multiset defined by a {@code LinkedHashMap}
 * of nₙ elements and their frequencies (e.g., {A=2, B=1} allows combinations like {A=2}, {A=1, B=1}).
 * The total number of combinations is given by the multiset combination formula C(nₙ, rᵣ) = Σ C(n₁, r₁)C(n₂, r₂)…,
 * where nₙ is the sum of frequencies, r₁+r₂+…=rᵣ, and nᵢ are individual frequencies. Combinations are
 * determined by ranks (e.g., 0ᵗʰ, 2ⁿᵈ) from the provided {@code Iterable<BigInteger>}, mapped to
 * combinations using a multiset un-ranking algorithm. Each combination is returned as an immutable
 * frequency map. The order of elements is controlled by the specified {@code Order} parameter:
 * {@code LEX} (ascending), {@code REVERSE_LEX} (descending), or {@code INSERTION} (map's insertion order).
 * </p>
 *
 * @param <T> the type of elements in the combinations; must be comparable for LEX/REVERSE_LEX ordering
 * @author Deepesh Patel
 */
public class MultisetCombinationByRanks<T> extends AbstractMultisetCombination<T> {

    /**
     * The iterable sequence of rank numbers to generate combinations.
     */
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a new MultisetCombinationByRanks instance.
     *
     * @param options a {@code LinkedHashMap} of distinct elements and their frequencies; insertion order affects INSERTION ordering
     * @param r the size of each combination (rᵣ); must be non-negative
     * @param ranks an iterable of 0-based rank numbers (0 ≤ rank < total combinations)
     * @return a new MultisetCombinationByRanks instance
     * @throws IllegalArgumentException if rᵣ is negative, options is invalid, or ranks are out of bounds
     */
    public MultisetCombinationByRanks(LinkedHashMap<T, Integer> options, int r, Iterable<BigInteger> ranks) {
        super(options, r);
        this.ranks = ranks;
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
        private final Map<T, Integer> outputMap = new HashMap<>();

        /**
         * Constructs an iterator for the given sequence of ranks.
         */
        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if the sequence has more ranks; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

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
            int[] countVector = unrankMultisetCombination(r, m.intValue());
            outputMap.clear();
            for (int i = 0; i < countVector.length; i++) {
                if (countVector[i] > 0) {
                    outputMap.put(options[i].getKey(), countVector[i]);
                }
            }
            return Map.copyOf(outputMap);
        }

        /**
         * Maps a rank to a multiset combination's frequency count vector.
         *
         * @param r the size of the combination (rᵣ)
         * @param rank the 0-based rank of the combination
         * @return an array representing the frequency count vector
         */
        private int[] unrankMultisetCombination(int r, int rank) {
            int[] combination = new int[frequencies.length];
            int remainingR = r;
            int remainingRank = rank;

            for (int i = 0; i < frequencies.length; i++) {
                int maxForThisType = Math.min(frequencies[i], remainingR);
                for (int x = maxForThisType; x >= 0; x--) {
                    long ways = Calculator.multisetCombinationsCountStartingFromIndex(remainingR - x, i + 1, frequencies).longValue();
                    if (remainingRank < ways) {
                        combination[i] = x;
                        remainingR -= x;
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