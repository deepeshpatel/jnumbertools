package io.github.deepeshpatel.jnumbertools.generator.combination.multiset;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitiveCombinationMultisetTest {

    @Test
    void assertCount() {
        int[] frequencies = {2, 3, 2};
        int size = 2;
        int expectedCount = 6;

        long actualCount = combination
                .multiset(A_B_C, frequencies, size)
                .lexOrder()
                .stream().count();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        int[] frequencies = {2, 3, 2};

        var iterable = combination
                .multiset(A_B_C, frequencies, 2)
                .lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCorrectCombinationsOfMultiset() {
        var expected = List.of(
            of("Red", "Red", "Red"),
            of("Red", "Red", "Green"),
            of("Red", "Red", "Blue"),
            of("Red", "Red", "Yellow"),
            of("Red", "Green", "Green"),
            of("Red", "Green", "Blue"),
            of("Red", "Green", "Yellow"),
            of("Red", "Blue", "Yellow"),
            of("Green", "Green", "Blue"),
            of("Green", "Green", "Yellow"),
            of("Green", "Blue", "Yellow")
        );

        var elements = of("Red", "Green", "Blue", "Yellow");
        int[] frequencies = {3, 2, 1, 1};  // 3 red, 2 green, 1 blue, 1 yellow
        int size = 3;
        assertIterableEquals(expected, output(elements, frequencies, size));
    }

    @Test
    void shouldReturnEmptyListWhenCombinationSizeIsEqualToZero() {
        assertIterableEquals(listOfEmptyList, output(of("A"), new int[]{3}, 0));
    }

    @Test
    void shouldReturnEmptyListWhenInputListIsEmpty() {
        assertIterableEquals(listOfEmptyList, output(Collections.emptyList(), new int[]{}, 0));
    }

    @Test
    void shouldThrowExceptionWhenMultisetFreqArrayIsNull() {
        assertThrows(IllegalArgumentException.class, () -> output(of('A'), null, 2));
    }

    @Test
    void shouldThrowExceptionWhenFrequenciesDoNotMatch() {
        assertThrows(IllegalArgumentException.class, () -> output(of("A", "B"), new int[]{3}, 2));
    }

    @Test
    void shouldThrowExceptionWhenNegativeFrequency() {
        assertThrows(IllegalArgumentException.class, () -> output(of("A", "B"), new int[]{-1, 2}, 2));
    }

    @Test
    void shouldHandleLargeFrequencies() {
        int[] frequencies = {100, 100, 100};
        assertNotNull(output(A_B_C, frequencies, 3));
    }

    @Test
    void shouldThrowExceptionWhenCombinationSizeIsGreaterThanNumberOfElements() {
        int[] frequencies = {2, 2};
        int size = 3;

        assertThrows(IllegalArgumentException.class, () -> output(A_B, frequencies, size));
    }

    private List<?> output(List<?> elements, int[] frequencies, int size) {
        return combination.multiset(elements, frequencies, size)
                .lexOrder()
                .stream().toList();
    }
}