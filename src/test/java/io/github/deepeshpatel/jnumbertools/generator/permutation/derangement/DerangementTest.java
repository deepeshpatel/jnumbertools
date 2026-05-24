/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.*;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link Derangement} (lexicographic order).
 *
 * <p>A derangement of n elements is a permutation where no element appears
 * in its original position. The count of derangements of n elements is !n
 * (subfactorial n). Known values: !0=1, !1=0, !2=1, !3=2, !4=9, !5=44, !6=265.
 */
@DisplayName("Derangements (Lex Order)")
class DerangementTest {

    // =========================================================
    // 1. Count correctness
    // =========================================================

    @Nested
    @DisplayName("Count: subfactorial !n")
    class CountTests {

        @Test
        @DisplayName("Known derangement counts !0 through !6")
        void knownSubfactorialCounts() {
            long[] expected = {1, 0, 1, 2, 9, 44, 265};
            for (int n = 0; n < expected.length; n++) {
                long size = derangement.of(n).lexOrder().stream().count();
                assertEquals(expected[n], size, "Wrong count for n=" + n);
            }
        }

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
        @DisplayName("count() matches calculator.subFactorial(n)")
        void builderCountMatchesSubfactorial(int n) {
            var builder = derangement.of(n);
            assertEquals(calculator.subFactorial(n), builder.count(),
                    "builder.count() mismatch for n=" + n);
        }

        @Test
        @DisplayName("count() matches stream count for n in [0,6]")
        void builderCountMatchesStreamCount() {
            for (int n = 0; n <= 6; n++) {
                var builder = derangement.of(n);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "n=" + n + ": count() vs stream mismatch");
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
        @DisplayName("n=0: exactly one derangement, the empty list")
        void n0ProducesOneEmptyDerangement() {
            var result = derangement.of(0).lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=1: no derangement exists (stream is empty)")
        void n1ProducesNoDerangements() {
            var result = derangement.of(List.of("A")).lexOrder().stream().toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("n=2: only derangement is a full swap [B,A]")
        void n2IsFullSwap() {
            var result = derangement.of("A", "B").lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertEquals(of("B", "A"), result.get(0));
        }

        @Test
        @DisplayName("All 2 derangements of [A,B,C] in lex order are [B,C,A] and [C,A,B]")
        void allDerangementsOf3InLexOrder() {
            var expected = List.of(of("B", "C", "A"), of("C", "A", "B"));
            var actual = derangement.of("A", "B", "C").lexOrder().stream().toList();
            assertIterableEquals(expected, actual);
        }

        @Test
        @DisplayName("All 9 derangements of n=4: no fixed points, all distinct, correct multiset")
        void allDerangementsOf4AreValid() {
            var actual = derangement.of("A", "B", "C", "D").lexOrder().stream().toList();
            assertEquals(9, actual.size());
            assertEquals(9, new HashSet<>(actual).size(), "all derangements must be distinct");

            List<String> origin = List.of("A", "B", "C", "D");
            for (var d : actual) {
                assertEquals(4, d.size(), "each derangement has n=4 elements");
                // No fixed point
                for (int i = 0; i < d.size(); i++) {
                    assertNotEquals(origin.get(i), d.get(i),
                            "fixed point at index " + i + " in " + d);
                }
                // Same multiset as input
                var sorted = new ArrayList<>(d);
                Collections.sort(sorted);
                assertEquals(List.of("A", "B", "C", "D"), sorted,
                        "derangement must contain all original elements: " + d);
            }
        }

        @Test
        @DisplayName("All derangements of [A,B,C,D,E]: count=44, all valid")
        void allDerangementsOf5AreValid() {
            var actual = derangement.of("A", "B", "C", "D", "E").lexOrder().stream().toList();
            assertEquals(44, actual.size());
            assertEquals(44, new HashSet<>(actual).size());

            List<String> origin = List.of("A", "B", "C", "D", "E");
            for (var d : actual) {
                for (int i = 0; i < d.size(); i++) {
                    assertNotEquals(origin.get(i), d.get(i),
                            "fixed point at index " + i);
                }
            }
        }

        @Test
        @DisplayName("Derangements are in non-decreasing lexicographic order")
        void derangementsInLexOrder() {
            var actual = derangement.of(5).lexOrder().stream().toList();
            for (int i = 1; i < actual.size(); i++) {
                List<Integer> prev = (List<Integer>) actual.get(i - 1);
                List<Integer> curr = (List<Integer>) actual.get(i);
                assertTrue(isLexLessOrEqual(prev, curr),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }

        @Test
        @DisplayName("Mixed-type elements: derangement of [1, A] is [[A, 1]]")
        void mixedTypeElements() {
            var expected = List.of(of("A", 1));
            var actual = derangement.of(1, "A").lexOrder().stream().toList();
            assertIterableEquals(expected, actual);
        }
    }

    // =========================================================
    // 3. Consistency with unrankOf
    // =========================================================

    @Nested
    @DisplayName("Consistency with unrankOf.derangement")
    class ConsistencyWithUnrank {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {3, 4, 5})
        @DisplayName("k-th lexOrder element equals unrankOf.derangement(k, n)")
        void lexOrderMatchesUnrank(int n) {
            var lexList = derangement.of(n).lexOrder().stream().toList();
            assertEquals(calculator.subFactorial(n).intValue(), lexList.size());

            for (int rank = 0; rank < lexList.size(); rank++) {
                int[] viaUnrank = unrankOf.derangement(rank, n);
                var expected = new ArrayList<Integer>(n);
                for (int v : viaUnrank) expected.add(v);
                assertEquals(expected, lexList.get(rank),
                        "Mismatch at rank=" + rank + " n=" + n);
            }
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
            Derangement<String> iterable = derangement.of("A", "B", "C", "D").lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }

        @Test
        @DisplayName("Outer collection from stream().toList() is immutable")
        void outerCollectionIsImmutable() {
            var results = derangement.of("A", "B", "C", "D").lexOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.add(of("X")));
            assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] !n counts correct for n in [0,9]")
    void stressTesting() {
        for (int n = 0; n <= 9; n++) {
            long count = derangement.of(n).lexOrder().stream().count();
            assertEquals(calculator.subFactorial(n).longValue(), count,
                    "n=" + n + " count mismatch");
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private boolean isLexLessOrEqual(List<Integer> a, List<Integer> b) {
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            int cmp = Integer.compare(a.get(i), b.get(i));
            if (cmp < 0) return true;
            if (cmp > 0) return false;
        }
        return a.size() <= b.size();
    }
}