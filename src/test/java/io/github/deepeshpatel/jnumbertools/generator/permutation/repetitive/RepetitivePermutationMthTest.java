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
        var exp = assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(-1, "A", "B")
                .lexOrderMth(1, 0)
                .stream()
                .toList());
        assertEquals(exp.getMessage(), "Width (r) cannot be negative for repetitive permutation generation");
    }

    @Test
    void testBoundaryConditionsForStartingValue() {

        var repetitivePerm = permutation.repetitive(2, "A", "B");

        var exp  = assertThrows(IllegalArgumentException.class, () -> repetitivePerm
                .lexOrderMth(1, -1)
                .stream()
                .toList());

        assertEquals(exp.getMessage(), "Start rank must be non-negative");

        //should return empty list if start rank is greater than total permutations
        var output = repetitivePerm.lexOrderMth(1,10).stream().toList();
        assertTrue(output.isEmpty());
    }

    @Test
    void shouldWorkForEmptyElementList() {
        //by the definition of exponentiation, for n=0 and k>0 0^k = 0
        //hence empty input should me allowed and the result is the empty collection
        var output = permutation.repetitive(2, Collections.emptyList())
                .lexOrderMth(1, 0)
                .stream()
                .toList();
        assertTrue(output.isEmpty());
    }

    @Test
    void shouldThrowExceptionForNegativeIncrement() {
        assertThrows(IllegalArgumentException.class, () -> permutation.repetitive(2, "A", "B")
                .lexOrderMth(-1, 0)
                .stream()
                .toList(), "Negative increment should throw exception");
    }

    @Test
    void shouldThrowExceptionForZeroAndNegativeIncrement() {
        List<String> elements = of("A", "B");
        int width = 2;
        var repetitivePerm = permutation.repetitive(width, elements);

        var exp1 = assertThrows(IllegalArgumentException.class, () -> repetitivePerm
                .lexOrderMth(0, 0)
                .stream()
                .toList());

        var exp2= assertThrows(IllegalArgumentException.class, () -> repetitivePerm
                .lexOrderMth(-1, 0)
                .stream()
                .toList());

        assertEquals(exp1.getMessage(), "Increment 'm' must be positive");
        assertEquals(exp2.getMessage(), "Increment 'm' must be positive");
    }

    @Test
    void shouldHandleLargeMWithNonZeroStart() {
        List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int width = 12;
        BigInteger largeM = BigInteger.valueOf(200_000_000_000L);
        BigInteger start = BigInteger.valueOf(100_000_000_000L);

        var permutations = permutation.repetitive(width, input)
                .lexOrderMth(largeM, start)
                .stream()
                .toList();

        // Just verify it doesn't throw and returns something
        assertNotNull(permutations);
        assertFalse(permutations.isEmpty());
    }

    @Test
    void shouldHandleIncrementLargerThanTotal() {
        int width = 2;
        int n = 2; // 2^2 = 4 total
        var result = permutation.repetitive(width, 0, 1)
                .lexOrderMth(10, 0) // increment > total
                .stream()
                .toList();

        assertEquals(1, result.size(), "Should return only first permutation");
        assertEquals(of(0, 0), result.get(0));
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