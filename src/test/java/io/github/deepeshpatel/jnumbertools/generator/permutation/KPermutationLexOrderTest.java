package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationLexOrderTest {

    @Test
    public void assertCount(){
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutations().of(input)
                        .k(k)
                        .lexOrder()
                        .stream().count();
                Assert.assertEquals(nPr(n, k), size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = JNumberTools.permutations().of("A", "B", "C").k(2).lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGeneratedOutputInLexOrder(){
        int k= 4;
        var input = List.of("A","B","C","D","E","F","G","H");
        String output = JNumberTools.permutations().of(input)
                .k(k)
                .lexOrder()
                .stream()
                .toList().toString();

        var sorted = new ArrayList<String>();
        JNumberTools.permutations().of(input)
                .k(k)
                .combinationOrder()
                .stream()
                .forEach(e-> sorted.add(e.toString()));

        Collections.sort(sorted);
        String expected = sorted.toString();
        Assert.assertEquals(expected, output);
    }
}