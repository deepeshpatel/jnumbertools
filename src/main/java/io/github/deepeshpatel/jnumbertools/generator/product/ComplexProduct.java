
package io.github.deepeshpatel.jnumbertools.generator.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.deepeshpatel.jnumbertools.generator.product.SimpleProduct.createNext;

public class ComplexProduct implements Iterable<List<?>> {

    private final List<List<List<?>>> elements;

    public ComplexProduct(List<List<List<?>>> elements) {
        this.elements = elements;
    }

    public long count() {
        long count = 1;
        for(var e: elements) {
            count  *= e.size();
        }
        return count;
    }

    @Override
    public Iterator<List<?>> iterator() {
        return new Itr();
    }

    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
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
            var result = indicesToList();
            hasNext = createNext(current, elements);
            return result;
        }

        private List<Object> indicesToList() {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < elements.size(); i++) {
                list.addAll(elements.get(i).get(current[i]));
            }
            return list;
        }
    }
}
