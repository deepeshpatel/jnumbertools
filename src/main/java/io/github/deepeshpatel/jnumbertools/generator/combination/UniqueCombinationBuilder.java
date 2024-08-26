package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.math.BigInteger;
import java.util.List;

public final class UniqueCombinationBuilder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;

    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    public UniqueCombination<T> lexOrder() {
        return new UniqueCombination<>(elements, size);
    }

    public UniqueCombinationMth<T> lexOrderMth(long m) {
        return lexOrderMth(BigInteger.valueOf(m));
    }

    public UniqueCombinationMth<T> lexOrderMth(BigInteger m) {
        return new UniqueCombinationMth<>(elements, size, m, calculator);
    }
}
