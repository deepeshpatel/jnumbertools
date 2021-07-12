/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator;

import io.github.deepeshpatel.jnumbertools.generator.combinadic.CombinatorialNumberSystemGenerator;
import io.github.deepeshpatel.jnumbertools.generator.combination.CombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.factoradic.FactorialNumberSystemGenerator;
import io.github.deepeshpatel.jnumbertools.generator.permutation.PermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

public class JNumberTools {

    private JNumberTools() { }

    public static <T> CombinationBuilder<T> combinationsOf(Collection<T> data) {
        return new CombinationBuilder<>(data);
    }

    @SafeVarargs
    public static <T> CombinationBuilder<T> combinationsOf(T... data) {
        return combinationsOf(Arrays.asList(data));
    }

    public static <T> PermutationBuilder<T>  permutationsOf(Collection<T> data) {
        return new PermutationBuilder<>(data);
    }

    @SafeVarargs
    public static <T> PermutationBuilder<T>  permutationsOf(T... data) {
        return permutationsOf(Arrays.asList(data));
    }

    public static <T> SubsetGenerator.Builder<T> subsetsOf(Collection<T> data) {
        return new SubsetGenerator.Builder<>(data);
    }

    @SafeVarargs
    public static <T> SubsetGenerator.Builder<T> subsetsOf(T... data) {
        return subsetsOf(Arrays.asList(data));
    }

    public static FactorialNumberSystemGenerator factoradic(long fromInclusive, long toExclusive) {
        return new FactorialNumberSystemGenerator(fromInclusive, toExclusive);
    }

    public static FactorialNumberSystemGenerator factoradic(BigInteger fromInclusive, BigInteger toExclusive) {
        return new FactorialNumberSystemGenerator(fromInclusive, toExclusive);
    }

    public static CombinatorialNumberSystemGenerator combinadic(int degree, long startInclusive, long endExclusive) {
        return new CombinatorialNumberSystemGenerator(degree, startInclusive, endExclusive);
    }

}
