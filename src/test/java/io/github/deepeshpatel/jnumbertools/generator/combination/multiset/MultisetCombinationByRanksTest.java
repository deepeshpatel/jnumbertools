package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

class MultisetCombinationByRanksTest {

    @Nested
    class MultisetCombinationMthTest {

        @Test
        void assertCount() {
            // Multiset combinations with mᵗʰ: multisetCombinationsCount(r, frequencies)/m
            int increment = 3;
            int start = 0;

            var options = new LinkedHashMap<>(Map.of("A", 3, "B", 2, "C", 1));
            int combinationSize = 2;

            long count = combination.multiset(options, combinationSize)
                    .lexOrderMth(increment, start)
                    .stream()
                    .count();

            long totalCombinations = calculator.multisetCombinationsCount(
                    combinationSize,
                    options.values().stream().mapToInt(Integer::intValue).toArray()
            ).longValue();
            double expected = Math.ceil(totalCombinations / (double) increment);
            assertEquals((long) expected, count);
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: Empty map, r=0 -> 1 -> should return [{}]
            var emptyMapZeroGenerator = combination.multiset(new LinkedHashMap<>(), 0).lexOrderMth(1, 0);
            var emptyMapZeroResult = emptyMapZeroGenerator.stream().toList();
            assertEquals(1, emptyMapZeroResult.size());
            assertEquals(Map.of(), emptyMapZeroResult.get(0));

            // Case 2: Empty map, r>0 -> 0 -> should return [] (empty iterator)
            var emptyMapPositiveGenerator = combination.multiset(new LinkedHashMap<>(), 2).lexOrderMth(1, 0);
            assertTrue(emptyMapPositiveGenerator.stream().toList().isEmpty());

            // Case 3: Non-empty map, r=0 -> 1 -> should return [{}]
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var positiveZeroGenerator = combination.multiset(options, 0).lexOrderMth(1, 0);
            var positiveZeroResult = positiveZeroGenerator.stream().toList();
            assertEquals(1, positiveZeroResult.size());
            assertEquals(Map.of(), positiveZeroResult.get(0));

            // Case 4: r > total available -> 0 -> should return [] (empty iterator)
            var greaterRGenerator = combination.multiset(options, 10).lexOrderMth(1, 0);
            assertTrue(greaterRGenerator.stream().toList().isEmpty());

            // Case 5: With m>1, should still respect count=0
            var emptyMapWithMthGenerator = combination.multiset(new LinkedHashMap<>(), 2).lexOrderMth(3, 0);
            assertTrue(emptyMapWithMthGenerator.stream().toList().isEmpty());

            // Case 6: Map with all zero frequencies -> treated as empty -> count=0 for r>0
            var zeroFreqOptions = new LinkedHashMap<String, Integer>();
            zeroFreqOptions.put("A", 0);
            zeroFreqOptions.put("B", 0);
            var zeroFreqGenerator = combination.multiset(zeroFreqOptions, 2).lexOrderMth(1, 0);
            assertTrue(zeroFreqGenerator.stream().toList().isEmpty());
        }

        @Test
        void shouldGenerateCorrectCombinationsForAllOrdersAtRank10() {
            int k = 8;
            int start = 10;
            var options = new LinkedHashMap<>(Map.of("A", 10, "B", 4, "C", 5, "D", 7, "E", 4, "F", 7, "G", 2, "H", 1, "I", 1, "J", 2));
            var combBuilder = combination.multiset(options,k);
            var mth = combBuilder.lexOrderMth(1,start).stream().toList();
            var elementAtIndex10 = combBuilder.lexOrder().stream().skip(start).findFirst().get();
            assertEquals(elementAtIndex10, mth.get(0));
        }

        @Test
        //@EnabledIfSystemProperty(named = "stress.testing", matches = "true")
        void shouldGenerateMthMultisetCombinations() {
            int size = 4;
            int start = 3;
            var options = new LinkedHashMap<>(Map.of("A", 10, "B", 4, "C", 5, "D", 7, "E", 4, "F", 7, "G", 2, "H", 1, "I", 1, "J", 2));
            for (int m = 1; m <= 32; m += 2) {
                var builder = combination.multiset(options, size);
                var all = builder.lexOrder();
                var mth = builder.lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }
    }

    @Nested
    class MultisetCombinationSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int combinationSize = 2;
            int sampleSize = 3;
            var combinations = combination.multiset(options, combinationSize)
                    .sample(sampleSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate exactly sampleSize combinations");
        }

        @Test
        void shouldGenerateUniqueCombinationsWithinSample() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int combinationSize = 2;
            int sampleSize = 4;
            var combinations = combination.multiset(options, combinationSize)
                    .sample(sampleSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations within sample should be unique");
        }

