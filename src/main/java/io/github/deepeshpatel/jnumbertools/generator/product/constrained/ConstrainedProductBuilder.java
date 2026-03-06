/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.*;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerChoice;
import io.github.deepeshpatel.jnumbertools.generator.numbers.BigIntegerSample;
import io.github.deepeshpatel.jnumbertools.generator.product.CartesianProductByRanks;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Builder for generating constrained Cartesian products where each dimension can have
 * complex constraints like distinct combinations, multi-select (repetitive) combinations,
 * or subsets within a size range.
 * <p>
 * Unlike simple products where each dimension is a flat list, constrained products allow
 * each dimension to be a combinatorial structure itself. This is particularly useful for
 * problems like generating all possible pizzas with constraints on toppings, cheeses, etc.
 * </p>
 *
 * <h2>Dimension Types</h2>
 * <ul>
 *   <li><b>Distinct combinations</b> - {@link #andDistinct(int, List)}: Select k distinct elements without repetition</li>
 *   <li><b>Multi-select combinations</b> - {@link #andMultiSelect(int, List)}: Select k elements with repetition allowed</li>
 *   <li><b>Subset range</b> - {@link #andInRange(int, int, List)}: Select any subset of size between from and to</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Pizza Configuration Problem</h3>
 * <pre>
 * // Define ingredient pools
 * List&lt;String&gt; bases = List.of("Small", "Medium", "Large");
 * List&lt;String&gt; cheeses = List.of("Mozzarella", "Cheddar", "Ricotta");
 * List&lt;String&gt; sauces = List.of("Tomato", "White", "Pesto");
 * List&lt;String&gt; toppings = List.of("Mushrooms", "Onions", "Peppers", "Olives", "Spinach");
 *
 * ConstrainedProductBuilder pizzaBuilder = new ConstrainedProductBuilder(1, bases, calculator)
 *     .andDistinct(2, cheeses)                    // Choose 2 distinct cheeses
 *     .andMultiSelect(2, sauces)                  // Choose 2 sauces (can repeat)
 *     .andInRange(1, 3, toppings);                // Choose 1 to 3 toppings
 *
 * System.out.println("Total pizza combinations: " + pizzaBuilder.count());
 * pizzaBuilder.lexOrder().limit(5).forEach(System.out::println);
 * </pre>
 *
 * <h3>Sports Team Selection</h3>
 * <pre>
 * // Select a team with constraints
 * List&lt;String&gt; positions = List.of("PG", "SG", "SF", "PF", "C");
 * List&lt;String&gt; players = List.of("Alice", "Bob", "Charlie", "Diana", "Eve", "Frank");
 *
 * ConstrainedProductBuilder teamBuilder = new ConstrainedProductBuilder(1, positions, calculator)
 *     .andDistinct(5, players);  // Assign 5 distinct players to 5 positions
 *
 * System.out.println("Possible lineups: " + teamBuilder.count()); // P(6,5) = 720
 * </pre>
 *
 * <h3>Multi-layered Constraints</h3>
 * <pre>
 * // Complex product with multiple constraint types
 * ConstrainedProductBuilder complexBuilder =
 *     new ConstrainedProductBuilder(2, List.of("A", "B", "C"), calculator)  // All 2-combinations
 *     .andMultiSelect(3, List.of("X", "Y"))                                 // All 3-multisets
 *     .andInRange(0, 2, List.of(1, 2, 3, 4))                                // All subsets size 0-2
 *     .andDistinct(1, List.of("Red", "Green", "Blue"));                     // Single element
 * </pre>
 *
 * <h3>Every mᵗʰ Product</h3>
 * <pre>
 * // Every 100th pizza configuration starting from rank 50
 * pizzaBuilder.lexOrderMth(100, 50)
 *             .forEach(System.out::println);
 * </pre>
 *
 * <h3>Random Sampling</h3>
 * <pre>
 * // Sample 10 unique configurations without replacement
 * pizzaBuilder.sample(10)
 *             .forEach(System.out::println);
 *
 * // Sample 5 configurations with replacement (duplicates allowed)
 * pizzaBuilder.choice(5)
 *             .forEach(System.out::println);
 * </pre>
 *
 * <h3>Rank-Based Access</h3>
 * <pre>
 * // Get configurations at specific ranks
 * pizzaBuilder.byRanks(List.of(
 *     BigInteger.ZERO,
 *     BigInteger.valueOf(1000),
 *     BigInteger.valueOf(5000)
 * )).forEach(System.out::println);
 * </pre>
 *
 * <h3>Empty and Edge Cases</h3>
 * <pre>
 * // Empty input for a dimension makes the entire product empty
 * ConstrainedProductBuilder emptyDimBuilder =
 *     new ConstrainedProductBuilder(1, List.of("A", "B"), calculator)
 *         .andDistinct(2, Collections.emptyList());  // Empty list with quantity > 0
 * System.out.println(emptyDimBuilder.count()); // 0
 *
 * // Quantity 0 produces a dimension with one empty element
 * ConstrainedProductBuilder zeroBuilder =
 *     new ConstrainedProductBuilder(0, List.of("A", "B"), calculator);
 * System.out.println(zeroBuilder.count()); // 1
 * zeroBuilder.lexOrder().forEach(System.out::println); // Prints: []
 *
 * // Invalid ranges throw exceptions
 * // new ConstrainedProductBuilder(1, bases, calculator)
 * //     .andInRange(5, 3, toppings); // Exception: from must be ≤ to
 * </pre>
 *
 * <h3>Performance Considerations</h3>
 * <pre>
 * // Each dimension is handled by specialized generators:
 * // - Distinct combinations: Uses UniqueCombinationBuilder (efficient for C(n,k))
 * // - Multi-select: Uses RepetitiveCombinationBuilder (efficient for C(n+k-1,k))
 * // - Subset range: Uses SubsetBuilder (efficient for 2ⁿ ranges)
 *
 * // The overall product uses mixed-radix decomposition for O(k) rank access
 * </pre>
 *
 * <h3>Mathematical Interpretation</h3>
 * <pre>
 * // Each valid tuple corresponds to a unique rank in the mixed-radix number system
 * // where each digit's radix is the size of its dimension's combinatorial space.
 * // For dimensions with sizes [s₁, s₂, ..., sₖ], rank r decomposes as:
 * // r = d₁ × (s₂×s₃×...×sₖ) + d₂ × (s₃×...×sₖ) + ... + dₖ
 * // where 0 ≤ dᵢ < sᵢ are the indices within each dimension.
 * </pre>
 *
 * <p>
 * This builder is immutable and thread-safe. All configuration methods return new instances.
 * </p>
 *
 * @see SimpleProductBuilder
 * @see CartesianProductByRanks
 * @see UniqueCombinationBuilder
 * @see RepetitiveCombinationBuilder
 * @see SubsetBuilder
 * @see <a href="https://en.wikipedia.org/wiki/Mixed_radix">Mixed Radix Number System</a>
 * @author Deepesh Patel
 */
@SuppressWarnings({"unchecked"})
public final class ConstrainedProductBuilder implements Builder<Object> {

    private final List<Builder<?>> builders;
    private final Calculator calculator;

    /**
     * Constructs a ConstrainedProductBuilder with an initial combination of size {@code n}.
     *
     * @param n          the size of the initial combinations
     * @param elements   the list of elements to create combinations from (null and empty allowed )
     * @param calculator the calculator used for combinatorial computations
     */
    public ConstrainedProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        this.builders = new ArrayList<>();
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && n > 0 ? new EmptyBuilder(elements.size()) : new Combinations(calculator).unique(n, elements));
    }

    /**
     * Private constructor for creating new instances with existing data.
     */
    private ConstrainedProductBuilder(List<Builder<?>> builders, Calculator calculator) {
        this.builders = builders;
        this.calculator = calculator;
    }

    /**
     * Adds a distinct combination of the specified quantity to the builder.
     *
     * @param quantity the size of the distinct combinations
     * @param elements the list of elements to create combinations from (null and empty allowed)
     * @return a new instance of ConstrainedProductBuilder with the additional combination
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        newBuilders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder(quantity) : new Combinations(calculator).unique(quantity, elements));
        return new ConstrainedProductBuilder(newBuilders, calculator);
    }

    /**
     * Adds a multi-select (repetitive) combination of the specified quantity to the builder.
     *
     * @param quantity the size of the multi-select combinations
     * @param elements the list of elements to create combinations from (null and empty allowed)
     * @return a new instance of ConstrainedProductBuilder with the additional combination
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        newBuilders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder(quantity) : new Combinations(calculator).repetitive(quantity, elements));
        return new ConstrainedProductBuilder(newBuilders, calculator);
    }

    /**
     * Adds a dimension consisting of all subsets of the given elements whose sizes fall within a range.
     * <p>
     * This creates a constrained dimension where each position can be any subset of the elements
     * with size between {@code from} and {@code to} (inclusive). The subsets are generated in
     * lexicographical order based on element indices.
     * </p>
     *
     * @param from     the minimum subset size (inclusive, must be ≥ 0)
     * @param to       the maximum subset size (inclusive, must be ≥ from)
     * @param elements the list of elements to create subsets from (null treated as empty)
     * @return a new ConstrainedProductBuilder instance with the additional subset dimension
     * @throws IllegalArgumentException if from < 0, to < from, or from > elements.size()
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        List<Builder<?>> newBuilders = new ArrayList<>(this.builders);
        newBuilders.add(new Subsets(calculator).of(elements).inRange(from, to));
        return new ConstrainedProductBuilder(newBuilders, calculator);
    }

    /**
     * Builds and returns a generator for all products in lexicographical order.
     *
     * @return an StreamableIterable containing all the generated products
     */
    @Override
    public StreamableIterable<Object> lexOrder() {
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
        EveryMthIterable.validateLexOrderMthParams(m, start, count());
        BigInteger maxCount = count();
        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders, new EveryMthIterable(start, m, maxCount));
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
     * @throws IllegalStateException    if no constraints were configured
     * @implNote Time complexity is O(k) where k is the number of dimensions,
     * as this performs a mixed-radix decomposition of the rank value
     */
    public CartesianProductByRanks<Object> byRanks(Iterable<BigInteger> ranks) {
        EveryMthIterable.validateByRanksParams(ranks);
        return new CartesianProductByRanks<>((List<Builder<Object>>) (List<?>) builders, ranks);
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

        // If product is empty, return empty result regardless of sampleSize
        if (maxCount.equals(BigInteger.ZERO)) {
            return new CartesianProductByRanks<>((List<Builder<Object>>) (List) builders,
                    Collections.emptyList());
        }

        return new CartesianProductByRanks((List<Builder<Object>>) (List) builders,
                new BigIntegerChoice(maxCount, sampleSize));
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

    @Override
    public String toString() {
        return "ConstrainedProductBuilder{" +
                "builders=" + builders +
                ", count=" + count() +
                '}';
    }


    // A special builder for empty inputs that produces either:
    // Mathematical rationale:
    // - quantity = 0: Exactly one way to select nothing from any set (including empty set)
    //   → count = 1, iterator returns one empty list []
    // - quantity > 0: Impossible to select positive number of items from empty set
    //   → count = 0, iterator returns no elements (empty iterator)
    private static class EmptyBuilder implements Builder<Object> {

        private final BigInteger count;
        private final boolean hasElements;

        EmptyBuilder(int quantity) {
            this.hasElements = quantity == 0;
            this.count = quantity > 0 ? BigInteger.ZERO : BigInteger.ONE;
        }

        @Override
        public BigInteger count() {
            return count;
        }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    return hasElements
                            ? Collections.singletonList(Collections.emptyList()).iterator()
                            : Collections.emptyIterator();
                }
            };
        }

        @Override
        public StreamableIterable<Object> lexOrderMth(BigInteger m, BigInteger start) {
            return new StreamableIteratorImpl<>(hasElements
                    ? Collections.singletonList(Collections.emptyList()).iterator()
                    : Collections.emptyIterator());
        }

        @Override
        public StreamableIterable<Object> byRanks(Iterable<BigInteger> ranks) {
            throw new UnsupportedOperationException("EmptyBuilder does not support rank-based generation");
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