package io.github.deepeshpatel.jnumbertools.generator.combination.repetitive;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class RepetitiveCombinationTest {

    @Test
    void assertCount() {
        // nCr with repetition: (n+r−1)!/(r!·(n−1)!)
        for (int n = 1; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int r = 0; r <= n+2; r++) {
                long count = combination.repetitive(r, input)
                        .lexOrder().stream().count();
                long expectedCount = calculator.nCrRepetitive(n, r).longValue();
                assertEquals(expectedCount, count);
            }
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // n=0 and r=0 -> by convention: count = 1, returns [[]]
        var zeroZeroBuilder = combination.repetitive(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, zeroZeroBuilder.count());
        assertTrue(zeroZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        // n=0 and r>0 -> 0 -> count = 0, returns []
        var zeroPositiveBuilder = combination.repetitive(2, Collections.emptyList());
        assertEquals(BigInteger.ZERO, zeroPositiveBuilder.count());
        assertTrue(zeroPositiveBuilder.lexOrder().stream().toList().isEmpty());

        // n>0 and r=0 -> 1 -> count = 1, returns [[]]
        var positiveZeroBuilder = combination.repetitive(0, "A", "B");
        assertEquals(BigInteger.ONE, positiveZeroBuilder.count());
        assertTrue(positiveZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        // Note: For repetitive combinations, r > n is always valid (unlike unique combinations)
        // This is tested in assertCount() loop
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
}
