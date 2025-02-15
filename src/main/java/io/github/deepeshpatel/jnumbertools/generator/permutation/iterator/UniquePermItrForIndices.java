/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * An iterator that generates unique permutations of integer indices in lexicographical order.
 * <p>
 * This iterator is designed to work with permutation generators by producing permutations of indices,
 * which can later be mapped to actual elements. For example, when instantiated with a size of 3, the
 * iterator produces the following sequence:
 * <pre>
 * [0, 1, 2]
 * [0, 2, 1]
 * [1, 0, 2]
 * [1, 2, 0]
 * [2, 0, 1]
 * [2, 1, 0]
 * </pre>
 * </p>
 * <p>
 * The iterator maintains the current permutation state in an array. When no further permutation exists,
 * the iterator’s {@code next()} method will throw a {@link NoSuchElementException}.
 * </p>
 *
 * @since 1.0.3
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermItrForIndices implements Iterator<int[]> {

    private int[] indices;

    /**
     * Constructs an iterator that generates permutations of indices of the specified size.
     * <p>
     * The initial permutation is the identity permutation: [0, 1, 2, ..., size - 1].
     * </p>
     *
     * @param size the number of indices to permute
     */
    UniquePermItrForIndices(int size) {
        // Starting permutation: identity sequence [0, 1, 2, ..., size - 1]
        this.indices = IntStream.range(0, size).toArray();
    }

    /**
     * Constructs an iterator with the specified starting permutation.
     *
     * @param startingPermutation the initial permutation of indices
     */
    public UniquePermItrForIndices(int[] startingPermutation) {
        this.indices = startingPermutation;
    }

    /**
     * Checks if there are more permutations to generate.
     *
     * @return {@code true} if a subsequent permutation exists; {@code false} otherwise
     */
    @Override
    public boolean hasNext() {
        return indices.length != 0;
    }

    /**
     * Returns the next permutation of indices.
     *
     * @return the next permutation as an array of integers
     * @throws NoSuchElementException if there are no more permutations
     */
    @Override
    public int[] next() {
        if (hasNext()) {
            int[] old = indices;
            indices = nextPermutation(indices);
            return old;
        }
        throw new NoSuchElementException("Reached maximum permutation");
    }

    /**
     * Computes the next permutation in lexicographical order.
     * <p>
     * The method follows these steps:
     * <ol>
     *   <li>Find the highest index <em>i</em> such that {@code c[i] < c[i+1]}.</li>
     *   <li>If no such index exists, the current permutation is the last permutation and an empty array is returned.</li>
     *   <li>Otherwise, find the highest index <em>j</em> such that {@code c[j] > c[i]}.</li>
     *   <li>Swap the values at indices <em>i</em> and <em>j</em>.</li>
     *   <li>Reverse the subarray from <em>i+1</em> to the end of the array.</li>
     * </ol>
     * </p>
     *
     * @param n the current permutation array
     * @return the next permutation as an array of integers, or an empty array if no further permutation exists
     */
    private static int[] nextPermutation(int[] n) {
        int[] c = Arrays.copyOf(n, n.length);
        int highestI = -1;

        // Find the rightmost index 'i' such that c[i] < c[i+1]
        for (int i = c.length - 2; i >= 0; i--) {
            if (c[i] < c[i + 1]) {
                highestI = i;
                break;
            }
        }

        // If no such index exists, this is the last permutation.
        if (highestI == -1) {
            return new int[]{};
        }

        // Find the rightmost index 'j' such that c[j] > c[highestI] and swap them.
        for (int j = c.length - 1; j > highestI; j--) {
            if (c[j] > c[highestI]) {
                swap(c, j, highestI);
                break;
            }
        }

        // Reverse the subarray from highestI + 1 to the end.
        for (int i = highestI + 1, j = c.length - 1; i < j; i++, j--) {
            swap(c, i, j);
        }

        return c;
    }

    /**
     * Swaps the elements at the specified indices in the array.
     *
     * @param a the array in which to swap elements
     * @param i the first index
     * @param j the second index
     */
    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
