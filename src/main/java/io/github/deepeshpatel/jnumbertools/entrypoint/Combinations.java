package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.combination.MultisetCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.RepetitiveCombinationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationBuilder;

import java.util.List;
import java.util.stream.IntStream;

public class Combinations {

    private final Calculator calculator;

    public Combinations() {
        this(new Calculator());
    }

    public Combinations(Calculator calculator) {
        this.calculator = calculator;
    }

    public final UniqueCombinationBuilder<Integer> unique(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return unique(r, elements);
    }

    @SafeVarargs
    public final <T> UniqueCombinationBuilder<T> unique(int size, T... elements) {
        return unique(size, List.of(elements));
    }

    public final <T> UniqueCombinationBuilder<T> unique(int size, List<T> elements) {
        return new UniqueCombinationBuilder<>(elements, size, calculator);
    }

    public final RepetitiveCombinationBuilder<Integer> repetitive(int n, int r) {
        var elements = IntStream.range(0,n).boxed().toList();
        return new RepetitiveCombinationBuilder<>(elements, r);
    }

    @SafeVarargs
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int size, T... elements) {
        return new RepetitiveCombinationBuilder<>(List.of(elements), size);
    }
    public final <T> RepetitiveCombinationBuilder<T> repetitive(int size, List<T> elements) {
        return new RepetitiveCombinationBuilder<>(elements, size);
    }

    public final <T> MultisetCombinationBuilder<T> multiset(List<T> elements, int[] frequencies, int size) {
        return new MultisetCombinationBuilder<>(elements, frequencies, size);
    }
}
