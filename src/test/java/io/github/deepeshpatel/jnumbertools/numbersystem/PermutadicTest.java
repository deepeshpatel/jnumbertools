package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.Factoradic;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.Permutadic;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class PermutadicTest {

    @Test
    public void shouldGenerateCorrectPermutadicForFirst7Values() {
        String[] expected = new String[] {
                "[0, 0, 0, 0, 0][8]",
                "[0, 0, 0, 0, 1][8]",
                "[0, 0, 0, 0, 2][8]",
                "[0, 0, 0, 0, 3][8]",
                "[0, 0, 0, 1, 0][8]",
                "[0, 0, 0, 1, 1][8]",
                "[0, 0, 0, 1, 2][8]" };

        for(int i=0; i<=6; i++) {
            Assert.assertEquals(expected[i], Permutadic.of(BigInteger.valueOf(i),8,5).toString());
        }
    }

    @Test
    public void shouldBeEqualToFactoradicForSizeEqualsDegree() {
        int start = 565656565;
        for(int i=start; i<=start+10; i++) {
            Factoradic f = new Factoradic(BigInteger.valueOf(i));
            int size = f.getValues().length;
            int[] permutadicValue = Permutadic.of(BigInteger.valueOf(i),size,size).getValue();
            Assert.assertEquals(f.toString(), Arrays.toString(permutadicValue));

        }
    }

    @Test
    public void shouldEncodeAndDecodeToCorrectDecimalValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<1679; i++) {
            Permutadic permutadic1 = Permutadic.of(BigInteger.valueOf(i), size, degree);
            int[] nthPermutation = permutadic1.toNthPermutationWithoutBoundCheck();
            Permutadic permutadic2 = Permutadic.fromNthPermutation(nthPermutation,size);
            Assert.assertEquals(i, permutadic2.decimalValue().intValue());
        }
    }

    @Test
    public void calculatedDeepCodeAndDecimalValueShouldBeEqual(){
        int size = 6;
        int degree = 3;
        for(long i=0; i<nPr(size,degree); i++) {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(i), size, degree);
            int[] nth = p1.toNthPermutationWithoutBoundCheck();
            Permutadic p2 = Permutadic.fromNthPermutation(nth,size);
            Assert.assertArrayEquals(p1.getValue(), p2.getValue());
        }
    }

    @Test
    public void shouldDecodeToNthPermutationForLastPossibleValue() {
        int[] perm = Permutadic.of(BigInteger.valueOf(1679),8,4).toNthPermutationWithBoundCheck();
        //8P4 = 1680 so 1679 should result is last possible permutation
        Assert.assertEquals("[7, 6, 5, 4]", Arrays.toString(perm));
    }
//
    @Test (expected = ArithmeticException.class)
    public void shouldThroughExceptionWhileDecodingOutOfRangeValue(){
        Permutadic.of(BigInteger.valueOf(1680),8,4).toNthPermutationWithBoundCheck();
    }

    @Test
    public void shouldGenerateCorrectPermutadicForOutOofPermutationRange() {
        String output = Permutadic.of(BigInteger.valueOf(1680),8,4).toString();
        Assert.assertEquals("[8, 0, 0, 0][8]", output);
    }

    @Test
    public void unRankingAndRankingShouldGenerateSameValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<nPr(size,degree); i++) {
            int[] ithPermutation = PermutadicAlgorithms.unRankWithoutBoundCheck(BigInteger.valueOf(i),size,degree);
            BigInteger rank = PermutadicAlgorithms.rank(ithPermutation,size);
            Assert.assertEquals(i, rank.longValue());
        }
    }
}