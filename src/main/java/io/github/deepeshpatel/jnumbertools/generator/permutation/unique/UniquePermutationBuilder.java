/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for generating unique permutations of a list of elements.
 * <p>
 * This builder provides methods to generate unique permutations in lexicographic order,
 * as well as to generate a specific mᵗʰ permutation and to use different permutation strategies.
 * The builder is intended for use with a separate builder class; therefore, instances of
 * {@code UniquePermutation} and related generators should be created via this builder.
 * </p>
 *
 * @param <T> the type of elements to permute
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class UniquePermutationBuilder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a {@code UniquePermutationBuilder} with the specified calculator and list of elements.
     *
     * @param calculator the calculator to use for permutation calculations
     * @param elements the list of elements to generate permutations from
     */
    public UniquePermutationBuilder(Calculator calculator, List<T> elements) {
        this.elements = (elements != null) ? new ArrayList<>(elements) : Collections.emptyList();
        this.calculator = calculator;
    }

    /**
     * Generates unique permutations of the elements in lexicographic order.
     *
     * @return a {@link UniquePermutation} generator for the elements
     */
    public UniquePermutation<T> lexOrder() {
        return new UniquePermutation<>(elements);
    }

    /**
     * Generates the mᵗʰ unique permutation in lexicographic order, starting from a given index.
     *
     * @param m the index of the desired permutation (0‑based)
     * @param start the starting index for generating permutations
     * @return a {@link UniquePermutationsMth} generator for the mᵗʰ permutation
     */
    public UniquePermutationsMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new UniquePermutationsMth<>(elements, m, start, calculator);
    }

    /**
     * Generates the mᵗʰ unique permutation in lexicographic order, starting from a given index.
     *
     * @param m the index of the desired permutation (0‑based)
     * @param start the starting index for generating permutations
     * @return a {@link UniquePermutationsMth} generator for the mᵗʰ permutation
     */
    public UniquePermutationsMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates a permutation from the previous permutation by interchanging a single pair of elements,
     * leaving the other n − 2 elements unchanged.
     *
     * @return a {@link UniquePermutationSingleSwap} generator that uses the single swap method
     */
    public UniquePermutationSingleSwap<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

    /**
     * Generates permutations rapidly using a fast method that does not guarantee order.
     *
     * @return an {@link AbstractGenerator} for permutations using the fast method
     * @throws RuntimeException as the method is not yet implemented
     */
    public AbstractGenerator<T> fast() {
        // TODO: Implement this
        throw new RuntimeException("method not yet implemented..");
    }
}
