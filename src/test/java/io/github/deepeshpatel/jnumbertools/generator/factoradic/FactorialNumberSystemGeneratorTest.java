package io.github.deepeshpatel.jnumbertools.generator.factoradic;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.stream.Collectors;

public class FactorialNumberSystemGeneratorTest {

    @Test
    public void shouldGenerateCorrectValuesForFirstSixNumbers() {

        String expected = "[[0], [1, 0], [1, 0, 0], [1, 1, 0], [2, 0, 0], [2, 1, 0]]";
        String output = JNumberTools.factoradic(0,6).stream().collect(Collectors.toList()).toString();
        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateCorrectValuesForFrom10To11() {
        String expected = "[[1, 2, 0, 0], [1, 2, 1, 0]]";
        String output = JNumberTools.factoradic(10,12).stream().collect(Collectors.toList()).toString();
        Assert.assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateCorrectValuesViaBinInteger() {
        String expected = "[[1, 2, 0, 0], [1, 2, 1, 0]]";
        BigInteger fromInclusive = BigInteger.TEN;
        BigInteger toExclusive = BigInteger.valueOf(12);

        String output = JNumberTools.factoradic(fromInclusive,toExclusive)
                .stream().collect(Collectors.toList()).toString();
        Assert.assertEquals(expected, output);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEndValueLessOrEqualsStart() {
        JNumberTools.factoradic(10,10).stream();
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeStartingValue() {
        JNumberTools.factoradic(-1,1).stream();
    }
}