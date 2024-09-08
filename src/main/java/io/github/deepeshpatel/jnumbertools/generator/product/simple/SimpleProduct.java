package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for generating Cartesian product combinations of elements from multiple lists.
 * @param <T> The type of elements in the lists.
 */
public class SimpleProduct<T> implements Iterable<List<T>> {

    private final List<List<T>> elements;

    /**
     * Constructor to initialize the simple product method.
     * @param elements A list of lists containing elements of type T.
     */
    public SimpleProduct(List<List<T>> elements) {
        this.elements = elements;
    }

    /**
     * Returns a stream of lists of type T.
     * @return A stream of lists.
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        final int[] current = new int[elements.size()];
        boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            List<T> result = indicesToList(elements, current);
            hasNext = CartesianProductUtils.createNext(current, elements);
            return result;
        }

        /**
         * Converts the current indices to a list of elements.
         * @param elements The list of lists containing elements.
         * @param indices The current indices.
         * @return A list of elements corresponding to the indices.
         */
        protected List<T> indicesToList(List<List<T>> elements, int[] indices) {
            List<T> list = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).get(indices[i]));
            }
            return list;
        }
    }
}
