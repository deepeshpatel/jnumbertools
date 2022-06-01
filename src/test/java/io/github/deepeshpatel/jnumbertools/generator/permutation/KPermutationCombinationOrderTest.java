package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;
import static org.junit.Assert.assertEquals;

public class KPermutationCombinationOrderTest {

    @Test
    public void assertCount(){
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .k(k)
                        .combinationOrder()
                        .stream().count();
                Assert.assertEquals(nPr(n, k), size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        KPermutationCombinationOrder<String> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .k(2)
                .combinationOrder();
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeK() {
        JNumberTools.permutationsOf(new ArrayList<>()).k(-3).combinationOrder();
    }

    @Test
    public void shouldNotThrowExceptionForZeroK() {
        JNumberTools.permutationsOf(new ArrayList<>()).k(0);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOf3Values(){
        String expected = "[[1, 2], [2, 1], [1, 3], [3, 1], [2, 3], [3, 2]]";
        String actual   = JNumberTools.permutationsOf("1", "2", "3")
                .k(2)
                .combinationOrder()
                .stream().collect(Collectors.toList()).toString();

        assertEquals(expected,actual);
    }

    @Test
    public void shouldGenerateKPermutations() {
        String expected = "[[A, B], [B, A], [A, C], [C, A], [B, C], [C, B]]";

        List<List<String>> output = JNumberTools.permutationsOf("A", "B", "C")
                .k(2)
                .combinationOrder()
                .stream()
                .collect(Collectors.toList());
        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateOneEmptyPermutationForKEqualsZero() {

        List<List<String>> output = JNumberTools.permutationsOf("A")
                .k(0)
                .combinationOrder()
                .stream()
                .collect(Collectors.toList());
        Assert.assertEquals("[[]]", output.toString());
    }
}