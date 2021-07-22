/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Implements the iterable of unique permutations of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each indices as unique.
 * For example all permutations of size 2 for [1,2,3] are -
 * <pre>
 *     [1, 2], [2, 1], [1, 3], [3, 1], [2, 3] and [3, 2]
 *
 * Code example -
 *
 *  new KPermutation&lt;&gt;(Arrays.asList("A","B","C"),2)
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
public class KPermutationCombinationOrder<T> extends AbstractGenerator<T> {

    final int k;

    /**
     * @param seed Input of size n from which unique permutations of size k will be generated.
     * @param k size of permutations. k must be &lt;=n
     */
    public KPermutationCombinationOrder(Collection<T> seed, int k) {
        super(seed);
        if(k<0) {
            throw new IllegalArgumentException(" k>=0");
        }
        this.k = k;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return (k==0) ? newEmptyIterator() : new OnDemandIterator();
    }

    private class OnDemandIterator implements Iterator<List<T>> {

        private final Iterator<List<T>> combinationIterator;
        private Iterator<List<T>> currentIterator;

        public OnDemandIterator() {
            combinationIterator = new UniqueCombination<>(seed,k).iterator();
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