package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;

public class CombinadicTest {

    @Test
    public void shouldReturnCorrectDecimalEquivalentOfCombinadic() {
        for(int i=0; i<=100; i++) {
            Combinadic c = Combinadic.of(i, 3, calculator);
            Assert.assertEquals(i,c.decimalValue.intValue());
        }
    }

    @Test
    public void shouldGenerateCorrectValuesForFirst7Combinadic() {
        String expect = "[[1, 0], [2, 0], [2, 1], [3, 0], [3, 1], [3, 2], [4, 0]]";
        List<String> output = new ArrayList<>();
        for(int i=0; i<=6; i++){
            output.add(Combinadic.of(i, 2, calculator).toString());
        }
        Assert.assertEquals(expect, output.toString());
    }

    @Test
    public void shouldGenerateCorrectValueOfNextCombinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = Combinadic.of(1000, 5, calculator);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for(int i=1; i<=6; i++){
            c = c.next();
            output.add(c.toString());
        }

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldGenerateCorrectValueOfNext7Combinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = Combinadic.of(1000, 5, calculator);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for(int i=1; i<=6; i++){
            c = c.next();
            output.add(c.toString());
        }

        Assert.assertEquals(expected, output.toString());
    }

//    @Test
//    public void shouldGenerateCorrectKthCombinadic() {
//        String expected = "[12, 10, 4, 1, 0]"; //for 1006
//        Combinadic c = Combinadic.of(1000, 5, calculator);
//        c = c.nextKthCombinadic(6,calculator);
//        Assert.assertEquals(expected, c.toString());
//    }

    @Test
    public void shouldReturnCorrectDegreeForCombinadic() {
        for(int degree=0; degree<=10; degree++) {
            Combinadic c = Combinadic.of(BigInteger.TEN, degree, calculator);
            Assert.assertEquals(degree, c.degree());
        }
    }

}