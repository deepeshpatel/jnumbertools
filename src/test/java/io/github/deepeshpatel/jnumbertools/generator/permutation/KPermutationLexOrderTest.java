package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationLexOrderTest {

    @Test
    public void assertCount(){
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .k(k)
                        .lexOrder()
                        .stream().count();
                Assert.assertEquals(nPr(n, k), size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        KPermutationLexOrder<String> iterable = JNumberTools.permutationsOf("A", "B", "C").k(2).lexOrder();
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGeneratedOutputInLexOrder(){
        int k= 4;
        List<String> input = Arrays.asList("A","B","C","D","E","F","G","H");
        String output = JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrder()
                .stream()
                .collect(Collectors.toList()).toString();

        List<String> sorted = new ArrayList<>();
        JNumberTools.permutationsOf(input)
                .k(k)
                .combinationOrder()
                .stream()
                .forEach(e-> sorted.add(e.toString()));

        Collections.sort(sorted);
        String expected = sorted.toString();
        Assert.assertEquals(expected, output);
    }
}