/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

    private final Calculator calculator;

    /**
     * Constructs an instance for generating k-permutations in combination order.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.base.Permutations#nPk(int, List)} and then
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder#combinationOrder()}.
     * All parameter validation (null check, 0 ≤ k ≤ n) is handled by the builder.
     * </p>
     *
     * @param elements the list of elements to permute (assumed non-null)
     * @param k the size of each permutation (assumed 0 ≤ k ≤ elements.size())
     * @param calculator the shared calculator used by the underlying combination/permutation builders
     */
    KPermutationCombinationOrder(List<T> elements, int k, Calculator calculator) {
        super(elements, k);
        this.calculator = calculator;
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
        private final Permutations permutations = new Permutations(calculator);
        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        /**
         * Initializes the iterator with the first combination's permutations.
         * Only reached when 0 < k < n, so at least one combination exists.
         */
        public Itr() {
            combinationIterator = new Combinations(calculator).unique(k, elements).lexOrder().iterator();
            getNextIterator();
        }

        /** Side-effect free: true while the current group or any later combination remains. */
        @Override
        public boolean hasNext() {
            return currentIterator.hasNext() || combinationIterator.hasNext();
        }

        /** Works without a preceding hasNext(): moves to the next combination when needed. */
        @Override
        public List<T> next() {
            if (!currentIterator.hasNext()) {
                if (!combinationIterator.hasNext()) {
                    throw new NoSuchElementException("No more permutations available");
                }
                getNextIterator();
            }
            return currentIterator.next();
        }

        /**
         * Advances to the next combination and returns an iterator over its k! permutations
         * (never empty, because k! >= 1).
         */
        private Iterator<List<T>> getNextIterator() {
            currentIterator = permutations
                    .unique(combinationIterator.next())
                    .lexOrder()
                    .iterator();
            return currentIterator;
        }
    }
}