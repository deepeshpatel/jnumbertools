package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

public class MultisetCombinationTest {

    private static final int CROSSOVER_THRESHOLD = 1000;

    @Test
    void assertCount() {
        int[] frequencies = {5, 4, 7, 3};
        int[] expectedCounts = Calculator.multisetCombinationsCountAll(frequencies);
        int maxSize = Arrays.stream(frequencies).sum();

        Map<Character, Integer> options = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) {
            options.put(A_B_C_D.get(i), frequencies[i]);
        }

        for (int size = 0; size <= maxSize; size++) {
            long actualCount = new MultisetCombination<>(options, size, MultisetCombination.Order.LEX)
                    .stream().count();
            int index = size < expectedCounts.length ? size : maxSize - size; // Symmetry: size or maxSize-size
            assertEquals(expectedCounts[index], actualCount, "Count mismatch for size=" + size);
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        Map<Character, Integer> options = Map.of('A', 2, 'B', 3, 'C', 2);
        MultisetCombination<Character> mc = new MultisetCombination<>(options, 2, MultisetCombination.Order.LEX);
        var lists1 = mc.stream().toList();
        var lists2 = mc.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCorrectCombinationsOfMultiset() {
        String expected = "[{A=1, B=2}, {A=1, B=1, C=1}, {A=1, B=1, D=1}, {A=1, C=2}, {A=1, C=1, D=1}, " +
                "{B=2, C=1}, {B=2, D=1}, {B=1, C=2}, {B=1, C=1, D=1}, {C=3}, {C=2, D=1}]";
        Map<String, Integer> options = new LinkedHashMap<>(Map.of("C", 3, "B", 2, "A", 1, "D", 1));
        int size = 3;

        var out = new MultisetCombination<>(options, size, MultisetCombination.Order.LEX).stream().toList().toString();
        assertEquals(expected, out);
    }

    @Test
    void shouldReturnEmptyListWhenCombinationSizeIsZero() {
        Map<String, Integer> options = new LinkedHashMap<>(Map.of("A", 2));
        var out = new MultisetCombination<>(options, 0, MultisetCombination.Order.LEX).stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldReturnEmptyListWhenInputListIsEmpty() {
        Map<String, Integer> options = new LinkedHashMap<>();
        var out = new MultisetCombination<>(options, 0, MultisetCombination.Order.LEX).stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldThrowExceptionWhenNegativeFrequency() {
        Map<String, Integer> options = new LinkedHashMap<>(Map.of("A", 2, "B", -1));
        assertThrows(IllegalArgumentException.class, () ->
                new MultisetCombination<>(options, 2, MultisetCombination.Order.LEX));
    }

    @Test
    void testIteratorEquivalence() {
        Map<String, Integer> input = new LinkedHashMap<>();
        input.put("Banana", 4000);
        input.put("Apple", 10000);
        input.put("Mango", 3);
        int[] rValues = {10, 5000};
        MultisetCombination.Order[] orders = {
                MultisetCombination.Order.LEX,
                MultisetCombination.Order.REVERSE_LEX,
                MultisetCombination.Order.INPUT
        };

        for (int r : rValues) {
            for (MultisetCombination.Order order : orders) {
                MultisetCombination<String> mc = new MultisetCombination<>(input, r, order);
                Iterator<Map<String, Integer>> arrayIterator = mc.new ArrayIterator();
                Iterator<Map<String, Integer>> freqIterator = mc.new FreqVectorIterator();

                while (arrayIterator.hasNext() && freqIterator.hasNext()) {
                    assertEquals(arrayIterator.next(), freqIterator.next(),
                            "Mismatch at r=" + r + ", order=" + order);
                }
                assertFalse(arrayIterator.hasNext() || freqIterator.hasNext(),
                        "Size mismatch for r=" + r + ", order=" + order);
            }
        }
    }

    @Test
    void shouldSwitchIteratorsAtThreshold() {
        Map<String, Integer> options = new LinkedHashMap<>(Map.of("A", 5, "B", 5));
        int[] rValues = {999, 1000}; // Below and at crossover

        for (int r : rValues) {
            var iterator = new MultisetCombination<>(options, r, MultisetCombination.Order.LEX).iterator();

            if (r < CROSSOVER_THRESHOLD) {
                assertTrue(iterator instanceof MultisetCombination.ArrayIterator,
                        "Expected ArrayIterator for r =" + r);
            } else {
                assertTrue(iterator instanceof MultisetCombination.FreqVectorIterator,
                        "Expected FreqVectorIterator for r =" + r);
            }
        }
    }

    @Test
    void shouldThrowExceptionForInvalidR() {
        Map<String, Integer> options = new LinkedHashMap<>(Map.of("A", 2, "B", 3));
        assertThrows(IllegalArgumentException.class, () ->
                new MultisetCombination<>(options, -1, MultisetCombination.Order.LEX));
    }

    @ParameterizedTest
    @EnumSource(MultisetCombination.Order.class)
    void shouldGenerateCorrectCombinationsForAllOrders(MultisetCombination.Order order) {
        Map<String, Integer> options = new LinkedHashMap<>();
        options.put("B", 3);
        options.put("A", 4);
        options.put("C", 2);
        int size = 3;

        String expected = switch (order) {
            case LEX -> "[{A=3}, {A=2, B=1}, {A=2, C=1}, {A=1, B=2}, {A=1, B=1, C=1}, " +
                    "{A=1, C=2}, {B=3}, {B=2, C=1}, {B=1, C=2}]";
            case REVERSE_LEX -> "[{C=2, B=1}, {C=2, A=1}, {C=1, B=2}, {C=1, B=1, A=1}, {C=1, A=2}, " +
                    "{B=3}, {B=2, A=1}, {B=1, A=2}, {A=3}]";
            case INPUT -> "[{B=3}, {B=2, A=1}, {B=2, C=1}, {B=1, A=2}, {B=1, A=1, C=1}, " +
                    "{B=1, C=2}, {A=3}, {A=2, C=1}, {A=1, C=2}]";
            default -> throw new IllegalArgumentException("Unknown order: " + order);
        };

        var output = new MultisetCombination<>(options, size, order).stream().toList();
        assertEquals(expected, output.toString(), "Order: " + order);
    }
}