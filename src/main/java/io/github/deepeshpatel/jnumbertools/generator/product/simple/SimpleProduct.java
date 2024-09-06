package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SimpleProduct implements Iterable<List<?>> {

    private final List<List<?>> elements;

    public SimpleProduct(List<List<?>> elements) {
        this.elements = elements;
    }

    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public static boolean createNext(int[] current, List<? extends List> elements) {
        for (int i = 0, j = current.length - 1; j >= 0; j--, i++) {
            if (current[j] == elements.get(j).size() - 1) {
                current[j] = 0;
            } else {
                current[j]++;
                return true;
            }
        }
        return false;
    }


    @Override
    public Iterator<List<?>> iterator() {
        return new Itr();
    }


    private class Itr implements Iterator<List<?>> {

        final int[] current = new int[elements.size()];
        boolean hasNext = true;

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public List<?> next() {
            var result = indicesToList(elements, current);
            hasNext = createNext(current, elements);
            return result;
        }

        protected List<List<?>> indicesToList(List<List<?>> elements, int[] indices) {
            var list = new ArrayList(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                list.add(elements.get(i).get(indices[i]));
            }
            return list;
        }

    }
}
