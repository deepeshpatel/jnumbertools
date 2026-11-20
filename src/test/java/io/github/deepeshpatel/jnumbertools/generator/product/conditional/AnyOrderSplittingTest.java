/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The work threshold of {@code anyOrder()} splitting: enough partitions for parallelism, never one per tuple. */
class AnyOrderSplittingTest {

    private static List<Integer> range(int size) {
        return IntStream.range(0, size).boxed().toList();
    }

    private static void collectLeaves(Spliterator<List<Object>> spliterator, List<Spliterator<List<Object>>> leaves) {
        Spliterator<List<Object>> lower = spliterator.trySplit();
        if (lower == null) {
            leaves.add(spliterator);
            return;
        }
        collectLeaves(lower, leaves);
        collectLeaves(spliterator, leaves);
    }

    @Test
    void fullSplittingStaysNearTheThresholdAndCoversEveryTupleOnce() {
        List<Integer> a = range(40);
        List<Integer> b = range(40);
        List<Integer> c = range(40);
        var product = cartesianProduct.conditionalProductOf(a).and(b).and(c)
                .when(a).isOneOf(List.of(1, 5, 9, 33)).then(b).isOneOf(List.of(2, 4, 8, 16, 32));
        List<List<Object>> expected = product.lexOrder().stream().toList();

        List<Spliterator<List<Object>>> leaves = new ArrayList<>();
        collectLeaves(product.anyOrder().spliterator(), leaves);

        long bound = 40L * 40 * 40;
        assertTrue(leaves.size() >= 8, "a large product must expose parallel work, got " + leaves.size());
        assertTrue(leaves.size() <= 4 * bound / AnyOrderGenerator.SPLIT_WORK_THRESHOLD + 8,
                "partitions must not shrink far below the threshold, got " + leaves.size());

        List<List<Object>> actual = new ArrayList<>();
        for (Spliterator<List<Object>> leaf : leaves) leaf.forEachRemaining(actual::add);
        assertEquals(expected.size(), actual.size(), "partitions must not overlap");
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
    }

    @Test
    void aSmallProductIsNotSplit() {
        List<Integer> a = range(5);
        List<Integer> b = range(5);
        List<Integer> c = range(5);
        var product = cartesianProduct.conditionalProductOf(a).and(b).and(c).when(a).is(1).then(b).isNot(2);
        Spliterator<List<Object>> spliterator = product.anyOrder().spliterator();
        assertTrue(spliterator.trySplit() == null, "125 candidates are below the threshold");
        List<List<Object>> actual = new ArrayList<>();
        spliterator.forEachRemaining(actual::add);
        assertEquals(new HashSet<>(product.lexOrder().stream().toList()), new HashSet<>(actual));
        assertEquals(new HashSet<>(product.lexOrder().stream().toList()),
                new HashSet<>(product.anyOrder().stream().parallel().toList()));
    }

    @Test
    void theEstimateIsAnUpperBoundOnTheValidTuples() {
        List<Integer> a = range(4);
        List<Integer> b = range(5);
        List<Integer> c = range(6);
        var product = cartesianProduct.conditionalProductOf(a).and(b).and(c).when(a).is(1).then(b).isNot(2);
        long valid = product.lexOrder().stream().count();
        long estimate = product.anyOrder().spliterator().estimateSize();
        assertEquals(4L * 5 * 6, estimate);
        assertTrue(estimate >= valid);

        var empty = cartesianProduct.conditionalProductOf(a).and(List.<Integer>of()).and(c);
        assertEquals(0L, empty.anyOrder().spliterator().estimateSize());
    }
}
