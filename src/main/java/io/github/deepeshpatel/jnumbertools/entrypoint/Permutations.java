package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.KPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.MultisetPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.RepetitivePermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.builder.UniquePermutationBuilder;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public class Permutations {

    private final Calculator calculator;

    public Permutations() {
        this(new Calculator());
    }

    public Permutations(Calculator calculator) {
        this.calculator = calculator;
    }

    public final UniquePermutationBuilder<Integer> unique(int size) {
        return unique(IntStream.range(0, size).boxed().toList());
    }

    public final <T> UniquePermutationBuilder<T> unique(List<T> list) {
        return new UniquePermutationBuilder<>(calculator, list);
    }

    @SafeVarargs
    public final <T> UniquePermutationBuilder<T> unique(T... array) {
        return unique(List.of(array));
    }

    public final KPermutationBuilder<Integer> nPr(int n, int r) {
        return nPr(r, IntStream.range(0, n).boxed().toList());
    }

    public final <T> KPermutationBuilder<T> nPr(int r, List<T> allElements) {
        return new KPermutationBuilder<>(allElements, r, calculator);
    }

    @SafeVarargs
    public final <T> KPermutationBuilder<T> nPr(int r, T... allElements) {
        return nPr(r, asList(allElements));
    }

    public final <T> MultisetPermutationBuilder<T> multiset(List<T> elements, int[] frequencies) {
        return new MultisetPermutationBuilder<>(elements, frequencies, calculator);
    }

    public final <T> RepetitivePermutationBuilder<T> repetitive(int width, List<T> elements) {
        return new RepetitivePermutationBuilder<>(width, elements);
    }

    @SafeVarargs
    public final <T> RepetitivePermutationBuilder<T> repetitive(int width, T... elements) {
        return repetitive(width, List.of(elements));
    }

    public final RepetitivePermutationBuilder<Integer> repetitive(int width, int base) {
        var symbols = IntStream.range(0, base).boxed().toList();
        return repetitive(width, symbols);
    }
}
