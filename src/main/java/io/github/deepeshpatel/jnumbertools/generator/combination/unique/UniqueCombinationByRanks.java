/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Generates unique combinations of size r from n items based on a sequence of ranks.
 * <p>
 * A unique combination is a selection of r items from n distinct items where order does not matter,
 * with the total number of combinations given by ⁿCᵣ = n! / (r! * (n-r)!). Each combination is
 * identified by a rank in [0, ⁿCᵣ), and this class maps provided ranks to combinations using
 * combinadic unranking. Supports strategies like random sampling or lexicographical sequences.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public class UniqueCombinationByRanks<T> extends AbstractGenerator<T> {

    private final int r;
    private final BigInteger nCr;
    private final CombinadicAlgorithms algorithms;
    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a generator for unique combinations based on a rank sequence.
     *
     * @param elements   the list of n items to generate combinations from (must not be null or empty)
     * @param r          the size of each combination (0 ≤ r ≤ n)
     * @param ranks      the iterable of ranks (each rank in [0, ⁿCᵣ))
     * @param calculator the calculator for computing combination counts
     * @throws IllegalArgumentException if r < 0, r > n, elements is null/empty, or any rank < 0 or ≥ ⁿCᵣ
     */
    public UniqueCombinationByRanks(List<T> elements, int r, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.r = r;
        this.ranks = ranks;
        checkParamCombination(elements.size(), r, "unique combination sequence");
        this.nCr = calculator.nCr(elements.size(), r);
        this.algorithms = new CombinadicAlgorithms(calculator);
    }

    /**
     * Returns an iterator over unique combinations based on the provided rank sequence.
     *
     * @return an iterator of lists, each representing a combination
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    /**
     * Iterator for generating unique combinations based on the rank sequence.
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
         * @throws IllegalArgumentException if the rank is < 0 or ≥ ⁿCᵣ
         */
        @Override
        public List<T> next() {
            BigInteger m = rankIterator.next();
            int[] indices = algorithms.unRank(m, nCr, elements.size(), r);
            return indicesToValues(indices);
        }
    }
}