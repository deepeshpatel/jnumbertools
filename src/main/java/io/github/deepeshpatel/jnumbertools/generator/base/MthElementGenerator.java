package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.List;

public interface MthElementGenerator <E> {

    /**
     * Use this method instead of iterator if you need only mth value and not 0th, mth, 2mth
     * creating iterator is expensive because hasNext() implementation requires expensive calculations.
     */
    List<E> build();
}
