package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CombinadicTest {

    @Test
    public void shouldGenerateCorrectValuesForFirst7Combinadics() {
        String expect = "[[1, 0], [2, 0], [2, 1], [3, 0], [3, 1], [3, 2], [4, 0]]";
        List<String> output = new ArrayList<>();
        for(int i=0; i<=6; i++){
            output.add(new Combinadic(i, 2).toString());
        }
        Assert.assertEquals(expect, output.toString());
    }

    @Test
    public void shouldGenerateCorrectValueOfNextCombinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = new Combinadic(1000, 5);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for(int i=1; i<=6; i++){
            c= c.nextCombinadic();
            output.add(c.toString());
        }

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateCorrectValueOfNext7Combinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = new Combinadic(1000, 5);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for(int i=1; i<=6; i++){
            c= c.nextCombinadic();
            output.add(c.toString());
        }

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateCorrectKthCombinadic() {

        String expected = "[12, 10, 4, 1, 0]"; //for 1006

        Combinadic c = new Combinadic(1000, 5);
        c = c.nextKthCombinadic(6);

        Assert.assertEquals(expected, c.toString());
    }
}