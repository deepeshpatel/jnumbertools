package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class PermutadicTest {

    @Test
    public void shouldGenerateCorrectPermutadicForFirst7Values() {
        String[] expected = new String[] {
                "[0](3)",
                "[1](3)",
                "[2](3)",
                "[3](3)",
                "[1,0](3)",
                "[1,1](3)",
                "[1,2](3)" };

        for(int i=0; i<=6; i++) {
            Assert.assertEquals(expected[i], Permutadic.of(BigInteger.valueOf(i),8-5).toString());
        }
    }

    @Test
    public void shouldBeEqualToFactoradicForSizeEqualsDegree() {
        int start = 565656565;
        for(int i=start; i<=start+10; i++) {
            Factoradic f = Factoradic.of(i);
            List<Integer> permutadicValue = Permutadic.of(BigInteger.valueOf(i), 0).permutadicValues;
            List<Integer> factorialValues = f.factoradicValues;
            Assert.assertEquals(factorialValues.toString(), permutadicValue.toString());
        }
    }

    @Test
    public void shouldEncodeAndDecodeToCorrectDecimalValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<1679; i++) {
            Permutadic permutadic1 = Permutadic.of(BigInteger.valueOf(i), size-degree);
            int[] nthPermutation = permutadic1.toNthPermutation(4);
            Permutadic permutadic2 = Permutadic.fromNthPermutation(nthPermutation,size-degree);
            Assert.assertEquals(i, permutadic2.decimalValue.intValue());
        }
    }

    @Test
    public void shouldResultInCorrectValueToAndFromNthPermutation(){
        int size = 6;
        int degree = 3;
        for(long i=0; i<nPr(size,degree); i++) {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(i), size-degree);
            int[] nth = p1.toNthPermutation(degree);
            Permutadic p2 = Permutadic.fromNthPermutation(nth,size-degree);
            Assert.assertEquals(p1, p2);
            Assert.assertEquals(p1.hashCode(), p2.hashCode());
        }
    }

    @Test
    public void shouldDecodeToNthPermutationForLastPossibleValue() {
        int[] perm = Permutadic.of(BigInteger.valueOf(1679),8-4).toNthPermutation(4);
        //8P4 = 1680 so 1679 should result is last possible permutation
        Assert.assertEquals("[7, 6, 5, 4]", Arrays.toString(perm));
    }


    @Test
    public void shouldGenerateCorrectPermutadicForOutOofPermutationRange() {
        String output = Permutadic.of(BigInteger.valueOf(1680),8-4).toString();
        Assert.assertEquals("[1,0,0,0,0](4)", output);
    }

    @Test
    public void unRankingAndRankingShouldGenerateSameValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<nPr(size,degree); i++) {
            Permutadic permutadic = Permutadic.of(i,size-degree);
            int[] ithPermutation = permutadic.toNthPermutation(4);
            BigInteger rank = Permutadic.fromNthPermutation(ithPermutation, size-degree).decimalValue;
            Assert.assertEquals(i, rank.longValue());
        }
    }

    @Test
    public void shouldGenerateCorrectMathExpression() {
        String expected =
                "2(²⁸P₁₄) + 17(²⁷P₁₃) + 22(²⁶P₁₂) + 20(²⁵P₁₁) + 12(²⁴P₁₀) + " +
                "10(²³P₉) + 2(²²P₈) + 17(²¹P₇) + 12(²⁰P₆) + 15(¹⁹P₅) + " +
                "18(¹⁸P₄) + 2(¹⁷P₃) + 0(¹⁶P₂) + 8(¹⁵P₁) + 7(¹⁴P₀)";

        Permutadic permutadic = Permutadic.of(Long.MAX_VALUE, 14);
        Assert.assertEquals(expected, permutadic.toMathExpression());
    }
}