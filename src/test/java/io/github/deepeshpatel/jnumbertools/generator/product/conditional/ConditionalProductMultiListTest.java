/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionalProductMultiListTest {

    @Test
    void multipleListOverloadMatchesAndChaining() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> c = List.of("c0", "c1");

        var overloaded = cartesianProduct.conditionalProductOf(a, b, c)
                .when(a).is("a1").then(c).is("c1");
        var chained = cartesianProduct.conditionalProductOf(a)
                .and(b).and(c)
                .when(a).is("a1").then(c).is("c1");

        assertEquals(chained.count(), overloaded.count());
        assertEquals(chained.lexOrder().stream().toList(), overloaded.lexOrder().stream().toList());
    }
}
