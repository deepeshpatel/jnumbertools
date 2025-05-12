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
 * <p>
 * This class produces all permutations of a subset of size {@code k} from the input list, ordered by
 * combinations in lexicographical order followed by permutations of each combination. For example,
 * for [1, 2, 3] and k=2, it generates:
 * <pre>
 * [1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]
 * </pre>
 * Elements are treated as distinct based on their position (e.g., [elements₀, elements₁, ...]).
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationCombinationOrder<T> extends AbstractKPermutation<T> {

    /**
     * Constructs an instance for generating k-permutations in combination order.
     *
     * @param elements the list of elements to permute
     * @param k        the size of each permutation; must be between 0 and elements.size()
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements, k);
    }

    /**
     * Returns an iterator over unique k-permutations in combination order.
     * <p>
     * If k=0 or the list is empty, returns an empty iterator. If k equals the list size, uses a direct
     * permutation iterator. Otherwise, generates combinations of indices and permutes each.
     * </p>
     *
     * @return an iterator over lists representing k-permutations
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

        private Iterator<List<T>> getNextIterator() {
            currentIterator = new Permutations(null)
                    .unique(combinationIterator.next())
                    .lexOrder()
                    .iterator();
            return currentIterator;
        }
    }
}