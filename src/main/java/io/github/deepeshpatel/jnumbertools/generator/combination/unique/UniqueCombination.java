/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Generates unique combinations of size r from a list of n items in lexicographical order.
 * A combination is a selection of r items from n distinct items where order does not matter.
 * The total number of possible combinations is given by ⁿCᵣ = n! / (r! * (n-r)!).
 * <p>
 * For example, for items [1, 2, 3, 4] and r=2, the combinations in lexicographical order are:
 * [1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4].
 * <p>
 * Elements are treated as distinct based on their indices in the input list, with no duplicate checking.
 * Instances are created via a builder, so the constructor is package-private.
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 */
public final class UniqueCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * Constructs a generator for unique combinations.
     *
     * @param elements the list of n items to generate combinations from (must not be null or empty)
     * @param r        the size of each combination (0 ≤ r ≤ n)
     * @throws IllegalArgumentException if r < 0, r > n, or elements is null/empty
     */
    UniqueCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
        checkParamCombination(elements.size(), r, "unique combinations");
    }

    /**
     * Returns an iterator over unique r-combinations in lexicographical order.
     *
     * @return an iterator of lists, each representing a combination
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    /**
     * Iterator for generating unique r-combinations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination as an array of indices, where indices[i] is the index
         * of the selected element from the input list (0 ≤ i < r).
         */
        int[] indices;

        private Itr() {
            indices = IntStream.range(0, r).toArray();
        }

        /**
         * Checks if more combinations are available.
         *
         * @return true if further combinations exist, false otherwise
         */
        @Override
        public boolean hasNext() {
            return indices != null;
        }

        /**
         * Returns the next unique combination.
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
            indices = nextCombination(indices, elements.size());
            return indicesToValues(old);
        }

        /**
         * Computes the next combination in lexicographical order.
         * <p>
         * Given the current combination of indices [a₀, a₁, ..., aᵣ₋₁], this method
         * finds the rightmost index i where aᵢ can be incremented (i.e., aᵢ < n - (r - i)).
         * It increments aᵢ and resets subsequent indices to maintain ascending order.
         * Returns null if no further combination exists.
         *
         * @param currentKCombination the current combination of indices
         * @param n                   the total number of elements in the input list
         * @return the next combination of indices, or null if none exists
         */
        private int[] nextCombination(int[] currentKCombination, int n) {
            if (currentKCombination.length == 0 ||
                    currentKCombination[0] == n - currentKCombination.length) {
                return null;
            }
            int[] next = Arrays.copyOf(currentKCombination, currentKCombination.length);
            int i = next.length - 1;
            int maxSupportedValueAtIndexI = n - 1;

            while (i >= 0 && next[i] == maxSupportedValueAtIndexI) {
                i--;
                maxSupportedValueAtIndexI--;
            }
            next[i] = next[i] + 1;

            for (int j = i + 1; j < next.length; j++) {
                next[j] = next[j - 1] + 1;
            }
            return next;
        }
    }
}