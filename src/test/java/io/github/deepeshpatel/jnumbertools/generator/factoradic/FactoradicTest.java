package io.github.deepeshpatel.jnumbertools.generator.factoradic;

import io.github.deepeshpatel.jnumbertools.numbersystem.Factoradic;
import org.junit.Assert;
import org.junit.Test;

public class FactoradicTest {

    @Test
    public void shouldGenerateCorrectValuesForFirst7Factoradics() {
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
            Factoradic f = new Factoradic(i);
            Assert.assertEquals(expected[i], f.toString());
        }
    }

    @Test
    public void shouldReturnCorrectFactoradicForFewVeryLargeNumbers(){

        String[] expected = new String[]{
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
        };

        long decimalValue = 4611686018427387904L;
        Factoradic f1 = new Factoradic(decimalValue);
        Factoradic f2 = new Factoradic(decimalValue+1);
        Factoradic f2ViaAddition = f1.add(1);
        Assert.assertEquals(expected[0],f1.toString());
        Assert.assertEquals(expected[1],f2.toString());
        Assert.assertEquals(expected[1],f2ViaAddition.toString());

    }

    @Test
    public void shouldGenerateCorrectValueForNextKthFactoradic(){
        for(int i=0; i<=20; i++) {
            Factoradic f = new Factoradic(i);
            for(int j=0; j<100; j++) {
                Factoradic nextKth = f.add(j);
                Factoradic expected = new Factoradic(i+j);
                Assert.assertEquals(expected, nextKth);
            }
        }
    }
}