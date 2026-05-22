/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.Derangadic;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates derangements at a sequence of lexicographical ranks.
 * <p>
 * Each rank in the supplied {@link Iterable} is converted to a derangement via
 * {@link Derangadic#derangement()}  #unrank(BigInteger, int, Calculator)}. Used by
 * {@link DerangementBuilder} to implement {@code lexOrderMth},
 * {@code byRanks}, {@code sample} and {@code choice}.
 * </p>
 * <p>
 * Rank validation (negative / out-of-range) is deferred to iteration time, so
 * that streaming a long rank sequence does not pay for upfront validation.
 * </p>
 *
 * <h3>Edge cases</h3>
 * <ul>
 *   <li>For {@code n = 0} ({@code D_0 = 1}), the only valid rank is {@code 0}
 *       which decodes to an empty list.</li>
 *   <li>For {@code n = 1} ({@code D_1 = 0}), no rank is valid; iteration over
 *       a non-empty rank sequence throws {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * @param <T> the element type
 * @author Deepesh Patel &amp; Aditya Patel
 * @see DerangementBuilder
 * @since 3.0.2
 */
public final class DerangementByRanks<T> extends AbstractGenerator<T> {

    private final Iterable<BigInteger> ranks;
    private final Calculator calculator;
    private final BigInteger totalDerangements;

    /**
     * Constructs the generator.
     * <p><strong>Internal use only.</strong> Library users should obtain
     * instances through the {@link DerangementBuilder} factory methods
     * ({@code lexOrderMth}, {@code byRanks}, {@code sample}, {@code choice}).
     * Null checks on {@code ranks} are performed by the builder.</p>
     */
    public DerangementByRanks(List<T> elements, Iterable<BigInteger> ranks, Calculator calculator) {
        super(elements);
        this.ranks = ranks;
        this.calculator = calculator;
        this.totalDerangements = calculator.subFactorial(elements.size());
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    private final class SequenceIterator implements Iterator<List<T>> {

        private final Iterator<BigInteger> rankIterator = ranks.iterator();
        private final int n = elements.size();

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            if (!rankIterator.hasNext()) {
                throw new NoSuchElementException("No more derangements available in rank sequence");
            }
            BigInteger rank = rankIterator.next();
            if (rank.signum() < 0) {
                throw new IllegalArgumentException(
                        "Rank " + rank + " cannot be negative. Valid range is [0, "
                                + totalDerangements + ")");
            }
            if (rank.compareTo(totalDerangements) >= 0) {
                throw new IllegalArgumentException(
                        "Rank " + rank + " exceeds total derangements " + totalDerangements);
            }

            if (n == 0) {
                // D_0 = 1; the lone derangement is the empty list.
                return Collections.emptyList();
            }
            // n >= 2 here: n=1 has total=0 so the rank check above always rejects.
            var derangadic = new Derangadic(n, rank, calculator);
            return indicesToValues(derangadic.derangement());
        }
    }
}

