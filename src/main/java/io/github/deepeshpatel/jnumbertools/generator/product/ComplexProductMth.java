package io.github.deepeshpatel.jnumbertools.generator.product;

import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComplexProductMth { // implements Iterable<List> {

    private final long m;
    private final List<Builder> builders;

    public ComplexProductMth(long m, List<Builder> builders) {
        this.m = m;
        this.builders = builders;
    }

    public List build() {
        List output = new ArrayList<>();
        long remaining = m;
        for(int i = builders.size()-1; i>=0; i--) {
            Builder e = builders.get(i);
            long index = remaining % e.count();
            remaining = remaining / e.count();
            List values = e.lexOrderMth(index,0).build();
            Collections.reverse(values);
            output.addAll(values);
        }
        Collections.reverse(output);
        return output;
    }
}
