package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
            Assert.assertEquals(expected[i], Factoradic.of(i).toString());
        }
    }

    @Test
    public void shouldReturnCorrectDecimalEquivalentOfFactoradic() {
        for(int i=0; i<=100; i++) {
            Assert.assertEquals(i,Factoradic.of(i).decimalValue.intValue());
        }
    }

    @Test
    public void shouldReturnCorrectFactoradicForFewVeryLargeNumbers(){

        String[] expected = new String[]{
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
        };

        long decimalValue = 4611686018427387904L;
        Factoradic f1 = Factoradic.of(decimalValue);
        Factoradic f2 = Factoradic.of(decimalValue+1);
        Assert.assertEquals(expected[0],f1.toString());
        Assert.assertEquals(expected[1],f2.toString());
    }

    @Test
    public void testEqualsAndHashCodeForCoverage() {
        int decimal = 23456;
        Factoradic f1 = Factoradic.of(decimal);
        Factoradic f2 = Factoradic.of(decimal);
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertEquals(f1.hashCode(), decimal);
    }
}