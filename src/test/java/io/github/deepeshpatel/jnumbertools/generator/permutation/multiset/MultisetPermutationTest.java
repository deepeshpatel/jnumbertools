package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;


import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class MultisetPermutationTest {

    @Test
    void assertCount() {
        Random random = new Random(System.currentTimeMillis());
        for (int n = 2; n <= 7; n++) {
            var input = IntStream.range(0,n).boxed().toList();
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            LinkedHashMap<Character, Integer> options = createMap(input, frequency);
            long count = permutation.multiset(options).lexOrder().stream().count();
            assertEquals(calculator.multinomial(frequency).longValue(), count);
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {

        var elements = of("A", "B", "C");
        int[] frequencies = {3, 2, 2};
        LinkedHashMap<Character, Integer> options = createMap(elements, frequencies);

        var iterable = permutation.multiset(options).lexOrder();

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

        LinkedHashMap<String, Integer> options = new LinkedHashMap<>();

        options.put("Red",2);
        options.put("Green",1);
        options.put("Blue",1);

        var output = permutation.multiset(options)
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

        LinkedHashMap<Character, Integer> options = createMap(elements, frequencies);

        var output = permutation.multiset(options)
                .lexOrder()
                .stream()
                .toList();

        assertIterableEquals(expected, output);
    }

}
