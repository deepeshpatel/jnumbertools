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
    public void shouldBeEqualToFactoradicForSizeEqualsDegree() {
        int start = 565656565;
        for(int i=start; i<=start+10; i++) {
            Factoradic f = new Factoradic(i);
            int size = f.getValues().size();
            int[] permutadicValue = new Permutadic(i, size,size).getValue();
            String expected = f.toString();
            String output = Arrays.toString(permutadicValue);
            Assert.assertEquals(expected,output);

        }
    }

    @Test
    public void shouldConvertToPermutadicToCorrectDecimalValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<1679; i++) {
            int[] decoded = new Permutadic(i, size, degree).decodeToNthPermutation();
            Permutadic permutadic = Permutadic.encodeNthPermutation(decoded,size);
            Assert.assertEquals(i,permutadic.toDecimal());
        }
    }

    @Test
    public void calculatedDeepCodeAndDecimalValueShouldBeEqual(){
        int size = 6;
        int degree = 3;
        for(long i=0; i<nPr(size,degree); i++) {
            Permutadic p1 = new Permutadic(i, size, degree);
            int[] nth = p1.decodeToNthPermutation();
            Permutadic p2 = Permutadic.encodeNthPermutation(nth,size);
            Assert.assertArrayEquals(p1.getValue(), p2.getValue());
        }
    }

    @Test
    public void shouldDecodeToNthPermutationForLastPossibleValue() {
        int[] perm = new Permutadic(1679,8,4).decodeToNthPermutation();
        //8P4 = 1680 so 1679 should result is last possible permutation
        Assert.assertEquals("[7, 6, 5, 4]", Arrays.toString(perm));
    }

    @Test (expected = ArithmeticException.class)
    public void shouldThroughExceptionWhileDecodingOutOfRangeValue(){
        new Permutadic(1680,8,4).decodeToNthPermutation();
    }
}