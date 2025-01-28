package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitiveCombinationTest {

    @Test
    void assertCount() {
        for (int n = 1; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int r = 0; r <= n; r++) {
                long count = combination.repetitive(r, input)
                        .lexOrder().stream().count();
                long expectedCount = calculator.nCrRepetitive(n, r).longValue();
                assertEquals(expectedCount, count);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = combination
                .repetitive(2, A_B_C).lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCombinationsInInputOrder() {
        var expected = List.of(
                of("Red", "Red"),
                of("Red", "Green"),
                of("Red", "Blue"),
                of("Green", "Green"),
                of("Green", "Blue"),
                of("Blue", "Blue")
        );
        assertIterableEquals(expected, output(2, "Red", "Green", "Blue"));
    }

    @Test
    void shouldReturnEmptyListForSizeLessThanOrEqualsZero() {
        assertEquals(listOfEmptyList, output(0, "A", "B"));
    }

    @Test
    void shouldAbleToGenerateRepetitivePermutationForSizeGreaterThanN() {
        var expected = List.of(
                of("A", "A", "A"),
                of("A", "A", "B"),
                of("A", "B", "B"),
                of("B", "B", "B")
        );

        assertEquals(expected, output(3, "A", "B"));
    }

    private List<?> output(int size, String... elements) {
        return combination
                .repetitive(size, elements)
                .lexOrder()
                .stream()
                .toList();
    }

    @Test
    void shouldThrowExceptionForNegativeRValue() {
        assertThrows(IllegalArgumentException.class, () -> combination.repetitive(3, -2).lexOrder());
    }
}
