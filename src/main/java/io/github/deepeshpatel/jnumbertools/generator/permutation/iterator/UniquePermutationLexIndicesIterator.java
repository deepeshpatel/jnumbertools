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
 * This iterator produces all nₙ! permutations of indices {0, 1, ..., nₙ−1} (e.g., [0, 1, 2], [0, 2, 1], etc.),
 * where nₙ is the input size. These can be mapped to actual elements by higher-level classes. For example,
 * with size=3, it generates:
 * <pre>
 * [0, 1, 2], [0, 2, 1], [1, 0, 2], [1, 2, 0], [2, 0, 1], [2, 1, 0]
 * </pre>
 * When no further permutations exist, the iterator signals completion by returning an empty array,
 * and subsequent calls to {@code next()} throw a {@link NoSuchElementException}.
 * </p>
 *
 * @author Deepesh Patel
 */
public final class UniquePermutationLexIndicesIterator implements Iterator<int[]> {

    private int[] indices;

    /**
     * Constructs an iterator for permutations of indices of the specified size.
     * <p>
     * Initializes with the identity permutation: [0, 1, 2, ..., nₙ−1].
     * </p>
     *
     * @param size the number of indices to permute (nₙ); must be non-negative
     * @return a new UniquePermutationLexIndicesIterator instance
     * @throws IllegalArgumentException if size is negative
     */
    UniquePermutationLexIndicesIterator(int size) {
        this.indices = IntStream.range(0, size).toArray();
    }

    /**
     * Constructs an iterator with a specified starting permutation.
     *
     * @param startingPermutation the initial permutation of indices; must be a valid permutation of {0, 1, ..., nₙ−1}
     * @return a new UniquePermutationLexIndicesIterator instance
     * @throws IllegalArgumentException if startingPermutation is invalid
     */
    public UniquePermutationLexIndicesIterator(int[] startingPermutation) {
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
     * @throws NoSuchElementException if no more permutations are available
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
     * The algorithm:
     * <ol>
     *   <li>Finds the rightmost index i where cᵢ < cᵢ₊₁.</li>
     *   <li>If no such index exists, returns an empty array (indicating the last permutation).</li>
     *   <li>Finds the rightmost index j where cⱼ > cᵢ and swaps cᵢ and cⱼ.</li>
     *   <li>Reverses the subarray from i+1 to the end.</li>
     * </ol>
     * </p>
     *
     * @param n the current permutation array
     * @return the next permutation as an array of integers, or an empty array if no further permutation exists
     */
    private static int[] nextPermutation(int[] n) {
        int[] c = Arrays.copyOf(n, n.length);
        int highestI = -1;

        for (int i = c.length - 2; i >= 0; i--) {
            if (c[i] < c[i + 1]) {
                highestI = i;
                break;
            }
        }

        if (highestI == -1) {
            return new int[]{};
        }

        for (int j = c.length - 1; j > highestI; j--) {
            if (c[j] > c[highestI]) {
                swap(c, j, highestI);
                break;
            }
        }

        for (int i = highestI + 1, j = c.length - 1; i < j; i++, j--) {
            swap(c, i, j);
        }

        return c;
    }

    /**
     * Swaps two elements in the array at the specified indices.
     *
     * @param a the array to modify
     * @param i the index of the first element
     * @param j the index of the second element
     */
    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}