/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.generator.product.simple;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SimpleProductBuilder, covering all, mth, choice, and sample operations.
 */
public class SimpleCartesianProductTest {

    @Nested
    class All {
        @Test
        void shouldGenerateAllElementsOfCartesianProduct() {
            var expected = List.of(
                    List.of(0, 'A', 1), List.of(0, 'A', 2), List.of(0, 'A', 3),
                    List.of(0, 'B', 1), List.of(0, 'B', 2), List.of(0, 'B', 3),
                    List.of(1, 'A', 1), List.of(1, 'A', 2), List.of(1, 'A', 3),
                    List.of(1, 'B', 1), List.of(1, 'B', 2), List.of(1, 'B', 3)
            );
            var list = cartesianProduct
                    .simpleProductOf(List.of(0, 1))
                    .and(A_B)
                    .and(num_1_2_3)
                    .lexOrder().stream().toList();
            assertIterableEquals(expected, list);
        }
    }

    @Nested
    class Mth {
        @Test
        void shouldGenerateOutputSimilarToRepetitivePermMthWhenProductIsWithSameListMultipleTimes() {
            var decimalDigits = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            for (int m = 2; m < 1000; m += 2) {
                var result = cartesianProduct.simpleProductOf(decimalDigits)
                        .and(decimalDigits)
                        .and(decimalDigits)
                        .lexOrderMth(m, 0).stream().toList();
                var expected = permutation.repetitive(3, decimalDigits)
                        .lexOrderMth(m, 0).stream().toList();
                assertEquals(expected, result);
            }
        }

        @Test
        void shouldGenerateOutputSimilarToRepetitivePermWhenMEquals1AndProductIsWithSameListMultipleTimes() {
            var result = cartesianProduct.simpleProductOf(A_B_C)
                    .and(A_B_C)
                    .and(A_B_C)
                    .lexOrderMth(1, 0).stream().toList();
            var expected = permutation.repetitive(3, A_B_C)
                    .lexOrder().stream().toList();
            assertIterableEquals(expected, result);
        }

        @Test
        void shouldGenerateCorrectMthOutputRelativeToListOfAllCartesianValues() {
            int max = A_B_C.size() * num_1_2_3.size() * num_1_to_4.size();
            int start = 3;
            for (int m = 2; m <= max / 2; m++) {
                var builder = cartesianProduct
                        .simpleProductOf(A_B_C)
                        .and(num_1_2_3)
                        .and(num_1_to_4);
                var all = builder.lexOrder().stream();
                var mth = builder.lexOrderMth(m, start).stream();
                assertEveryMthValue(all, mth, start, m);
            }
        }

        @Test
        void shouldGenerateCorrectOutputForDifferentStartPositions() {
            var builder = cartesianProduct.simpleProductOf(A_B_C).and(num_1_2_3);
            assertEquals(
                    List.of(List.of('A', 1), List.of('A', 3), List.of('B', 2), List.of('C', 1), List.of('C', 3)),
                    builder.lexOrderMth(2, 0).stream().toList()
            );
            assertEquals(
                    List.of(List.of('A', 2), List.of('B', 1), List.of('B', 3), List.of('C', 2)),
                    builder.lexOrderMth(2, 1).stream().toList()
            );
            assertEquals(
                    List.of(List.of('B', 1), List.of('B', 3), List.of('C', 2)),
                    builder.lexOrderMth(2, 3).stream().toList()
            );
        }
    }

    @Nested
    class Choice {
        @Test
        void shouldGenerateRandomChoice() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of("X", "Y"))
                    .choice(3);
            var list = product.stream().toList();
            assertEquals(3, list.size());
            for (var item : list) {
                assertEquals(2, item.size());
                assertTrue(List.of("A", "B").contains(item.get(0)));
                assertTrue(List.of("X", "Y").contains(item.get(1)));
            }
        }

        @Test
        void shouldGenerateSingleListChoice() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1, 2)).choice(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            for (var item : list) {
                assertEquals(1, item.size());
                assertTrue(List.of(0, 1, 2).contains(item.get(0)));
            }
        }
    }

    @Nested
    class Sample {
        @Test
        void shouldGenerateRandomSample() {
            var product = cartesianProduct.simpleProductOf(List.of("A", "B"))
                    .and(List.of("X", "Y"))
                    .sample(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            assertEquals(2, list.stream().distinct().count()); // Ensure uniqueness
            for (var item : list) {
                assertEquals(2, item.size());
                assertTrue(List.of("A", "B").contains(item.get(0)));
                assertTrue(List.of("X", "Y").contains(item.get(1)));
            }
        }

        @Test
        void shouldGenerateSingleListSample() {
            var product = cartesianProduct.simpleProductOf(List.of(0, 1, 2)).sample(2);
            var list = product.stream().toList();
            assertEquals(2, list.size());
            assertEquals(2, list.stream().distinct().count());
            for (var item : list) {
                assertEquals(1, item.size());
                assertTrue(List.of(0, 1, 2).contains(item.get(0)));
            }
        }
    }
}