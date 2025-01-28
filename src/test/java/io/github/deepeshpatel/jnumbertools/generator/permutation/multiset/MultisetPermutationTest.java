package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class MultisetPermutationTest {

    @Test
    void assertCount() {
        Random random = new Random(System.currentTimeMillis());
        for (int n = 1; n <= 6; n++) {
            var input = Collections.nCopies(n, 'A');
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            long count = permutation.multiset(input, frequency)
                    .lexOrder().stream().count();

            int sum = Arrays.stream(frequency).reduce(0, Integer::sum);
            long numerator = calculator.factorial(sum).longValue();
            long denominator = IntStream.of(frequency).asLongStream()
                    .reduce(1, (a, b) -> (a * calculator.factorial((int) b).longValue()));
            long expected = numerator / denominator;
            //( ∑ ai.si)! / Π(si!)
            assertEquals(expected, count);
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {

        var elements = of("A", "B", "C");
        int[] frequencies = {3, 2, 2};

        var iterable = permutation.multiset(elements, frequencies).lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateAllUniquePermutationsOfMultiset2() {
        var expected = of(
                of("Red", "Red", "Green", "Blue"),
                of("Red", "Red", "Blue", "Green"),
                of("Red", "Green", "Red", "Blue"),
                of("Red", "Green", "Blue", "Red"),
                of("Red", "Blue", "Red", "Green"),
                of("Red", "Blue", "Green", "Red"),
                of("Green", "Red", "Red", "Blue"),
                of("Green", "Red", "Blue", "Red"),
                of("Green", "Blue", "Red", "Red"),
                of("Blue", "Red", "Red", "Green"),
                of("Blue", "Red", "Green", "Red"),
                of("Blue", "Green", "Red", "Red")
        );

        var elements = of("Red", "Green", "Blue");
        int[] frequencies = {2, 1, 1};
        var output = permutation.multiset(elements, frequencies)
                .lexOrder()
                .stream()
                .toList();

        assertIterableEquals(expected, output);
    }

    @Test
    void shouldGenerateAllUniquePermutationsOfMultiset() {
        var expected = List.of(
                of("A", "A", "B", "C"),
                of("A", "A", "C", "B"),
                of("A", "B", "A", "C"),
                of("A", "B", "C", "A"),
                of("A", "C", "A", "B"),
                of("A", "C", "B", "A"),
                of("B", "A", "A", "C"),
                of("B", "A", "C", "A"),
                of("B", "C", "A", "A"),
                of("C", "A", "A", "B"),
                of("C", "A", "B", "A"),
                of("C", "B", "A", "A")
        );

        var elements = of("A", "B", "C");
        int[] frequencies = {2, 1, 1};

        var output = permutation.multiset(elements, frequencies)
                .lexOrder()
                .stream()
                .toList();

        assertIterableEquals(expected, output);
    }

    @Test
    void shouldThrowExceptionWhenFrequenciesIsNull() {
        var elements = of("A", "B", "C");

        assertThrows(IllegalArgumentException.class, () ->
                permutation.multiset(elements, null).lexOrder());
    }

}
