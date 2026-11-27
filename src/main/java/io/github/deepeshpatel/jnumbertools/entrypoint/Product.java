/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.builders.CartesianProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Static entry point for Cartesian product generation.

 *
 * <h2>Simple products</h2>
 * <pre>{@code
 * Product.simple(os, browsers, networks).lexOrder();
 * }</pre>
 *
 * <h2>Constrained products</h2>
 * <pre>{@code
 * Calculator calculator = new Calculator();
 * Product.constrained(calculator)
 *     .ofDistinct(2, colors)
 *     .andMultiSelect(3, sizes)
 *     .lexOrder();
 * }</pre>
 *
 * <h2>Conditional products</h2>
 * <pre>{@code
 * Product.conditional(os, browsers, networks)
 *     .when(os).is("Linux").then(browsers).isOneOf("Firefox", "Safari")
 *     .lexOrder();
 *
 * Product.conditional(os, browsers, networks)
 *     .withLambda(os, browsers, networks)
 *     .when((system, browser, network) ->
 *         !system.equals("Linux") || browser.equals("Firefox") || browser.equals("Safari"))
 *     .lexOrder();
 * }</pre>
 *
 * @see CartesianProductBuilder
 * @author Deepesh Patel
 */
public final class Product {

    private Product() {
        // Static factory class.
    }

    /**
     * Creates a simple Cartesian product over the supplied dimensions.
     *
     * @param dimensions product dimensions, in tuple order
     * @return a simple product builder
     * @throws NullPointerException if the array or any dimension is null
     * @throws IllegalArgumentException if no dimensions are supplied
     */
    public static SimpleProductBuilder simple(List<?>... dimensions) {
        Objects.requireNonNull(dimensions, "dimensions must not be null");
        if (dimensions.length == 0) {
            throw new IllegalArgumentException("At least one product dimension is required");
        }
        CartesianProductBuilder products = new CartesianProductBuilder();
        if (dimensions.length == 1) {
            return products.simpleProductOf(dimensions[0]);
        }
        return products.simpleProductOf(dimensions[0], dimensions[1],
                Arrays.copyOfRange(dimensions, 2, dimensions.length));
    }

    /**
     * Starts a constrained-product factory using a fresh calculator.
     *
     * @return a constrained-product factory
     */
    public static Constrained constrained() {
        return constrained(new Calculator());
    }

    /**
     * Starts a constrained-product factory using the supplied calculator.
     * Reusing the same calculator across factories allows its combinatorial caches to be shared.
     *
     * @param calculator calculator shared by the constrained product dimensions
     * @return a constrained-product factory
     * @throws NullPointerException if calculator is null
     */
    public static Constrained constrained(Calculator calculator) {
        return new Constrained(calculator);
    }

    /**
     * Creates a pairwise conditional-product builder over the supplied dimensions.
     *
     * @param dimensions product dimensions, in tuple order
     * @return a conditional-product builder
     * @throws NullPointerException if the array or any dimension is null
     * @throws IllegalArgumentException if no dimensions are supplied
     */
    public static ConditionalProductBuilder conditional(List<?>... dimensions) {
        Objects.requireNonNull(dimensions, "dimensions must not be null");
        if (dimensions.length == 0) {
            throw new IllegalArgumentException("At least one product dimension is required");
        }
        CartesianProductBuilder products = new CartesianProductBuilder();
        if (dimensions.length == 1) {
            return products.conditionalProductOf(dimensions[0]);
        }
        return products.conditionalProductOf(dimensions[0], dimensions[1],
                Arrays.copyOfRange(dimensions, 2, dimensions.length));
    }

    /**
     * Factory for constrained Cartesian products.
     *
     * <p>The calculator is retained and passed to every dimension created by the resulting
     * {@link ConstrainedProductBuilder}.</p>
     */
    public static final class Constrained {
        private final Calculator calculator;

        private Constrained(Calculator calculator) {
            this.calculator = Objects.requireNonNull(calculator, "calculator must not be null");
        }

        /**
         * Starts a product whose first dimension consists of distinct combinations.
         *
         * @param quantity number of elements in each combination
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofDistinct(int quantity, List<?> elements) {
            return new CartesianProductBuilder(calculator).constrainedProductOfDistinct(quantity, elements);
        }

        /**
         * Starts a product whose first dimension consists of distinct combinations.
         *
         * @param quantity number of elements in each combination
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofDistinct(int quantity, Object... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            return ofDistinct(quantity, Arrays.asList(elements));
        }

        /**
         * Starts a product whose first dimension consists of combinations with repetition.
         *
         * @param quantity number of elements in each combination
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofMultiSelect(int quantity, List<?> elements) {
            return new CartesianProductBuilder(calculator).constrainedProductOfMultiSelect(quantity, elements);
        }

        /**
         * Starts a product whose first dimension consists of combinations with repetition.
         *
         * @param quantity number of elements in each combination
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofMultiSelect(int quantity, Object... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            return ofMultiSelect(quantity, Arrays.asList(elements));
        }

        /**
         * Starts a product whose first dimension contains subsets in the inclusive size range.
         *
         * @param from minimum subset size
         * @param to maximum subset size
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofInRange(int from, int to, List<?> elements) {
            return new CartesianProductBuilder(calculator).constrainedProductOfInRange(from, to, elements);
        }

        /**
         * Starts a product whose first dimension contains subsets in the inclusive size range.
         *
         * @param from minimum subset size
         * @param to maximum subset size
         * @param elements source elements
         * @return the constrained product builder
         */
        public ConstrainedProductBuilder ofInRange(int from, int to, Object... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            return ofInRange(from, to, Arrays.asList(elements));
        }
    }
}
