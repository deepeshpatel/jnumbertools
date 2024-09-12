/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder class for constructing complex products of combinations and subsets.
 * <p>
 * This class allows the creation of complex products by combining different types of combinations and subsets.
 * The products can be generated in lexicographical order or at specific positions.
 * <p>
 * Example usage:
 * <pre>
 * ComplexProductBuilder builder = new ComplexProductBuilder(2, List.of("A", "B", "C"), calculator);
 * builder.andDistinct(3, List.of("X", "Y"))
 *        .andMultiSelect(2, List.of("P", "Q"))
 *        .lexOrder();
 * </pre>
 * This example creates a complex product consisting of combinations of size 2 from the first list,
 * distinct combinations of size 3 from the second list, and multi-select combinations of size 2 from the third list.
 *
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class ComplexProductBuilder {

    private final List<Builder> builders = new ArrayList<>();

    private final Calculator calculator;

    /**
     * Constructs a ComplexProductBuilder with an initial combination of size {@code n}.
     *
     * @param n the size of the initial combinations
     * @param elements the list of elements to create combinations from
     * @param calculator the calculator used for computations
     */
    public ComplexProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        builders.add(new Combinations(calculator).unique(n, elements));
    }

    /**
     * Adds a distinct combination of the specified quantity to the builder.
     *
     * @param quantity the size of the distinct combinations
     * @param elements the list of elements to create combinations from
     * @return the current instance of ComplexProductBuilder for method chaining
     */
    public ComplexProductBuilder andDistinct(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).unique(quantity, elements));
        return this;
    }

    /**
     * Adds a multi-select combination of the specified quantity to the builder.
     *
     * @param quantity the size of the multi-select combinations
     * @param elements the list of elements to create combinations from
     * @return the current instance of ComplexProductBuilder for method chaining
     */
    public ComplexProductBuilder andMultiSelect(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).repetitive(quantity, elements));
        return this;
    }

    /**
     * Adds a subset of elements within a specified range to the builder.
     *
     * @param from the minimum size of the subsets
     * @param to the maximum size of the subsets
     * @param elements the list of elements to create subsets from
     * @return the current instance of ComplexProductBuilder for method chaining
     */
    public ComplexProductBuilder andInRange(int from, int to, List<?> elements) {
        builders.add(new Subsets(calculator).of(elements).inRange(from, to));
        return this;
    }

    /**
     * Builds and returns a ComplexProduct with all added combinations and subsets in lexicographical order.
     *
     * @return a ComplexProduct containing all the generated combinations and subsets
     */
    public ComplexProduct lexOrder() {
        List<List<List<?>>> all = new ArrayList<>();
        for (var e : builders) {
            all.add(e.lexOrder().stream().toList());
        }
        return new ComplexProduct(all);
    }

    /**
     * Builds and returns a ComplexProductMth with all added combinations and subsets, generating every m<sup>th</sup> product.
     *
     * @param m the interval to select every m<sup>th</sup> permutation
     * @param start the starting position
     * @return a ComplexProductMth containing the generated combinations and subsets at specific intervals
     */
    public ComplexProductMth lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public ComplexProductMth lexOrderMth(BigInteger m, BigInteger start) {
        return new ComplexProductMth(m, start, builders);
    }
}
