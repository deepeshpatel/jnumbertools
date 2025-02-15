/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

/**
 * Utility for generating r-combinations of an input set with repetition allowed.
 * <p>
 * This class generates r-combinations from an input set of n elements (e.g., elements₀, elements₁, …, elementsₙ₋₁),
 * with combinations produced in lexicographical order based on the indices of items in a list.
 * The class does not check for duplicate values and treats all values as distinct based on their index.
 * </p>
 * <p>
 * Example of repetitive combinations of 3 items out of 2 {1, 2}:
 * <pre>
 * 111 112 122 222
 * </pre>
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder; therefore, it does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements in the combination.
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class RepetitiveCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * Constructs a new {@code RepetitiveCombination} generator.
     *
     * @param elements a list of n items (e.g., elements₀, elements₁, …, elementsₙ₋₁) from which combinations will be generated.
     *                 n is the length of the input list.
     * @param r        the number of items in each combination.
     */
    RepetitiveCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
    }

    /**
     * Returns an iterator over the repetitive combinations.
     *
     * @return an {@code Iterator} over lists representing the repetitive combinations.
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (r == 0 || elements.isEmpty()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination of indices, where each element indicesₖ corresponds to the index of the chosen element
         * in the input list for the kth position (0 ≤ k < r).
         */
        int[] indices = new int[r];

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if additional combinations exist, {@code false} otherwise.
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Generates the next combination in lexicographical order.
         *
         * @return the next combination as a list of elements.
         * @throws NoSuchElementException if no further combinations are available.
         */
        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = nextRepetitiveCombination(indices, elements.size());
            return indicesToValues(old);
        }

        /**
         * Computes the next combination of indices.
         * <p>
         * Each element aₖ in the input array {@code a} represents the index of the element selected for the kth position
         * in the combination. This method calculates the next lexicographical combination of these indices.
         * </p>
         *
         * @param a the current combination of indices.
         * @param n the number of elements in the input list.
         * @return the next combination of indices, or an empty array if no further combinations exist.
         */
        private int[] nextRepetitiveCombination(int[] a, int n) {

            final int[] next = Arrays.copyOf(a, a.length);
            final int maxSupportedValue = n - 1;
            int i = next.length - 1;

            while (i >= 0 && a[i] == maxSupportedValue) {
                i--;
            }
            if (i == -1) {
                return new int[]{};
            }

            int fillValue = a[i] + 1;

            for (int k = i; k < next.length; k++) {
                next[k] = fillValue;
            }
            return next;
        }
    }
}
