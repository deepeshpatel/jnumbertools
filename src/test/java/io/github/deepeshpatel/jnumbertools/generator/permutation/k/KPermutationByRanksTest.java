package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.assertEveryMthValue;
import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class KPermutationByRanksTest {

    @Test
    void assertCount() {
        // nPk: n!/(n−k)!
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 0; k <= n; k++) {
                long size = permutation.nPk(k, input)
                        .lexOrder()
                        .stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Nested
    class KPermutationMthTest {

        @Test
        void shouldGenerateMthKPermutations() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 3;
            int start = 2;
            for (int m = 1; m <= 10; m += 2) {
                var all = permutation.nPk(k, input);
                var mth = permutation.nPk(k, input).lexOrderMth(m, start);
                assertEveryMthValue(all.lexOrder().stream(), mth.stream(), start, m);
            }
        }

        @Test
        void shouldReturnEmptyListForStartGreaterThanTotalPermutations() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            var result = permutation.nPk(k, input).lexOrderMth(1, 20).stream().toList();
            assertTrue(result.isEmpty(), "Should return empty list when start > total permutations");
        }

        @Test
        void shouldGenerateCorrectMthPermutationForKEquals1() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 1;
            var result = permutation.nPk(k, input).lexOrderMth(2, 1).stream().toList();
            assertEquals(2, result.size(), "Should generate correct number of permutations");
            for (List<Integer> perm : result) {
                assertEquals(1, perm.size(), "Each permutation should have size 1");
            }
        }

        @Test
        void shouldGenerateCorrectMthPermutationForKEqualsN() {
            var input = List.of(0, 1, 2, 3);
            int k = 4;
            var result = permutation.nPk(k, input).lexOrderMth(3, 0).stream().toList();
            assertEquals(8, result.size(), "Should generate correct number of permutations"); // 4! / 3 = 8
            for (List<Integer> perm : result) {
                assertEquals(4, perm.size(), "Each permutation should have size 4");
                assertEquals(new HashSet<>(perm).size(), 4, "Permutation should contain all unique elements");
            }
        }

        @Test
        void shouldHandleLargeIncrement() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            int increment = 100;
            var result = permutation.nPk(k, input).lexOrderMth(increment, 0).stream().toList();
            assertTrue(result.size() <= 1, "Large increment should result in at most one permutation");
        }

        @Test
        void shouldThrowExceptionForNegativeIncrement() {
            var input = List.of(0, 1, 2);
            int k = 2;
            var kPermBuilder = permutation.nPk(k, input);
            assertThrows(IllegalArgumentException.class, () -> kPermBuilder.lexOrderMth(0, 1));
            assertThrows(IllegalArgumentException.class, () -> kPermBuilder.lexOrderMth(-1, 1));
        }

        @Test
        void shouldThrowExceptionForNegativeStart() {
            var input = List.of(0, 1, 2);
            int k = 2;
            var kPermBuilder = permutation.nPk(k, input);
            assertThrows(IllegalArgumentException.class, () -> kPermBuilder.lexOrderMth(1, -1));
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            var iterable = permutation.nPk(k, input).lexOrderMth(2, 0);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        void shouldGenerateValidPermutationSizes() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 3;
            var result = permutation.nPk(k, input).lexOrderMth(3, 0).stream().toList();
            for (List<Integer> perm : result) {
                assertEquals(k, perm.size(), "Each permutation should have size k");
                assertEquals(new HashSet<>(perm).size(), k, "Permutation should contain unique elements");
                // Verify all elements are within bounds [0, input.size()-1]
                for (Integer element : perm) {
                    assertTrue(element >= 0 && element < input.size(), "Element should be in range [0, input.size()-1]: " + element);
                }
            }
        }
    }

    @Nested
    class KPermutationSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 3;
            int sampleSize = 4;
            var permutations = permutation.nPk(k, input)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate exactly sampleSize permutations");
        }

        @Test
        void shouldGenerateUniquePermutationsWithinSample() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            int sampleSize = 6;
            var permutations = permutation.nPk(k, input)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, permutations.size(), "Should generate sampleSize permutations");
            assertEquals(sampleSize, new HashSet<>(permutations).size(), "All permutations within sample should be unique");
        }

        @Test
        void shouldGenerateValidKPermutations() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 3;
            int sampleSize = 3;
            var permutations = permutation.nPk(k, input)
                    .sample(sampleSize)
                    .stream()
                    .toList();

            assertEquals(sampleSize, permutations.size());
            for (List<Integer> perm : permutations) {
                assertEquals(k, perm.size(), "Each permutation should have size k");
                assertEquals(new HashSet<>(perm).size(), k, "Permutation should contain unique elements");
                // Verify all elements are within bounds [0, input.size()-1]
                for (Integer element : perm) {
                    assertTrue(element >= 0 && element < input.size(), "Element should be in range [0, input.size()-1]: " + element);
                }
            }
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanTotalPermutations() {
            var input = List.of(0, 1, 2);
            int k = 2;
            int totalPermutations = 6; // 3P2 = 6
            assertThrows(IllegalArgumentException.class, () -> permutation.nPk(k, input)
                    .sample(totalPermutations + 1)
                    .stream()
                    .toList(), "Sample size exceeding total permutations should throw exception");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            var input = List.of(0, 1, 2);
            int k = 2;
            assertThrows(IllegalArgumentException.class, () -> permutation.nPk(k, input)
                    .sample(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleKEqualsZero() {
            var input = List.of(0, 1, 2);
            int k = 0;
            var permutations = permutation.nPk(k, input)
                    .sample(1)
                    .stream()
                    .toList();
            assertEquals(1, permutations.size(), "Should generate 1 permutation when k=0");
            for (List<Integer> perm : permutations) {
                assertEquals(0, perm.size(), "Each permutation should be empty");
            }
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            var iterable = permutation.nPk(k, input).sample(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(3, list1.size(), "First iterator should produce sampleSize permutations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize permutations");
            assertEquals(3, new HashSet<>(list1).size(), "First sample should contain unique permutations");
            assertEquals(3, new HashSet<>(list2).size(), "Second sample should contain unique permutations");
        }
    }

    @Nested
    class KPermutationChoiceTest {

        @Test
        void shouldGenerateExactChoiceSize() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            int choiceSize = 5;
            var permutations = permutation.nPk(k, input)
                    .choice(choiceSize)
                    .stream()
                    .toList();
            assertEquals(choiceSize, permutations.size(), "Should generate exactly choiceSize permutations");
        }

        @Test
        void shouldAllowDuplicatePermutations() {
            var input = List.of(0, 1, 2);
            int k = 2;
            int choiceSize = 8; // > 3P2 = 6 to ensure duplicates
            var permutations = permutation.nPk(k, input)
                    .choice(choiceSize)
                    .stream()
                    .toList();
            assertEquals(choiceSize, permutations.size(), "Should generate choiceSize permutations");
            // With replacement, duplicates are allowed, so we don't check for uniqueness
        }

        @Test
        void shouldGenerateValidKPermutations() {
            var input = List.of(0, 1, 2, 3, 4);
            int k = 3;
            int choiceSize = 4;
            var permutations = permutation.nPk(k, input)
                    .choice(choiceSize)
                    .stream()
                    .toList();

            assertEquals(choiceSize, permutations.size());
            for (List<Integer> perm : permutations) {
                assertEquals(k, perm.size(), "Each permutation should have size k");
                assertEquals(new HashSet<>(perm).size(), k, "Permutation should contain unique elements");
                // Verify all elements are within bounds [0, n-1]
                for (Integer element : perm) {
                    assertTrue(element >= 0 && element < input.size(), "Element should be in range [0, n-1]: " + element);
                }
            }
        }

        @Test
        void shouldHandleChoiceSizeLargerThanTotalPermutations() {
            var input = List.of(0, 1, 2);
            int k = 2;
            int choiceSize = 10; // > 3P2 = 6
            var permutations = permutation.nPk(k, input)
                    .choice(choiceSize)
                    .stream()
                    .toList();
            assertEquals(choiceSize, permutations.size(), "Should generate choiceSize permutations even if > total permutations");
        }

        @Test
        void shouldThrowExceptionForNegativeChoiceSize() {
            var input = List.of(0, 1, 2);
            int k = 2;
            assertThrows(IllegalArgumentException.class, () -> permutation.nPk(k, input)
                    .choice(-1)
                    .stream()
                    .toList(), "Negative choiceSize should throw exception");
        }

        @Test
        void shouldHandleKEqualsZero() {
            var input = List.of(0, 1, 2);
            int k = 0;
            var permutations = permutation.nPk(k, input)
                    .choice(3)
                    .stream()
                    .toList();
            assertEquals(3, permutations.size(), "Should generate choiceSize permutations");
            for (List<Integer> perm : permutations) {
                assertEquals(0, perm.size(), "Each permutation should be empty");
            }
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var input = List.of(0, 1, 2, 3);
            int k = 2;
            var iterable = permutation.nPk(k, input).choice(4);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(4, list1.size(), "First iterator should produce choiceSize permutations");
            assertEquals(4, list2.size(), "Second iterator should produce choiceSize permutations");
            for (List<Integer> perm : list1) {
                assertEquals(2, perm.size(), "Each permutation should have size k");
            }
            for (List<Integer> perm : list2) {
                assertEquals(2, perm.size(), "Each permutation should have size k");
            }
        }
    }

    @Nested
    class ByRanksValidationTest {

        @Test
        void byRanks_withNegativeRank_shouldThrowException() {
            var result = JNumberTools.permutations().nPk(2, "A", "B", "C").byRanks(of(java.math.BigInteger.valueOf(-1)));
            
            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Negative rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowException() {
            var result = JNumberTools.permutations().nPk(2, "A", "B", "C").byRanks(of(java.math.BigInteger.valueOf(1000000000)));
            
            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Out-of-bounds rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withValidRanks_shouldWork() {
            var result = JNumberTools.permutations().nPk(2, "A", "B", "C").byRanks(of(
                java.math.BigInteger.ZERO,
                java.math.BigInteger.ONE,
                java.math.BigInteger.valueOf(2)
            ));
            
            assertDoesNotThrow(() -> {
                var permutations = result.stream().toList();
                assertEquals(3, permutations.size());
            });
        }
    }
}
