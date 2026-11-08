/*
 * JNumberTools Library v4.0.0
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.generator.base.*;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;

import java.math.BigInteger;
import java.util.*;

/**
 * Builder for generating simple Cartesian products of multiple sets.
 *
 * <p>Instances are <em>immutable</em>: {@link #and(List)} returns a new builder
 * with the additional dimension appended; the original is unchanged. This means
 * chained calls like {@code b.and(x).and(y)} are safe and {@code b} remains a
 * valid single-dimension builder throughout.
 *
 * <h2>Rules (see {@code ValidationRules.md})</h2>
 * <ul>
 *   <li>A {@code null} dimension is a programmer error: {@link NullPointerException}.
 *       So is a {@code null} element inside a dimension; it is detected while the list is
 *       copied, i.e. when the builder is created, not later during iteration.</li>
 *   <li>Every dimension is copied (snapshot): changing the caller's list afterwards has no effect
 *       on {@link #count()} or on any generator this builder returns.</li>
 *   <li>Generators are re-iterable (each {@code iterator()} call is a fresh traversal) and every
 *       tuple they return is immutable.</li>
 *   <li>{@link #isEmpty()} is {@code true} if and only if {@link #count()} is {@code 0}.</li>
 * </ul>
 *
 * <h2>Count</h2>
 * <pre>
 * ┌──────────────────────────┬───────────┬──────────────┬───────────┐
 * │ Scenario                 │ count()   │ iterator     │ isEmpty() │
 * ├──────────────────────────┼───────────┼──────────────┼───────────┤
 * │ Any dimension empty      │ 0         │ []           │ true      │
 * │ All dimensions non-empty │ Π sizes   │ products     │ false     │
 * └──────────────────────────┴───────────┴──────────────┴───────────┘
 * </pre>
 * <p>
 * This includes a single empty dimension: {@code simpleProductOf([])} has no tuples.
 * Each tuple is the concatenation of one element per dimension, so a non-empty product never
 * contains the empty tuple.
 * </p>
 *
 * <h2>Implementation notes</h2>
 * <ul>
 *   <li>Rank decomposition uses direct {@code list.get(index)}: O(1) per dimension.</li>
 *   <li>{@code and()} copies only the new dimension plus the O(dimensions) list of lists.</li>
 *   <li>{@code count()} is computed once, whenever a dimension is added.</li>
 * </ul>
 *
 * @author Deepesh Patel
 */
public final class SimpleProductBuilder implements Builder<Object> {

    private final List<List<?>> allLists;   // private snapshots; unmodifiable
    private final BigInteger cachedCount;   // always pre-computed, never null
    private final BigInteger[] dimSizes;    // dimSizes[i] = allLists.get(i).size(), pre-computed for rank decomposition

    // ── Public constructors / factory ────────────────────────────────────────

    /**
     * Creates a builder with a single dimension.
     *
     * @param elements first dimension; copied, so later changes to the caller's list have no effect.
     *                 An empty list is allowed and makes the product empty.
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public SimpleProductBuilder(List<?> elements) {
        this(List.<List<?>>of(snapshot(elements)), null);
    }

    /**
     * Private constructor shared by the public one and {@link #and(List)}; the caller has already made the
     * defensive copies. The second parameter only distinguishes it from the public constructor (same erasure).
     */
    private SimpleProductBuilder(List<List<?>> allLists, Void unused) {
        this.allLists = allLists;
        this.cachedCount = computeCount(allLists);
        this.dimSizes = new BigInteger[allLists.size()];
        for (int i = 0; i < dimSizes.length; i++) {
            dimSizes[i] = BigInteger.valueOf(allLists.get(i).size());
        }
    }

    // ── Dimension chaining ───────────────────────────────────────────────────

