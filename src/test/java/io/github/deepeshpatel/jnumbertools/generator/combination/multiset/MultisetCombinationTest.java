/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@code combination.multiset(options, r).lexOrder()} — r-combinations
 * of a multiset (each element {@code eᵢ} has multiplicity {@code nᵢ}). The number
 * of r-combinations of size r is the coefficient of {@code xʳ} in
 * {@code Π_i (1 + x + … + x^{nᵢ})}.
 */
@DisplayName("Multiset Combinations (lex order)")
class MultisetCombinationTest {

    /**
     * The MultisetCombination implementation switches its internal iterator at this
     * cardinality. Kept private here purely for the implementation-aware switch test.
     */
    private static final int CROSSOVER_THRESHOLD = 1000;

    // =========================================================
    // 1. Count correctness
    // =========================================================
    @Nested
    @DisplayName("Count correctness")
    class CountTests {

        @Test
        @DisplayName("Count matches multisetCombinationsCountAll for each size 0..Σnᵢ")
        void countMatchesPolynomialCoefficients() {
            int[] frequencies = {5, 4, 7, 3};
            int[] expectedCounts = Calculator.multisetCombinationsCountAll(frequencies);
            int maxSize = Arrays.stream(frequencies).sum();

            LinkedHashMap<Character, Integer> options = new LinkedHashMap<>();
            for (int i = 0; i < frequencies.length; i++) {
                options.put(A_B_C_D.get(i), frequencies[i]);
            }
            for (int size = 0; size <= maxSize; size++) {
                long actualCount = combination.multiset(options, size).lexOrder()
                        .stream().count();
                // expectedCounts is symmetric — fall back if needed
                int index = size < expectedCounts.length ? size : maxSize - size;
                assertEquals(expectedCounts[index], actualCount,
                        "size=" + size);
            }
        }

        @Test
        @DisplayName("count() agrees with stream().count() for a representative case")
        void builderCountMatchesStreamCount() {
            var options = new LinkedHashMap<>(Map.of('A', 2, 'B', 3, 'C', 2));
            var builder = combination.multiset(options, 3);
            assertEquals(builder.count().longValue(),
                    builder.lexOrder().stream().count());
        }
    }

    // =========================================================
    // 2. Edge cases
    // =========================================================
    @Nested
    @DisplayName("Edge cases (options, r)")
    class EdgeCases {

        @Test
        @DisplayName("Empty multiset, r=0: single empty selection")
        void emptyMultisetRZero() {
            var b = combination.multiset(new LinkedHashMap<>(), 0);
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(Map.of(), result.get(0));
        }

        @Test
        @DisplayName("Empty multiset, r>0: empty stream")
        void emptyMultisetRPositive() {
            var b = combination.multiset(new LinkedHashMap<>(), 2);
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Non-empty multiset, r=0: single empty selection")
        void nonEmptyMultisetRZero() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var b = combination.multiset(options, 0);
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(Map.of(), result.get(0));
            // also via listOfEmptyMap helper
            assertIterableEquals(listOfEmptyMap, result);
        }

