/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.compound;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.HashSet;

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

    @Test
    void multiTriggerAndOrGroupsUseAndPrecedenceWithAnyOrder() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        List<String> database = List.of("PostgreSQL", "Oracle");
        var product = cartesianProduct.compoundConditionalProductOf(os, browser, database)
                .when(browser).is("Safari").and(os).is("Windows")
                .or(browser).is("Chrome").and(os).is("Linux")
                .then(database).isNot("PostgreSQL");

        List<List<Object>> expected = List.of(
                List.of("Windows", "Chrome", "PostgreSQL"),
                List.of("Windows", "Chrome", "Oracle"),
                List.of("Windows", "Safari", "Oracle"),
                List.of("Windows", "Firefox", "PostgreSQL"),
                List.of("Windows", "Firefox", "Oracle"),
                List.of("macOS", "Chrome", "PostgreSQL"),
                List.of("macOS", "Chrome", "Oracle"),
                List.of("macOS", "Safari", "PostgreSQL"),
                List.of("macOS", "Safari", "Oracle"),
                List.of("macOS", "Firefox", "PostgreSQL"),
                List.of("macOS", "Firefox", "Oracle"),
                List.of("Linux", "Chrome", "Oracle"),
                List.of("Linux", "Safari", "PostgreSQL"),
                List.of("Linux", "Safari", "Oracle"),
                List.of("Linux", "Firefox", "PostgreSQL"),
                List.of("Linux", "Firefox", "Oracle")
        );
        assertEquals(BigInteger.valueOf(16), product.count());
        assertEquals(new HashSet<>(expected), new HashSet<>(product.generate().stream().toList()));
        assertFalse(product.isEmpty());
    }

    @Test
    void orAlternativesOnOneDimensionAreSupported() {
        List<String> os = List.of("Windows", "macOS", "Linux");
        List<String> browser = List.of("Chrome", "Safari", "Firefox");
        var product = cartesianProduct.compoundConditionalProductOf(os, browser)
                .when(browser).is("Safari").or(browser).is("Chrome")
                .then(os).is("Windows");

        assertEquals(BigInteger.valueOf(5), product.count());
        List<List<Object>> expected = List.of(
                List.of("Windows", "Chrome"),
                List.of("Windows", "Safari"),
                List.of("Windows", "Firefox"),
                List.of("macOS", "Firefox"),
                List.of("Linux", "Firefox")
        );
        assertEquals(new HashSet<>(expected), new HashSet<>(product.generate().stream().toList()));
    }

    @Test
    void multiTriggerSupportsAnArbitraryLengthConjunction() {
        List<String> a = List.of("a0", "a1");
        List<String> b = List.of("b0", "b1");
        List<String> c = List.of("c0", "c1");
        List<String> d = List.of("d0", "d1");
        List<String> target = List.of("x", "y");

        var product = cartesianProduct.compoundConditionalProductOf(a, b, c, d, target)
                .when(a).is("a1").and(b).is("b1").and(c).is("c1").and(d).is("d1")
                .then(target).is("y");

        assertEquals(BigInteger.valueOf(31), product.count());
        assertEquals(31, product.generate().stream().count());
        assertFalse(product.isEmpty());
    }

    @Test
    void multiTriggerCanMakeTheEntireProductEmpty() {
        List<String> a = List.of("a");
        List<String> b = List.of("b");
        List<String> target = List.of("x", "y");
        var product = cartesianProduct.compoundConditionalProductOf(a, b, target)
                .when(a).is("a").and(b).is("b")
                .then(target).isNoneOf("x", "y");

        assertEquals(BigInteger.ZERO, product.count());
        assertTrue(product.isEmpty());
        assertTrue(product.generate().stream().toList().isEmpty());
    }

}
