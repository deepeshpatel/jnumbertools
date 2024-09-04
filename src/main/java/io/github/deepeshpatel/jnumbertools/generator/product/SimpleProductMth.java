package io.github.deepeshpatel.jnumbertools.generator.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SimpleProductMth implements Iterable<List<?>> {

    private final long m;
    private final  List<List<?>> elements;
    private final long start;

    public SimpleProductMth(long m, long start, List<List<?>> elements) {
        this.elements = elements;
        this.m = m;
        this.start = start;
        //TODO: implement starting index
    }

    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public Iterator<List<?>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<?>> {

        private final int[] indices = new int[elements.size()];
        private boolean hasNext = true;

        public Itr() {
            this.hasNext = nextMthCartesian(indices, start);
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<List<?>> next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            var result = indicesToList(elements, indices);
            hasNext = nextMthCartesian(indices, m);
            return result;
        }

        protected List<List<?>> indicesToList(List<List<?>> elements, int[] indices) {
            var list = new ArrayList(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).get(indices[i]));
            }
            return list;
        }

        private boolean nextMthCartesian(int[] indices, long k) {

            long nextK = k;

            for (int i = indices.length-1; i >= 0; i--) {
                int base = elements.get(i).size();
                long v = (indices[i] + nextK) % base;
                nextK = (indices[i] + nextK) / base;
                indices[i] = (int) v;
            }
            return nextK == 0;
        }
    }
}
