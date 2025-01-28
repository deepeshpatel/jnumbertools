package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Test;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleCartesianProductTest {

    @Test
    void shouldGenerateAllElementsOfCartesianProduct() {
        String expected = "[[0, A, 1], [0, A, 2], [0, A, 3], [0, B, 1], [0, B, 2], [0, B, 3], " +
                "[1, A, 1], [1, A, 2], [1, A, 3], [1, B, 1], [1, B, 2], [1, B, 3]]";
        var list = cartesianProduct
                .simpleProductOf(0, 1)
                .and(A_B)
                .and(num_1_2_3)
                .lexOrder().stream().toList();

        assertEquals(expected, list.toString());
    }
}
