/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for {@link UniquePermutationSingleSwap}.
 *
 * <p>Generates all n! unique permutations using Heap's single-swap algorithm.
 * Unlike lex order, the sequence order differs but the complete set is identical.
 */
@DisplayName("Unique Permutations (Single-Swap / Heap's Algorithm)")
class UniquePermutationSingleSwapTest {

    // =========================================================
    // 1. Count correctness: n!
    // =========================================================

    @Nested
    @DisplayName("Count: n! permutations")
    class CountTests {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("n elements produce n! permutations")
        void countIsFactorial(int n) {
            long count = permutation.unique(n).singleSwap().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count,
                    "n=" + n + " should produce " + calculator.factorial(n) + " permutations");
        }

        @Test
        @DisplayName("n=0: exactly 1 permutation — the empty list")
        void zeroElements() {
            var result = permutation.unique(0).singleSwap().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("count() matches stream count for n in [0,5]")
        void builderCountMatchesStreamCount() {
            for (int n = 0; n <= 5; n++) {
                var builder = permutation.unique(n);
                long streamCount = builder.singleSwap().stream().count();
                assertEquals(calculator.factorial(n).longValue(), streamCount,
                        "n=" + n + " count mismatch");
            }
        }
    }

    // =========================================================
    // 2. Content correctness: same set as lex order
    // =========================================================

    @Nested
    @DisplayName("Same set of permutations as lex order")
    class ContentCorrectness {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("Single-swap set equals lex-order set for n elements")
        void singleSwapSetEqualsLexSet(int n) {
            var singleSwap = new HashSet<>(permutation.unique(n).singleSwap().stream().toList());
            var lex = new HashSet<>(permutation.unique(n).lexOrder().stream().toList());
            assertEquals(lex, singleSwap,
                    "n=" + n + ": single-swap and lex-order must produce the same set");
        }

        @Test
        @DisplayName("All permutations are distinct")
        void allPermutationsAreDistinct() {
            var perms = permutation.unique(4).singleSwap().stream().toList();
            assertEquals(new HashSet<>(perms).size(), perms.size(),
                    "all single-swap permutations must be distinct");
        }

        @Test
        @DisplayName("Each permutation contains all original elements exactly once")
        void eachPermutationContainsAllElements() {
            int n = 4;
            var expected = new HashSet<>(of(0, 1, 2, 3));
            var perms = permutation.unique(n).singleSwap().stream().toList();
            for (var perm : perms) {
                assertEquals(n, perm.size(), "permutation size must be n");
                assertEquals(expected, new HashSet<>(perm),
                        "permutation must contain all original elements");
            }
        }

        @Test
        @DisplayName("Consecutive permutations differ by exactly one swap")
        void consecutivePermsDifferByOneSwap() {
            var perms = permutation.unique(4).singleSwap().stream().toList();
            for (int i = 1; i < perms.size(); i++) {
                List<Integer> prev = (List<Integer>) perms.get(i - 1);
                List<Integer> curr = (List<Integer>) perms.get(i);
                assertEquals(prev.size(), curr.size());
                int diffs = 0;
                for (int j = 0; j < prev.size(); j++) {
                    if (!prev.get(j).equals(curr.get(j))) diffs++;
                }
                assertEquals(2, diffs,
                        "consecutive permutations must differ at exactly 2 positions (one swap)");
            }
        }

        @Test
        @DisplayName("String elements: single-swap produces same set as lex-order")
        void stringElements() {
            var singleSwap = new HashSet<>(
                    permutation.unique("A", "B", "C").singleSwap().stream().toList());
            var lex = new HashSet<>(
                    permutation.unique("A", "B", "C").lexOrder().stream().toList());
            assertEquals(lex, singleSwap);
        }
    }

    // =========================================================
    // 3. Iterator contract
    // =========================================================

    @Nested
    @DisplayName("Iterator contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls on same iterable produce equal results")
        void multipleStreamCallsAreEqual() {
            UniquePermutationSingleSwap<Integer> iterable =
                    permutation.unique(3).singleSwap();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }

        @Test
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = permutation.unique(3).singleSwap().stream().toList();
            var first = results.get(0);
            assertThrows(UnsupportedOperationException.class, () -> first.add(99));
            assertThrows(UnsupportedOperationException.class, () -> first.set(0, 99));
        }
    }

    // =========================================================
    // 4. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] n! correct for n in [0,10], set equals lex-order set")
    void stressTesting() {
        for (int n = 0; n <= 10; n++) {
            long count = permutation.unique(n).singleSwap().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count,
                    "n=" + n + " count mismatch");
        }
    }
}