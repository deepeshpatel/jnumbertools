/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link UniquePermutationOfRanks}, covering mᵗʰ permutations, choice, and sampling strategies.
 */
class UniquePermutationOfRanksTest {

    @Nested
    public class UniquePermutationMthTest {

        @Test
        void assertCount() {
            for (int n = 0; n < 6; n++) {
                var input = Collections.nCopies(n, "A");
                for (int increment = 1; increment <= 4; increment++) {
                    long size = permutation
                            .unique(input)
                            .lexOrderMth(increment, 0)
                            .stream().count();
                    double expected = Math.ceil(calculator.nPr(n, n).longValue() / (double) increment);
                    assertEquals((long) expected, size);
                }
            }
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var iterable = permutation.unique("A", "B", "C").lexOrderMth(3, 0);
            var lists1 = iterable.stream().toList();
            var lists2 = iterable.stream().toList();
            assertIterableEquals(lists1, lists2);
        }

        @Test
        void shouldGenerateAllUniquePermutationsOf3Values() {
            var expected = List.of(
                    of(1, 2, 3),
                    of(2, 3, 1)
            );
            assertIterableEquals(expected, uniquePermutation(3, 0, 1, 2, 3));
        }

        @Test
        void shouldGenerateEmptyListForNullInput() {
            assertIterableEquals(listOfEmptyList, uniquePermutation(3, 0, (List<Object>) null));
        }

        @Test
        void shouldGenerateEmptyListForEmptyInput() {
            assertEquals(listOfEmptyList, uniquePermutation(2, 0, new ArrayList<>()));
        }

        @Test
        void shouldGenerateUniqueMthPermutations() {
            var expected = List.of(
                    of("Green", "Red", "Blue"),
                    of("Blue", "Red", "Green")
            );
            var actual = uniquePermutation(2, 2, "Red", "Green", "Blue");
            assertIterableEquals(expected, actual);
        }

        @Test
        void shouldSupportVeryLargePermutations() {
            var input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
            var expected = List.of(
                    of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                    of(2, 1, 0, 8, 10, 7, 9, 12, 4, 6, 11, 3, 5),
                    of(4, 2, 1, 3, 8, 5, 7, 11, 10, 6, 9, 0, 12),
                    of(6, 3, 1, 10, 2, 11, 0, 9, 4, 5, 7, 8, 12),
                    of(8, 4, 2, 3, 12, 5, 10, 7, 1, 9, 11, 0, 6),
                    of(10, 5, 2, 11, 7, 12, 4, 3, 8, 1, 6, 0, 9),
                    of(12, 6, 3, 5, 4, 8, 1, 7, 0, 2, 9, 10, 11)
            );
            var actual = permutation.unique(input)
                    .lexOrderMth(1_000_000_000, 0)
                    .stream().toList();
            assertIterableEquals(expected, actual);
        }

        @Test
        void shouldGenerateMthUniquePermutations() {
            int size = 6;
            int start = 3;
            for(int m=1; m<=32; m+=2) {
                var all = permutation.unique(size).lexOrder();
                var mth = permutation.unique(size).lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }

        @Test
        void shouldHandleSingleElement() {
            var expected = List.of(of("A"));
            assertIterableEquals(expected, uniquePermutation(1, 0, "A"));
        }

        @Test
        void shouldHandleIncrementLargerThanPermutations() {
            var input = of("A", "B");
            assertTrue(uniquePermutation(5, 5, input).isEmpty());
        }

        @Test
        void shouldThrowExceptionForNegativeIncrement() {
            assertThrows(IllegalArgumentException.class,
                    () -> uniquePermutation(-1, 0, "A", "B", "C"));
        }

        @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
        @Test
        void stressTesting() {
            int n = 10;
            for (int x = 1000; x < 5000; x += 500) {
                BigInteger nthOfFactorialX = calculator.factorial(x).divide(BigInteger.valueOf(n));
                long count = permutation.unique(x).lexOrderMth(nthOfFactorialX, BigInteger.ZERO).stream().count();
                assertEquals(n, count);
            }
        }

        private List<?> uniquePermutation(int increment, int start, Object... elements) {
            return permutation.unique(elements).lexOrderMth(increment, start).stream().toList();
        }

        private List<?> uniquePermutation(int increment, int start, List<?> elements) {
            return permutation.unique(elements).lexOrderMth(increment, start).stream().toList();
        }
    }

