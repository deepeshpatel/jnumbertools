package io.github.deepeshpatel.jnumbertools.generator.combination;

import java.util.List;

public final class RepetitiveCombinationBuilder<T> {

    private final List<T> elements;
    private final int size;

    public RepetitiveCombinationBuilder(List<T> elements, int size) {
        this.elements = elements;
        this.size = size;
    }

    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }
}
