/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Iterator;
import java.util.List;

/**
 * Generates all subsets of a set within a specified size range.
 * <p>
 * This class implements {@link Iterable} to provide subsets of the input set with sizes ranging from {@code fromSize} to {@code toSize}.
 * It treats each item in the collection as unique and does not check for duplicates in the input collection.
 *
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the subsets
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class SubsetGenerator<T> extends AbstractGenerator<T> {

    private final int fromSize;
    private final int toSize;

    /**
     * Constructs a {@link SubsetGenerator} to generate subsets within the specified size range.
     * <p>
     * When both {@code fromSize} and {@code toSize} are negative, no output will be generated.
     * If {@code fromSize} is less than or equal to zero and {@code toSize} is greater than or equal
     * to zero, one empty set will be generated. This behavior is mathematically correct, though it
     * may seem unintuitive.
     *
     * @param elements the list of elements from which subsets are generated. Each item in the collection is treated as unique.
     * @param fromSize the minimum size (inclusive) of the subsets to generate.
     * @param toSize the maximum size (inclusive) of the subsets to generate. Must be greater than or equal to {@code fromSize}.
     */
    SubsetGenerator(List<T> elements, int fromSize, int toSize) {
        super(elements);
        this.fromSize = fromSize;
        this.toSize = toSize;
    }

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
         * Constructs an {@link OnDemandIterator} starting from the specified subset size.
         *
         * @param start the starting size for generating subsets
         */
        public OnDemandIterator(int start) {
            this.start = start;
            current = new Combinations(null).unique(start, elements).lexOrder().iterator();
        }

        @Override
        public boolean hasNext() {
            if (current.hasNext()) {
                return true;
            }
            if (start < toSize) {
                current = new Combinations(null).unique(++start, elements).lexOrder().iterator();
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
