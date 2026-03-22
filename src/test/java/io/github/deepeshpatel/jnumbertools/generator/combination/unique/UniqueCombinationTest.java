package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class UniqueCombinationTest {

    @Test
    void assertCount() {
        // nCr: n!/(r!·(n−r)!)
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
    void assertCountAndContentForSpecialCase() {
        // n=0 and r=0 -> 0C0 = 1 -> count = 1, returns [[]]
        var zeroZeroBuilder = combination.unique(0, Collections.emptyList());
        assertEquals(BigInteger.ONE, zeroZeroBuilder.count());
        assertTrue(zeroZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        // n=0 and r>0 -> 0Cr = 0 -> count = 0, returns []
        var zeroPositiveBuilder = combination.unique(1, Collections.emptyList());
        assertEquals(BigInteger.ZERO, zeroPositiveBuilder.count());
        assertTrue(zeroPositiveBuilder.lexOrder().stream().toList().isEmpty());

        // n>0 and r=0 -> nC0 = 1 -> count = 1, returns [[]]
        var positiveZeroBuilder = combination.unique(0, "A", "B", "C");
        assertEquals(BigInteger.ONE, positiveZeroBuilder.count());
        assertTrue(positiveZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        // n>0 and r>n -> nCr = 0 -> count = 0, returns []
        var greaterRBuilder = combination.unique(2, "A");
        assertEquals(BigInteger.ZERO, greaterRBuilder.count());
        assertTrue(greaterRBuilder.lexOrder().stream().toList().isEmpty());
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

    @Test
    void shouldReturnImmutableOuterAndInnerCollection() {
        var results = combination.unique(2, "A", "B", "C").lexOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));

        var first = results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
        assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
    }

    private List<?> output(int size, List<?> elements) {
        return combination.unique(size, elements).lexOrder().stream().toList();
    }
}
