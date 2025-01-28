package io.github.deepeshpatel.jnumbertools.generator.permutation.repetitive;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class RepetitivePermutationMthTest {

    @Test
    void assertCount() {
        int increment = 4;
        for (int n = 1; n <= 5; n++) {
            var input = Collections.nCopies(n, 'A');
            for (int size = 0; size <= 3; size++) {
                long count = permutation.repetitive(size, input)
                        .lexOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(Math.pow(n, size) / increment);
                assertEquals((long) expected, count);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation
                .repetitive(2, 'A', 'B', 'C')
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
                .stream().toList();

        assertIterableEquals(expected, output);
    }

    @Test
    void shouldGenerateRepetitiveMthPermutations() {
        var expected = List.of(
                of("A", "A"),
                of("A", "C"),
                of("B", "B"),
                of("C", "A"),
                of("C", "C")
        );
        var output = permutation.repetitive(2, "A", "B", "C")
                .lexOrderMth(2, 0).stream().toList();

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
                .lexOrderMth(2, 3).stream().toList();
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
                    .stream().count();
            assertEquals(n, count, 1);
        }
    }
}
