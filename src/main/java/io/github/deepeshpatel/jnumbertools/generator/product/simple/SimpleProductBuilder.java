package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("rawtypes")
public final class SimpleProductBuilder {

    private final List<List<?>> allLists = new ArrayList<>();

    public SimpleProductBuilder(List elements) {
        allLists.add(elements);
    }

    public SimpleProductBuilder and(List elements) {
        allLists.add(elements);
        return this;
    }

    @SafeVarargs
    public final <E> SimpleProductBuilder and(E... elements) {
        return and(List.of(elements));
    }

    public SimpleProduct lexOrder() {
        return new SimpleProduct(allLists);
    }

    public SimpleProductMth lexOrderMth(long m, long start) {
        return new SimpleProductMth(m, start, allLists);
    }
}
