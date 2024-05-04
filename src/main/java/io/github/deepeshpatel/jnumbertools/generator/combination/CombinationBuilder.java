/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

public class CombinationBuilder<T> {

    private final Collection<T> data;
    private final int ofSize;
    private final Calculator calculator;

    public CombinationBuilder(Collection<T> data, int ofSize, Calculator calculator) {
        this.data = data;
        this.ofSize = ofSize;
        this.calculator = calculator;
    }

    public UniqueCombination<T> unique() {
        return new UniqueCombination<>(data, ofSize);
    }

    public UniqueCombinationNth<T> uniqueNth(long increment) {
        return uniqueNth(BigInteger.valueOf(increment));
    }

    public UniqueCombinationNth<T> uniqueNth(BigInteger increment) {
        return new UniqueCombinationNth<>(data, ofSize, increment, calculator);
    }

    public RepetitiveCombination<T> repetitive() {
        return new RepetitiveCombination<>(data, ofSize);
    }

    public RepetitiveCombinationMultiset<T> repetitiveMultiset(int... multisetFreqArray) {
        return new RepetitiveCombinationMultiset<>(data, ofSize, multisetFreqArray);
    }

    public RepetitiveCombinationMultiset<T> repetitiveMultiset(Integer... multisetFreqArray) {
        int[] frequencies = Arrays.stream(multisetFreqArray).mapToInt(Integer::intValue).toArray();
        return new RepetitiveCombinationMultiset<>(data, ofSize, frequencies);
    }

    public RepetitiveCombinationMultiset<T> repetitiveMultiset(Collection<Integer> multisetFreqList) {
        int[] multisetFreqArray = multisetFreqList.stream().mapToInt(i->i).toArray();
        return new RepetitiveCombinationMultiset<>(data, ofSize, multisetFreqArray);
    }
}
