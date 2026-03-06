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

/**
 * Generates every mᵗʰ multiset permutation in lexicographical order, starting from a specified rank.
 * <p>
 * This class produces permutations of a multiset with specified multiplicities, output as frequency maps,
 * in lexicographical order or by rank. The number of permutations is given by the multinomial coefficient
 * divided by m for every mᵗʰ permutation:
 * </p>
 * <p>
 * <strong>Formula:</strong> Total permutations = n!/(n₁!·n₂!·...·nₖ!)/m
 * <br>
 * where nᵢ are the multiplicities of each distinct element.
 * </p>
 * <p>
 * <strong>Performance:</strong>
 * <ul>
 *   <li>Time Complexity: O(1) per permutation (direct computation)</li>
 *   <li>Space Complexity: O(k) where k is number of distinct elements</li>
 *   <li>Memory Efficient: Generates permutations on-demand</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Example:</strong> For multiset {A:2, B:1, C:1} with m=2, start=0:
 * Generates 0th, 2nd, 4th... permutations instead of all 12 permutations.
 * </p>
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 * @see <a href="https://en.wikipedia.org/wiki/Multinomial_coefficient">Wikipedia: Multinomial Coefficient</a>
 */
public final class MultisetPermutationMth<T> extends AbstractMultisetPermutation<T> {

    private final BigInteger possiblePermutations;
    private final BigInteger start;
    private final BigInteger increment;

    /**
     * Constructs a generator for every mᵗʰ multiset permutation.
     *
     * @param multiset   the multiset with element multiplicities (must not be null)
     * @param increment  the step size between permutations (must be positive)
     * @param start      the starting rank (must be non-negative)
     * @param calculator the calculator for combinatorial computations (must not be null)
     * @throws IllegalArgumentException if multiset or calculator is null, increment ≤ 0, or start < 0
     */
    MultisetPermutationMth(LinkedHashMap<T, Integer> multiset, BigInteger increment, BigInteger start, Calculator calculator) {
        super(multiset, calculator);
        if (start.signum() < 0) {
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
            currentIndices = createFlattenedIndices(frequencies);
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