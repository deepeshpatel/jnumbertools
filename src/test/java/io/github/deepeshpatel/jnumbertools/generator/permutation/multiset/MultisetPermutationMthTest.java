package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;


import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;
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
        int start = 0;

        for (int n = 4; n <= 6; n++) {
            var input = IntStream.range(0,n).boxed().toList();
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            var options = createMap(input, frequency);

            long count = permutation.multiset(options)
                    .lexOrderMth(increment, start).stream().count();

            double expected = calculator.totalMthMultinomial(start, increment, frequency).longValue();
            assertEquals(expected, count);
        }
    }

    @Test
    void shouldThrowExceptionForNegativeIncrementValues() {
        var elements = of("A", "B", "C");
        int[] frequencies = {-1, 3, 2};
        LinkedHashMap<String, Integer> options = createMap(elements, frequencies);
        assertThrows(IllegalArgumentException.class, () ->
                permutation.multiset(options).lexOrderMth(5, 0));
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var elements = of("A", "B", "C");
        int[] frequencies = {3, 2, 3};
        LinkedHashMap<String, Integer> options = createMap(elements, frequencies);

        var iterable = permutation
                .multiset(options)
                .lexOrderMth(3, 0);

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCorrectMthMultisetPermutation() {
        var input = of('A', 'B', 'C');
        int[] freqArray = {3, 2, 3};
        LinkedHashMap<Character, Integer> options = createMap(input, freqArray);
        int start= 2;
        for (int increment = 1; increment <= 24; increment+=2) {
            var all = permutation.multiset(options).lexOrder().stream();
            var mth = permutation.multiset(options).lexOrderMth(increment, start).stream();
            assertEveryMthValue(all, mth, start, increment);
        }
    }

    @Test
    void test_start_parameter_greater_than_0() {
        var expected = of(
                of('a', 'c', 'b', 'c', 'b', 'c'),
                of('b', 'c', 'c', 'a', 'c', 'b'),
                of('c', 'b', 'c', 'b', 'c', 'a')
        );

        LinkedHashMap<Character, Integer> options = createMap(of('a', 'b', 'c'), new int[]{1, 2, 3});

        var output = permutation.multiset(options)
                .lexOrderMth(20, 5)
                .stream().toList();
        assertIterableEquals(expected, output);
    }
}
