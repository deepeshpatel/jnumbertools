/*
 * JNumberTools Library
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation;


import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class UniquePermutationInSizeRange<T> extends AbstractGenerator<T> {


    private final int fromInclusive;
    private final int toInclusive;

    public UniquePermutationInSizeRange(Collection<T> seed, int fromInclusive, int toInclusive) {
        super(seed);
        this.fromInclusive = fromInclusive;
        this.toInclusive = toInclusive;
    }

    @Override
    public Iterator<List<T>> iterator() {
       return new OnDemandIterator();
    }

    private class OnDemandIterator implements Iterator<List<T>> {

        final Iterator<List<T>> subsetIterator;
        Iterator<List<T>> currentIterator;
        public OnDemandIterator() {
            subsetIterator = new SubsetGenerator<>(seed, fromInclusive, toInclusive).iterator();
            getNextIterator();
        }

        @Override
        public boolean hasNext() {
            if(currentIterator.hasNext()) {
                return true;
            }

            if(!subsetIterator.hasNext()) {
                return false;
            }

            return getNextIterator().hasNext();

        }

        private Iterator<List<T>> getNextIterator(){
            currentIterator = new UniquePermutation<>(subsetIterator.next()).iterator();
            return currentIterator;
        }

        @Override
        public List<T> next() {
            return currentIterator.next();
        }
    }
}