        @Test
        @DisplayName("r exceeds total available: empty stream")
        void rGreaterThanTotal() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var b = combination.multiset(options, 10);
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }
    }

    // =========================================================
    // 3. Content correctness
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("Selects from {C=3,B=2,A=1,D=1} of size 3 in declared order")
        void correctSizeThreeOnMixed() {
            String expected = "[{C=3}, {C=2, B=1}, {C=2, A=1}, {C=2, D=1}, {C=1, B=2}, {C=1, B=1, A=1},"
                    + " {C=1, B=1, D=1}, {C=1, A=1, D=1}, {B=2, A=1}, {B=2, D=1}, {B=1, A=1, D=1}]";
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("C", 3);
            options.put("B", 2);
            options.put("A", 1);
            options.put("D", 1);
            var out = combination.multiset(options, 3).lexOrder().stream().toList();
            assertEquals(expected, out.toString());
        }

        @Test
        @DisplayName("LinkedHashMap insertion order is preserved in the output")
        void respectsInsertionOrder() {
            var options = new LinkedHashMap<String, Integer>();
            options.put("B", 3);
            options.put("A", 4);
            options.put("C", 2);
            String expected = "[{B=3}, {B=2, A=1}, {B=2, C=1}, {B=1, A=2}, {B=1, A=1, C=1}, "
                    + "{B=1, C=2}, {A=3}, {A=2, C=1}, {A=1, C=2}]";
            var output = combination.multiset(options, 3).lexOrder().stream().toList();
            assertEquals(expected, output.toString());
        }
    }

    // =========================================================
    // 4. Iterator contract — including implementation-specific switching
    // =========================================================
    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls produce equal results")
        void multipleStreamCallsAreEqual() {
            var options = new LinkedHashMap<>(Map.of('A', 2, 'B', 3, 'C', 2));
            var mc = combination.multiset(options, 2).lexOrder();
            var list1 = mc.stream().toList();
            var list2 = mc.stream().toList();
            assertIterableEquals(list1, list2);
        }

        /**
         * Whitebox: the two internal iterators (ArrayIterator and FreqVectorIterator)
         * must produce identical output for the same input. This is exercised below
         * the crossover threshold and above it.
         */
        @Test
        @DisplayName("ArrayIterator and FreqVectorIterator produce identical output")
        void iteratorImplementationsAgree() {
            var input = new LinkedHashMap<String, Integer>();
            input.put("Banana", 400);
            input.put("Apple", 1000);
            input.put("Mango", 3);

            int[] rValues = {10, 500};
            for (int r : rValues) {
                MultisetCombination<String> mc = combination.multiset(input, r).lexOrder();
                Iterator<Map<String, Integer>> arrayIterator = mc.new ArrayIterator();
                Iterator<Map<String, Integer>> freqIterator  = mc.new FreqVectorIterator();
                while (arrayIterator.hasNext() && freqIterator.hasNext()) {
                    assertEquals(arrayIterator.next(), freqIterator.next(),
                            "mismatch at r=" + r);
                }
                assertFalse(arrayIterator.hasNext() || freqIterator.hasNext(),
                        "size mismatch for r=" + r);
            }
        }

        @Test
        @DisplayName("Iterator implementation chosen by crossover threshold")
        void iteratorSwitchesAtThreshold() {
            var options = new LinkedHashMap<>(Map.of("A", 500, "B", 900));
            int totalAvailable = 1400;
            int[] rValues = {900, 999, 1000, 1500};

            for (int r : rValues) {
                var iterator = combination.multiset(options, r).lexOrder().iterator();
                if (r > totalAvailable) {
                    assertFalse(iterator.hasNext(), "r=" + r + " should be empty");
                } else if (r < CROSSOVER_THRESHOLD) {
                    assertTrue(iterator instanceof MultisetCombination.ArrayIterator,
                            "expected ArrayIterator for r=" + r);
                } else {
                    assertTrue(iterator instanceof MultisetCombination.FreqVectorIterator,
                            "expected FreqVectorIterator for r=" + r);
                }
            }
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] counts match multisetCombinationsCountAll for freq=[3,3,3,3,3]")
    void stressTesting() {
        int[] frequencies = {3, 3, 3, 3, 3};
        int[] expected = Calculator.multisetCombinationsCountAll(frequencies);
        int maxSize = Arrays.stream(frequencies).sum();
        var options = new LinkedHashMap<Character, Integer>();
        for (int i = 0; i < frequencies.length; i++) {
            options.put(A_B_C_D.get(i % A_B_C_D.size()), frequencies[i]);
        }
        // collisions in keys — rewrite with distinct keys
        options.clear();
        options.put('A', 3); options.put('B', 3); options.put('C', 3); options.put('D', 3); options.put('E', 3);
        for (int size = 0; size <= maxSize; size++) {
            long actual = combination.multiset(options, size).lexOrder().stream().count();
            int idx = size < expected.length ? size : maxSize - size;
            assertEquals(expected[idx], actual, "size=" + size);
        }
    }
}