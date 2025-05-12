/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

import java.util.List;

/**
 * Generates Cartesian products of elements from given sets.
 * <p>
 * The Cartesian product of sets A₁, A₂, ..., Aₖ is the set of all ordered tuples (a₁, a₂, ..., aₖ)
 * where aᵢ ∈ Aᵢ. This class supports:
 * <p>
 * - Simple Product: Computes Aⁿ, where A is a single set of elements repeated n times.
 * - Constrained Product: Computes A₁ × A₂ × ... × Aₖ, where k is a specified tuple length and each Aᵢ is a set of elements.
 * </p>
 * Example usage:
 * <pre>
 * CartesianProduct cartesianProduct = new CartesianProduct();
 * SimpleProductBuilder builder1 = cartesianProduct.simpleProductOf(Arrays.asList("A", "B", "C"));
 * ConstrainedProductBuilder builder2 = cartesianProduct.constrainedProductOf(2, "A", "B", "C");
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
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
     * specified in the builder.
     * </p>
     *
     * @param elements the list of elements forming the set
     * @return a SimpleProductBuilder for configuring the product
     */
    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        return new SimpleProductBuilder(elements, calculator);
    }

    /**
     * Creates a builder for simple Cartesian products over a varargs set of elements.
     * <p>
     * Generates the product Aⁿ, where A is the input set and n is the number of repetitions
     * specified in the builder.
     * </p>
     *
     * @param elements the varargs list of elements forming the set
     * @param <E> the type of elements
     * @return a SimpleProductBuilder for configuring the product
     */
    @SafeVarargs
    public final <E> SimpleProductBuilder simpleProductOf(E... elements) {
        return simpleProductOf(List.of(elements));
    }

    /**
     * Creates a builder for constrained Cartesian products with a specified tuple length.
     * <p>
     * Generates the product A₁ × A₂ × ... × Aₖ, where k is the quantity (tuple length) and
     * each Aᵢ is the input set of elements.
     * </p>
     *
     * @param quantity the tuple length (number of sets, k)
     * @param elements the list of elements forming each set
     * @return a ConstrainedProductBuilder for configuring the product
     */
    public ConstrainedProductBuilder constrainedProductOf(int quantity, List<?> elements) {
        return new ConstrainedProductBuilder(quantity, elements, calculator);
    }

    /**
     * Creates a builder for constrained Cartesian products with a specified tuple length.
     * <p>
     * Generates the product A₁ × A₂ × ... × Aₖ, where k is the quantity (tuple length) and
     * each Aᵢ is the input set of elements.
     * </p>
     *
     * @param quantity the tuple length (number of sets, k)
     * @param elements the varargs list of elements forming each set
     * @param <E> the type of elements
     * @return a ConstrainedProductBuilder for configuring the product
     */
    @SafeVarargs
    public final <E> ConstrainedProductBuilder constrainedProductOf(int quantity, E... elements) {
        return constrainedProductOf(quantity, List.of(elements));
    }
}