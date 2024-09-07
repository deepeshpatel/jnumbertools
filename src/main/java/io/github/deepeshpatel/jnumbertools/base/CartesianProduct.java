package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.complex.ComplexProductBuilder;

import java.util.List;

public final class CartesianProduct {

    private final Calculator calculator;

    public CartesianProduct() {
        this(new Calculator());
    }

    public CartesianProduct(Calculator calculator) {
        this.calculator = calculator;
    }

    public SimpleProductBuilder simpleProductOf(List<?> elements) {
        return new SimpleProductBuilder(elements);
    }

    @SafeVarargs
    public final <E> SimpleProductBuilder simpleProductOf(E... elements) {
        return simpleProductOf(List.of(elements));
    }

    public ComplexProductBuilder complexProductOf(int quantity, List<?> elements) {
        return new ComplexProductBuilder(quantity, elements, calculator);
    }

    @SafeVarargs
    public final <E> ComplexProductBuilder complexProductOf(int quantity, E... elements) {
        return complexProductOf(quantity, List.of(elements));
    }
}
