/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.StreamableIterable;
import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Generates Cartesian products based on a sequence of rank positions.
 *
 * <p>This class is the <em>generic</em> rank-based backend used by both
 * {@link SimpleProductBuilder} and {@link ConstrainedProductBuilder} when their
 * dimension lists are built from arbitrary {@link Builder} instances (e.g. from the
 * public API surface that accepts a {@code List<Builder>} directly).
 *
 * <p><strong>Note:</strong> {@link SimpleProductBuilder} and
 * {@link ConstrainedProductBuilder} now perform rank decomposition internally
 * (with direct list access), so this class is mainly needed for external / API callers
 * that compose their own {@link Builder} dimensions. The {@code getMth()} implementation
 * here still uses {@code lexOrderMth(ONE, index)} per dimension as before, which is
 * correct for the generic case.  If all your dimensions are simple lists, prefer the
 * builder's own {@code byRanks()} method for better performance.
 *
 * <h2>Rank Ordering</h2>
 * Ranks follow row-major order (rightmost dimension varies fastest).
 *
 * @param <T> the type of elements in the products
 * @author Deepesh Patel
 */
public class CartesianProductByRanks<T> implements StreamableIterable<T> {

    private final Iterable<BigInteger> ranks;
    private final List<Builder<T>> builders;
    private final BigInteger maxCount;
    private final boolean isEmptyProduct;
    private final BigInteger[] counts;      // per-dimension counts, taken once (builders are immutable)
    private final long[] longCounts;        // the same as longs when the whole product fits in 62 bits, else null

    public CartesianProductByRanks(List<Builder<T>> builders, Iterable<BigInteger> ranks) {
        this.builders = builders != null ? builders : Collections.emptyList();
        this.ranks    = ranks;
        this.isEmptyProduct = this.builders.stream().anyMatch(Builder::isEmpty);
        this.maxCount = computeMaxCount();
        if (isEmptyProduct || this.builders.isEmpty()) {
            this.counts = new BigInteger[0];
            this.longCounts = null;
        } else {
            this.counts = this.builders.stream().map(Builder::count).toArray(BigInteger[]::new);
            if (maxCount.bitLength() <= 62) {
                this.longCounts = new long[counts.length];
                for (int i = 0; i < counts.length; i++) longCounts[i] = counts[i].longValue();
            } else {
                this.longCounts = null;
            }
        }
    }

    private BigInteger computeMaxCount() {
        if (builders.isEmpty()) return BigInteger.ONE;
        if (isEmptyProduct)     return BigInteger.ZERO;
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    @Override
    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    public BigInteger count()  { return maxCount; }
    public boolean    isEmpty(){ return isEmptyProduct; }

    @Override
    public String toString() {
        return "CartesianProductByRanks{maxCount=" + maxCount
                + ", isEmpty=" + isEmptyProduct
                + ", dimensions=" + builders.size() + "}";
    }

    // ── Rank resolution ──────────────────────────────────────────────────────

    // ── Iterator ─────────────────────────────────────────────────────────────

    /** A rank source whose current value is set by the caller; it never runs out. */
    private static final class RankSlot implements Iterable<BigInteger>, Iterator<BigInteger> {
        BigInteger value;

        @Override public Iterator<BigInteger> iterator() { return this; }
        @Override public boolean hasNext() { return true; }
        @Override public BigInteger next() { return value; }
    }

    private class Itr implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIt = ranks.iterator();

        // One long-lived by-rank generator per dimension, fed through a settable slot. Fetching the element at
        // one position of a dimension then costs a single unrank, instead of building a generator, an iterator
        // and its validation for every position of every result.
        private RankSlot[] slots;
        private Iterator<List<T>>[] dimensions;

        @Override public boolean hasNext() { return rankIt.hasNext(); }

        @Override
        public List<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            return getMth(rankIt.next());
        }

        @SuppressWarnings("unchecked")
        private void openDimensions() {
            slots = new RankSlot[builders.size()];
            dimensions = (Iterator<List<T>>[]) new Iterator[builders.size()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new RankSlot();
                // Builder.byRanks(...) is required by the Builder interface, so no fallback path is
                // needed: a builder without rank support would not compile. If a future Builder ever
                // does need to reject rank access, letting UnsupportedOperationException propagate is
                // the correct behaviour — silently falling back to lexOrderMth(ONE, position) would
                // be O(rank) per element.
                dimensions[i] = builders.get(i).byRanks(slots[i]).iterator();
            }
        }

        private List<T> fetch(int dimension, BigInteger position) {
            // RankSlot.hasNext() always returns true, so the iterator is guaranteed to yield one element
            // per slot value; no hasNext() check is needed.
            slots[dimension].value = position;
            return dimensions[dimension].next();
        }

        private List<T> getMth(BigInteger rank) {
            if (isEmptyProduct) {
                throw new IllegalArgumentException("Product is empty - no ranks available");
            }
            if (builders.isEmpty()) {
                if (rank.equals(BigInteger.ZERO)) return Collections.emptyList();
                throw new IllegalArgumentException(
                        "Rank " + rank + " out of bounds for empty product [0, 1)");
            }
            if (rank.signum() < 0 || rank.compareTo(maxCount) >= 0) {
                throw new IllegalArgumentException(
                        "Rank " + rank + " out of bounds [0, " + maxCount + ")");
            }
            if (dimensions == null) openDimensions();

            // Mixed-radix decomposition: iterate dimensions right-to-left.
            int n = builders.size();
            @SuppressWarnings("unchecked")
            List<T>[] parts = (List<T>[]) new List[n];
            int total = 0;
            if (longCounts != null) {
                long remaining = rank.longValue();
                for (int i = n - 1; i >= 0; i--) {
                    long quotient = remaining / longCounts[i];
                    parts[i] = fetch(i, BigInteger.valueOf(remaining - quotient * longCounts[i]));
                    remaining = quotient;
                    total += parts[i].size();
                }
            } else {
                BigInteger remaining = rank;
                for (int i = n - 1; i >= 0; i--) {
                    BigInteger[] qr = remaining.divideAndRemainder(counts[i]);
                    remaining = qr[0];
                    parts[i] = fetch(i, qr[1]);
                    total += parts[i].size();
                }
            }

            // One array for the whole tuple, wrapped without a further copy.
            Object[] output = new Object[total];
            int position = 0;
            for (List<T> part : parts) {
                for (int j = 0, size = part.size(); j < size; j++) output[position++] = part.get(j);
            }
            return Tuple.wrap(output);
        }
    }
}
