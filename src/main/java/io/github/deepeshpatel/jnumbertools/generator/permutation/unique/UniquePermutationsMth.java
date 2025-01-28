/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
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
 * Utility to generate permutations with unique values starting from
 * ginve permutation number and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values where each value is considered unique.
 * <p>
 * Generating the n<sup>th</sup> permutation directly is crucial for efficiently handling cases where
 * generating permutations sequentially would be infeasible due to the enormous number of possible
 * permutations (e.g., 100! permutations for 100 items).
 * <p>
 * This class provides a mechanism to generate directly the next n<sup>th</sup> lexicographical
 * permutation.
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @author Deepesh Patel
 * @param <T> the type of elements to permute
 */
public final class UniquePermutationsMth<T> extends AbstractGenerator<T> {

    private final BigInteger start;
    private final BigInteger increment;
    private BigInteger numOfPermutations;
    private final int[] initialIndices;
    private final Calculator calculator;

    /**
     * Constructs a UniquePermutationsMth with the specified parameters.
     *
     * @param elements the list of elements to permute
     * @param increment the increment to generate the next n<sup>th</sup> permutation
     * @param start the starting index for permutation generation
     * @param calculator a calculator instance for computing factorials
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
     * Builds and returns the m<sup>th</sup> permutation directly.
     * <p>
     * Use this method instead of the iterator if you need only a specific m<sup>th</sup> permutation
     * and not a sequence of permutations.
     *
     * @return the m<sup>th</sup> permutation as a List of elements
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
         * Calculates the k<sup>th</sup> permutation given its rank.
         *
         * @param kth the rank of the permutation
         * @param numberOfItems the total number of items
         * @return the indices representing the n<sup>th</sup> permutation
         */
        public int[] nextPermutation(BigInteger kth, int numberOfItems) {
            return FactoradicAlgorithms.unRank(kth, numberOfItems);
        }
    }
}
