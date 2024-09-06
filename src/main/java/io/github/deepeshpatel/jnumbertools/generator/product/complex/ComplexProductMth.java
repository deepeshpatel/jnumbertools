package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ComplexProductMth implements Iterable<List<?>> {

    private final long m;
    private final long start;
    private final List<Builder> builders;
    private long maxCount;

    public ComplexProductMth(long m, long start, List<Builder> builders) {
        this.m = m;
        this.start = start;
        this.builders = builders;
    }

    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }


    public List build() {
        return getMth(m);
    }

    private synchronized long maxCount() {
        if(maxCount != 0) {
            return maxCount;
        }
        maxCount = 1;
        for(var builder: builders) {
            maxCount *= builder.count();
        }
        return maxCount;
    }

    private List getMth(long m) {
        List output = new ArrayList<>();
        long remaining = m;
        for(int i = builders.size()-1; i>=0; i--) {
            Builder e = builders.get(i);
            long index = remaining % e.count();
            remaining = remaining / e.count();
            List values = e.lexOrderMth(index,0).build();
            Collections.reverse(values);
            output.addAll(values);
        }
        Collections.reverse(output);
        return output;
    }



    @Override
    public Iterator<List<?>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<?>> {

        private long position = start;
        private final long memoizedMaxCount;

        public Itr() {
            memoizedMaxCount = maxCount();
        }

        @Override
        public boolean hasNext() {
            return position < memoizedMaxCount;
        }

        @Override
        public List<?> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            var result = getMth(position);
            position += m;
            return result;
        }
    }
}
