package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class MultisetPermutationMthTest {

    @Test
    void assertCount() {
        Random random = new Random(System.currentTimeMillis());
        int increment = 4;
        for (int n = 4; n <= 6; n++) {
            var input = Collections.nCopies(n, 'A');
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            long count = permutation.multiset(input, frequency)
                    .lexOrderMth(increment, 0).stream().count();

            int sum = Arrays.stream(frequency).reduce(0, Integer::sum);
            double numerator = calculator.factorial(sum).longValue();
            long denominator = increment * IntStream.of(frequency).asLongStream()
                    .reduce(1, (a, b) -> (a * calculator.factorial((int) b).longValue()));

            double expected = Math.ceil(numerator / denominator);
            //( ∑ ai.si)! / Π(si!) * m
            assertEquals((long)expected, count);
        }
    }

    @Test
    void shouldThrowExceptionForNegativeIncrementValues() {
        var elements = of("A", "B", "C");
        int[] frequencies = {-1, 3, 2};

        assertThrows(IllegalArgumentException.class, () ->
                permutation.multiset(elements, frequencies).lexOrderMth(5, 0));
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var elements = of("A", "B", "C");
        int[] frequencies = {3, 2, 3};

        var iterable = permutation
                .multiset(elements, frequencies)
                .lexOrderMth(3, 0);

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCorrectMthMultisetPermutation() {
        var input = of('A', 'B', 'C');
        int[] freqArray = {3, 2, 3};
        for (int increment = 1; increment <= 24; increment++) {
            var output = getResultViaDirectIncrement(input, increment, freqArray);
            var expected = getExpectedResultViaOneByOneIteration(input, increment, freqArray);
            assertIterableEquals(expected, output);
        }
    }

    @Test
    void test_start_parameter_greater_than_0() {
        var expected = of(
                of('a', 'c', 'b', 'c', 'b', 'c'),
                of('b', 'c', 'c', 'a', 'c', 'b'),
                of('c', 'b', 'c', 'b', 'c', 'a')
        );
        var output = permutation.multiset(of('a', 'b', 'c'), new int[]{1, 2, 3})
                .lexOrderMth(20, 5)
                .stream().toList();
        assertIterableEquals(expected, output);
    }

    private List<?> getResultViaDirectIncrement(List<?> input, int increment, int[] freqArray) {
        return permutation.multiset(input, freqArray)
                .lexOrderMth(increment, 0)
                .stream().toList();
    }

    private List<?> getExpectedResultViaOneByOneIteration(List<?> input, int increment, int[] freqArray) {
        var stream = permutation.multiset(input, freqArray).lexOrder().stream();
        return everyMthValue(stream, increment);
    }
}
