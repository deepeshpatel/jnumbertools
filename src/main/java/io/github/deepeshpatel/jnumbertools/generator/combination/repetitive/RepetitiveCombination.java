/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates repetitive combinations of size r from n items in lexicographical order.
 * <p>
 * A repetitive combination allows items to be selected multiple times, with the total number of
 * combinations given by ⁿ⁺ᵣ⁻¹Cᵣ = (n+r-1)! / (r! * (n-1)!). Order does not matter, and elements
 * are treated as distinct based on their indices in the input list, with no duplicate checking.
 * </p>
 * <p>
 * For example, for items [1, 2] and r=3, the combinations are:
 * <pre>
 * [1, 1, 1], [1, 1, 2], [1, 2, 2], [2, 2, 2]
 * </pre>
 * Instances are created via a builder, so the constructor is package-private.
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class RepetitiveCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * Constructs a generator for repetitive combinations.
     *
     * @param elements the list of n items to generate combinations from (must not be null or empty)
     * @param r        the size of each combination (r ≥ 0)
     * @throws IllegalArgumentException if r < 0 or elements is null/empty
     */
    RepetitiveCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
    }

    /**
     * Returns an iterator over repetitive combinations in lexicographical order.
     *
     * @return an iterator of lists, each representing a combination
     */
    @Override
    public Iterator<List<T>> iterator() {
        return (r == 0 || elements.isEmpty()) ? Util.emptyListIterator() : new Itr();
    }

    /**
     * Iterator for generating repetitive combinations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination as an array of indices, where indices[i] is the index
         * of the selected element from the input list (0 ≤ i < r).
         */
        int[] indices = new int[r];

        /**
         * Checks if more combinations are available.
         *
         * @return true if further combinations exist, false otherwise
         */
        @Override
        public boolean hasNext() {
            return indices.length != 0;
        }

        /**
         * Returns the next repetitive combination.
         *
         * @return a list representing the next combination
         * @throws NoSuchElementException if no further combinations are available
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
         * Computes the next repetitive combination in lexicographical order.
         * <p>
         * Given the current combination of indices [a₀, a₁, ..., aᵣ₋₁], this method
         * finds the rightmost index i where aᵢ < n-1, increments it, and sets all
         * subsequent indices to the same value to maintain lexicographical order.
         * Returns an empty array if no further combination exists.
         *
         * @param a the current combination of indices
         * @param n the number of elementsචn the number of items in the input list
         * @return the next combination of indices, or an empty array if none exists
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