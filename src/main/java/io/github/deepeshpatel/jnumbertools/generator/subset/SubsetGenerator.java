/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates all subsets of a given set within a specified size range.
 * <p>
 * This class implements {@code Iterable} to provide subsets of the input set whose sizes range
 * from {@code fromSize} (inclusive) to {@code toSize} (inclusive). Each element in the input
 * collection is treated as unique; duplicate values are not eliminated.
 * </p>
 * <p>
 * <strong>Note:</strong> Instances of this class are intended to be constructed via a builder,
 * so its constructor is package-private.
 * </p>
 *
 * @param <T> the type of elements in the subsets
 * @author Deepesh Patel
 */
public final class SubsetGenerator<T> extends AbstractGenerator<T> {

    private final int fromSize;
    private final int toSize;

    /**
     * Constructs a {@code SubsetGenerator} to generate subsets with sizes ranging from
     * {@code fromSize} to {@code toSize}.
     * <p>
     * If both {@code fromSize} and {@code toSize} are negative, no subsets will be generated.
     * If {@code fromSize} is less than or equal to zero and {@code toSize} is greater than or equal to zero,
     * one empty subset will be generated. This behavior is mathematically correct, although it might seem unintuitive.
     * </p>
     *
     * @param elements the list of elements from which subsets are generated (each item is treated as unique)
     * @param fromSize the minimum subset size to generate (inclusive)
     * @param toSize the maximum subset size to generate (inclusive); must be greater than or equal to {@code fromSize}
     */
    SubsetGenerator(List<T> elements, int fromSize, int toSize) {
        super(elements);
        this.fromSize = fromSize;
        this.toSize = toSize;
    }

    /**
     * Returns an iterator over all subsets within the specified size range.
     * <p>
     * Subsets are generated in lexicographical order, first by increasing size,
     * then by combination order within each size.
     * </p>
     * <p>
     * Example: For elements [A, B, C] with range 1 to 2, the iterator produces:
     * [A], [B], [C], [A,B], [A,C], [B,C]
     * </p>
     *
     * @return an iterator over subsets in lexicographical order;
     *         returns an iterator with a single empty subset if fromSize = 0;
     *         returns an empty iterator if fromSize > toSize or fromSize > elements.size()
     * @see SubsetBuilder#inRange(int, int)
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new OnDemandIterator(fromSize);
    }

    /**
     * Iterator class for generating subsets on demand.
     */
    class OnDemandIterator implements Iterator<List<T>> {
        int start;
        Iterator<List<T>> current;

        /**
         * Constructs an {@code OnDemandIterator} starting from the specified subset size.
         *
         * @param start the initial subset size from which to begin generating subsets
         */
        public OnDemandIterator(int start) {
            this.start = start;
            if (start > elements.size()) {
                current = Collections.emptyIterator();
            } else {
                current = new Combinations(null).unique(start, elements).lexOrder().iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (current.hasNext()) {
                return true;
            }
            if (start < toSize) {
                start++;
                if (start > elements.size()) {
                    current = Collections.emptyIterator();
                } else {
                    current = new Combinations(null).unique(start, elements).lexOrder().iterator();
                }
                return hasNext();
            }
            return false;
        }

        @Override
        public List<T> next() {
            return current.next();
        }
    }
}