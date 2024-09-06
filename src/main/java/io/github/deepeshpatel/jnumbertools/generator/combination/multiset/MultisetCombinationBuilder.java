package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import java.util.List;

public final class MultisetCombinationBuilder<T> {

    private final List<T> elements;
    private final int[] frequencies;
    private final int size;

    public MultisetCombinationBuilder(List<T> elements, int[] frequencies, int size) {
        this.elements = elements;
        this.frequencies = frequencies;
        this.size = size;
    }

    public RepetitiveCombinationMultiset<T> lexOrder() {
        //TODO: change the signature and name.
        return new RepetitiveCombinationMultiset<>(elements, size,frequencies);
    }
}
