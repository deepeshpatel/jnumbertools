/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.api.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutation;

import java.util.Collections;
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
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link Permutations#nPk(int, List)} and then
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder#combinationOrder()}.
     * All parameter validation (null check, 0 ≤ k ≤ n) is handled by the builder.
     * </p>
     *
     * @param elements the list of elements to permute (assumed non-null)
     * @param k the size of each permutation (assumed 0 ≤ k ≤ elements.size())
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over k-permutations in combination order.
     * <p>
     * Permutations are ordered first by their underlying combination in lexicographical order,
     * then by all permutations within each combination group.
     * </p>
     * <p>
     * Example for elements [A, B, C] with k=2, the iterator produces:
     * [A,B], [B,A], [A,C], [C,A], [B,C], [C,B]
     * </p>
     *
     * @return an iterator over k-permutations in combination order
     */
    @Override
    public Iterator<List<T>> iterator() {
        // Case 1: k = 0 → one empty permutation (ⁿP₀ = 1)
        if (k == 0) {
            return Util.emptyListIterator();
        }

        // Case 2: Empty list with k > 0 → no permutations (⁰Pₖ = 0)
        // Case 3: k > n → no permutations (ⁿPₖ = 0)
        if (elements.isEmpty() || k > elements.size()) {
            return Collections.emptyIterator();
        }

        // Case 4: k = n → full permutations
        if (k == elements.size()) {
            return new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
        }

        // Case 5: Normal case (0 < k < n)
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
            combinationIterator = new UniqueCombinationBuilder<>(elements, k, null).lexOrder().iterator();
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
            currentIterator = new UniquePermutation<>(combinationIterator.next()).iterator();
            return currentIterator;
        }
    }
}