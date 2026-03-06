/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.examples.AllExamples;
import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

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
 * cp.constrainedProductOf(1, "Small", "Medium", "Large")
 *   .andDistinct(2, "Ricotta", "Mozzarella", "Cheddar")
 *   .andMultiSelect(2, "Tomato", "White", "Green")
 *   .andInRange(1, 5, "tomato", "capsicum", "onion", "paneer", "corn")
 *   .lexOrder()
 *   .forEach(System.out::println);
 *
 * // Get specific rank in constrained product space
 * cp.constrainedProductOf(1, "A", "B")
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
     * @param elements the list of elements forming the first set (null treated as empty)
     * @return a SimpleProductBuilder for configuring additional dimensions
     */
    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        return new SimpleProductBuilder(elements);
    }

    /**
     * Creates a builder for simple Cartesian products over a varargs set of elements.
     * <p>
     * Convenience method for {@link #simpleProductOf(List)}.
     * </p>
     *
     * @param elements the varargs list of elements forming the first set
     * @param <E> the type of elements
     * @return a SimpleProductBuilder for configuring additional dimensions
     */
    @SafeVarargs
    public final <E> SimpleProductBuilder simpleProductOf(E... elements) {
        return simpleProductOf(List.of(elements));
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
     */
    public ConstrainedProductBuilder constrainedProductOf(int quantity, List<?> elements) {
        return new ConstrainedProductBuilder(quantity, elements, calculator);
    }

    /**
     * Creates a builder for constrained Cartesian products with a specified tuple length.
     * <p>
     * Convenience method for {@link #constrainedProductOf(int, List)}.
     * </p>
     *
     * @param quantity the size of combinations for the first dimension (must be ≥ 0)
     * @param elements the varargs list of elements for the first dimension
     * @param <E> the type of elements
     * @return a ConstrainedProductBuilder for configuring additional constrained dimensions
     */
    @SafeVarargs
    public final <E> ConstrainedProductBuilder constrainedProductOf(int quantity, E... elements) {
        return constrainedProductOf(quantity, List.of(elements));
    }
}
