/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamKPermutation;
import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.newEmptyIterator;

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
public class KPermutationLexOrder<T> extends AbstractGenerator<T> {

    final int k;

    /**
     * @param input Input of size n from which unique permutations of size k will be generated.
     * @param k size of permutations. k must be &lt;=n
     */
    public KPermutationLexOrder(Collection<T> input, int k) {
        super(input);
        this.k = k;
        checkParamKPermutation(seed.size(), k, "K-permutation");
    }

    @Override
    public Iterator<List<T>> iterator() {
        return (k==0) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;
        final LinkedList<Integer> list;

        public Itr() {
            indices = IntStream.range(0, k).toArray();
            list= new LinkedList<>();
            for(int i=k; i<seed.size(); i++) {
                list.add(i);
            }
        }

        @Override
        public boolean hasNext() {
            return indices.length > 0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = indices;
            indices = kPermutationNextLex(indices,list, seed.size()-1);
            return  indicesToValues(old, seed);
        }

        private int[] kPermutationNextLex(int[] current, LinkedList<Integer> remaining, int maxAllowed) {

            int[] a = Arrays.copyOf(current, current.length);

            if(!remaining.isEmpty()) {
                int next = remaining.removeFirst();
                a[a.length-1] = next;
                return a;
            }

            int index = -1;
            for(int i=a.length-1; i>0; i--) {
                if(a[i-1] < a[i]) {
                    index = i-1;
                    break;
                }
            }

            if(index ==-1) {
                //exceeds last permutation
                return new int[0];
            }

            remaining.addAll(IntStream.range(0,maxAllowed+1).boxed().toList());

            for(int i=0; i<index; i++) {
                remaining.remove(Integer.valueOf(a[i]));
            }
            int valueForCurrentIndex = a[index]+1;
            while(!remaining.contains(valueForCurrentIndex)) {
                valueForCurrentIndex++;
            }
            a[index] = valueForCurrentIndex;
            remaining.remove(Integer.valueOf(valueForCurrentIndex));

            for(int i=index+1; i<a.length; i++) {
                a[i] = remaining.removeFirst();
            }
            return a;
        }
    }
}