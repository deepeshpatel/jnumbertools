/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

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
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public class RepetitiveCombinationOfRanks<T> extends AbstractGenerator<T> {

    private final int r;
    private final Calculator calculator;
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a generator for repetitive combinations based on a rank sequence.
     *
     * @param elements   the list of n items to generate combinations from (must not be null or empty)
     * @param r          the size of each combination (r ≥ 0)
     * @param ranks      the iterable of ranks (each rank in [0, ⁿ⁺ᵣ⁻¹Cᵣ))
     * @param calculator the calculator for computing combination counts
     * @throws IllegalArgumentException if r < 0, elements is null/empty, or any rank < 0 or ≥ ⁿ⁺ᵣ⁻¹Cᵣ
     */
    public RepetitiveCombinationOfRanks(List<T> elements, int r, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.r = r;
        this.ranks = ranks;
        this.calculator = calculator;
        if (r < 0) {
            throw new IllegalArgumentException("Combination size (r) must be non-negative");
        }
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
            int[] indices = mthCombinationWithRepetition(m.longValueExact());
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
         * @param m the rank of the combination (m ≥ 0)
         * @return an array of indices representing the combination
         * @throws ArithmeticException if m cannot be converted to a long
         */
        private int[] mthCombinationWithRepetition(long m) {
            int n = elements.size();
            int[] result = new int[r];
            BigInteger remainingM = BigInteger.valueOf(m);
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