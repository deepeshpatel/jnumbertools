/*
 * JNumberTools Library v3.0.2
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.permutation.derangement;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DerangementByRanks} covering mᵗʰ derangements, choice, sample
 * and byRanks strategies.
 */
class DerangementByRanksTest {

    @Nested
    public class DerangementMthTest {

        @Test
        void assertCount() {
            // !n with mᵗʰ: ceil(!n / m)
            for (int n = 2; n < 7; n++) {
                long total = calculator.subFactorial(n).longValue();
                for (int m = 1; m <= 4; m++) {
                    long size = derangement.of(n).lexOrderMth(m, 0).stream().count();
                    double expected = Math.ceil(total / (double) m);
                    assertEquals((long) expected, size, "n=" + n + ", m=" + m);
                }
            }
        }

        @Test
        void assertCountAndContentForN0() {
            // !0 = 1, the only derangement is the empty list, accessible at rank 0
            var zero = derangement.of(0).lexOrderMth(1, 0).stream().toList();
            assertEquals(1, zero.size());
            assertTrue(zero.get(0).isEmpty());
        }

        @Test
        void shouldHandleN1ForLexOrderMth() {
            // !1 = 0; iteration must yield nothing for any (m, start)
            // valid against the shared validator (consistent with the way
            // other builders treat empty domains).
            var empty = derangement.of(List.of("A")).lexOrderMth(1, 0)
                    .stream().toList();
            assertTrue(empty.isEmpty());

            // Negative start is still rejected
            assertThrows(IllegalArgumentException.class,
                    () -> derangement.of(List.of("A"))
                            .lexOrderMth(java.math.BigInteger.ONE,
                                         java.math.BigInteger.valueOf(-1)));
        }

        @Test
        void shouldReturnSameResultForDifferentIteratorObjects() {
            var iterable = derangement.of("A", "B", "C", "D").lexOrderMth(3, 0);
            var lists1 = iterable.stream().toList();
            var lists2 = iterable.stream().toList();
            assertIterableEquals(lists1, lists2);
        }

        @Test
        void shouldYieldEveryMthValueOfLexOrder() {
            // n=5 → D_5 = 44; sweep several (m, start) pairs
            int n = 5;
            int start = 3;
            for (int m = 1; m <= 16; m += 2) {
                var all = derangement.of(n).lexOrder();
                var mth = derangement.of(n).lexOrderMth(m, start);
                assertEveryMthValue(all.stream(), mth.stream(), start, m);
            }
        }

        @Test
        void shouldGenerateMthDerangementsForN4() {
            // D_4 = 9, step m=3 starting at 0 → ranks 0, 3, 6
            var lex = derangement.of("A", "B", "C", "D").lexOrder().stream().toList();
            var mth = derangement.of("A", "B", "C", "D").lexOrderMth(3, 0).stream().toList();
            assertEquals(3, mth.size());
            assertEquals(lex.get(0), mth.get(0));
            assertEquals(lex.get(3), mth.get(1));
            assertEquals(lex.get(6), mth.get(2));
        }
    }

    @Nested
    public class DerangementByRanksValidationTest {

        @Test
        void byRanks_withValidRanks_shouldWork() {
            var result = derangement.of("A", "B", "C", "D").byRanks(of(
                    BigInteger.ZERO,
                    BigInteger.valueOf(4),
                    BigInteger.valueOf(8)));
            var list = result.stream().toList();
            assertEquals(3, list.size());
        }

        @Test
        void byRanks_withNegativeRank_shouldThrowDuringIteration() {
            var result = derangement.of("A", "B", "C", "D")
                    .byRanks(of(BigInteger.valueOf(-1)));
            assertThrows(IllegalArgumentException.class, () -> result.stream().toList());
        }

        @Test
        void byRanks_withOutOfBoundRank_shouldThrowDuringIteration() {
            // D_4 = 9, so rank 9 is out of range
            var result = derangement.of("A", "B", "C", "D")
                    .byRanks(of(BigInteger.valueOf(9)));
            assertThrows(IllegalArgumentException.class, () -> result.stream().toList());
        }

