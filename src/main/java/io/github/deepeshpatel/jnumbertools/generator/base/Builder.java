package io.github.deepeshpatel.jnumbertools.generator.base;

public interface Builder<E> {

    AbstractGenerator<E> lexOrder();
    MthElementGenerator<E> lexOrderMth(long m, long start);

    long count();

}
