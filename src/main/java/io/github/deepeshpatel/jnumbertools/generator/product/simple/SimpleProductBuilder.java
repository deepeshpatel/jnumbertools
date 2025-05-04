package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating instances of {@link SimpleProduct} and {@link SimpleProductMth}.
 * <p>
 * This builder allows you to add multiple lists of elements and then generate the Cartesian product.
 * You can either generate the complete product in lexicographical order using {@link #lexOrder()},
 * or directly obtain every mᵗʰ product starting from a given index using {@link #lexOrderMth(long, long)}.
 * </p>
 *
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */

@SuppressWarnings({"rawtypes"})


public final class SimpleProductBuilder {

    private final List<List> allLists = new ArrayList<>();

    /**
     * Initializes the builder with an initial list of elements.
     *
     * @param elements the initial list of elements
     */
    public SimpleProductBuilder(List elements) {
        allLists.add(elements);
    }

    /**
     * Adds another list of elements to the builder.
     *
     * @param elements the list of elements to add
     * @return this builder instance for chaining
     */
    public SimpleProductBuilder and(List elements) {
        allLists.add(elements);
        return this;
    }

    /**
     * Adds a varargs list of elements to the builder.
     *
     * @param elements the elements to add
     * @return this builder instance for chaining
     */

    public SimpleProductBuilder and(Object... elements) {
        return and(List.of(elements));
    }

    /**
     * Creates a {@link SimpleProduct} instance with the lists of elements added to the builder.
     * <p>
     * The resulting product contains all combinations in lexicographical order.
     * </p>
     *
     * @return a new {@link SimpleProduct} instance
     */
    public SimpleProduct lexOrder() {
        return new SimpleProduct(allLists);
    }

    /**
     * Creates a {@link SimpleProductMth} instance with the lists of elements added to the builder.
     * <p>
     * The generated product will start from the specified starting index and produce every mᵗʰ product in lexicographical order.
     * </p>
     *
     * @param m     the mᵗʰ product to start from (0-based)
     * @param start the starting index for generating the product
     * @return a new {@link SimpleProductMth} instance
     */
    public SimpleProductMth lexOrderMth(long m, long start) {
        return new SimpleProductMth(m, start, allLists);
    }
}
