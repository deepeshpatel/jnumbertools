/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link UniqueCombination} (lexicographic order).
 */
@DisplayName("Unique Combinations (Lex Order)")
class UniqueCombinationTest {

    // =========================================================
    // 1. Count correctness
    // =========================================================

    @Nested
    @DisplayName("Count: C(n,r) combinations")
    class CountTests {

        @Test
        @DisplayName("C(n,r) count is correct for all n in [0,4], r in [0,n]")
        void nCrCountForSmallValues() {
            for (int n = 0; n <= 4; n++) {
                List<String> input = Collections.nCopies(n, "A");
                for (int r = 0; r <= n; r++) {
                    long count = combination.unique(r, input).lexOrder().stream().count();
                    assertEquals(calculator.nCr(n, r).longValue(), count,
                            "C(" + n + "," + r + ") mismatch");
                }
            }
        }

        @Test
        @DisplayName("n=0, r=0: C(0,0)=1, returns [[]]")
        void zeroChooseZero() {
            var builder = combination.unique(0, Collections.emptyList());
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, r>0: C(0,r)=0, returns []")
        void zeroChoosePositive() {
            var builder = combination.unique(1, Collections.emptyList());
            assertEquals(BigInteger.ZERO, builder.count());
            assertTrue(builder.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n>0, r=0: C(n,0)=1, returns [[]]")
        void positiveChooseZero() {
            var builder = combination.unique(0, "A", "B", "C");
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("r>n: C(n,r)=0, returns []")
        void rGreaterThanN() {
            var builder = combination.unique(2, "A");  // C(1,2)=0
            assertEquals(BigInteger.ZERO, builder.count());
            assertTrue(builder.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("r=n: C(n,n)=1, returns [all elements]")
        void rEqualsN() {
            var builder = combination.unique(3, "A", "B", "C");
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(of("A", "B", "C"), result.get(0));
        }

        @Test
        @DisplayName("count() matches stream count for various (n,r)")
        void builderCountMatchesStreamCount() {
            int[][] pairs = {{5,2},{6,3},{7,4},{8,1},{4,4}};
            for (int[] pair : pairs) {
                int n = pair[0], r = pair[1];
                List<Integer> input = Stream.iterate(0, i -> i+1).limit(n)
                        .collect(Collectors.toList());
                var builder = combination.unique(r, input);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "C(" + n + "," + r + "): count() vs stream mismatch");
            }
        }
    }

    // =========================================================
    // 2. Content correctness
    // =========================================================

    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All C(3,2)=3 combinations of [Red,Green,Blue] in lex order")
        void combinationsOf3Choose2() {
            var expected = List.of(
                    of("Red", "Green"),
                    of("Red", "Blue"),
                    of("Green", "Blue")
            );
            assertIterableEquals(expected, output(2, of("Red", "Green", "Blue")));
        }

        @Test
        @DisplayName("All C(4,2)=6 combinations of [1,2,3,4] in lex order")
        void combinationsOf4Choose2() {
            var expected = List.of(
                    of(1, 2), of(1, 3), of(1, 4),
                    of(2, 3), of(2, 4),
                    of(3, 4)
            );
            assertIterableEquals(expected, output(2, of(1, 2, 3, 4)));
        }

        @Test
        @DisplayName("Each combination has exactly r elements")
        void eachCombinationHasRElements() {
            int r = 3;
            var input = of("A", "B", "C", "D", "E");
            var combos = combination.unique(r, input).lexOrder().stream().toList();
            for (var combo : combos) {
                assertEquals(r, combo.size(), "each combination must have r=" + r + " elements");
            }
        }

        @Test
        @DisplayName("Each combination contains only elements from input, no repeats within")
        void eachCombinationHasUniqueElements() {
            var input = of("A", "B", "C", "D");
            var combos = combination.unique(2, input).lexOrder().stream().toList();
            for (var combo : combos) {
                assertEquals(new HashSet<>(combo).size(), combo.size(),
                        "combination must have no repeated elements: " + combo);
                assertTrue(new HashSet<>(input).containsAll(combo),
                        "all elements must come from input: " + combo);
            }
        }

        @Test
        @DisplayName("All combinations are distinct")
        void allCombinationsAreDistinct() {
            var combos = combination.unique(3, "A", "B", "C", "D", "E")
                    .lexOrder().stream().toList();
            assertEquals(new HashSet<>(combos).size(), combos.size(),
                    "all combinations must be distinct");
        }

        @Test
        @DisplayName("Combinations are in non-decreasing lexicographic order")
        void combinationsInLexOrder() {
            List<Integer> input = of(1, 2, 3, 4, 5);
            var combos = combination.unique(3, input).lexOrder().stream().toList();
            for (int i = 1; i < combos.size(); i++) {
                List<Integer> prev = (List<Integer>) combos.get(i - 1);
                List<Integer> curr = (List<Integer>) combos.get(i);
                assertTrue(isLexLessOrEqual(prev, curr),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }
    }

    // =========================================================
    // 3. Large input
    // =========================================================

    @Nested
    @DisplayName("Large inputs")
    class LargeInputs {

        @ParameterizedTest(name = "C(20,{0})")
        @ValueSource(ints = {1, 5, 10, 15, 19})
        @DisplayName("C(20,r) count is correct for varying r")
        void largeInputCountIsCorrect(int r) {
            List<Integer> input = Stream.iterate(1, i -> i + 1).limit(20)
                    .collect(Collectors.toList());
            long count = combination.unique(r, input).lexOrder().stream().count();
            assertEquals(calculator.nCr(20, r).longValue(), count,
                    "C(20," + r + ") count mismatch");
        }
    }

    // =========================================================
    // 4. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls on same iterable produce equal results")
        void multipleStreamCallsAreEqual() {
            var iterable = combination.unique(2, "A", "B", "C").lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] C(30,15) count matches formula")
    void stressTesting() {
        List<Integer> input = Stream.iterate(0, i -> i + 1).limit(30)
                .collect(Collectors.toList());
        long count = combination.unique(15, input).lexOrder().stream().count();
        assertEquals(calculator.nCr(30, 15).longValue(), count);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<?> output(int r, List<?> elements) {
        return combination.unique(r, elements).lexOrder().stream().toList();
    }

    private boolean isLexLessOrEqual(List<Integer> a, List<Integer> b) {
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            int cmp = Integer.compare(a.get(i), b.get(i));
            if (cmp < 0) return true;
            if (cmp > 0) return false;
        }
        return a.size() <= b.size();
    }
}