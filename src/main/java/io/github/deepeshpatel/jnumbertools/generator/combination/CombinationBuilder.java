/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import java.math.BigInteger;
import java.util.Collection;

public class CombinationBuilder<T> {

    private final Collection<T> data;

    public CombinationBuilder(Collection<T> data) {
        this.data = data;
    }

    public UniqueCombination<T> unique(int ofSize) {
        return new UniqueCombination<>(data, ofSize);
    }

    public UniqueCombinationNth<T> uniqueNth(int ofSize, long skipTo) {
        return uniqueNth(ofSize, BigInteger.valueOf(skipTo));
    }

    public UniqueCombinationNth<T> uniqueNth(int ofSize, BigInteger skipTo) {
        return new UniqueCombinationNth<>(data, ofSize, skipTo);
    }

    public RepetitiveCombination<T> repetitive(int ofSize) {
        return new RepetitiveCombination<>(data, ofSize);
    }

    public RepetitiveCombinationLimitedSupply<T> repetitiveWithSupply(int ofSize, int[] supply) {
        return new RepetitiveCombinationLimitedSupply<>(data, ofSize, supply);
    }
}
