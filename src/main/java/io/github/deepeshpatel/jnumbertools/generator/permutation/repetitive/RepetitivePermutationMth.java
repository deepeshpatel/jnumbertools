/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility to generate permutations with repeated values starting from
 * the 0<sup>th</sup> permutation (input) and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values. This is particularly useful when dealing
 * with large numbers of permutations, where directly computing all permutations and incrementing
 * to a specific permutation is impractical.
 * <p>
 * This class allows you to directly generate the next n<sup>th</sup> lexicographical permutation.
 * </p>
 *
 * <p>
 * Code example:
 * </p>
 * <pre>
 *      new RepetitivePermutationMth&lt;&gt;(List.of("A","B"),3,2)
 *                     .forEach(System.out::println);
 *
 *      or
 *
 *      JNumberTools.permutationsOf("A","B")
 *                 .repetitiveMth(3,2)
 *                 .forEach(System.out::println);
 *
 * will generate the following (0th, 2nd, 4th, and 6th) repetitive permutations of "A" and "B" of size 3:
 *
 * [A, A, A]
 * [A, B, A]
 * [B, A, A]
 * [B, B, A]
 * </pre>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 */
public final class RepetitivePermutationMth<T>  extends AbstractGenerator<T> {

    private final int size;
    private final long increment;
    private final long start;

    /**
     * Constructs a RepetitivePermutationMth instance.
     *
     * @param elements the list of elements to permute
     * @param size the size of each permutation. This can be greater than the number of elements due to repetition.
     * @param increment the step size for generating permutations
     * @param start the starting index for generating permutations
     */
    RepetitivePermutationMth(List<T> elements, int size, long increment, long start) {
        super(elements);
        this.size = size;
        this.start = start;
        this.increment = increment;
        AbstractGenerator.checkParamIncrement(BigInteger.valueOf(increment), "repetitive permutations");
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new NumberIterator();
    }

    private class NumberIterator implements Iterator<List<T>> {

        private final int[] currentIndices = new int[size];
        private boolean hasNext;

        /**
         * Initializes the iterator with the starting permutation index.
         */
        public NumberIterator() {
            hasNext = nextRepetitiveKthPermutation(currentIndices, elements.size(), start);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }

            List<T> result = indicesToValues(currentIndices);
            hasNext = nextRepetitiveKthPermutation(currentIndices, elements.size(), increment);
            return result;
        }

        /**
         * Computes the next repetitive permutation based on the current indices and step size.
         *
         * @param indices the current indices of the permutation
         * @param base the number of elements available for permutation
         * @param k the step size for generating permutations
         * @return true if there are more permutations, false otherwise
         */
        private boolean nextRepetitiveKthPermutation(int[] indices, int base, long k) {
            long nextK = k;

            for (int i = indices.length - 1; i >= 0; i--) {
                long v = (indices[i] + nextK) % base;
                nextK = (indices[i] + nextK) / base;
                indices[i] = (int) v;
            }
            return nextK == 0;
        }
    }
}
