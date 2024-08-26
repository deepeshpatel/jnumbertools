package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.subset.SubsetGenerator;

import java.util.List;
import java.util.stream.IntStream;

public final class Subsets {

    @SafeVarargs
    public final <T> SubsetGenerator.Builder<T> of(T... data) {
        return of(List.of(data));
    }

    public <T> SubsetGenerator.Builder<T> of(List<T> data) {
        return new SubsetGenerator.Builder<>(data);
    }

    public SubsetGenerator.Builder<Integer> of(int dataSize) {
        var list = IntStream.range(0, dataSize).boxed().toList();
        return new SubsetGenerator.Builder<>(list);
    }
}
