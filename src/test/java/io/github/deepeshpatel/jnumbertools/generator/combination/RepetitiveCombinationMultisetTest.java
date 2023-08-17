package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class RepetitiveCombinationMultisetTest {

    @Test
    public void assertCount() {
        //TODO: Whats the count for this
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        RepetitiveCombinationMultiset<String> iterable = JNumberTools.combinations()
                .of(List.of("A","B","C"),2)
                .repetitiveMultiset(new int[]{2,3,2});

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateCorrectCombinationsWithPrimitiveArrayAndWrapperArrayAndList() {

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

        String outputWithPrimitiveArray = JNumberTools.combinations()
                .of(List.of("Red", "Green", "Blue","Yellow"),3)
                .repetitiveMultiset(new int[]{3,2,1,1}) //3 red ,2 green, 1 blue, 1 yellow
                .stream().toList().toString();

        String outputWithWrapperArray = JNumberTools.combinations()
                .of(List.of("Red", "Green", "Blue","Yellow"),3)
                .repetitiveMultiset(new Integer[]{3,2,1,1}) //3 red ,2 green, 1 blue, 1 yellow
                .stream().toList().toString();

        String outputWithList = JNumberTools.combinations()
                .of(List.of("Red", "Green", "Blue","Yellow"),3)
                .repetitiveMultiset(List.of(3,2,1,1)) //3 red ,2 green, 1 blue, 1 yellow
                .stream().toList().toString();

        Assert.assertEquals(expected, outputWithPrimitiveArray);
        Assert.assertEquals(expected, outputWithWrapperArray);
        Assert.assertEquals(expected, outputWithList);
    }

    @Test
    public void shouldReturnEmptyListWhenREqualsZero() {
        String output = JNumberTools.combinations().of(List.of("A", "B"),0)
                .repetitiveMultiset(new int[]{3,2}) //3 red ,2 green
                .stream().toList().toString();

        Assert.assertEquals("[[]]", output);
    }

    @Test
    public void shouldReturnEmptyListWhenInputListIsEmpty() {
        String output = JNumberTools.combinations().of(Collections.emptyList(),0)
                .repetitiveMultiset(new int[]{})
                .stream().toList().toString();

        Assert.assertEquals("[[]]", output);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMultisetFreqArrayIsNull() {
        var seed = List.of("A", "B");
        new RepetitiveCombinationMultiset<>(seed, 2, null);
    }

//    @Test void shouldResultSameForPrimitiveArrayAndWrapperArrayAndList() {
//        String expected = "[[Red, Red, Red], " +
//                "[Red, Red, Green], " +
//                "[Red, Red, Blue], " +
//                "[Red, Red, Yellow], " +
//                "[Red, Green, Green], " +
//                "[Red, Green, Blue], " +
//                "[Red, Green, Yellow], " +
//                "[Red, Blue, Yellow], " +
//                "[Green, Green, Blue], " +
//                "[Green, Green, Yellow], " +
//                "[Green, Blue, Yellow]]";
//
//
//
//        Assert.assertEquals(expected, output);
//    }
}