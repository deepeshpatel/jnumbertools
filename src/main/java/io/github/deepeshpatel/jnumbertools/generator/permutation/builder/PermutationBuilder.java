/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.builder;

import io.github.deepeshpatel.jnumbertools.generator.permutation.*;

import java.math.BigInteger;
import java.util.Collection;

public class PermutationBuilder<T> {

    final Collection<T> seed;

    public PermutationBuilder(Collection<T> seed) {
        this.seed = seed;
    }

    public UniquePermutation<T> unique() {
        return new UniquePermutation<>(seed);
    }

    public UniquePermutationsNth<T> uniqueNth(long increment) {
        return uniqueNth(BigInteger.valueOf(increment));
    }

    public UniquePermutationsNth<T> uniqueNth(BigInteger increment) {
        return new UniquePermutationsNth<>(seed, increment);
    }

    public RepetitivePermutation<T> repetitive(int size) {
        return new RepetitivePermutation<>(seed,size);
    }

    public RepetitivePermutationNth<T> repetitiveNth(int size, long increment) {
        return new RepetitivePermutationNth<>(seed,size,increment);
    }

    public MultisetPermutation<T> multiset(int... multisetFreqArray){
        return new MultisetPermutation<>(seed,multisetFreqArray);
    }

    public MultisetPermutation<T> multiset(Collection<Integer> multisetFreqList){
        int[] multisetFreqArray = multisetFreqList.stream().mapToInt(i->i).toArray();
        return new MultisetPermutation<>(seed,multisetFreqArray);
    }

    public MultisetPermutationNth<T> multisetNth(long increment, int... multisetFreqArray){
        return new MultisetPermutationNth<>(seed, increment, multisetFreqArray);
    }

    public MultisetPermutationNth<T> multisetNth(long increment, Collection<Integer> multisetFreqList){
        int[] multisetFreqArray = multisetFreqList.stream().mapToInt(i->i).toArray();
        return new MultisetPermutationNth<>(seed, increment, multisetFreqArray);
    }


    public KPermutationBuilder<T> k(int k) {
        return new KPermutationBuilder<>(seed, k);
    }
}