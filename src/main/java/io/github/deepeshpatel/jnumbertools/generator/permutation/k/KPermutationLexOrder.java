/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.builders.PermutationsBuilder;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.*;

/**
 * Generates unique k-permutations in lexicographical order from a list of elements.
 *
 * <p>
 * Produces all ordered arrangements Pₙ,ₖ = n!/(n−k)! of size k from the input list,
 * where elements are treated as distinct by their position (e₀, e₁, ..., eₙ₋₁).
 * Permutations are ordered lexicographically based on element indices.
 *
 * <p>Example for elements [A, B, C] (distinct by position) and k=2:
 * <pre>
 * [A, B], [A, C], [B, A], [B, C], [C, A], [C, B]
 * </pre>
 *
 * <p>This is different from combination-order permutations where permutations are
 * grouped by their underlying combinations first.
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public final class KPermutationLexOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs a generator for k-permutations in lexicographical order.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link PermutationsBuilder#nPk(int, List)}.
     * All parameter validation (null check, 0 ≤ k ≤ n) is handled by the builder.
     * </p>
     *
     * @param elements the list of elements to permute (assumed non-null)
     * @param k the size of each permutation (assumed 0 ≤ k ≤ elements.size())
     */
    KPermutationLexOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over k-permutations in lexicographical order.
     *
     * <p>The iterator behavior:
     * <ul>
     *   <li>Returns an empty iterator if k &gt; n</li>
     *   <li>Returns a single empty permutation if k=0</li>
     *   <li>Uses optimized full permutation iterator when k = n</li>
     *   <li>Otherwise uses incremental lexicographical generation</li>
     * </ul>
     *
     * <p>Note: This method relies on {@code indicesToValues}, defined in the parent class
     * {@code AbstractKPermutation}, to map index arrays to element lists.
     *
     * @return an iterator producing Pₙ,ₖ permutations in lexicographical order
     */
    @Override
    public Iterator<List<T>> iterator() {
        //⁰Pₖ = 0  and ⁿPₖ = 0 for k>n
        if(k > elements.size()) return Collections.emptyIterator();
        //⁰P₀ = 1 and ⁿP₀ = 1
        if(k ==0)  return Util.emptyListIterator();
        if (k == elements.size()) return new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
        return new Itr();
    }

    /**
     * Iterator implementation that generates k-permutations incrementally
     * in lexicographical order.
     */
    private class Itr implements Iterator<List<T>> {
        private final int n = elements.size();
        private final int[] indices = new int[k];      // the current k-permutation, advanced in place
        private final int[] pool = new int[n];         // unused elements in ascending order, from poolHead
        private final boolean[] used = new boolean[n];
        private int poolHead;
        private int poolSize;
        private boolean hasNext = true;

        public Itr() {
            for (int i = 0; i < k; i++) indices[i] = i;
            for (int v = k; v < n; v++) pool[poolSize++] = v;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException("No more permutations available");
            }
            // indicesToValues copies the values out, so the indices can be advanced in place afterwards.
            List<T> result = indicesToValues(indices);
            hasNext = advance();
            return result;
        }

        /** Moves to the next k-permutation in lexicographic order; false when the current one was the last. */
        private boolean advance() {
            int[] a = indices;

            // Most steps only replace the last position with the next unused element.
            if (poolHead < poolSize) {
                a[a.length - 1] = pool[poolHead++];
                return true;
            }

            int index = -1;
            for (int i = a.length - 1; i > 0; i--) {
                if (a[i - 1] < a[i]) {
                    index = i - 1;
                    break;
                }
            }
            if (index == -1) {
                return false;
            }

            // Everything outside the fixed prefix a[0..index-1] is available again.
            Arrays.fill(used, false);
            for (int i = 0; i < index; i++) {
                used[a[i]] = true;
            }
            int valueForCurrentIndex = a[index] + 1;
            while (used[valueForCurrentIndex]) {
                valueForCurrentIndex++;
            }
            a[index] = valueForCurrentIndex;
            used[valueForCurrentIndex] = true;

            // The positions after it take the smallest unused elements, the rest become the new pool.
            int slot = index + 1;
            poolHead = 0;
            poolSize = 0;
            for (int x = 0; x < n; x++) {
                if (used[x]) continue;
                if (slot < a.length) {
                    a[slot++] = x;
                } else {
                    pool[poolSize++] = x;
                }
            }
            return true;
        }
    }
}
