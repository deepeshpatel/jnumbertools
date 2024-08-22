package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.generator.product.ProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.ProductBuilderMth;

import java.util.List;

public class Product {

    private final Calculator calculator;

    public Product() {
        this(new Calculator());
    }

    public Product(Calculator calculator) {
        this.calculator = calculator;
    }

    public <T> ProductBuilder<T> of(int quantity, List<T> data ) {
            return new ProductBuilder(quantity, data, calculator);
    }

    public <T> ProductBuilderMth<T> mthOf(long m, int quantity, List<T> data) {
        return new ProductBuilderMth(m, quantity, data, calculator);
    }





}
