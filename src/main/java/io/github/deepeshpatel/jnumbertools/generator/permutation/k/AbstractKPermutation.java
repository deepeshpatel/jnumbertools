/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.List;

/**
 * Abstract base class for generating permutations of a subset of size k from a list of elements.
 * <p>
 * This class provides foundational functionality for k-permutation generators, ensuring the subset size
 * {@code k} is valid (between 0 and the input list size, inclusive). Subclasses implement specific
 * permutation generation strategies.
 * </p>
 *
 * @param <T> the type of elements in the permutations
 * @author Deepesh Patel
 * @version 3.0.1
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
     * @param k        the size of the subset to permute; must be between 0 and elements.size()
     * @throws IllegalArgumentException if k is negative or exceeds the input list size
     */
    protected AbstractKPermutation(List<T> elements, int k) {
        super(elements);
        this.k = k;
        checkBoundsOfK(elements.size(), k);
    }

    /**
     * Validates the subset size k against the input list size.
     *
     * @param inputSize the size of the input list
     * @param k         the size of the subset to permute
     * @throws IllegalArgumentException if k is negative or exceeds inputSize
     */
    protected void checkBoundsOfK(int inputSize, int k) {
        if (k > inputSize) {
            throw new IllegalArgumentException("k must be <= input length to generate permutation");
        }
        if (k < 0) {
            throw new IllegalArgumentException("k must be >= 0 to generate permutation");
        }
    }
}