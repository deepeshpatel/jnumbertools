/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import io.github.deepeshpatel.jnumbertools.generator.base.*;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.generator.product.CartesianProductByRanks;
import io.github.deepeshpatel.jnumbertools.numbersystem.MixedRadix;

import java.math.BigInteger;
import java.util.*;

/**
 * Builder for generating simple Cartesian products of multiple sets.
 * <p>
 * A simple Cartesian product A₁ × A₂ × ... × Aₖ generates all possible ordered tuples
 * (a₁, a₂, ..., aₖ) where aᵢ ∈ Aᵢ. This builder allows adding sets incrementally
 * and provides various generation strategies.
 * </p>
 *
 * <h2>Mathematical Rules</h2>
 * <pre>
 * ┌────────────────────┬───────────┬──────────────┬───────────┐
 * │ Scenario           │ count()   │ iterator     │ isEmpty() │
 * ├────────────────────┼───────────┼──────────────┼───────────┤
 * │ No dimensions      │ 1         │ [[]]         │ false     │
 * │ Single empty dim   │ 1         │ [[]]         │ false     │
 * │ Multiple dims,     │ Π sizes   │ products     │ false     │
 * │   none empty       │           │              │           │
 * │ Multiple dims,     │ 0         │ []           │ true      │
 * │   any empty        │           │              │           │
 * └────────────────────┴───────────┴──────────────┴───────────┘
 * </pre>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Product</h3>
 * <pre>
 * SimpleProductBuilder builder = new SimpleProductBuilder(List.of("A", "B"))
 *     .and(List.of(1, 2))
 *     .and(List.of("X", "Y"));
 *
 * // All 2 × 2 × 2 = 8 tuples
 * builder.lexOrder()
 *        .forEach(System.out::println);
 * // Output: [A,1,X], [A,1,Y], [A,2,X], [A,2,Y],
 * //         [B,1,X], [B,1,Y], [B,2,X], [B,2,Y]
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // Single empty dimension -> count=1, returns [[]]
 * SimpleProductBuilder emptyDim = new SimpleProductBuilder(Collections.emptyList());
 * System.out.println(emptyDim.count());        // 1
 * System.out.println(emptyDim.isEmpty());      // false
 * emptyDim.lexOrder().forEach(System.out::println); // Prints: []
 *
 * // Adding empty dimension to existing product -> becomes empty
 * SimpleProductBuilder withEmpty = new SimpleProductBuilder(List.of("A", "B"))
 *     .and(Collections.emptyList());
 * System.out.println(withEmpty.count());        // 0
 * System.out.println(withEmpty.isEmpty());      // true
 * System.out.println(withEmpty.lexOrder().stream().count()); // 0
 * </pre>
 *
 * @see SimpleProduct
 * @see CartesianProductByRanks
 * @see MixedRadix
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class SimpleProductBuilder implements Builder<Object> {

    private final List<Builder<Object>> builders;
    private final List<List<?>> allLists;
    private final boolean hasEmptyDimension;  // true if any dimension is empty
    private final boolean isEmptyProduct;      // true if product yields no elements

    /**
     * Constructs a SimpleProductBuilder for a 0-dimensional Cartesian product (nullary product).
     * yields a single empty tuple [[]].
     */
    public SimpleProductBuilder() {
        this.allLists = new ArrayList<>();
        this.builders = new ArrayList<>();
        this.hasEmptyDimension = false;
        this.isEmptyProduct = false;
    }

    /**
     * Constructs a SimpleProductBuilder with an initial list of elements.
     *
     * @param elements the initial list of elements (null treated as empty)
     */
    public SimpleProductBuilder(List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        this.allLists = new ArrayList<>();
        this.builders = new ArrayList<>();
        this.allLists.add(elements);
        this.builders.add(new SingleListBuilder(elements));

        // Track empty dimensions
        this.hasEmptyDimension = elements.isEmpty();
        // Product is empty if ANY dimension is empty
        this.isEmptyProduct = elements.isEmpty();
    }

    /**
     * Private constructor for creating new instances with existing data.
     */
    private SimpleProductBuilder(List<Builder<Object>> builders,
                                 List<List<?>> allLists,
                                 boolean hasEmptyDimension,
                                 boolean isEmptyProduct) {
        this.builders = builders;
        this.allLists = allLists;
        this.hasEmptyDimension = hasEmptyDimension;
        this.isEmptyProduct = isEmptyProduct;
    }

    /**
     * Adds another set of elements to the Cartesian product.
     * <p>
     * The resulting product will be the Cartesian product of all previously added sets
     * and this new set. Order matters: elements from earlier sets appear first in each tuple.
     * </p>
     * <p>
     * <strong>Performance optimization:</strong> If the product is already empty,
     * this method returns an empty builder immediately without processing further dimensions.
     * </p>
     *
     * @param elements the list of elements to add as a new dimension (null treated as empty)
     * @return a new SimpleProductBuilder instance with the additional set
     * @throws NullPointerException if elements is null
     */
    public SimpleProductBuilder and(List<?> elements) {
        Util.validateInput(elements);

        // If product is already empty, return empty builder immediately
        if (isEmptyProduct) {
            return new SimpleProductBuilder(builders, allLists, true, true);
        }

        boolean newDimEmpty = elements.isEmpty();
        boolean newHasEmptyDimension = hasEmptyDimension || newDimEmpty;

        // Determine if product becomes empty:
        // - If we already had a dimension and this new one is empty, product becomes empty
        // - If this is the first dimension, and it's empty, product is NOT empty (handled in constructor)
        boolean newIsEmptyProduct = (!builders.isEmpty() && newDimEmpty);

        List<Builder<Object>> newBuilders = new ArrayList<>(builders);
        List<List<?>> newAllLists = new ArrayList<>(allLists);
        newBuilders.add(new SingleListBuilder(elements));
        newAllLists.add(elements);

        return new SimpleProductBuilder(newBuilders, newAllLists,
                newHasEmptyDimension, newIsEmptyProduct);
    }

    /**
     * Builds and returns a generator for all products in lexicographical order.
     *
     * @return a StreamableIterable containing all the generated products
     */
    @Override
    public StreamableIterable<Object> lexOrder() {
        // Case 1: Product is empty -> return empty iterator
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        // Case 2: 0-dimensional product -> return [[]]
        if (builders.isEmpty()) {
            return new StreamableIteratorImpl<>(Util.emptyListIterator());
        }

        // Case 3: Normal product
        return new SimpleProductGenerator();
    }

    /**
     * Builds and returns a generator for every mᵗʰ product in lexicographical order.
     *
     * @param m     the interval to select every mᵗʰ product
     * @param start the starting rank
     * @return a StreamableIterable for the specified intervals
     * @throws IllegalArgumentException if m ≤ 0, start < 0, or start ≥ count() when count() > 0
     */
    @Override
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        Util.validateLexOrderMthParams(m, start, count());

        // Case 1: Product is empty -> return empty iterator
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        // Case 2: 0-dimensional product -> only rank 0 exists
        if (builders.isEmpty()) {
            if (start.equals(BigInteger.ZERO) && m.equals(BigInteger.ONE)) {
                return new StreamableIteratorImpl<>(Util.emptyListIterator());
            }
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        // Case 3: Normal product
        BigInteger maxCount = count();
        return new StreamableIteratorImpl<>(
                new CartesianProductByRanks<>(builders, new EveryMthIterable(start, m, maxCount)).iterator()
        );
    }

    /**
     * Generates Cartesian product tuples at specified rank positions.
     * <p>
     * Ranks follow row-major order (rightmost indices vary fastest).
     * </p>
     *
     * @param ranks Iterable of 0-based rank numbers
     * @return a StreamableIterable for the specified ranks
     * @throws IllegalArgumentException if ranks is null
     */
    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        Util.validateByRanksParams(ranks);
        return new StreamableIteratorImpl<>(
                new CartesianProductByRanks<>(builders, ranks).iterator()
        );
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
    public StreamableIterable<Object> choice(int sampleSize, Random random) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }

        // If product is empty, return empty result regardless of sampleSize
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        BigInteger maxCount = count();
        return new StreamableIteratorImpl<>(
                new CartesianProductByRanks<>(builders, new BigIntegerChoice(maxCount, sampleSize, random)).iterator()
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
    public StreamableIterable<Object> sample(int sampleSize, Random random) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }

        // If product is empty, return empty result regardless of sampleSize
        if (isEmptyProduct) {
            return new StreamableIteratorImpl<>(Collections.emptyIterator());
        }

        BigInteger maxCount = count();
        if (BigInteger.valueOf(sampleSize).compareTo(maxCount) > 0) {
            throw new IllegalArgumentException(
                    "Sample size cannot exceed total products: " + maxCount
            );
        }

        return new StreamableIteratorImpl<>(
                new CartesianProductByRanks<>(builders, new BigIntegerSample(maxCount, sampleSize, random)).iterator()
        );
    }

    /**
     * Returns the total number of possible products.
     *
     * @return the count of products as a BigInteger
     */
    @Override
    public BigInteger count() {
        // Case 1: Product is empty (multiple dimensions with empty)
        if (isEmptyProduct) {
            return BigInteger.ZERO;
        }

        // Case 2: 0-dimensional product -> nullary product has 1 element ([[]])
        if (builders.isEmpty()) {
            return BigInteger.ONE;
        }

        // Case 3: Normal product
        return allLists.stream()
                .map(list -> BigInteger.valueOf(list.size()))
                .reduce(BigInteger.ONE, BigInteger::multiply);
    }

    /**
     * Indicates whether this builder will produce any elements.
     * <p>
     * A builder is empty if and only if its iterator will have no elements.
     * Note that {@code count() == 1} does NOT imply {@code !isEmpty()},
     * as a single empty dimension has count=1 and produces one element ([[]]).
     * </p>
     *
     * @return {@code true} if the product yields no elements, {@code false} otherwise
     */
    @Override
    public boolean isEmpty() {
        return isEmptyProduct;
    }

    @Override
    public String toString() {
        return "SimpleProductBuilder{" +
                "allLists=" + allLists +
                ", count=" + count() +
                ", isEmpty=" + isEmpty() +
                '}';
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
    private record SingleListBuilder(List<?> elements) implements Builder<Object> {
        private SingleListBuilder {
            elements = elements != null ? elements : Collections.emptyList();
        }

        @Override
        public BigInteger count() {
            return BigInteger.valueOf(elements.size());
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        @Override
        public StreamableIterable<Object> lexOrder() {
            return new AbstractGenerator<>(Collections.emptyList()) {
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
        public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
            Util.validateLexOrderMthParams(m, start, count());

            if (elements.isEmpty()) {
                return new StreamableIteratorImpl<>(Collections.emptyIterator());
            }

            return new StreamableIteratorImpl<>(new Iterator<>() {
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
            });
        }

        @Override
        public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
            return new StreamableIteratorImpl<>(new Iterator<>() {
                private final Iterator<BigInteger> rankIterator = ranks.iterator();

                @Override
                public boolean hasNext() {
                    return rankIterator.hasNext();
                }

                @Override
                public List<Object> next() {
                    BigInteger index = rankIterator.next();
                    if (index.compareTo(BigInteger.valueOf(elements.size())) >= 0) {
                        throw new IllegalArgumentException(
                                "Rank " + index + " out of bounds for size " + elements.size()
                        );
                    }
                    return List.of(elements.get(index.intValue()));
                }
            });
        }

        @Override
        public StreamableIterable<Object> choice(int sampleSize, Random random) {
            throw new UnsupportedOperationException("Single dimension does not support choice");
        }

        @Override
        public StreamableIterable<Object> sample(int sampleSize, Random random) {
            throw new UnsupportedOperationException("Single dimension does not support sample");
        }
    }
}
