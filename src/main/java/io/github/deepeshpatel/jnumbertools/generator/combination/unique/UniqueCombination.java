/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;
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
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.base.Combinations#unique(int, List)}.
     * All parameter validation (null check, 0 ≤ r ≤ n) is handled by the builder.
     * </p>
     *
     * @param elements the list of items to generate combinations from (assumed non-null)
     * @param r        the size of each combination (assumed 0 ≤ r ≤ elements.size())
     */
    UniqueCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
        //checkParamCombination(elements.size(), r, "unique combinations");
    }

    /**
     * Returns an iterator over unique r-combinations in lexicographical order.
     * <p>
     * The iterator generates all possible combinations of size r from the input elements,
     * where order does not matter. Each combination is returned as an immutable list.
     * </p>
     * <p>
     * Example for elements [A, B, C, D] with r=2, the iterator produces:
     * [A,B], [A,C], [A,D], [B,C], [B,D], [C,D]
     * </p>
     *
     * @return an iterator over unique r-combinations in lexicographical order;
     *         returns an empty iterator if r > elements.size() or the input list is empty
     * @see UniqueCombination
     */
    @Override
    public Iterator<List<T>> iterator() {
        // Case 1: ⁿC₀ = 0 for n > 0 and r > n
        if (r > elements.size()) return Collections.emptyIterator();

        // Case 2: ⁿC₀ = 1
        if (r == 0) return Util.emptyListIterator();

        // Case 3: r = n -> one combination containing all elements (ⁿCₙ = 1)
        if (r == elements.size()) return Collections.singletonList(List.copyOf(elements)).iterator(); // immutable copy, never the internal list

        // Case 4: Normal case (0 < r < n)
        return new Itr();
    }

    /**
     * Iterator for generating unique r-combinations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination as an array of indices, where indices[i] is the index
         * of the selected element from the input list (0 ≤ i < r). Advanced in place.
         */
        private final int[] indices;
        private boolean hasNext = true;

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
            return hasNext;
        }

        /**
         * Returns the next unique combination.
         *
         * @return a list representing the next combination
         * @throws NoSuchElementException if no further combinations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            // indicesToValues copies the values out, so the indices can be advanced in place afterwards.
            List<T> result = indicesToValues(indices);
            hasNext = advance(indices, elements.size());
            return result;
        }

        /**
         * Moves to the next combination in lexicographical order, in place.
         * <p>
         * Given the current combination of indices [a₀, a₁, ..., aᵣ₋₁], this method
         * finds the rightmost index i where aᵢ can be incremented (i.e., aᵢ < n - (r - i)).
         * It increments aᵢ and resets subsequent indices to maintain ascending order.
         *
         * @param a the current combination of indices, updated in place
         * @param n the total number of elements in the input list
         * @return false if the current combination was the last one
         */
        private boolean advance(int[] a, int n) {
            if (a.length == 0 || a[0] == n - a.length) {
                return false;
            }
            int i = a.length - 1;
            int maxSupportedValueAtIndexI = n - 1;

            while (i >= 0 && a[i] == maxSupportedValueAtIndexI) {
                i--;
                maxSupportedValueAtIndexI--;
            }
            a[i] = a[i] + 1;

            for (int j = i + 1; j < a.length; j++) {
                a[j] = a[j - 1] + 1;
            }
            return true;
        }
    }
}
