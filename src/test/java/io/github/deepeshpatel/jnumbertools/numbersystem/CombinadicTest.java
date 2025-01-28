package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.numberSystem;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinadicTest {

    @Test
    void shouldReturnCorrectDecimalEquivalentOfCombinadic() {
        for (int i = 0; i <= 100; i++) {
            var decimal = numberSystem.combinadic(i, 3).decimalValue.intValue();
            assertEquals(i, decimal);
        }
    }

    @Test
    void shouldGenerateCorrectValuesForFirst7Combinadic() {
        String expect = "[[1, 0], [2, 0], [2, 1], [3, 0], [3, 1], [3, 2], [4, 0]]";
        List<String> output = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            output.add(Combinadic.of(i, 2, calculator).toString());
        }
        assertEquals(expect, output.toString());
    }

    @Test
    void shouldGenerateCorrectValueOfNextCombinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = numberSystem.combinadic(1000, 5);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for (int i = 1; i <= 6; i++) {
            c = c.next();
            output.add(c.toString());
        }

        assertEquals(expected, output.toString());
    }

    @Test
    void shouldGenerateCorrectValueOfNext7Combinadic() {

        String expected = "[[12, 9, 8, 7, 5], [12, 9, 8, 7, 6], [12, 10, 2, 1, 0], [12, 10, 3, 1, 0], " +
                "[12, 10, 3, 2, 0], [12, 10, 3, 2, 1], [12, 10, 4, 1, 0]]";

        Combinadic c = Combinadic.of(1000, 5, calculator);
        List<String> output = new ArrayList<>();
        output.add(c.toString());

        for (int i = 1; i <= 6; i++) {
            c = c.next();
            output.add(c.toString());
        }

        assertEquals(expected, output.toString());
    }

    @Test
    void shouldReturnCorrectDegreeForCombinadic() {
        for (int degree = 0; degree <= 10; degree++) {
            Combinadic c = Combinadic.of(BigInteger.TEN, degree, calculator);
            assertEquals(degree, c.degree());
        }
    }
}
