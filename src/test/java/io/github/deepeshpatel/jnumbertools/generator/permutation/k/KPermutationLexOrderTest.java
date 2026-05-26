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
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for k-permutations in lexicographic order:
 * <pre>{@code permutation.nPk(k, input).lexOrder()}</pre>
 * Generates all ordered arrangements of k elements chosen from n, count = nPr(n, k).
 */
@DisplayName("K-Permutations (lex order)")
class KPermutationLexOrderTest {

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
                    long size = permutation.nPk(k, input).lexOrder().stream().count();
                    assertEquals(calculator.nPr(n, k).longValue(), size,
                            "nPr(" + n + "," + k + ") mismatch");
                }
            }
        }

        @Test
        @DisplayName("count() matches stream().count() for various (n,k)")
        void builderCountMatchesStreamCount() {
            int[][] pairs = {{5,2},{5,5},{4,3},{6,2}};
            for (int[] p : pairs) {
                int n = p[0], k = p[1];
                var input = Collections.nCopies(n, "X");
                var builder = permutation.nPk(k, input);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "n=" + n + " k=" + k);
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
        @DisplayName("n=0, k>0: ⁰Pₖ = 0, empty stream")
        void nZeroKPositive() {
            var b = permutation.nPk(0, 1);   // (n, k) = (0, 1)
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n>0, k>n: ⁿPₖ = 0, empty stream")
        void kGreaterThanN() {
            var b = permutation.nPk(1, 2);   // (n, k) = (1, 2)
            assertEquals(BigInteger.ZERO, b.count());
            assertTrue(b.lexOrder().stream().toList().isEmpty());
        }

        @Test
        @DisplayName("n=0, k=0: ⁰P₀ = 1, single empty permutation")
        void nZeroKZero() {
            var b = permutation.nPk(0, 0);   // (n, k) = (0, 0)
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("n>0, k=0: ⁿP₀ = 1, single empty permutation")
        void nPositiveKZero() {
            var b = permutation.nPk(2, 0);   // (n, k) = (2, 0)
            assertEquals(BigInteger.ONE, b.count());
            var result = b.lexOrder().stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }

        @Test
        @DisplayName("Empty input list with k=0: single empty permutation")
        void emptyInputWithKZero() {
            var result = permutation.nPk(0, Collections.emptyList()).lexOrder().stream().toList();
            assertIterableEquals(listOfEmptyList, result);
        }

        @Test
        @DisplayName("Negative k throws IllegalArgumentException")
        void negativeKThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    permutation.nPk(-1, of("A", "B")).lexOrder());
        }
    }

    // =========================================================
    // 3. Content correctness
    // =========================================================
    @Nested
    @DisplayName("Content correctness")
    class ContentCorrectness {

        @Test
        @DisplayName("All 6 two-permutations of [A,B,C]")
        void twoPermutationsOfThree() {
            var expected = List.of(
                    of('A', 'B'),
                    of('A', 'C'),
                    of('B', 'A'),
                    of('B', 'C'),
                    of('C', 'A'),
                    of('C', 'B')
            );
            var output = permutation.nPk(2, 'A', 'B', 'C').lexOrder().stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        @DisplayName("k=n: nPk equals full unique permutation")
        void kEqualsNMatchesUnique() {
            var unique = permutation.unique('A', 'B', 'C').lexOrder().stream().toList();
            var kPerm  = permutation.nPk(3, 'A', 'B', 'C').lexOrder().stream().toList();
            assertIterableEquals(unique, kPerm);
        }

        @Test
        @DisplayName("Each output is in lex order and has exactly k elements")
        void allOutputsLexOrderedAndSizeK() {
            int k = 3;
            var input = of("A","B","C","D","E","F");
            var output = permutation.nPk(k, input).lexOrder().stream().toList();
            for (var p : output) {
                assertEquals(k, p.size(), "each permutation must be of size k");
            }
            for (int i = 1; i < output.size(); i++) {
                assertTrue(isLexLessOrEqual(output.get(i - 1), output.get(i)),
                        "rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }

        @Test
        @DisplayName("lex output is a sorted view of combinationOrder output")
        void lexOrderEqualsSortedCombinationOrder() {
            int k = 4;
            var input = of("A","B","C","D","E","F","G","H");
            var lex = permutation.nPk(k, input).lexOrder().stream()
                    .map(Object::toString).toList();
            var comboSorted = permutation.nPk(k, input).combinationOrder().stream()
                    .map(Object::toString).sorted().toList();
            assertEquals(comboSorted, lex);
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
            var iterable = permutation.nPk(2, "A", "B", "C").lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }

        @Test
        @DisplayName("Outer collection is immutable")
        void outerCollectionIsImmutable() {
            var results = permutation.nPk(2, "A", "B", "C").lexOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X", "Y")));
            assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
        }

        @Test
        @DisplayName("Inner lists are immutable")
        void innerListsAreImmutable() {
            var results = permutation.nPk(2, "A", "B", "C").lexOrder().stream().toList();
            var first = results.get(0);
            assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
            assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
        }
    }

    // =========================================================
    // 5. Stress test (opt-in)
    // =========================================================
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] count = nPr(n,k) for n in [0,8], k in [0,n]")
    void stressTesting() {
        for (int n = 0; n <= 8; n++) {
            var input = Collections.nCopies(n, "X");
            for (int k = 0; k <= n; k++) {
                long count = permutation.nPk(k, input).lexOrder().stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), count,
                        "n=" + n + " k=" + k);
            }
        }
    }

    // ---------- helpers ----------

    @SuppressWarnings("unchecked")
    private static boolean isLexLessOrEqual(List<?> a, List<?> b) {
        List<Comparable> ca = (List<Comparable>) a;
        List<Comparable> cb = (List<Comparable>) b;
        for (int i = 0; i < Math.min(ca.size(), cb.size()); i++) {
            int cmp = ca.get(i).compareTo(cb.get(i));
            if (cmp < 0) return true;
            if (cmp > 0) return false;
        }
        return ca.size() <= cb.size();
    }
}