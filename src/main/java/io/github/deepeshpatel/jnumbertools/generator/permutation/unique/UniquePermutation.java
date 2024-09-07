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
 * Utility to generate all n! unique permutations (with no repeated values)
 * Permutations are generated in lex order of indices of input values,
 * considering value at each index as unique.
 * For more information on Permutations see
 * @see <a href="https://en.wikipedia.org/wiki/Permutation">
 * Wikipedia Permutation</a>
 *
 * <pre>
 *     Code example:
 *     new UniquePermutation&lt;&gt;(List.of("A","B","C"))
 *                 .forEach(System.out::println);
 *     or
 *
 *     JNumberTools.permutationsOf("A","B","C")
 *                 .unique()
 *                 .forEach(System.out::println);
 *
 * will generate following (all posible permutations of A,B and C in lex order) -
 * [A, B, C]
 * [A, C, B]
 * [B, A, C]
 * [B, C, A]
 * [C, A, B]
 * [C, B, A]
 * </pre>
 *
 * @author Deepesh Patel
 */
public final class UniquePermutation<T> extends AbstractGenerator<T> implements Iterable<List<T>>{

    UniquePermutation(List<T> elements) {
        super(elements);
    }

    public Iterator<List<T>> iterator() {
        return elements.isEmpty() ? newEmptyIterator() :
                new UniquePermItrForElements<>(elements, this::indicesToValues);
    }
}