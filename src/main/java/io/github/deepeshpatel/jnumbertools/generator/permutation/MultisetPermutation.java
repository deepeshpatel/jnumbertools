/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.itertor.UniquePermutationIterator;

import java.util.*;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.initIndicesForMultisetPermutation;

/**
 * Implements the iterable of repetitive permutations where every element in input has associated count
 * which denotes home many times that element can be repeated.
 * Permutations are generated in lex order of indices of input values, considering value at each index as unique.
 * <pre>
 *     Code example-
 *     //here A can be repeated max 2 times and B can not be repeated(1 time)
 *     JNumberTools.permutationsOf("A","B")
 *                 ..repetitiveMultiset(2,1)
 *                 .forEach(System.out::println);
 *
 * will generate following -
 * [A, A, B]
 * [A, B, A]
 * [B, A, A]
 * </pre>
 * @author Deepesh Patel
 */
public class MultisetPermutation<T>  extends AbstractGenerator<T> {

    private final int[] initialIndices;

    /**
     *
     * @param input input from which permutations are generated.
     *             Permutations are generated in lex order of indices of input values,
     *             considering value at each index as unique.
     * @param multisetFreqArray int array containing the number of times(count) element in input can be repeated.
     *               multisetFreqArray[0] contains the count for 0<sup>th</sup> element in input
     *               multisetFreqArray[1] contains the count for 1<sup>st</sup> element in input, ...
     *               multisetFreqArray[n] contains the count of n<sup>th</sup> element in input
     *               count of every element must be &gt;=1
     */
    public MultisetPermutation(Collection<T> input, int... multisetFreqArray) {
        super(input);
        initialIndices = initIndicesForMultisetPermutation(multisetFreqArray);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private final UniquePermutationIterator iterator;

        public Itr() {
            iterator = new UniquePermutationIterator(initialIndices);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public List<T> next() {
            int[] currentIndices = iterator.next();
            return indicesToValues(currentIndices, seed);
        }
    }
}