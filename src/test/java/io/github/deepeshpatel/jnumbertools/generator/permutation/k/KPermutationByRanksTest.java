package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.api.JNumberTools;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class KPermutationByRanksTest {

    @Nested
    class KPermutationMthTest {

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

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: n=0, k=0 -> ⁰P₀ = 1 -> should return [[]]
            var zeroZeroGenerator = permutation.nPk(0, 0).lexOrderMth(1, 0);
            var zeroZeroResult = zeroZeroGenerator.stream().toList();
            assertEquals(1, zeroZeroResult.size());
            assertTrue(zeroZeroResult.get(0).isEmpty());

            // Case 2: n=0, k>0 -> ⁰Pₖ = 0 -> should return [] (empty iterator)
            var zeroPositiveGenerator = permutation.nPk(0, 2).lexOrderMth(1, 0);
            assertTrue(zeroPositiveGenerator.stream().toList().isEmpty());

            // Case 3: n>0, k=0 -> ⁿP₀ = 1 -> should return [[]]
            var positiveZeroGenerator = permutation.nPk(3, 0).lexOrderMth(1, 0);
            var positiveZeroResult = positiveZeroGenerator.stream().toList();
            assertEquals(1, positiveZeroResult.size());
            assertTrue(positiveZeroResult.get(0).isEmpty());

            // Case 4: n>0, k>n -> ⁿPₖ = 0 -> should return [] (empty iterator)
            var greaterKGenerator = permutation.nPk(2, 3).lexOrderMth(1, 0);
            assertTrue(greaterKGenerator.stream().toList().isEmpty());

            // Case 5: With m>1, should still respect count=0
            var greaterKWithMthGenerator = permutation.nPk(2, 3).lexOrderMth(3, 0);
            assertTrue(greaterKWithMthGenerator.stream().toList().isEmpty());

            // Case 6: Empty list with k>0 -> ⁰Pₖ = 0
            var emptyListGenerator = permutation.nPk(2, Collections.emptyList()).lexOrderMth(1, 0);
            assertTrue(emptyListGenerator.stream().toList().isEmpty());
        }

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
                assertEquals(4, new HashSet<>(perm).size(), "Permutation should contain all unique elements");
            }
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
                assertEquals(k, new HashSet<>(perm).size(), "Permutation should contain unique elements");
                // Verify all elements are within bounds [0, input.size()-1]
                for (Integer element : perm) {
                    assertTrue(element >= 0 && element < input.size(), "Element should be in range [0, input.size()-1]: " + element);
                }
            }
        }

        @Test
        void shouldSupportVeryLargeMthKPermutation() {
            // Find every 10^29th 20-Permutation of 40 elements (20P40)
            BigInteger increment = new BigInteger("100000000000000000000000000000");
            var expected = of(
                    of(11, 37, 6, 5, 26, 15, 0, 25, 9, 22, 27, 16, 12, 21, 24, 31, 33, 34, 17, 38),
                    of(23, 34, 12, 11, 10, 28, 1, 8, 18, 3, 9, 30, 24, 0, 6, 20, 26, 27, 37, 35),
                    of(35, 30, 18, 16, 38, 1, 2, 32, 23, 22, 33, 3, 29, 17, 26, 6, 12, 14, 11, 28)
            );

            var permutations = permutation.nPk(40, 20)
                    .lexOrderMth(increment, increment)
                    .stream().toList();
            assertIterableEquals(expected, permutations);
        }

        @Test
        void testStartParameterGreaterThanZero() {
            var expected = List.of(
                    of('a', 'c', 'e'),
                    of('c', 'a', 'd'),
                    of('d', 'e', 'a')
            );
            var output = permutation.nPk(3, 'a', 'b', 'c', 'd', 'e')
                    .lexOrderMth(20, 5)
                    .stream().toList();
            assertIterableEquals(expected, output);
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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
                    .stream()
                    .toList();

            assertEquals(sampleSize, permutations.size());
            for (List<Integer> perm : permutations) {
                assertEquals(k, perm.size(), "Each permutation should have size k");
                assertEquals(k, new HashSet<>(perm).size(), "Permutation should contain unique elements");
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
                    .sample(totalPermutations + 1, random)
                    .stream()
                    .toList(), "Sample size exceeding total permutations should throw exception");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            var input = List.of(0, 1, 2);
            int k = 2;
            assertThrows(IllegalArgumentException.class, () -> permutation.nPk(k, input)
                    .sample(-1, random)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleKEqualsZero() {
            var input = List.of(0, 1, 2);
            int k = 0;
            var permutations = permutation.nPk(k, input)
                    .sample(1, TestBase.random)
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
            var iterable = permutation.nPk(k, input).sample(3, TestBase.random);
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
                    .choice(choiceSize, TestBase.random)
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
                    .choice(choiceSize, TestBase.random)
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
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();

            assertEquals(choiceSize, permutations.size());
            for (List<Integer> perm : permutations) {
                assertEquals(k, perm.size(), "Each permutation should have size k");
                assertEquals(k, new HashSet<>(perm).size(), "Permutation should contain unique elements");
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
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(choiceSize, permutations.size(), "Should generate choiceSize permutations even if > total permutations");
        }

        @Test
        void shouldThrowExceptionForNegativeChoiceSize() {
            var input = List.of(0, 1, 2);
            int k = 2;
            assertThrows(IllegalArgumentException.class, () -> permutation.nPk(k, input)
                    .choice(-1, TestBase.random)
                    .stream()
                    .toList(), "Negative choiceSize should throw exception");
        }

        @Test
        void shouldHandleKEqualsZero() {
            var input = List.of(0, 1, 2);
            int k = 0;
            var permutations = permutation.nPk(k, input)
                    .choice(3, TestBase.random)
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
            var iterable = permutation.nPk(k, input).choice(4, TestBase.random);
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
