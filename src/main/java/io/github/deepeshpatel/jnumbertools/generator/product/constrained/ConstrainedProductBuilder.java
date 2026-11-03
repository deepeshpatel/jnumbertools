/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.*;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.generator.product.CartesianProductByRanks;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

    /**
     * Most tuples, over all dimensions together, that lexOrder() keeps in memory for fast restarts. Capped by the
     * heap (a tuple needs roughly 200 bytes; 2048 per budgeted tuple leaves a wide margin), so a small JVM
     * materializes less. Only speed depends on this value, never the results.
     */
    private static final long MATERIALIZE_BUDGET = Math.min(250_000L, Runtime.getRuntime().maxMemory() / 2048);

    private final List<Builder<?>> builders;
    private final Calculator calculator;
    private final boolean isEmptyProduct;
    private final BigInteger cachedCount; // pre-computed; count() is called frequently

    /**
     * Constructs a ConstrainedProductBuilder with an initial multi-select combination.
     */
    public ConstrainedProductBuilder(int n, List<?> elements, boolean isMultiSelect, Calculator calculator) {
        Util.validateInput(elements);
        Util.validateNotNegative(n, "quantity");   // also for an empty element list, which skips Combinations' own check
        this.calculator = calculator;
        this.builders = new ArrayList<>();
        Builder<?> firstBuilder;
        if (elements.isEmpty()) {
            firstBuilder = new EmptyBuilder(n);
        } else {
            firstBuilder = isMultiSelect
                    ? new Combinations(calculator).repetitive(n, elements)
                    : new Combinations(calculator).unique(n, elements);
        }
        this.builders.add(firstBuilder);
        this.isEmptyProduct = firstBuilder.isEmpty();
        this.cachedCount = computeCount(this.builders, this.isEmptyProduct);
    }

    /**
     * Constructs a ConstrainedProductBuilder with an initial subset range.
     */
    public ConstrainedProductBuilder(int from, int to, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        this.builders = new ArrayList<>();
        Builder<?> firstBuilder = new Subsets(calculator).of(elements).inRange(from, to);
        this.builders.add(firstBuilder);
        this.isEmptyProduct = firstBuilder.isEmpty();
        this.cachedCount = computeCount(this.builders, this.isEmptyProduct);
    }

    /**
     * Private constructor for creating new instances with existing data.
     */
    private ConstrainedProductBuilder(List<Builder<?>> builders, Calculator calculator, boolean isEmptyProduct) {
        this.builders = builders;
        this.calculator = calculator;
        this.isEmptyProduct = isEmptyProduct;
        this.cachedCount = computeCount(builders, isEmptyProduct);
    }

    // ── count helper ────────────────────────────────────────────────────────

    private static BigInteger computeCount(List<Builder<?>> builders, boolean isEmptyProduct) {
        if (isEmptyProduct)      return BigInteger.ZERO;
        if (builders.isEmpty())  return BigInteger.ONE;
        return builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    // ── Dimension chaining ───────────────────────────────────────────────────

    /**
     * Adds a distinct combination dimension.
     * <p>
     * The arguments are always validated, even when the product is already empty.
     * <strong>Optimization:</strong> an already-empty product stays empty, so the new
     * dimension is not added.
     * </p>
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        Util.validateInput(elements);
        Util.validateNotNegative(quantity, "quantity");
        // Building the dimension validates it (null elements, sizes) and snapshots the list.
        Builder<?> newBuilder = elements.isEmpty()
                ? new EmptyBuilder(quantity)
                : new Combinations(calculator).unique(quantity, elements);
        return append(newBuilder);
    }

    /**
     * Adds a multi-select (repetitive) combination dimension.
     * <p>
     * The arguments are always validated, even when the product is already empty.
     * <strong>Optimization:</strong> an already-empty product stays empty, so the new
     * dimension is not added.
     * </p>
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        Util.validateInput(elements);
        Util.validateNotNegative(quantity, "quantity");
        Builder<?> newBuilder = elements.isEmpty()
                ? new EmptyBuilder(quantity)
                : new Combinations(calculator).repetitive(quantity, elements);
        return append(newBuilder);
    }

    /**
     * Adds a subset range dimension.
     * <p>
     * The arguments are always validated, even when the product is already empty.
     * <strong>Optimization:</strong> an already-empty product stays empty, so the new
     * dimension is not added.
     * </p>
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        Util.validateInput(elements);
        // Building the dimension validates the range and the elements and snapshots the list.
        return append(new Subsets(calculator).of(elements).inRange(from, to));
    }

    /**
     * Adds an already validated dimension. An already-empty product stays empty, so nothing is added to it.
     */
    private ConstrainedProductBuilder append(Builder<?> newDimension) {
        if (isEmptyProduct) {
            return new ConstrainedProductBuilder(builders, calculator, true);
        }
        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        newBuilders.add(newDimension);
        return new ConstrainedProductBuilder(newBuilders, calculator, newDimension.isEmpty());
    }

    // ── Builder interface ────────────────────────────────────────────────────

    @Override
    public StreamableIterable<Object> lexOrder() {
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }
        return new ConstrainedProductGenerator();
    }

    @Override
    public CartesianProductByRanks<Object> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, cachedCount);
        if (isEmptyProduct) {
            return new CartesianProductByRanks<>(
                    (List<Builder<Object>>) (List) builders,
                    Collections.emptyList()
            );
        }
        return new CartesianProductByRanks(
                (List<Builder<Object>>) (List) builders,
                new EveryMthIterable(start, m, cachedCount)
        );
    }

    @Override
    public CartesianProductByRanks<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new CartesianProductByRanks<>((List<Builder<Object>>) (List<?>) builders, ranks);
    }

    @Override
    public BigInteger count() {
        return cachedCount;
    }

    @Override
    public boolean isEmpty() {
        return isEmptyProduct;
    }

    /**
     * Generates a random sample of products with replacement using custom random generator.
     *
     * @param sampleSize the number of products to generate
     * @param random     the random generator to use
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is not positive or the product is empty (nothing to draw from)
     * @throws NullPointerException     if random is null
     */
    @Override
    public CartesianProductByRanks<Object> choice(int sampleSize, Random random) {
        // BigIntegerChoice rejects sampleSize <= 0, an empty domain (count 0) and a null random source.
        return new CartesianProductByRanks(
                (List<Builder<Object>>) (List) builders,
                new BigIntegerChoice(cachedCount, sampleSize, random)
        );
    }

    /**
     * Generates a random sample of unique products without replacement using custom random generator.
     *
     * @param sampleSize the number of unique products to generate
     * @param random     the random generator to use
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is not positive or exceeds the total number of products
     *                                  (which includes every size for an empty product)
     * @throws NullPointerException     if random is null
     */
    @Override
    public CartesianProductByRanks<Object> sample(int sampleSize, Random random) {
        // BigIntegerSample rejects sampleSize <= 0, sampleSize > count and a null random source.
        return new CartesianProductByRanks(
                (List<Builder<Object>>) (List) builders,
                new BigIntegerSample(cachedCount, sampleSize, random)
        );
    }

    // ── ConstrainedProductGenerator ─────────────────────────────────────────

    private class ConstrainedProductGenerator extends AbstractGenerator<Object> {

        ConstrainedProductGenerator() {
            super(Collections.emptyList());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<List<Object>> iterator() {
            // A dimension is restarted every time a dimension to its left moves. Restarting a dimension that is
            // held in memory is a plain list iteration; restarting a lazy one regenerates its tuples, which costs
            // about twice as much per product tuple. So the smallest dimensions are kept in memory, as long as
            // all of them together stay within MATERIALIZE_BUDGET tuples, and every other dimension (for example
            // C(34,17), which can never fit) is generated lazily and never materialized.
            // The first dimension is never restarted - it is traversed once - so keeping it in memory would only
            // cost time up front, and it is always lazy.
            int n = builders.size();
            Integer[] smallestFirst = new Integer[n];
            for (int i = 0; i < n; i++) smallestFirst[i] = i;
            Arrays.sort(smallestFirst, Comparator.comparing(i -> builders.get(i).count()));

            Iterable<List<Object>>[] dimensions = new Iterable[n];
            long remaining = MATERIALIZE_BUDGET;
            for (int i : smallestFirst) {
                Builder<?> dimension = builders.get(i);
                BigInteger count = dimension.count();
                if (i > 0 && count.compareTo(BigInteger.valueOf(remaining)) <= 0) {
                    remaining -= count.longValue();
                    dimensions[i] = (List<List<Object>>) (List<?>) dimension.lexOrder().stream().toList();
                } else {
                    dimensions[i] = () -> (Iterator<List<Object>>) (Iterator<?>) dimension.lexOrder().iterator();
                }
            }
            return new ConstrainedProduct(Arrays.asList(dimensions)).iterator();
        }
    }

    // ── EmptyBuilder ─────────────────────────────────────────────────────────

    /**
     * Empty builder for dimensions with no valid combinations.
     */
    private static class EmptyBuilder implements Builder<Object> {

        private final BigInteger count;
        private final boolean isEmpty;

        EmptyBuilder(int quantity) {
            this.count   = quantity > 0 ? BigInteger.ZERO : BigInteger.ONE;
            this.isEmpty = quantity > 0;
        }

        @Override public BigInteger count()  { return count; }
        @Override public boolean   isEmpty() { return isEmpty; }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    return isEmpty ? Collections.emptyIterator() : Util.emptyListIterator();
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
                return new StreamableIteratorImpl<>(Util.emptyListIterator());
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