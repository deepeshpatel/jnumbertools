/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link RepetitivePermutationByRanks}, covering choice and sampling strategies.
 */
class RepetitivePermutationByRanksTest {

    @Nested
    public class RepetitivePermutationChoiceTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int width = 3;
            int sampleSize = 4;
            var permutations = permutation.repetitive(width, "A", "B")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate exactly sampleSize permutations");
        }

        @Test
        void shouldAllowDuplicatePermutations() {
            int width = 2;
            int sampleSize = 5; // > 2^2 = 4 to ensure possible duplicates
            var permutations = permutation.repetitive(width, "A", "B")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            int uniqueCount = new HashSet<>(permutations).size();
            assertTrue(uniqueCount <= calculator.power(2, width).intValue(),
                    "Unique permutations should not exceed n^width");
        }

        @Test
        void shouldGenerateValidPermutations() {
            List<String> elements = of("A", "B", "C");
            int width = 2;
            int sampleSize = 3;
            var permutations = permutation.repetitive(width, elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            for (List<String> perm : permutations) {
                assertEquals(width, perm.size(), "Each permutation should have size width");
                assertTrue(elements.containsAll(perm), "All elements should come from input");
            }
        }

        @Test
        void shouldHandleSampleSizeLargerThanTotalPermutations() {
            List<String> elements = of("A", "B");
            int width = 2;
            int sampleSize = 5; // > 2^2 = 4
            var permutations = permutation.repetitive(width, elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate sampleSize permutations even if > n^width");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, "A", "B")
                    .choice(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInput() {
            var permutations = permutation.repetitive(0, Collections.emptyList())
                    .choice(1)
                    .stream()
                    .toList();
            assertEquals(1, permutations.size());
            assertEquals(of(), permutations.get(0), "Should generate one empty permutation for width=0");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var iterable = permutation.repetitive(2, "A", "B").choice(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertEquals(3, list1.size(), "First iterator should produce sampleSize permutations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize permutations");
            for (List<String> perm : list1) {
                assertEquals(2, perm.size(), "Each permutation should have size width");
                assertTrue(of("A", "B").containsAll(perm), "Elements should be from input");
            }
        }
    }

    @Nested
    public class RepetitivePermutationSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int width = 3;
            int sampleSize = 4;
            var permutations = permutation.repetitive(width, "A", "B")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate exactly sampleSize permutations");
        }

        @Test
        void shouldGenerateUniquePermutations() {
            int width = 2;
            int sampleSize = 3; // < 2^2 = 4 to ensure uniqueness possible
            var permutations = permutation.repetitive(width, "A", "B")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            assertEquals(sampleSize, new HashSet<>(permutations).size(), "All permutations should be unique");
        }

        @Test
        void shouldGenerateValidPermutations() {
            List<String> elements = of("A", "B", "C");
            int width = 2;
            int sampleSize = 3;
            var permutations = permutation.repetitive(width, elements)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size());
            for (List<String> perm : permutations) {
                assertEquals(width, perm.size(), "Each permutation should have size width");
                assertTrue(elements.containsAll(perm), "All elements should come from input");
            }
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanTotalPermutations() {
            List<String> elements = of("A", "B");
            int width = 2;
            int total = calculator.power(2, width).intValue(); // 2^2 = 4
            assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(width, elements)
                    .sample(total + 1)
                    .stream()
                    .toList(), "Sample size exceeding n^width should throw exception");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, "A", "B")
                    .sample(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInput() {
            var permutations = permutation.repetitive(0, Collections.emptyList())
                    .sample(1)
                    .stream()
                    .toList();
            assertEquals(1, permutations.size());
            assertEquals(of(), permutations.get(0), "Should generate one empty permutation for width=0");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var iterable = permutation.repetitive(2, "A", "B").sample(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertEquals(3, list1.size(), "First iterator should produce sampleSize permutations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize permutations");
            assertEquals(3, new HashSet<>(list1).size(), "First sample should be unique");
            assertEquals(3, new HashSet<>(list2).size(), "Second sample should be unique");
            for (List<String> perm : list1) {
                assertEquals(2, perm.size(), "Each permutation should have size width");
                assertTrue(of("A", "B").containsAll(perm), "Elements should be from input");
            }
        }
    }
}