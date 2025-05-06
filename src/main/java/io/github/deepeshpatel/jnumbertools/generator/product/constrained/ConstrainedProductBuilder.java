/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;
import io.github.deepeshpatel.jnumbertools.generator.base.EveryMthIterable;
import io.github.deepeshpatel.jnumbertools.generator.product.ProductForSequence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A builder class for constructing constrained products of combinations and subsets.
 * <p>
 * This class allows the creation of constrained products by combining different types of combinations and subsets.
 * You can add distinct combinations, multi-select (repetitive) combinations, or subsets (of a given range)
 * from various input lists. The final constrained product can be generated in lexicographical order
 * (via {@link #lexOrder()}), at specific intervals (via {@link #lexOrderMth(long, long)}), or based on a custom
 * sequence of ranks (via {@link #fromSequence(Iterable)}).
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
//TODO: [1] Implement Builder interface hera and in SimpleProductBuildr to be in sync with other builders
// [2] and add choice and sample methods
// and modify test class accordingly
// and remove mth as sequence is available
public final class ConstrainedProductBuilder {

    private final List<Builder> builders = new ArrayList<>();
    private final Calculator calculator;

    /**
     * Constructs a ConstrainedProductBuilder with an initial combination of size {@code n}.
     *
     * @param n          the size of the initial combinations
     * @param elements   the list of elements to create combinations from (may be null or empty)
     * @param calculator the calculator used for combinatorial computations
     */
    public ConstrainedProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && n > 0 ? new EmptyBuilder() : new Combinations(calculator).unique(n, elements));
    }

    /**
     * Adds a distinct combination of the specified quantity to the builder.
     *
     * @param quantity the size of the distinct combinations
     * @param elements the list of elements to create combinations from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andDistinct(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder() : new Combinations(calculator).unique(quantity, elements));
        return this;
    }

    /**
     * Adds a multi-select (repetitive) combination of the specified quantity to the builder.
     *
     * @param quantity the size of the multi-select combinations
     * @param elements the list of elements to create combinations from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andMultiSelect(int quantity, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
        builders.add(elements.isEmpty() && quantity > 0 ? new EmptyBuilder() : new Combinations(calculator).repetitive(quantity, elements));
        return this;
    }

    /**
     * Adds a subset of elements within a specified size range to the builder.
     *
     * @param from     the minimum size of the subsets
     * @param to       the maximum size of the subsets
     * @param elements the list of elements to create subsets from (may be null or empty)
     * @return the current instance of ConstrainedProductBuilder for method chaining
     */
    public ConstrainedProductBuilder andInRange(int from, int to, List<?> elements) {
        elements = elements == null ? Collections.emptyList() : elements;
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
     * Builds and returns a ProductForSequence with all added combinations and subsets, generating every mᵗʰ product.
     *
     * @param m     the interval to select every mᵗʰ permutation (0‑based)
     * @param start the starting position
     * @return a ProductForSequence containing the generated combinations and subsets at the specified intervals
     */
    public ProductForSequence lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Builds and returns a ProductForSequence with all added combinations and subsets, generating every mᵗʰ product.
     *
     * @param m     the interval to select every mᵗʰ permutation as a {@link BigInteger}
     * @param start the starting position as a {@link BigInteger}
     * @return a ProductForSequence containing the generated combinations and subsets at the specified intervals
     */
    public ProductForSequence lexOrderMth(BigInteger m, BigInteger start) {
        BigInteger maxCount = builders.isEmpty() || builders.stream().allMatch(b -> b.count().equals(BigInteger.ZERO))
                ? BigInteger.ONE
                : builders.stream()
                .map(Builder::count)
                .reduce(BigInteger.ONE, BigInteger::multiply);
        return new ProductForSequence(builders, new EveryMthIterable(start, m, maxCount));
    }

    /**
     * Builds and returns a ProductForSequence with all added combinations and subsets, generating products
     * based on a custom sequence of ranks.
     *
     * @param ranks the iterable providing the sequence of ranks
     * @return a ProductForSequence containing the generated products for the specified ranks
     */
    public ProductForSequence fromSequence(Iterable<BigInteger> ranks) {
        return new ProductForSequence(builders, ranks);
    }

    /**
     * A special builder for empty inputs, producing one empty combination.
     */
    private static class EmptyBuilder implements Builder<Object> {
        @Override
        public BigInteger count() {
            return BigInteger.ONE;
        }

        @Override
        public AbstractGenerator<Object> lexOrder() {
            return new AbstractGenerator<Object>(Collections.emptyList()) {
                @Override
                public Iterator<List<Object>> iterator() {
                    return (Iterator<List<Object>>) Collections.emptyList();
                }
            };
        }

        @Override
        public Iterable<List<Object>> lexOrderMth(BigInteger m, BigInteger start) {
            return m.equals(BigInteger.ZERO) ? Collections.singletonList(Collections.emptyList()) : Collections.emptyList();
        }
    }
}