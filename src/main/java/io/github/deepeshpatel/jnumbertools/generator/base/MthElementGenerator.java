package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.List;

/**
 * Interface for generating the mth element in a sequence of permutations or combinations.
 *
 * @param <E> The type of elements to be generated.
 */
public interface MthElementGenerator <E> {

    /**
     * Generates the mth element in the sequence of permutations or combinations.
     * <p>
     * Use this method if you need only the mth value and do not require the 0th, mth, 2mth, etc.
     * Creating an iterator can be expensive because the `hasNext()` implementation involves costly calculations.
     *
     * @return A {@link List} containing the mth element in the sequence.
     */
    List<E> build();
}
