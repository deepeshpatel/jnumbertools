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
 * <p>
 * This class takes a list of builders (each representing a dimension) and an iterable of ranks,
 * and generates the corresponding tuples at those ranks. It supports both simple and constrained
 * Cartesian products.
 * </p>
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌────────────────────────────┬─────────────────────────────┐
 * │ Condition                  │ Behavior                    │
 * ├────────────────────────────┼─────────────────────────────┤
 * │ No builders                │ Only rank 0 is valid,       │
 * │                            │ returns [[]]                │
 * ├────────────────────────────┼─────────────────────────────┤
 * │ Any builder is empty       │ Product is empty,           │
 * │ (builder.isEmpty() true)   │ all rank access throws      │
 * ├────────────────────────────┼─────────────────────────────┤
 * │ All builders non-empty     │ Normal rank-based access    │
 * │                            │ with range [0, maxCount)    │
 * └────────────────────────────┴─────────────────────────────┘
 * </pre>
 *
 * <h2>Rank Ordering</h2>
 * <p>
 * Ranks follow row-major order (rightmost dimension varies fastest).
 * For dimensions with sizes [s₁, s₂, ..., sₖ], rank r decomposes as:
 * <pre>
 * r = d₁ × (s₂×s₃×...×sₖ) + d₂ × (s₃×...×sₖ) + ... + dₖ
 * where 0 ≤ dᵢ < sᵢ
 * </pre>
 * </p>
 *
 * <h2>Usage Examples</h2>
 * <pre>
 * // Create builders for dimensions
 * List<Builder<String>> builders = List.of(
 *     new SingleListBuilder(List.of("A", "B")),
 *     new SingleListBuilder(List.of("X", "Y"))
 * );
 *
 * // Generate tuples at ranks 0, 2, and 3
 * var ranks = List.of(BigInteger.ZERO, BigInteger.valueOf(2), BigInteger.valueOf(3));
 * var product = new CartesianProductByRanks<>(builders, ranks);
 *
 * product.stream().forEach(System.out::println);
 * // Output: [A,X], [B,X], [B,Y]
 * </pre>
 *
 * @param <T> the type of elements in the products
 * @author Deepesh Patel
 * @see SimpleProductBuilder
 * @see ConstrainedProductBuilder
 */
public class CartesianProductByRanks<T> implements StreamableIterable<T> {

    private final Iterable<BigInteger> ranks;
    private final List<Builder<T>> builders;
    private final BigInteger maxCount;
    private final boolean isEmptyProduct;

    /**
     * Constructs a new CartesianProductByRanks instance.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder#byRanks(Iterable)}
     * or {@link io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder#byRanks(Iterable)}.
     * All parameter validation (null checks) is handled by the builder.
     * Rank validation (non-negative, less than total) is deferred to iteration.
     * </p>
     *
     * @param builders the list of builders for each dimension (assumed non-null)
     * @param ranks    the iterable providing the sequence of ranks
     */
    public CartesianProductByRanks(List<Builder<T>> builders, Iterable<BigInteger> ranks) {
        this.builders = builders != null ? builders : Collections.emptyList();
        this.ranks = ranks;

        // Compute product state
        isEmptyProduct = this.builders.stream().anyMatch(Builder::isEmpty);
        this.maxCount = computeMaxCount();
    }

    /**
     * Computes the total number of possible products.
     *
     * @return the total number of products as a BigInteger
     */
    private BigInteger computeMaxCount() {
        // Case 1: No builders → one empty tuple
        if (builders.isEmpty()) {
            return BigInteger.ONE;
        }

        // Case 2: Any builder is empty → product is empty
        if (isEmptyProduct) {
            return BigInteger.ZERO;
        }

        // Case 3: Normal case - multiply all builder counts
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * Returns a stream of products based on the provided rank sequence.
     *
     * @return a stream of product lists
     */
    @Override
    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Generates the product at the specified rank.
     *
     * @param rank the rank of the product to generate (0 ≤ rank < maxCount when count > 0)
     * @return the product as a list of elements
     * @throws IllegalArgumentException if the product is empty, no builders exist with invalid rank,
     *         or rank is out of bounds
     */
    private List<T> getMth(BigInteger rank) {
        // Case 1: Empty product - always throw
        if (isEmptyProduct) {
            throw new IllegalArgumentException(
                    "Product is empty - no ranks available"
            );
        }

        // Case 2: No builders - only rank 0 is valid
        if (builders.isEmpty()) {
            if (rank.equals(BigInteger.ZERO)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException(
                    "Rank " + rank + " is out of bounds for empty product [0, 1)"
            );
        }

        // Case 3: Validate rank range
        if (rank.signum() < 0 || rank.compareTo(maxCount) >= 0) {
            throw new IllegalArgumentException(
                    "Rank " + rank + " is out of bounds [0, " + maxCount + ")"
            );
        }

        // Case 4: Normal rank decomposition
        Deque<T> output = new ArrayDeque<>();
        BigInteger remaining = rank;

        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> builder = builders.get(i);
            BigInteger dimensionCount = builder.count();

            // Decompose rank using mixed-radix system
            BigInteger[] division = remaining.divideAndRemainder(dimensionCount);
            remaining = division[0];

            // Get the element(s) at this dimension's index
            List<T> values = builder.lexOrderMth(BigInteger.ONE, division[1])
                    .iterator()
                    .next();

            // Add elements to the front (maintaining original order)
            for (int j = values.size() - 1; j >= 0; j--) {
                output.addFirst(values.get(j));
            }
        }

        return List.copyOf(output);
    }

    /**
     * Returns an iterator over the products at specified ranks.
     *
     * @return an iterator over product lists
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    @Override
    public String toString() {
        return String.format(
                "CartesianProductByRanks{maxCount=%s, isEmpty=%s, dimensions=%d}",
                maxCount, isEmptyProduct, builders.size()
        );
    }

    /**
     * Iterator implementation for rank-based product generation.
     */
    private class Itr implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

        Itr() {
            this.rankIterator = ranks.iterator();
        }

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more ranks available");
            }

            BigInteger rank = rankIterator.next();
            return getMth(rank);
        }
    }

    /**
     * Returns the total number of possible products.
     *
     * @return the total count as a BigInteger
     */
    public BigInteger count() {
        return maxCount;
    }

    /**
     * Indicates whether this product is empty (produces no tuples).
     *
     * @return {@code true} if the product is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return isEmptyProduct;
    }
}