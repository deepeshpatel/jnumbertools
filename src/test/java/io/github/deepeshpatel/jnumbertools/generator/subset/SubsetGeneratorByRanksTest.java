/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.subset;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.A_B_C_D;
import static io.github.deepeshpatel.jnumbertools.TestBase.subsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SubsetGeneratorByRanks, covering mth, choice, and sample operations.
 */
public class SubsetGeneratorByRanksTest {

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
        void shouldThrowErrorForOutOfRangeRank() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            var error = assertThrows(IllegalArgumentException.class, () ->
                    builder.byRanks(List.of(BigInteger.valueOf(15))).stream().toList()
            );
            assertEquals(error.getMessage(), "start must be < total subsets in range (0-based)");
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
            var builder = subsets.of(A_B_C_D).inRange(2, 3).choice(5);
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
                    () -> subsets.of(A_B_C_D).inRange(1, 3).choice(0).stream().toList());
            assertTrue(exception.getMessage().contains("Sample size must be positive"));
        }

        @Test
        void shouldThrowOnNegativeChoice() {
            var builder = subsets.of(A_B_C_D).all();
            assertThrows(IllegalArgumentException.class, () -> builder.choice(-1));
        }

        @Test
        void shouldAllowDuplicatesInChoice() {
            var builder = subsets.of(List.of('A', 'B')).all();
            var result = builder.choice(10).stream().toList();
            assertEquals(10, result.size());
        }

        @Test
        void shouldWorkForEmptyElements() {
            var builder = subsets.of(List.of()).all();
            var result = builder.choice(3).stream().toList();
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
            var result = builder.sample(5).stream().toList();
            assertEquals(5, result.size());
            assertEquals(5, new HashSet<>(result).size());  // unique subsets
            for (var subset : result) {
                assertTrue(subset.size() >= 2 && subset.size() <= 3);
            }
        }

        @Test
        void shouldGenerateZeroSample() {
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> subsets.of(A_B_C_D).inRange(1, 3).sample(0).stream().toList());
            assertTrue(exception.getMessage().contains("Sample size must be positive"));
        }

        @Test
        void shouldThrowOnNegativeSample() {
            var builder = subsets.of(A_B_C_D).all();
            assertThrows(IllegalArgumentException.class, () -> builder.sample(-1));
        }

        @Test
        void shouldThrowWhenSampleSizeExceedsTotal() {
            var builder = subsets.of(A_B_C_D).inRange(3, 3);
            assertEquals(BigInteger.valueOf(4), builder.count());
            assertThrows(IllegalArgumentException.class, () -> builder.sample(5));
        }

        @Test
        void shouldGenerateAllWhenSampleSizeEqualsTotal() {
            var builder = subsets.of(A_B_C_D).inRange(3, 3);
            var result = builder.sample(4).stream().toList();
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
            var result = builder.sample(1).stream().toList();
            assertEquals(1, result.size());
            assertTrue(result.get(0).isEmpty());
        }
    }

    @Nested
    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    class Stress {

    }
}