package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class KPermutationLexOrderMthTest {

    @Test
    void assertCount() {
        int increment = 3;
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .lexOrderMth(increment, 0)
                        .stream().count();

                double expected = Math.ceil(calculator.nPr(n, k).longValue() / (double) increment);
                assertEquals((long) expected, size);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation.nPk(2, "A", "B", "C")
                .lexOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateMthKPermutations() {

        var input = of("A", "B", "C", "D", "E", "F", "G");
        for (int k = 1; k <= input.size() / 2; k++) {
            for (int increment = 1; increment <= 5; increment++) {
                var expected = getExpectedResultViaOneByOneIteration(input, k, increment);
                var output = getResultViaDirectIncrement(input, k, increment);
                assertIterableEquals(expected, output);
            }
        }
    }

    @Test
    void shouldSupportVeryLargeMthKPermutation() {

        // Find every 10^29th 20-Permutation of 40 elements (20P40)
        BigInteger increment = new BigInteger("100000000000000000000000000000");
        var expected = of(
                of(11, 37, 6, 5, 26, 15, 0, 25, 9, 22, 27, 16, 12, 21, 24, 31, 33, 34, 17, 38),
                of(23, 34, 12, 11, 10, 28, 1, 8, 18, 3, 9, 30, 24, 0, 6, 20, 26, 27, 37, 35),
                of(35, 30, 18, 16, 38, 1, 2, 32, 23, 22, 33, 3, 29, 17, 26, 6, 12, 14, 11, 28)
        );

        var permutations = permutation.nPk(40, 20)
                .lexOrderMth(increment, increment)
                .stream().toList();
        assertIterableEquals(expected, permutations);
    }

    @Test
    void testStartParameterGreaterThanZero() {
        var expected = List.of(
                of('a', 'c', 'e'),
                of('c', 'a', 'd'),
                of('d', 'e', 'a')
        );
        var output = permutation.nPk(3, 'a', 'b', 'c', 'd', 'e')
                .lexOrderMth(20, 5)
                .stream().toList();
        assertIterableEquals(expected, output);
    }

    private List<?> getResultViaDirectIncrement(List<?> input, int k, int increment) {
        return permutation.nPk(k, input)
                .lexOrderMth(increment, 0)
                .stream().toList();
    }

    private List<?> getExpectedResultViaOneByOneIteration(List<?> input, int k, int increment) {
        var stream = permutation.nPk(k, input)
                .lexOrder().stream();

        return everyMthValue(stream, increment);
    }
}
