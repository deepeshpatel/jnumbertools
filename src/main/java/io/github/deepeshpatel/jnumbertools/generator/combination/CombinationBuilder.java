/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
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

    public UniqueCombinationNth<T> uniqueNth(int ofSize, long increment) {
        return uniqueNth(ofSize, BigInteger.valueOf(increment));
    }

    public UniqueCombinationNth<T> uniqueNth(int ofSize, BigInteger increment) {
        return new UniqueCombinationNth<>(data, ofSize, increment);
    }

    public RepetitiveCombination<T> repetitive(int ofSize) {
        return new RepetitiveCombination<>(data, ofSize);
    }

    public RepetitiveCombinationMultiset<T> repetitiveMultiset(int ofSize, int... multisetFreqArray) {
        return new RepetitiveCombinationMultiset<>(data, ofSize, multisetFreqArray);
    }

    public RepetitiveCombinationMultiset<T> repetitiveMultiset(int ofSize, Collection<Integer> multisetFreqList) {
        int[] multisetFreqArray = multisetFreqList.stream().mapToInt(i->i).toArray();
        return new RepetitiveCombinationMultiset<>(data, ofSize, multisetFreqArray);
    }
}
