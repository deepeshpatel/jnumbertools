/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for creating instances of {@link RepetitivePermutation} and {@link RepetitivePermutationMth}.
 * <p>
 * This class provides methods to generate permutations with repetition allowed. It allows
 * you to generate all permutations of a given size or a specific m<sup>th</sup> permutation directly.
 * </p>
 * @author Deepesh Patel
 * @param <T> the type of elements to permute
 */
public final class RepetitivePermutationBuilder<T> {

    private final int width;
    private final List<T> elements;

    /**
     * Constructs a RepetitivePermutationBuilder with the specified width and elements.
     *
     * @param width the size of each permutation. This can be greater than the number of elements due to repetition.
     * @param elements the list of elements to permute
     */
    public RepetitivePermutationBuilder(int width, List<T> elements) {
        this.width = width;
        this.elements = elements;
    }

    /**
     * Creates a {@link RepetitivePermutation} that generates permutations in lexicographical order.
     *
     * @return a {@link RepetitivePermutation} instance
     */
    public RepetitivePermutation<T> lexOrder() {
        return new RepetitivePermutation<>(elements, width);
    }

    /**
     * Creates a {@link RepetitivePermutationMth} that generates the m<sup>th</sup> permutation and every subsequent
     * permutation with a given increment, starting from a specific index.
     *
     * @param m the index of the first permutation to generate
     * @param start the index to start generating permutations from
     * @return a {@link RepetitivePermutationMth} instance
     */
    public RepetitivePermutationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public RepetitivePermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new RepetitivePermutationMth<>(elements, width, m, start);
    }

}
