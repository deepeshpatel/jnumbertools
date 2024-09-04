package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.everyMthValue;

public class MultisetPermutationMthTest {

    @Test
    public void assertCount(){
        //TODO: complete this
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeIncrementValues(){

        var elements = List.of("A", "B", "C");
        int[] frequencies = {-1,3,2};

        permutation.multiset(elements, frequencies)
                .lexOrderMth(5);
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects() {

        var elements = List.of("A", "B", "C");
        int[] frequencies = {3,2,3};

        var iterable = permutation
                .multiset(elements, frequencies)
                .lexOrderMth(3);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectMthMultisetPermutation() {
        List<String> input = List.of("A","B","C");
        int[] freqArray = {3, 2, 3};
        for(int increment = 1; increment<=24; increment++) {
            String output = getResultViaDirectIncrement(input,increment, freqArray);
            String expected = getExpectedResultViaOneByOneIteration(input, increment, freqArray);
            Assert.assertEquals(expected, output);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int increment, int[] freqArray) {
        return permutation.multiset(input, freqArray)
                .lexOrderMth(increment)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int increment, int[] freqArray) {
        var stream = permutation.multiset(input, freqArray).lexOrder().stream();
        return everyMthValue(stream, increment).toString();
    }
}
