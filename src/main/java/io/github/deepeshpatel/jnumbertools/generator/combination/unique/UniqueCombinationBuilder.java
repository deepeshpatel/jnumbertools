package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;

public final class UniqueCombinationBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final int size;
    private final Calculator calculator;
    private long count = -1;

    public UniqueCombinationBuilder(List<T> elements, int size, Calculator calculator) {
        this.elements = elements;
        this.size = size;
        this.calculator = calculator;
    }

    public UniqueCombination<T> lexOrder() {
        return new UniqueCombination<>(elements, size);
    }

    public UniqueCombinationMth<T> lexOrderMth(long m, long start) {
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public UniqueCombinationMth<T> lexOrderMth(BigInteger m, BigInteger start) {
        return new UniqueCombinationMth<>(elements, size, start, m, calculator);
    }

    public synchronized long count() {
        if(count == -1) {
            count =  calculator.nCr(elements.size(), size).longValue();
        }
        return count;
    }
}
