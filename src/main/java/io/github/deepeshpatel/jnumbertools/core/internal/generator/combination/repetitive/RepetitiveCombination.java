/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.core.internal.generator.base.StreamableIterable;

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
     * {@link io.github.deepeshpatel.jnumbertools.api.Combinations#repetitive(int, List)}.
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
     *         returns {@link StreamableIterable#emptyListIterator()} for r = 0 (one empty combination);
     *         returns empty iterator for n = 0, r > 0 (no combinations possible)
     */
    @Override
    public Iterator<List<T>> iterator() {
        // Case 1: r = 0 → one empty combination (⁰C₀ = 1, ⁿC₀ = 1)
        if (r == 0) return emptyListIterator();

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
