/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for k-permutations in combination order:
 * <pre>{@code permutation.nPk(k, input).combinationOrder()}</pre>
 * Generates all ordered arrangements of k elements, but ordered so that all
 * permutations of the same underlying combination are emitted contiguously,
 * with the combinations themselves traversed in lexicographic order.
 */
@DisplayName("K-Permutations (combination order)")
class KPermutationCombinationOrderTest {

    // =========================================================
    // 1. Count correctness: nPr(n, k)
    // =========================================================
    @Nested
    @DisplayName("Count: nPr(n, k)")
    class CountTests {

        @Test
        @DisplayName("Count equals nPr(n,k) for 1 ≤ k ≤ n ≤ 4")
        void countFormula() {
            for (int n = 1; n <= 4; n++) {
                var input = Collections.nCopies(n, "A");
                for (int k = 1; k <= n; k++) {
                    long size = permutation.nPk(k, input).combinationOrder().stream().count();
                    assertEquals(calculator.nPr(n, k).longValue(), size,
                            "nPr(" + n + "," + k + ") mismatch");
                }
            }
        }
    }

    // =========================================================
    // 2. Special cases
    // =========================================================
    @Nested
    @DisplayName("Edge cases (n,k)")
    class EdgeCases {

        @Test
        @DisplayName("n=0, k=0: returns single empty permutation")
        void nZeroKZero() {
            var b = permutation.nPk(0, Collections.emptyList());
            assertEquals(BigInteger.ONE, b.count());
            var result = b.combinationOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n=0, k>0: returns empty stream")
        void nZeroKPositive() {
            var b = permutation.nPk(2, Collections.emptyList());
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.combinationOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n>0, k=0: returns single empty permutation")
        void nPositiveKZero() {
            var b = permutation.nPk(0, of("A", "B", "C"));
            assertEquals(BigInteger.ONE, b.count());
            var result = b.combinationOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n>0, k>n: returns empty stream")
        void kGreaterThanN() {
            var b = permutation.nPk(4, of('A', 'B', 'C'));
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.combinationOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("Negative k throws IllegalArgumentException")
        void negativeKThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutation.nPk(-3, new ArrayList<>()).combinationOrder());
        }
    }

    // =========================================================
    // 3. Content correctness
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All 6 two-permutations of [1,2,3]")
        void twoPermutationsOfThree() {
            var expected = List.of(
                    of(1, 2),
                    of(2, 1),
                    of(1, 3),
                    of(3, 1),
                    of(2, 3),
                    of(3, 2)
            );
            var actual = permutation.nPk(2, 1, 2, 3).combinationOrder().stream().toList();
            assertIterableEquals(expected, actual);
        }

        @Test
        @DisplayName("All 6 two-permutations of [A,B,C]")
        void twoPermutationsOfThreeChars() {
            var expected = List.of(
                    of('A', 'B'),
                    of('B', 'A'),
                    of('A', 'C'),
                    of('C', 'A'),
                    of('B', 'C'),
                    of('C', 'B')
            );
            var output = permutation.nPk(2, 'A', 'B', 'C').combinationOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("Permutations of the same underlying combination are contiguous")
        void sameCombinationsAreContiguous() {
            int k = 3;
            var input = of("A","B","C","D","E");
            var output = permutation.nPk(k, input).combinationOrder().stream().toList();

            var seenCombinations = new HashSet<TreeSet<String>>();
            TreeSet<String> previousCombination = null;
            for (var perm : output) {
                @SuppressWarnings("unchecked")
                TreeSet<String> combo = new TreeSet<>((List<String>) perm);
                if (!combo.equals(previousCombination)) {
                    // boundary into a new combination — we must not have seen it before
                    assertFalse(seenCombinations.contains(combo),
                            "combination " + combo + " was split across non-contiguous blocks");
                    seenCombinations.add(combo);
                    previousCombination = combo;
                }
            }
        }

        @Test
        @DisplayName("Underlying combinations appear in lex order")
        void combinationsAppearInLexOrder() {
            int k = 3;
            var input = of("A","B","C","D","E");
            var output = permutation.nPk(k, input).combinationOrder().stream().toList();

            var combosInOrder = new ArrayList<List<String>>();
            List<String> lastCombo = null;
            for (var perm : output) {
                @SuppressWarnings("unchecked")
                List<String> sorted = ((List<String>) perm).stream().sorted().toList();
                if (!sorted.equals(lastCombo)) {
                    combosInOrder.add(sorted);
                    lastCombo = sorted;
                }
            }
            var lexSortedCombos = combosInOrder.stream().sorted(
                    (a, b) -> {
                        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
                            int c = a.get(i).compareTo(b.get(i));
                            if (c != 0) return c;
                        }
                        return Integer.compare(a.size(), b.size());
                    }).toList();
            assertEquals(lexSortedCombos, combosInOrder,
                    "underlying combinations must traverse in lex order");
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
            var iterable = permutation.nPk(2, "A", "B", "C").combinationOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        @DisplayName("Outer collection is immutable")
        void outerCollectionIsImmutable() {
            var results = permutation.nPk(2, "A", "B", "C").combinationOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        }

        @Test
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = permutation.nPk(2, "A", "B", "C").combinationOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.get(0).add("X"));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] count = nPr(n,k) for n in [0,8]")
    void stressTesting() {
        for (int n = 0; n <= 8; n++) {
            var input = Collections.nCopies(n, "X");
            for (int k = 0; k <= n; k++) {
                long count = permutation.nPk(k, input).combinationOrder().stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), count,
                        "n=" + n + " k=" + k);
            }
        }
    }
}