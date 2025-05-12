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
 * Utility class for generating unique r‑combinations from an input set.
 * <p>
 * Given an input list of n items (e.g. elements₀, elements₁, …, elementsₙ₋₁), this class generates
 * all unique r‑combinations (with r ≤ n) in lexicographic order based on the indices of the items.
 * <br>
 * Note that the class does not perform any duplicate value checks; it treats each element as distinct
 * based solely on its position in the list.
 * </p>
 * <p>
 * For example, the unique 4‑combinations from a 6‑element list {1, 2, 3, 4, 5, 6} might be:
 * <pre>
 * 1234 1245 1345 1456 2356
 * 1235 1246 1346 2345 2456
 * 1236 1256 1356 2346 3456
 * </pre>
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder, so the constructor is package‑private.
 * </p>
 *
 * @param <T> the type of elements in the combinations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniqueCombination<T> extends AbstractGenerator<T> {

    private final int r;

    /**
     * Constructs a new UniqueCombination generator with the specified list of elements and combination size.
     * <p>
     * The provided list contains n items (e.g. elements₀, elements₁, …, elementsₙ₋₁), and r must be ≤ n.
     * </p>
     *
     * @param elements the list of n items from which combinations will be generated
     * @param r        the number of items in each combination (r‑combination); r must be ≤ n
     */
    UniqueCombination(List<T> elements, int r) {
        super(elements);
        this.r = r;
        checkParamCombination(elements.size(), r, "unique combinations");
    }

    /**
     * Returns an iterator over all unique r‑combinations of the input elements in lexicographic order.
     *
     * @return an iterator over lists representing the unique combinations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    /**
     * An iterator implementation for generating the unique r‑combinations in lexicographic order.
     */
    private class Itr implements Iterator<List<T>> {

        /**
         * The current combination represented as an array of indices.
         * For example, indices[k] represents the index of the chosen element for the kth position (0 ≤ k < r).
         */
        int[] indices;

        private Itr() {
            indices = IntStream.range(0, r).toArray();
        }

        /**
         * Checks if there are more combinations to generate.
         *
         * @return {@code true} if the current combination is not {@code null}; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return indices != null;
        }

        /**
         * Returns the next unique combination.
         *
         * @return the next combination as a list of elements
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
         * Computes the next lexicographic combination of indices.
         * <p>
         * Given the current combination (e.g. [a₀, a₁, …, aᵣ₋₁]), this method generates the next combination
         * by incrementing the appropriate index while ensuring that the result remains in ascending order.
         * If the current combination is the last possible combination, {@code null} is returned.
         * </p>
         *
         * @param currentKCombination the current combination of indices
         * @param n                   the total number of elements in the input list
         * @return the next combination of indices, or {@code null} if no further combination exists
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
