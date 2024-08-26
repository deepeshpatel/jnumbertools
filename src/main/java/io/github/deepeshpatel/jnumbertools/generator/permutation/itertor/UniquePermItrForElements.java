/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.itertor;

import java.util.Iterator;
import java.util.List;

/**
 * Used by permutation generators in this package
 * @author Deepesh Patel
 */
public final class UniquePermItrForElements<T> implements Iterator<List<T>> {

    private final List<T> seed;
    private final IndicesToValueMapper<T> mapper;
    private final UniquePermItrForIndices iterator;

    public UniquePermItrForElements(List<T> seed, IndicesToValueMapper<T> mapper) {
        this.seed = seed;
        this.mapper = mapper;
        iterator = new UniquePermItrForIndices(seed.size());
    }

    public UniquePermItrForElements(List<T> seed, IndicesToValueMapper<T> mapper, int[] initialStateOfIndices) {
        this.seed = seed;
        this.mapper = mapper;
        iterator = new UniquePermItrForIndices(initialStateOfIndices);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public List<T> next() {
        return mapper.indicesToValues(iterator.next(), seed);
    }

    public interface IndicesToValueMapper<T> {
        List<T> indicesToValues(int[] indices, List<T> input);
    }
}