package io.github.deepeshpatel.jnumbertools.generator.permutation.builder;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationCombinationOrder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationCombinationOrderMth;
import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationLexOrder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationLexOrderMth;

import java.math.BigInteger;
import java.util.List;

public class KPermutationBuilder<T> {

    private final List<T> allElements;
    private final int r;
    private final Calculator calculator;

    public KPermutationBuilder(List<T> allElements, int r, Calculator calculator) {
        this.allElements = allElements;
        this.r = r;
        this.calculator = calculator;
    }

    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(allElements, r);
    }

    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(allElements, r);
    }

    public KPermutationLexOrderMth<T> lexOrderMth(long m) {
        return lexOrderMth(BigInteger.valueOf(m));
    }

    public KPermutationLexOrderMth<T> lexOrderMth(BigInteger m) {
        return new KPermutationLexOrderMth<>(allElements, r, m, calculator);
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m) {
        return combinationOrderMth(BigInteger.valueOf(m));
    }

    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m) {
        return new KPermutationCombinationOrderMth<>(allElements, r, m, calculator);
    }

}
