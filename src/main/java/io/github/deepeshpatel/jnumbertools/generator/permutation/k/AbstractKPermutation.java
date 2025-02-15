/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import java.util.List;

/**
 * AbstractKPermutation is an abstract base class for generating permutations
 * of a subset of elements (denoted by {@code k}) from a given list.
 * <p>
 * This class serves as the foundation for specific implementations of k‑permutations.
 * It ensures that the subset size {@code k} is within valid bounds; that is,
 * {@code k} must be between 0 and the size of the input list (inclusive).
 * </p>
 *
 * @param <T> the type of elements in the permutation
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public abstract class AbstractKPermutation<T> extends AbstractGenerator<T> {

    /**
     * The size of the subset to be permuted.
     */
    protected final int k;

    /**
     * Constructs an AbstractKPermutation with the specified list of elements
     * and the subset size {@code k} to permute.
     *
     * @param elements the list of elements from which permutations will be generated
     * @param k the size of the subset to permute; must be between 0 and the number of elements in the list (inclusive)
     * @throws IllegalArgumentException if {@code k} is greater than the number of elements in the list or less than 0
     */
    protected AbstractKPermutation(List<T> elements, int k) {
        super(elements);
        this.k = k;
        checkBoundsOfK(elements.size(), k);
    }

    /**
     * Checks whether the subset size {@code k} is within valid bounds.
     * <p>
     * The value of {@code k} must be greater than or equal to 0 and less than or equal to the size
     * of the input list. If {@code k} is outside these bounds, an {@code IllegalArgumentException}
     * is thrown.
     * </p>
     *
     * @param inputSize the size of the input list
     * @param k the size of the subset to permute
     * @throws IllegalArgumentException if {@code k} is greater than {@code inputSize} or less than 0
     */
    protected void checkBoundsOfK(int inputSize, int k) {
        if (k > inputSize) {
            throw new IllegalArgumentException("k should be <= input length to generate permutation");
        }
        if (k < 0) {
            throw new IllegalArgumentException("k should be >= 0 to generate permutation");
        }
    }
}
