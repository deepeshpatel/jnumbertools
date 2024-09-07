package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UniquePermutationBuilder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    public UniquePermutationBuilder(Calculator calculator, List<T> elements) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.calculator = calculator;
    }

    public UniquePermutation<T> lexOrder() {
        return new UniquePermutation<>(elements);
    }

    public UniquePermutationsMth<T> lexOrderMth(BigInteger m,  BigInteger start) {
        return new UniquePermutationsMth<>(elements, m,start, calculator);
    }

    public UniquePermutationsMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public AbstractGenerator<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

    public AbstractGenerator<T> fast() {
        //TODO: Implement this
        throw new RuntimeException("method not yet implemented..");
    }

}
