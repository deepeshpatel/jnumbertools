package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder for creating instances of SimpleProduct and SimpleProductMth.
 * Allows adding lists of elements and generating permutations.
 *
 * @param <E> the type of elements in the lists
 */
public final class SimpleProductBuilder<E> {

    private final List<List<E>> allLists = new ArrayList<>();

    /**
     * Initializes the builder with a list of elements.
     *
     * @param elements the initial list of elements
     */
    public SimpleProductBuilder(List<E> elements) {
        allLists.add(elements);
    }

    /**
     * Adds another list of elements to the builder.
     *
     * @param elements the list of elements to add
     * @return this builder instance for chaining
     */
    public SimpleProductBuilder<E> and(List<E> elements) {
        allLists.add(elements);
        return this;
    }

    /**
     * Adds a varargs list of elements to the builder.
     *
     * @param elements the elements to add
     * @return this builder instance for chaining
     */
    @SafeVarargs
    public final SimpleProductBuilder<E> and(E... elements) {
        return and(List.of(elements));
    }

    /**
     * Creates a SimpleProduct with the lists of elements.
     *
     * @return a new SimpleProduct instance
     */
    public SimpleProduct<E> lexOrder() {
        return new SimpleProduct<>(allLists);
    }

    /**
     * Creates a SimpleProductMth with the lists of elements.
     *
     * @param m     the mth element to start from
     * @param start the starting index
     * @return a new SimpleProductMth instance
     */
    public SimpleProductMth<?> lexOrderMth(long m, long start) {
        return new SimpleProductMth<>(m, start, allLists);
    }
}
