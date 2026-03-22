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
        // nPk: n!/(n−k)!
        for (int n = 1; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 1; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .combinationOrder()
                        .stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // n=0, k=0 -> ⁰P₀ = 1 -> count=1, returns [[]]
        var zeroZeroBuilder = permutation.nPk(0, Collections.emptyList());
        assertEquals(calculator.nPr(0, 0), zeroZeroBuilder.count());
        var zeroZeroResult = zeroZeroBuilder.combinationOrder().stream().toList();
        assertEquals(1, zeroZeroResult.size());
        assertTrue(zeroZeroResult.get(0).isEmpty());

        // n=0, k>0 -> ⁰Pₖ = 0 -> count=0, returns [] (empty iterator)
        var zeroPositiveBuilder = permutation.nPk(2, Collections.emptyList());
        assertEquals(calculator.nPr(0, 2), zeroPositiveBuilder.count());
        assertTrue(zeroPositiveBuilder.combinationOrder().stream().toList().isEmpty());

        // n>0, k=0 -> ⁿP₀ = 1 -> count=1, returns [[]]
        var positiveZeroBuilder = permutation.nPk(0, List.of("A", "B", "C"));
        assertEquals(calculator.nPr(3, 0), positiveZeroBuilder.count());
        var positiveZeroResult = positiveZeroBuilder.combinationOrder().stream().toList();
        assertEquals(1, positiveZeroResult.size());
        assertTrue(positiveZeroResult.get(0).isEmpty());

        // n>0, k>n -> ⁿPₖ = 0 -> count=0, returns [] (empty iterator)
        var greaterKBuilder = permutation.nPk(4, List.of('A', 'B', 'C'));
        assertEquals(calculator.nPr(3, 4), greaterKBuilder.count());
        assertTrue(greaterKBuilder.combinationOrder().stream().toList().isEmpty());
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
    void shouldReturnImmutableOuterAndInnerCollection() {
        var results = permutation.nPk(2, "A", "B", "C").combinationOrder().stream().toList();
        assertThrows(UnsupportedOperationException.class, () -> results.add(List.of("X")));
        assertThrows(UnsupportedOperationException.class, () -> results.remove(0));

        var first = results.get(0);
        assertThrows(UnsupportedOperationException.class, () -> first.add("X"));
        assertThrows(UnsupportedOperationException.class, () -> first.set(0, "X"));
    }
}
