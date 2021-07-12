package io.github.deepeshpatel.jnumbertools.generator.permutation;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Used by permutation generators in the this package
 * @author Deepesh Patel
 */
public class UniquePermutationIterator implements Iterator<int[]> {

    private int[] indices;

    protected UniquePermutationIterator(int[] indices) {
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

        int[] c = new int[n.length];
        System.arraycopy(n,0,c,0,c.length);

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
