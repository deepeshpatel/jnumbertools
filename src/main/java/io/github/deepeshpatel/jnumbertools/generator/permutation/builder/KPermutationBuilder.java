/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.builder;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.KPermutationCombinationOrder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.KPermutationCombinationOrderNth;
import io.github.deepeshpatel.jnumbertools.generator.permutation.KPermutationLexOrder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.KPermutationLexOrderNth;

import java.math.BigInteger;
import java.util.Collection;

public class KPermutationBuilder<T> {
    final Collection<T> seed;
    final int k;

    private final Calculator calculator;

    public KPermutationBuilder(Collection<T> seed, int k, Calculator calculator) {
        this.seed = seed;
        this.k = k;
        this.calculator = calculator;
    }

    public KPermutationCombinationOrder<T> combinationOrder(){
        return new KPermutationCombinationOrder<>(seed, k);
    }

    public KPermutationCombinationOrderNth<T> combinationOrderNth(long increment){
        return combinationOrderNth(BigInteger.valueOf(increment));
    }

    public KPermutationCombinationOrderNth<T> combinationOrderNth(BigInteger increment){
        return new KPermutationCombinationOrderNth<>(seed, k, increment, calculator);
    }

    public KPermutationLexOrder<T> lexOrder(){
        return new KPermutationLexOrder<>(seed, k);
    }

    public KPermutationLexOrderNth<T> lexOrderNth(long increment){
        return lexOrderNth(BigInteger.valueOf(increment));
    }

    public KPermutationLexOrderNth<T> lexOrderNth(BigInteger increment){
        return new KPermutationLexOrderNth<>(seed, k, increment, calculator);
    }

}
