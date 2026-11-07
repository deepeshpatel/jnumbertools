package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import io.github.deepeshpatel.jnumbertools.TestBase;
import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

class RepetitiveCombinationByRanksTest {

    @Nested
    public class RepetitiveCombinationMthTest {

        @Test
        void assertCount() {
            // nCr with repetition and mᵗʰ: (n+r−1)!/(r!·(n−1)!)/m
            int increment = 4;
            int start = 0;

            for (int n = 1; n <= 4; n++) {
                for (int r = 1; r <= 3; r++) {
                    long count = combination.repetitive(n, r)
                            .lexOrderMth(increment, start)
                            .stream()
                            .count();

                    long totalCombinations = calculator.nCrRepetitive(n, r).longValue();
                    double expected = Math.ceil(totalCombinations / (double) increment);
                    assertEquals((long) expected, count);
                }
            }
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: n=0, r=0 -> 1 -> should return [[]]
            var zeroZeroGenerator = combination.repetitive(0, 0).lexOrderMth(1, 0);
            var zeroZeroResult = zeroZeroGenerator.stream().toList();
            assertEquals(1, zeroZeroResult.size());
            assertTrue(zeroZeroResult.get(0).isEmpty());

            // Case 2: n=0, r>0 -> 0 -> should return [] (empty iterator)
            var zeroPositiveGenerator = combination.repetitive(0, 2).lexOrderMth(1, 0);
            assertTrue(zeroPositiveGenerator.stream().toList().isEmpty());

            // Case 3: n>0, r=0 -> 1 -> should return [[]]
            var positiveZeroGenerator = combination.repetitive(3, 0).lexOrderMth(1, 0);
            var positiveZeroResult = positiveZeroGenerator.stream().toList();
            assertEquals(1, positiveZeroResult.size());
            assertTrue(positiveZeroResult.get(0).isEmpty());

            // Case 4: With m>1, should still respect count=0
            var zeroPositiveWithMthGenerator = combination.repetitive(0, 2).lexOrderMth(3, 0);
            assertTrue(zeroPositiveWithMthGenerator.stream().toList().isEmpty());

            // Case 5: Empty list with r>0 -> 0
            var emptyListGenerator = combination.repetitive(2, Collections.emptyList()).lexOrderMth(1, 0);
            assertTrue(emptyListGenerator.stream().toList().isEmpty());
        }

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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate all possible combinations when sampleSize equals nCrRepetitive");
            assertEquals(sampleSize, new HashSet<>(combinations).size(), "All combinations should be unique");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = JNumberTools.combinations()
                    .repetitive(0, Collections.emptyList())
                    .sample(1, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForNegativeRValue() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(-1, "A", "B")
                    .sample(1, TestBase.random)
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
                    .sample(sampleSize, TestBase.random)
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
                    .sample(3, TestBase.random);
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
                    .choice(sampleSize, TestBase.random)
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
                    .choice(sampleSize, TestBase.random)
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
                    .choice(sampleSize, TestBase.random)
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
                    .choice(sampleSize, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(sampleSize, combinations.size(), "Should generate sampleSize combinations even if > nCrRepetitive");
        }

        @Test
        void shouldHandleEmptyInputWithZeroSize() {
            var combinations = JNumberTools.combinations()
                    .repetitive(0, Collections.emptyList())
                    .choice(1, TestBase.random)
                    .stream()
                    .toList();
            assertEquals(1, combinations.size(), "Should generate one empty combination");
            assertEquals(of(), combinations.get(0), "Combination should be empty list");
        }

        @Test
        void shouldThrowExceptionForNegativeRValue() {
            assertThrows(IllegalArgumentException.class, () -> JNumberTools.combinations()
                    .repetitive(-1, "A", "B")
                    .choice(1, TestBase.random)
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
                    .choice(sampleSize, TestBase.random)
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
                    .choice(4, TestBase.random);
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

    @Nested
    class ByRanksValidationTest {

        @Test
        void byRanks_withNegativeRank_shouldThrowException() {
            var result = combination.repetitive(2, "A", "B", "C").byRanks(of(BigInteger.valueOf(-1)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Negative rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowException() {
            var result = combination.repetitive(2, "A", "B", "C").byRanks(of(BigInteger.valueOf(1000000000)));

            assertThrows(IllegalArgumentException.class, () -> {
                result.stream().toList(); // Should throw during iteration
            }, "Out-of-bounds rank should throw IllegalArgumentException");
        }

        @Test
        void byRanks_withValidRanks_shouldWork() {
            var result = combination.repetitive(2, "A", "B", "C").byRanks(of(
                    BigInteger.ZERO,
                    BigInteger.ONE,
                    BigInteger.valueOf(2)
            ));

            assertDoesNotThrow(() -> {
                var combinations = result.stream().toList();
                assertEquals(3, combinations.size());
            });
        }
    }

    /**
     * Domains whose size exceeds the range of a long (2^63). Ranks, samples and choices must work across the
     * whole range, not just the part representable as a long.
     */
    @Nested
    public class LargeDomainTests {

        private static final int N = 100;
        private static final int R = 100;   // C(199, 100): about 2^195 combinations
        private final BigInteger two63 = BigInteger.ONE.shiftLeft(63);

        /** Independent rank: number of combinations lexicographically smaller than the given indices. */
        private BigInteger rankOf(List<Integer> combination) {
            BigInteger rank = BigInteger.ZERO;
            int previous = 0;
            for (int i = 0; i < R; i++) {
                for (int j = previous; j < combination.get(i); j++) {
                    rank = rank.add(calculator.nCrRepetitive(N - j, R - i - 1));
                }
                previous = combination.get(i);
            }
            return rank;
        }

        private void assertValidCombination(List<Integer> combination) {
            assertEquals(R, combination.size());
            for (int i = 0; i < R; i++) {
                assertTrue(combination.get(i) >= 0 && combination.get(i) < N, "value out of range: " + combination);
                if (i > 0) {
                    assertTrue(combination.get(i) >= combination.get(i - 1), "not non-decreasing: " + combination);
                }
            }
        }

        @Test
        void domainIsLargerThanLong() {
            assertTrue(combination.repetitive(N, R).count().bitLength() > 64);
        }

        @Test
        void byRanks_firstAndLastRankOfLargeDomain() {
            BigInteger last = combination.repetitive(N, R).count().subtract(BigInteger.ONE);

            var result = combination.repetitive(N, R).byRanks(of(BigInteger.ZERO, last)).stream().toList();

            assertEquals(Collections.nCopies(R, 0), result.get(0));
            assertEquals(Collections.nCopies(R, N - 1), result.get(1));
        }

        @Test
        void byRanks_ranksAroundAndBeyondLongRange_roundTrip() {
            BigInteger count = combination.repetitive(N, R).count();
            List<BigInteger> ranks = new ArrayList<>(List.of(
                    two63.subtract(BigInteger.ONE),
                    two63,
                    two63.add(BigInteger.ONE),
                    BigInteger.ONE.shiftLeft(64),
                    BigInteger.ONE.shiftLeft(70),
                    count.shiftRight(1),
                    count.subtract(BigInteger.ONE)));
            Random random = new Random(21);
            for (int i = 0; i < 50; i++) {
                ranks.add(new BigInteger(count.bitLength() + 4, random).mod(count));
            }

            var result = combination.repetitive(N, R).byRanks(ranks).stream().toList();

            assertEquals(ranks.size(), result.size());
            for (int i = 0; i < ranks.size(); i++) {
                assertValidCombination(result.get(i));
                assertEquals(ranks.get(i), rankOf(result.get(i)), "rank " + ranks.get(i));
            }
        }

        @Test
        void byRanks_rankEqualToCount_isStillRejected() {
            BigInteger count = combination.repetitive(N, R).count();
            var result = combination.repetitive(N, R).byRanks(of(count));

            assertThrows(IllegalArgumentException.class, () -> result.stream().toList());
        }

        @Test
        void sample_fromLargeDomain_returnsDistinctValidCombinations() {
            var result = combination.repetitive(N, R).sample(25, new Random(7)).stream().toList();

            assertEquals(25, result.size());
            result.forEach(this::assertValidCombination);
            assertEquals(25, new HashSet<>(result).size());
        }

        @Test
        void choice_fromLargeDomain_returnsValidCombinations() {
            var result = combination.repetitive(N, R).choice(25, new Random(7)).stream().toList();

            assertEquals(25, result.size());
            result.forEach(this::assertValidCombination);
        }
    }
}
