/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * Internal iterator for generating lexicographically ordered permutation indices.
 * Intended for internal use only; use the public permutation builders instead.
 * @author Deepesh Patel
 */
public final class UniquePermutationLexIndicesIterator implements Iterator<int[]> {

    private int[] indices;
    private boolean exhausted;

    UniquePermutationLexIndicesIterator(int size) {
        this.indices = IntStream.range(0, size).toArray();
        this.exhausted = (size == 0);
    }

    public UniquePermutationLexIndicesIterator(int[] startingPermutation) {
        // Defensive copy to prevent caller from mutating our internal state
        if (startingPermutation != null) {
            this.indices = Arrays.copyOf(startingPermutation, startingPermutation.length);
        } else {
            this.indices = new int[0];
        }
        this.exhausted = (this.indices.length == 0);
    }

    @Override
    public boolean hasNext() {
        return !exhausted;
    }

    @Override
    public int[] next() {
        if (exhausted) { throw new NoSuchElementException("Reached maximum permutation");}
        int[] result = Arrays.copyOf(indices, indices.length);
        if (!advanceToNextPermutation(indices)) { exhausted = true;}
        return result;
    }

    private static boolean advanceToNextPermutation(int[] n) {
        int highestI = -1;

        for (int i = n.length - 2; i >= 0; i--) {
            if (n[i] < n[i + 1]) {
                highestI = i;
                break;
            }
        }

        if (highestI == -1) {
            return false;
        }

        for (int j = n.length - 1; j > highestI; j--) {
            if (n[j] > n[highestI]) {
                swap(n, j, highestI);
                break;
            }
        }

        for (int i = highestI + 1, j = n.length - 1; i < j; i++, j--) {
            swap(n, i, j);
        }

        return true;
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
