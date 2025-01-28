package io.github.deepeshpatel.jnumbertools.numbersystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class PermutadicTest {

    @Test
    void shouldGenerateCorrectPermutadicForFirst7Values() {
        String[] expected = {
                "[0](3)",
                "[1](3)",
                "[2](3)",
                "[3](3)",
                "[1,0](3)",
                "[1,1](3)",
                "[1,2](3)" };

        for(int i=0; i<=6; i++) {
            Assertions.assertEquals(expected[i], Permutadic.of(BigInteger.valueOf(i),8-5).toString());
        }
    }

    @Test
    void shouldBeEqualToFactoradicForSizeEqualsDegree() {
        int start = 565656565;
        for(int i=start; i<=start+10; i++) {
            Factoradic f = Factoradic.of(i);
            List<Integer> permutadicValue = Permutadic.of(BigInteger.valueOf(i), 0).permutadicValues;
            List<Integer> factorialValues = f.factoradicValues;
            Assertions.assertEquals(factorialValues.toString(), permutadicValue.toString());
        }
    }

    @Test
    void shouldEncodeAndDecodeToCorrectDecimalValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i<1679; i++) {
            Permutadic permutadic1 = Permutadic.of(BigInteger.valueOf(i), size-degree);
            int[] mthPermutation = permutadic1.toMthPermutation(4);
            Permutadic permutadic2 = Permutadic.fromMthPermutation(mthPermutation,size-degree);
            Assertions.assertEquals(i, permutadic2.decimalValue.intValue());
        }
    }

    @Test
    void shouldResultInCorrectValueToAndFromMthPermutation(){
        int size = 6;
        int degree = 3;
        for(long i=0; i<calculator.nPr(size,degree).longValue(); i++) {
            Permutadic p1 = Permutadic.of(BigInteger.valueOf(i), size-degree);
            int[] mth = p1.toMthPermutation(degree);
            Permutadic p2 = Permutadic.fromMthPermutation(mth,size-degree);
            Assertions.assertEquals(p1, p2);
            Assertions.assertEquals(p1.hashCode(), p2.hashCode());
        }
    }

    @Test
    void shouldDecodeToMthPermutationForLastPossibleValue() {
        int[] expected = {7, 6, 5, 4};
        int[] permutation = Permutadic.of(BigInteger.valueOf(1679),8-4).toMthPermutation(4);
        //8P4 = 1680 so 1679 should result is last possible permutation
        assertArrayEquals(expected, permutation);
    }


    @Test
    void shouldGenerateCorrectPermutadicForOutOofPermutationRange() {
        String output = Permutadic.of(BigInteger.valueOf(1680),8-4).toString();
        Assertions.assertEquals("[1,0,0,0,0](4)", output);
    }

    @Test
    void unRankingAndRankingShouldGenerateSameValue() {
        int size = 8;
        int degree = 4;
        for(long i=0; i< calculator.nPr(size,degree).longValue(); i++) {
            Permutadic permutadic = Permutadic.of(i,size-degree);
            int[] ithPermutation = permutadic.toMthPermutation(4);
            BigInteger rank = Permutadic.fromMthPermutation(ithPermutation, size-degree).decimalValue;
            Assertions.assertEquals(i, rank.longValue());
        }
    }

    @Test
    void shouldGenerateCorrectMathExpression() {
        String expected =
                "2(²⁸P₁₄) + 17(²⁷P₁₃) + 22(²⁶P₁₂) + 20(²⁵P₁₁) + 12(²⁴P₁₀) + " +
                "10(²³P₉) + 2(²²P₈) + 17(²¹P₇) + 12(²⁰P₆) + 15(¹⁹P₅) + " +
                "18(¹⁸P₄) + 2(¹⁷P₃) + 0(¹⁶P₂) + 8(¹⁵P₁) + 7(¹⁴P₀)";

        Permutadic permutadic = Permutadic.of(Long.MAX_VALUE, 14);
        Assertions.assertEquals(expected, permutadic.toMathExpression());
    }
}