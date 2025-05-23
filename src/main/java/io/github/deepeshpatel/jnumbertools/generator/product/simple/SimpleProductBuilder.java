/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
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
 * A builder for constructing simple Cartesian products of input lists.
 * <p>
 * This class allows the creation of Cartesian products by combining elements from multiple input lists.
 * Each input list represents a set of elements, and the Cartesian product is generated by selecting
 * one element from each list. The final product can be generated in lexicographical order
 * (via {@link #lexOrder()}), at specific intervals (via {@link #lexOrderMth(BigInteger, BigInteger)}),
 * based on a custom sequence of ranks (via {@link #byRanks(Iterable)}), or sampled randomly with
 * or without replacement (via {@link #choice(int)} and {@link #sample(int)}).
 * </p>
 * <p>
 * Implements {@link Builder} to support composition in complex combinatorial structures,
 * such as nested products or filtered generators. Uses {@code Object} to handle mixed element types.
 * </p>
 *
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class SimpleProductBuilder implements Builder<Object> {

    private final List<Builder<Object>> builders = new ArrayList<>();
    private final List<List<?>> allLists = new ArrayList<>();
    private final Calculator calculator;

    /**
     * Constructs a SimpleProductBuilder with an initial list of elements.
     *
     * @param elements   the initial list of elements (may be null or empty)
     * @param calculator the calculator used for combinatorial computations
     */
    public SimpleProductBuilder(List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        elements = elements == null ? Collections.emptyList() : elements;
        allLists.add(elements);
        builders.add(new SingleListBuilder(elements));
    }

    /**
     * Adds a new list of elements to the product.
     *
     * @param elements the list of elements to add (may be null or empty)
     * @return the current instance of SimpleProductBuilder for method chaining
     */
    public SimpleProductBuilder and(List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        allLists.add(elements);
        builders.add(new SingleListBuilder(elements));
        return this;
    }

    /**
     * Adds a new list of elements to the product from a varargs array.
     *
     * @param elements the elements to add
     * @return the current instance of SimpleProductBuilder for method chaining
     */
    public SimpleProductBuilder and(Object... elements) {
        return and(List.of(elements));
    }

    /**
     * Returns the total number of possible products.
     *
     * @return the count of products as a BigInteger
     */
    @Override
    public BigInteger count() {
        if (allLists.isEmpty() || allLists.stream().allMatch(List::isEmpty)) {
            return BigInteger.ONE;
        }
        return allLists.stream()
                .map(list -> BigInteger.valueOf(list.size()))
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * Builds and returns a generator for all products in lexicographical order.
     *
     * @return an AbstractGenerator containing all the generated products
     */
    @Override
    public AbstractGenerator<Object> lexOrder() {
        return new SimpleProductGenerator();
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
        return new CartesianProductByRanks(builders, new EveryMthIterable(start, m, maxCount));
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
     * Generates Cartesian product tuples at specified rank positions.
     * <p>
     * Ranks follow row-major order (rightmost indices vary fastest).
     * </p>
     * <p>
     * <b>Example for [A,B] × [1,2] × [X,Y]:</b>
     * <pre>
     * Rank | Tuple   | Mixed-Radix Digits
     * -----|---------|--------------------
     * 0    | [A,1,X] | [0,0,0]
     * 1    | [A,1,Y] | [0,0,1]
     * 2    | [A,2,X] | [0,1,0]
     * 3    | [A,2,Y] | [0,1,1]
     * 4    | [B,1,X] | [1,0,0]
     * 5    | [B,1,Y] | [1,0,1]
     * 6    | [B,2,X] | [1,1,0]
     * 7    | [B,2,Y] | [1,1,1]
     *
     * byRanks([0, 7]) → [A,1,X], [B,2,Y]
     * </pre>
     *
     * @param ranks Iterable of 0-based rank numbers (0 ≤ rank < product of set sizes)
     * @return Product generator for specified ranks
     * @throws IllegalArgumentException if any rank exceeds product space
     * @throws IllegalStateException if no sets were added
     */
    public CartesianProductByRanks<Object> byRanks(Iterable<BigInteger> ranks) {
        return new CartesianProductByRanks(builders, ranks);
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
        return new CartesianProductByRanks(builders, new BigIntegerChoice(maxCount, sampleSize));
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
        return new CartesianProductByRanks(builders, new BigIntegerSample(maxCount, sampleSize));
    }

    /**
     * A generator for simple products, extending AbstractGenerator.
     */
    private class SimpleProductGenerator extends AbstractGenerator<Object> {
        SimpleProductGenerator() {
            super(Collections.emptyList());
        }

        @Override
        public Iterator<List<Object>> iterator() {
            List<List<Object>> lists = new ArrayList<>();
            for (List<?> list : allLists) {
                lists.add((List<Object>) list);
            }
            return new SimpleProduct(lists).iterator();
        }
    }

    /**
     * A builder that wraps a single list as a set of single-element combinations.
     */
    private static class SingleListBuilder implements Builder<Object> {
        private final List<?> elements;

        SingleListBuilder(List<?> elements) {
            this.elements = elements;
        }

        @Override
        public BigInteger count() {
            return BigInteger.valueOf(elements.size());
        }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<Object>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    List<List<Object>> result = new ArrayList<>();
                    for (Object e : elements) {
                        result.add(List.of(e));
                    }
                    return result.iterator();
                }
            };
        }

        @Override
        public Iterable<List<Object>> lexOrderMth(BigInteger m, BigInteger start) {
            return () -> new Iterator<List<Object>>() {
                private final Iterator<BigInteger> indexIterator =
                        new EveryMthIterable(start, m, BigInteger.valueOf(elements.size())).iterator();

                @Override
                public boolean hasNext() {
                    return indexIterator.hasNext();
                }

                @Override
                public List<Object> next() {
                    BigInteger index = indexIterator.next();
                    return List.of(elements.get(index.intValue()));
                }
            };
        }

        @Override
        public Iterable<List<Object>> byRanks(Iterable<BigInteger> ranks) {
            return () -> new Iterator<List<Object>>() {
                private final Iterator<BigInteger> rankIterator = ranks.iterator();

                @Override
                public boolean hasNext() {
                    return rankIterator.hasNext();
                }

                @Override
                public List<Object> next() {
                    BigInteger index = rankIterator.next();
                    return List.of(elements.get(index.intValue()));
                }
            };
        }
    }
}