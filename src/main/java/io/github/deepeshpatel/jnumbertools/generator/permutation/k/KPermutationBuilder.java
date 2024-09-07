package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

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

    public KPermutationLexOrderMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public KPermutationLexOrderMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationLexOrderMth<>(elements, r, m, start, calculator);
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m, long start) {
        return combinationOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationCombinationOrderMth<>(elements, r, m, start, calculator);
    }

}
