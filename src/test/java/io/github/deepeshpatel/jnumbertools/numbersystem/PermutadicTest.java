package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class PermutadicTest {

    @Test
    public void shouldGenerateCorrectPermutadicForFirst7Values() {
        String expected = "[[0, 0, 0, 0, 0][8], [0, 0, 0, 0, 1][8], [0, 0, 0, 0, 2][8], [0, 0, 0, 0, 3][8]," +
                " [0, 0, 0, 1, 0][8], [0, 0, 0, 1, 1][8], [0, 0, 0, 1, 2][8]]";

        List<String> output = new ArrayList<>();
        for(int i=0; i<=6; i++) {
            output.add(new Permutadic(i, 8,5).toString());
        }

        Assert.assertEquals(expected, output.toString());
    }

    @Test
    public void shouldBeEqualToFactoradicForkEqualsS() {
        int start = 565656565;
        for(int i=start; i<=start+10; i++) {
            Factoradic f = new Factoradic(i);
            int[] permutadicValue = new Permutadic(i, f.getValues().size(),f.getValues().size()).getValue();
            String expected = f.toString();
            String output = Arrays.toString(permutadicValue);
            Assert.assertEquals(expected,output);

        }
    }

    @Test
    public void calculatedDeepCodeAndDecimalValueShouldBeEqual(){
        int s = 10;
        int k = 5;
        for(long i=0; i<nPr(s,k); i++) {
            int[] p = new Permutadic(i, s, k).getValue();
            int[] nth = Permutadic.decodePermutadicToNthPermutation(p,s);
            int[] encode = Permutadic.encodeNthPermutationToPermutadic(nth,s);
            long decimalValue = Permutadic.toDecimal(p,s);

            Assert.assertEquals(i,decimalValue);
            Assert.assertArrayEquals(p, encode);
        }
    }
}