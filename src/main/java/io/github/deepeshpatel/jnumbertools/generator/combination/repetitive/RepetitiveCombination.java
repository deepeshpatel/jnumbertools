/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.builders.CombinationsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.util.*;

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
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link CombinationsBuilder#repetitive(int, List)}.
     * All parameter validation (null check, r ≥ 0) is handled by the builder.
     * </p>
     *
     * @param elements the list of items to generate combinations from (assumed non-null)
     * @param r        the size of each combination (assumed r ≥ 0)
     */
    RepetitiveCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
    }

    /**
     * Returns an iterator over repetitive combinations in lexicographical order.
     * <p>
     * A repetitive combination allows items to be selected multiple times. The combinations
     * are generated in lexicographical order based on element indices.
     * </p>
     * <p>
     * Example for elements [A, B] with r=3, the iterator produces:
     * [A,A,A], [A,A,B], [A,B,B], [B,B,B]
     * </p>
     *
     * @return an iterator over repetitive combinations of size r in lexicographical order;
     *         returns {@link Util#emptyListIterator()} for r = 0 (one empty combination);
     *         returns empty iterator for n = 0, r > 0 (no combinations possible)
     */
    @Override
    public Iterator<List<T>> iterator() {
        // Case 1: r = 0 → one empty combination (⁰C₀ = 1, ⁿC₀ = 1)
        if (r == 0) return Util.emptyListIterator();

        // Case 2: n = 0 and r > 0 → 0ʳ = 0 → empty iterator
        if (elements.isEmpty()) return Collections.emptyIterator();

        // Case 3: Normal case (n > 0, r > 0)
        return new Itr();
    }
    /**
     * Iterator for generating repetitive combinations in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination as an array of indices, where indices[i] is the index
         * of the selected element from the input list (0 ≤ i < r). Advanced in place.
         */
        private final int[] indices = new int[r];
        private boolean hasNext = true;

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
         * Returns the next repetitive combination.
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
         * Moves to the next repetitive combination in lexicographical order, in place.
         * <p>
         * Given the current combination of indices [a₀, a₁, ..., aᵣ₋₁], this method
         * finds the rightmost index i where aᵢ < n-1, increments it, and sets all
         * subsequent indices to the same value to maintain lexicographical order.
         *
         * @param a the current combination of indices, updated in place
         * @param n the number of items in the input list
         * @return false if the current combination was the last one (a is then unchanged)
         */
        private boolean advance(int[] a, int n) {
            final int maxSupportedValue = n - 1;
            int i = a.length - 1;

            while (i >= 0 && a[i] == maxSupportedValue) {
                i--;
            }
            if (i == -1) {
                return false;
            }

            int fillValue = a[i] + 1;

            for (int k = i; k < a.length; k++) {
                a[k] = fillValue;
            }
            return true;
        }
    }
}
