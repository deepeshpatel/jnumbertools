/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product;

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

    public CartesianProductByRanks(List<Builder<T>> builders, Iterable<BigInteger> ranks) {
        this.builders = builders != null ? builders : Collections.emptyList();
        this.ranks    = ranks;
        this.isEmptyProduct = this.builders.stream().anyMatch(Builder::isEmpty);
        this.maxCount = computeMaxCount();
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

        // Mixed-radix decomposition: iterate dimensions right-to-left.
        Deque<T> output = new ArrayDeque<>();
        BigInteger remaining = rank;
        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> b = builders.get(i);
            BigInteger dimCount = b.count();
            BigInteger[] qr = remaining.divideAndRemainder(dimCount);
            remaining = qr[0];

            // Retrieve the single element at position qr[1] in this dimension.
            // lexOrderMth(ONE, index) is the generic way when we only have a Builder.
            List<T> values = b.lexOrderMth(BigInteger.ONE, qr[1]).iterator().next();
            for (int j = values.size() - 1; j >= 0; j--) {
                output.addFirst(values.get(j));
            }
        }
        return List.copyOf(output);
    }

    // ── Iterator ─────────────────────────────────────────────────────────────

    private class Itr implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIt = ranks.iterator();

        @Override public boolean hasNext() { return rankIt.hasNext(); }

        @Override
        public List<T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            return getMth(rankIt.next());
        }
    }
}