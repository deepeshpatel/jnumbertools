package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.util.List;

public final class MultisetPermutationBuilder<T> {

    private final List<T>  elements;
    private final int[] frequencies;
    private final Calculator calculator;

    public MultisetPermutationBuilder(List<T> elements, int[] frequencies, Calculator calculator) {
        this.elements = elements;
        this.frequencies = frequencies;
        this.calculator = calculator;
        assertArguments();
    }

    private void assertArguments() {
        if(frequencies == null || frequencies.length < elements.size() || !frequenciesArePositive()) {
            throw new IllegalArgumentException("frequencies should be (1) Not null and (2) its length should be equal to" +
                    " number of elements and (3) should contain +ve values");
        }
    }

    private boolean frequenciesArePositive() {
        for(int e: frequencies) {
            if(e<1) {
                return false;
            }
        }
        return true;
    }

    public MultisetPermutation<T> lexOrder() {
        return new MultisetPermutation<>(elements, frequencies);
    }

    public MultisetPermutationMth<T> lexOrderMth(long m) {
        //TODO: also implement for BigInteger increment
        return new MultisetPermutationMth<>(elements, m , frequencies, calculator);
    }



}
