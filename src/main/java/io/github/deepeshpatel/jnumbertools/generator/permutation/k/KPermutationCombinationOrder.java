/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;
import io.github.deepeshpatel.jnumbertools.generator.permutation.itertor.UniquePermItrForElements;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutation;

import java.util.Iterator;
import java.util.List;

/**
 *
 * Implements the iterable of unique permutations of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each index as unique.
 * For example all permutations of size 2 for [1,2,3] are -
 * <pre>
 *     [1, 2], [2, 1], [1, 3], [3, 1], [2, 3] and [3, 2]
 *
 * Code example -
 *
 *  new KPermutation&lt;&gt;(List.of("A","B","C"),2)
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JJNumberTools.permutationsOf("A","B","C")
 *      .k(2)
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [B, A]
 * [A, C]
 * [C, A]
 * [B, C]
 * [C, B]
 * </pre>
 * @author Deepesh Patel
 */
public final class KPermutationCombinationOrder<T> extends AbstractKPermutation<T> {


    /**
     * @param elements Input of size n from which unique permutations of size k will be generated.
     * @param k size of permutations. k must be &lt;=n
     */
    KPermutationCombinationOrder(List<T> elements, int k) {
        super(elements,k);
    }

    @Override
    public Iterator<List<T>> iterator() {
        if ( k==0 || elements.isEmpty())    return newEmptyIterator();
        /* Use the faster version */
        if( k== elements.size())     return new UniquePermItrForElements<>(elements, this::indicesToValues);
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        public Itr() {
            combinationIterator = new UniqueCombination<>(elements,k).iterator();
            getNextIterator();
        }

        @Override
        public boolean hasNext() {
            if(currentIterator.hasNext()) {
                return true;
            }

            if(!combinationIterator.hasNext()) {
                return false;
            }

            return getNextIterator().hasNext();

        }

        private Iterator<List<T>> getNextIterator(){
            currentIterator = new UniquePermutation<>(combinationIterator.next()).iterator();
            return currentIterator;
        }

        @Override
        public List<T> next() {
            return currentIterator.next();
        }
    }
}