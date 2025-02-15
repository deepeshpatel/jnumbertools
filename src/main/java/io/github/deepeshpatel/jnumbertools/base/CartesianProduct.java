/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.complex.ComplexProductBuilder;

import java.util.List;

/**
 * Provides methods for generating Cartesian products of elements.
 * This class includes functionality to compute Cartesian products with different configurations,
 * including simple and complex products.
 * The class supports:
 * <ul>
 *     <li><strong>Simple Product:</strong> Computes the Cartesian product of a list of elements, where each element can appear in any position.</li>
 *     <li><strong>Complex Product:</strong> Computes the Cartesian product with a specified quantity of elements, allowing more flexible product configurations.</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * CartesianProduct cartesianProduct = new CartesianProduct();
 * SimpleProductBuilder builder1 = cartesianProduct.simpleProductOf(Arrays.asList("A", "B", "C"));
 * ComplexProductBuilder builder2 = cartesianProduct.complexProductOf(2, "A", "B", "C");
 * </pre>
 *
 * @see io.github.deepeshpatel.jnumbertools.examples.AllExamples
 * @see <a href="overview.html">Overview</a> for more detailed examples and usage scenarios.
 * @author Deepesh Patel
 */
public final class CartesianProduct {

    private final Calculator calculator;

    /**
     * Constructs a new {@code CartesianProduct} instance with a default {@code Calculator}.
     */
    public CartesianProduct() {
        this(new Calculator());
    }

    /**
     * Constructs a new {@code CartesianProduct} instance with the specified {@code Calculator}.
     *
     * @param calculator The {@code Calculator} to use for generating Cartesian products.
     */
    public CartesianProduct(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Creates a builder for simple Cartesian products of a list of elements.
     *
     * @param elements The list of elements to compute the Cartesian product for.
     * @return A builder for simple Cartesian products.
     */
    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        return new SimpleProductBuilder(elements);
    }

    /**
     * Creates a builder for simple Cartesian products of a varargs list of elements.
     *
     * @param elements The varargs list of elements to compute the Cartesian product for.
     * @param <E> The type of elements.
     * @return A builder for simple Cartesian products.
     */
    @SafeVarargs
    public final <E> SimpleProductBuilder simpleProductOf(E... elements) {
        return simpleProductOf(List.of(elements));
    }

    /**
     * Creates a builder for complex Cartesian products of a specified quantity from a list of elements.
     *
     * @param quantity The quantity of elements for the Cartesian product.
     * @param elements The list of elements to compute the Cartesian product for.
     * @return A builder for complex Cartesian products.
     */
    public ComplexProductBuilder complexProductOf(int quantity, List<?> elements) {
        return new ComplexProductBuilder(quantity, elements, calculator);
    }

    /**
     * Creates a builder for complex Cartesian products of a specified quantity from a varargs list of elements.
     *
     * @param quantity The quantity of elements for the Cartesian product.
     * @param elements The varargs list of elements to compute the Cartesian product for.
     * @param <E> The type of elements.
     * @return A builder for complex Cartesian products.
     */
    @SafeVarargs
    public final <E> ComplexProductBuilder complexProductOf(int quantity, E... elements) {
        return complexProductOf(quantity, List.of(elements));
    }
}
