package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
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
        for (int n = 1; n <= 4; n++) {
            var input = Collections.nCopies(n, "A");
            for (int k = 1; k <= n; k++) {
                long size = permutation.nPk(k, input)
                        .lexOrder()
                        .stream().count();
                assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    void assertCountAndContentForSpecialCase() {
        //n=0 and k>0
        var nZeroBuilder = permutation.nPk(0,1);
        assertEquals(BigInteger.ZERO, nZeroBuilder.count());
        assertTrue(nZeroBuilder.lexOrder().stream().toList().isEmpty());

        //n> and k>n
        var greaterKBuilder =  permutation.nPk(1,2);
        assertEquals(BigInteger.ZERO,greaterKBuilder.count());
        assertTrue(greaterKBuilder.lexOrder().stream().toList().isEmpty());

        //n=0 and k=0
        var bothZeroBuilder = permutation.nPk(0,0);
        assertEquals(BigInteger.ONE,bothZeroBuilder.count());
        assertTrue(bothZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

        //n>0 and k=0
        var onlyKZeroBuilder = permutation.nPk(2,0);
        assertEquals(BigInteger.ONE,onlyKZeroBuilder.count());
        assertTrue(onlyKZeroBuilder.lexOrder().stream().toList().get(0).isEmpty());

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
