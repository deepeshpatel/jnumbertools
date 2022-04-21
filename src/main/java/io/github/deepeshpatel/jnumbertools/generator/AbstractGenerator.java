/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractGenerator<T> implements Iterable<List<T>> {

    protected final List<T> seed;
    private final List<List<T>> empty = new ArrayList<>();

    protected AbstractGenerator(Collection<T> seed) {
        this.seed = new ArrayList<>(seed);
        empty.add(Collections.emptyList());
    }

    public static <T> List<T> indicesToValues(int[] indices, List<T> input) {

        List<T> output = new ArrayList<>(indices.length);

        for(int indexValue: indices) {
            output.add(input.get(indexValue));
        }
        return output;
    }

    public static <T> List<T> indicesToValuesReverse(int[] indices, List<T> input) {
        List<T> output = new ArrayList<>(indices.length);

        for(int i=indices.length-1; i>=0; i--) {
            output.add(input.get(indices[i]));
        }
        return output;
    }

    //making it mathematically correct as set should contain null/empty set by definition.
    // for r=0, nPr = nCr = 1 and hence should contain one null(empty) value
    public  Iterator<List<T>> newEmptyIterator(){
        return empty.iterator();
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }


}