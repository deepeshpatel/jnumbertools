/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator;

import io.github.deepeshpatel.jnumbertools.generator.combination.CombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.PermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;

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

}
