/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2021 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.generator.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombination;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the Iterable to generate all subsets of set in a given size range.
 * Implementation does not check for duplicates in input collection and treats every item
 * in a collection as unique
 *
 * <pre>
 *     Code example:
 *        new SubsetGenerator&lt;&gt;(Arrays.asList("A", "B", "C"),1,3)
 *            .forEach(System.out::println);
 *
 *   or
 *
 *        JNumberTools.subsetsOf("A", "B", "C")
 *             .inRange(1, 3)
 *             .forEach(System.out::println);
 *
 * will generate following output
 * [A]
 * [B]
 * [C]
 * [A, B]
 * [A, C]
 * [B, C]
 * [A, B, C]
 * </pre>
 *
 * @author Deepesh Patel
 */
public class SubsetGenerator<T> extends AbstractGenerator<T> {

    private final int fromSize;
    private final int toSize;

    /**
     *
     * When fromSize and toSize both are -ve, will not generate any output but when
     * fromSize is &lt;=0 and toSize &gt;= will generate one empty set. This sounds
     * unintuitive but is mathematically correct.
     *
     * @param data input set from which subsets are generated.
     *             Implementation does not check for duplicates in input collection
     *             and treats every item in a collection as unique.
     *
     * @param fromSize minimum-size(inclusive) of subset.
     * @param toSize max-size(inclusive) of subset. toSize must be &gt;= fromSize
     */
    public SubsetGenerator(Collection<T> data, int fromSize, int toSize){
        super(data);
        this.fromSize = fromSize;
        this.toSize = toSize;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new OnDemandIterator(fromSize);
    }

    class OnDemandIterator implements Iterator<List<T>> {
        int start;
        Iterator<List<T>> current;

        public OnDemandIterator(int start) {
            this.start = start;
            current = new UniqueCombination<>(seed,this.start).iterator();
        }

        @Override
        public boolean hasNext() {
            if(current.hasNext()) {
                return true;
            }
            if(start < toSize) {
                current = new UniqueCombination<>(seed,++start).iterator();
                return hasNext();
            }
            return false;
        }

        @Override
        public List<T> next() {
            return current.next();
        }
    }

    public static class Builder<T> {

        private final Collection<T> data;

        public Builder(Collection<T> data) {
            this.data = data;
        }

        public SubsetGenerator<T> all(){
            return new SubsetGenerator<>(data,0, data.size());
        }

        public SubsetGenerator<T> inRange(int from, int to) {
            return new SubsetGenerator<>(data,from, to);
        }
    }
}
