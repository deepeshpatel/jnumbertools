package io.github.deepeshpatel.jnumbertools.generator.permutation.builder;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutation;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationsMth;

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

    public UniquePermutationsMth<T> lexOrderMth(BigInteger m) {
        //TODO: check if we can skip the factorial calculation
        return new UniquePermutationsMth<>(elements, m, calculator.factorial(elements.size()));
    }

    public UniquePermutationsMth<T> lexOrderMth(long m) {
        return lexOrderMth(BigInteger.valueOf(m));
    }

//    public AbstractGenerator<T> singleSwap() {
//        //TODO: Implement this
//        throw new RuntimeException("method not yet implemented..");
//    }
//
//    public AbstractGenerator<T> fast() {
//        //TODO: Implement this
//        throw new RuntimeException("method not yet implemented..");
//    }

}
