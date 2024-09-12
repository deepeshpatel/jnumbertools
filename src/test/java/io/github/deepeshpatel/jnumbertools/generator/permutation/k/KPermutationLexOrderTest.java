package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;


public class KPermutationLexOrderTest {

    @Test
    public void assertCount(){
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k,input)
                        .lexOrder()
                        .stream().count();
                Assert.assertEquals(calculator.nPr(n, k).longValue(), size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.nPk(2,"A", "B", "C").lexOrder();
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGeneratedOutputInLexOrder(){
        int k= 4;
        var input = List.of("A","B","C","D","E","F","G","H");
        String output = permutation.nPk(k,input)
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
        Assert.assertEquals(expected, output);
    }
}