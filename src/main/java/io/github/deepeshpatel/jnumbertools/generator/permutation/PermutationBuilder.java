/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation;

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

    public UniquePermutationsNth<T> uniqueNth(long skipTo) {
        return uniqueNth(BigInteger.valueOf(skipTo));
    }

    public UniquePermutationsNth<T> uniqueNth(BigInteger skipTo) {
        return new UniquePermutationsNth<>(seed, skipTo);
    }

    public UniquePermutationInSizeRange<T> uniqueInSizeRange(int fromInclusive, int toInclusive) {
        return new UniquePermutationInSizeRange<>(seed, fromInclusive, toInclusive);
    }

    public RepetitivePermutation<T> repetitive(int size) {
        return new RepetitivePermutation<>(seed,size);
    }

    public RepetitivePermutationNth<T> repetitiveNth(int size, long skipTo) {
        return new RepetitivePermutationNth<>(seed,size,skipTo);
    }

    public RepetitivePermutationLimitedSupply<T> repetitiveWithSupply(int... supply){
        return new RepetitivePermutationLimitedSupply<>(seed,supply);
    }

    public KPermutation<T> k(int k) {
        return new KPermutation<>(seed, k);
    }

    public KPermutationNth<T> kNth(int k, long skipTo){
        return kNth(k, BigInteger.valueOf(skipTo));
    }

    public KPermutationNth<T> kNth(int k, BigInteger skipTo){
        return new KPermutationNth<>(seed,k, skipTo);
    }
}