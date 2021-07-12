/*
 * JNumberTools Library v1.0.0
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator;

import java.util.*;

public class IteratorSequence<T> implements Iterator<T> {

    private final List<Iterator<T>> iterators = new LinkedList<>();
    private Iterator<T> current;

    public IteratorSequence(List<Iterator<T>> iterators) {
        this.iterators.addAll(iterators);
        current = (iterators.isEmpty()) ? this.iterators.remove(0) : Collections.emptyIterator();
    }

    @Override
    public boolean hasNext() {

        if (current.hasNext()) {
            return true;
        }

        if (iterators.isEmpty()) {
            return false;
        }

        current = iterators.remove(0);
        return hasNext();
    }

    @Override
    public T next() {
        return current.next();
    }
}