/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility for generating every mᵗʰ repetitive permutation of a specified width in lexicographical order.
 * <p>
 * This class generates permutations of length {@code width} from the input list, allowing repetition,
 * starting from a specified rank (default 0) and advancing by {@code m} in lexicographical order of indices
 * (e.g., for [A, B], width=2, m=2: [A, A], [B, A]). It uses a base-n (n = number of elements) addition
 * algorithm with carry propagation on an integer array for efficiency. Total permutations are n^width;
 * iteration stops when the permutation exceeds this implicitly via carry propagation. Instances are
 * created via a builder.
 * </p>
 * <p>Example:
 * <pre>
 * List<String> elements = List.of("A", "B");
 * RepetitivePermutationMth<String> perms = new RepetitivePermutationMth<>(elements, 2, BigInteger.valueOf(2), BigInteger.ZERO);
 * perms.stream().toList(); // [[A, A], [B, A]]
 * </pre>
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class RepetitivePermutationMth<T> extends AbstractGenerator<T> {

    private final int width;
    private final BigInteger increment;
    private final BigInteger start;

    /**
     * Constructs a new {@code RepetitivePermutationMth} instance.
     *
     * @param elements  the list of elements to permute; must not be null or empty
     * @param width     the length of each permutation; must be non-negative
     * @param increment the step size for generating permutations; must be positive
     * @param start     the starting permutation index (0-based); must be non-negative
     * @throws IllegalArgumentException if width is negative, increment is non-positive, start is negative, or elements is null/empty
     */
    RepetitivePermutationMth(List<T> elements, int width, BigInteger increment, BigInteger start) {
        super(elements);
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative: " + width);
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Elements list cannot be empty for repetitive permutations");
        }
        if (increment.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Increment must be positive: " + increment);
        }
        if (start.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Start rank must be non-negative: " + start);
        }
        this.width = width;
        this.start = start;
        this.increment = increment;
    }

    /**
     * Returns an iterator over every mᵗʰ repetitive permutation starting from the specified rank.
     *
     * @return an iterator producing each permutation as a {@code List<T>}
     */
    @Override
    public Iterator<List<T>> iterator() {
        return width == 0 ? Util.emptyListIterator() : new RepetitiveMthIterator();
    }

    /**
     * Iterator implementation for generating every mᵗʰ repetitive permutation.
     */
    private class RepetitiveMthIterator implements Iterator<List<T>> {
        private final int[] currentIndices = new int[width];
        private boolean hasNext;

        /**
         * Initializes the iterator with the starting permutation.
         */
        public RepetitiveMthIterator() {
            hasNext = incrementIndices(currentIndices, elements.size(), start);
        }

        /**
         * Checks if there is a next permutation.
         * <p>
         * Returns true if the previous increment operation succeeded, false if the permutation has
         * exceeded n^width implicitly via carry propagation.
         * </p>
         *
         * @return {@code true} if the next permutation exists; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next mᵗʰ repetitive permutation.
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if no further permutations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException("No more permutations available");
            }
            List<T> result = indicesToValues(currentIndices);
            hasNext = incrementIndices(currentIndices, elements.size(), increment);
            return result;
        }

        /**
         * Increments the current indices by the specified step size to generate the next permutation.
         * <p>
         * Treats the indices as a base-n number (n = number of elements) and adds the step size,
         * propagating carries to higher positions. For example, with elements [A, B] (n=2), width=2,
         * increment=1: [0,0] → [0,1] → [1,0] → [1,1] maps to [A,A], [A,B], [B,A], [B,B]. Returns false
         * when the carry propagates beyond the most significant digit, indicating exhaustion.
         * </p>
         *
         * @param indices the current permutation indices to update
         * @param base    the number of unique elements (n)
         * @param step    the step size to increment by (typically {@code increment} or {@code start} for initial)
         * @return {@code true} if the increment succeeded; {@code false} if exhausted
         */
        private boolean incrementIndices(int[] indices, int base, BigInteger step) {
            BigInteger nextK = step;
            for (int i = indices.length - 1; i >= 0; i--) {
                BigInteger sum = nextK.add(BigInteger.valueOf(indices[i]));
                BigInteger[] division = sum.divideAndRemainder(BigInteger.valueOf(base));
                nextK = division[0];
                indices[i] = division[1].intValue();
            }
            return nextK.equals(BigInteger.ZERO);
        }
    }
}