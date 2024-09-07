package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import io.github.deepeshpatel.jnumbertools.base.Combinations;
import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class SubsetGeneratorMth<T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final BigInteger increment;
    private final Calculator calculator;
    private final Combinations combinations;
    private BigInteger limit;
    private final BigInteger initialM;
    private final int from;
    private final int to;

    SubsetGeneratorMth(int from ,int to, BigInteger m, BigInteger start, List<T> elements, Calculator calculator) {
        super(elements);
        this.from = from;
        this.to = to;
        this.increment = m;
        this.calculator = calculator;
        combinations = new Combinations(calculator);
        initialM = start.add(increment).add(totalSubsetsBeforeRange());
    }

    /**
     * Use this method instead of iterator if you need only mth value and not 0th, mth, 2mth
     * creating iterator is expensive because hasNext() implementation requires expensive calculations.
     */
    public List<T> build() {
        var mthIndices =  mth(initialM);
        return indicesToValues(mthIndices.stream().mapToInt(Integer::intValue).toArray());
    }

    private List<Integer> mth(BigInteger m) {

        int r=0;
        int size = elements.size();
        BigInteger sum= BigInteger.ZERO;
        BigInteger prev= BigInteger.ZERO;

        for(; r<size  && sum.compareTo(m)< 0; r++) {
            prev = sum;
            sum =  sum.add(calculator.nCr(size,r));
        }

        if(sum.equals(m)) {
            return combinations.unique(size, r).lexOrder().iterator().next();
        }

        m=m.subtract(prev);
        return new Combinations(calculator).unique(size,r-1).lexOrderMth(m, BigInteger.ZERO).build();
    }

    private BigInteger totalSubsetsBeforeRange() {
        return calculator.totalSubsetsInRange(0, from-1, elements.size());
    }

    @Override
    public synchronized Iterator<List<T>> iterator() {

        if (limit == null) {
            limit = calculator.totalSubsetsInRange(0, to, elements.size());
        }
        return new Itr();
    }

    private class Itr implements Iterator<List<T>>{

        private BigInteger m = initialM.subtract(increment);

        @Override
        public boolean hasNext() {
            return m.compareTo(limit) < 0;
        }

        @Override
        public List<T> next() {

            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            List<Integer> result = mth(m);
            m = m.add(increment);
            return indicesToValues(result.stream().mapToInt(Integer::intValue).toArray());
        }
    }
}
