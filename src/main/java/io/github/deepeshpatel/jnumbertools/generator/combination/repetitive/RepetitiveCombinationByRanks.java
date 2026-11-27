/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.LongBinomials;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Generates repetitive combinations of size r from n items based on a sequence of ranks.
 * <p>
 * A repetitive combination allows items to be selected multiple times, with the total number of
 * combinations given by ⁿ⁺ᵣ⁻¹Cᵣ = (n+r-1)! / (r! * (n-1)!). Order does not matter, and elements
 * are treated as distinct based on their indices in the input list. Each combination is identified
 * by a rank in [0, ⁿ⁺ᵣ⁻¹Cᵣ), mapped to a combination using a lexicographical unranking algorithm.
 * Supports strategies like random sampling or lexicographical sequences.
 * </p>
 * <p>
 * Ranks are 0-based in lexicographical order.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public class RepetitiveCombinationByRanks<T> extends AbstractGenerator<T> {

    private final int r;
    private final Calculator calculator;
    private final Iterable<BigInteger> ranks;
    private final BigInteger totalCombinations;
    private final long[][] longTable;   // binomials for the primitive path, or null when it does not apply

    /**
     * Constructs a generator for repetitive combinations based on a rank sequence.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link RepetitiveCombinationBuilder#byRanks(Iterable)}.
     * All parameter validation (null check, r ≥ 0) is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param elements   the list of items to generate combinations from (assumed non-null)
     * @param r          the size of each combination (assumed r ≥ 0)
     * @param ranks      the iterable of ranks
     * @param calculator the calculator for computing combination counts (assumed non-null)
     */
    public RepetitiveCombinationByRanks(List<T> elements, int r, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.r = r;
        this.ranks = ranks;
        this.calculator = calculator;
        if (r < 0) {
            throw new IllegalArgumentException("Combination size (r) must be non-negative");
        }
        totalCombinations = calculator.nCrRepetitive(elements.size(), r);
        // C(n - j + k - 1, k) is looked up for j < n and k < r, so rows up to n + r - 2 and columns up to r - 1.
        longTable = totalCombinations.bitLength() <= 62 && r > 0 && !elements.isEmpty()
                ? LongBinomials.table(elements.size() + r - 1, r) : null;
    }

    /**
     * Returns an iterator over repetitive combinations based on the provided rank sequence.
     *
     * @return an iterator of lists, each representing a combination
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    /**
     * Iterator for generating repetitive combinations based on the rank sequence.
     */
    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

        private SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        /**
         * Checks if more combinations are available.
         *
         * @return true if the rank sequence has more ranks, false otherwise
         */
        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        /**
         * Returns the combination corresponding to the next rank.
         *
         * @return a list representing the next combination
         * @throws java.util.NoSuchElementException if no more ranks are available
         * @throws IllegalArgumentException if the rank is < 0 or ≥ ⁿ⁺ᵣ⁻¹Cᵣ
         */
        @Override
        public List<T> next() {
            BigInteger m = rankIterator.next();

            if (m.signum() < 0 || m.compareTo(totalCombinations) >= 0) {
                throw new IllegalArgumentException("Rank " + m + " is out of bounds [0, " + totalCombinations + ")");
            }

            int[] indices = longTable != null ? mthCombinationLong(m.longValue()) : mthCombinationWithRepetition(m);
            return indicesToValues(indices);
        }

        /**
         * Computes the indices for the mᵗʰ repetitive combination.
         * <p>
         * Given a rank m in [0, ⁿ⁺ᵣ⁻¹Cᵣ), this method calculates the corresponding combination
         * indices using a lexicographical unranking algorithm based on the repetitive combination
         * formula. It iteratively selects indices by comparing the remaining rank against the
         * number of combinations for each possible element.
         *
         * @param remainingM the rank of the combination (m ≥ 0); may exceed the range of a {@code long}
         * @return an array of indices representing the combination
         */
        private int[] mthCombinationLong(long remainingM) {
            int n = elements.size();
            int[] result = new int[r];
            int currentValue = 0;

            for (int i = 0; i < r; i++) {
                int k = r - i - 1;
                for (int j = currentValue; j < n; j++) {
                    long combinationsWithCurrentElement = longTable[n - j + k - 1][k];   // = nCrRepetitive(n - j, k)
                    if (remainingM < combinationsWithCurrentElement) {
                        result[i] = j;
                        currentValue = j;
                        break;
                    }
                    remainingM -= combinationsWithCurrentElement;
                }
            }
            return result;
        }

        private int[] mthCombinationWithRepetition(BigInteger m) {
            int n = elements.size();
            int[] result = new int[r];
            BigInteger remainingM = m;
            int currentValue = 0;

            for (int i = 0; i < r; i++) {
                for (int j = currentValue; j < n; j++) {
                    BigInteger combinationsWithCurrentElement = calculator.nCrRepetitive(n - j, r - i - 1);
                    if (remainingM.compareTo(combinationsWithCurrentElement) < 0) {
                        result[i] = j;
                        currentValue = j;
                        break;
                    }
                    remainingM = remainingM.subtract(combinationsWithCurrentElement);
                }
            }
            return result;
        }
    }
}
