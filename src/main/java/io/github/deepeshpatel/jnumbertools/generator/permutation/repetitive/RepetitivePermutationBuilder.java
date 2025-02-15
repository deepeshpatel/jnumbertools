/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import java.math.BigInteger;
import java.util.List;

/**
 * Builder for creating instances of {@link RepetitivePermutation} and {@link RepetitivePermutationMth}.
 * <p>
 * This class provides methods to generate permutations with repetition allowed. It allows you to generate
 * all permutations of a given size in lexicographical order or to directly retrieve the mᵗʰ permutation
 * (and every subsequent permutation with a given increment) starting from a specific index.
 * </p>
 *
 * @param <T> the type of elements to permute
 *
 * @author Deepesh Patel
 * @version 3.0.1
 */
public final class RepetitivePermutationBuilder<T> {

    private final int width;
    private final List<T> elements;

    /**
     * Constructs a new RepetitivePermutationBuilder with the specified width and elements.
     *
     * @param width    the size (length) of each permutation. This value can be greater than the number of elements due to repetition.
     * @param elements the list of elements to permute
     */
    public RepetitivePermutationBuilder(int width, List<T> elements) {
        this.width = width;
        this.elements = elements;
    }

    /**
     * Creates a {@link RepetitivePermutation} instance that generates permutations in lexicographical order.
     *
     * @return a {@link RepetitivePermutation} instance for generating all permutations of the given width
     */
    public RepetitivePermutation<T> lexOrder() {
        return new RepetitivePermutation<>(elements, width);
    }

    /**
     * Creates a {@link RepetitivePermutationMth} instance that generates the mᵗʰ permutation and every subsequent
     * permutation with a given increment, starting from a specified index.
     *
     * @param m     the index of the first permutation to generate (0‑based)
     * @param start the starting index for generating permutations
     * @return a {@link RepetitivePermutationMth} instance for generating permutations with repetition in lexicographical order
     */
    public RepetitivePermutationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    /**
     * Creates a {@link RepetitivePermutationMth} instance that generates the mᵗʰ permutation and every subsequent
     * permutation with a given increment, starting from a specified index.
     *
     * @param m     the index of the first permutation to generate (0‑based) as a {@link BigInteger}
     * @param start the starting index for generating permutations as a {@link BigInteger}
     * @return a {@link RepetitivePermutationMth} instance for generating permutations with repetition in lexicographical order
     */
    public RepetitivePermutationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new RepetitivePermutationMth<>(elements, width, m, start);
    }
}
