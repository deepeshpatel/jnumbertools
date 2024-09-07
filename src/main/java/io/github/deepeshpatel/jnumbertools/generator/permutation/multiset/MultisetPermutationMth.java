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

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.checkParamIncrement;
import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.initIndicesForMultisetPermutation;

/**
 * @author Deepesh Patel
 */
public final class MultisetPermutationMth<T>  extends AbstractGenerator<T> {

    private final int[] frequencies;
    private final BigInteger possiblePermutations;
    private final int initialSum;
    private final long start;
    private final long increment;
    private final Calculator calculator;

    /**
     *
     * @param elements input from which permutations are generated.
     *             Permutations are generated in lex order of indices of input values,
     *             considering value at each index as unique.
     * @param frequencies int array containing the number of times(count) element in input can be repeated.
     *               frequencies[0] contains the count for 0<sup>th</sup> element in input
     *               frequencies[1] contains the count for 1<sup>st</sup> element in input, ...
     *               frequencies[n] contains the count of n<sup>th</sup> element in input
     *               count of every element must be &gt;=1
     */
    MultisetPermutationMth(List<T> elements, long increment, long start, int[] frequencies, Calculator calculator) {

        super(elements);
        this.calculator = calculator;
        this.possiblePermutations  = findTotalPossiblePermutations(frequencies);
        this.start = start;
        this.increment = increment;
        checkParamIncrement(BigInteger.valueOf(increment), "Multiset permutations");

        this.frequencies = frequencies;
        this.initialSum = Arrays.stream(frequencies).sum();

    }

    //total multi-set permutations = ( ∑ f! / Π(f!) where f=freq
    private BigInteger findTotalPossiblePermutations(int[] count) {
        int sum = Arrays.stream(count).sum();
        BigInteger productOfFact = calculator.factorial(count[0]);
        for(int i=1; i<count.length; i++) {
            productOfFact = productOfFact.multiply(calculator.factorial(count[i]));
        }
        return  calculator.factorial(sum).divide(productOfFact);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        private long currentN = start;
        private int[] currentIndices;

        public Itr() {
            currentIndices = initIndicesForMultisetPermutation(frequencies);
        }

        @Override
        public boolean hasNext() {
            return  currentN < possiblePermutations.longValue();
        }

        @Override
        public List<T> next() {
            currentIndices = getIndicesForMthPermutation(currentN, initialSum);
            currentN = currentN + increment;
            return indicesToValues(currentIndices);
        }

        private int[] getIndicesForMthPermutation(long n, int initialSum){

            int[] output = new int[initialSum];
            int[] currentMultisetCount = Arrays.copyOf(frequencies, frequencies.length);
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