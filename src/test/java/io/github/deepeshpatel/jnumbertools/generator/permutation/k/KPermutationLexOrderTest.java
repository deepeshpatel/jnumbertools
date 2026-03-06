package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.jupiter.api.Assertions.*;

public class KPermutationLexOrderTest {

    @Test
    void assertCount() {
        // nPk: n!/(n−k)!
        for (int n = 0; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .lexOrder()
                        .stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    void shouldHandleEmptyInput() {
        // Test with empty input list
        var result = permutation.nPk(0, Collections.emptyList()).lexOrder().stream().toList();
        assertEquals(1, result.size());
        assertTrue(result.get(0).isEmpty());

        // Test with empty input and k=0
        var result2 = permutation.nPk(0, Collections.emptyList()).lexOrder().stream().toList();
        assertEquals(1, result2.size());
        assertTrue(result2.get(0).isEmpty());
    }

    @Test
    void shouldHandleKEqualsZero() {
        // k=0 should return one empty permutation
        var result = permutation.nPk(0, List.of("A", "B", "C")).lexOrder().stream().toList();
        assertEquals(1, result.size());
        assertTrue(result.get(0).isEmpty());
    }

    @Test
    void shouldThrowExceptionForNegativeK() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(-1, List.of("A", "B")).lexOrder());
    }

    @Test
    void shouldThrowExceptionForKGreaterThanN() {
        assertThrows(IllegalArgumentException.class, () ->
                permutation.nPk(5, List.of("A", "B", "C")).lexOrder());
    }

    @Test
    void shouldReturnSameResultForDifferentIteratorObjects() {
        var iterable = permutation.nPk(2, "A", "B", "C").lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        assertIterableEquals(lists1, lists2);
    }

    @Test
    void shouldGeneratedOutputInLexOrder(){
        int k= 4;
        var input = List.of("A","B","C","D","E","F","G","H");
        var output = permutation.nPk(k,input)
                .lexOrder()
                .stream()
                .toList().toString();

        var sorted = new ArrayList<String>();
        permutation.nPk(k, input)
                .combinationOrder()
                .stream()
                .forEach(e-> sorted.add(e.toString()));

        Collections.sort(sorted);
        String expected = sorted.toString();
        assertEquals(expected, output);
    }
}