        @Test
        void byRanks_forN1_anyRankShouldThrow() {
            // D_1 = 0; any rank is out of range
            var result = derangement.of(List.of("A")).byRanks(of(BigInteger.ZERO));
            assertThrows(IllegalArgumentException.class, () -> result.stream().toList());
        }

        @Test
        void byRanks_forN0_rank0DecodesToEmpty() {
            var result = derangement.of(List.<String>of()).byRanks(of(BigInteger.ZERO));
            var list = result.stream().toList();
            assertEquals(1, list.size());
            assertTrue(list.get(0).isEmpty());
        }
    }

    @Nested
    public class DerangementChoiceTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int sampleSize = 4;
            var samples = derangement.of("A", "B", "C", "D")
                    .choice(sampleSize, TestBase.random)
                    .stream().toList();
            assertEquals(sampleSize, samples.size());
        }

        @Test
        void shouldAllowDuplicates() {
            // D_3 = 2, so sample of 6 with replacement must contain duplicates
            int sampleSize = 6;
            var samples = derangement.of("A", "B", "C")
                    .choice(sampleSize, TestBase.random)
                    .stream().toList();
            assertEquals(sampleSize, samples.size());
            int distinct = new HashSet<>(samples).size();
            assertTrue(distinct <= 2, "Distinct count should not exceed D_3 = 2");
        }

        @Test
        void shouldGenerateValidDerangements() {
            int sampleSize = 5;
            var samples = derangement.of("A", "B", "C", "D")
                    .choice(sampleSize, TestBase.random)
                    .stream().toList();
            assertEquals(sampleSize, samples.size());
            List<Character> origin = List.of('A', 'B', 'C', 'D');
            for (List<String> d : samples) {
                assertEquals(4, d.size());
                for (int i = 0; i < 4; i++) {
                    assertNotEquals(origin.get(i).toString(), d.get(i));
                }
            }
        }

        @Test
        void shouldThrowForNonPositiveSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> derangement.of("A", "B", "C", "D")
                    .choice(0, TestBase.random));
            assertThrows(IllegalArgumentException.class, () -> derangement.of("A", "B", "C", "D")
                    .choice(-1, TestBase.random));
        }
    }

    @Nested
    public class DerangementSampleTest {

        @Test
        void shouldGenerateExactSampleSize() {
            int sampleSize = 5;
            var samples = derangement.of("A", "B", "C", "D")
                    .sample(sampleSize, TestBase.random)
                    .stream().toList();
            assertEquals(sampleSize, samples.size());
        }

        @Test
        void shouldGenerateUniqueDerangements() {
            int sampleSize = 7;
            var samples = derangement.of("A", "B", "C", "D")
                    .sample(sampleSize, TestBase.random)
                    .stream().toList();
            assertEquals(sampleSize, samples.size());
            assertEquals(sampleSize, new HashSet<>(samples).size(),
                    "Sample without replacement must be all distinct");
        }

        @Test
        void shouldThrowForSampleSizeGreaterThanTotal() {
            // D_3 = 2; asking for 3 must throw
            assertThrows(IllegalArgumentException.class, () -> derangement.of("A", "B", "C")
                    .sample(3, TestBase.random));
        }

        @Test
        void shouldThrowForNonPositiveSampleSize() {
            assertThrows(IllegalArgumentException.class, () -> derangement.of("A", "B", "C", "D")
                    .sample(0, TestBase.random));
            assertThrows(IllegalArgumentException.class, () -> derangement.of("A", "B", "C", "D")
                    .sample(-1, TestBase.random));
        }

        @Test
        void shouldHandleN0() {
            // !0 = 1; a sample of size 1 must yield the lone empty derangement
            var samples = derangement.of(Collections.<String>emptyList())
                    .sample(1, TestBase.random)
                    .stream().toList();
            assertEquals(1, samples.size());
            assertTrue(samples.get(0).isEmpty());
        }
    }
}

