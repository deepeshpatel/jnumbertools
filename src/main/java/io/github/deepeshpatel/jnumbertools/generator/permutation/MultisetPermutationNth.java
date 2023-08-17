/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamIncrement;
import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.initIndicesForMultisetPermutation;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.factorial;

/**
 * @author Deepesh Patel
 */
public class MultisetPermutationNth<T>  extends AbstractGenerator<T> {

    private final int[] multisetFreqArray;
    private final BigInteger possiblePermutations;
    private final int initialSum;
    private final long increment;

    /**
     *
     * @param input input from which permutations are generated.
     *             Permutations are generated in lex order of indices of input values,
     *             considering value at each index as unique.
     * @param multisetFreqArray int array containing the number of times(count) element in input can be repeated.
     *               multisetFreqArray[0] contains the count for 0<sup>th</sup> element in input
     *               multisetFreqArray[1] contains the count for 1<sup>st</sup> element in input, ...
     *               multisetFreqArray[n] contains the count of n<sup>th</sup> element in input
     *               count of every element must be &gt;=1
     */
    public MultisetPermutationNth(Collection<T> input, long increment, int[] multisetFreqArray) {

        super(input);
        this.possiblePermutations  = findTotalPossiblePermutations(multisetFreqArray);
        this.increment = increment;
        checkParamIncrement(BigInteger.valueOf(increment), "Multiset permutations");

        this.multisetFreqArray = multisetFreqArray;
        this.initialSum = Arrays.stream(multisetFreqArray).sum();
    }

    private BigInteger findTotalPossiblePermutations(int[] count) {
        int sum = Arrays.stream(count).sum();
        BigInteger productOfFact = factorial(count[0]);
        for(int i=1; i<count.length; i++) {
            productOfFact = productOfFact.multiply(factorial(count[i]));
        }
        return  factorial(sum).divide(productOfFact);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private long currentN = 0;
        private int[] currentIndices;

        public Itr() {
            currentIndices = initIndicesForMultisetPermutation(multisetFreqArray);
        }

        @Override
        public boolean hasNext() {
            return  currentN < possiblePermutations.longValue();
        }

        @Override
        public List<T> next() {
            currentIndices = getIndicesForNthPermutation(currentN, initialSum);
            currentN = currentN + increment;
            return indicesToValues(currentIndices, seed);
        }

        private int[] getIndicesForNthPermutation(long n, int initialSum){

            int[] output = new int[initialSum];
            int[] currentMultisetCount = Arrays.copyOf(multisetFreqArray, multisetFreqArray.length);
            long sum = initialSum;

            BigInteger  totalPossible = possiblePermutations;

            for(int j=0; j<output.length; j++) {
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
            return  output;
        }
    }
}