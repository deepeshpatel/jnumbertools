/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class generates the mth lexicographical permutation of a multiset where repetition of elements is allowed.
 * <p>
 * It uses a specified starting point and an increment to generate permutations in a lexicographical order.
 * This is particularly useful when dealing with large datasets where generating all previous permutations
 * is inefficient.
 * Instance of this class is intended to be created via builder and hence do not have any public constructor.
 *
 * @param <T> the type of elements in the multiset
 *
 * @author Deepesh Patel
 */
public final class MultisetPermutationMth<T> extends AbstractGenerator<T> {

    private final int[] frequencies;
    private final BigInteger possiblePermutations;
    private final int initialSum;
    private final long start;
    private final long increment;
    private final Calculator calculator;

    /**
     * Constructs a MultisetPermutationMth instance to generate the mth lexicographical permutation of a multiset.
     *
     * @param elements    the input list from which permutations are generated.
     *                    Permutations are generated in the lexicographical order of the indices of the input values,
     *                    considering the value at each index as unique.
     * @param increment   the step size for generating subsequent permutations.
     *                    This value should be positive.
     * @param start       the starting permutation number, where the first permutation is numbered as 0.
     * @param frequencies an array of integers representing the number of times each element in the input can be repeated.
     *                    <ul>
     *                    <li>frequencies[0] contains the count for the 0<sup>th</sup> element in the input.</li>
     *                    <li>frequencies[1] contains the count for the 1<sup>st</sup> element in the input.</li>
     *                    <li>frequencies[n] contains the count for the n<sup>th</sup> element in the input.</li>
     *                    </ul>
     *                    The count for every element must be &ge; 1.
     * @param calculator  an instance of {@link Calculator} to perform calculations required for generating permutations.
     */
    MultisetPermutationMth(List<T> elements, long increment, long start, int[] frequencies, Calculator calculator) {
        super(elements);
        checkParamMultisetFreqArray(elements.size(), frequencies, "mth permutation");
        checkParamIncrement(BigInteger.valueOf(increment), "Multiset permutations");
        this.calculator = calculator;
        this.possiblePermutations = findTotalPossiblePermutations(frequencies);
        this.start = start;
        this.increment = increment;
        this.frequencies = frequencies;
        this.initialSum = Arrays.stream(frequencies).sum();
    }

    /**
     * Calculates the total number of possible permutations of the multiset.
     *
     * @param count an array representing the frequencies of the elements in the multiset.
     * @return the total number of possible permutations.
     */
    private BigInteger findTotalPossiblePermutations(int[] count) {
        int sum = Arrays.stream(count).sum();
        BigInteger productOfFact = calculator.factorial(count[0]);
        for (int i = 1; i < count.length; i++) {
            productOfFact = productOfFact.multiply(calculator.factorial(count[i]));
        }
        return calculator.factorial(sum).divide(productOfFact);
    }

    /**
     * Returns an iterator over permutations of the multiset.
     *
     * @return an iterator over permutations.
     */
    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    /**
     * Iterator class to generate permutations one by one.
     */
    private class Itr implements Iterator<List<T>> {

        private long currentN = start;
        private int[] currentIndices;

        /**
         * Initializes the iterator by setting up the initial indices.
         */
        public Itr() {
            currentIndices = initIndicesForMultisetPermutation(frequencies);
        }

        /**
         * Checks if there are more permutations to generate.
         *
         * @return true if there are more permutations to generate; false otherwise.
         */
        @Override
        public boolean hasNext() {
            return currentN < possiblePermutations.longValue();
        }

        /**
         * Generates the next permutation.
         *
         * @return the next permutation as a list of elements.
         */
        @Override
        public List<T> next() {
            currentIndices = getIndicesForMthPermutation(currentN, initialSum);
            currentN = currentN + increment;
            return indicesToValues(currentIndices);
        }

        /**
         * Generates the indices for the mth permutation of the multiset.
         *
         * @param n          the permutation number to generate.
         * @param initialSum the sum of the frequencies of the elements in the multiset.
         * @return an array representing the indices of the elements in the mth permutation.
         */
        private int[] getIndicesForMthPermutation(long n, int initialSum) {
            int[] output = new int[initialSum];
            int[] currentMultisetCount = Arrays.copyOf(frequencies, frequencies.length);
            long sum = initialSum;
            BigInteger totalPossible = possiblePermutations;

            for (int j = 0; j < output.length; j++) {
                long countTillX = 0;

                for (int i = 0; i < currentMultisetCount.length; i++) {
                    long countOfX = totalPossible
                            .multiply(BigInteger.valueOf(currentMultisetCount[i]))
                            .divide(BigInteger.valueOf(sum))
                            .longValue();

                    long countTillPrevious = countTillX;
                    countTillX = countTillPrevious + countOfX;

                    if (n < countTillX) {
                        output[j] = i;
                        n = n - countTillPrevious;
                        currentMultisetCount[i] = currentMultisetCount[i] - 1;
                        sum = Arrays.stream(currentMultisetCount).sum();
                        break;
                    }
                }

                totalPossible = findTotalPossiblePermutations(currentMultisetCount);
            }
            return output;
        }
    }
}
