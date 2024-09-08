/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.List;

/**
 * A builder class for generating k-permutations of a given list of elements.
 *
 * @param <T> the type of elements in the list.
 */
public final class KPermutationBuilder<T> {

    private final List<T> elements;
    private final int r;
    private final Calculator calculator;

    /**
     * Constructs a builder for generating k-permutations.
     *
     * @param elements the list of elements from which permutations are generated.
     * @param r the length of the permutation.
     * @param calculator a utility for performing mathematical operations.
     */
    public KPermutationBuilder(List<T> elements, int r, Calculator calculator) {
        this.elements = elements;
        this.r = r;
        this.calculator = calculator;
    }

    /**
     * Generates all k-permutations in lexicographical order.
     *
     * @return an instance of {@link KPermutationLexOrder}.
     */
    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(elements, r);
    }

    /**
     * Generates all k-permutations in combination order.
     *
     * @return an instance of {@link KPermutationCombinationOrder}.
     */
    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(elements, r);
    }

    /**
     * Generates the m-th k-permutation in lexicographical order.
     *
     * @param m the index of the permutation to generate.
     * @param start the starting index for permutation generation.
     * @return an instance of {@link KPermutationLexOrderMth}.
     */
    public KPermutationLexOrderMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates the m-th k-permutation in lexicographical order.
     *
     * @param m the index of the permutation to generate.
     * @param start the starting index for permutation generation.
     * @return an instance of {@link KPermutationLexOrderMth}.
     */
    public KPermutationLexOrderMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationLexOrderMth<>(elements, r, m, start, calculator);
    }

    /**
     * Generates the m-th k-permutation in combination order.
     *
     * @param m the index of the permutation to generate.
     * @param start the starting index for permutation generation.
     * @return an instance of {@link KPermutationCombinationOrderMth}.
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m, long start) {
        return combinationOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates the m-th k-permutation in combination order.
     *
     * @param m the index of the permutation to generate.
     * @param start the starting index for permutation generation.
     * @return an instance of {@link KPermutationCombinationOrderMth}.
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationCombinationOrderMth<>(elements, r, m, start, calculator);
    }
}
