/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utility for generating permutations with repeated values, starting from the 0ᵗʰ permutation
 * and then generating every nᵗʰ permutation in lexicographical order of the input indices.
 * <p>
 * This class is particularly useful when the total number of permutations is very large, making it impractical
 * to generate all permutations and then advance to a specific one. Instead, you can directly generate the next
 * nᵗʰ permutation. Instances of this class are intended to be created via a builder, so they do not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements to permute
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class RepetitivePermutationMth<T> extends AbstractGenerator<T> {

    private final int size;
    private final BigInteger increment;
    private final BigInteger start;

    /**
     * Constructs a new {@code RepetitivePermutationMth} instance.
     *
     * @param elements  the list of elements to permute
     * @param size      the size (length) of each permutation. This value can be greater than the number of unique elements due to repetition.
     * @param increment the step size for generating permutations; it determines how many permutations to skip between outputs.
     * @param start     the starting permutation index from which generation begins
     * @throws IllegalArgumentException if {@code increment} is less than or equal to zero
     */
    RepetitivePermutationMth(List<T> elements, int size, BigInteger increment, BigInteger start) {
        super(elements);
        this.size = size;
        this.start = start;
        this.increment = increment;
        AbstractGenerator.checkParamIncrement(increment, "repetitive permutations");
    }

    /**
     * Returns an iterator over repetitive permutations.
     * <p>
     * The iterator generates permutations in lexicographical order based on the indices of the input values.
     * </p>
     *
     * @return an iterator that produces each repetitive permutation as a {@code List<T>}
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new NumberIterator();
    }

    /**
     * Iterator implementation for generating repetitive permutations.
     */
    private class NumberIterator implements Iterator<List<T>> {

        private final int[] currentIndices = new int[size];
        private boolean hasNext;

        /**
         * Initializes the iterator with the starting permutation.
         * <p>
         * The iterator begins at the permutation corresponding to the {@code start} index.
         * </p>
         */
        public NumberIterator() {
            hasNext = nextRepetitiveKthPermutation(currentIndices, elements.size(), start);
        }

        /**
         * Returns {@code true} if there is a next permutation.
         *
         * @return {@code true} if the next permutation exists; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next repetitive permutation.
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if no further permutations are available
         */
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
         * Computes the next repetitive permutation based on the current indices and a given step size.
         * <p>
         * This method treats the current indices as digits of a number in base {@code base} (where {@code base} is the number of unique elements)
         * and adds the step size to it, propagating any carry to more significant digits.
         * </p>
         *
         * @param indices the current permutation represented as an array of indices
         * @param base    the total number of unique elements in the input list
         * @param k       the step size for generating the next permutation
         * @return {@code true} if the next permutation was successfully computed; {@code false} if the last permutation is reached
         */
        private boolean nextRepetitiveKthPermutation(int[] indices, int base, BigInteger k) {
            BigInteger nextK = k;
            for (int i = indices.length - 1; i >= 0; i--) {
                BigInteger sum = nextK.add(BigInteger.valueOf(indices[i]));
                BigInteger[] division = sum.divideAndRemainder(BigInteger.valueOf(base));
                nextK = division[0];
                indices[i] = division[1].intValue();
            }
            return BigInteger.ZERO.equals(nextK);
        }
    }
}