        @Test
        void shouldGenerateValidMultisetCombinations() {
            var options = new LinkedHashMap<>(Map.of("A", 3, "B", 2, "C", 1));
            int combinationSize = 3;
            int sampleSize = 3;
            var combinations = combination.multiset(options, combinationSize)
                    .sample(sampleSize, TestBase.random)
                    .stream()
                    .toList();

            assertEquals(sampleSize, combinations.size());
            for (Map<String, Integer> combo : combinations) {
                int totalElements = combo.values().stream().mapToInt(Integer::intValue).sum();
                assertEquals(combinationSize, totalElements, "Each combination should have total size equal to combinationSize");

                // Verify frequencies don't exceed original limits
                for (Map.Entry<String, Integer> entry : combo.entrySet()) {
                    assertTrue(entry.getValue() <= options.get(entry.getKey()),
                            "Frequency should not exceed original limit for " + entry.getKey());
                }
            }
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanTotalCombinations() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            int combinationSize = 2;
            int totalCombinations = 1; // Only {A=1, B=1} possible
            assertThrows(IllegalArgumentException.class, () -> combination.multiset(options, combinationSize)
                    .sample(totalCombinations + 1, TestBase.random)
                    .stream()
                    .toList(), "Sample size exceeding total combinations should throw exception");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            assertThrows(IllegalArgumentException.class, () -> combination.multiset(options, 1)
                    .sample(-1, TestBase.random)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var options = new LinkedHashMap<String, Integer>();
            var combinations = combination.multiset(options, 0)
                    .sample(1, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(Map.of(), combinations.get(0), "Combination should be empty map");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            var iterable = combination.multiset(options, 2).sample(3, TestBase.random);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(3, list1.size(), "First iterator should produce sampleSize combinations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize combinations");
            assertEquals(3, new HashSet<>(list1).size(), "First sample should contain unique combinations");
            assertEquals(3, new HashSet<>(list2).size(), "Second sample should contain unique combinations");
        }
    }

    @Nested
    class MultisetCombinationChoiceTest {

        @Test
        void shouldGenerateExactChoiceSize() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int combinationSize = 2;
            int choiceSize = 4;
            var combinations = combination.multiset(options, combinationSize)
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(choiceSize, combinations.size(), "Should generate exactly choiceSize combinations");
        }

        @Test
        void shouldAllowDuplicateCombinations() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            int combinationSize = 2;
            int choiceSize = 4; // > total combinations to ensure duplicates
            var combinations = combination.multiset(options, combinationSize)
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(choiceSize, combinations.size(), "Should generate choiceSize combinations");
            // With replacement, duplicates are allowed, so we don't check for uniqueness
        }

        @Test
        void shouldGenerateValidMultisetCombinations() {
            var options = new LinkedHashMap<>(Map.of("A", 3, "B", 2, "C", 1));
            int combinationSize = 3;
            int choiceSize = 3;
            var combinations = combination.multiset(options, combinationSize)
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();

            assertEquals(choiceSize, combinations.size());
            for (Map<String, Integer> combo : combinations) {
                int totalElements = combo.values().stream().mapToInt(Integer::intValue).sum();
                assertEquals(combinationSize, totalElements, "Each combination should have total size equal to combinationSize");

                // Verify frequencies don't exceed original limits
                for (Map.Entry<String, Integer> entry : combo.entrySet()) {
                    assertTrue(entry.getValue() <= options.get(entry.getKey()),
                            "Frequency should not exceed original limit for " + entry.getKey());
                }
            }
        }

        @Test
        void shouldHandleChoiceSizeLargerThanTotalCombinations() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            int combinationSize = 2;
            int choiceSize = 5; // > total combinations
            var combinations = combination.multiset(options, combinationSize)
                    .choice(choiceSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(choiceSize, combinations.size(), "Should generate choiceSize combinations even if > total combinations");
        }

        @Test
        void shouldThrowExceptionForNegativeChoiceSize() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            assertThrows(IllegalArgumentException.class, () -> combination.multiset(options, 1)
                    .choice(-1, TestBase.random)
                    .stream()
                    .toList(), "Negative choiceSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var options = new LinkedHashMap<String, Integer>();
            var combinations = combination.multiset(options, 0)
                    .choice(1, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(Map.of(), combinations.get(0), "Combination should be empty map");
        }

        @Test
        void shouldGenerateConsistentResultsAcrossIterators() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            var iterable = combination.multiset(options, 2).choice(3, TestBase.random);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(3, list1.size(), "First iterator should produce choiceSize combinations");
            assertEquals(3, list2.size(), "Second iterator should produce choiceSize combinations");
            for (Map<String, Integer> combo : list1) {
                int totalElements = combo.values().stream().mapToInt(Integer::intValue).sum();
                assertEquals(2, totalElements, "Each combination should have size 2");
            }
            for (Map<String, Integer> combo : list2) {
                int totalElements = combo.values().stream().mapToInt(Integer::intValue).sum();
                assertEquals(2, totalElements, "Each combination should have size 2");
            }
        }
    }

    @Nested
    class ByRanksValidationTest {

        @Test
        void byRanks_withNegativeRank_shouldThrowException() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2);
            options.put("B", 1);

            var result = combination.multiset(options, 2).byRanks(java.util.List.of(java.math.BigInteger.valueOf(-1)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Negative rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowException() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2);
            options.put("B", 1);

            var result = combination.multiset(options, 2).byRanks(java.util.List.of(java.math.BigInteger.valueOf(1000000000)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Out-of-bounds rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withValidRanks_shouldWork() {
            LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
            options.put("A", 2);
            options.put("B", 1);

            var result = combination.multiset(options, 2).byRanks(java.util.List.of(
                    java.math.BigInteger.ZERO,
                    java.math.BigInteger.ONE
            ));

            assertDoesNotThrow(() -> {
                var combinations = result.stream().toList();
                assertEquals(2, combinations.size());
            });
        }
    }
}