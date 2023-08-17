package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.PermutationBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class Permutations {

        public <T> PermutationBuilder<T> of(Collection<T> data) {
        return new PermutationBuilder<>(data);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final <T> PermutationBuilder<T>  of(T... data) {
        return of(List.of(data));
    }

    public PermutationBuilder<Integer> of(int size) {
        var list = IntStream.range(0, size).boxed().toList();
        return of(list);
    }
}
