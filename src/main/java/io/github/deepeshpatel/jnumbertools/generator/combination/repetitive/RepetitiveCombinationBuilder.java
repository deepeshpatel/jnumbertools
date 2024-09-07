package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.util.List;

public final class RepetitiveCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;
    private long count = -1;

    public RepetitiveCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    public RepetitiveCombination<T> lexOrder() {
        return new RepetitiveCombination<>(elements, size);
    }

    public RepetitiveCombinationMth<T> lexOrderMth(long m, long start) {
        return new RepetitiveCombinationMth<>(elements, size, m, start, calculator);
    }

    public synchronized long count() {

        if (count == -1) {
            count = calculator.nCrRepetitive(elements.size(), size).longValue();
        }
        return count;
    }
}
