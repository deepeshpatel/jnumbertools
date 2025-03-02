/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link RepetitivePermutationMth}, covering mᵗʰ permutation generation including large increments and corner cases.
 */
public class RepetitivePermutationMthTest {

    @Test
    void assertCount() {
        int increment = 4;
        for (int n = 1; n <= 5; n++) {
            var input = Collections.nCopies(n, 'A');
            for (int size = 0; size <= 3; size++) {
                long count = permutation.repetitive(size, input)
                        .lexOrderMth(increment, 0)
                        .stream()
                        .count();
                double expected = Math.ceil(Math.pow(n, size) / increment);
                assertEquals((long) expected, count);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation.repetitive(2, 'A', 'B', 'C')
                .lexOrderMth(2, 0);
        assertIterableEquals(iterable.stream().toList(), iterable.stream().toList());
    }

    @Test
    void shouldGenerateAllPermutationsOf2Values() {
        var expected = List.of(
                of(0, 0, 0),
                of(0, 1, 0),
                of(1, 0, 0),
                of(1, 1, 0)
        );
        var output = permutation.repetitive(3, 0, 1)
                .lexOrderMth(2, 0)
                .stream()
                .toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void testMthPermutationAgainstFixedOutput() {
        var expected = List.of(
                of("A", "A"),
                of("A", "C"),
                of("B", "B"),
                of("C", "A"),
                of("C", "C")
        );
        var output = permutation.repetitive(2, "A", "B", "C")
                .lexOrderMth(2, 0)
                .stream()
                .toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void test_start_parameter_greater_than_0() {
        var expected = List.of(
                of("B", "A"),
                of("B", "C"),
                of("C", "B")
        );
        var output = permutation.repetitive(2, "A", "B", "C")
                .lexOrderMth(2, 3)
                .stream()
                .toList();
        assertIterableEquals(expected, output);
    }

    @EnabledIfSystemProperty(named = "stress.testing", matches = "true")
    @Test
    void stressTesting() {
        int n = 4;
        int width = 1000;
        for (int x = 2000; x <= 2010; x++) {
            BigInteger totalRepetitivePerm = calculator.power(x, width).divide(BigInteger.valueOf(n));
            long count = permutation.repetitive(width, x)
                    .lexOrderMth(totalRepetitivePerm, BigInteger.ZERO)
                    .stream()
                    .count();
            assertEquals(n, count, 1);
        }
    }

    @Test
    void shouldGeneratePermutationsForVeryLargeM() {
        List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int width = 12;

        // Hardcoded expected result for very big m, verified once for correctness
        var expected = List.of(
                of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                of(2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                of(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                of(6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                of(8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        );

        BigInteger largeM = BigInteger.valueOf(200_000_000_000L); // 200 billion

        var permutations = permutation.repetitive(width, input)
                .lexOrderMth(largeM, BigInteger.ZERO)
                .stream()
                .toList();

        assertIterableEquals(expected, permutations);
    }

    @Test
    void shouldHandleZeroWidth() {
        var expected = List.of(of());
        var output = permutation.repetitive(0, "A", "B")
                .lexOrderMth(1, 0)
                .stream()
                .toList();
        assertIterableEquals(expected, output, "Should generate one empty permutation for width=0");
    }

    @Test
    void shouldThrowExceptionForNegativeWidth() {
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(-1, "A", "B")
                .lexOrderMth(1, 0)
                .stream()
                .toList(), "Negative width should throw exception");
    }

    @Test
    void shouldThrowExceptionForNegativeStart() {
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, "A", "B")
                .lexOrderMth(1, -1)
                .stream()
                .toList(), "Negative start should throw exception");
    }

    @Test
    void shouldThrowExceptionForEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, Collections.emptyList())
                .lexOrderMth(1, 0)
                .stream()
                .toList(), "Empty input should throw exception");
    }

    @Test
    void shouldThrowExceptionForNegativeIncrement() {
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, "A", "B")
                .lexOrderMth(-1, 0)
                .stream()
                .toList(), "Negative increment should throw exception");
    }

    @Test
    void shouldHandleZeroIncrement() {
        List<String> elements = of("A", "B");
        int width = 2;
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(width, elements)
                .lexOrderMth(0, 0)
                .stream()
                .toList(), "Zero increment should throw exception");
    }

    @Test
    void shouldGenerateRepetitiveMthPermutations() {
        int width =3;
        int noOfElements = 5;
        int start = 2;
        for(int m=1; m<=32; m++) {
            var all =      permutation.repetitive(width, noOfElements).lexOrder().stream();
            var everyMth = permutation.repetitive(width, noOfElements).lexOrderMth(m, start).stream();
            assertEveryMthValue(all, everyMth, start, m);
        }
    }
}