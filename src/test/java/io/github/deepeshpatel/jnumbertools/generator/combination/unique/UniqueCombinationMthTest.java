package io.github.deepeshpatel.jnumbertools.generator.combination.unique;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class UniqueCombinationMthTest {

    @Test
    void assertCount() {
        for (int n = 3; n < 6; n++) {
            var input = Collections.nCopies(n, "A");
            for (int increment = 1; increment <= 4; increment++) {
                int combinationSize = input.size() / 2;
                long size = combination.unique(combinationSize, input).lexOrderMth(increment, 0).stream().count();
                double expected = Math.ceil(calculator.nCr(n, combinationSize).longValue() / (double) increment);
                assertEquals((long) expected, size);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = combination.unique(2, A_B_C).lexOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGenerateCombinationsWithIncrementingToEvery3rdCombination() {
        var expected = List.of(
                of(0, 1, 2),
                of(0, 2, 3),
                of(1, 2, 3),
                of(2, 3, 4)
        );

        assertIterableEquals(expected, output(of(0, 1, 2, 3, 4), 3, 3));
    }

    @Test
    void shouldSupportVeryLargeMthCombination() {
        var expected = List.of(
                of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
                of(0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33),
                of(2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33)
        );

        int n = 34;
        int r = 17;
        int increment = 1000_000_000; // jump to 1 billionth combination
        assertIterableEquals(expected, output(n, r, increment));
    }

    @Test
    void shouldGenerateMthCombinationIncrementingValuesInBetween() {
        var input = of('A', 'B', 'C', 'D', 'E', 'F');
        for (int k = 1; k <= input.size() / 2; k++) {
            for (int increment = 1; increment <= 10; increment++) {
                var expected = getExpectedResultViaOneByOneIteration(input, k, increment);
                var output = getResultViaDirectIncrement(input, k, increment);
                assertIterableEquals(expected, output);
            }
        }
    }

    @Test
    void testStartParameterGreaterThanZero() {
        var expected = List.of(
                of('B', 'C'),
                of('C', 'D')
        );
        var output = combination.unique(2, 'A', 'B', 'C', 'D')
                .lexOrderMth(2, 3).stream().toList();
        assertIterableEquals(expected, output);
    }

    private List<?> getResultViaDirectIncrement(List<?> input, int r, int increment) {
        return combination.unique(r, input)
                .lexOrderMth(increment, 0)
                .stream().toList();
    }

    private List<?> getExpectedResultViaOneByOneIteration(List<?> input, int r, int increment) {
        var stream = combination.unique(r, input).lexOrder().stream();
        return TestBase.everyMthValue(stream, increment);
    }

    private List<?> output(List<?> elements, int size, int m) {
        return combination.unique(size, elements).lexOrderMth(m, 0).stream().toList();
    }

    private  List<?>output(int n, int r, int m) {
        return combination.unique(n, r).lexOrderMth(m, 0).stream().toList();
    }
}
