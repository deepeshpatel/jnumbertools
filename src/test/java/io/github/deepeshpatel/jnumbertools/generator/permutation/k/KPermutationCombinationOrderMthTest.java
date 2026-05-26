/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for k-permutations skipping by m in combination order:
 * <pre>{@code permutation.nPk(k, input).combinationOrderMth(m, start)}</pre>
 * Returns every m-th element of the {@code combinationOrder()} sequence, starting
 * at index {@code start}.
 */
@DisplayName("K-Permutations (combinationOrderMth)")
class KPermutationCombinationOrderMthTest {

    // =========================================================
    // 1. Count correctness: ceil(nPr(n,k) / m)
    // =========================================================
    @Nested
    @DisplayName("Count: ⌈nPr(n,k) / m⌉")
    class CountTests {

        @Test
        @DisplayName("count of mth stream = ⌈nPr(n,k)/m⌉ for n ≤ 4, increment=2")
        void mthCountMatchesCeilDivision() {
            int increment = 2;
            for (int n = 0; n <= 4; n++) {
                var input = Collections.nCopies(n, 'A');
                for (int k = 0; k <= n; k++) {
                    long size = permutation.nPk(k, input)
                            .combinationOrderMth(increment, 0)
                            .stream().count();
                    long expected = (long) Math.ceil(calculator.nPr(n, k).longValue() / (double) increment);
                    assertEquals(expected, size,
                            "n=" + n + " k=" + k + " m=" + increment);
                }
            }
        }
    }

    // =========================================================
    // 2. Edge cases
    // =========================================================
    @Nested
    @DisplayName("Edge cases (n,k,m)")
    class EdgeCases {

        @Test
        @DisplayName("n=0, k=0: ⁰P₀ = 1, returns [[]]")
        void nZeroKZero() {
            var result = permutation.nPk(0, 0).combinationOrderMth(1, 0).stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, k>0: ⁰Pₖ = 0, empty stream")
        void nZeroKPositive() {
            assertTrue(permutation.nPk(0, 2).combinationOrderMth(1, 0).stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n>0, k=0: ⁿP₀ = 1, returns [[]]")
        void nPositiveKZero() {
            var result = permutation.nPk(3, 0).combinationOrderMth(1, 0).stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n>0, k>n: ⁿPₖ = 0, empty stream regardless of m")
        void kGreaterThanN() {
            assertTrue(permutation.nPk(2, 3).combinationOrderMth(1, 0).stream().toList().isEmpty());
            assertTrue(permutation.nPk(2, 3).combinationOrderMth(3, 0).stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Empty list with k>0: empty stream")
        void emptyInputKPositive() {
            assertTrue(permutation.nPk(2, Collections.emptyList())
                    .combinationOrderMth(1, 0).stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Empty list with k=0: single empty permutation")
        void emptyInputKZero() {
            var output = permutation.nPk(0, Collections.emptyList())
                    .combinationOrderMth(1, 0).stream().toList();
            assertIterableEquals(listOfEmptyList, output);
        }

        @Test
        @DisplayName("Negative k throws IllegalArgumentException")
        void negativeKThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutation.nPk(1, -1).combinationOrderMth(3, 0));   // (n, k) = (1, -1)
        }

        @Test
        @DisplayName("Negative n throws IllegalArgumentException")
        void negativeNThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutation.nPk(-1, 1).combinationOrderMth(3, 0));   // (n, k) = (-1, 1)
        }

        @Test
        @DisplayName("Non-positive m throws (m must be > 0)")
        void nonPositiveMThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutation.nPk(2, A_B_C).combinationOrderMth(0, 0));
        }
    }

    // =========================================================
    // 3. Content correctness — derived from combinationOrder, not lexOrder
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        /**
         * The mth stream of combinationOrder must equal every m-th element of
         * the full combinationOrder sequence. Previously this test was bugged:
         * it built {@code lexOrder} / {@code lexOrderMth} streams, so it never
         * exercised combinationOrderMth at all.
         */
        @ParameterizedTest(name = "k={0}, m={1}, start={2}")
        @org.junit.jupiter.params.provider.CsvSource({
                "1, 1, 0",
                "1, 2, 0",
                "1, 3, 1",
                "2, 1, 0",
                "2, 2, 0",
                "2, 3, 2",
                "3, 5, 0",
                "3, 5, 7"
        })
        @DisplayName("mth output equals every-m-th of combinationOrder")
        void mthMatchesEveryMthOfCombinationOrder(int k, int m, int start) {
            var all = permutation.nPk(k, A_B_C_D).combinationOrder().stream();
            var mth = permutation.nPk(k, A_B_C_D).combinationOrderMth(m, start).stream();
            assertEveryMthValue(all, mth, start, m);
        }

        @Test
        @DisplayName("start>0 yields the documented subsequence on a 5-element input")
        void startParameterPicksTheRightSubsequence() {
            // every 20th combinationOrder element of nPk(3, [a,b,c,d,e]), starting at index 5
            var expected = List.of(
                    of('c', 'b', 'a'),
                    of('a', 'e', 'c'),
                    of('c', 'e', 'b')
            );
            var output = permutation.nPk(3, 'a', 'b', 'c', 'd', 'e')
                    .combinationOrderMth(20, 5).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("k=n, m=1: full nPk count in combinationOrder")
        void kEqualsNFullStream() {
            var output = permutation.nPk(3, A_B_C).combinationOrderMth(1, 0).stream().toList();
            assertEquals(calculator.nPr(3, 3).longValue(), output.size());
            // also verify the exact sequence
            assertEquals(
                    "[[A, B, C], [A, C, B], [B, A, C], [B, C, A], [C, A, B], [C, B, A]]",
                    output.toString());
        }

        @Test
        @DisplayName("Large input: m=1 produces full nPr(10,5) = 30240 permutations")
        void largeInputM1() {
            var input = of('A','B','C','D','E','F','G','H','I','J');
            long count = permutation.nPk(5, input).combinationOrderMth(1, 0).stream().count();
            assertEquals(30240L, count);
            assertEquals(calculator.nPr(10, 5).longValue(), count);
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
            var iterable = permutation.nPk(2, A_B_C).combinationOrderMth(2, 0);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = permutation.nPk(2, A_B_C).combinationOrderMth(1, 0).stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.get(0).add('Z'));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @ParameterizedTest(name = "m={0}")
    @ValueSource(ints = {1, 2, 3, 5, 7})
    @DisplayName("[STRESS] mth output matches every-m-th of combinationOrder, n=6")
    void stressTesting(int m) {
        int n = 6;
        var input = of('A','B','C','D','E','F');
        for (int k = 0; k <= n; k++) {
            var all = permutation.nPk(k, input).combinationOrder().stream();
            var mth = permutation.nPk(k, input).combinationOrderMth(m, 0).stream();
            assertEveryMthValue(all, mth, 0, m);
        }
    }

}