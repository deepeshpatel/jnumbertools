/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.factorial;

/**
 * Utility to generate permutations with unique values starting from
 * 0<sup>th</sup> permutation(input) and then generate every n<sup>th</sup> permutation in
 * lexicographical order of indices of input values where each value is considered unique.
 * Generating n<sup>th</sup> permutation is important because say, if we need to
 * generate next 100 trillionth permutation
 * of 100 items then it will take months to compute if we go sequentially and then skip
 * the unwanted permutations because the total # of permutations is astronomical (100!= 9.3326E X 10<sup>157</sup>)
 *
 * This class will provide a mechanism to generate directly the next n<sup>th</sup>
 * lexicographical permutation.
 * <pre>
 *  Code example:
 *         new UniquePermutationsNth&lt;&gt;(Arrays.asList("A","B","C"),2)
 *                     .forEach(System.out::println);
 *         or
 *
 *         JNumberTools.permutationsOf("A","B","C")
 *                 .uniqueNth(2)
 *                 .forEach(System.out::println);
 *
 *  will generate following (0th, 2nd and 4th) unique permutations of A,B and C)-
 *
 * [A, B, C]
 * [B, A, C]
 * [C, A, B]
 * </pre>
 *
 * @author Deepesh Patel
 */
public class UniquePermutationsNth<T> extends AbstractGenerator<T> {

    private final BigInteger withIncrement;

    public UniquePermutationsNth(Collection<T> seed, BigInteger skipTo) {
        super(seed);
        this.withIncrement = skipTo;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new KthItemIterator();
    }

    private class KthItemIterator implements  Iterator<List<T>> {

        private int[] currentIndices;
        private final int[] initialIndices;

        private boolean hasNext = true;
        final BigInteger k;
        BigInteger nextK;
        final BigInteger numOfPermutations;

        public KthItemIterator() {
            initialIndices = IntStream.range(0, seed.size()).toArray();
            currentIndices = IntStream.range(0, seed.size()).toArray();

            this.k = withIncrement;
            this.nextK = withIncrement;
            numOfPermutations = factorial(initialIndices.length);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<T> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            int[] oldIndices = currentIndices;
            currentIndices = nextPermutation(initialIndices, nextK);
            hasNext = nextK.compareTo(numOfPermutations) <0;
            nextK = nextK.add(k);
            return AbstractGenerator.indicesToValues(oldIndices, seed);
        }

        public int[] nextPermutation( int[] input, BigInteger kth) {
            int[] indices = factoradic(kth, input.length);
            return createLehmerCode(input, indices);
        }

        private  int[] factoradic(BigInteger k, int outputLength) {

            int[] output = new int[outputLength];

            BigInteger d = BigInteger.ONE;
            long kDivideByD;
            for (int i = 1; i <=output.length; i++) {

                kDivideByD = k.divide(d).longValue();

                if(kDivideByD == 0)
                    break;

                //save in reverse order for correct printing order while testing.
                output[output.length-i] = (int)kDivideByD % i;
                d = d.multiply(BigInteger.valueOf(i));
            }
            return output;
        }

        private int[] createLehmerCode(int[] input, int[] factoradic){
            int[] output = new int[input.length];
            System.arraycopy(input, 0, output,0, input.length);

            for (int i = 0; i < output.length; i++) {
                int index = factoradic[i] + i;

                if(index != i) {
                    int temp = output[index];
                    if (index - i >= 0) {
                        System.arraycopy(output, i, output, i + 1, index - i);
                    }
                    output[i] = temp;
                }
            }
            return output;
        }
    }
}