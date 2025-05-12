/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.*;

public final class MultisetPermutationOFRanks<T> extends AbstractMultisetPermutation<T> {

    private final Iterable<BigInteger> ranks;

    public MultisetPermutationOFRanks(LinkedHashMap<T, Integer> multiset, Iterable<BigInteger> ranks, Calculator calculator) {
        super(multiset, calculator);
        this.ranks = ranks;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new SequenceIterator();
    }

    private class SequenceIterator implements Iterator<List<T>> {
        private final Iterator<BigInteger> rankIterator;

        public SequenceIterator() {
            this.rankIterator = ranks.iterator();
        }

        @Override
        public boolean hasNext() {
            return rankIterator.hasNext();
        }

        @Override
        public List<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more permutations available in rank sequence");
            }
            BigInteger rank = rankIterator.next();
            if (rank.compareTo(getTotalPermutations()) >= 0) {
                throw new IllegalArgumentException("Rank " + rank + " exceeds total permutations " + getTotalPermutations());
            }

            return generatePermutationFromRank(rank);
        }

        private List<T> generatePermutationFromRank(BigInteger rank) {
            int[] indices = getIndicesForMthPermutation(rank, totalLength);
            return indicesToValues(indices);
        }
    }

    private int[] getIndicesForMthPermutation(BigInteger m, int totalLength) {
        int[] output = new int[totalLength];
        int[] currentMultisetCount = Arrays.copyOf(frequencies, frequencies.length);
        long sum = totalLength;
        BigInteger totalPossible = getTotalPermutations();

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
