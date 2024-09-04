package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.base.AbstractGenerator;
import io.github.deepeshpatel.jnumbertools.generator.base.MthElementGenerator;

import java.util.Iterator;
import java.util.List;

/*
 * Note: This class Do not actually implement mth repetitive combination.
 * Used as a hack to implement ComplexProductBuilder for now.
 * Will do it later
 */
public class RepetitiveCombinationMth <T> extends AbstractGenerator<T> implements MthElementGenerator<T> {

    private final int r;
    private final long start;
    private final long increment;


    RepetitiveCombinationMth(List<T> elements, int r, long increment, long start) {
        super(elements);
        this.r = r;
        this.increment = increment;
        this.start = start;
    }

    public List<T> build() {
        return new RepetitiveCombination<>(elements, r).stream().toList().get((int)increment);
    }

    @Override
    public Iterator<List<T>> iterator() {
        throw new UnsupportedOperationException("iterator not implemented for mth repetitive combination");
    }
}
