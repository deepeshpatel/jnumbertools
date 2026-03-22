package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.core.internal.generator.combination.multiset.MultisetCombination;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
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
        // Multiset combinations: Σₖ Πᵢ C(nᵢ, kᵢ)
        int[] frequencies = {5, 4, 7, 3};
        int[] expectedCounts = calculator.multisetCombinationsCountAll(frequencies);
        int maxSize = Arrays.stream(frequencies).sum();

        LinkedHashMap<Character, Integer> options = new LinkedHashMap<>();
        for (int i = 0; i < frequencies.length; i++) {
            options.put(A_B_C_D.get(i), frequencies[i]);
        }

        for (int size = 0; size <= maxSize; size++) {
            long actualCount = combination.multiset(options,size).lexOrder()
                    .stream().count();
            int index = size < expectedCounts.length ? size : maxSize - size;
            assertEquals(expectedCounts[index], actualCount, "Count mismatch for size=" + size);
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // Empty map, r=0 -> count = 1, returns [{}]
        var emptyZeroBuilder = combination.multiset(new LinkedHashMap<>(), 0);
        assertEquals(BigInteger.ONE, emptyZeroBuilder.count());
        var emptyZeroResult = emptyZeroBuilder.lexOrder().stream().toList();
        assertEquals(1, emptyZeroResult.size());
        assertEquals(Map.of(), emptyZeroResult.get(0));

        // Empty map, r>0 -> count = 0, returns []
        var emptyPositiveBuilder = combination.multiset(new LinkedHashMap<>(), 2);
        assertEquals(BigInteger.ZERO, emptyPositiveBuilder.count());
        assertTrue(emptyPositiveBuilder.lexOrder().stream().toList().isEmpty());

        // Non-empty map, r=0 -> count = 1, returns [{}]
        var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
        var positiveZeroBuilder = combination.multiset(options, 0);
        assertEquals(BigInteger.ONE, positiveZeroBuilder.count());
        var positiveZeroResult = positiveZeroBuilder.lexOrder().stream().toList();
        assertEquals(1, positiveZeroResult.size());
        assertEquals(Map.of(), positiveZeroResult.get(0));

        // r > total available -> count = 0, returns []
        var greaterRBuilder = combination.multiset(options, 10);
        assertEquals(BigInteger.ZERO, greaterRBuilder.count());
        assertTrue(greaterRBuilder.lexOrder().stream().toList().isEmpty());
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

        LinkedHashMap<String, Integer> options = new LinkedHashMap<>();
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
        // keep this test because it's a combination of 2 conditions -
        // Empty map (∅) and r = 0
        var options = new LinkedHashMap<String, Integer>();
        var out = combination.multiset(options,0).lexOrder().stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void testIteratorEquivalence() {
        var input = new LinkedHashMap<String, Integer>();
        input.put("Banana", 400);
        input.put("Apple", 1000);
        input.put("Mango", 3);
        int[] rValues = {10, 500};

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
        var options = new LinkedHashMap<>(Map.of("A", 500, "B", 900));
        int[] rValues = {900, 999, 1000, 1500};

        int totalAvailable = 500 + 900; // 1400

        for (int r : rValues) {
            var iterator = combination.multiset(options, r).lexOrder().iterator();

            if (r > totalAvailable) {
                // Case: r exceeds total available → empty iterator
                assertFalse(iterator.hasNext());
            } else if (r < CROSSOVER_THRESHOLD) {
                assertInstanceOf(MultisetCombination.ArrayIterator.class, iterator, "Expected ArrayIterator for r=" + r);
            } else {
                assertInstanceOf(MultisetCombination.FreqVectorIterator.class, iterator, "Expected FreqVectorIterator for r=" + r);
            }
        }
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

    @Test
    void shouldReturnImmutableOuterAndInnerCollection() {
        var options = new LinkedHashMap<>(Map.of("A", 2, "B", 1));
        var results = combination.multiset(options, 2).lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(Map.of("X", 1)));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));

        var first = results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.put("X", 1));
    }
}
