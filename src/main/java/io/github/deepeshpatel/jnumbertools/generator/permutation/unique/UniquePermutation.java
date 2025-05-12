/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;
import io.github.deepeshpatel.jnumbertools.generator.permutation.iterator.UniquePermutationLexElementIterator;

import java.util.Iterator;
import java.util.List;

/**
 * Generates all n! unique permutations of a list of elements in lexicographical order.
 * <p>
 * Permutations are produced based on the indices of the input values (e.g., elements₀, elements₁, ...),
 * treating each element as distinct by its position. For example, for [A, B, C], it generates:
 * <pre>
 * [A, B, C], [A, C, B], [B, A, C], [B, C, A], [C, A, B], [C, B, A]
 * </pre>
 * Instances should be created via a builder (e.g., {@code new Permutations().unique(size).lexOrder()}).
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @see <a href="https://en.wikipedia.org/wiki/Permutation">Wikipedia: Permutation</a>
 */
public final class UniquePermutation<T> extends AbstractGenerator<T> {

    /**
     * Constructs a generator for unique permutations of the specified elements.
     *
     * @param elements the list of elements to permute; may be empty
     */
    UniquePermutation(List<T> elements) {
        super(elements);
    }

    /**
     * Returns an iterator over all unique permutations in lexicographical order.
     * <p>
     * If the input list is empty, returns an empty iterator. Otherwise, uses an efficient index-based
     * iterator to generate all n! permutations.
     * </p>
     *
     * @return an iterator over lists representing unique permutations
     */
    @Override
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? Util.emptyListIterator() :
                new UniquePermutationLexElementIterator<>(elements.size(), this::indicesToValues);
    }

}