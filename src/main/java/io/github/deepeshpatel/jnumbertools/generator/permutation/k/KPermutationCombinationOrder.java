/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermItrForElements;

import java.util.Iterator;
import java.util.List;

/**
 * An iterable for generating unique k‑permutations from a given list of elements.
 * <p>
 * This class generates all unique permutations of a subset of size {@code k} from the input list.
 * Permutations are produced in lexicographical order based on the lexicographical order of the
 * corresponding combinations of indices (treating each element as unique by its position, e.g. elements₀, elements₁, …, elementsₙ₋₁).
 * </p>
 * <p>
 * For example, for the list [1, 2, 3] and {@code k = 2}, the unique permutations are:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]
 * </pre>
 * </p>
 * <p>
 * Instances of this class are intended to be created via a builder.
 * </p>
 *
 * @param <T> the type of elements in the permutation
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationCombinationOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs an instance of {@code KPermutationCombinationOrder} for generating
     * unique k‑permutations from the specified list of elements.
     *
     * @param elements the list of elements from which permutations are generated
     * @param k the size of the permutations; must be ≤ the size of the list
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over the unique k‑permutations of the input list.
     * <p>
     * If {@code k} is 0 or the input list is empty, an empty iterator is returned.
     * <br>
     * If {@code k} equals the size of the input list, a faster iterator is used that directly generates
     * permutations of the entire list.
     * Otherwise, an iterator is created that first generates all unique combinations of indices of size
     * {@code k} (in lexicographical order) and then, for each combination, generates the corresponding
     * permutations.
     * </p>
     *
     * @return an {@code Iterator} over lists representing the unique k‑permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0 || elements.isEmpty()) {
            return newEmptyIterator();
        }

        // Use a faster iterator when k equals the size of the elements list.
        if (k == elements.size()) {
            return new UniquePermItrForElements<>(elements.size(), this::indicesToValues);
        }

        return new Itr();
    }

    /**
     * Iterator implementation for generating k‑permutations based on combinations of indices.
     */
    private class Itr implements Iterator<List<T>> {

        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        /**
         * Constructs the iterator and initializes the combination iterator.
         * <p>
         * The combination iterator generates all unique combinations (of indices) of size {@code k} in lexicographical order.
         * For each combination, the corresponding permutations are generated using a permutation iterator.
         * </p>
         */
        public Itr() {
            combinationIterator = new Combinations(null).unique(k, elements).lexOrder().iterator();
            getNextIterator();
        }

        /**
         * Checks if there are more k‑permutations available.
         *
         * @return {@code true} if additional permutations exist; {@code false} otherwise
         */
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

        /**
         * Advances to the next combination and initializes the corresponding permutation iterator.
         *
         * @return the iterator for the next set of permutations
         */
        private Iterator<List<T>> getNextIterator() {
            currentIterator = new Permutations(null)
                    .unique(combinationIterator.next())
                    .lexOrder()
                    .iterator();
            return currentIterator;
        }

        /**
         * Returns the next k‑permutation.
         *
         * @return the next permutation as a list of elements
         */
        @Override
        public List<T> next() {
            return currentIterator.next();
        }
    }
}
