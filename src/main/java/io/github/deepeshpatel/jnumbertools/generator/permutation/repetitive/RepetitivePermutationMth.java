/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility to generate permutations with repeated values starting from
 * 0<sup>th</sup> permutation(input) and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values. This is important because say, if we need to
 * generate next 1 billionth permutation of [A,B,C,D,E,F,G,H,I,J,K,L,M] then it is not
 * feasible to generate all 13^13=302875106592253 permutations and then increment to a billionth permutation.
 * <p>
 * This class will provide a mechanism to generate directly the next n<sup>th</sup>
 * lexicographical permutation.
 *
 *  <pre>
 *      Code example -
 *      new RepetitivePermutationMth&lt;&gt;(List.of("A","B"),3,2)
 *                     .forEach(System.out::println);
 *
 *      or
 *
 *      JNumberTools.permutationsOf("A","B")
 *                 .repetitiveMth(3,2)
 *                 .forEach(System.out::println);
 *
 * will generate following (0th, 2nd 4th and 6th) repetitive permutation of "A" and "B of size 3
 *
 * [A, A, A]
 * [A, B, A]
 * [B, A, A]
 * [B, B, A]
 *  </pre>
 *
 * @author Deepesh Patel
 */
public class RepetitivePermutationMth<T>  extends AbstractGenerator<T> {

    private final int size;
    private final long increment;

    public RepetitivePermutationMth(List<T> input, int size, long increment) {
        super(input);
        this.size = size;
        this.increment = increment;
        CombinatoricsUtil.checkParamIncrement(BigInteger.valueOf(increment), " repetitive permutations");
    }

    @Override
    public  Iterator<List<T>> iterator() {
        return new NumberIterator();
    }

    private class NumberIterator implements Iterator<List<T>> {

        private final int[] currentIndices = new int[size];
        private boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            List<T> result = indicesToValues(currentIndices, seedElements);
            hasNext = nextRepetitiveKthPermutation(currentIndices, seedElements.size(), increment);
            return result;
        }

        private boolean nextRepetitiveKthPermutation(int[] indices, int base, long k) {

            long nextK = k;

            for (int i = indices.length-1; i >= 0; i--) {
                long v = (indices[i] + nextK) % base;
                nextK = (indices[i] + nextK) / base;
                indices[i] = (int) v;
            }
            return nextK == 0;
        }
    }
}