/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;

import java.util.*;

/**
 * Implements the iterable of repetitive permutations where every element in input has associated count
 * which denotes home many times that element can be repeated.
 * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
 * <pre>
 *     Code example-
 *     new RepetitivePermutationLimitedSupply&lt;&gt;(Arrays.asList("A","B"),new int[]{2,1})
 *                     .forEach(System.out::println);
 *
 *     or
 *
 *     //here A can be repeated max 2 times and B can not be repeated(1 time)
 *     JNumberTools.permutationsOf("A","B")
 *                 .repetitiveWithSupply(2,1)
 *                 .forEach(System.out::println);
 *
 * will generate following -
 * [A, A, B]
 * [A, B, A]
 * [B, A, A]
 * </pre>
 * @author Deepesh Patel
 */
public class RepetitivePermutationLimitedSupply<T>  extends AbstractGenerator<T> {

    private final int[] supply;
    private final int[] initialIndices;

    /**
     *
     * @param seed input from which permutations are generated.
     *             Permutations are generated in lex order of indices of input values,
     *             considering value at each indices as unique.
     * @param supply int array containing the number of times(count) element in seed can be repeated.
     *               supply[0] contains the count for 0<sup>th</sup> element in seed
     *               supply[1] contains the count for 1<sup>st</sup> element in seed, ...
     *               supply[n] contains the count of n<sup>th</sup> element in seed
     *               count of every element must be &gt;=1
     */
    public RepetitivePermutationLimitedSupply(Collection<T> seed, int... supply) {
        super(seed);
        this.supply = supply;
        initialIndices = initIndices();
    }

    private int[] initIndices(){
        List<Integer> a = new ArrayList<>();
        for(int i=0; i<supply.length;i++) {
            a.addAll(Collections.nCopies(supply[i], i));
        }
        return a.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private final UniquePermutationIterator iterator;

        public Itr() {
            int[] indices = new int[initialIndices.length];
            System.arraycopy(initialIndices,0, indices,0, indices.length);
            iterator = new UniquePermutationIterator(indices);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public List<T> next() {
            int[] currentIndices = iterator.next();
            return AbstractGenerator.indicesToValues(currentIndices, seed);
        }
    }
}