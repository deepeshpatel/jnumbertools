package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

public class RepetitiveCombinationLimitedSupplyTest {



    @Test
    public void shouldGenerateCorrectCombinationsWithGivenSupply() {

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

        String output = JNumberTools.combinationsOf("Red", "Green", "Blue","Yellow")
                .repetitiveWithSupply(3, new int[]{3,2,1,1}) //3 red ,2 green
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldReturnEmptyListWhenRLessThanOrEqualsZero() {
        String output = JNumberTools.combinationsOf("A", "B")
                .repetitiveWithSupply(0, new int[]{3,2}) //3 red ,2 green
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals("[]", output);
    }

    @Test
    public void shouldReturnEmptyListWhenInputListIsEmpty() {
        String output = JNumberTools.combinationsOf()
                .repetitiveWithSupply(2, new int[]{3,2})
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals("[]", output);
    }

    @Test
    public void shouldReturnEmptyListWhenSupplyArrayIsNull() {
        String output = JNumberTools.combinationsOf("A","B")
                .repetitiveWithSupply(2,null)
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals("[]", output);
    }
}