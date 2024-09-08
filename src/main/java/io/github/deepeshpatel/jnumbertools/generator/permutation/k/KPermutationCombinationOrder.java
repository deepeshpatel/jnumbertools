/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.base.Permutations;
import io.github.deepeshpatel.jnumbertools.generator.permutation.itertor.UniquePermItrForElements;

import java.util.Iterator;
import java.util.List;

/**
 * Implements an iterable for generating unique permutations of a subset of size <code>k</code>
 * from a list of elements. The permutations are generated in lexicographical order of combinations
 * of indices of the input values, treating the value at each index as unique.
 *
 * <p>For example, all permutations of size 2 for the list [1, 2, 3] are:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]
 * </pre>
 *
 * <p>Code example:
 * <pre>
 * new KPermutation&lt;&gt;(List.of("A","B","C"), 2)
 *     .forEach(System.out::println);
 *
 * // or
 *
 * JJNumberTools.permutationsOf("A","B","C")
 *     .k(2)
 *     .forEach(System.out::println);
 *
 * // will generate:
 * [A, B]
 * [B, A]
 * [A, C]
 * [C, A]
 * [B, C]
 * [C, B]
 * </pre>
 *
 * @param <T> the type of elements in the permutation
 *
 * @author Deepesh Patel
 */
public final class KPermutationCombinationOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs an instance of {@code KPermutationCombinationOrder} for generating
     * unique permutations of size <code>k</code> from the specified list of elements.
     *
     * @param elements the list of elements to generate permutations from
     * @param k the size of the permutations; must be less than or equal to the size of the list
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over the unique permutations of the subset of size <code>k</code>.
     *
     * @return an {@code Iterator} over the permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        if (k == 0 || elements.isEmpty()) {
            return newEmptyIterator();
        }

        /* Use the faster version when k equals the size of elements */
        if (k == elements.size()) {
            return new UniquePermItrForElements<>(elements, this::indicesToValues);
        }

        return new Itr();
    }

    /**
     * Iterator implementation for generating permutations based on combinations of elements.
     */
    private class Itr implements Iterator<List<T>> {

        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        /**
         * Constructs the iterator and initializes the combination iterator.
         */
        public Itr() {
            combinationIterator = new Combinations(null).unique(k, elements).lexOrder().iterator();
            getNextIterator();
        }

        /**
         * Checks if there are more permutations available.
         *
         * @return {@code true} if there are more permutations; {@code false} otherwise
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
            currentIterator = new Permutations(null).unique(combinationIterator.next()).lexOrder().iterator();
            return currentIterator;
        }

        /**
         * Returns the next permutation in the iteration.
         *
         * @return the next permutation as a list
         */
        @Override
        public List<T> next() {
            return currentIterator.next();
        }
    }
}
