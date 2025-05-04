/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.product.constrained;

import io.github.deepeshpatel.jnumbertools.generator.product.simple.CartesianProductUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a constrained product of multiple sets of combinations or subsets.
 * <p>
 * This class provides an iterator that generates the Cartesian product of the lists contained
 * within the constrained product. Each element of the product is obtained by combining one element from each
 * inner list, with the combinations generated in lexicographical order of the indices.
 * </p>
 * <p>
 * <strong>Note:</strong> Instances of this class are intended to be created via a builder and therefore do not have a public constructor.
 * </p>
 *
 * @since 1.0.3
 * @author Deepesh Patel

 */

@SuppressWarnings({"rawtypes"})

public final class ConstrainedProduct implements Iterable<List> {

    private final List<List<List>> elements;

    /**
     * Constructs a {@code ConstrainedProduct} with the specified list of combinations or subsets.
     *
     * @param elements a list of lists where each inner list contains lists of elements; each inner list represents a dimension of the product.
     */
    ConstrainedProduct(List<List<List>> elements) {
        this.elements = elements;
    }

    /**
     * Computes the total number of possible combinations in the constrained product.
     *
     * @return the total number of possible combinations, computed as the product of the sizes of the inner lists.
     */
    public long count() {
        long count = 1;
        for (var e : elements) {
            count *= e.size();
        }
        return count;
    }

    @Override
    public Iterator<List> iterator() {
        return new Itr();
    }

    /**
     * Returns a sequential {@code Stream} with this constrained product as its source.
     *
     * @return a stream of combinations from the Cartesian product
     */
    public Stream<List> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Iterator implementation for generating the Cartesian product combinations.
     */
    private class Itr implements Iterator<List> {

        final int[] current = new int[elements.size()];
        boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            var result = indicesToList();
            hasNext = CartesianProductUtils.createNext(current, elements);
            return result;
        }

        /**
         * Converts the current indices into a list of elements by taking the corresponding element from each inner list.
         *
         * @return a list representing one combination from the Cartesian product
         */
        private List<Object> indicesToList() {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                list.addAll(elements.get(i).get(current[i]));
            }
            return list;
        }
    }
}
