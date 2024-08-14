package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

public abstract class AbstractKPermutation<T> extends AbstractGenerator<T> {

    protected final int k;

    public AbstractKPermutation(List<T> seed, int k) {
        super(seed);
        this.k = k;
        checkBoundsOfK(seed.size(), k);
    }

    protected void checkBoundsOfK(int inputSize, int k) {
        if(k > inputSize) {
            throw new IllegalArgumentException("k should be <= input length to generate permutation");
        }
        if(k < 0) {
            throw new IllegalArgumentException("k should be >= 0 to generate permutation");
        }
    }
}
