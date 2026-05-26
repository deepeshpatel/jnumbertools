/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code cartesianProduct.simpleProductOf(L₁).and(L₂)...} — Cartesian
 * product whose count is {@code |L₁|·|L₂|·…·|Lₖ|}.
 *
 * <p>Convention: a single empty dimension yields the unique empty tuple
 * (count = 1, output = [[]]); any other empty dimension causes the whole product
 * to be empty (count = 0, output = []).
 */
@DisplayName("Simple Cartesian Product (lex order)")
class SimpleCartesianProductTest {

    // =========================================================
    // 1. Count correctness: Π |Lᵢ|
    // =========================================================
    @Nested
    @DisplayName("Count: Π |Lᵢ|")
    class CountTests {

        @Test
        @DisplayName("|L₁|·|L₂|·|L₃| for non-empty dimensions")
        void countFormula() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1))
                    .and(A_B)
                    .and(num_1_2_3);
            long expected = 2L * 2 * 3;
            assertEquals(expected, product.lexOrder().stream().count());
            assertEquals(BigInteger.valueOf(expected), product.count());
        }

        @Test
        @DisplayName("count() agrees with stream count")
        void builderCountMatchesStreamCount() {
            var product = cartesianProduct.simpleProductOf(num_1_to_4).and(A_B_C);
            assertEquals(product.count().longValue(),
                    product.lexOrder().stream().count());
        }
    }

    // =========================================================
    // 2. Edge cases — empty dimensions
    // =========================================================
    @Nested
    @DisplayName("Edge cases with empty dimensions")
    class EmptyDimensions {

        @Test
        @DisplayName("Single empty dimension: count=1, single empty tuple")
        void singleEmpty() {
            var b = cartesianProduct.simpleProductOf(Collections.emptyList());
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("Non-empty × empty: count=0, no tuples")
        void nonEmptyTimesEmpty() {
            var b = cartesianProduct.simpleProductOf(A_B).and(Collections.emptyList());
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Empty × non-empty: count=0, no tuples")
        void emptyTimesNonEmpty() {
            var b = cartesianProduct.simpleProductOf(Collections.emptyList()).and(A_B);
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Empty × empty: count=0, no tuples")
        void emptyTimesEmpty() {
            var b = cartesianProduct.simpleProductOf(Collections.emptyList())
                    .and(Collections.emptyList());
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Empty in the middle of multiple dimensions: count=0")
        void emptyInMiddle() {
            var b = cartesianProduct.simpleProductOf(A_B)
                    .and(Collections.emptyList())
                    .and(num_1_2_3);
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("and(empty) returns a NEW builder; original is unchanged")
        void andEmptyDoesNotMutateOriginal() {
            var original = cartesianProduct.simpleProductOf(A_B);
            var withEmpty = original.and(Collections.emptyList());
            assertEquals(BigInteger.valueOf(2), original.count(),
                    "the builder used to start the chain must remain unchanged");
            assertEquals(BigInteger.ZERO, withEmpty.count());
        }
    }

    // =========================================================
    // 3. Content correctness
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All 12 tuples of {0,1}×{A,B}×{1,2,3} in lex order")
        void fullThreeDimensionalProduct() {
            var expected = List.of(
                    List.of(0, 'A', 1), List.of(0, 'A', 2), List.of(0, 'A', 3),
                    List.of(0, 'B', 1), List.of(0, 'B', 2), List.of(0, 'B', 3),
                    List.of(1, 'A', 1), List.of(1, 'A', 2), List.of(1, 'A', 3),
                    List.of(1, 'B', 1), List.of(1, 'B', 2), List.of(1, 'B', 3)
            );
            var actual = cartesianProduct
                    .simpleProductOf(List.of(0, 1))
                    .and(A_B)
                    .and(num_1_2_3)
                    .lexOrder().stream().toList();
            assertIterableEquals(expected, actual);
        }

        @Test
        @DisplayName("Single dimension: identity, one 1-tuple per element")
        void singleDimensionIsIdentity() {
            var list = List.of("A", "B", "C");
            var product = cartesianProduct.simpleProductOf(list).lexOrder().stream().toList();
            assertEquals(list.size(), product.size());
            for (int i = 0; i < list.size(); i++) {
                assertEquals(List.of(list.get(i)), product.get(i));
            }
        }

        @Test
        @DisplayName("Last (rightmost) dimension varies fastest (lex order)")
        void rightmostDimensionVariesFastest() {
            var list = cartesianProduct.simpleProductOf(List.of(0, 1)).and(A_B)
                    .lexOrder().stream().toList();
            assertEquals(List.of(0, 'A'), list.get(0));
            assertEquals(List.of(0, 'B'), list.get(1));
            assertEquals(List.of(1, 'A'), list.get(2));
            assertEquals(List.of(1, 'B'), list.get(3));
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================
    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls produce equal results")
        void multipleStreamCallsAreEqual() {
            var p = cartesianProduct.simpleProductOf(A_B).and(num_1_2_3).lexOrder();
            var list1 = p.stream().toList();
            var list2 = p.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        @Disabled("TODO: currently not implemented as immutable. Should we make the inner lists immutable?")
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = cartesianProduct.simpleProductOf(A_B).and(num_1_2_3)
                    .lexOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.get(0).add("X"));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] 10×10×10 product produces 1000 tuples")
    void stressTesting() {
        var digits = List.of(0,1,2,3,4,5,6,7,8,9);
        var product = cartesianProduct.simpleProductOf(digits).and(digits).and(digits);
        assertEquals(1000L, product.lexOrder().stream().count());
        assertEquals(BigInteger.valueOf(1000), product.count());
    }
}