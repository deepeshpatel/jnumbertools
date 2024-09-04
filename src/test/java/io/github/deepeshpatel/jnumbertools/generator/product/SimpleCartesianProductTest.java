package io.github.deepeshpatel.jnumbertools.generator.product;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;

public class SimpleCartesianProductTest {

    @Test
    public void shouldGenerateAllElementsOfCartesianProduct() {
        String expected = "[[0, A, 1], [0, A, 2], [0, A, 3], [0, B, 1], [0, B, 2], [0, B, 3], " +
                "[1, A, 1], [1, A, 2], [1, A, 3], [1, B, 1], [1, B, 2], [1, B, 3]]";
        var list = cartesianProduct
                .simpleProductOf(0,1)
                .and("A","B")
                .and(List.of(1,2,3))
                .lexOrder().stream().toList();

        Assert.assertEquals(expected,list.toString());
    }



}