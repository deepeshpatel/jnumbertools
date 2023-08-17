package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNthTest.collectEveryNthValue;

public class MultisetPermutationNthTest {

    @Test
    public void assertCount(){
        //TODO: complete this
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeIncrementValues(){
        JNumberTools.permutations().of("A","B","C")
                .multisetNth(-1,3,2,3);
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = JNumberTools.permutations()
                .of("A", "B", "C")
                .multisetNth(3, 3, 2, 3);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectNthMultisetPermutation() {
        List<String> input = List.of("A","B","C");
        int[] freqArray = new int[]{3, 2, 3};
        for(int increment = 1; increment<=24; increment++) {
            String output = getResultViaDirectIncrement(input,increment, freqArray);
            String expected = getExpectedResultViaOneByOneIteration(input, increment, freqArray);
            Assert.assertEquals(expected, output);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int increment, int[] freqArray) {
        return JNumberTools.permutations().of(input)
                .multisetNth(increment, freqArray)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int increment, int[] freqArray) {
        var stream = JNumberTools.permutations().of(input).multiset(freqArray).stream();
        return collectEveryNthValue(stream, increment).toString();
    }
}
