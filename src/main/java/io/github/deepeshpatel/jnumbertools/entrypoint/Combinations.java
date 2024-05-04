package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.combination.CombinationBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

public class Combinations {

    private final Calculator calculator;

    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    public <T> CombinationBuilder<T> of(Collection< T> data, int ofSize) {
        return new CombinationBuilder<>(data, ofSize, calculator);
    }

    @SafeVarargs
    public final <T> CombinationBuilder<T> of(int size, T... data) {
        return of(List.of(data),size);
    }

    public CombinationBuilder<Integer> ofnCr(int n, int r) {
        var list = IntStream.range(0, n).boxed().toList();
        return of(list,r);
    }

}
