/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator;

import io.github.deepeshpatel.jnumbertools.generator.combination.CombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.PermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.rank.RankOf;
import io.github.deepeshpatel.jnumbertools.generator.rank.UnRankOf;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JNumberTools {

    private JNumberTools() { }

    public static <T> CombinationBuilder<T> combinationsOf(Collection<T> data, int ofSize) {
        return new CombinationBuilder<>(data, ofSize);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> CombinationBuilder<T> combinationsOf(int size,T... data) {
        return combinationsOf(Arrays.asList(data),size);
    }

    public static CombinationBuilder<Integer> combinationsOfnCr(int n, int r) {
        List<Integer> list = IntStream.range(0, n).boxed().collect(Collectors.toList());
        return combinationsOf(list,r);
    }

    public static <T> PermutationBuilder<T>  permutationsOf(Collection<T> data) {
        return new PermutationBuilder<>(data);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> PermutationBuilder<T>  permutationsOf(T... data) {
        return permutationsOf(Arrays.asList(data));
    }

    public static  PermutationBuilder<Integer> permutationsOf(int size) {
        List<Integer> list = IntStream.range(0, size).boxed().collect(Collectors.toList());
        return permutationsOf(list);
    }

    public static <T> SubsetGenerator.Builder<T> subsetsOf(Collection<T> data) {
        return new SubsetGenerator.Builder<>(data);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> SubsetGenerator.Builder<T> subsetsOf(T... data) {
        return subsetsOf(Arrays.asList(data));
    }

    public static SubsetGenerator.Builder<Integer> subsetsOf(int dataSize) {
        List<Integer> list = IntStream.range(0, dataSize).boxed().collect(Collectors.toList());
        return new SubsetGenerator.Builder<>(list);
    }

    public static RankOf rankOf() {
        return new RankOf();
    }

    public static UnRankOf unRankingOf() {
        return new UnRankOf();
    }

}
