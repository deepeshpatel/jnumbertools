package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.*;

public abstract class AbstractKPermutation<T> extends AbstractGenerator<T> {

    protected final int k;

    protected AbstractKPermutation(List<T> elements, int k) {
        super(elements);
        this.k = k;
        checkBoundsOfK(elements.size(), k);
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
