package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.combination.multiset.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.repetitive.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.unique.UniqueCombinationBuilder;

import java.util.List;
import java.util.stream.IntStream;

public final class Combinations {

    private final Calculator calculator;

    public Combinations() {
        this(new Calculator());
    }

    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    public UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return unique(r, elements);
    }

    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int size, T... elements) {
        return unique(size, List.of(elements));
    }

    public <T> UniqueCombinationBuilder<T> unique(int size, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, size, calculator);
    }

    public RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return repetitive(r, elements);
    }

    @SafeVarargs
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int size, T... elements) {
        return  repetitive(size, List.of(elements));
    }
    public  <T> RepetitiveCombinationBuilder<T> repetitive(int size, List<T> elements) {
        return new RepetitiveCombinationBuilder<>(elements, size, calculator);
    }

    public <T> MultisetCombinationBuilder<T> multiset(List<T> elements, int[] frequencies, int size) {
        return new MultisetCombinationBuilder<>(elements, frequencies, size);
    }
}
