/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The work estimate of the unordered compound traversal, which parallel streams use to decide on splitting. */
class CompoundAnyOrderSplittingTest {

    private static List<Integer> range(int size) {
        return IntStream.range(0, size).boxed().toList();
    }

    @Test
    void anEmptyDimensionAfterOverflowingDimensionsMeansNoWorkAndNoSplit() {
        // Seven dimensions of 1,000 values multiply past Long.MAX_VALUE. The empty dimension after them still
        // makes the product empty, so the spliterator must report no work and must not split.
        List<List<Integer>> large = new ArrayList<>();
        for (int i = 0; i < 7; i++) large.add(range(1000));
        List<Integer> empty = new ArrayList<>();
        var product = cartesianProduct.compoundProductOf(large.get(0), large.get(1),
                large.get(2), large.get(3), large.get(4), large.get(5), large.get(6), empty);

        Spliterator<List<Object>> spliterator = product.generate().spliterator();
        assertEquals(0L, spliterator.estimateSize());
        assertTrue(spliterator.trySplit() == null, "an empty product has nothing to split");
        List<List<Object>> actual = new ArrayList<>();
        spliterator.forEachRemaining(actual::add);
        assertTrue(actual.isEmpty());
    }

    @Test
    void aMultiTriggerProductWithAnEmptyDimensionIsEmptyAndNotSplit() {
        // The rule's three dimensions are small; the seven large dimensions and the empty one are outside it.
        List<Integer> a = range(3);
        List<Integer> b = range(3);
        List<Integer> c = range(3);
        List<List<Integer>> large = new ArrayList<>();
        for (int i = 0; i < 7; i++) large.add(range(1000));
        List<Integer> empty = new ArrayList<>();
        var product = cartesianProduct.compoundProductOf(a, b, c)
                .and(large.get(0)).and(large.get(1)).and(large.get(2)).and(large.get(3))
                .and(large.get(4)).and(large.get(5)).and(large.get(6)).and(empty)
                .when(condition(a).is(0).and(condition(b).is(1))).then(c).isNot(0);

        Spliterator<List<Object>> spliterator = product.generate().spliterator();
        assertEquals(0L, spliterator.estimateSize());
        assertTrue(spliterator.trySplit() == null);
        assertTrue(product.isEmpty());
    }
}
