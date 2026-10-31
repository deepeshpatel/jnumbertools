/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
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
 * <h2>Key optimisations vs. previous version</h2>
 * <ul>
 *   <li>No {@code SingleListBuilder} inner class — eliminates per-rank iterator
 *       allocation that the old design required to fetch one element per dimension.</li>
 *   <li>Rank decomposition uses direct {@code list.get(index)} — O(1) per dimension.</li>
 *   <li>{@code and()} copies only the one {@code allLists} ArrayList (O(dimensions),
 *       typically 2–5 elements) instead of the previous two-list copy.</li>
 *   <li>{@code count()} result is cached; recomputed only when a new dimension is added.</li>
 * </ul>
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌──────────────────────────┬───────────┬──────────────┬───────────┐
 * │ Scenario                 │ count()   │ iterator     │ isEmpty() │
 * ├──────────────────────────┼───────────┼──────────────┼───────────┤
 * │ Single empty dim         │ 1         │ [[]]         │ false     │
 * │ Multiple dims, all > 0   │ Π sizes   │ products     │ false     │
 * │ Multiple dims, any empty │ 0         │ []           │ true      │
 * └──────────────────────────┴───────────┴──────────────┴───────────┘
 * </pre>
 *
 * @author Deepesh Patel
 */
@SuppressWarnings("unchecked")
public final class SimpleProductBuilder implements Builder<Object> {

    private final List<List<?>> allLists;
    private final boolean emptyProduct;
    private final BigInteger cachedCount; // always pre-computed, never null

    // ── Public constructors / factory ────────────────────────────────────────

    /**
     * Creates a builder with a single dimension.
     *
     * @param elements first dimension; null treated as empty list
     */
    public SimpleProductBuilder(List<?> elements) {
        if (elements == null) elements = Collections.emptyList();
        this.allLists     = new ArrayList<>(List.of(elements));
        this.emptyProduct = false;          // single empty dim is NOT an empty product
        this.cachedCount  = computeCount(this.allLists, false);
    }

    /** Private copy constructor used by {@link #and(List)}. */
    private SimpleProductBuilder(List<List<?>> allLists, boolean emptyProduct) {
        this.allLists     = allLists;       // caller already made the defensive copy
        this.emptyProduct = emptyProduct;
        this.cachedCount  = computeCount(allLists, emptyProduct);
    }

    // ── Dimension chaining ───────────────────────────────────────────────────

    /**
     * Returns a <em>new</em> builder with {@code elements} appended as the next dimension.
     * This builder is not modified.
     *
     * @param elements the new dimension (must not be null)
     * @return new immutable builder
     * @throws NullPointerException if elements is null
     */
    public SimpleProductBuilder and(List<?> elements) {
        Util.validateInput(elements);
        List<List<?>> newLists = new ArrayList<>(allLists); // shallow copy — O(dimensions)
        newLists.add(elements);
        boolean newEmpty = emptyProduct || elements.isEmpty();
        return new SimpleProductBuilder(newLists, newEmpty);
    }

    // ── Builder interface ────────────────────────────────────────────────────

    @Override
    public StreamableIterable<Object> lexOrder() {
        if (emptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }
        if (isSingleEmptyDim()) {
            return new StreamableIteratorImpl<>(Util.emptyListIterator());
        }
        return new SimpleProductGenerator();
    }

    @Override
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, cachedCount);
        if (emptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }
        if (isSingleEmptyDim()) {
            return new StreamableIteratorImpl<>(Util.emptyListIterator());
        }
        return rankBased(new EveryMthIterable(start, m, cachedCount));
    }

    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        if (isSingleEmptyDim()) {
            // count=1; only rank 0 is valid — validate lazily to match rule-book
            return new StreamableIteratorImpl<>(new Iterator<>() {
                private final Iterator<BigInteger> it = ranks.iterator();
                @Override public boolean hasNext() { return it.hasNext(); }
                @Override public List<Object> next() {
                    BigInteger rank = it.next();
                    if (!rank.equals(BigInteger.ZERO))
                        throw new IllegalArgumentException(
                                "Rank " + rank + " out of bounds [0, 1)");
                    return List.of();
                }
            });
        }
        return rankBased(ranks);
    }

    @Override
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        Util.validateSampleParams(sampleSize, random, true, cachedCount);
        if (emptyProduct) return new StreamableIteratorImpl<>(Collections.emptyIterator());
        return rankBased(new BigIntegerChoice(cachedCount, sampleSize, random));
    }

    @Override
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        Util.validateSampleParams(sampleSize, random, false, cachedCount);
        if (emptyProduct) return new StreamableIteratorImpl<>(Collections.emptyIterator());
        return rankBased(new BigIntegerSample(cachedCount, sampleSize, random));
    }

    @Override
    public BigInteger count() {
        return cachedCount;
    }

    @Override
    public boolean isEmpty() {
        return emptyProduct;
    }

    @Override
    public String toString() {
        return "SimpleProductBuilder{allLists=" + allLists
                + ", count=" + cachedCount + ", isEmpty=" + emptyProduct + '}';
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private boolean isSingleEmptyDim() {
        return allLists.size() == 1 && allLists.get(0).isEmpty();
    }

    private static BigInteger computeCount(List<List<?>> lists, boolean emptyProduct) {
        if (emptyProduct) return BigInteger.ZERO;
        if (lists.size() == 1 && lists.get(0).isEmpty()) return BigInteger.ONE;
        BigInteger c = BigInteger.ONE;
        for (List<?> l : lists) c = c.multiply(BigInteger.valueOf(l.size()));
        return c;
    }

    /**
     * Rank-based iteration via direct mixed-radix decomposition on the raw lists.
     * No intermediate Builder or iterator objects are created per element — each
     * rank resolves to a tuple via one {@code list.get(index)} call per dimension.
     */
    private StreamableIterable<Object> rankBased(Iterable<BigInteger> ranks) {
        @SuppressWarnings("rawtypes")
        final List[] dims = allLists.toArray(new List[0]);
        final BigInteger[] dimSizes = new BigInteger[dims.length];
        for (int i = 0; i < dims.length; i++) {
            dimSizes[i] = BigInteger.valueOf(dims[i].size());
        }
        final BigInteger total = cachedCount;

        return new StreamableIteratorImpl<>(new Iterator<>() {
            private final Iterator<BigInteger> rankIt = ranks.iterator();

            @Override public boolean hasNext() { return rankIt.hasNext(); }

            @Override
            public List<Object> next() {
                if (!hasNext()) throw new NoSuchElementException();
                BigInteger rank = rankIt.next();

                if (rank.signum() < 0 || rank.compareTo(total) >= 0) {
                    throw new IllegalArgumentException(
                            "Rank " + rank + " is out of bounds [0, " + total + ")");
                }

                Object[] result = new Object[dims.length];
                BigInteger remaining = rank;
                for (int i = dims.length - 1; i >= 0; i--) {
                    BigInteger[] qr = remaining.divideAndRemainder(dimSizes[i]);
                    remaining = qr[0];
                    result[i] = dims[i].get(qr[1].intValue());
                }
                return Arrays.asList(result);
            }
        });
    }

    // ── lexOrder generator ───────────────────────────────────────────────────

    private class SimpleProductGenerator extends AbstractGenerator<Object> {

        SimpleProductGenerator() { super(Collections.emptyList()); }

        @Override
        public Iterator<List<Object>> iterator() {
            List<List<Object>> lists = new ArrayList<>(allLists.size());
            for (List<?> l : allLists) lists.add((List<Object>) l);
            return new SimpleProduct(lists).iterator();
        }
    }
}