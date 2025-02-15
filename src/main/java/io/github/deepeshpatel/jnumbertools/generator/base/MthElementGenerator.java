/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.List;

/**
 * Interface for generating the mᵗʰ element in a sequence of permutations or combinations.
 *
 * @param <E> the type of elements to be generated.
 *            This type is used to specify the elements that the generator will produce.
 * @author Deepesh Patel
 */
public interface MthElementGenerator<E> {

    /**
     * Generates the mᵗʰ element in the sequence of permutations or combinations.
     * <p>
     * Use this method if you need only the mᵗʰ value and do not require the 0ᵗʰ, mᵗʰ, 2mᵗʰ, etc.
     * Creating an iterator can be expensive because the {@code hasNext()} implementation involves costly calculations.
     * </p>
     *
     * @return a {@link List} containing the mᵗʰ element in the sequence.
     */
    List<E> build();
}
