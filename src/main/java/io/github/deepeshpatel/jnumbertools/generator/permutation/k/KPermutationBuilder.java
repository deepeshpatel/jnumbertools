package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.math.BigInteger;
import java.util.List;

public final class KPermutationBuilder<T> {

    private final List<T> elements;
    private final int r;
    private final Calculator calculator;

    public KPermutationBuilder(List<T> elements, int r, Calculator calculator) {
        this.elements = elements;
        this.r = r;
        this.calculator = calculator;
    }

    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(elements, r);
    }

    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(elements, r);
    }

    public KPermutationLexOrderMth<T> lexOrderMth(long m) {
        return lexOrderMth(BigInteger.valueOf(m));
    }

    public KPermutationLexOrderMth<T> lexOrderMth(BigInteger m) {
        return new KPermutationLexOrderMth<>(elements, r, m, calculator);
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m) {
        return combinationOrderMth(BigInteger.valueOf(m));
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m) {
        return new KPermutationCombinationOrderMth<>(elements, r, m, calculator);
    }

}
