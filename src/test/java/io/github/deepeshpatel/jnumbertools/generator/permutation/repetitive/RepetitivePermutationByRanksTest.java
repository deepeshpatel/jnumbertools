package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class RepetitivePermutationByRanksTest {

    @Nested
    public class RepetitivePermutationMthTest {
        @Test
        void assertCount() {
            // nʳ permutations with repetition
            for (int n = 1; n <= 4; n++) {
                var input = Collections.nCopies(n, "A");
                for (int r = 0; r <= n + 1; r++) {
                    long size = permutation.repetitive(r, input)
                            .lexOrder()
                            .stream().count();
                    assertEquals(repetitionCount(input, n, r), size);
                }
            }
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: n=0, r=0 -> 0⁰ = 1 -> should return [[]]
            var zeroZeroGenerator = permutation.repetitive(0, Collections.emptyList())
                    .lexOrderMth(1, 0);
            var zeroZeroResult = zeroZeroGenerator.stream().toList();
            assertEquals(1, zeroZeroResult.size());
            assertTrue(zeroZeroResult.get(0).isEmpty());

            // Case 2: n=0, r>0 -> 0ʳ = 0 -> should return [] (empty iterator)
            var zeroPositiveGenerator = permutation.repetitive(2, Collections.emptyList())
                    .lexOrderMth(1, 0);
            assertTrue(zeroPositiveGenerator.stream().toList().isEmpty());

            // Case 3: n>0, r=0 -> n⁰ = 1 -> should return [[]]
            var positiveZeroGenerator = permutation.repetitive(0, "A", "B")
                    .lexOrderMth(1, 0);
            var positiveZeroResult = positiveZeroGenerator.stream().toList();
            assertEquals(1, positiveZeroResult.size());
            assertTrue(positiveZeroResult.get(0).isEmpty());

            // Case 4: With m>1, should still respect count=0
            var zeroPositiveWithMthGenerator = permutation.repetitive(2, Collections.emptyList())
                    .lexOrderMth(3, 0);
            assertTrue(zeroPositiveWithMthGenerator.stream().toList().isEmpty());
        }

        //TODO: Add method in calculator and remove from here and builder if suitable
        private static int repetitionCount(List<String> input, int n, int r) {
            if (input.isEmpty() && r > 0) {
                return 0;
            }
            if (r == 0) {
                return 1;
            }
            return calculator.power(input.size(), r).intValue();
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var iterable = permutation.repetitive(2, 'A', 'B', 'C')
                    .lexOrderMth(2, 0);
            assertIterableEquals(iterable.stream().toList(), iterable.stream().toList());
        }

        @Test
        void shouldGenerateAllPermutationsOf2Values() {
            var expected = List.of(
                    of(0, 0, 0),
                    of(0, 1, 0),
                    of(1, 0, 0),
                    of(1, 1, 0)
            );
            var output = permutation.repetitive(3, 0, 1)
                    .lexOrderMth(2, 0)
                    .stream()
                    .toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void testMthPermutationAgainstFixedOutput() {
            var expected = List.of(
                    of("A", "A"),
                    of("A", "C"),
                    of("B", "B"),
                    of("C", "A"),
                    of("C", "C")
            );
            var output = permutation.repetitive(2, "A", "B", "C")
                    .lexOrderMth(2, 0)
                    .stream()
                    .toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void test_start_parameter_greater_than_0() {
            var expected = List.of(
                    of("B", "A"),
                    of("B", "C"),
                    of("C", "B")
            );
            var output = permutation.repetitive(2, "A", "B", "C")
                    .lexOrderMth(2, 3)
                    .stream()
                    .toList();
            assertIterableEquals(expected, output);
        }

        @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
        @Test
        void stressTesting() {
            int n = 4;
            int width = 1000;
            for (int x = 2000; x <= 2010; x++) {
                BigInteger totalRepetitivePerm = calculator.power(x, width).divide(BigInteger.valueOf(n));
                long count = permutation.repetitive(width, x)
                        .lexOrderMth(totalRepetitivePerm, BigInteger.ZERO)
                        .stream()
                        .count();
                assertEquals(n, count, 1);
            }
        }

        @Test
        void shouldGeneratePermutationsForVeryLargeM() {
            List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            int width = 12;

            // Hardcoded expected result for very big m, verified once for correctness
            var expected = List.of(
                    of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                    of(2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                    of(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                    of(6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                    of(8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            );

            BigInteger largeM = BigInteger.valueOf(200_000_000_000L); // 200 billion

            var permutations = permutation.repetitive(width, input)
                    .lexOrderMth(largeM, BigInteger.ZERO)
                    .stream()
                    .toList();

            assertIterableEquals(expected, permutations);
        }

        @Test
        void shouldHandleLargeMWithNonZeroStart() {
            List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            int width = 12;
            BigInteger largeM = BigInteger.valueOf(200_000_000_000L);
            BigInteger start = BigInteger.valueOf(100_000_000_000L);

            var permutations = permutation.repetitive(width, input)
                    .lexOrderMth(largeM, start)
                    .stream()
                    .toList();

            // Just verify it doesn't throw and returns something
            assertNotNull(permutations);
            assertFalse(permutations.isEmpty());
        }

        @Test
        void shouldHandleIncrementLargerThanTotal() {
            int width = 2;
            var result = permutation.repetitive(width, 0, 1)
                    .lexOrderMth(10, 0) // increment > total
                    .stream()
                    .toList();

            assertEquals(1, result.size(), "Should return only first permutation");
            assertEquals(of(0, 0), result.get(0));
        }

        @Test
        void shouldGenerateRepetitiveMthPermutations() {
            int width =3;
            int noOfElements = 5;
            int start = 2;
            for(int m=1; m<=32; m++) {
                var all =      permutation.repetitive(width, noOfElements).lexOrder().stream();
                var everyMth = permutation.repetitive(width, noOfElements).lexOrderMth(m, start).stream();
                assertEveryMthValue(all, everyMth, start, m);
            }
        }
    }


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

    @Nested
    class ByRanksValidationTest {

        @Test
        void byRanks_withNegativeRank_shouldThrowException() {
            var result = permutation.repetitive(2, "A", "B", "C").byRanks(of(java.math.BigInteger.valueOf(-1)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Negative rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowException() {
            var result = permutation.repetitive(2, "A", "B", "C").byRanks(of(java.math.BigInteger.valueOf(1000000000)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Out-of-bounds rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withValidRanks_shouldWork() {
            var result = permutation.repetitive(2, "A", "B", "C").byRanks(of(
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