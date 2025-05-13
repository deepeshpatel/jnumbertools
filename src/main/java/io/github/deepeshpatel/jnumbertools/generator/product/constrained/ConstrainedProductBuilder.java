/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.generator.product.CartesianProductByRanks;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A builder for constructing constrained products of combinations and subsets.
 * <p>
 * This class allows the creation of constrained products by combining different types of combinations and subsets.
 * You can add distinct combinations, multi-select (repetitive) combinations, or subsets (of a given range)
 * from various input lists. The final constrained product can be generated in lexicographical order
 * (via {@link #lexOrder()}), at specific intervals (via {@link #lexOrderMth(BigInteger, BigInteger)}),
 * based on a custom sequence of ranks (via {@link #byRanks(Iterable)}), or sampled randomly with
 * or without replacement (via {@link #choice(int)} and {@link #sample(int)}).
 * </p>
 * <p>
 * Implements {@link Builder} to support future composition in complex combinatorial structures,
 * such as nested products or filtered generators. Uses {@code Object} to handle mixed element types.
 * </p>
 *
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class ConstrainedProductBuilder implements Builder<Object> {

    private final List<Builder<?>> builders = new ArrayList<>();
    private final Calculator calculator;

    /**
     * Constructs a ConstrainedProductBuilder with an initial combination of size {@code n}.
     *
     * @param n          the size of the initial combinations
     * @param elements   the list of elements to create combinations from (may be null or empty)
     * @param calculator the calculator used for combinatorial computations
     */
    public ConstrainedProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && n > 0 ? new EmptyBuilder() : new Combinations(calculator).unique(n, elements));
    }

    /**
     * Adds a distinct combination of the specified quantity to the builder.
     *
     * @param quantity the size of the distinct combinations
     * @param elements the list of elements to create combinations from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder() : new Combinations(calculator).unique(quantity, elements));
        return this;
    }

    /**
     * Adds a multi-select (repetitive) combination of the specified quantity to the builder.
     *
     * @param quantity the size of the multi-select combinations
     * @param elements the list of elements to create combinations from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder() : new Combinations(calculator).repetitive(quantity, elements));
        return this;
    }

    /**
     * Adds a subset of elements within a specified size range to the builder.
     *
     * @param from     the minimum size of the subsets
     * @param to       the maximum size of the subsets
     * @param elements the list of elements to create subsets from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(new Subsets(calculator).of(elements).inRange(from, to));
        return this;
    }

    /**
     * Returns the total number of possible products.
     *
     * @return the count of products as a BigInteger
     */
    @Override
    public BigInteger count() {
        if (builders.isEmpty() || builders.stream().allMatch(b -> b.count().equals(BigInteger.ZERO))) {
            return BigInteger.ONE;
        }
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * Builds and returns a generator for all products in lexicographical order.
     *
     * @return an AbstractGenerator containing all the generated products
     */
    @Override
    public AbstractGenerator<Object> lexOrder() {
        return new ConstrainedProductGenerator();
    }

    /**
     * Builds and returns a CartesianProductByRanks for every mᵗʰ product.
     *
     * @param m     the interval to select every mᵗʰ product
     * @param start the starting position
     * @return a CartesianProductByRanks for the specified intervals
     */
    @Override
    public CartesianProductByRanks<Object> lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger maxCount = count();
        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders, new EveryMthIterable(start, m, maxCount));
    }

    /**
     * Convenience method for lexOrderMth using long values.
     *
     * @param m     the interval to select every mᵗʰ product
     * @param start the starting position
     * @return a CartesianProductByRanks for the specified intervals
     */
    public CartesianProductByRanks<Object> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates constrained Cartesian product tuples at specified rank positions, where
     * each rank corresponds to a valid tuple in the constrained product space.
     * <p>
     * The rank ordering follows the lexicographical enumeration of valid tuples
     * after applying all constraints. For example with constraints A ≠ X:
     * </p>
     * <pre>
     * Input sets: [A, B] × [X, Y]
     * Valid tuples: [A,Y], [B,X], [B,Y] (total 3)
     *
     * byRank(0) → [A, Y]
     * byRank(2) → [B, Y]
     * </pre>
     *
     * @param ranks The 0-based position in the constrained product enumeration
     * @return The tuple at the specified rank
     * @throws IllegalArgumentException if rank < 0 or rank ≥ totalValidTuples()
     * @throws IllegalStateException if no constraints were configured
     * @implNote Time complexity is O(k) where k is the number of dimensions,
     *           as this performs a mixed-radix decomposition of the rank value
     */
    public CartesianProductByRanks<Object> byRanks(Iterable<BigInteger> ranks) {
        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders, ranks);
    }

    /**
     * Generates a random sample of products with replacement.
     *
     * @param sampleSize the number of products to generate
     * @return a CartesianProductByRanks for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative
     */
    public CartesianProductByRanks<Object> choice(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger maxCount = count();
        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders, new BigIntegerChoice(maxCount, sampleSize));
    }

    /**
     * Generates a random sample of unique products.
     *
     * @param sampleSize the number of unique products to generate
     * @return a CartesianProductByRanks for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative or exceeds total products
     */
    public CartesianProductByRanks<Object> sample(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger maxCount = count();
        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders, new BigIntegerSample(maxCount, sampleSize));
    }

    /**
     * A generator for constrained products, extending AbstractGenerator.
     */
    private class ConstrainedProductGenerator extends AbstractGenerator<Object> {
        ConstrainedProductGenerator() {
            super(Collections.emptyList());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<List<Object>> iterator() {
            List<List<List<Object>>> all = new ArrayList<>();
            for (var e : builders) {
                List<List<Object>> generated = (List<List<Object>>) (List) e.lexOrder().stream().toList();
                all.add(generated);
            }
            return new ConstrainedProduct(all).iterator();
        }
    }

    /**
     * A special builder for empty inputs, producing one empty combination.
     */
    private static class EmptyBuilder implements Builder<Object> {
        @Override
        public BigInteger count() {
            return BigInteger.ONE;
        }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<Object>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    return Collections.singletonList(Collections.emptyList()).iterator();
                }
            };
        }

        @Override
        public Iterable<List<Object>> lexOrderMth(BigInteger m, BigInteger start) {
            return m.equals(BigInteger.ZERO) ? Collections.singletonList(Collections.emptyList()) : Collections.emptyList();
        }

        @Override
        public Iterable<List<Object>> byRanks(Iterable<BigInteger> ranks) {
            throw new UnsupportedOperationException("EmptyBuilder does not support rank-based generation");
        }
    }
}