package io.github.deepeshpatel.jnumbertools.generator.product.complex;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.Combinations;
import io.github.deepeshpatel.jnumbertools.entrypoint.Subsets;
import io.github.deepeshpatel.jnumbertools.generator.base.Builder;

import java.util.ArrayList;
import java.util.List;

public final class ComplexProductBuilder {

    private final List<Builder> builders = new ArrayList<>();

    private final Calculator calculator;

    public ComplexProductBuilder(int n, List<?> elements, Calculator calculator) {
        this.calculator = calculator;
        builders.add(new Combinations(calculator).unique(n, elements));
    }

    public ComplexProductBuilder andDistinct(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).unique(quantity, elements));
        return this;
    }

    public ComplexProductBuilder andMultiSelect(int quantity, List<?> elements) {
        builders.add(new Combinations(calculator).repetitive(quantity, elements));
        return this;
    }

    public ComplexProductBuilder andInRange(int from, int to, List<?> elements) {
        builders.add(new Subsets(calculator).of(elements).inRange(from, to));
        return this;
    }

    public ComplexProduct lexOrder() {

        List<List<List<?>>> all = new ArrayList<>();
        for(var e: builders) {
            all.add(e.lexOrder().stream().toList());
        }

        return new ComplexProduct(all);
    }

    public ComplexProductMth lexOrderMth(long m, long start) {
        return new ComplexProductMth(m, start, builders);
    }


}
