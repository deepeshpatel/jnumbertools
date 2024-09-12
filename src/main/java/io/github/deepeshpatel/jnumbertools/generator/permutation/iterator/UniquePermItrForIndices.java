/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * An iterator that generates unique permutations of integer indices.
 * <p>
 * This iterator is used by permutation generators to produce permutations of indices.
 * The permutations are generated in lexicographical order.
 * <p>
 * Example usage:
 * <pre>
 * UniquePermItrForIndices iterator = new UniquePermItrForIndices(3);
 * while (iterator.hasNext()) {
 *     System.out.println(Arrays.toString(iterator.next()));
 * }
 * </pre>
 * The output for {@code new UniquePermItrForIndices(3)} will be:
 * <pre>
 * [0, 1, 2]
 * [0, 2, 1]
 * [1, 0, 2]
 * [1, 2, 0]
 * [2, 0, 1]
 * [2, 1, 0]
 * </pre>
 *
 * @since 1.0.3
 * @author Deepesh Patel
 */
public final class UniquePermItrForIndices implements Iterator<int[]> {

    private int[] indices;

    /**
     * Constructs an iterator that generates permutations of indices of the specified size.
     * The initial permutation is the identity permutation (0, 1, 2, ..., size-1).
     *
     * @param size the number of indices to permute
     */
    UniquePermItrForIndices(int size) {
        // Starting permutation index is zero
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
     * @return {@code true} if there are more permutations; {@code false} otherwise
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
        throw new NoSuchElementException("Reached to maximum permutation");
    }

    /**
     * Computes the next permutation in lexicographical order.
     *
     * @param n the current permutation
     * @return the next permutation as an array of integers
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

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
