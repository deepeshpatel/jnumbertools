/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2026 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.Util;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates every mᵗʰ repetitive permutation of a list of elements in lexicographical order.
 * <p>
 * This class computes permutations at regularly spaced 0-based ranks without generating
 * the permutations between those ranks. Elements are selected from the input list, and
 * each position is selected independently, so an element may appear more than once.
 * </p>
 * <p><b>Example:</b>
 * <pre>{@code
 * Calculator calculator = new Calculator();
 * List<String> items = List.of("A", "B");
 * RepetitivePermutationBuilder<String> builder =
 *     new RepetitivePermutationBuilder<>(2, items, calculator);
 *
 * // Get permutations at ranks 1, 3, ...
 * RepetitivePermutationMth<String> generator =
 *     builder.lexOrderMth(BigInteger.valueOf(2), BigInteger.ONE);
 * generator.forEach(System.out::println);
 * // Outputs: [A, B], [B, B]
 * }</pre>
 * <p>
 * <b>Note:</b> For n input elements and permutation width r, the total number of
 * repetitive permutations is nʳ.
 * </p>
 *
 * @param <T> the type of elements in the permutation
 * @author Deepesh Patel
 * @see RepetitivePermutation
 * @see RepetitivePermutationBuilder
 */
public final class RepetitivePermutationMth<T> extends AbstractGenerator<T> {

    private final int width;
    private final BigInteger increment;
    private final BigInteger start;

    /**
     * Constructs a generator for every mᵗʰ repetitive permutation.
     *
     * <p>
     * <strong>Note:</strong> This constructor is intended for internal use only.
     * Instances should be created via
     * {@link io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive.RepetitivePermutationBuilder#lexOrderMth(BigInteger, BigInteger)}.
     * All parameter validation is handled by the builder.
     * </p>
     *
     * @param elements the list of elements to permute (assumed non-null)
     * @param width the length of each permutation (assumed ≥ 0)
     * @param increment the step size between permutations (assumed > 0)
     * @param start the starting rank (assumed ≥ 0 and < total permutations when count > 0)
     */
    RepetitivePermutationMth(List<T> elements, int width, BigInteger increment, BigInteger start) {
        super(elements);
        this.width = width;
        this.start = start;
        this.increment = increment;
    }

    /**
     * Returns an iterator over every mᵗʰ repetitive permutation starting from the specified rank.
     *
     * @return an iterator producing each permutation as a {@code List<T>}
     */
    @Override
    public Iterator<List<T>> iterator() {
        //width = 0 -> one empty permutation (n⁰ = 1)
        if(width == 0 ) return Util.emptyListIterator();

        //empty elements with width > 0 -> no permutations (0ʳ = 0)
        if(elements.isEmpty()) return Collections.emptyListIterator();

        return new RepetitiveMthIterator();
    }

    /**
     * Iterator implementation for generating every mᵗʰ repetitive permutation.
     */
    private class RepetitiveMthIterator implements Iterator<List<T>> {
        private final int[] currentIndices = new int[width];
        private boolean hasNext;

        /**
         * Initializes the iterator with the starting permutation.
         */
        public RepetitiveMthIterator() {
            hasNext = incrementIndices(currentIndices, elements.size(), start);
        }

        /**
         * Checks if there is a next permutation.
         * <p>
         * Returns true if the previous increment operation succeeded, false if the permutation has
         * exceeded n^width implicitly via carry propagation.
         * </p>
         *
         * @return {@code true} if the next permutation exists; {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next mᵗʰ repetitive permutation.
         *
         * @return the next permutation as a {@code List<T>}
         * @throws NoSuchElementException if no further permutations are available
         */
        @Override
        public List<T> next() {
            if (!hasNext) {
                throw new NoSuchElementException("No more permutations available");
            }
            List<T> result = indicesToValues(currentIndices);
            hasNext = incrementIndices(currentIndices, elements.size(), increment);
            return result;
        }

        /**
         * Increments the current indices by the specified step size to generate the next permutation.
         * <p>
         * Treats the indices as a base-n number (n = number of elements) and adds the step size,
         * propagating carries to higher positions. For example, with elements [A, B] (n=2), width=2,
         * increment=1: [0,0] → [0,1] → [1,0] → [1,1] maps to [A,A], [A,B], [B,A], [B,B]. Returns false
         * when the carry propagates beyond the most significant digit, indicating exhaustion.
         * </p>
         *
         * @param indices the current permutation indices to update
         * @param base    the number of unique elements (n)
         * @param step    the step size to increment by (typically {@code increment} or {@code start} for initial)
         * @return {@code true} if the increment succeeded; {@code false} if exhausted
         */
        private boolean incrementIndices(int[] indices, int base, BigInteger step) {
            BigInteger nextK = step;
            for (int i = indices.length - 1; i >= 0; i--) {
                BigInteger sum = nextK.add(BigInteger.valueOf(indices[i]));
                BigInteger[] division = sum.divideAndRemainder(BigInteger.valueOf(base));
                nextK = division[0];
                indices[i] = division[1].intValue();
            }
            return nextK.equals(BigInteger.ZERO);
        }
    }
}
