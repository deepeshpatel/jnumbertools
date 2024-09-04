package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import java.util.List;

public final class RepetitivePermutationBuilder<T> {

    private final int width;
    private final List<T> elements;

    public RepetitivePermutationBuilder(int width, List<T> elements) {
        this.width = width;
        this.elements = elements;
    }

    public RepetitivePermutation<T> lexOrder() {
        return new RepetitivePermutation<>(elements, width);
    }

    public RepetitivePermutationMth<T> lexOrderMth(long m) {
        return new RepetitivePermutationMth<>(elements, width, m);
    }

}
