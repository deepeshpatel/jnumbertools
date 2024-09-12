package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for generating simple product combinations.
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 * @param <T> The type of elements in the lists.
 * @author Deepesh Patel
 */
public class SimpleProductMth<T> implements Iterable<List<T>> {

    private final long m;
    private final List<List<T>> elements;
    private final long start;

    /**
     * Constructor to initialize the simple product method.
     * @param m The step size for iteration.
     * @param start The starting position.
     * @param elements A list of lists containing elements of type T.
     */
    SimpleProductMth(long m, long start, List<List<T>> elements) {
        this.elements = elements;
        this.m = m;
        this.start = start;
        // TODO: implement starting index
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

        private final int[] indices = new int[elements.size()];
        private boolean hasNext;

        public Itr() {
            this.hasNext = nextMthCartesian(indices, start);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            List<T> result = indicesToList(elements, indices);
            hasNext = nextMthCartesian(indices, m);
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

        /**
         * Computes the next Cartesian product combination.
         * @param indices The current indices array.
         * @param k The step size for iteration.
         * @return True if there are no more combinations, false otherwise.
         */
        private boolean nextMthCartesian(int[] indices, long k) {
            long nextK = k;

            for (int i = indices.length - 1; i >= 0; i--) {
                int base = elements.get(i).size();
                long v = (indices[i] + nextK) % base;
                nextK = (indices[i] + nextK) / base;
                indices[i] = (int) v;
            }
            return nextK == 0;
        }
    }
}
