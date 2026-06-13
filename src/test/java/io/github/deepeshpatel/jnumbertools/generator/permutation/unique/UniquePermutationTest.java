/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for unique (full) permutation generation in lexicographic order.
 */
@DisplayName("Unique Permutations (Lex Order)")
class UniquePermutationTest {

    // =========================================================
    // 1. Count correctness
    // =========================================================

    @Nested
    @DisplayName("Count: n! permutations")
    class CountTests {

        @ParameterizedTest(name = "n={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("n elements produce n! permutations")
        void countIsFactorial(int n) {
            var input = Collections.nCopies(n, "A");
            long size = permutation.unique(input).lexOrder().stream().count();
            assertEquals(calculator.nPr(n, n).longValue(), size,
                    "n=" + n + " should produce " + calculator.nPr(n, n) + " permutations");
        }

        @Test
        @DisplayName("n=0 produces exactly 1 permutation: the empty list")
        void zeroElementsProducesOneEmptyPermutation() {
            var builder = permutation.unique(0);
            assertEquals(BigInteger.ONE, builder.count());
            var results = builder.lexOrder().stream().toList();
            assertEquals(1, results.size());
            assertTrue(results.get(0).isEmpty());
        }

        @Test
        @DisplayName("count() matches stream count")
        void builderCountMatchesStreamCount() {
            for (int n = 0; n <= 5; n++) {
                var builder = permutation.unique(n);
                long streamCount = builder.lexOrder().stream().count();
                assertEquals(builder.count().longValue(), streamCount,
                        "n=" + n + ": builder.count() must match stream count");
            }
        }
    }

    // =========================================================
    // 2. Correctness of generated permutations
    // =========================================================

    @Nested
    @DisplayName("Correctness of generated values")
    class ContentCorrectness {

        @Test
        @DisplayName("All 6 permutations of [1,2,3] in lex order")
        void allPermutationsOf3Integers() {
            var expected = List.of(
                    of(1, 2, 3), of(1, 3, 2),
                    of(2, 1, 3), of(2, 3, 1),
                    of(3, 1, 2), of(3, 2, 1)
            );
            assertIterableEquals(expected, permutationsOf(1, 2, 3));
        }

        @Test
        @DisplayName("All 6 permutations of strings in lex order")
        void allPermutationsOfStrings() {
            var expected = List.of(
                    of("Red", "Green", "Blue"), of("Red", "Blue", "Green"),
                    of("Green", "Red", "Blue"), of("Green", "Blue", "Red"),
                    of("Blue", "Red", "Green"), of("Blue", "Green", "Red")
            );
            assertIterableEquals(expected, permutationsOf("Red", "Green", "Blue"));
        }

        @Test
        @DisplayName("Single element produces [[A]]")
        void singleElement() {
            assertIterableEquals(List.of(of("A")), permutationsOf("A"));
        }

        @Test
        @DisplayName("Two elements produce [[A,B],[B,A]]")
        void twoElements() {
            assertIterableEquals(List.of(of("A", "B"), of("B", "A")),
                    permutationsOf("A", "B"));
        }

        @Test
        @DisplayName("Empty input produces [[]] (the empty permutation)")
        void emptyInputProducesOneEmptyResult() {
            assertIterableEquals(listOfEmptyList, permutationsOf(new ArrayList<>()));
        }

        @Test
        @DisplayName("Mixed-type elements are supported")
        void mixedTypes() {
            var expected = List.of(of(1, "A"), of("A", 1));
            assertIterableEquals(expected, permutationsOf(1, "A"));
        }

        @Test
        @DisplayName("Each permutation contains all original elements exactly once")
        void eachPermutationContainsAllElements() {
            List<Integer> input = of(1, 2, 3, 4);
            var perms = permutation.unique(input).lexOrder().stream().toList();
            for (var perm : perms) {
                assertEquals(input.size(), perm.size());
                assertEquals(new HashSet<>(input), new HashSet<>(perm),
                        "permutation must contain all original elements");
            }
        }

        @Test
        @DisplayName("All generated permutations are distinct")
        void allPermutationsAreDistinct() {
            var perms = permutation.unique("A", "B", "C", "D").lexOrder().stream().toList();
            assertEquals(new HashSet<>(perms).size(), perms.size(),
                    "all permutations must be distinct");
        }

        @Test
        @DisplayName("Permutations are in non-decreasing lexicographic order")
        void permutationsInLexOrder() {
            var perms = permutation.unique(1, 2, 3, 4).lexOrder().stream().toList();
            for (int i = 1; i < perms.size(); i++) {
                assertTrue(isLexLessOrEqual(perms.get(i - 1), perms.get(i)),
                        "permutation at rank " + (i-1) + " must be lex ≤ rank " + i);
            }
        }
    }

    // =========================================================
    // 3. Iterator / stream contract
    // =========================================================

    @Nested
    @DisplayName("Iterator and stream contract")
    class IteratorContract {

        @Test
        @DisplayName("Multiple stream() calls on the same iterable produce equal results")
        void multipleStreamCallsAreEqual() {
            UniquePermutation<String> iterable = permutation.unique("A", "B", "C").lexOrder();
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
            assertNotSame(list1, list2);
        }

        @Test
        @DisplayName("Outer collection returned by stream().toList() is immutable")
        void outerCollectionIsImmutable() {
            var results = permutation.unique("A", "B").lexOrder().stream().toList();
            assertThrows(UnsupportedOperationException.class, () -> results.add(of("X")));
            assertThrows(UnsupportedOperationException.class, () -> results.remove(0));
        }

        @Test
        @DisplayName("Inner lists returned are immutable")
        void innerListsAreImmutable() {
            var results = permutation.unique("A", "B").lexOrder().stream().toList();
            var first = results.get(0);
            assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
            assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
        }

        @Test
        @DisplayName("Immutable input list is handled correctly")
        void immutableInputList() {
            var input = of("A", "B");
            var expected = List.of(of("A", "B"), of("B", "A"));
            assertIterableEquals(expected, permutationsOf(input));
        }
    }

    // =========================================================
    // 4. Stress test (opt-in)
    // =========================================================

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    @DisplayName("[STRESS] All n! permutations counted correctly for n = 0..10")
    void stressTesting() {
        for (int n = 0; n <= 10; n++) {
            long count = permutation.unique(n).lexOrder().stream().count();
            assertEquals(calculator.factorial(n).longValue(), count,
                    "n=" + n + " count mismatch");
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private List<List<Object>> permutationsOf(Object... elements) {
        return permutation.unique(elements).lexOrder().stream().toList();
    }

    @SuppressWarnings("unchecked")
    private List<?> permutationsOf(List<?> elements) {
        return permutation.unique(elements).lexOrder().stream().toList();
    }
}