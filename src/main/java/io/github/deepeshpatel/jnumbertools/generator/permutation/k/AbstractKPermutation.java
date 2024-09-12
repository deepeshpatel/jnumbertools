package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;

import java.util.List;

/**
 * AbstractKPermutation is an abstract class for generating permutations
 * of a subset of elements (denoted by <code>k</code>) from a given list.
 * It serves as the base class for specific implementations of k-permutations.
 *
 * <p>This class ensures that the subset size <code>k</code> is within valid bounds,
 * where <code>k</code> must be greater than or equal to 0 and less than or equal to the
 * size of the input list.
 * @author Deepesh Patel
 * @param <T> the type of elements in the permutation
 */
public abstract class AbstractKPermutation<T> extends AbstractGenerator<T> {

    /**
     * The size of the subset to be permuted.
     */
    protected final int k;

    /**
     * Constructs an AbstractKPermutation with the specified list of elements
     * and the size of the subset to permute.
     *
     * @param elements the list of elements to generate permutations from
     * @param k the size of the subset to permute
     * @throws IllegalArgumentException if <code>k</code> is greater than the number
     *         of elements in the list or less than 0
     */
    protected AbstractKPermutation(List<T> elements, int k) {
        super(elements);
        this.k = k;
        checkBoundsOfK(elements.size(), k);
    }

    /**
     * Checks whether the subset size <code>k</code> is within valid bounds.
     * <p>
     * The value of <code>k</code> must be less than or equal to the size of the input list
     * and greater than or equal to 0. If <code>k</code> is outside these bounds, an
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param inputSize the size of the input list
     * @param k the size of the subset to permute
     * @throws IllegalArgumentException if <code>k</code> is greater than <code>inputSize</code>
     *         or less than 0
     */
    protected void checkBoundsOfK(int inputSize, int k) {
        if(k > inputSize) {
            throw new IllegalArgumentException("k should be <= input length to generate permutation");
        }
        if(k < 0) {
            throw new IllegalArgumentException("k should be >= 0 to generate permutation");
        }
    }
}
