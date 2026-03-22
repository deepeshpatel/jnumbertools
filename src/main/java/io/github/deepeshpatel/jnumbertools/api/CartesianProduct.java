/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.api;

import io.github.deepeshpatel.jnumbertools.api.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.core.external.Calculator;
import io.github.deepeshpatel.jnumbertools.core.internal.CalculatorImpl;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.product.simple.SimpleProductBuilder;

import java.util.List;

/**
 * Generates Cartesian products of elements from given sets.
 * <p>
 * The Cartesian product of sets A₁, A₂, ..., Aₖ is the set of all ordered tuples (a₁, a₂, ..., aₖ)
 * where aᵢ ∈ Aᵢ. This class supports two types of products:
 * <ul>
 *   <li><b>Simple Product</b>: Aⁿ where A is a single set repeated n times</li>
 *   <li><b>Constrained Product</b>: A₁ × A₂ × ... × Aₖ where each Aᵢ can be a set, combination, or subset with constraints</li>
 * </ul>
 * </p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Simple Cartesian Product</h3>
 * <pre>
 * CartesianProduct cp = new CartesianProduct();
 *
 * // Product of [A, B] × [1, 2] × [X, Y]
 * cp.simpleProductOf("A", "B")
 *   .and(1, 2)
 *   .and("X", "Y")
 *   .lexOrder()
 *   .forEach(System.out::println);
 * // Output: [A,1,X], [A,1,Y], [A,2,X], [A,2,Y], [B,1,X], [B,1,Y], [B,2,X], [B,2,Y]
 *
 * // Every 2nd product starting from rank 1
 * cp.simpleProductOf(0, 1, 2)
 *   .and(0, 1, 2)
 *   .lexOrderMth(2, 1)
 *   .forEach(System.out::println);
 *
 * // Random sample of 3 products without replacement
 * cp.simpleProductOf("A", "B", "C")
 *   .and("X", "Y")
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
 * @see AllExamples
 * @see SimpleProductBuilder
 * @see ConstrainedProductBuilder
 * @see <a href="overview.html">Overview</a> for detailed examples and usage scenarios
 * @author Deepesh Patel
 */
public final class CartesianProduct {

    private final Calculator calculator;

    /**
     * Constructs a new CartesianProduct instance with a default Calculator.
     */
    public CartesianProduct() {
        this(new CalculatorImpl());
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
     * Creates a builder for a 0-dimensional simple Cartesian product (nullary product).
     * <p>
     * Generates a single empty tuple [[]]. This represents the mathematical product of zero sets.
     * </p>
     *
     * @return a SimpleProductBuilder initialized with no dimensions
     */
    public SimpleProductBuilder simpleProductOf() {
        return new SimpleProductBuilder();
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
     * @param elements the list of elements forming the first set (null treated as empty)
     * @return a SimpleProductBuilder for configuring additional dimensions
     * @throws NullPointerException if elements is null
     */
    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        Util.validateInput(elements);
        return new SimpleProductBuilder(elements);
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
    @Deprecated(since = "3.0.2", forRemoval = true)
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
     * @throws NullPointerException if elements is null
     * @throws IllegalArgumentException if quantity < 0
     */
    public ConstrainedProductBuilder constrainedProductOfDistinct(int quantity, List<?> elements) {
        Util.validateInput(elements);
        validateNotNegative(quantity);
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
     * @throws NullPointerException if elements is null
     * @throws IllegalArgumentException if quantity < 0
     */
    public ConstrainedProductBuilder constrainedProductOfMultiSelect(int quantity, List<?> elements) {
        Util.validateInput(elements);
        validateNotNegative(quantity);
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
     * @throws NullPointerException if elements is null
     * @throws IllegalArgumentException if from < 0, to < from, or from > elements.size()
     */
    public ConstrainedProductBuilder constrainedProductOfInRange(int from, int to, List<?> elements) {
        Util.validateInput(elements);
        return new ConstrainedProductBuilder(from, to, elements, calculator);
    }

    private static void validateNotNegative(int n) {
        if(n < 0)   throw new IllegalArgumentException("quantity must be ≥ 0");
    }
}
