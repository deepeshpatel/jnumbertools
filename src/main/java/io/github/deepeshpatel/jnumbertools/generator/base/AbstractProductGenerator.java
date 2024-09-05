package io.github.deepeshpatel.jnumbertools.generator.base;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

abstract public class AbstractProductGenerator implements  Iterable<List<?>> {

    protected final List<List<?>> elements;

    public AbstractProductGenerator(List<List<?>> elements) {
        this.elements = elements;
    }

    public Stream<List<?>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

//    @SuppressWarnings({"rawtypes","unchecked"})
//    protected List<List<?>> indicesToList(List<List<?>> elements, int[] indices) {
//        List<List<?>> list = new ArrayList(elements.size());
//        for (int i = 0; i < elements.size(); i++) {
//            list.add(elements.get(i).get(indices[i]));
//        }
//        return list;
//    }
}
