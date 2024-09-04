package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.math.BigInteger;
import java.util.List;


public class SubsetBuilder<T> implements Builder<T> {

    private final List<T> elements;
    private final Calculator calculator;
    private int from = -1;
    private int to = -1;
    private long count = -1;



    public SubsetBuilder(List<T> elements, Calculator calculator) {
        this.elements = elements;
        this.calculator = calculator;
    }

    public SubsetBuilder<T> all(){
        this.from = 0;
        this.to = elements.size();
        return this;
    }

    public SubsetBuilder<T> inRange(int from, int to) {
        if(to < from) {
            throw new IllegalArgumentException("parameter 'to' >= parameter 'from");
        }
        this.from = from;
        this.to = to;
        return this;
    }

    public  SubsetGenerator<T> lexOrder(){
        if(from <0 || to < 0) {
            throw new IllegalArgumentException("Must specify range of subsets via method inRange()  or all()");
        }

        return new SubsetGenerator<>(elements,from, to);
    }

    public SubsetGeneratorMth<T> lexOrderMth(long m, long start){
        return lexOrderMth(BigInteger.valueOf(m), BigInteger.valueOf(start));
    }

    public SubsetGeneratorMth<T> lexOrderMth(BigInteger m, BigInteger start){
        return new SubsetGeneratorMth<>(from, to, m, start, elements, calculator);
    }

    public synchronized long count() {
        if(count == -1) {
            count =  calculator.totalSubsetsInRange(from, to, elements.size()).longValue();
        }
        return count;
    }
}

