package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;

public class KPermutationCombinationOrderMthTest {

    @Test
    void assertCount() {
        // nPk with mᵗʰ: n!/(n−k)!/m
        int increment = 2;
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, 'A');
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .combinationOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n, k).longValue() / (double) increment);
                assertEquals((long) expected, size);
            }
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        // Case 1: n=0, k=0 -> ⁰P₀ = 1 -> should return [[]]
        var zeroZeroGenerator = permutation.nPk(0, 0).combinationOrderMth(1, 0);
        var zeroZeroResult = zeroZeroGenerator.stream().toList();
        assertEquals(1, zeroZeroResult.size());
        assertTrue(zeroZeroResult.get(0).isEmpty());

        // Case 2: n=0, k>0 -> ⁰Pₖ = 0 -> should return [] (empty iterator)
        var zeroPositiveGenerator = permutation.nPk(0, 2).combinationOrderMth(1, 0);
        assertTrue(zeroPositiveGenerator.stream().toList().isEmpty());

        // Case 3: n>0, k=0 -> ⁿP₀ = 1 -> should return [[]]
        var positiveZeroGenerator = permutation.nPk(3, 0).combinationOrderMth(1, 0);
        var positiveZeroResult = positiveZeroGenerator.stream().toList();
        assertEquals(1, positiveZeroResult.size());
        assertTrue(positiveZeroResult.get(0).isEmpty());

        // Case 4: n>0, k>n -> ⁿPₖ = 0 -> should return [] (empty iterator)
        var greaterKGenerator = permutation.nPk(2, 3).combinationOrderMth(1, 0);
        assertTrue(greaterKGenerator.stream().toList().isEmpty());

        // Case 5: With m>1, should still respect count=0
        var greaterKWithMthGenerator = permutation.nPk(2, 3).combinationOrderMth(3, 0);
        assertTrue(greaterKWithMthGenerator.stream().toList().isEmpty());

        // Case 6: Empty list with k>0 -> ⁰Pₖ = 0
        var emptyListGenerator = permutation.nPk(2, Collections.emptyList()).combinationOrderMth(1, 0);
        assertTrue(emptyListGenerator.stream().toList().isEmpty());
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
    void shouldGenerateMthKPermutations() {
        int start = 2;
        for (int k = 1; k <= 3; k++) {
            for (int m = 1; m <= 10; m+=2) {
                var all = permutation.nPk(k, A_B_C_D).lexOrder().stream();
                var mth = permutation.nPk(k, A_B_C_D).lexOrderMth(m, start).stream();
                assertEveryMthValue(all, mth, start, m);
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
    void shouldGenerateAllPermutationsWhenKEqualsInputSize() {
        var output = permutation.nPk(3, A_B_C)
                .combinationOrderMth(1, 0)
                .stream().toList();

        assertEquals("[[A, B, C], [A, C, B], [B, A, C], [B, C, A], [C, A, B], [C, B, A]]",
                output.toString());
    }

    @Test
    void shouldHandleLargeInputSize() {
        var input = of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J');
        var output = permutation.nPk(5, input)
                .combinationOrderMth(1, 0)
                .stream().toList();

        assertEquals(30240, output.size());
    }
}
