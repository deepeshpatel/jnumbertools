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
 * <h2>Key Characteristics</h2>
 * <ul>
 *   <li><b>Order matters</b> - Tuples preserve the order of input sets</li>
 *   <li><b>Rightmost index varies fastest</b> - Lexicographical order follows row-major order</li>
 *   <li><b>Mixed types supported</b> - Different sets can contain different element types</li>
 *   <li><b>Total count</b> - Product of sizes of all input sets</li>
 * </ul>
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
 * <h3>Single Set Product</h3>
 * <pre>
 * // Product of a single set returns each element as a singleton tuple
 * SimpleProductBuilder singleBuilder = new SimpleProductBuilder(List.of("A", "B", "C"));
 * singleBuilder.lexOrder().forEach(System.out::println);
 * // Output: [A], [B], [C]
 * </pre>
 *
 * <h3>Varargs Convenience</h3>
 * <pre>
 * // Using varargs for cleaner syntax
 * SimpleProductBuilder builder2 = new SimpleProductBuilder(List.of("A", "B"))
 *     .and(1, 2, 3)           // varargs automatically converted to list
 *     .and("X", "Y", "Z");
 * </pre>
 *
 * <h3>Every mᵗʰ Product</h3>
 * <pre>
 * // Every 2nd product starting from rank 1
 * builder.lexOrderMth(2, 1)
 *        .forEach(System.out::println);
 * // Output: [A,1,Y], [A,2,Y], [B,1,Y], [B,2,Y] (ranks 1,3,5,7)
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 3 unique products without replacement
 * builder.sample(3)
 *        .forEach(System.out::println);
 *
 * // Sample 5 products with replacement (duplicates allowed)
 * builder.choice(5)
 *        .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get products at ranks 0, 3, and 6
 * builder.byRanks(List.of(BigInteger.ZERO,
 *                         BigInteger.valueOf(3),
 *                         BigInteger.valueOf(6)))
 *        .forEach(System.out::println);
 * // Output: [A,1,X], [A,2,Y], [B,2,X] (ranks 0,3,6)
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // Empty list as input
 * SimpleProductBuilder emptyBuilder = new SimpleProductBuilder(Collections.emptyList());
 * System.out.println(emptyBuilder.count()); // 1
 * emptyBuilder.lexOrder().forEach(System.out::println); // Prints: []
 *
 * // Adding empty list makes entire product empty
 * SimpleProductBuilder withEmpty = new SimpleProductBuilder(List.of("A", "B"))
 *     .and(Collections.emptyList())
 *     .and(List.of("X", "Y"));
 * System.out.println(withEmpty.count()); // 0
 * System.out.println(withEmpty.lexOrder().stream().count()); // 0
 *
 * // Single empty list returns one empty tuple
 * SimpleProductBuilder singleEmpty = new SimpleProductBuilder(Collections.emptyList());
 * System.out.println(singleEmpty.count()); // 1
 * singleEmpty.lexOrder().forEach(System.out::println); // Prints: []
 * </pre>
 *
 * <h3>Mathematical Properties</h3>
 * <pre>
 * // Product of sizes: |A| × |B| × |C|
 * SimpleProductBuilder sizes = new SimpleProductBuilder(List.of(1,2,3))      // size 3
 *     .and(List.of('a','b'))                                                 // size 2
 *     .and(List.of("X","Y","Z","W"));                                        // size 4
 * System.out.println(sizes.count()); // 3 × 2 × 4 = 24
 *
 * // Each tuple corresponds to a mixed-radix number
 * // For sets of sizes [3,2,4], rank 5 = [0,1,1] in mixed radix
 * // meaning first set index 0, second set index 1, third set index 1
 * </pre>
 *
 * <h3>Nested Builders</h3>
 * <pre>
 * // SimpleProductBuilder implements Builder&lt;Object&gt;, allowing composition
 * SimpleProductBuilder outer = new SimpleProductBuilder(List.of("Prefix"))
 *     .and(builder)  // Can include another builder's products as a dimension
 *     .and(List.of("Suffix"));
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @see SimpleProduct
 * @see CartesianProductByRanks
 * @see MixedRadix
 * @see <a href="https://en.wikipedia.org/wiki/Cartesian_product">Cartesian Product</a>
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class SimpleProductBuilder implements Builder<Object> {

    private final List<Builder<Object>> builders;
    private final List<List<?>> allLists;

    /**
     * Constructs a SimpleProductBuilder with an initial list of elements.
     * @param elements   the initial list of elements (null and empty allowed)
     */
    public SimpleProductBuilder(List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        this.allLists = new ArrayList<>();
        this.builders = new ArrayList<>();
        this.allLists.add(elements);
        this.builders.add(new SingleListBuilder(elements));
    }

    /**
     * Private constructor for creating new instances with existing data.
     */
    private SimpleProductBuilder() {
        this.allLists = new ArrayList<>();
        this.builders = new ArrayList<>();
    }

    /**
     * Adds another set of elements to the Cartesian product.
     * <p>
     * The resulting product will be the Cartesian product of all previously added sets
     * and this new set. Order matters: elements from earlier sets appear first in each tuple.
     * </p>
     *
     * @param elements the list of elements to add as a new dimension (null treated as empty)
     * @return a new SimpleProductBuilder instance with the additional set
     */
    public SimpleProductBuilder and(List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        SimpleProductBuilder newBuilder = new SimpleProductBuilder();
        newBuilder.allLists.addAll(this.allLists);
        newBuilder.builders.addAll(this.builders);
        newBuilder.allLists.add(elements);
        newBuilder.builders.add(new SingleListBuilder(elements));
        return newBuilder;
    }

    /**
     * Adds a new list of elements to the product from a varargs array.
     *
     * @param elements the elements to add
     * @return a new instance of SimpleProductBuilder with the additional elements
     */
    public SimpleProductBuilder and(Object... elements) {
        return and(List.of(elements));
    }

    /**
     * Builds and returns a generator for all products in lexicographical order.
     *
     * @return a StreamableIterable containing all the generated products
     */
    @Override
    public StreamableIterable<Object> lexOrder() {
        return new SimpleProductGenerator();
    }

    /**
     * Builds and returns a generator for every mᵗʰ product in lexicographical order.
     *
     * @param m     the interval to select every mᵗʰ product
     * @param start the starting rank
     * @return a StreamableIterable for the specified intervals
     */
    @Override
    public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
        EveryMthIterable.validateLexOrderMthParams(m, start, count());
        BigInteger maxCount = count();
        return new StreamableIteratorImpl<>(new CartesianProductByRanks<>(builders, new EveryMthIterable(start, m, maxCount)).iterator());
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
     * @return a StreamableIterable for the specified ranks
     * @throws IllegalArgumentException if any rank exceeds product space
     * @throws IllegalStateException if no sets were added
     */
    @Override
    public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
        EveryMthIterable.validateByRanksParams(ranks);
        return new StreamableIteratorImpl<>(new CartesianProductByRanks<>(builders, ranks).iterator());
    }

    /**
     * Generates a random sample of products with replacement.
     *
     * @param sampleSize the number of products to generate
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative
     */
    public StreamableIterable<Object> choice(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger maxCount = count();
        return new StreamableIteratorImpl<>(new CartesianProductByRanks<>(builders, new BigIntegerChoice(maxCount, sampleSize)).iterator());
    }

    /**
     * Generates a random sample of unique products.
     *
     * @param sampleSize the number of unique products to generate
     * @return a StreamableIterable for the sampled products
     * @throws IllegalArgumentException if sampleSize is negative or exceeds total products
     */
    public StreamableIterable<Object> sample(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size cannot be negative");
        }
        BigInteger maxCount = count();
        return new StreamableIteratorImpl<>(new CartesianProductByRanks<>(builders, new BigIntegerSample(maxCount, sampleSize)).iterator());
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

    @Override
    public String toString() {
        return "SimpleProductBuilder{" +
                "allLists=" + allLists +
                ", count=" + count() +
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
        private SingleListBuilder(List<?> elements) {
            this.elements = elements != null ? elements : Collections.emptyList();
        }

        @Override
        public BigInteger count() {
            // Empty set has one conceptual element (the empty tuple)
            return elements.isEmpty() ? BigInteger.ONE : BigInteger.valueOf(elements.size());
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
            if (elements.isEmpty()) {
                // For empty list, only rank 0 exists and returns empty list
                return new StreamableIteratorImpl<>(new Iterator<>() {
                    private boolean hasNext = start.equals(BigInteger.ZERO) && m.equals(BigInteger.ONE);

                    @Override
                    public boolean hasNext() {
                        return hasNext;
                    }

                    @Override
                    public List<Object> next() {
                        if (!hasNext) throw new NoSuchElementException();
                        hasNext = false;
                        return List.of();
                    }
                });
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
                    return List.of(elements.get(index.intValue()));
                }
            });
        }

        @Override
        public StreamableIterable<Object> choice(int sampleSize) {
            return null;
        }

        @Override
        public StreamableIterable<Object> sample(int sampleSize) {
            return null;
        }
    }
}