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
 * Generates the mᵗʰ repetitive permutation of a given multiset in lexicographical order.
 * <p>
 * This class is designed to efficiently compute a specific permutation at rank m (0-based)
 * from a multiset where elements may have repetitions. The implementation uses combinatorial
 * mathematics to determine the permutation without generating all preceding permutations.
 * </p>
 * <p><b>Example:</b>
 * <pre>{@code
 * // Get the 2nd repetitive permutation of [A, A, B]
 * List<String> items = Arrays.asList("A", "A", "B");
 * int[] multiset = {2, 1}; // 2 A's, 1 B
 * RepetitivePermutationMth<String> generator = new RepetitivePermutationMth<>(items, multiset, BigInteger.valueOf(2));
 * System.out.println(generator.next()); // Outputs: [A, B, A]
 * }</pre>
 * <p>
 * <b>Note:</b> The total number of permutations is calculated as n! / (f₁! * f₂! * ... * fₖ!),
 * where n is the total number of elements, and fᵢ are the frequencies of distinct elements.
 * </p>
 *
 * @param <T> the type of elements in the permutation
 * @author Deepesh Patel
 * @see RepetitivePermutation
 * @see RepetitivePermutationBuilder
 */
public final class RepetitivePermutationMth<T> extends AbstractGenerator<T> {

    private final int width;
    private final BigInteger increment;
    private final BigInteger start;

    /**
     * Constructs a generator for every mᵗʰ repetitive permutation.
     *
     * @param elements the elements to permute (must not be null or empty)
     * @param width the length of each permutation (must be positive)
     * @param increment the step size between permutations (must be positive)
     * @param start the starting rank (0-based, must be non-negative)
     * @throws IllegalArgumentException if elements is empty, length ≤ 0,
     *         m ≤ 0, or start < 0
     */
    RepetitivePermutationMth(List<T> elements, int width, BigInteger increment, BigInteger start) {
        super(elements);
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative: " + width);
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Elements list cannot be empty for repetitive permutations");
        }
        if (increment.signum() <= 0) {
            throw new IllegalArgumentException("Increment must be positive: " + increment);
        }
        if (start.signum() < 0) {
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