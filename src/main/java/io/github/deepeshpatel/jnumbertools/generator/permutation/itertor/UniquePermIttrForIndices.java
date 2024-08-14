/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.itertor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;


/**
 * Used by permutation generators in this package
 * @author Deepesh Patel
 */
public class UniquePermIttrForIndices implements Iterator<int[]> {

    private int[] indices;


    public UniquePermIttrForIndices(int size) {
        //starting permutation is zero
        this.indices = IntStream.range(0, size).toArray();
    }

    public UniquePermIttrForIndices(int[] startingPermutation) {
        this.indices = startingPermutation;
    }

    @Override
    public boolean hasNext() {
        return indices.length!=0;
    }

    @Override
    public int[] next() {

        if (hasNext()) {
            int[] old = indices;
            indices = nextPermutation(indices);
            return old;
        }

        throw new NoSuchElementException("Reached to maximum permutation");
    }

    private static int[] nextPermutation(int[] n) {

        int[] c = Arrays.copyOf(n,n.length);
        int highestI = -1;

        for (int i = c.length - 2; i >= 0; i--) {
            if(c[i] < c[i+1]) {
                highestI = i;
                break;
            }
        }

        if(highestI == -1) {
            return new int[]{};
        }

        for (int j = c.length - 1; j > highestI; j--) {
            if(c[j] > c[highestI]) {
                swap(c, j, highestI);
                break;
            }
        }

        for (int i = highestI + 1, j = c.length - 1; i < j; i++, j--) {
            swap(c, i, j);
        }

        return c;
    }

    private static void swap(int[]a, int i, int j) {
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
    }
}
