/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.product.ProductForSequence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Builder for creating instances of {@link SimpleProduct} and {@link ProductForSequence}.
 * <p>
 * This builder allows you to add multiple lists of elements and then generate the Cartesian product.
 * You can either generate the complete product in lexicographical order using {@link #lexOrder()},
 * generate every mᵗʰ product starting from a given index using {@link #lexOrderMth(long, long)},
 * or generate products based on a custom sequence of ranks using {@link #fromSequence(Iterable)}.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
@SuppressWarnings({"rawtypes"})
public final class SimpleProductBuilder {

    private final List<List> allLists = new ArrayList<>();
    private final Calculator calculator;

    /**
     * Initializes the builder with an initial list of elements and a calculator.
     *
     * @param elements   the initial list of elements (may be null or empty)
     * @param calculator the calculator for combinatorial computations
     */
    public SimpleProductBuilder(List elements, Calculator calculator) {
        this.calculator = calculator;
        allLists.add(elements == null ? Collections.emptyList() : elements);
    }

    /**
     * Adds another list of elements to the builder.
     *
     * @param elements the list of elements to add (may be null or empty)
     * @return this builder instance for chaining
     */
    public SimpleProductBuilder and(List elements) {
        allLists.add(elements == null ? Collections.emptyList() : elements);
        return this;
    }

    /**
     * Adds a varargs list of elements to the builder.
     *
     * @param elements the elements to add
     * @return this builder instance for chaining
     */
    public SimpleProductBuilder and(Object... elements) {
        return and(elements == null ? Collections.emptyList() : List.of(elements));
    }

    /**
     * Creates a {@link SimpleProduct} instance with the lists of elements added to the builder.
     * <p>
     * The resulting product contains all combinations in lexicographical order.
     * </p>
     *
     * @return a new {@link SimpleProduct} instance
     */
    public SimpleProduct lexOrder() {
        return new SimpleProduct(allLists);
    }

    /**
     * Creates a {@link ProductForSequence} instance with the lists of elements added to the builder.
     * <p>
     * The generated product will start from the specified starting index and produce every mᵗʰ product in lexicographical order.
     * </p>
     *
     * @param m     the mᵗʰ product to start from (0-based)
     * @param start the starting index for generating the product
     * @return a new {@link ProductForSequence} instance
     */
    public ProductForSequence lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a {@link ProductForSequence} instance with the lists of elements added to the builder.
     * <p>
     * The generated product will start from the specified starting index and produce every mᵗʰ product in lexicographical order.
     * </p>
     *
     * @param m     the mᵗʰ product to start from (0-based) as a BigInteger
     * @param start the starting index for generating the product as a BigInteger
     * @return a new {@link ProductForSequence} instance
     */
    public ProductForSequence lexOrderMth(BigInteger m, BigInteger start) {
        List<Builder> builders = new ArrayList<>();
        boolean allEmpty = allLists.isEmpty() || allLists.stream().allMatch(List::isEmpty);
        if (allEmpty) {
            builders.add(new EmptyBuilder());
        } else {
            for (List list : allLists) {
                builders.add(list.isEmpty() ? new EmptyBuilder() : new Combinations(calculator).repetitive(1, list));
            }
        }
        BigInteger maxCount = allEmpty ? BigInteger.ONE : BigInteger.ONE;
        if (!allEmpty) {
            for (List list : allLists) {
                maxCount = maxCount.multiply(BigInteger.valueOf(list.size() > 0 ? list.size() : 1));
            }
        }
        return new ProductForSequence(builders, new EveryMthIterable(start, m, maxCount));
    }

    /**
     * Creates a {@link ProductForSequence} instance with the lists of elements added to the builder.
     * <p>
     * The generated product will include products corresponding to the specified sequence of ranks.
     * </p>
     *
     * @param ranks the iterable providing the sequence of ranks
     * @return a new {@link ProductForSequence} instance
     */
    public ProductForSequence fromSequence(Iterable<BigInteger> ranks) {
        List<Builder> builders = new ArrayList<>();
        boolean allEmpty = allLists.isEmpty() || allLists.stream().allMatch(List::isEmpty);
        if (allEmpty) {
            builders.add(new EmptyBuilder());
        } else {
            for (List list : allLists) {
                builders.add(list.isEmpty() ? new EmptyBuilder() : new Combinations(calculator).repetitive(1, list));
            }
        }
        return new ProductForSequence(builders, ranks);
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
                    return (Iterator<List<Object>>) Collections.emptyList();
                }
            };
            //return Collections.singletonList(Collections.emptyList());
        }

        @Override
        public Iterable<List<Object>> lexOrderMth(BigInteger m, BigInteger start) {
            return m.equals(BigInteger.ZERO) ? Collections.singletonList(Collections.emptyList()) : Collections.emptyList();
        }
    }
}