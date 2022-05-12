/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.itertor;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.getClone;

/**
 * Used by permutation generators in this package
 * @author Deepesh Patel
 */
public class UniquePermutationIterator implements Iterator<int[]> {

    private int[] indices;

    public UniquePermutationIterator(int[] indices) {
        this.indices = indices;
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

    private int[] nextPermutation(int[] n) {

        int[] c =  getClone(n);
        int highestI = -1;

        for (int i = c.length - 2; i >= 0; i--) {
            if(c[i] <c[i+1]) {
                highestI = i;
                break;
            }
        }

        if(highestI == -1) {
            return new int[]{};
        }

        for (int j = c.length - 1; j > highestI; j--) {
            if(c[j] > c[highestI]) {
                int temp = c[j];
                c[j] = c[highestI];
                c[highestI]= temp;
                break;
            }
        }

        for (int i = highestI + 1, j = c.length - 1; i < j; i++, j--) {
            int t = c[i];
            c[i] = c[j];
            c[j] = t;
        }

        return c;
    }
}
