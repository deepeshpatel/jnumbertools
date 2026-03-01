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

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SubsetGeneratorByRanks, covering mth, choice, and sample operations.
 */
public class SubsetGeneratorByRanksTest {

    @Nested
    class Mth {
        @Test
        void shouldGenerateCorrectMthValueForAllSubsets() {
            var builder = subsets.of(A_B_C_D).all();
            int m = 5;
            int start = 3;
            var expected = List.of(List.of('C'), List.of('B', 'C'), List.of('A', 'C', 'D'));
            var result = builder.lexOrderMth(m, start).stream().toList();
            assertIterableEquals(expected, result);
        }

        @Test
        void shouldGenerateCorrectMthValueForRangedSubsets() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            int m = 5;
            int start = 3;
            var expected = List.of(List.of('B', 'C'), List.of('A', 'C', 'D'));
            var result = builder.lexOrderMth(m, start).stream().toList();
            assertIterableEquals(expected, result);
        }

        @Test
        void shouldGenerateEmptyForStartGreaterThanTotal() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            var result = builder.lexOrderMth(5, 10).stream().toList();
            assertTrue(result.isEmpty());
        }

        @Test
        void shouldGenerateCorrectWithLargeM() {
            var builder = subsets.of(A_B_C_D).all();
            var result = builder.lexOrderMth(10, 2).stream().toList();
            assertIterableEquals(List.of(List.of('B'), List.of('A', 'B', 'D')), result);
        }

        @Test
        void shouldThrowOnNegativeM() {
            var builder = subsets.of(A_B_C_D).inRange(1, 3);
            assertThrows(IllegalArgumentException.class, () -> builder.lexOrderMth(BigInteger.valueOf(-1), BigInteger.ZERO));
        }

        @Test
        void shouldGenerateForMEqualsOne() {
            var builder = subsets.of(A_B_C_D).inRange(2, 2);
            var result = builder.lexOrderMth(1, 0).stream().toList();
            assertEquals(6, result.size());
            assertIterableEquals(List.of(List.of('A', 'B'), List.of('A', 'C'), List.of('A', 'D'), List.of('B', 'C'), List.of('B', 'D'), List.of('C', 'D')), result);
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoice() {
            var builder = subsets.of(A_B_C_D).inRange(2, 3);
            var result = builder.choice(5).stream().toList();
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