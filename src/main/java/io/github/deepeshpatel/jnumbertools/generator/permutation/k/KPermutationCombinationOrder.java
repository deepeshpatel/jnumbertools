/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Generates unique k-permutations in combination order from a list of elements.
 *
 * <p>
 * This class produces all permutations of size k (Pₖ) from the input list, where permutations are ordered by:
 * <ol>
 *   <li>First by their underlying combination in lexicographical order</li>
 *   <li>Then by permutations within each combination group</li>
 * </ol>
 *
 * <p>Example for elements [A, B, C] (distinct by position) and k=2:
 * <pre>
 * [A, B], [B, A], [A, C], [C, A], [B, C], [C, B]
 * </pre>
 *
 * <p>Elements are treated as distinct based on their position (e₀, e₁, e₂,...).
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public final class KPermutationCombinationOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs an instance for generating k-permutations in combination order.
     *
     * @param elements the list of elements to permute (must not be null)
     * @param k the size of each permutation (0 ≤ k ≤ elements.size())
     * @throws IllegalArgumentException if k is negative or exceeds elements size
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over unique k-permutations in combination order.
     *
     * <p>The iterator behavior:
     * <ul>
     *   <li>Returns empty iterator if k=0 or elements is empty</li>
     *   <li>Uses direct permutation iterator when k = n (full permutations)</li>
     *   <li>Otherwise generates C(n,k) combinations and P(k,k) permutations for each</li>
     * </ul>
     *
     * <p>Note: This method relies on {@code indicesToValues}, defined in the parent class,
     * to map index arrays to element lists.
     *
     * @return an iterator producing k-permutations in combination order
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0 || elements.isEmpty()) {
            return Util.emptyListIterator();
        }
        if (k == elements.size()) {
            return new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
        }
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {
        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        /**
         * Initializes the iterator by creating a combination iterator for C(n,k)
         * and setting up the first permutation iterator.
         *
         * <p>Note: The {@code Combinations} and {@code Permutations} constructors
         * are passed {@code null} as the first argument, which is specific to the
         * library's implementation for default behavior.
         */
        public Itr() {
            combinationIterator = new Combinations(null).unique(k, elements).lexOrder().iterator();
            getNextIterator();
        }

        @Override
        public boolean hasNext() {
            if (currentIterator.hasNext()) {
                return true;
            }
            if (!combinationIterator.hasNext()) {
                return false;
            }
            return getNextIterator().hasNext();
        }

        @Override
        public List<T> next() {
            return currentIterator.next();
        }

        /**
         * Retrieves the next permutation iterator for the next combination.
         *
         * @return the iterator for permutations of the next combination
         */
        private Iterator<List<T>> getNextIterator() {
            currentIterator = new Permutations(null)
                    .unique(combinationIterator.next())
                    .lexOrder()
                    .iterator();
            return currentIterator;
        }
    }
}