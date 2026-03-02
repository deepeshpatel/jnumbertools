/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates subsets of a set within a specified size range, producing the subsets
 * at the specified ranks in lexicographical order.
 * <p>
 * This class is a thin wrapper that delegates single-rank lookups to {@link SubsetGeneratorMth#build()}
 * and arithmetic progressions (lexOrderMth) directly to {@link SubsetGeneratorMth}.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @author Deepesh Patel
 */
public final class SubsetGeneratorByRanks<T> extends AbstractGenerator<T> {

    private final Calculator calculator;
    private final Iterable<BigInteger> ranks;
    private final int from;
    private final int to;

    SubsetGeneratorByRanks(int from, int to, Iterable<BigInteger> ranks, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.ranks = ranks;
        this.calculator = calculator;
    }

    /**
     * Creates a generator for every mᵗʰ subset in lexicographical order, starting from the specified position.
     * Delegates directly to the optimized {@link SubsetGeneratorMth} class.
     *
     * @param m     the step size (must be > 0)
     * @param start the starting rank (must be >= 0)
     * @return a {@code SubsetGeneratorMth} instance
     * @throws IllegalArgumentException if m <= 0 or start < 0
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        EveryMthIterable.validateLexOrderMthParams(m,start);
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    @Override
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
            return new Subsets(calculator).of(elements).inRange(from,to).lexOrderMth(BigInteger.ONE, relativeRank).build();

        }
    }
}