package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class UniqueCombinationTest {

    @Test
    void assertCount() {

        for (int n = 0; n <= 4; n++) {
            List<String> input = Collections.nCopies(n, "A");
            for (int r = 0; r <= n; r++) {
                long count = combination.unique(r, input)
                        .lexOrder().stream().count();
                assertEquals(calculator.nCr(n, r).longValue(), count);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = combination.unique(2, "A", "B", "C").lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCombinationsInInputOrder() {
        var elements = of("Red", "Green", "Blue");
        var expected = List.of(
                of("Red", "Green"),
                of("Red", "Blue"),
                of("Green", "Blue")
        );
        assertIterableEquals(expected, output(2, elements));
    }

    @Test
    void shouldReturnEmptyListForSizeEqualsZero() {
        assertEquals(listOfEmptyList, output(0, of("A")));
    }

    @Test
    void shouldThrowExceptionForSizeGreaterThanN() {
        assertThrows(IllegalArgumentException.class, () -> output(3, of("A")));
    }

    @Test
    void shouldGenerateEmptyListForNullInput() {
        assertEquals(listOfEmptyList, output(0, Collections.emptyList()));
    }

    @Test
    void shouldThrowExceptionForEmptyInputListWithNonZeroSize() {
        assertThrows(IllegalArgumentException.class, () -> output(3, Collections.emptyList()));
    }

    @Test
    void shouldHandleLargeCombinations() {
        List<Integer> largeInput = Stream.iterate(1, i -> i + 1).limit(20).collect(Collectors.toList());
        assertEquals(calculator.nCr(20, 10).longValue(), combination.unique(10, largeInput).lexOrder().stream().count());
    }

    @Test
    void shouldWorkWithDifferentTypes() {
        List<Integer> input = of(1, 2, 3, 4);
        var expected = List.of(
                of(1, 2),
                of(1, 3),
                of(1, 4),
                of(2, 3),
                of(2, 4),
                of(3, 4)
        );

        assertIterableEquals(expected, output(2, input));
    }

    private List<?> output(int size, List<?> elements) {
        return combination.unique(size, elements).lexOrder().stream().toList();
    }
}
