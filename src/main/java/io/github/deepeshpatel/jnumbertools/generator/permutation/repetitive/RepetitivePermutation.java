/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility for generating all permutations of a given set of items with repetition allowed.
 * <p>
 * Permutations are generated in lexicographical order based on the indices of the input values,
 * treating each value at a specific index (e.g., elements₀, elements₁, …, elementsₙ₋₁) as unique.
 * This means that even if the same element appears more than once in a permutation, its occurrence
 * is determined by its position (i.e., index) in the input list.
 * </p>
 * <p>
 * Note that the length of each permutation (specified by {@code size}) may be greater than the number
 * of unique elements in the input, thereby allowing repeated use of elements.
 * <br>
 * Instances of this class are intended to be created via a builder; hence, it does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 */
public final class RepetitivePermutation<T> extends AbstractGenerator<T> {

    private final int size;

    /**
     * Constructs a new {@code RepetitivePermutation} instance for generating all permutations
     * (with repetition) of the specified size from the given list of elements.
     *
     * @param elements the list of elements to permute
     * @param size the size (length) of each permutation. This value can be greater than the number of unique elements.
     */
    RepetitivePermutation(List<T> elements, int size) {
        super(elements);
        this.size = size;
    }

    /**
     * Returns an iterator over all repetitive permutations of the input elements.
     * <p>
     * The iterator generates each permutation by treating the current indices as a base-n counter
     * (where n is the number of unique elements) and incrementing it in lexicographical order.
     * </p>
     *
     * @return an iterator for generating permutations as lists of elements
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new NumberIterator();
    }

    /**
     * Iterator implementation that generates permutations with repetition.
     */
    private class NumberIterator implements Iterator<List<T>> {

        /**
         * The current permutation represented as an array of indices.
         * The initial value is an array of zeros (e.g., [0, 0, …, 0]), corresponding to repeating the first element.
         */
        private final int[] currentIndices = new int[size];

        /**
         * Flag indicating whether further permutations are available.
         */
        private boolean hasNext = true;

        /**
         * Returns {@code true} if there are more permutations to generate.
         *
         * @return {@code true} if the next permutation exists; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next permutation in lexicographical order.
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if no more permutations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            List<T> result = indicesToValues(currentIndices);
            hasNext = nextRepetitivePermutation(currentIndices, elements.size());
            return result;
        }

        /**
         * Advances the indices array to the next permutation in lexicographical order.
         * <p>
         * This method treats the indices as digits of a base-n number (where n equals the
         * number of unique elements) and increments the number by one. If a digit reaches its maximum value
         * (i.e., base-1), it is reset to 0 and the next more significant digit is incremented.
         * </p>
         *
         * @param indices the current indices representing the permutation
         * @param base the number of unique elements in the input list
         * @return {@code true} if the indices were successfully advanced to the next permutation;
         *         {@code false} if the last permutation has been reached
         */
        private boolean nextRepetitivePermutation(int[] indices, int base) {
            // Iterate from the rightmost digit (least significant) to the leftmost.
            for (int i = 0, j = indices.length - 1; j >= 0; j--, i++) {
                if (indices[j] == base - 1) {
                    indices[j] = 0; // Reset this digit if it has reached its maximum value.
                } else {
                    indices[j]++;
                    return true;
                }
            }
            return false; // All digits were at their maximum; no further permutation exists.
        }
    }
}