    @Nested
    public class UniquePermutationChoiceTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int sampleSize = 4;
            var permutations = permutation.unique("A", "B", "C")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate exactly sampleSize permutations");
        }

        @Test
        void shouldAllowDuplicatePermutations() {
            int sampleSize = 4; // > 3! = 6 for small input to ensure duplicates
            var permutations = permutation.unique("A", "B")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            int uniqueCount = new HashSet<>(permutations).size();
            assertTrue(uniqueCount <= calculator.nPr(2, 2).intValue(), "Unique permutations should not exceed n!");
        }

        @Test
        void shouldGenerateValidPermutations() {
            List<String> elements = of("A", "B", "C");
            int sampleSize = 3;
            var permutations = permutation.unique(elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            for (List<String> perm : permutations) {
                assertEquals(elements.size(), perm.size(), "Each permutation should have size n");
                assertEquals(new HashSet<>(perm).size() , elements.size(), "Permutation should be unique within itself");
                assertTrue(elements.containsAll(perm), "All elements should come from input");
            }
        }

        @Test
        void shouldHandleSampleSizeLargerThanTotalPermutations() {
            List<String> elements = of("A", "B");
            int sampleSize = 5; // > 2! = 2
            var permutations = permutation.unique(elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate sampleSize permutations even if > n!");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> permutation.unique("A", "B")
                    .choice(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInput() {
            var permutations = permutation.unique(Collections.emptyList())
                    .choice(1)
                    .stream()
                    .toList();
            assertEquals(1, permutations.size());
            assertEquals(of(), permutations.get(0), "Should generate one empty permutation");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var iterable = permutation.unique("A", "B", "C").choice(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertEquals(3, list1.size(), "First iterator should produce sampleSize permutations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize permutations");
            for (List<String> perm : list1) {
                assertEquals(3, perm.size(), "Each permutation should have size n");
                assertEquals(new HashSet<>(perm).size(), 3, "Permutations should be unique within themselves");
            }
        }
    }

    @Nested
    public class UniquePermutationSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int sampleSize = 3;
            var permutations = permutation.unique("A", "B", "C")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate exactly sampleSize permutations");
        }

        @Test
        void shouldGenerateUniquePermutations() {
            int sampleSize = 4;
            var permutations = permutation.unique("A", "B", "C", "D")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            assertEquals(sampleSize, new HashSet<>(permutations).size(), "All permutations should be unique");
        }

        @Test
        void shouldGenerateValidPermutations() {
            List<String> elements = of("A", "B", "C", "D");
            int sampleSize = 3;
            var permutations = permutation.unique(elements)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            for (List<String> perm : permutations) {
                assertEquals(elements.size(), perm.size(), "Each permutation should have size n");
                assertEquals(new HashSet<>(perm).size(), elements.size(), "Permutation should be unique within itself");
                assertTrue(elements.containsAll(perm), "All elements should come from input");
            }
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanTotalPermutations() {
            List<String> elements = of("A", "B");
            int total = calculator.nPr(2, 2).intValue(); // 2! = 2
            assertThrows(IllegalArgumentException.class, () -> permutation.unique(elements)
                    .sample(total + 1)
                    .stream()
                    .toList(), "Sample size exceeding n! should throw exception");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> permutation.unique("A", "B")
                    .sample(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInput() {
            var permutations = permutation.unique(Collections.emptyList())
                    .sample(1)
                    .stream()
                    .toList();
            assertEquals(1, permutations.size());
            assertEquals(of(), permutations.get(0), "Should generate one empty permutation");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var iterable = permutation.unique("A", "B", "C").sample(2);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertEquals(2, list1.size(), "First iterator should produce sampleSize permutations");
            assertEquals(2, list2.size(), "Second iterator should produce sampleSize permutations");
            assertEquals(2, new HashSet<>(list1).size(), "First sample should be unique");
            assertEquals(2, new HashSet<>(list2).size(), "Second sample should be unique");
            for (List<String> perm : list1) {
                assertEquals(3, perm.size(), "Each permutation should have size n");
                assertEquals(new HashSet<>(perm).size(), 3, "Permutations should be unique within themselves");
            }
        }
    }
}