package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class for generating Cartesian product combinations with a given step.
 * <p>
 * This class produces every mᵗʰ product combination starting from a specified position.
 * It is intended to be created via a builder and therefore does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the lists.
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class SimpleProductMth<T> implements Iterable<List<T>> {

    private final long m;
    private final List<List<T>> elements;
    private final long start;

    /**
     * Package-private constructor to initialize the simple product with a step.
     *
     * @param m        the step size for iteration (i.e. the mᵗʰ product will be generated)
     * @param start    the starting position
     * @param elements a list of lists containing elements of type T
     */
    SimpleProductMth(long m, long start, List<List<T>> elements) {
        this.elements = elements;
        this.m = m;
        this.start = start;
        // TODO: implement starting index
    }

    /**
     * Returns a sequential {@code Stream} with this Cartesian product as its source.
     *
     * @return a stream of lists of type T.
     */
    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    /**
     * Iterator that generates every mᵗʰ Cartesian product combination.
     */
    private class Itr implements Iterator<List<T>> {

        private final int[] indices = new int[elements.size()];
        private boolean hasNext;

        public Itr() {
            // Initialize the indices based on the provided starting position.
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
            // Advance the indices by the step size m.
            hasNext = nextMthCartesian(indices, m);
            return result;
        }

        /**
         * Converts the current indices into a list of elements by selecting the element
         * at each index from its corresponding list.
         *
         * @param elements the list of lists containing elements
         * @param indices  the current indices
         * @return a list of elements corresponding to the indices.
         */
        protected final List<T> indicesToList(List<List<T>> elements, int[] indices) {
            List<T> list = new ArrayList<>(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).get(indices[i]));
            }
            return list;
        }

        /**
         * Computes the next Cartesian product combination by advancing the current indices
         * as if they represent a number in base‑<em>n</em> (where <em>n</em> is the size of each inner list),
         * adding the given step.
         * <p>
         * This method updates the {@code indices} array in place and returns true if the indices
         * were successfully updated to a valid next combination (i.e. no overflow occurred),
         * and false if the addition produced an overflow, indicating that there are no more combinations.
         * </p>
         *
         * @param indices the current indices array
         * @param k       the step size to add (e.g. {@code m} or {@code start})
         * @return true if a valid next combination was produced; false if the last combination has been reached.
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
