/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Rules are compiled through a value-to-position index shared by every builder derived from the same dimensions. */
class RuleCompilerTest {

    @Test
    void aValueThatAppearsTwiceInADimensionConstrainsBothPositions() {
        List<Integer> a = new ArrayList<>(List.of(1, 2, 2, 3));
        List<Integer> b = new ArrayList<>(List.of(10, 20));
        var builder = cartesianProduct.conditionalProductOf(a).and(b).when(a).is(2).then(b).isOneOf(10);

        List<List<Object>> expected = new ArrayList<>();
        for (int x : a) for (int y : b) if (x != 2 || y == 10) expected.add(List.of(x, y));
        assertEquals(expected, builder.lexOrder().stream().toList());
        assertEquals(BigInteger.valueOf(expected.size()), builder.count());

        var negated = cartesianProduct.conditionalProductOf(a).and(b).when(a).is(2).then(b).isNot(10);
        List<List<Object>> expectedNegated = new ArrayList<>();
        for (int x : a) for (int y : b) if (x != 2 || y != 10) expectedNegated.add(List.of(x, y));
        assertEquals(expectedNegated, negated.lexOrder().stream().toList());
    }

    @Test
    void unknownAndRepeatedValuesAreReported() {
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> b = new ArrayList<>(List.of(10, 20));
        var builder = cartesianProduct.conditionalProductOf(a).and(b);

        var error = assertThrows(IllegalArgumentException.class, () -> builder.when(a).is(99));
        assertTrue(error.getMessage().contains("Not a value of that dimension: [99]"), error.getMessage());
        assertThrows(IllegalArgumentException.class, () -> builder.when(a).isOneOf(List.of(1, 77, 88)));
        assertThrows(IllegalArgumentException.class, () -> builder.when(a).isOneOf(List.<Integer>of()));

        // naming a value twice is the same as naming it once
        var repeated = builder.when(a).isOneOf(List.of(2, 2, 2)).then(b).is(20);
        var once = builder.when(a).is(2).then(b).is(20);
        assertEquals(once.lexOrder().stream().toList(), repeated.lexOrder().stream().toList());
    }

    @Test
    void ruleOnADimensionAddedAfterEarlierRulesUsesTheExtendedIndex() {
        List<Integer> a = new ArrayList<>(List.of(0, 1, 2, 3));
        List<Integer> b = new ArrayList<>(List.of(0, 1, 2, 3));
        List<Integer> c = new ArrayList<>(List.of(0, 1, 2, 3));
        var builder = cartesianProduct.conditionalProductOf(a).and(b)
                .when(a).isOneOf(List.of(0, 1)).then(b).isOneOf(List.of(1, 2))
                .and(c)
                .when(b).is(2).then(c).isNotIn(List.of(0, 3))
                .when(c).is(1).then(a).isNot(1);

        List<List<Object>> expected = new ArrayList<>();
        for (int x : a) for (int y : b) for (int z : c) {
            boolean ok = (x > 1 || y == 1 || y == 2) && (y != 2 || (z != 0 && z != 3)) && (z != 1 || x != 1);
            if (ok) expected.add(List.of(x, y, z));
        }
        assertEquals(expected, builder.lexOrder().stream().toList());
        assertEquals(BigInteger.valueOf(expected.size()), builder.count());
    }

    @Test
    void builderDerivedConcurrentlyFromOneBaseAllSeeTheSameIndex() {
        List<Integer> a = new ArrayList<>(IntStream.range(0, 40).boxed().toList());
        List<Integer> b = new ArrayList<>(IntStream.range(0, 40).boxed().toList());
        var base = cartesianProduct.conditionalProductOf(a).and(b);

        List<BigInteger> counts = IntStream.range(0, 64).parallel()
                .mapToObj(i -> base.when(a).is(i % 40).then(b).isOneOf(List.of(i % 7, (i + 1) % 40)).count())
                .toList();
        for (int i = 0; i < 64; i++) {
            long allowed = (i % 7) == ((i + 1) % 40) ? 1 : 2;
            assertEquals(BigInteger.valueOf(39L * 40 + allowed), counts.get(i), "rule " + i);
        }
    }
}
