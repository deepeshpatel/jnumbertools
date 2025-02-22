package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import io.github.deepeshpatel.jnumbertools.base.Calculator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;

import static org.junit.jupiter.api.Assertions.*;

public class MultisetCombinationTest {

    @Test
    void assertCount() {
        int[] frequencies = {5,4,7,3};
        int[] expectedCounts = Calculator.multisetCombinationsCountAll(frequencies);
        int maxSize = Arrays.stream(frequencies).sum();

        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < frequencies.length; i++) {
            map.put(A_B_C_D.get(i), frequencies[i]);
        }

        for(int size=0; size<=maxSize; size++) {

            long actualCount = combination
                    .multiset(map, size)
                    .lexOrder().stream().count();
            int index = size<expectedCounts.length? size: maxSize-size;

            assertEquals(expectedCounts[index], actualCount);
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {

        var options = Map.of('A', 2, 'B',3, 'C',2);
        var iterable = combination
                .multiset(options, 2)
                .lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCorrectCombinationsOfMultiset() {
        var expected = "[{A=1, B=2}," +
                " {A=1, B=1, C=1}," +
                " {A=1, B=1, D=1}," +
                " {A=1, C=2}," +
                " {A=1, C=1, D=1}," +
                " {B=2, C=1}," +
                " {B=2, D=1}," +
                " {B=1, C=2}," +
                " {B=1, C=1, D=1}," +
                " {C=3}, {C=2, D=1}]";

        var options = Map.of("C", 3,"B",2, "A", 1,"D",1);
        int size = 3;

        var out = combination.multiset(options, size).lexOrder().stream().toList().toString();
        assertEquals(expected, out);
    }

    @Test
    void shouldReturnEmptyListWhenCombinationSizeIsEqualToZero() {
        var out = combination.multiset(Map.of("A",2), 0).lexOrder().stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldReturnEmptyListWhenInputListIsEmpty() {
        var out = combination.multiset(new HashMap<String, Integer>(), 0).lexOrder().stream().toList();
        assertIterableEquals(listOfEmptyMap, out);
    }

    @Test
    void shouldThrowExceptionWhenNegativeFrequency() {
        var options = Map.of("A",2, "B",-1);
        assertThrows(IllegalArgumentException.class, () -> combination.multiset(options, 2).lexOrder());
    }

    @Test
    void shouldHandleLargeFrequencies() {
        var options = Map.of('A',100,'B',100,'C',100);
        assertNotNull(combination.multiset(options, 3));
    }
}