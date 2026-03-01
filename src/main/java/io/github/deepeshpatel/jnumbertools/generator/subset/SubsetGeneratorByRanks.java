/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates subsets of a set within a specified size range, producing the subsets
 * at the specified ranks in lexicographical order.
 * <p>
 * This generator uses an efficient approach to directly access the subset at a given rank
 * without generating all preceding subsets.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @author Deepesh Patel
 */
public final class SubsetGeneratorByRanks<T> extends AbstractGenerator<T> {

    private final Calculator calculator;
    private final Combinations combinations;
    private final Iterable<BigInteger> ranks;
    private final int from;
    private final int to;

    /**
     * Constructs a {@code SubsetGeneratorByRanks} to generate subsets at the given ranks
     * within the specified size range.
     *
     * @param from       the minimum subset size (inclusive)
     * @param to         the maximum subset size (inclusive)
     * @param ranks      the iterable providing the ranks (0-based) of subsets to generate
     * @param elements   the list of elements from which subsets are generated
     * @param calculator the calculator used for combinatorial calculations
     */
    SubsetGeneratorByRanks(int from, int to, Iterable<BigInteger> ranks, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.ranks = ranks;
        this.calculator = calculator;
        this.combinations = new Combinations(calculator);
    }

    /**
     * Computes the indices of the subset at the given rank in lexicographical order.
     *
     * @param rank the position of the subset to compute
     * @return the list of indices representing the subset at the given rank
     */
    private List<Integer> subsetAtRank(BigInteger rank) {
        int r = 0;
        int size = elements.size();
        BigInteger sum = BigInteger.ZERO;
        BigInteger prev = BigInteger.ZERO;

        for (; r < size && sum.compareTo(rank) < 0; r++) {
            prev = sum;
            sum = sum.add(calculator.nCr(size, r));
        }

        if (sum.equals(rank)) {
            return combinations.unique(size, r).lexOrder().iterator().next();
        }

        rank = rank.subtract(prev);
        return new Combinations(calculator).unique(size, r - 1)
                .lexOrderMth(rank, rank).iterator().next();
    }

    /**
     * Calculates the total number of subsets before the specified range.
     *
     * @return the total number of subsets with sizes from 0 to from−1
     */
    private BigInteger totalSubsetsBeforeRange() {
        return calculator.totalSubsetsInRange(0, from - 1, elements.size());
    }

    public synchronized Iterator<List<T>> iterator() {
        return new Itr();
    }

    public String toString() {
        return "SubsetGeneratorByRanks{from=" + from + ", to=" + to + ", ranks=" + ranks + "}";
    }

    private class Itr implements Iterator<List<T>> {

        private final Iterator<BigInteger> rankIterator = ranks.iterator();

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            BigInteger relativeRank = rankIterator.next();
            BigInteger offset = totalSubsetsBeforeRange();
            BigInteger absoluteRank = relativeRank.add(offset);

            List<Integer> result = subsetAtRank(absoluteRank);
            return indicesToValues(result.stream().mapToInt(Integer::intValue).toArray());
        }
    }
}