package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RepetitiveCombinationMultisetTest {

    @Test
    public void assertCount() {
        //TODO: Whats the count for this
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        RepetitiveCombinationMultiset<String> iterable = JNumberTools
                .combinationsOf(Arrays.asList("A","B","C"),2)
                .repetitiveMultiset(new int[]{2,3,2});

        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectCombinations() {

        String expected = "[[Red, Red, Red], " +
                "[Red, Red, Green], " +
                "[Red, Red, Blue], " +
                "[Red, Red, Yellow], " +
                "[Red, Green, Green], " +
                "[Red, Green, Blue], " +
                "[Red, Green, Yellow], " +
                "[Red, Blue, Yellow], " +
                "[Green, Green, Blue], " +
                "[Green, Green, Yellow], " +
                "[Green, Blue, Yellow]]";

        String output = JNumberTools
                .combinationsOf(Arrays.asList("Red", "Green", "Blue","Yellow"),3)
                .repetitiveMultiset(new int[]{3,2,1,1}) //3 red ,2 green, 1 blue, 1 yellow
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListWhenREqualsZero() {
        String output = JNumberTools.combinationsOf(Arrays.asList("A", "B"),0)
                .repetitiveMultiset(new int[]{3,2}) //3 red ,2 green
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals("[[]]", output);
    }

    @Test
    public void shouldReturnEmptyListWhenInputListIsEmpty() {
        String output = JNumberTools.combinationsOf(Collections.emptyList(),0)
                .repetitiveMultiset(new int[]{})
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals("[[]]", output);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMultisetFreqArrayIsNull() {
        List<String> seed = Arrays.asList("A", "B");
        new RepetitiveCombinationMultiset<>(seed, 2, null);
    }
}