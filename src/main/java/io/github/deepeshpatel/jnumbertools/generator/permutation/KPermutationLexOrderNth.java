/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
 * Permutations are generated in lex order of combinations of indices of input values, considering value at each indices as unique.
 * <pre>
 * Code example -
 *
 *  new KPermutationNth&lt;&gt;(Arrays.asList("A","B","C"),2,2) //size =2 and skipTo every 2nd permutation
 *      .forEach(System.out::println);
 *
 *  or
 *
 *  JNumberTools.permutationsOf("A","B","C")
 *      .kNth(2,2) //size =2 and skipTo every 2nd permutation starting from 0
 *      .forEach(System.out::println);
 *
 * will generate -
 * [A, B]
 * [A, C]
 * [B, C]
 * </pre>
 * @author Deepesh Patel
 */
public class KPermutationLexOrderNth<T> extends AbstractGenerator<T> {

    final int k;
    //TODO: use BigInteger
    final long skip;

    /**
     * Implements the iterable generating every n<sup>th</sup> unique permutation of size k.
     * Permutations are generated in lex order of indices of input values, considering value at each indices as unique.
     * @param seed Input of size n from which permutations of size k will be generated
     * @param k size of permutations. k must be &lt;=n
     * @param skipTo position relative to first permutation which will be generated next in lexicographical order.
     *                  skipping every skipTo-1 permutation in a sequence.
     *                  For example, if skipTo is set to 2, the output will generate 0<sup>th</sup>, 2<sup>nd</sup>,
     *                  4<sup>th</sup>.. permutations.
     *                  This is important because for large k, it is impractical to generate all possible k! permutations
     *                  and then skip to the desired position
     */

    public KPermutationLexOrderNth(Collection<T> seed, int k, long skipTo) {
        super(seed);

        if(k<0 || k>seed.size()) {
            throw new IllegalArgumentException("k must be >= 0 and < length of input");
        }
        if(skipTo <=0) {
            throw new IllegalArgumentException("skipTo must be > 0");
        }
        this.k = k;
        this.skip = skipTo;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return k==0 ? newEmptyIterator() : new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        final int[] initialValue;
        int[] next;
        long currentSkip;
        long nPk;
        long divisor;

        public Itr() {
            //TODO: move this outside Iterator as this is not going to be changed
            initialValue = IntStream.range(0,k).toArray();
            next = initialValue;
            currentSkip = skip;
            nPk = MathUtil.nPr(seed.size(),initialValue.length);
            divisor = nPk/seed.size();
        }

        @Override
        public boolean hasNext() {
            return next.length >0;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] old = next;
            next = nextNthKPermutation(initialValue,seed.size(), currentSkip, divisor );
            currentSkip +=  skip;
            return  AbstractGenerator.indicesToValues(old, seed);
        }

        private int[] nextNthKPermutation(int[] initialValue, int size, long n, long divisor) {

            if(n >= nPk) {
                return new int[0];
            }

            int[] a = new int[initialValue.length];
            System.arraycopy(initialValue, 0, a,0, a.length );
            List<Integer> allValues = new ArrayList<>(IntStream.range(0, size).boxed().collect(Collectors.toList()));
            //allValues.addAll();

            long next = size-1;
            long remainingValue = n;

            for(int i=0; i<a.length; i++) {
                int quotient = (int) (remainingValue / divisor);
                remainingValue = remainingValue % divisor;
                a[i] = allValues.remove(quotient);
                divisor = divisor / next;
                next--;
            }
            return a;
        }
    }
}