/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.itertor.UniquePermItrForElements;

import java.util.Iterator;
import java.util.List;

/**
 * Utility to generate all n! unique permutations (with no repeated values).
 * <p>
 * Permutations are generated in lexicographic order of indices of input values,
 * treating the value at each index as unique.
 * </p>
 *
 * <p>
 * For more information on permutations, see
 * <a href="https://en.wikipedia.org/wiki/Permutation">Wikipedia Permutation</a>.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * new UniquePermutation&lt;&gt;(List.of("A", "B", "C"))
 *         .forEach(System.out::println);
 *
 * // or
 *
 * JNumberTools.permutationsOf("A", "B", "C")
 *         .unique()
 *         .forEach(System.out::println);
 * </pre>
 * This will generate the following output (all possible permutations of "A", "B", and "C" in lexicographic order):
 * <pre>
 * [A, B, C]
 * [A, C, B]
 * [B, A, C]
 * [B, C, A]
 * [C, A, B]
 * [C, B, A]
 * </pre>
 *
 * @param <T> the type of elements in the permutation
 * @see <a href="https://en.wikipedia.org/wiki/Permutation">Wikipedia Permutation</a>
 * @author Deepesh Patel
 */
public final class UniquePermutation<T> extends AbstractGenerator<T> implements Iterable<List<T>>{

    /**
     * Constructs a UniquePermutation generator for the specified list of elements.
     *
     * @param elements the list of elements to generate permutations from
     */
    UniquePermutation(List<T> elements) {
        super(elements);
    }

    /**
     * Returns an iterator over the unique permutations of the elements.
     *
     * @return an Iterator of lists, each representing a unique permutation
     */
    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? newEmptyIterator() :
                new UniquePermItrForElements<>(elements, this::indicesToValues);
    }
}
