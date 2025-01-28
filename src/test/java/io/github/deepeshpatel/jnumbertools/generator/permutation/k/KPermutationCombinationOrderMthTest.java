package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KPermutationCombinationOrderMthTest {

    @Test
    void assertCount() {
        int increment = 2;
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, 'A');
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .combinationOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n, k).longValue() / (double) increment);
                Assertions.assertEquals((long) expected, size);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation.nPk(2, A_B_C)
                .combinationOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldThrowExceptionForKLessThan0() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(-1, 1).combinationOrderMth(3, 0));
    }

    @Test
    void shouldThrowExceptionForKGreaterThanInputLength() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(1, 5).combinationOrderMth(3, 0));
    }

    @Test
    void shouldThrowExceptionForZeroAndNegativeIncrementValue() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(1, 1).combinationOrderMth(0, 0));
    }

    @Test
    void shouldGenerateMthKPermutations() {
        for (int k = 1; k <= 3; k++) {
            for (int increment = 1; increment <= 10; increment++) {
                var expected = getExpectedResultViaOneByOneIteration(A_B_C_D, k, increment);
                var output = getResultViaDirectIncrement(A_B_C_D, k, increment);
                assertIterableEquals(expected, output);
            }
        }
    }

    @Test
    void test_start_parameter_greater_than_0() {
        var expected = List.of(
                of('c', 'b', 'a'),
                of('a', 'e', 'c'),
                of('c', 'e', 'b')
        );
        var output = permutation.nPk(3, 'a', 'b', 'c', 'd', 'e')
                .combinationOrderMth(20, 5)
                .stream().toList();

        assertIterableEquals(expected, output);
    }

    @Test
    void shouldHandleEmptyInput() {
        var output = permutation.nPk(0, Collections.emptyList())
                .combinationOrderMth(1, 0).stream().toList();
        assertIterableEquals(listOfEmptyList, output);
    }

    @Test
    void shouldHandleLargeIncrementValues() {
        var input = of('A', 'B', 'C', 'D');
        var output = permutation.nPk(2, input)
                .combinationOrderMth(1, 12)
                .stream().toList();
        Assertions.assertTrue(output.isEmpty());
    }

    @Test
    void shouldGenerateAllPermutationsWhenKEqualsInputSize() {
        var output = permutation.nPk(3, A_B_C)
                .combinationOrderMth(1, 0)
                .stream().toList();

        Assertions.assertEquals("[[A, B, C], [A, C, B], [B, A, C], [B, C, A], [C, A, B], [C, B, A]]",
                output.toString());
    }

    @Test
    void shouldHandleLargeInputSize() {
        var input = of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J');
        var output = permutation.nPk(5, input)
                .combinationOrderMth(1, 0)
                .stream().toList();

        Assertions.assertEquals(30240, output.size());
    }

    private List<?> getResultViaDirectIncrement(List<?> input, int k, int increment) {
        return permutation.nPk(k, input)
                .combinationOrderMth(increment, 0)
                .stream().toList();
    }

    private List<?> getExpectedResultViaOneByOneIteration(List<?> input, int k, int increment) {
        var stream = permutation.nPk(k, input)
                .combinationOrder()
                .stream();
        return everyMthValue(stream, increment);
    }
}
