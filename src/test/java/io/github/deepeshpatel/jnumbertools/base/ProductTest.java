/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.base;

import io.github.deepeshpatel.jnumbertools.datastructure.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.Product;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void simpleProductStartsAtProduct() {
        List<String> letters = List.of("A", "B");
        List<Integer> numbers = List.of(1, 2, 3);

        var product = Product.simple(letters, numbers);

        assertEquals(BigInteger.valueOf(6), product.count());
        assertEquals(6, product.lexOrder().stream().count());
    }

    @Test
    void constrainedProductSupportsDefaultAndSharedCalculator() {
        List<String> values = List.of("A", "B", "C");
        Calculator calculator = new Calculator();

        var defaultCalculatorProduct = Product.constrained()
                .ofDistinct(2, values);
        var sharedCalculatorProduct = Product.constrained(calculator)
                .ofDistinct(2, values);

        assertEquals(BigInteger.valueOf(3), defaultCalculatorProduct.count());
        assertEquals(BigInteger.valueOf(3), sharedCalculatorProduct.count());
    }

    @Test
    void typedLambdaOverloadsSupportOneThroughFiveDimensions() {
        List<Integer> d = List.of(0, 1);

        var one = Product.conditional(d)
                .withLambda(d)
                .when(value -> value == 1);
        var two = Product.conditional(d, d)
                .withLambda(d, d)
                .when((a, b) -> a <= b);
        var three = Product.conditional(d, d, d)
                .withLambda(d, d, d)
                .when((a, b, c) -> a + b + c >= 2);
        var four = Product.conditional(d, d, d, d)
                .withLambda(d, d, d, d)
                .when((a, b, c, e) -> a + b + c + e >= 3);
        var five = Product.conditional(d, d, d, d, d)
                .withLambda(d, d, d, d, d)
                .when((a, b, c, e, f) -> a + b + c + e + f >= 4);

        assertEquals(BigInteger.ONE, one.count());
        assertEquals(BigInteger.valueOf(3), two.count());
        assertEquals(BigInteger.valueOf(4), three.count());
        assertEquals(BigInteger.valueOf(5), four.count());
        assertEquals(BigInteger.valueOf(6), five.count());
    }

    @Test
    void typedLambdaTransitionPreservesPreviouslyConfiguredPairwiseRules() {
        List<String> operatingSystems = List.of("Windows", "Linux");
        List<String> browsers = List.of("Chrome", "Firefox");

        var product = Product.conditional(operatingSystems, browsers)
                .when(operatingSystems).is("Linux").then(browsers).is("Firefox")
                .withLambda(operatingSystems, browsers)
                .when((os, browser) -> os.equals("Windows") || browser.equals("Firefox"));

        assertEquals(BigInteger.valueOf(3), product.count());
        assertEquals(List.of(
                List.of("Windows", "Chrome"),
                List.of("Windows", "Firefox"),
                List.of("Linux", "Firefox")
        ), product.lexOrder().stream().toList());
    }

    @Test
    void noArgumentLambdaEntryPointRetainsArrayBasedSupport() {
        List<Integer> d = List.of(0, 1);

        var product = Product.conditional(d, d, d, d, d, d)
                .withLambda()
                .when(values -> values[0].equals(values[5]));

        assertEquals(BigInteger.valueOf(32), product.count());
    }

    @Test
    void typedLambdaEntryPointsValidateDimensionOrderAndArity() {
        List<Integer> first = List.of(0, 1);
        List<Integer> second = List.of(2, 3);

        var product = Product.conditional(first, second);

        assertThrows(IllegalArgumentException.class, () -> product.withLambda(second, first));
        assertThrows(IllegalArgumentException.class, () -> product.withLambda(first));
        assertThrows(NullPointerException.class, () -> product.withLambda(first, null));
    }
}
