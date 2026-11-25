/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionMatchesTest {

    @Test
    void matchesSupportsDifferentDimensionTypesAndBooleanComposition() {
        List<String> browser = List.of("Chrome", "Safari", "Chromium", "Firefox");
        List<Integer> memory = List.of(8, 16, 32);
        List<String> deployment = List.of("Cloud", "OnPremise");

        var trigger = condition(browser).matches(value -> value.startsWith("Ch"))
                .and(condition(memory).matches(value -> value >= 16))
                .or(condition(memory).matches(value -> value == 8));

        var product = cartesianProduct.compoundProductOf(browser, memory, deployment)
                .when(trigger)
                .then(deployment).isNot("OnPremise");

        long expected = 0;
        for (String b : browser) {
            for (int m : memory) {
                for (String d : deployment) {
                    boolean matches = (b.startsWith("Ch") && m >= 16) || m == 8;
                    if (!matches || !d.equals("OnPremise")) expected++;
                }
            }
        }
        assertEquals(expected, product.count().longValueExact());
        assertEquals(expected, product.generate().stream().count());
    }

    @Test
    void matchesPreservesDuplicateDimensionPositions() {
        List<String> browser = List.of("Chrome", "Firefox", "Chrome");
        List<String> deployment = List.of("Cloud", "OnPremise");

        var product = cartesianProduct.compoundProductOf(browser, deployment)
                .when(condition(browser).matches(value -> value.startsWith("Ch")))
                .then(deployment).isNot("OnPremise");

        assertEquals(4, product.count().intValueExact());
        assertEquals(4, product.generate().stream().count());
    }

    @Test
    void predicateIsEvaluatedDuringCompilationNotGeneration() {
        List<Integer> memory = List.of(8, 16, 32, 64);
        List<String> deployment = List.of("Cloud", "OnPremise");
        AtomicInteger evaluations = new AtomicInteger();

        var condition = condition(memory).matches(value -> {
            evaluations.incrementAndGet();
            return value >= 16;
        });
        var product = cartesianProduct.compoundProductOf(memory, deployment)
                .when(condition)
                .then(deployment).isNot("OnPremise");

        assertEquals(memory.size(), evaluations.get());
        product.count();
        product.generate().stream().toList();
        product.generate().stream().toList();
        assertEquals(memory.size(), evaluations.get());
    }

    @Test
    void predicateExceptionPropagatesWhenConditionIsCompiled() {
        List<Integer> memory = List.of(8, 16, 32);
        List<String> deployment = List.of("Cloud", "OnPremise");
        var builder = cartesianProduct.compoundProductOf(memory, deployment);

        assertThrows(IllegalStateException.class,
                () -> builder.when(condition(memory).matches(value -> {
                    if (value == 16) throw new IllegalStateException("predicate failed");
                    return true;
                })));
    }

    @Test
    void matchesRejectsNullPredicate() {
        List<String> browser = new ArrayList<>(List.of("Chrome"));
        assertThrows(NullPointerException.class, () -> condition(browser).matches(null));
    }
}
