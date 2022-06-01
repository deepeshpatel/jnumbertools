package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.Factoradic;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class FactoradicTest {

    @Test
    public void shouldGenerateCorrectValuesForFirst7Factoradic() {
        String[] expected = new String[] {
                "[0]",
                "[1, 0]",
                "[1, 0, 0]",
                "[1, 1, 0]",
                "[2, 0, 0]",
                "[2, 1, 0]",
                "[1, 0, 0, 0]"
        };
        for(int i=0; i<=6; i++) {
            Factoradic f = new Factoradic(BigInteger.valueOf(i));
            Assert.assertEquals(expected[i], f.toString());
        }
    }

    @Test
    public void shouldReturnCorrectDecimalEquivalentOfFactoradic() {
        for(int i=0; i<=100; i++) {
            Factoradic f = new Factoradic(BigInteger.valueOf(i));
            Assert.assertEquals(i,f.decimalValue().intValue());
        }
    }

    @Test
    public void shouldReturnCorrectFactoradicForFewVeryLargeNumbers(){

        String[] expected = new String[]{
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
        };

        long decimalValue = 4611686018427387904L;
        Factoradic f1 = new Factoradic(BigInteger.valueOf(decimalValue));
        Factoradic f2 = new Factoradic(BigInteger.valueOf(decimalValue+1));
        Assert.assertEquals(expected[0],f1.toString());
        Assert.assertEquals(expected[1],f2.toString());

    }
}