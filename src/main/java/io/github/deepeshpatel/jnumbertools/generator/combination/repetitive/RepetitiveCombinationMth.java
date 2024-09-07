package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

public class RepetitiveCombinationMth <T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final int r;
    private final long start;
    private final long increment;
    private final Calculator calculator;

    RepetitiveCombinationMth(List<T> elements, int r, long increment, long start, Calculator calculator) {
        super(elements);
        this.r = r;
        this.increment = increment;
        this.start = start;
        this.calculator = calculator;

    }

    /**
     * Use this method instead of iterator if you need only mth value and not 0th, mth, 2mth
     * creating iterator is expensive because hasNext() implementation requires expensive calculations.
     */
    public List<T> build() {
        int[] idx = mthCombinationWithRepetition(increment);
        return super.indicesToValues(idx);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<List<T>> {

        final long max;
        long current = start;

        public Itr() {
            this.max = calculator.nCrRepetitive(elements.size(), r).longValue();
        }

        @Override
        public boolean hasNext() {

            return current< max;
        }

        @Override
        public List<T> next() {
            int[] idx = mthCombinationWithRepetition(current);
            var result = indicesToValues(idx);
            current += increment;
            return result;
        }
    }

    private  int[] mthCombinationWithRepetition(long m) {
        int n = elements.size();
        int[] result = new int[r];
        BigInteger remainingM = BigInteger.valueOf(m);
        int currentValue = 0;

        for (int i = 0; i < r; i++) {
            for (int j = currentValue; j < n; j++) {
                BigInteger combinationsWithCurrentElement =  calculator.nCrRepetitive(n - j, r - i - 1);
                if (remainingM.compareTo(combinationsWithCurrentElement) < 0) {
                    result[i] = j;
                    currentValue = j;
                    break;
                }
                remainingM = remainingM.subtract(combinationsWithCurrentElement);
            }
        }

        return result;
    }
}
