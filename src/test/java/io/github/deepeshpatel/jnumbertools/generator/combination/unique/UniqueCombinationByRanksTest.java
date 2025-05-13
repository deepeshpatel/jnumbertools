package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class UniqueCombinationByRanksTest {

    @Nested
    class UniqueCombinationChoiceTest {
        @Test
        void shouldGenerateExactSampleSize() {
            int n = 5;
            int r = 3;
            int sampleSize = 4;
            var combinations = JNumberTools.combinations()
                    .unique(n, r)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate exactly sampleSize combinations");
        }

        @Test
        void shouldAllowDuplicateCombinations() {
            int n = 3;
            int r = 2;
            int sampleSize = 4; // Larger than nCr=3 to increase duplicate chance
            var combinations = JNumberTools.combinations()
                    .unique(n, r)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            int uniqueCount = new HashSet<>(combinations).size();
            assertTrue(uniqueCount <= calculator.nCr(n, r).intValueExact(), "Unique combinations should not exceed nCr");
            // Not asserting duplicates must occur (random), but allowing them
        }

        @Test
        void shouldGenerateValidCombinations() {
            List<String> elements = of("A", "B", "C", "D");
            int r = 2;
            int sampleSize = 3;
            var combinations = JNumberTools.combinations()
                    .unique(r, elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();

            assertEquals(sampleSize, combinations.size());
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertEquals(new HashSet<>(combo).size(), r, "Each combination should have unique elements");
                assertTrue(elements.containsAll(combo), "All elements should come from the input list");
            }
        }

        @Test
        void shouldHandleSampleSizeLargerThanNcr() {
            int n = 3;
            int r = 2;
            int sampleSize = 5; // nCr = 3C2 = 3, sampleSize > nCr
            var combinations = JNumberTools.combinations()
                    .unique(n, r)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations even if > nCr");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .unique(2, "A", "B", "C")
                    .choice(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = JNumberTools.combinations()
                    .unique(0, Collections.emptyList())
                    .choice(1)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForEmptyInputWithNonZeroSize() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .unique(1, Collections.emptyList())
                    .choice(1)
                    .stream()
                    .toList(), "Non-zero size with empty input should throw exception");
        }

        @Test
        void shouldHandleLargeInput() {
            List<Integer> largeInput = Stream.iterate(0, i -> i + 1).limit(20).collect(Collectors.toList());
            int r = 3;
            int sampleSize = 10;
            var combinations = JNumberTools.combinations()
                    .unique(r, largeInput)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations for large input");
            for (List<Integer> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertEquals(new HashSet<>(combo).size(), r, "Each combination should have unique elements");
                assertTrue(largeInput.containsAll(combo), "All elements should come from input");
            }
        }

        @Test
        void shouldGenerateConsistentPropertiesAcrossIterators() {
            var iterable = JNumberTools.combinations()
                    .unique(2, "A", "B", "C")
                    .choice(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(3, list1.size(), "First iterator should produce sampleSize combinations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize combinations");
            for (List<String> combo : list1) {
                assertEquals(2, combo.size(), "Each combination in first sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "First sample should use input elements");
            }
            for (List<String> combo : list2) {
                assertEquals(2, combo.size(), "Each combination in second sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "Second sample should use input elements");
            }
        }

        @Test
        void shouldGenerateMthUniqueCombinations() {
            int start = 3;
            int n = 8;
            int r = 4;
            for(int m=1; m<=32; m+=2) {
                var all = combination.unique(n, r).lexOrder();
                var mth = combination.unique(n, r).lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }
    }

    @Nested
    public class UniqueCombinationSampleTest {

        @Test
        void shouldThrowExceptionForSampleSizeZero() {
            assertThrows(IllegalArgumentException.class, () -> combination
                    .unique(2, "A", "B", "C")
                    .sample(0)
                    .stream()
                    .toList(), "Sample size of 0 should throw IllegalArgumentException");
        }

        @Test
        void shouldGenerateConsistentPropertiesAcrossIterators() {
            var iterable = combination
                    .unique(2, "A", "B", "C")
                    .sample(2);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(2, list1.size(), "First iterator should produce sampleSize combinations");
            assertEquals(2, list2.size(), "Second iterator should produce sampleSize combinations");
            assertEquals(2, new HashSet<>(list1).size(), "First sample should contain unique combinations");
            assertEquals(2, new HashSet<>(list2).size(), "Second sample should contain unique combinations");
            for (List<String> combo : list1) {
                assertEquals(2, combo.size(), "Each combination in first sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "First sample should use input elements");
            }
            for (List<String> combo : list2) {
                assertEquals(2, combo.size(), "Each combination in second sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "Second sample should use input elements");
            }
        }

        @Test
        void shouldGenerateExactSampleSize() {
            int n = 5;
            int r = 3;
            int sampleSize = 4;
            var combinations = combination
                    .unique(n, r)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate exactly sampleSize combinations");
        }

        @Test
        void shouldGenerateUniqueCombinations() {
            int n = 5;
            int r = 2;
            int sampleSize = 3;
            var combinations = combination
                    .unique(n, r)
                    .sample(sampleSize)
                    .stream()
                    .collect(Collectors.toSet()); // Use Set to check uniqueness
            assertEquals(sampleSize, combinations.size(), "All generated combinations should be unique");
        }

        @Test
        void shouldGenerateValidCombinations() {
            List<String> elements = of("A", "B", "C", "D");
            int r = 2;
            int sampleSize = 2;
            var combinations = combination
                    .unique(r, elements)
                    .sample(sampleSize)
                    .stream()
                    .toList();

            assertEquals(sampleSize, combinations.size());
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertEquals(new HashSet<>(combo).size() , r , "Each combination should have unique elements");
                assertTrue(elements.containsAll(combo), "All elements should come from the input list");
            }
        }

        @Test
        void shouldHandleSampleSizeEqualsNcr() {
            int n = 4;
            int r = 2;
            int sampleSize = calculator.nCr(n, r).intValueExact(); // 4C2 = 6
            var combinations = combination
                    .unique(n, r)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate all possible combinations when sampleSize equals nCr");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations should be unique");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> combination
                    .unique(2, "A", "B", "C")
                    .sample(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanNcr() {
            int n = 3;
            int r = 2;
            int nCr = calculator.nCr(n, r).intValueExact(); // 3C2 = 3
            assertThrows(IllegalArgumentException.class, () -> combination
                    .unique(n, r)
                    .sample(nCr + 1)
                    .stream()
                    .toList(), "Sample size exceeding nCr should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = combination
                    .unique(0, Collections.emptyList())
                    .sample(1)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForEmptyInputWithNonZeroSize() {
            assertThrows(IllegalArgumentException.class, () -> combination
                    .unique(1, Collections.emptyList())
                    .sample(1)
                    .stream()
                    .toList(), "Non-zero size with empty input should throw exception");
        }

        @Test
        void shouldHandleLargeInput() {
            List<Integer> largeInput = Stream.iterate(0, i -> i + 1).limit(20).collect(Collectors.toList());
            int r = 3;
            int sampleSize = 5;
            var combinations = combination
                    .unique(r, largeInput)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations for large input");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations should be unique");
        }
    }

    @Nested
    public class UniqueCombinationMthTest {

        @Test
        void assertCount() {
            for (int n = 3; n < 6; n++) {
                var input = Collections.nCopies(n, "A");
                for (int increment = 1; increment <= 4; increment++) {
                    int combinationSize = input.size() / 2;
                    long size = combination.unique(combinationSize, input).lexOrderMth(increment, 0).stream().count();
                    double expected = Math.ceil(calculator.nCr(n, combinationSize).longValue() / (double) increment);
                    assertEquals((long) expected, size);
                }
            }
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var iterable = combination.unique(2, A_B_C).lexOrderMth(2, 0);
            var lists1 = iterable.stream().toList();
            var lists2 = iterable.stream().toList();
            assertIterableEquals(lists1, lists2);
        }

        @Test
        void shouldGenerateCombinationsWithIncrementingToEvery3rdCombination() {
            var expected = List.of(of(0, 1, 2), of(0, 2, 3), of(1, 2, 3), of(2, 3, 4));
            var output = combination.unique(3, of(0, 1, 2, 3, 4))
                    .lexOrderMth(3, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void shouldSupportVeryLargeMthCombination() {
            var expected = List.of(
                    of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
                    of(0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33),
                    of(2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33)
            );

            int n = 34;
            int r = 17;
            int m = 1000_000_000; // jump to 1 billionth combination
            var output = combination.unique(n, r).lexOrderMth(m, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void shouldGenerateMthUniqueCombination() {
            var input = of('A', 'B', 'C', 'D', 'E', 'F');
            int start = 1;
            for (int r = 1; r <= input.size() / 2; r++) {
                for (int m = 1; m <= 10; m+=2) {
                    var all = combination.unique(r, input).lexOrder().stream();
                    var mth = combination.unique(r, input).lexOrderMth(m, start).stream();
                    assertEveryMthValue(all, mth, start, m);
                }
            }
        }

        @Test
        void testStartParameterGreaterThanZero() {
            var expected = List.of(
                    of('B', 'C'),
                    of('C', 'D')
            );
            var output = combination.unique(2, 'A', 'B', 'C', 'D')
                    .lexOrderMth(2, 3).stream().toList();
            assertIterableEquals(expected, output);
        }

    }

}