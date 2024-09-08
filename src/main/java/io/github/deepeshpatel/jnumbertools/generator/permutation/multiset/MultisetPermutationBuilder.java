package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.util.List;

/**
 * Builder class for generating permutations of a multiset.
 * <p>
 * This class allows you to generate permutations of a multiset where elements
 * may repeat according to specified frequencies. It provides methods to generate
 * permutations in lexicographical order or retrieve the mth permutation directly
 * without generating the preceding permutations.
 *
 * @param <T> the type of elements in the multiset
 * @author Deepesh Patel
 */
public final class MultisetPermutationBuilder<T> {

    private final List<T> elements;
    private final int[] frequencies;
    private final Calculator calculator;

    /**
     * Constructs a new builder for generating multiset permutations.
     *
     * @param elements    the elements in the multiset
     * @param frequencies an array representing the frequency of each element in the multiset
     * @param calculator  an instance of {@link Calculator} used for internal calculations
     * @throws IllegalArgumentException if frequencies is null, its length is less than the size of elements,
     *                                  or if any frequency is not a positive integer
     */
    public MultisetPermutationBuilder(List<T> elements, int[] frequencies, Calculator calculator) {
        this.elements = elements;
        this.frequencies = frequencies;
        this.calculator = calculator;
        assertArguments();
    }

    /**
     * Ensures that the arguments provided to the constructor are valid.
     *
     * @throws IllegalArgumentException if frequencies is null, its length is less than the size of elements,
     *                                  or if any frequency is not a positive integer
     */
    private void assertArguments() {
        if(frequencies == null || frequencies.length < elements.size() || !frequenciesArePositive()) {
            throw new IllegalArgumentException("frequencies should be (1) Not null and (2) its length should be equal to" +
                    " number of elements and (3) should contain +ve values");
        }
    }

    /**
     * Checks whether all frequencies are positive integers.
     *
     * @return {@code true} if all frequencies are positive, {@code false} otherwise
     */
    private boolean frequenciesArePositive() {
        for(int e: frequencies) {
            if(e < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an instance of {@link MultisetPermutation} to generate permutations in lexicographical order.
     *
     * @return an instance of {@link MultisetPermutation}
     */
    public MultisetPermutation<T> lexOrder() {
        return new MultisetPermutation<>(elements, frequencies);
    }

    /**
     * Returns an instance of {@link MultisetPermutationMth} to generate the mth permutation
     * in lexicographical order, starting from a specified index.
     *
     * @param m     the mth permutation to generate
     * @param start the starting index for generating permutations
     * @return an instance of {@link MultisetPermutationMth}
     */
    public MultisetPermutationMth<T> lexOrderMth(long m, long start) {
        return new MultisetPermutationMth<>(elements, m, start, frequencies, calculator);
    }
}
