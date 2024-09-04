/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractGenerator<E> implements Iterable<List<E>> {

    protected final List<E> elements;

    protected AbstractGenerator(List<E> elements) {
        this.elements = (elements != null) ? elements : Collections.emptyList();
    }

    protected List<E> indicesToValues(int[] indices) {

        var output = new ArrayList<E>(indices.length);

        for(int index: indices) {
            output.add(elements.get(index));
        }
        return output;
    }

    public Stream<List<E>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    //making it mathematically correct as set should contain null/empty set by definition.
    // for r=0, nPr = nCr = 1 and hence should contain one null(empty) value
    public static<E> Iterator<List<E>> newEmptyIterator(){
        var list = new ArrayList<List<E>>();
        list.add(Collections.emptyList());
        return list.iterator();
    }
}