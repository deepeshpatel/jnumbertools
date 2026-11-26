/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.LambdaConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

import java.util.List;

/**
 * Generates Cartesian products of elements from given sets.
 * <p>
 * The Cartesian product of sets A₁, A₂, ..., Aₖ is the set of all ordered tuples (a₁, a₂, ..., aₖ)
 * where aᵢ ∈ Aᵢ. This class supports these kinds of products:
 * <ul>
 *   <li><b>Simple Product</b>: Aⁿ where A is a single set repeated n times</li>
 *   <li><b>Constrained Product</b>: A₁ × A₂ × ... × Aₖ where each Aᵢ can be a set, combination, or subset with constraints</li>
 *   <li><b>Conditional Product</b>: a simple product restricted by pairwise rules</li>
 *   <li><b>Compound Product</b>: a simple product restricted by compound AND/OR trigger rules</li>
 *   <li><b>Lambda Product</b>: a simple product restricted by Java predicates over some or all dimensions,
 *       compiled once into the same constraints (see {@link #withLambda(List)})</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Simple Cartesian Product</h3>
 * <pre>
 * CartesianProduct cp = new CartesianProduct();
 *
 * // Product of [A, B] × [1, 2] × [X, Y]
 * cp.simpleProductOf(List.of("A", "B"), List.of(1, 2), List.of("X", "Y"))
 *   .lexOrder()
 *   .forEach(System.out::println);
 * // Output: [A,1,X], [A,1,Y], [A,2,X], [A,2,Y], [B,1,X], [B,1,Y], [B,2,X], [B,2,Y]
 *
 * // Every 2nd product starting from rank 1
 * cp.simpleProductOf(List.of(0, 1, 2), List.of(0, 1, 2))
 *   .lexOrderMth(2, 1)
 *   .forEach(System.out::println);
 *
 * // Random sample of 3 products without replacement
 * cp.simpleProductOf(List.of("A", "B", "C"), List.of("X", "Y"))
 *   .sample(3)
 *   .forEach(System.out::println);
 * </pre>
 *
 * <h3>Constrained Cartesian Product</h3>
 * <pre>
 * // Build a pizza with constraints:
 * // - 1 base from [Small, Medium, Large]
 * // - 2 distinct cheeses from [Ricotta, Mozzarella, Cheddar]
 * // - 2 sauces (with repetition allowed) from [Tomato, White, Green]
 * // - 1 to 5 toppings from [tomato, capsicum, onion, paneer, corn]
 *
 * cp.constrainedProductOfDistinct(1, "Small", "Medium", "Large")
 *   .andDistinct(2, "Ricotta", "Mozzarella", "Cheddar")
 *   .andMultiSelect(2, "Tomato", "White", "Green")
 *   .andInRange(1, 5, "tomato", "capsicum", "onion", "paneer", "corn")
 *   .lexOrder()
 *   .forEach(System.out::println);
 *
 * // Get specific rank in constrained product space
 * cp.constrainedProductOfDistinct(1, "A", "B")
 *   .andDistinct(1, "X", "Y", "Z")
 *   .byRanks(List.of(BigInteger.valueOf(3)))
 *   .forEach(System.out::println);
 * </pre>
 *
 * <p>
 * This class is immutable and thread-safe. All methods return new builder instances.
 * </p>
 *
 * @see SimpleProductBuilder
 * @see ConstrainedProductBuilder
 * @see LambdaConditionalProductBuilder
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class CartesianProduct {

    private final Calculator calculator;

    /**
     * Constructs a new CartesianProduct instance with a default Calculator.
     */
    public CartesianProduct() {
        this(new Calculator());
    }

    /**
     * Constructs a new CartesianProduct instance with the specified Calculator.
     *
     * @param calculator the Calculator for combinatorial computations
     */
    public CartesianProduct(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for simple Cartesian products over a set of elements.
     * <p>
     * Generates the product Aⁿ, where A is the input set and n is the number of repetitions
     * specified by subsequent {@code and()} calls. Each tuple contains one element from
     * each dimension, preserving the order of dimensions.
     * </p>
     * <p>
     * Example: simpleProductOf([A, B]).and([1, 2]) generates A×B = {[A,1], [A,2], [B,1], [B,2]}
     * </p>
     *
     * <p>
     * Every dimension is copied, so later changes to the caller's list have no effect. An empty list is
     * allowed, but an empty dimension makes the whole product empty (count 0, no tuples).
     * </p>
     *
     * @param elements the list of elements forming the first set (must not be null or contain null)
     * @return a SimpleProductBuilder for configuring additional dimensions
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        Util.validateInput(elements);
        return new SimpleProductBuilder(elements);
    }

    /**
     * Creates a builder for a simple Cartesian product over two or more dimensions.
     * <p>
     * This convenience overload is equivalent to calling {@link #simpleProductOf(List)}
     * with {@code first}, then adding {@code second} and every {@code additional} dimension
     * in order. Every dimension is snapshotted when this method is called.
     * </p>
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param additional any further dimensions, in product order
     * @return a simple product builder containing all supplied dimensions
     * @throws NullPointerException if any dimension or the additional array is null,
     *         or if any dimension contains a null element
     */
    public SimpleProductBuilder simpleProductOf(
            List<?> first, List<?> second, List<?>... additional) {
        return new SimpleProductBuilder(first, second, additional);
    }

    /**
     * Creates a builder for a Cartesian product whose dimensions are linked by pairwise rules,
     * such as {@code when(browser).is("Safari").then(os).isOneOf("macOS")}.
     * <p>
     * Add further dimensions with {@code and(list)}. Rules refer to dimensions by the same list
     * instances passed to this builder. For compound AND/OR trigger expressions, use
     * {@link #compoundProductOf(List)} instead.
     * See {@link ConditionalProductBuilder} for rule semantics and constraint-size limits.
     * </p>
     *
     * @param elements the first dimension; copied. An empty list is allowed and makes the product empty.
     * @return a ConditionalProductBuilder for adding dimensions and pairwise rules
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public ConditionalProductBuilder conditionalProductOf(List<?> elements) {
        Util.validateInput(elements);
        return new ConditionalProductBuilder(elements);
    }

    /**
     * Creates a builder for a conditional Cartesian product with two or more dimensions.
     * <p>
     * This is a convenience overload equivalent to calling {@link #conditionalProductOf(List)}
     * with the first dimension and then adding each remaining dimension through
     * {@link ConditionalProductBuilder#and(List)}. The dimensions retain the supplied order.
     * </p>
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param additional any further dimensions, in product order
     * @return a conditional product builder containing all supplied dimensions
     * @throws NullPointerException if any dimension is null or contains a null element
     */
    public ConditionalProductBuilder conditionalProductOf(
            List<?> first, List<?> second, List<?>... additional) {
        return new ConditionalProductBuilder(first, second, additional);
    }

    /**
     * Creates a builder for a Cartesian product constrained by compound trigger expressions.
     *
     * <p>Build a composed {@link io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition}
     * with nested {@code and(Condition)} and {@code or(Condition)} calls, then attach it with
     * {@code when(condition)}. The resulting builder exposes generation, exact counting and
     * emptiness checks; it does not expose lexicographic or rank-based operations.</p>
     *
     * @param elements the first dimension; copied
     * @return a compound conditional product builder
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public CompoundConditionalProductBuilder compoundProductOf(List<?> elements) {
        return new CompoundConditionalProductBuilder(elements);
    }

    /**
     * Creates a compound conditional product builder with two or more dimensions.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param additional further dimensions, in product order
     * @return a compound conditional product builder containing all supplied dimensions
     */
    public CompoundConditionalProductBuilder compoundProductOf(
            List<?> first, List<?> second, List<?>... additional) {
        return new CompoundConditionalProductBuilder(first, second, additional);
    }

    /**
     * Creates a strongly typed lambda-constrained builder for one dimension.
     * The predicate filters the values of that dimension.
     *
     * <p>Lambda predicates are evaluated once, while each rule is added, over the combinations of the dimensions
     * the rule covers. See {@link LambdaConditionalProductBuilder} for the cost and purity requirements.</p>
     *
     * @param elements the dimension; copied
     * @param <A> the element type
     * @return a typed lambda builder for one dimension
     * @throws NullPointerException if {@code elements} is null or contains a null element
     */
    public <A> LambdaConditionalProductBuilder.Typed1<A> withLambda(List<A> elements) {
        return new LambdaConditionalProductBuilder.Typed1<>(new ConditionalProductBuilder(elements));
    }

    /**
     * Creates a strongly typed lambda-constrained builder for two dimensions.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param <A> the element type of the first dimension
     * @param <B> the element type of the second dimension
     * @return a typed lambda builder for two dimensions
     * @throws NullPointerException if a dimension is null or contains a null element
     */
    public <A, B> LambdaConditionalProductBuilder.Typed2<A, B> withLambda(
            List<A> first, List<B> second) {
        return new LambdaConditionalProductBuilder.Typed2<>(new ConditionalProductBuilder(first, second));
    }

    /**
     * Creates a strongly typed lambda-constrained builder for three dimensions.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param dimension3 the third dimension
     * @param <A> the element type of the first dimension
     * @param <B> the element type of the second dimension
     * @param <C> the element type of the third dimension
     * @return a typed lambda builder for three dimensions
     * @throws NullPointerException if a dimension is null or contains a null element
     */
    public <A, B, C> LambdaConditionalProductBuilder.Typed3<A, B, C> withLambda(
            List<A> first, List<B> second, List<C> dimension3) {
        return new LambdaConditionalProductBuilder.Typed3<>(
                new ConditionalProductBuilder(first, second, dimension3));
    }

    /**
     * Creates a strongly typed lambda-constrained builder for four dimensions.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param dimension3 the third dimension
     * @param dimension4 the fourth dimension
     * @param <A> the element type of the first dimension
     * @param <B> the element type of the second dimension
     * @param <C> the element type of the third dimension
     * @param <D> the element type of the fourth dimension
     * @return a typed lambda builder for four dimensions
     * @throws NullPointerException if a dimension is null or contains a null element
     */
    public <A, B, C, D> LambdaConditionalProductBuilder.Typed4<A, B, C, D> withLambda(
            List<A> first, List<B> second, List<C> dimension3, List<D> dimension4) {
        return new LambdaConditionalProductBuilder.Typed4<>(
                new ConditionalProductBuilder(first, second, dimension3, dimension4));
    }

    /**
     * Creates a strongly typed lambda-constrained builder for five dimensions.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param dimension3 the third dimension
     * @param dimension4 the fourth dimension
     * @param dimension5 the fifth dimension
     * @param <A> the element type of the first dimension
     * @param <B> the element type of the second dimension
     * @param <C> the element type of the third dimension
     * @param <D> the element type of the fourth dimension
     * @param <E> the element type of the fifth dimension
     * @return a typed lambda builder for five dimensions
     * @throws NullPointerException if a dimension is null or contains a null element
     */
    public <A, B, C, D, E> LambdaConditionalProductBuilder.Typed5<A, B, C, D, E> withLambda(
            List<A> first, List<B> second, List<C> dimension3, List<D> dimension4, List<E> dimension5) {
        return new LambdaConditionalProductBuilder.Typed5<>(
                new ConditionalProductBuilder(first, second, dimension3, dimension4, dimension5));
    }

    /**
     * Creates an array-based lambda-constrained builder for more than five dimensions.
     * Its predicates receive the values as an {@code Object} array, in declaration order. Products with two
     * to five dimensions select the strongly typed overloads above instead.
     *
     * @param first the first dimension
     * @param second the second dimension
     * @param additional the remaining dimensions, in product order
     * @return an array-based lambda builder
     * @throws NullPointerException if a dimension is null or contains a null element
     */
    public LambdaConditionalProductBuilder.ArrayBased withLambda(
            List<?> first, List<?> second, List<?>... additional) {
        return new LambdaConditionalProductBuilder.ArrayBased(
                new ConditionalProductBuilder(first, second, additional));
    }

    /**
     * Creates a builder for constrained Cartesian products with a specified tuple length.
     * <p>
     * Generates the product A₁ × A₂ × ... × Aₖ where each dimension can be constrained using
     * methods like {@code andDistinct()}, {@code andMultiSelect()}, or {@code andInRange()}.
     * The first dimension consists of combinations of size {@code quantity} from the input set.
     * </p>
     * <p>
     * Example: constrainedProductOf(2, [A, B, C]) creates a first dimension of all 2-element
     * combinations from [A, B, C]: [A,B], [A,C], [B,C]
     * </p>
     *
     * @param quantity the size of combinations for the first dimension (must be ≥ 0)
     * @param elements the list of elements for the first dimension
     * @return a ConstrainedProductBuilder for configuring additional constrained dimensions
     * @deprecated Use {@link #constrainedProductOfDistinct(int, List)} instead for clearer intent
     */
    @Deprecated(since = "4.0.0", forRemoval = true)
    public ConstrainedProductBuilder constrainedProductOf(int quantity, List<?> elements) {
        return new ConstrainedProductBuilder(quantity, elements, false, calculator);
    }

    /**
     * Creates a builder for constrained Cartesian products with distinct combinations in the first dimension.
     * <p>
     * The first dimension consists of distinct combinations of size {@code quantity} from the input set.
     * Subsequent dimensions can be configured using {@code andDistinct()}, {@code andMultiSelect()}, 
     * or {@code andInRange()} methods.
     * </p>
     *
     * @param quantity the size of distinct combinations for the first dimension (must be ≥ 0)
     * @param elements the list of elements for the first dimension
     * @return a ConstrainedProductBuilder for configuring additional constrained dimensions
     */
    public ConstrainedProductBuilder constrainedProductOfDistinct(int quantity, List<?> elements) {
        Util.validateInput(elements);
        Util.validateNotNegative(quantity, "quantity");
        return new ConstrainedProductBuilder(quantity, elements, false, calculator);
    }

    /**
     * Creates a builder for constrained Cartesian products with multi-select combinations in the first dimension.
     * <p>
     * The first dimension consists of combinations with repetition allowed of size {@code quantity} 
     * from the input set. Subsequent dimensions can be configured using {@code andDistinct()}, 
     * {@code andMultiSelect()}, or {@code andInRange()} methods.
     * </p>
     *
     * @param quantity the size of multi-select combinations for the first dimension (must be ≥ 0)
     * @param elements the list of elements for the first dimension
     * @return a ConstrainedProductBuilder for configuring additional constrained dimensions
     */
    public ConstrainedProductBuilder constrainedProductOfMultiSelect(int quantity, List<?> elements) {
        Util.validateInput(elements);
        Util.validateNotNegative(quantity, "quantity");
        return new ConstrainedProductBuilder(quantity, elements, true, calculator);
    }

    /**
     * Creates a builder for constrained Cartesian products with subset range in the first dimension.
     * <p>
     * The first dimension consists of all subsets of the input set with sizes between {@code from} 
     * and {@code to} (inclusive). Subsequent dimensions can be configured using {@code andDistinct()}, 
     * {@code andMultiSelect()}, or {@code andInRange()} methods.
     * </p>
     *
     * @param from     the minimum subset size (inclusive, must be ≥ 0)
     * @param to       the maximum subset size (inclusive, must be ≥ from)
     * @param elements the list of elements for the first dimension
     * @return a ConstrainedProductBuilder for configuring additional constrained dimensions
     * @throws IllegalArgumentException if from &lt; 0, to &lt; from, or from > elements.size()
     */
    public ConstrainedProductBuilder constrainedProductOfInRange(int from, int to, List<?> elements) {
        Util.validateInput(elements);
        return new ConstrainedProductBuilder(from, to, elements, calculator);
    }
}
