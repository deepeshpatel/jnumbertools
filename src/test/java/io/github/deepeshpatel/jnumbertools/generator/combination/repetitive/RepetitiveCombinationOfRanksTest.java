package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class RepetitiveCombinationOfRanksTest {

    @Nested
    public class RepetitiveCombinationMthTest {

        @Test
        void should_generate_correct_combinations_for_r_greater_than_n() {
            int n = 3;
            int r = 5;
            var expected = combination.repetitive(n, r).lexOrder().stream().toList();
            var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void should_generate_correct_combination_for_r_equals_1() {
            int n = 4;
            int r = 1;
            var expected = combination.repetitive(n, r).lexOrder().stream().toList();
            var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void should_generate_correct_combination_for_n_equals_1() {
            int n = 1;
            int r = 5;
            var expected = combination.repetitive(n, r).lexOrder().stream().toList();
            var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void should_generate_correct_combination_for_mth_value() {
            int n = 3;
            int r = 3;
            var expected = List.of(
                    of(0, 1, 1),
                    of(1, 2, 2)
            );

            var output = combination.repetitive(n, r).lexOrderMth(5, 3).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void should_return_empty_lists_for_m_out_of_bounds() {
            int n = 2;
            int r = 2;
            var list = combination.repetitive(n, r).lexOrderMth(8, 8).stream().toList();
            assertTrue(list.isEmpty());
        }

        @Test
        void should_return_empty_list_when_r_equals_0() {
            int n = 3;
            int r = 0;
            var expected = combination.repetitive(n, r).lexOrder().stream().toList();
            var output = combination.repetitive(n, r).lexOrderMth(1, 0).stream().toList();
            assertIterableEquals(expected, output);
        }

        @Test
        void should_throw_exception_for_negative_r_value() {
            int n = 3;
            int r = -2;
            assertThrows(IllegalArgumentException.class, () ->
                    combination.repetitive(n, r).lexOrderMth(1, 0));
        }

        @Test
        void shouldGenerateMthRepetitiveCombinations() {
            int start = 3;
            int n = 8;
            int r = 4;
            for(int m=1; m<=15; m++) {
                var all = combination.repetitive(n, r).lexOrder();
                var mth = combination.repetitive(n, r).lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }
    }

    /**
     * Test class for RepetitiveCombinationSample, verifying random sampling of repetitive combinations without replacement.
     */
    @Nested
    public class RepetitiveCombinationSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int r = 2;
            int sampleSize = 3;
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B", "C")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate exactly sampleSize combinations");
        }

        @Test
        void shouldGenerateUniqueCombinationsWithinSample() {
            int r = 2;
            int sampleSize = 4;
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B", "C")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations within sample should be unique");
        }

        @Test
        void shouldGenerateValidRepetitiveCombinations() {
            List<String> elements = of("A", "B", "C");
            int r = 2;
            int sampleSize = 3;
            var combinations = JNumberTools.combinations()
                    .repetitive(r, elements)
                    .sample(sampleSize)
                    .stream()
                    .toList();

            assertEquals(sampleSize, combinations.size());
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertTrue(elements.containsAll(combo), "All elements should come from the input list");
            }
        }

        @Test
        void shouldHandleSampleSizeEqualsNcrRepetitive() {
            int n = 2;
            int r = 2;
            int sampleSize = calculator.nCrRepetitive(n, r).intValueExact(); // (2+2-1)C2 = 3C2 = 3
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B")
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate all possible combinations when sampleSize equals nCrRepetitive");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations should be unique");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(2, "A", "B", "C")
                    .sample(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldThrowExceptionForSampleSizeGreaterThanNcrRepetitive() {
            int n = 2;
            int r = 2;
            int nCrRepetitive = calculator.nCrRepetitive(n, r).intValueExact(); // 3C2 = 3
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(r, "A", "B")
                    .sample(nCrRepetitive + 1)
                    .stream()
                    .toList(), "Sample size exceeding nCrRepetitive should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = JNumberTools.combinations()
                    .repetitive(0, Collections.emptyList())
                    .sample(1)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForNegativeRValue() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(-1, "A", "B")
                    .sample(1)
                    .stream()
                    .toList(), "Negative r value should throw exception");
        }

        @Test
        void shouldHandleLargeInputAndRGreaterThanN() {
            List<String> elements = of("A", "B", "C");
            int r = 4; // r > n
            int sampleSize = 5; // (3+4-1)C4 = 6C4 = 15
            var combinations = JNumberTools.combinations()
                    .repetitive(r, elements)
                    .sample(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations should be unique");
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertTrue(elements.containsAll(combo), "All elements should come from input");
            }
        }

        @Test
        void shouldGenerateConsistentPropertiesAcrossIterators() {
            var iterable = JNumberTools.combinations()
                    .repetitive(2, "A", "B", "C")
                    .sample(3);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(3, list1.size(), "First iterator should produce sampleSize combinations");
            assertEquals(3, list2.size(), "Second iterator should produce sampleSize combinations");
            assertEquals(3, new HashSet<>(list1).size(), "First sample should contain unique combinations");
            assertEquals(3, new HashSet<>(list2).size(), "Second sample should contain unique combinations");
            for (List<String> combo : list1) {
                assertEquals(2, combo.size(), "Each combination in first sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "First sample should use input elements");
            }
            for (List<String> combo : list2) {
                assertEquals(2, combo.size(), "Each combination in second sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "Second sample should use input elements");
            }
        }
    }

    /**
     * Test class for RepetitiveCombinationChoice, verifying random sampling of repetitive combinations with replacement.
     */
    @Nested
    public class RepetitiveCombinationChoiceTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int r = 2;
            int sampleSize = 4;
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B", "C")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate exactly sampleSize combinations");
        }

        @Test
        void shouldAllowDuplicateCombinations() {
            int n = 2;
            int r = 2;
            int sampleSize = 4; // nCrRepetitive = (2+2-1)C2 = 3C2 = 3
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            int uniqueCount = new HashSet<>(combinations).size();
            assertTrue(uniqueCount <= calculator.nCrRepetitive(n, r).intValueExact(), "Unique combinations should not exceed nCrRepetitive");
        }

        @Test
        void shouldGenerateValidRepetitiveCombinations() {
            List<String> elements = of("A", "B", "C");
            int r = 2;
            int sampleSize = 3;
            var combinations = JNumberTools.combinations()
                    .repetitive(r, elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();

            assertEquals(sampleSize, combinations.size());
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertTrue(elements.containsAll(combo), "All elements should come from the input list");
            }
        }

        @Test
        void shouldHandleSampleSizeLargerThanNcrRepetitive() {
            int r = 2;
            int sampleSize = 5; // nCrRepetitive = 3C2 = 3, sampleSize > nCrRepetitive
            var combinations = JNumberTools.combinations()
                    .repetitive(r, "A", "B")
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations even if > nCrRepetitive");
        }

        @Test
        void shouldThrowExceptionForNegativeSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(2, "A", "B", "C")
                    .choice(-1)
                    .stream()
                    .toList(), "Negative sampleSize should throw exception");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = JNumberTools.combinations()
                    .repetitive(0, Collections.emptyList())
                    .choice(1)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForNegativeRValue() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(-1, "A", "B")
                    .choice(1)
                    .stream()
                    .toList(), "Negative r value should throw exception");
        }

        @Test
        void shouldHandleLargeInputAndRGreaterThanN() {
            List<String> elements = of("A", "B", "C");
            int r = 4; // r > n
            int sampleSize = 7; // (3+4-1)C4 = 6C4 = 15
            var combinations = JNumberTools.combinations()
                    .repetitive(r, elements)
                    .choice(sampleSize)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations");
            for (List<String> combo : combinations) {
                assertEquals(r, combo.size(), "Each combination should have size r");
                assertTrue(elements.containsAll(combo), "All elements should come from input");
            }
        }

        @Test
        void shouldGenerateConsistentPropertiesAcrossIterators() {
            var iterable = JNumberTools.combinations()
                    .repetitive(2, "A", "B", "C")
                    .choice(4);
            var list1 = iterable.stream().toList();
            var list2 = iterable.stream().toList();

            assertEquals(4, list1.size(), "First iterator should produce sampleSize combinations");
            assertEquals(4, list2.size(), "Second iterator should produce sampleSize combinations");
            for (List<String> combo : list1) {
                assertEquals(2, combo.size(), "Each combination in first sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "First sample should use input elements");
            }
            for (List<String> combo : list2) {
                assertEquals(2, combo.size(), "Each combination in second sample should have size r");
                assertTrue(of("A", "B", "C").containsAll(combo), "Second sample should use input elements");
            }
        }
    }

}