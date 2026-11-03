/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder#lexOrder()}
     * after configuring the range with {@code inRange()} or {@code all()}.
     * All parameter validation (range validation, null checks) is handled by the builder.
     * </p>
     *
     * <p>
     * Mathematical behavior for edge cases:
     * <ul>
     *   <li>If {@code fromSize} = 0, the single empty subset is generated first</li>
     *   <li>Sizes above {@code elements.size()} do not exist, so {@code toSize} is effectively capped at
     *       {@code elements.size()}; if {@code fromSize} is above it, nothing is generated</li>
     * </ul>
     * </p>
     *
     * @param elements the list of elements from which subsets are generated (assumed non-null)
     * @param fromSize the minimum subset size to generate (inclusive)
     * @param toSize the maximum subset size to generate (inclusive); must be ≥ {@code fromSize}
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
     * <p>
     * Every call starts a fresh, independent traversal, and every returned subset is immutable.
     * </p>
     *
     * @return an iterator over subsets in lexicographical order;
     *         returns an iterator with a single empty subset if fromSize = 0;
     *         returns an empty iterator if fromSize > toSize or fromSize > elements.size()
     * @see SubsetBuilder#inRange(int, int)
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new OnDemandIterator();
    }

    /**
     * Iterator class for generating subsets on demand, one size block at a time.
     */
    class OnDemandIterator implements Iterator<List<T>> {

        /** Largest size that can exist; also bounds the work when the empty set is given a huge toSize. */
        private final int lastSize = Math.min(toSize, elements.size());
        private int nextSize = fromSize;
        private Iterator<List<T>> current = Collections.emptyIterator();

        @Override
        public boolean hasNext() {
            // Advance past exhausted (or empty) size blocks. Iterative, so the number of blocks is not
            // limited by stack depth.
            while (!current.hasNext() && nextSize <= lastSize) {
                current = new Combinations(null).unique(nextSize++, elements).lexOrder().iterator();
            }
            return current.hasNext();
        }

        @Override
        public List<T> next() {
            // hasNext() moves on to the next size block, so next() works without a prior hasNext() call.
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }
}