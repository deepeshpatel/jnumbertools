/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator.initIndicesForMultisetPermutation;

public final class MultisetPermutationMth<T> extends AbstractMultisetPermutation<T> {

    private final BigInteger possiblePermutations;
    private final BigInteger start;
    private final BigInteger increment;

    MultisetPermutationMth(LinkedHashMap<T, Integer> multiset, BigInteger increment, BigInteger start,  Calculator calculator) {
        super(multiset, calculator);
        if (start.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Start must be non-negative: " + start);
        }
        this.possiblePermutations = getTotalPermutations();
        this.start = start;
        this.increment = increment;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {
        private BigInteger currentN = start;
        private int[] currentIndices;

        public Itr() {
            currentIndices = initIndicesForMultisetPermutation(frequencies);
        }

        @Override
        public boolean hasNext() {
            return currentN.compareTo(possiblePermutations) < 0;
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available");
            }
            currentIndices = getIndicesForMthPermutation(currentN, totalLength);
            currentN = currentN.add(increment);
            return indicesToValues(currentIndices);
        }

        private int[] getIndicesForMthPermutation(BigInteger m, int totalLength) {
            int[] output = new int[totalLength];
            int[] currentMultisetCount = Arrays.copyOf(frequencies, frequencies.length);
            long sum = totalLength;
            BigInteger totalPossible = possiblePermutations;

            for (int j = 0; j < output.length; j++) {
                BigInteger permutationsBefore = BigInteger.ZERO;

                for (int i = 0; i < currentMultisetCount.length; i++) {
                    if (currentMultisetCount[i] > 0) {
                        BigInteger perms = totalPossible.multiply(BigInteger.valueOf(currentMultisetCount[i]))
                                .divide(BigInteger.valueOf(sum));

                        BigInteger rangeEnd = permutationsBefore.add(perms);
                        if (m.compareTo(rangeEnd) < 0) {
                            output[j] = i;
                            m = m.subtract(permutationsBefore);
                            currentMultisetCount[i]--;
                            sum--;
                            totalPossible = perms;
                            break;
                        }
                        permutationsBefore = rangeEnd;
                    }
                }
            }
            return output;
        }

    }
}