/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractGenerator<T> implements Iterable<List<T>> {

    protected final List<T> seed;

    protected AbstractGenerator(Collection<T> seed) {
        this.seed = seed == null ? new ArrayList<>() : new ArrayList<>(seed);
    }

    protected List<T> indicesToValues(int[] indices, List<T> input) {

        List<T> output = new ArrayList<>(indices.length);

        for(int indexValue: indices) {
            output.add(input.get(indexValue));
        }
        return output;
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

}