/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.compiled.CompiledShapes;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.compiled.CompiledShapes.Shape;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The public compiled API must produce the same answers as the ordinary conditional builder. */
class CompiledConditionalProductTest {

    private static List<Object> first(Iterator<?> it, int n) {
        List<Object> out = new ArrayList<>();
        while (out.size() < n && it.hasNext()) out.add(it.next());
        return out;
    }

    @Test
    void everyShapeAndDatasetMatchesTheOrdinaryBuilder() {
        for (String name : CompiledShapes.SHAPES) {
            Shape shape = CompiledShapes.shape(name);
            var compiled = CompiledShapes.compile(shape);
            for (List<List<Integer>> dims : CompiledShapes.datasets(shape, 5)) {
                ConditionalProductBuilder ordinary = CompiledShapes.uncompiled(shape, dims);
                var bound = CompiledShapes.bind(compiled, dims);
                assertEquals(ordinary.count(), bound.count(), name);
                assertEquals(ordinary.isEmpty(), bound.isEmpty(), name);
                for (LexOrderStrategy strategy : new LexOrderStrategy[]{LexOrderStrategy.PRUNED_BITSET,
                        LexOrderStrategy.PRUNED_DFS, LexOrderStrategy.DP}) {
                    assertEquals(first(ordinary.lexOrder(strategy).iterator(), 2000),
                            first(bound.lexOrder(strategy).iterator(), 2000), name + "/" + strategy);
                }
            }
        }
    }

    @Test
    void missingRuleValueFailsAtBindTime() {
        var compiled = JNumberTools.cartesianProduct().conditional()
                .compiledForDimensions("a", "b")
                .when("a").is(1).then("b").isOneOf(9)
                .compile();
        assertThrows(IllegalArgumentException.class,
                () -> compiled.bind(List.of(1, 2), List.of(3, 4)));
    }

    @Test
    void unknownAndRepeatedNamesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> JNumberTools.cartesianProduct().conditional().compiledForDimensions("a", "a"));
        assertThrows(IllegalArgumentException.class,
                () -> JNumberTools.cartesianProduct().conditional().compiledForDimensions("a", "b").when("c"));
    }

    @Test
    void uncompiledEntryPointUsesOfList() {
        List<String> a = List.of("a", "b");
        List<Integer> b = List.of(1, 2);
        var builder = JNumberTools.cartesianProduct().conditional().ofList(a).and(b);
        assertEquals(4, builder.count().intValueExact());
    }
}
