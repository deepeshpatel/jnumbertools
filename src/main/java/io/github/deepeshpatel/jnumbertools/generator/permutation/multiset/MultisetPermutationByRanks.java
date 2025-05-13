/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.*;

/**
 * Generates multiset permutations at specific lexicographical rank positions.
 * <p>
 * Given a multiset with element frequencies (f₁, f₂, ..., fₖ), this class efficiently computes
 * permutations at specified ranks without generating all preceding permutations.
 * The total number of permutations is n!/(f₁! × f₂! × ... × fₖ!) where n = ∑fᵢ.
 * </p>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * // Generate permutations at ranks 0, 2 for multiset [A, A, B]
 * LinkedHashMap<String, Integer> multiset = new LinkedHashMap<>();
 * multiset.put("A", 2);
 * multiset.put("B", 1);
 *
 * new MultisetPermutationByRanks<>(multiset, List.of(BigInteger.ZERO, BigInteger.valueOf(2)), calculator)
 *     .forEach(System.out::println);
 *
 * // Output:
 * // [A, A, B] (rank 0)
 * // [A, B, A] (rank 2)
 * }</pre>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @see MultisetPermutation
 * @see MultisetPermutationBuilder
 * @since 1.0.0
 */
public final class MultisetPermutationByRanks<T> extends AbstractMultisetPermutation<T> {

    private final Iterable<BigInteger> ranks;

    /**
     * Constructs a multiset permutation generator for specific ranks.
     *
     * @param multiset the multiset with element frequencies (must not be null or empty)
     * @param ranks the 0-based rank positions to generate (each rank must be 0 ≤ rank < n!/(∏fᵢ!))
     * @param calculator the calculator for combinatorial operations
     * @throws IllegalArgumentException if multiset is empty or any rank is invalid
     */
    public MultisetPermutationByRanks(LinkedHashMap<T, Integer> multiset, Iterable<BigInteger> ranks, Calculator calculator) {
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