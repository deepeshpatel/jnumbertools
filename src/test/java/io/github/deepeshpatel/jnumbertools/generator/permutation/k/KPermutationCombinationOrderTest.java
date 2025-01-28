package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class KPermutationCombinationOrderTest {

    @Test
    void assertCount() {
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .combinationOrder()
                        .stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation.nPk(2, "A", "B", "C")
                .combinationOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldThrowExceptionForNegativeK() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(-3, new ArrayList<>()).combinationOrder());
    }

    @Test
    void shouldNotThrowExceptionForZeroK() {
        var output = permutation
                .nPk(0, new ArrayList<>())
                .combinationOrder()
                .stream().toList();

        assertIterableEquals(listOfEmptyList, output);
    }

    @Test
    void shouldGenerateAllUniquePermutationsOf3Values() {
        var expected = List.of(
                of(1, 2),
                of(2, 1),
                of(1, 3),
                of(3, 1),
                of(2, 3),
                of(3, 2)
        );
        var actual = permutation.nPk(2, 1, 2, 3)
                .combinationOrder()
                .stream().toList();

        assertIterableEquals(expected, actual);
    }

    @Test
    void shouldGenerateKPermutations() {
        var expected = List.of(
                of('A', 'B'),
                of('B', 'A'),
                of('A', 'C'),
                of('C', 'A'),
                of('B', 'C'),
                of('C', 'B')
        );

        var output = permutation.nPk(2, 'A', 'B', 'C')
                .combinationOrder().stream().toList();
        assertIterableEquals(expected, output);
    }

    @Test
    void shouldGenerateOneEmptyPermutationForKEqualsZero() {
        var output = permutation.nPk(0, "A")
                .combinationOrder()
                .stream()
                .toList();
        assertIterableEquals(listOfEmptyList, output);
    }

    @Test
    void shouldGenerateEmptyListForKGreaterThanInputSize() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(4, 'A', 'B', 'C').combinationOrder());
    }
}
