/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SubsetGeneratorByRanks, covering mth, choice, and sample operations.
 */
public class SubsetGeneratorByRanksTest {

    @Nested
    public class SubsetGeneratorMthTest {

        @Test
        void assertCount() {
            // 2ⁿ with mᵗʰ: 2ⁿ/m
            int increment = 3;
            int start = 0;

            // Test all subsets
            for (int n = 1; n <= 5; n++) {
                var input = num_0_to_5.subList(0, n);
                long count = subsets.of(input).all()
                        .lexOrderMth(increment, start)
                        .stream()
                        .count();
                double expected = Math.ceil(Math.pow(2, n) / increment);
                assertEquals((long) expected, count);
            }

            // Test range subsets using correct calculator method
            for (int from = 1; from <= 3; from++) {
                for (int to = from; to <= 4; to++) {
                    var input = num_0_to_5.subList(0, 5);
                    long count = subsets.of(input).inRange(from, to)
                            .lexOrderMth(increment, start)
                            .stream()
                            .count();
                    long totalRange = calculator.totalSubsetsInRange(from, to, input.size()).longValue();
                    double expected = Math.ceil(totalRange / (double) increment);
                    assertEquals((long) expected, count);
                }
            }
        }

        @Test
        void assertCountAndContentForSpecialCase() {
            // Case 1: n=0, range [0,0] -> 1 -> should return [[]]
            var zeroZeroGenerator = subsets.of(Collections.emptyList()).inRange(0, 0).lexOrderMth(1, 0);
            var zeroZeroResult = zeroZeroGenerator.stream().toList();
            assertEquals(1, zeroZeroResult.size());
            assertTrue(zeroZeroResult.get(0).isEmpty());

            // Case 2: n=0, range [0,2] -> only empty subset exists -> should return [[]]
            var zeroRangeGenerator = subsets.of(Collections.emptyList()).inRange(0, 2).lexOrderMth(1, 0);
            var zeroRangeResult = zeroRangeGenerator.stream().toList();
            assertEquals(1, zeroRangeResult.size());
            assertTrue(zeroRangeResult.get(0).isEmpty());

            // Case 3: n=0, range [1,2] -> no non-empty subsets -> should return [] (empty iterator)
            var zeroPositiveGenerator = subsets.of(Collections.emptyList()).inRange(1, 2).lexOrderMth(1, 0);
            assertTrue(zeroPositiveGenerator.stream().toList().isEmpty());

            // Case 4: n>0, range [0,0] -> one empty subset -> should return [[]]
            var positiveZeroGenerator = subsets.of(A_B_C_D).inRange(0, 0).lexOrderMth(1, 0);
            var positiveZeroResult = positiveZeroGenerator.stream().toList();
            assertEquals(1, positiveZeroResult.size());
            assertTrue(positiveZeroResult.get(0).isEmpty());

            // Case 5: With m>1, should still respect count=0
            var zeroPositiveWithMthGenerator = subsets.of(Collections.emptyList()).inRange(1, 2).lexOrderMth(3, 0);
            assertTrue(zeroPositiveWithMthGenerator.stream().toList().isEmpty());
        }

        @Test
        void shouldGenerate_AllSubsets_For_4Elements_and_M_Equals1() {

            var iter_expected = subsets.of(A_B_C_D).all().lexOrder().iterator();
            var iter_testResult = subsets.of(A_B_C_D).all().lexOrderMth(1, 0).iterator();
            while (iter_expected.hasNext()) {
                assertEquals(iter_expected.next(), iter_testResult.next());
            }
        }

        @Test
        void should_generate_all_subsets_for_6elements_and_different_m_for_range_3_6() {
            int start = 0;
            for (int from = 1; from < num_0_to_5.size(); from++) {
                for (int to = from; to < num_0_to_5.size(); to++) {
                    for (int m = 1; m < 6; m++) {
                        var all = subsets.of(num_0_to_5).inRange(from, to).lexOrder().stream();
                        var mth = subsets.of(num_0_to_5).inRange(from, to).lexOrderMth(m, start).stream();
                        assertEveryMthValue(all, mth, start, m);
                    }
                }
            }
        }

        @Test
        void should_generate_all_subsets_for_4elements_and_different_m() {
            int start = 3;
            for (int m = 1; m <= 15; m+=2) {
                var all = subsets.of(A_B_C_D).all().lexOrder().stream();
                var mth = subsets.of(A_B_C_D).all().lexOrderMth(m, start).stream();
                assertEveryMthValue(all, mth, start, m);
            }
        }

        @Test
        void test_start_parameter_greater_than_0() {
            var builder = subsets.of(A_B_C_D);
            var listOfAll = builder.all()
                    .lexOrderMth(5, 3).stream().toList();
            assertEquals("[[C], [B, C], [A, C, D]]", listOfAll.toString());

            var listOfRange = builder.inRange(2, 3)
                    .lexOrderMth(5, 3).stream().toList();
            assertEquals("[[B, C], [A, C, D]]", listOfRange.toString());
        }
    }

    @Nested
    class CustomRanks {

