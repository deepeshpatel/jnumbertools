/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.core.external.Calculator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.*;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.unique.UniqueCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.product.CartesianProductByRanks;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.product.simple.SimpleProductBuilder;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.subset.SubsetBuilder;

import java.math.BigInteger;
import java.util.*;

/**
 * Builder for generating constrained Cartesian products where each dimension can have
 * complex constraints like distinct combinations, multi-select (repetitive) combinations,
 * or subsets within a size range.
 *
 * <h2>Mathematical Rules for Empty Dimensions</h2>
 * <pre>
 * ┌──────────────────────────────────────┬───────────┬──────────────┬───────────┐
 * │ Scenario                             │ count()   │ iterator     │ isEmpty() │
 * ├──────────────────────────────────────┼───────────┼──────────────┼───────────┤
 * │ No dimensions                        │ 1         │ [[]]         │ false     │
 * │ Single dimension with count=0        │ 0         │ []           │ true      │
 * │ Multiple dims, all count>0           │ Π counts  │ products     │ false     │
 * │ Multiple dims, any count=0           │ 0         │ []           │ true      │
 * └──────────────────────────────────────┴───────────┴──────────────┴───────────┘
 * </pre>
 *
 * <p>
 * <strong>Optimization:</strong> Once any dimension becomes empty (count=0),
 * the entire product is known to be empty. Further dimensions are ignored
 * and an empty builder is returned immediately.
 * </p>
 *
 * @see SimpleProductBuilder
 * @see CartesianProductByRanks
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class ConstrainedProductBuilder implements Builder<Object> {

    private final List<Builder<?>> builders;
    private final Calculator calculator;
    private final boolean isEmptyProduct;  // Optimization flag

    /**
     * Constructs a ConstrainedProductBuilder with an initial multi-select combination.
     */
    public ConstrainedProductBuilder(int n, List<?> elements, boolean isMultiSelect, Calculator calculator) {
        this.calculator = calculator;
        this.builders = new ArrayList<>();

        Builder<?> firstBuilder;
        if (elements.isEmpty()) {
            firstBuilder = new EmptyBuilder(n);
        } else {
            firstBuilder = isMultiSelect
                    ? new RepetitiveCombinationBuilder<>(elements, n, calculator)
                    : new UniqueCombinationBuilder<>(elements, n, calculator);
        }

        this.builders.add(firstBuilder);
        this.isEmptyProduct = firstBuilder.isEmpty();
    }

    /**
     * Constructs a ConstrainedProductBuilder with an initial subset range.
     */
    public ConstrainedProductBuilder(int from, int to, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        this.builders = new ArrayList<>();
        Builder<?> firstBuilder = new SubsetBuilder<>(elements, calculator).inRange(from, to);
        this.builders.add(firstBuilder);
        this.isEmptyProduct = firstBuilder.isEmpty();
    }

    /**
     * Private constructor for creating new instances with existing data.
     */
    private ConstrainedProductBuilder(List<Builder<?>> builders, Calculator calculator, boolean isEmptyProduct) {
        this.builders = builders;
        this.calculator = calculator;
        this.isEmptyProduct = isEmptyProduct;
    }

    /**
     * Adds a distinct combination dimension.
     * <p>
     * <strong>Optimization:</strong> If the product is already empty,
     * returns an empty builder immediately without processing.
     * </p>
     * @throws NullPointerException if elements is null
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        Util.validateInput(elements);

        // Optimization: if already empty, return empty builder
        if (isEmptyProduct) {
            return new ConstrainedProductBuilder(builders, calculator, true);
        }

        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        Builder<?> newBuilder;
        if (elements.isEmpty()) {
            newBuilder = new EmptyBuilder(quantity);
        } else {
            newBuilder = new UniqueCombinationBuilder<>(elements, quantity, calculator);
        }
        newBuilders.add(newBuilder);

        // Product becomes empty only if the new builder is empty
        return new ConstrainedProductBuilder(newBuilders, calculator, newBuilder.isEmpty());
    }

    /**
     * Adds a multi-select (repetitive) combination dimension.
     * <p>
     * <strong>Optimization:</strong> If the product is already empty,
     * returns an empty builder immediately without processing.
     * </p>
     * @throws NullPointerException if elements is null
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        Util.validateInput(elements);
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0: " + quantity);
        }

        // Optimization: if already empty, return empty builder
        if (isEmptyProduct) {
            return new ConstrainedProductBuilder(builders, calculator, true);
        }

        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        Builder<?> newBuilder;
        if (elements.isEmpty()) {
            newBuilder = new EmptyBuilder(quantity);
        } else {
            newBuilder = new RepetitiveCombinationBuilder<>(elements, quantity, calculator);
        }
        newBuilders.add(newBuilder);
        return new ConstrainedProductBuilder(newBuilders, calculator, newBuilder.isEmpty());
    }

    /**
     * Adds a subset range dimension.
     * <p>
     * <strong>Optimization:</strong> If the product is already empty,
     * returns an empty builder immediately without processing.
     * </p>
     * @throws NullPointerException if elements is null
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        Util.validateInput(elements);

        // Optimization: if already empty, return empty builder
        if (isEmptyProduct) {
            return new ConstrainedProductBuilder(builders, calculator, true);
        }

        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        Builder<?> newBuilder = new SubsetBuilder<>(elements, calculator).inRange(from, to);

        newBuilders.add(newBuilder);

        // Product becomes empty only if the new builder is empty
        return new ConstrainedProductBuilder(newBuilders, calculator, newBuilder.isEmpty());
    }

    @Override
    public StreamableIterable<Object> lexOrder() {
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }
        return new ConstrainedProductGenerator();
    }

    @Override
    public CartesianProductByRanks<Object> lexOrderMth(BigInteger m, BigInteger start) {
        validateLexOrderMthParams(m, start, count());

        if (isEmptyProduct) {
            return new CartesianProductByRanks<>(
                    (List<Builder<Object>>) (List) builders,
                    Collections.emptyList()
            );
        }

        BigInteger maxCount = count();
        return new CartesianProductByRanks<>(
                (List<Builder<Object>>) (List) builders,
                new EveryMthIterable(start, m, maxCount)
        );
    }

    @Override
    public CartesianProductByRanks<Object> byRanks(Iterable<BigInteger> ranks) {
        validateByRanksParams(ranks);
        return new CartesianProductByRanks<>((List<Builder<Object>>) (List<?>) builders, ranks);
    }

    @Override
    public BigInteger count() {
        if (isEmptyProduct) return BigInteger.ZERO;
        if (builders.isEmpty()) return BigInteger.ONE;

        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    @Override
    public boolean isEmpty() {
        return isEmptyProduct;
    }

    /**
     * Generates a random sample of products with replacement using custom random generator.
     *
     * @param sampleSize the number of products to generate
     * @param random the random generator to use
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative or random is null
     */
    @Override
    public CartesianProductByRanks<Object> choice(int sampleSize, Random random) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }

        if (isEmptyProduct) {
            return new CartesianProductByRanks<>(
                    (List<Builder<Object>>) (List) builders,
                    Collections.emptyList()
            );
        }

        BigInteger maxCount = count();
        return new CartesianProductByRanks<>(
                (List<Builder<Object>>) (List) builders,
                new BigIntegerChoice(maxCount, sampleSize, random)
        );
    }

    /**
     * Generates a random sample of unique products without replacement using custom random generator.
     *
     * @param sampleSize the number of unique products to generate
     * @param random the random generator to use
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative, exceeds total products, or random is null
     */
    @Override
    public CartesianProductByRanks<Object> sample(int sampleSize, Random random) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }

        BigInteger maxCount = count();
        if (BigInteger.valueOf(sampleSize).compareTo(maxCount) > 0) {
            throw new IllegalArgumentException(
                    "Sample size cannot exceed total products: " + maxCount
            );
        }

        if (isEmptyProduct) {
            return new CartesianProductByRanks<>(
                    (List<Builder<Object>>) (List) builders,
                    Collections.emptyList()
            );
        }

        return new CartesianProductByRanks<>(
                (List<Builder<Object>>) (List) builders,
                new BigIntegerSample(maxCount, sampleSize, random)
        );
    }

    /**
     * A generator for constrained products.
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
     * Empty builder for dimensions with no valid combinations.
     */
    private static class EmptyBuilder implements Builder<Object> {
        private final BigInteger count;
        private final boolean isEmpty;

        EmptyBuilder(int quantity) {
            this.count = quantity > 0 ? BigInteger.ZERO : BigInteger.ONE;
            this.isEmpty = quantity > 0;
        }

        @Override
        public BigInteger count() {
            return count;
        }

        @Override
        public boolean isEmpty() {
            return isEmpty;
        }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    return isEmpty ? Collections.emptyIterator() : StreamableIterable.emptyIterator();
                }
            };
        }

        @Override
        public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
            if (isEmpty) {
                return new StreamableIteratorImpl<>(Collections.emptyIterator());
            }
            // quantity=0 case: only rank 0 exists
            if (start.equals(BigInteger.ZERO) && m.equals(BigInteger.ONE)) {
                return new StreamableIteratorImpl<>(StreamableIterable.emptyIterator());
            }
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        @Override
        public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
            throw new UnsupportedOperationException("EmptyBuilder does not support rank-based generation");
        }

        @Override
        public StreamableIterable<Object> choice(int sampleSize, Random random) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        @Override
        public StreamableIterable<Object> sample(int sampleSize, Random random) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }
    }
}
