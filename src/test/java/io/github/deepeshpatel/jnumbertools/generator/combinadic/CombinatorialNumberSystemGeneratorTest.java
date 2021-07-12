package io.github.deepeshpatel.jnumbertools.generator.combinadic;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Collectors;

public class CombinatorialNumberSystemGeneratorTest {

    @Test
    public void shouldGenerateCombinadicsOfDegree1() {

        String expected = "[[0], [1], [2], [3]]";
        String output = JNumberTools.combinadic(1, 0, 4)
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateCombinadicsOfDegree2() {

        String expected = "[[1, 0], [2, 0], [2, 1], [3, 0]]";
        String output = JNumberTools.combinadic(2, 0, 4)
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateCombinadicsOfDegree3() {

        String expected = "[[4, 2, 0], [4, 2, 1], [4, 3, 0], " +
                "[4, 3, 1], [4, 3, 2], [5, 1, 0]]";

        String output = JNumberTools.combinadic(3, 5, 11)
                .stream().collect(Collectors.toList()).toString();

        Assert.assertEquals(expected, output);
    }

}