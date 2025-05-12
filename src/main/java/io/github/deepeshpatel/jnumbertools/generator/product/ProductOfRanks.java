/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Generates a sequence of Cartesian products (simple or constrained) based on a provided sequence of ranks.
 * <p>
 * This class generates products of size determined by the input builders, where each product is selected
 * based on a rank from the provided {@code Iterable<BigInteger>}. It supports both simple and constrained
 * Cartesian products, replacing {@code SimpleProductMth} and {@code ConstrainedProductMth}. The products
 * are computed efficiently using combinadic unranking, suitable for random sampling, choice, or mᵗʰ sequences.
 * </p>
 *
 * @param <T> the type of elements in the products
 * @since 3.0.1
 * @author Deepesh Patel
 */
public class ProductOfRanks<T> implements Iterable<List<T>> {

    private final Iterable<BigInteger> ranks;
    private final List<Builder<T>> builders;
    private final BigInteger maxCount;

    /**
     * Constructs a new ProductOfRanks instance.
     *
     * @param builders the list of builders generating combinations or subsets for each dimension
     * @param ranks    the iterable providing the sequence of ranks
     */
    public ProductOfRanks(List<Builder<T>> builders, Iterable<BigInteger> ranks) {
        this.builders = builders;
        this.ranks = ranks;
        this.maxCount = maxCount();
    }

    /**
     * Returns a stream of products based on the provided rank sequence.
     *
     * @return a stream of product lists
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Computes the total number of possible products.
     *
     * @return the total number of products as a BigInteger
     */
    private BigInteger maxCount() {
        if (builders.isEmpty()) {
            return BigInteger.ONE; // Empty input produces one empty product
        }
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * Generates the product at the specified rank.
     *
     * @param m the rank of the product to generate
     * @return the product as a list of elements
     * @throws IllegalArgumentException if m is invalid
     */
    private List<T> getMth(BigInteger m) {
        if (builders.isEmpty()) {
            if (m.equals(BigInteger.ZERO)) {
                return Collections.emptyList();
            }
            throw new IllegalArgumentException("Rank " + m + " is out of bounds [0, 1)");
        }
        if (m.compareTo(BigInteger.ZERO) < 0 || m.compareTo(maxCount) >= 0) {
            throw new IllegalArgumentException("Rank " + m + " is out of bounds [0, " + maxCount + ")");
        }
        Deque<T> output = new ArrayDeque<>();
        BigInteger remaining =m;

        for (int i = builders.size() - 1; i >= 0; i--) {
            Builder<T> e = builders.get(i);
            BigInteger[] division = remaining.divideAndRemainder(e.count());
            remaining = division[0];

            List<T> values = e.lexOrderMth(division[1], division[1]).iterator().next();

            // Add elements to the front in reverse order
            for (int j = values.size() - 1; j >= 0; j--) {
                output.addFirst(values.get(j));
            }
        }
        return List.copyOf(output);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    @Override
    public String toString() {
        return String.format("ProductOfRanks{maxCount=%s, buildersCount=%d}", maxCount, builders.size());
    }

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
            BigInteger m = rankIterator.next();
            return getMth(m);
        }
    }
}