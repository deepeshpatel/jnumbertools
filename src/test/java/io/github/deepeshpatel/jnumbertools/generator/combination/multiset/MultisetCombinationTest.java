package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static org.junit.jupiter.api.Assertions.*;

public class MultisetCombinationTest {

    private static final int CROSSOVER_THRESHOLD = 1000;

    @Test
    void assertCount() {
        int[] frequencies = {5, 4, 7, 3};
        int[] expectedCounts = Calculator.multisetCombinationsCountAll(frequencies);
        int maxSize = Arrays.stream(frequencies).sum();

        LinkedHashMap<Character, Integer> options = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) {
            options.put(A_B_C_D.get(i), frequencies[i]);
        }

        for (int size = 0; size <= maxSize; size++) {
            long actualCount = combination.multiset(options,size).lexOrder()
                    .stream().count();
            int index = size < expectedCounts.length ? size : maxSize - size; // Symmetry: size or maxSize-size
            assertEquals(expectedCounts[index], actualCount, "Count mismatch for size=" + size);
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var options = new LinkedHashMap<>(Map.of('A', 2, 'B', 3, 'C', 2));
        var mc = combination.multiset(options,2).lexOrder();
        var lists1 = mc.stream().toList();
        var lists2 = mc.stream().toList();
        assertEquals(lists1.toString(), lists2.toString());
    }

    @Test
    void shouldGenerateCorrectCombinationsOfMultiset() {
        int size = 3;

        String expected = "[{C=3}, {C=2, B=1}, {C=2, A=1}, {C=2, D=1}, {C=1, B=2}, {C=1, B=1, A=1}," +
                " {C=1, B=1, D=1}, {C=1, A=1, D=1}, {B=2, A=1}, {B=2, D=1}, {B=1, A=1, D=1}]";

        LinkedHashMap<String, Integer> options = new LinkedHashMap<String, Integer>();
        options.put("C", 3);
        options.put("B", 2);
        options.put("A", 1);
        options.put("D", 1);

        var out = combination.multiset(options,size).lexOrder().stream().toList().toString();
        assertEquals(expected, out);
    }

    @Test
    void shouldReturnEmptyListWhenCombinationSizeIsZero() {
        var options = new LinkedHashMap<>(Map.of("A", 2));
        var out = combination.multiset(options,0).lexOrder().stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldReturnEmptyListWhenInputListIsEmpty() {
        var options = new LinkedHashMap<String, Integer>();
        var out = combination.multiset(options,0).lexOrder().stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldThrowExceptionWhenNegativeFrequency() {
        var options = new LinkedHashMap<>(Map.of("A", 2, "B", -1));
        assertThrows(IllegalArgumentException.class, () ->
                combination.multiset(options,2).lexOrder());
    }

    @Test
    void testIteratorEquivalence() {
        var input = new LinkedHashMap<String, Integer>();
        input.put("Banana", 4000);
        input.put("Apple", 10000);
        input.put("Mango", 3);
        int[] rValues = {10, 5000};

        for (int r : rValues) {
            MultisetCombination<String> mc = combination.multiset(input,r).lexOrder();
            Iterator<Map<String, Integer>> arrayIterator = mc.new ArrayIterator();
            Iterator<Map<String, Integer>> freqIterator = mc.new FreqVectorIterator();

            while (arrayIterator.hasNext() && freqIterator.hasNext()) {
                assertEquals(arrayIterator.next(), freqIterator.next(),
                        "Mismatch at r=" + r );
            }
            assertFalse(arrayIterator.hasNext() || freqIterator.hasNext(),
                    "Size mismatch for r=" + r );

        }
    }

    @Test
    void shouldSwitchIteratorsAtThreshold() {
        var options = new LinkedHashMap<>(Map.of("A", 5, "B", 5));
        int[] rValues = {999, 1000}; // Below and at crossover

        for (int r : rValues) {
            var iterator = combination.multiset(options, r).lexOrder().iterator();

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
        var options = new LinkedHashMap<>(Map.of("A", 2, "B", 3));
        assertThrows(IllegalArgumentException.class, () ->
                combination.multiset(options,-1).lexOrder());
    }

    @Test
    void shouldGenerateCorrectCombinationsInInsertionOrder() {
        var options = new LinkedHashMap<String, Integer>();
        options.put("B", 3);
        options.put("A", 4);
        options.put("C", 2);
        int size = 3;

        String expected = "[{B=3}, {B=2, A=1}, {B=2, C=1}, {B=1, A=2}, {B=1, A=1, C=1}, " +
                    "{B=1, C=2}, {A=3}, {A=2, C=1}, {A=1, C=2}]";

        var output = combination.multiset(options,size).lexOrder().stream().toList();
        assertEquals(expected, output.toString());
    }

}