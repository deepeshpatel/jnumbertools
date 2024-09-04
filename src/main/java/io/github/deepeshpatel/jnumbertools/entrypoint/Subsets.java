package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetBuilder;

import java.util.List;
import java.util.stream.IntStream;

public final class Subsets {

    private final Calculator calculator;

    public Subsets() {
        this(new Calculator());
    }

    public Subsets(Calculator calculator) {
        this.calculator = calculator;
    }

    @SafeVarargs
    public final <T> SubsetBuilder<T> of(T... elements) {
        return of(List.of(elements));
    }

    public <T> SubsetBuilder<T> of(List<T> elements) {
        return new SubsetBuilder<>(elements, calculator);
    }

    public SubsetBuilder<Integer> of(int dataSize) {
        var elements = IntStream.range(0, dataSize).boxed().toList();
        return new SubsetBuilder<>(elements, calculator);
    }


}
