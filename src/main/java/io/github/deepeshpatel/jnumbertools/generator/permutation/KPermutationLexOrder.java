/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class KPermutationLexOrder<T> extends AbstractGenerator<T> {

    final int k;

    /**
     * @param seed Input of size n from which unique permutations of size k will be generated.
     * @param k size of permutations. k must be &lt;=n
     */
    public KPermutationLexOrder(Collection<T> seed, int k) {
        super(seed);
        if(k<0) {
            throw new IllegalArgumentException(" k>=0");
        }
        this.k = k;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return (k==0 || k> seed.size()) ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        int[] indices;
        LinkedList<Integer> list;

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
            return  AbstractGenerator.indicesToValues(old, seed);
        }

        private int[] kPermutationNextLex(int[] current, LinkedList<Integer> remaining, int maxAllowed) {

            int[] a = new int[current.length];
            System.arraycopy(current, 0,a,0, a.length);

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
                return new int[0];
            }

            remaining.addAll(IntStream.range(0,maxAllowed+1).boxed().collect(Collectors.toList()));

            for(int i=0; i<index; i++) {
                remaining.remove(Integer.valueOf(a[i]));
            }
            int valueForCurrentIndex = a[index]+1;
            while(!remaining.contains(valueForCurrentIndex)) {
                valueForCurrentIndex++;
            }
            a[index] = valueForCurrentIndex;
            remaining.remove(Integer.valueOf(valueForCurrentIndex));

            for(int i=index+1; i< a.length; i++) {
                a[i] = remaining.removeFirst();
            }
            return a;
        }
    }
}