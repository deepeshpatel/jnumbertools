/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
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
 * as well as to generate specific mth permutations and use different permutation strategies.
 *
 * @param <T> the type of elements to permute
 */
public final class UniquePermutationBuilder<T> {

    private final List<T> elements;
    private final Calculator calculator;

    /**
     * Constructs a UniquePermutationBuilder with the specified calculator and list of elements.
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
     * @return a UniquePermutation generator for the elements
     */
    public UniquePermutation<T> lexOrder() {
        return new UniquePermutation<>(elements);
    }

    /**
     * Generates the mth unique permutation in lexicographic order, starting from a given index.
     *
     * @param m the index of the desired permutation
     * @param start the starting index for generating permutations
     * @return a UniquePermutationsMth generator for the mth permutation
     */
    public UniquePermutationsMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new UniquePermutationsMth<>(elements, m, start, calculator);
    }

    /**
     * Generates the mth unique permutation in lexicographic order, starting from a given index.
     *
     * @param m the index of the desired permutation
     * @param start the starting index for generating permutations
     * @return a UniquePermutationsMth generator for the mth permutation
     */
    public UniquePermutationsMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Generates unique permutations using a single swap method.
     *
     * @return an AbstractGenerator for permutations using the single swap method
     */
    public AbstractGenerator<T> singleSwap() {
        return new UniquePermutationSingleSwap<>(elements);
    }

    /**
     * Generates unique permutations using a fast method.
     * <p>
     * This method is not yet implemented.
     *
     * @return an AbstractGenerator for permutations using a fast method
     * @throws RuntimeException if the method is not yet implemented
     */
    public AbstractGenerator<T> fast() {
        //TODO: Implement this
        throw new RuntimeException("method not yet implemented..");
    }
}
