/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates subsets of a set within a specified size range, producing the subsets
 * at the specified ranks in lexicographical order.
 * <p>
 * This class is a thin wrapper that delegates rank lookups to {@link SubsetGeneratorMth} (which holds
 * the precomputed prefix-sum table, built once per generator) and arithmetic progressions
 * (lexOrderMth) directly to {@link SubsetGeneratorMth}.
 * </p>
 * <p>
 * Ranks are 0-based in lexicographical order. The rank sequence is consumed lazily, one rank per
 * {@code next()} call, and is never scanned up front: it may be infinite or single-use. A rank
 * {@code < 0} or {@code ≥ count()} throws {@link IllegalArgumentException} when it is reached.
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

    /** Built once: holds the prefix-sum table, so each rank costs only an unrank. Never iterated itself. */
    private final SubsetGeneratorMth<T> unranker;

    /**
     * Constructs a generator for subsets at specified ranks.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder#byRanks(Iterable)}.
     * All parameter validation (range validation, null checks) is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param from       minimum subset size (assumed 0 ≤ from ≤ elements.size())
     * @param to         maximum subset size (assumed from ≤ to ≤ elements.size())
     * @param ranks      iterable of 0-based rank numbers
     * @param elements   list of elements from which subsets are generated (assumed non-null)
     * @param calculator calculator for combinatorial operations (assumed non-null)
     */
    SubsetGeneratorByRanks(int from, int to, Iterable<BigInteger> ranks, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.ranks = ranks;
        this.calculator = calculator;
        this.unranker = new SubsetGeneratorMth<>(from, to, BigInteger.ONE, BigInteger.ZERO, elements, calculator);
    }

    /**
     * Creates a generator for every mᵗʰ subset in lexicographical order, starting from the specified position.
     * Delegates directly to the optimized {@link SubsetGeneratorMth} class.
     * <p>
     * Note that this walks the configured range itself; the {@code ranks} this generator was created
     * with play no part in it.
     * </p>
     *
     * @param m     the step size (must be > 0)
     * @param start the starting rank (must be ≥ 0, and &lt; the number of subsets in the range when that is &gt; 0)
     * @return a {@code SubsetGeneratorMth} instance
     * @throws IllegalArgumentException if m ≤ 0, start &lt; 0, or start ≥ the number of subsets in the range (when &gt; 0)
     */
    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, unranker.count());
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

            // Each rank is validated here, lazily: a rank < 0 or ≥ count() (every rank, if the range is
            // empty) throws IllegalArgumentException only when that rank is reached.
            return unranker.unrank(rankIterator.next());
        }
    }
}