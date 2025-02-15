/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.numbersystem.FactoradicAlgorithms;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Utility to generate permutations with unique values starting from a given permutation number,
 * and then to generate every nᵗʰ permutation in lexicographical order of the input indices,
 * where each value at a specific index (e.g. elements₀, elements₁, …, elementsₙ₋₁) is treated as unique.
 * <p>
 * Directly generating the nᵗʰ permutation is crucial for efficiently handling cases where generating
 * permutations sequentially is infeasible due to the enormous number of possibilities (e.g., 100! for 100 items).
 * </p>
 * <p>
 * <strong>Note:</strong> This class is intended to be constructed via a builder and does not have a public constructor.
 * </p>
 *
 * @param <T> the type of elements to permute
 * @since 1.0.3
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutationsMth<T> extends AbstractGenerator<T> {

    private final BigInteger start;
    private final BigInteger increment;
    private BigInteger numOfPermutations;
    private final int[] initialIndices;
    private final Calculator calculator;

    /**
     * Constructs a new {@code UniquePermutationsMth} instance with the specified parameters.
     *
     * @param elements   the list of elements to permute
     * @param increment  the increment to generate the next nᵗʰ permutation (i.e. the step size)
     * @param start      the starting index for permutation generation
     * @param calculator a calculator instance for computing factorials and related values
     * @throws IllegalArgumentException if {@code increment} is less than or equal to zero
     */
    UniquePermutationsMth(List<T> elements, BigInteger increment, BigInteger start, Calculator calculator) {
        super(elements);
        this.start = start;
        this.increment = increment;
        checkParamIncrement(increment, "unique permutations");
        this.calculator = calculator;
        initialIndices = IntStream.range(0, elements.size()).toArray();
    }

    /**
     * Builds and returns the mᵗʰ permutation directly.
     * <p>
     * Use this method if you need only a specific mᵗʰ permutation rather than an iterator over a sequence.
     * </p>
     *
     * @return the mᵗʰ permutation as a {@code List<T>}
     */
    public List<T> build() {
        var indices = FactoradicAlgorithms.unRank(increment, initialIndices.length);
        return indicesToValues(indices);
    }

    @Override
    public Iterator<List<T>> iterator() {
        synchronized (this) {
            numOfPermutations = numOfPermutations == null ? calculator.factorial(elements.size()) : numOfPermutations;
        }
        return new KthItemIterator();
    }

    private class KthItemIterator implements Iterator<List<T>> {

        private BigInteger nextK = start;

        @Override
        public boolean hasNext() {
            return nextK.compareTo(numOfPermutations) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] currentIndices = nextPermutation(nextK, initialIndices.length);
            nextK = nextK.add(increment);
            return indicesToValues(currentIndices);
        }

        /**
         * Calculates the kᵗʰ permutation given its rank.
         *
         * @param kth           the rank of the permutation to compute
         * @param numberOfItems the total number of items
         * @return an array of indices representing the nᵗʰ permutation
         */
        public int[] nextPermutation(BigInteger kth, int numberOfItems) {
            return FactoradicAlgorithms.unRank(kth, numberOfItems);
        }
    }
}
