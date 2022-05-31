package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.generator.TestUtil.iteratorToList;

public class MultisetPermutationNthTest {

    @Test
    public void assertCount(){
        //TODO: complete this
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeIncrementValues(){
        JNumberTools.permutationsOf("A","B","C")
                .multisetNth(-1,3,2,3);
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        Iterable<List<String>> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .multisetNth(3, 3, 2, 3);

        List<List<String>> lists1 = iteratorToList(iterable.iterator());
        List<List<String>> lists2 = iteratorToList(iterable.iterator());

        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectNthMultisetPermutation() {

        List<String> input = Arrays.asList("A","B","C");
        int[] freqArray = new int[]{3, 2, 3};
        for(int increment = 1; increment<=24; increment++) {

            String output = getResultViaDirectIncrement(input,increment, freqArray);
            String expected = getExpectedResultViaOneByOneIteration(input, increment, freqArray);
            Assert.assertEquals(expected, output);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int increment, int[] freqArray) {
        return JNumberTools.permutationsOf(input)
                .multisetNth(increment, freqArray)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int increment, int[] freqArray) {
        Iterable<List<String>> iterable = JNumberTools.permutationsOf(input)
                .multiset(freqArray);
        return TestUtil.collectEveryNthValue(iterable, increment).toString();
    }
}
