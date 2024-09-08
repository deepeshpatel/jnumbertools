package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static org.junit.Assert.assertEquals;

public class KPermutationCombinationOrderTest {

    @Test
    public void assertCount(){
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPr(k, input)
                        .combinationOrder()
                        .stream().count();
                Assert.assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        KPermutationCombinationOrder<String> iterable = permutation.nPr(2,"A", "B", "C")
                .combinationOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeK() {
        permutation.nPr(-3, new ArrayList<>()).combinationOrder();
    }

    @Test
    public void shouldNotThrowExceptionForZeroK() {
         String output = permutation
                 .nPr(0, new ArrayList<>())
                 .combinationOrder()
                         .stream().toList().toString();

         assertEquals("[[]]", output);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]]";
        String actual   = permutation.nPr(2,1,2,3)
                .combinationOrder()
                .stream().toList().toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateKPermutations() {
        String expected = "[[A, B], [B, A], [A, C], [C, A], [B, C], [C, B]]";

        var output = permutation.nPr(2, "A", "B", "C")
                .combinationOrder()
                .stream()
                .toList();
        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateOneEmptyPermutationForKEqualsZero() {

        var output = permutation.nPr(0, "A")
                .combinationOrder()
                .stream()
                .toList();
        Assert.assertEquals("[[]]", output.toString());
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldGenerateEmptyListForKGreaterThanInputSize() {
        permutation.nPr(4, 'A', 'B', 'C')
                .combinationOrder();
    }
}