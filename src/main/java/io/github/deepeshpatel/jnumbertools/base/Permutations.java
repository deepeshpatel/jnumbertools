package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.permutation.k.KPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.multiset.MultisetPermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive.RepetitivePermutationBuilder;
import io.github.deepeshpatel.jnumbertools.generator.permutation.unique.UniquePermutationBuilder;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;


/**
 * Utility class to generate 12 different types of permutations
 *
 * Examples -
 *
 * Generate all unique permutations in lex order
 * <pre>JNumberTools.permutations().unique("A","B","C").lexOrder()</pre>
 *
 * Generate very mth unique permutation staring from given index
 * <pre>JNumberTools.permutations().unique("A","B","C").lexOrderMth(3,0)</pre>
 *
 * Generate all repetitive permutations in lex order
 * <pre>JNumberTools.permutations().repetitive(3,"A","B","C").lexOrder()</pre>
 *
 * Generate every mth repetitive permutation staring from given index
 * <pre>JNumberTools.permutations().repetitive(3,"A","B","C").lexOrderMth(3, 0)</pre>
 *
 *
 */
public final class Permutations {

    private final Calculator calculator;

    public Permutations() {
        this(new Calculator());
    }

    public Permutations(Calculator calculator) {
        this.calculator = calculator;
    }

    public UniquePermutationBuilder<Integer> unique(int size) {
        return unique(IntStream.range(0, size).boxed().toList());
    }

    public <T> UniquePermutationBuilder<T> unique(List<T> list) {
        return new UniquePermutationBuilder<>(calculator, list);
    }

    @SafeVarargs
    public final <T> UniquePermutationBuilder<T> unique(T... array) {
        return unique(List.of(array));
    }

    public KPermutationBuilder<Integer> nPr(int n, int r) {
        return nPr(r, IntStream.range(0, n).boxed().toList());
    }

    public <T> KPermutationBuilder<T> nPr(int r, List<T> allElements) {
        return new KPermutationBuilder<>(allElements, r, calculator);
    }

    @SafeVarargs
    public final <T> KPermutationBuilder<T> nPr(int r, T... allElements) {
        return nPr(r, asList(allElements));
    }

    public <T> MultisetPermutationBuilder<T> multiset(List<T> elements, int[] frequencies) {
        return new MultisetPermutationBuilder<>(elements, frequencies, calculator);
    }

    public  <T> RepetitivePermutationBuilder<T> repetitive(int width, List<T> elements) {
        return new RepetitivePermutationBuilder<>(width, elements);
    }

    @SafeVarargs
    public final <T> RepetitivePermutationBuilder<T> repetitive(int width, T... elements) {
        return repetitive(width, List.of(elements));
    }

    public RepetitivePermutationBuilder<Integer> repetitive(int width, int base) {
        var symbols = IntStream.range(0, base).boxed().toList();
        return repetitive(width, symbols);
    }
}
