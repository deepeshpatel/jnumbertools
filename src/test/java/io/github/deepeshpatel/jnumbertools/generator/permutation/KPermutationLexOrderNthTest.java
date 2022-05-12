package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.generator.TestUtil.iteratorToList;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationLexOrderNthTest {

    @Test
    public void assertCount(){
        int increment = 3;
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .k(k)
                        .lexOrderNth(increment)
                        .stream().count();

                double expected = Math.ceil(nPr(n,k)/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        Iterable<List<String>> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .k(2)
                .lexOrderNth(2);
        List<List<String>> lists1 = iteratorToList(iterable.iterator());
        List<List<String>> lists2 = iteratorToList(iterable.iterator());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateNthKPermutations() {

        List<String> input = Arrays.asList("A","B","C","D","E","F","G");
        for(int k=1; k<= input.size()/2; k++) {
            for(int increment=1; increment<=5;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected,output);
            }
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrderNth(increment)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        Iterable<List<String>> iterable = JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrder();

        return TestUtil.collectEveryNthValue(iterable, increment).toString();
    }

}