        @Test
        void shouldGenerateSingleRank() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            var result = builder.byRanks(List.of(BigInteger.valueOf(3))).stream().toList();
            assertEquals(1, result.size());
            assertEquals(List.of('B', 'C'), result.get(0));
        }

        @Test
        void shouldGenerateMultipleCustomRanks() {
            var builder = subsets.of(A_B_C_D).all();
            var ranks = List.of(BigInteger.ZERO, BigInteger.valueOf(3), BigInteger.valueOf(10));
            var result = builder.byRanks(ranks).stream().toList();
            assertEquals(3, result.size());
            assertEquals(List.of(), result.get(0));           // rank 0 = empty
            assertEquals(List.of('C'), result.get(1));        // rank 3 = [C]
            assertEquals(List.of('C', 'D'), result.get(2)); // rank 10 = [A,B,D]
        }

        @Test
        void shouldWorkWithEmptyRankSequence() {
            var builder = subsets.of(A_B_C_D).all();
            var result = builder.byRanks(List.of()).stream().toList();
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoice() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3).choice(5, TestBase.random);
            var result = builder.stream().toList();
            assertEquals(5, result.size());
            for (var subset : result) {
                assertTrue(subset.size() >= 2 && subset.size() <= 3);
                assertEquals(subset.size(), new HashSet<>(subset).size());  // no duplicates in subset
            }
        }

        @Test
        void shouldGenerateZeroChoice() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> subsets.of(A_B_C_D).inRange(1, 3).choice(0, TestBase.random).stream().toList());
            assertTrue(exception.getMessage().contains("Sample size must be positive"));
        }

        @Test
        void shouldThrowOnNegativeChoice() {
            var builder = subsets.of(A_B_C_D).all();
            assertThrows(IllegalArgumentException.class, () -> builder.choice(-1, TestBase.random));
        }

        @Test
        void shouldAllowDuplicatesInChoice() {
            var builder = subsets.of(List.of('A', 'B')).all();
            var result = builder.choice(10, TestBase.random).stream().toList();
            assertEquals(10, result.size());
        }

        @Test
        void shouldWorkForEmptyElements() {
            var builder = subsets.of(List.of()).all();
            var result = builder.choice(3, TestBase.random).stream().toList();
            assertEquals(3, result.size());
            for (var subset : result) {
                assertTrue(subset.isEmpty());
            }
        }
    }

    @Nested
    class Sample {
        @Test
        void shouldGenerateUniqueRandomSample() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            var result = builder.sample(5, TestBase.random).stream().toList();
            assertEquals(5, result.size());
            assertEquals(5, new HashSet<>(result).size());  // unique subsets
            for (var subset : result) {
                assertTrue(subset.size() >= 2 && subset.size() <= 3);
            }
        }

        @Test
        void shouldGenerateZeroSample() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> subsets.of(A_B_C_D).inRange(1, 3).sample(0, TestBase.random).stream().toList());
            assertTrue(exception.getMessage().contains("Sample size must be positive"));
        }

        @Test
        void shouldThrowOnNegativeSample() {
            var builder = subsets.of(A_B_C_D).all();
            assertThrows(IllegalArgumentException.class, () -> builder.sample(-1, random));
        }

        @Test
        void shouldThrowWhenSampleSizeExceedsTotal() {
            var builder = subsets.of(A_B_C_D).inRange(3, 3);
            assertEquals(BigInteger.valueOf(4), builder.count());
            assertThrows(IllegalArgumentException.class, () -> builder.sample(5, TestBase.random));
        }

        @Test
        void shouldGenerateAllWhenSampleSizeEqualsTotal() {
            var builder = subsets.of(A_B_C_D).inRange(3, 3);
            var result = builder.sample(4, TestBase.random).stream().toList();
            assertEquals(4, result.size());
            assertEquals(4, new HashSet<>(result).size());
            assertTrue(result.contains(List.of('A', 'B', 'C')));
            assertTrue(result.contains(List.of('A', 'B', 'D')));
            assertTrue(result.contains(List.of('A', 'C', 'D')));
            assertTrue(result.contains(List.of('B', 'C', 'D')));
        }

        @Test
        void shouldWorkForEmptyElements() {
            var builder = subsets.of(List.of()).all();
            assertEquals(BigInteger.ONE, builder.count());
            var result = builder.sample(1, TestBase.random).stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }
    }

    // ---- Rules from ValidationRules.md: byRanks / lexOrderMth traversal rules ----

    @Nested
    class RankRules {

        private BigInteger big(long value) {
            return BigInteger.valueOf(value);
        }

        @Test
        void invalidRanksAreReportedLazilyWhenReached() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);   // count = 6 + 4 = 10

            // building the generator does not look at the ranks, only iterating does
            var outOfRange = builder.byRanks(List.of(big(0), big(10)));
            var iterator = outOfRange.iterator();
            assertEquals(List.of('A', 'B'), iterator.next());
            assertThrows(IllegalArgumentException.class, iterator::next);

            var negative = builder.byRanks(List.of(big(1), big(-1))).iterator();
            negative.next();
            assertThrows(IllegalArgumentException.class, negative::next);
        }

        @Test
        void rankSequenceIsConsumedLazilyOneRankAtATime() {
            AtomicInteger pulled = new AtomicInteger();
            Iterable<BigInteger> counting = () -> new Iterator<>() {
                public boolean hasNext() { return true; }          // never ends
                public BigInteger next() { pulled.incrementAndGet(); return BigInteger.ZERO; }
            };

            var generator = subsets.of(A_B_C_D).all().byRanks(counting);
            assertEquals(0, pulled.get(), "ranks must not be scanned when the generator is created");

            var iterator = generator.iterator();
            assertEquals(0, pulled.get(), "ranks must not be scanned when iteration starts");
            iterator.next();
            iterator.next();
            assertEquals(2, pulled.get());

            // an endless rank source can be cut short by the caller
            assertEquals(5, subsets.of(A_B_C_D).all().byRanks(counting).stream().limit(5).count());
        }

        @Test
        void everyRankOfEveryRangeMatchesLexOrder() {
            for (int n = 0; n <= 5; n++) {
                List<Integer> list = IntStream.range(0, n).boxed().toList();
                for (int from = 0; from <= 6; from++) {
                    for (int to = from; to <= 6; to++) {
                        if (n > 0 && to > n) continue;
                        var builder = subsets.of(list).inRange(from, to);
                        var expected = builder.lexOrder().stream().toList();
                        var ranks = IntStream.range(0, expected.size()).mapToObj(BigInteger::valueOf).toList();

                        String where = "n=" + n + ", range [" + from + "," + to + "]";
                        assertEquals(builder.count().intValue(), expected.size(), where);
                        assertEquals(expected, builder.byRanks(ranks).stream().toList(), where);
                    }
                }
            }
        }

        @Test
        void emptyDomainRejectsEveryRankButAcceptsNoRanks() {
            var emptyDomain = subsets.of(Collections.emptyList()).inRange(1, 2);   // count = 0

            assertTrue(emptyDomain.byRanks(List.of()).stream().toList().isEmpty());
            var generator = emptyDomain.byRanks(List.of(BigInteger.ZERO));
            assertThrows(IllegalArgumentException.class, () -> generator.stream().toList());
        }

        @Test
        void domainWithOnlyTheEmptySubsetYieldsOneEmptyListPerRank() {
            // n = 0 with [0, m], and n > 0 with [0, 0]: count = 1, [[]]
            var domains = List.of(
                    subsets.of(Collections.emptyList()).all(),
                    subsets.of(Collections.emptyList()).inRange(0, 3),
                    subsets.of(A_B_C_D).inRange(0, 0));

            for (var domain : domains) {
                var zeros = List.of(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
                assertEquals(List.of(List.of(), List.of(), List.of()), domain.byRanks(zeros).stream().toList());

                // every rank must be 0
                var withOne = domain.byRanks(List.of(BigInteger.ZERO, BigInteger.ONE));
                assertThrows(IllegalArgumentException.class, () -> withOne.stream().toList());
            }
        }

        @Test
        void byRanksIsReIterable() {
            var generator = subsets.of(A_B_C_D).all().byRanks(List.of(big(3), big(0), big(15)));
            var first = generator.stream().toList();
            assertEquals(first, generator.stream().toList());
            assertEquals(List.of(List.of('C'), List.of(), List.of('A', 'B', 'C', 'D')), first);
        }

        @Test
        void lexOrderMthOnByRanksGeneratorValidatesItsParameters() {
            var generator = subsets.of(A_B_C_D).inRange(2, 3).byRanks(List.of());   // count = 10

            assertThrows(IllegalArgumentException.class, () -> generator.lexOrderMth(BigInteger.ZERO, BigInteger.ZERO));
            assertThrows(IllegalArgumentException.class, () -> generator.lexOrderMth(BigInteger.ONE, big(-1)));
            assertThrows(IllegalArgumentException.class, () -> generator.lexOrderMth(BigInteger.ONE, big(10)));
            assertEquals(10, generator.lexOrderMth(BigInteger.ONE, BigInteger.ZERO).stream().count());
        }

        @Test
        void buildReturnsTheStartSubsetAndFailsOnAnEmptyRange() {
            assertEquals(List.of('B', 'C'),
                    subsets.of(A_B_C_D).inRange(2, 3).lexOrderMth(BigInteger.ONE, big(3)).build());

            var emptyRange = subsets.of(Collections.emptyList()).inRange(1, 2).lexOrderMth(BigInteger.ONE, BigInteger.ZERO);
            assertThrows(NoSuchElementException.class, emptyRange::build);
        }

        @Test
        void nextAfterTheLastResultThrows() {
            var mth = subsets.of(A_B).all().lexOrderMth(big(2), BigInteger.ZERO).iterator();   // ranks 0 and 2
            assertEquals(List.of(), mth.next());
            assertEquals(List.of('B'), mth.next());
            assertFalse(mth.hasNext());
            assertThrows(NoSuchElementException.class, mth::next);

            var ranked = subsets.of(A_B).all().byRanks(List.of(BigInteger.ONE)).iterator();
            ranked.next();
            assertFalse(ranked.hasNext());
            assertThrows(NoSuchElementException.class, ranked::next);
        }
    }

}