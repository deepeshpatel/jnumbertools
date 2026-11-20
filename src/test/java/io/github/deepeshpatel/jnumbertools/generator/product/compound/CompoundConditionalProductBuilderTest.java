/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.cartesianProduct;
import static org.junit.jupiter.api.Assertions.*;

class CompoundConditionalProductBuilderTest {

    @Test
    void compoundEntryPointGeneratesAndCountsMatchingTuples() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");

        var product = cartesianProduct.compoundConditionalProductOf(os, browser, database)
                .when(browser).is("Safari").and(os).is("Windows")
                .or(browser).is("Chrome").and(os).is("Linux")
                .then(database).isNot("PostgreSQL");

        assertEquals(BigInteger.valueOf(16), product.count());
        assertFalse(product.isEmpty());
        assertEquals(16, product.generate().stream().count());
    }

    @Test
    void compoundBuilderCanBeExtendedAfterACompletedRule() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> target = List.of("x", "y");

        var product = cartesianProduct.compoundConditionalProductOf(a)
                .and(b)
                .and(target)
                .when(a).is("a1").and(b).is("b1")
                .then(target).is("y")
                .when(a).is("a0")
                .then(target).is("x");

        assertEquals(BigInteger.valueOf(5), product.count());
        assertEquals(5, product.generate().stream().count());
    }

    @Test
    void compoundBuilderReportsEmptyProducts() {
        List<String> a = List.of("a");
        List<String> b = List.of("b");
        List<String> target = List.of("x");

        var product = cartesianProduct.compoundConditionalProductOf(a, b, target)
                .when(a).is("a").and(b).is("b")
                .then(target).isNoneOf("x");

        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.isEmpty());
        assertTrue(product.generate().stream().toList().isEmpty());
    }
}
