/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractGenerator<T> implements Iterable<List<T>> {

    protected final List<T> seedElements;

    protected AbstractGenerator(List<T> seedElements) {
        this.seedElements = (seedElements != null) ? seedElements : Collections.emptyList();
    }

    protected List<T> indicesToValues(int[] indices, List<T> input) {

        var output = new ArrayList<T>(indices.length);

        for(int index: indices) {
            output.add(input.get(index));
        }
        return output;
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    //making it mathematically correct as set should contain null/empty set by definition.
    // for r=0, nPr = nCr = 1 and hence should contain one null(empty) value
    public static<T> Iterator<List<T>> newEmptyIterator(){
        var list = new ArrayList<List<T>>();
        list.add(Collections.emptyList());
        return list.iterator();
    }

}