    /**
     * Returns a <em>new</em> builder with {@code elements} appended as the next dimension.
     * This builder is not modified.
     *
     * @param elements the new dimension; copied. An empty list is allowed and makes the product empty.
     * @return new immutable builder
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public SimpleProductBuilder and(List<?> elements) {
        List<?> copy = snapshot(elements);
        List<List<?>> newLists = new ArrayList<>(allLists.size() + 1); // O(dimensions)
        newLists.addAll(allLists);
        newLists.add(copy);
        return new SimpleProductBuilder(Collections.unmodifiableList(newLists), null);
    }

    // ── Builder interface ────────────────────────────────────────────────────

    @Override
    public StreamableIterable<Object> lexOrder() {
        return new SimpleProductGenerator();
    }

    /**
     * {@inheritDoc}
     * <p>If the product is empty ({@code count() == 0}) any {@code start ≥ 0} is accepted and the
     * generator yields nothing.</p>
     */
    @Override
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, cachedCount);
        return rankBased(new EveryMthIterable(start, m, cachedCount));
    }

    /**
     * {@inheritDoc}
     * <p>Only a null {@code ranks} is rejected up front. The sequence is consumed lazily, one rank at a
     * time (it may be infinite or single-use); a rank {@code < 0} or {@code ≥ count()} is reported as an
     * {@link IllegalArgumentException} during iteration, when that rank is reached.</p>
     */
    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return rankBased(ranks);
    }

    /**
     * {@inheritDoc}
     * <p>Sampling is with replacement, so {@code sampleSize} may exceed {@code count()}, but the product
     * must contain at least one tuple.</p>
     *
     * @throws IllegalArgumentException if {@code sampleSize ≤ 0} or {@code count() == 0}
     * @throws NullPointerException if {@code random} is null
     */
    @Override
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        // BigIntegerChoice enforces: count > 0, sampleSize > 0, random != null
        return rankBased(new BigIntegerChoice(cachedCount, sampleSize, random));
    }

    /**
     * {@inheritDoc}
     * <p>Sampling is without replacement: {@code sampleSize > count()} (including {@code count() == 0})
     * is an error.</p>
     *
     * @throws IllegalArgumentException if {@code sampleSize ≤ 0} or {@code sampleSize > count()}
     * @throws NullPointerException if {@code random} is null
     */
    @Override
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        // BigIntegerSample enforces: count > 0, 0 < sampleSize ≤ count, random != null
        return rankBased(new BigIntegerSample(cachedCount, sampleSize, random));
    }

    @Override
    public BigInteger count() {
        return cachedCount;
    }

    @Override
    public boolean isEmpty() {
        return cachedCount.signum() == 0;
    }

    @Override
    public String toString() {
        return "SimpleProductBuilder{allLists=" + allLists + ", count=" + cachedCount + '}';
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /** Null check + snapshot + null-element check in one pass (rules 1, 7 and 8). */
    private static List<?> snapshot(List<?> elements) {
        Util.validateInput(elements);
        return Util.copyOfNoNulls(elements);
    }

    private static BigInteger computeCount(List<List<?>> lists) {
        BigInteger c = BigInteger.ONE;
        for (List<?> l : lists) {
            if (l.isEmpty()) return BigInteger.ZERO;
            c = c.multiply(BigInteger.valueOf(l.size()));
        }
        return c;
    }

    private StreamableIterable<Object> rankBased(Iterable<BigInteger> ranks) {
        return new RankBasedGenerator(ranks);
    }

    // ── lexOrder generator ───────────────────────────────────────────────────

    private class SimpleProductGenerator extends AbstractGenerator<Object> {

        SimpleProductGenerator() { super(Collections.emptyList()); }

        @Override
        public Iterator<List<Object>> iterator() {
            return new SimpleProduct(allLists).iterator();
        }
    }

    // ── rank based generator (byRanks, lexOrderMth, choice, sample) ──────────

    /**
     * Rank-based iteration via direct mixed-radix decomposition on the raw lists. No intermediate
     * Builder or iterator objects are created per element: each rank resolves to a tuple with one
     * {@code list.get(index)} call per dimension.
     * <p>
     * Re-iterable: every {@code iterator()} call asks the rank source for a fresh iterator. Ranks are
     * consumed lazily and validated when reached.
     * </p>
     */
    private final class RankBasedGenerator extends AbstractGenerator<Object> {

        private final Iterable<BigInteger> ranks;

        RankBasedGenerator(Iterable<BigInteger> ranks) {
            super(Collections.emptyList());
            this.ranks = ranks;
        }

        @Override
        public Iterator<List<Object>> iterator() {
            return new Itr(ranks.iterator());
        }
    }

    private final class Itr implements Iterator<List<Object>> {

        private final Iterator<BigInteger> rankIt;

        Itr(Iterator<BigInteger> rankIt) { this.rankIt = rankIt; }

        @Override public boolean hasNext() { return rankIt.hasNext(); }

        @Override
        public List<Object> next() {
            if (!hasNext()) throw new NoSuchElementException();
            BigInteger rank = rankIt.next();

            if (rank.signum() < 0 || rank.compareTo(cachedCount) >= 0) {
                throw new IllegalArgumentException(
                        "Rank " + rank + " is out of bounds [0, " + cachedCount + ")");
            }

            Object[] result = new Object[allLists.size()];
            BigInteger remaining = rank;
            for (int i = result.length - 1; i >= 0; i--) {
                List<?> dim = allLists.get(i);
                BigInteger[] qr = remaining.divideAndRemainder(dimSizes[i]);
                remaining = qr[0];
                result[i] = dim.get(qr[1].intValue());
            }
            return Collections.unmodifiableList(Arrays.asList(result));
        }
    }
}
