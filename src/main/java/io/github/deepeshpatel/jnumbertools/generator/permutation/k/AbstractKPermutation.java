/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.List;

/**
 * Abstract base class for generating permutations of a subset of size kₖ from a list of elements.
 * <p>
 * This class provides foundational functionality for k-permutation generators, which produce permutations
 * of kₖ elements chosen from nₙ elements, where order matters. The total number of k-permutations is
 * given by Pₙ,ₖ = n!/(n−kₖ)!. Subclasses implement specific permutation generation strategies, ensuring
 * the subset size kₖ is valid (0 ≤ kₖ ≤ nₙ).
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 */
public abstract class AbstractKPermutation<T> extends AbstractGenerator<T> {

    /**
     * The size of the subset to be permuted.
     */
    protected final int k;

    /**
     * Constructs an instance with the specified elements and subset size.
     *
     * @param elements the list of elements from which permutations are generated
     * @param k the size of the subset to permute (kₖ); must be between 0 and elements.size()
     * @return a new AbstractKPermutation instance
     * @throws IllegalArgumentException if kₖ is negative or exceeds the input list size
     */
    protected AbstractKPermutation(List<T> elements, int k) {
        super(elements);
        this.k = k;
        checkBoundsOfK(elements.size(), k);
    }

    /**
     * Validates the subset size kₖ against the input list size.
     *
     * @param inputSize the size of the input list (nₙ)
     * @param k the size of the subset to permute (kₖ)
     * @throws IllegalArgumentException if kₖ is negative or exceeds nₙ
     */
    protected void checkBoundsOfK(int inputSize, int k) {
        if (k > inputSize) {
            throw new IllegalArgumentException("kₖ must be ≤ nₙ to generate permutation");
        }
        if (k < 0) {
            throw new IllegalArgumentException("kₖ must be ≥ 0 to generate permutation");
        }
    }
}