/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.List;

/**
 * A builder class for generating k‑permutations of a given list of elements.
 * <p>
 * This builder provides methods to generate k‑permutations in both lexicographical and combination order,
 * as well as methods to retrieve a specific mᵗʰ permutation in either order.
 * </p>
 *
 * @param <T> the type of elements in the list
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class KPermutationBuilder<T> {

    private final List<T> elements;
    private final int r;
    private final Calculator calculator;

    /**
     * Constructs a builder for generating k‑permutations.
     *
     * @param elements   the list of elements from which permutations are generated (e.g., elements₀, elements₁, …, elementsₙ₋₁)
     * @param r          the length of the permutation (i.e. the subset size to permute)
     * @param calculator a utility for performing mathematical operations related to combinatorics
     */
    public KPermutationBuilder(List<T> elements, int r, Calculator calculator) {
        this.elements = elements;
        this.r = r;
        this.calculator = calculator;
    }

    /**
     * Generates all k‑permutations in lexicographical order.
     *
     * @return an instance of {@link KPermutationLexOrder} for iterating over k‑permutations in lex order
     */
    public KPermutationLexOrder<T> lexOrder() {
        return new KPermutationLexOrder<>(elements, r);
    }

    /**
     * Generates all k‑permutations in combination order.
     *
     * @return an instance of {@link KPermutationCombinationOrder} for iterating over k‑permutations in combination order
     */
    public KPermutationCombinationOrder<T> combinationOrder() {
        return new KPermutationCombinationOrder<>(elements, r);
    }

    /**
     * Generates the mᵗʰ k‑permutation in lexicographical order.
     *
     * @param m     the index of the permutation to generate (0‑based)
     * @param start the starting index for permutation generation
     * @return an instance of {@link KPermutationLexOrderMth} representing the mᵗʰ k‑permutation in lex order
     */
    public KPermutationLexOrderMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates the mᵗʰ k‑permutation in lexicographical order.
     *
     * @param m     the index of the permutation to generate (0‑based)
     * @param start the starting index for permutation generation
     * @return an instance of {@link KPermutationLexOrderMth} representing the mᵗʰ k‑permutation in lex order
     */
    public KPermutationLexOrderMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationLexOrderMth<>(elements, r, m, start, calculator);
    }

    /**
     * Generates the mᵗʰ k‑permutation in combination order.
     *
     * @param m     the index of the permutation to generate (0‑based)
     * @param start the starting index for permutation generation
     * @return an instance of {@link KPermutationCombinationOrderMth} representing the mᵗʰ k‑permutation in combination order
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(long m, long start) {
        return combinationOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates the mᵗʰ k‑permutation in combination order.
     *
     * @param m     the index of the permutation to generate (0‑based)
     * @param start the starting index for permutation generation
     * @return an instance of {@link KPermutationCombinationOrderMth} representing the mᵗʰ k‑permutation in combination order
     */
    public KPermutationCombinationOrderMth<T> combinationOrderMth(BigInteger m, BigInteger start) {
        return new KPermutationCombinationOrderMth<>(elements, r, m, start, calculator);
    }
}
