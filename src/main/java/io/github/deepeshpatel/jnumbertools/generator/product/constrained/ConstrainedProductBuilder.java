/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder class for constructing constrained products of combinations and subsets.
 * <p>
 * This class allows the creation of constrained products by combining different types of combinations and subsets.
 * You can add distinct combinations, multi-select (repetitive) combinations, or subsets (of a given range)
 * from various input lists. The final constrained product can be generated either in lexicographical order
 * (via {@link #lexOrder()}) or at specific intervals (via {@link #lexOrderMth(long, long)}).
 * </p>
 * <p>
 * <strong>Example usage:</strong>
 * <pre><code class="language-java">
 * ConstrainedProductBuilder builder = new ConstrainedProductBuilder(2, List.of("A", "B", "C"), calculator);
 * builder.andDistinct(3, List.of("X", "Y"))
 *        .andMultiSelect(2, List.of("P", "Q"))
 *        .lexOrder();
 * </code></pre>
 * This example creates a constrained product consisting of combinations of size 2 from the first list,
 * distinct combinations of size 3 from the second list, and multi-select combinations of size 2 from the third list.
 * </p>
 *
 * @since 1.0.3
 * @author Deepesh Patel
 * @version 1.0.3
 */

@SuppressWarnings({"unchecked", "rawtypes"})


public final class ConstrainedProductBuilder {

    private final List<Builder> builders = new ArrayList<>();
    private final Calculator calculator;

    /**
     * Constructs a ConstrainedProductBuilder with an initial combination of size {@code n}.
     *
     * @param n          the size of the initial combinations
     * @param elements   the list of elements to create combinations from
     * @param calculator the calculator used for combinatorial computations
     */
    public ConstrainedProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        builders.add(new Combinations(calculator).unique(n, elements));
    }

    /**
     * Adds a distinct combination of the specified quantity to the builder.
     *
     * @param quantity the size of the distinct combinations
     * @param elements the list of elements to create combinations from
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).unique(quantity, elements));
        return this;
    }

    /**
     * Adds a multi-select (repetitive) combination of the specified quantity to the builder.
     *
     * @param quantity the size of the multi-select combinations
     * @param elements the list of elements to create combinations from
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).repetitive(quantity, elements));
        return this;
    }

    /**
     * Adds a subset of elements within a specified size range to the builder.
     *
     * @param from     the minimum size of the subsets
     * @param to       the maximum size of the subsets
     * @param elements the list of elements to create subsets from
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        builders.add(new Subsets(calculator).of(elements).inRange(from, to));
        return this;
    }

    /**
     * Builds and returns a ConstrainedProduct with all added combinations and subsets in lexicographical order.
     *
     * @return a ConstrainedProduct containing all the generated combinations and subsets
     */
    public ConstrainedProduct lexOrder() {
        List<List<List>> all = new ArrayList<>();
        for (var e : builders) {
            all.add(e.lexOrder().stream().toList());
        }
        return new ConstrainedProduct(all);
    }

    /**
     * Builds and returns a ConstrainedProductMth with all added combinations and subsets, generating every mᵗʰ product.
     *
     * @param m     the interval to select every mᵗʰ permutation (0‑based)
     * @param start the starting position
     * @return a ConstrainedProductMth containing the generated combinations and subsets at the specified intervals
     */
    public ConstrainedProductMth lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Builds and returns a ConstrainedProductMth with all added combinations and subsets, generating every mᵗʰ product.
     *
     * @param m     the interval to select every mᵗʰ permutation as a {@link BigInteger}
     * @param start the starting position as a {@link BigInteger}
     * @return a ConstrainedProductMth containing the generated combinations and subsets at the specified intervals
     */
    public ConstrainedProductMth lexOrderMth(BigInteger m, BigInteger start) {
        return new ConstrainedProductMth(m, start, builders);
    }
}
