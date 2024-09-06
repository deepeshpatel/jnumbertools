package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.everyMthValue;

public class KPermutationLexOrderMthTest {

    @Test
    public void assertCount(){
        int increment = 3;
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPr(k, input)
                        .lexOrderMth(increment, 0)
                        .stream().count();

                double expected = Math.ceil(calculator.nPr(n,k).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.nPr(2,"A", "B", "C")
                .lexOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateMthKPermutations() {

        var input = List.of("A","B","C","D","E","F","G");
        for(int k=1; k<= input.size()/2; k++) {
            for(int increment=1; increment<=5;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected,output);
            }
        }
    }

    @Test
    public void shouldSupportVeryLargeMthKPermutation() {

        //find every 10^29 th 20-Permutation of 40 elements(20P40)
        BigInteger increment = new BigInteger("100000000000000000000000000000");
        BigInteger expectedRank = BigInteger.ZERO;

        var permutations = permutation.nPr(40,20)
                .lexOrderMth(increment, BigInteger.ZERO)
                .stream().toList();


        for(List<Integer> permutation: permutations) {
            int[] p = permutation.stream().mapToInt(Integer::intValue).toArray();
            BigInteger rank  = PermutadicAlgorithms.rank(40, p);
            Assert.assertEquals(expectedRank,rank);
            expectedRank = expectedRank.add(increment);
        }
    }

    @Test
    public void test_start_parameter_greater_than_0() {
        var output = permutation.nPr(3, 'a','b','c','d','e')
                .lexOrderMth(20, 5)
                .stream().toList().toString();
        Assert.assertEquals("[[a, c, e], [c, a, d], [d, e, a]]", output);
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return permutation.nPr(k, input)
                .lexOrderMth(increment, 0)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        var stream = permutation.nPr(k, input)
                .lexOrder().stream();

        return everyMthValue(stream, increment).toString();
    }
}