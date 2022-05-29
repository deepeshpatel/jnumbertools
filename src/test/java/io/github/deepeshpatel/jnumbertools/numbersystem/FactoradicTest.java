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

        //large factoradic example
        //3,19,21,5,17,2,11,16,10,1,2,7,3,10,9,7,4,1,3,0,1,0,1,0
        //99999223372036854775807

        String[] expected = new String[]{
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 0, 0]",
                "[1, 17, 17, 5, 9, 7, 12, 1, 4, 1, 2, 9, 5, 3, 1, 4, 2, 2, 2, 1, 0]"
        };

        long decimalValue = 4611686018427387904L;
        Factoradic f1 = new Factoradic(BigInteger.valueOf(decimalValue));
        Factoradic f2 = new Factoradic(BigInteger.valueOf(decimalValue+1));
        //Factoradic f2ViaAddition = f1.add(1);
        Assert.assertEquals(expected[0],f1.toString());
        Assert.assertEquals(expected[1],f2.toString());
        //Assert.assertEquals(expected[1],f2ViaAddition.toString());

    }

//    @Test
//    public void shouldGenerateCorrectValueForNextKthFactoradic(){
//        for(int i=0; i<=20; i++) {
//            Factoradic f = new Factoradic(i);
//            for(int j=0; j<100; j++) {
//                Factoradic nextKth = f.add(j);
//                Factoradic expected = new Factoradic(i+j);
//                Assert.assertEquals(expected, nextKth);
//            }
//        }
//    }
}