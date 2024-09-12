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
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * <p>
 * This class allows you to directly generate the next n<sup>th</sup> lexicographical permutation.
 * <p>
 *
 * @param <T> the type of elements to permute
 * @author Deepesh Patel
 */
public final class RepetitivePermutationMth<T>  extends AbstractGenerator<T> {

    private final int size;
    private final BigInteger increment;
    private final BigInteger start;

    /**
     * Constructs a RepetitivePermutationMth instance.
     *
     * @param elements the list of elements to permute
     * @param size the size of each permutation. This can be greater than the number of elements due to repetition.
     * @param increment the step size for generating permutations
     * @param start the starting index for generating permutations
     */
    RepetitivePermutationMth(List<T> elements, int size, BigInteger increment, BigInteger start) {
        super(elements);
        this.size = size;
        this.start = start;
        this.increment = increment;
        AbstractGenerator.checkParamIncrement(increment, "repetitive permutations");
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
        private boolean nextRepetitiveKthPermutation(int[] indices, int base, BigInteger k) {
            BigInteger nextK = k;

            for (int i = indices.length - 1; i >= 0; i--) {

                BigInteger sum = nextK.add(BigInteger.valueOf(indices[i]));
                BigInteger[] division = sum.divideAndRemainder(BigInteger.valueOf(base));
                nextK = division[0];
                indices[i] = division[1].intValue();
            }
            return  BigInteger.ZERO.equals(nextK);
        }
    }
}
