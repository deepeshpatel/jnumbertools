package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class MultisetPermutationByRanksTest {

    @Nested
    class MultisetPermutationMthTest {

        @Test
        void assertCount() {
            // Multiset permutations: n!/(n₁!·n₂!·...·nₖ!)
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            long expected = calculator.multinomial(options.values().stream().mapToInt(Integer::intValue).toArray()).longValue();
            long actual = permutation.multiset(options).lexOrder().stream().count();
            assertEquals(expected, actual);
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: Empty map -> 0! = 1 -> should return [[]]
            var emptyMapGenerator = permutation.multiset(new LinkedHashMap<>()).lexOrderMth(1, 0);
            var emptyMapResult = emptyMapGenerator.stream().toList();
            assertEquals(1, emptyMapResult.size());
            assertTrue(emptyMapResult.get(0).isEmpty());

            // Case 2: Empty map with m>1 -> still should return [[]]
            var emptyMapWithMthGenerator = permutation.multiset(new LinkedHashMap<>()).lexOrderMth(3, 0);
            var emptyMapWithMthResult = emptyMapWithMthGenerator.stream().toList();
            assertEquals(1, emptyMapWithMthResult.size());
            assertTrue(emptyMapWithMthResult.get(0).isEmpty());

            // Case 3: Map with all zero frequencies (filtered out) -> treated as empty -> should return [[]]
            var zeroFreqOptions = new LinkedHashMap<String, Integer>();
            zeroFreqOptions.put("A", 0);
            zeroFreqOptions.put("B", 0);
            var zeroFreqGenerator = permutation.multiset(zeroFreqOptions).lexOrderMth(1, 0);
            var zeroFreqResult = zeroFreqGenerator.stream().toList();
            assertEquals(1, zeroFreqResult.size());
            assertTrue(zeroFreqResult.get(0).isEmpty());

            // Case 4: Single element with frequency f -> only one permutation
            var singleOptions = new LinkedHashMap<>(Map.of("X", 3));
            var singleGenerator = permutation.multiset(singleOptions).lexOrderMth(1, 0);
            var singleResult = singleGenerator.stream().toList();
            assertEquals(1, singleResult.size());
            assertEquals(3, singleResult.get(0).size());
            assertEquals("X", singleResult.get(0).get(0));
        }

        @Test
        void shouldGenerateMthMultisetPermutations() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int start = 2;
            for (int m = 1; m <= 10; m += 2) {
                var builder = permutation.multiset(options);
                var all = builder.lexOrder();
                var mth = builder.lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }

        @Test
        void shouldGenerateCorrectMthPermutationForSingleElement() {
            var options = new LinkedHashMap<>(Map.of("A", 3));
            var result = permutation.multiset(options).lexOrderMth(1, 0).stream().toList();
            assertEquals(1, result.size(), "Should generate one permutation for single element type");
            assertEquals(3, result.get(0).size(), "Permutation should have correct size");
            assertEquals("A", result.get(0).get(0), "All elements should be A");
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            var iterable = permutation.multiset(options).lexOrderMth(2, 0);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();
            assertIterableEquals(list1, list2);
        }

        @Test
        void shouldGenerateValidPermutationSizes() {
            var options = new LinkedHashMap<>(Map.of("A", 3, "B", 2));
            var result = permutation.multiset(options).lexOrderMth(5, 0).stream().toList();
            for (var perm : result) {
                assertEquals(5, perm.size(), "Each permutation should have total size equal to sum of frequencies");
                // Verify all elements are from the original set
                for (String element : perm) {
                    assertTrue(options.containsKey(element), "Element should be from original options: " + element);
                }
            }
        }

        @Test
        void test_start_parameter_greater_than_0() {
            var expected = of(
                    of('a', 'c', 'b', 'c', 'b', 'c'),
                    of('b', 'c', 'c', 'a', 'c', 'b'),
                    of('c', 'b', 'c', 'b', 'c', 'a')
            );

            LinkedHashMap<Character, Integer> options = createMap(of('a', 'b', 'c'), new int[]{1, 2, 3});

            var output = permutation.multiset(options)
                    .lexOrderMth(20, 5)
                    .stream().toList();
            assertIterableEquals(expected, output);
        }
    }

    @Nested
    class MultisetPermutationSampleTest {

        @Test
        void shouldGenerateSampledPermutationsWithinBounds() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1, "C", 1));
            int sampleSize = 3;
            var permBuilder = permutation.multiset(options);
            var sampled = permBuilder.sample(sampleSize, TestBase.random).stream().toList();

            assertEquals(sampleSize, sampled.size(), "Sample size should match requested size");
            assertTrue(sampled.stream().allMatch(p -> p.size() == 4));
            assertEquals(sampled.stream().distinct().count(), sampled.size(), "Sampled permutations should be unique");
        }

        @Test
        void shouldThrowExceptionForInvalidSampleSize() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            var permBuilder = permutation.multiset(options);
            assertThrows(IllegalArgumentException.class, () -> permBuilder.sample(0, TestBase.random), "Sample size of 0 should throw exception");
            assertThrows(IllegalArgumentException.class, () -> permBuilder.sample(3, TestBase.random), "Sample size exceeding total permutations should throw exception");
        }
    }

    @Nested
    class MultisetPermutationChoiceTest {

        @Test
        void shouldGenerateChoicePermutationsWithPossibleDuplicates() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            int choiceSize = 4;
            var permBuilder = permutation.multiset(options);
            var chosen = permBuilder.choice(choiceSize, TestBase.random).stream().toList();

            assertEquals(choiceSize, chosen.size(), "Choice size should match requested size");
            assertTrue(chosen.stream().allMatch(p -> p.size() == 3), "All permutations should have correct length");
            // With replacement, duplicates are allowed, so we don't check for uniqueness
        }

        @Test
        void shouldThrowExceptionForInvalidChoiceSize() {
            var options = new LinkedHashMap<>(Map.of("A", 1, "B", 1));
            var permBuilder = permutation.multiset(options);
            assertThrows(IllegalArgumentException.class, () -> permBuilder.choice(0, TestBase.random), "Choice size of 0 should throw exception");
        }
    }

    @Nested
    class ByRanksValidationTest {

        @Test
        void byRanks_withNegativeRank_shouldThrowException() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var result = permutation.multiset(options).byRanks(java.util.List.of(java.math.BigInteger.valueOf(-1)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Negative rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowException() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var result = permutation.multiset(options).byRanks(java.util.List.of(java.math.BigInteger.valueOf(1000000000)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Out-of-bounds rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withValidRanks_shouldWork() {
            var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
            var result = permutation.multiset(options).byRanks(java.util.List.of(
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