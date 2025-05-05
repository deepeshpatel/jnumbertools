package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for generating Cartesian product combinations from multiple lists.
 * <p>
 * This class produces all possible tuples (combinations) where each tuple contains one element from each list.
 * The Cartesian product is generated in lexicographical order based on the indices of the input lists.
 * Each inner list supplies the values for one dimension of the product.
 * </p>
 * <p>
 * <strong>Note:</strong> Instances of this class are intended to be created via a builder and hence do not have a public constructor.
 * </p>
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */

@SuppressWarnings({"unchecked", "rawtypes"})


public final class SimpleProduct implements Iterable<List> {

    private final List<List> elements;

    /**
     * Constructs a new {@code SimpleProduct} instance with the specified list of lists.
     *
     * @param elements a list of lists containing elements of type {@code T}. Each inner list represents the set of values for one dimension.
     */
    SimpleProduct(List<List> elements) {
        this.elements = elements;
    }

    /**
     * Returns a sequential {@code Stream} with this Cartesian product as its source.
     *
     * @return a stream of lists representing the Cartesian product combinations.
     */
    public Stream<List> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<List> iterator() {
        return new Itr();
    }

    /**
     * An iterator that generates Cartesian product combinations on demand.
     */
    private class Itr implements Iterator<List> {

        /**
         * An array of indices representing the current tuple in the Cartesian product.
         */
        final int[] current = new int[elements.size()];

        /**
         * Flag indicating whether there are more tuples to generate.
         */
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
            List result = indicesToList(elements, current);
            // Update the current indices to the next combination using CartesianProductUtils.
            hasNext = CartesianProductUtils.createNext(current, elements);
            return result;
        }

        /**
         * Converts the current indices into a list of elements by selecting the element at each index from its corresponding list.
         *
         * @param elements the list of lists containing the possible elements for each dimension.
         * @param indices  the current indices for each list.
         * @return a list of elements corresponding to the current combination of indices.
         */
        protected List indicesToList(List<List> elements, int[] indices) {
            List list = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).get(indices[i]));
            }
            return List.copyOf(list);
        }
    }
}
