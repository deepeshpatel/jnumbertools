package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.tools;
import static io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNthTest.collectEveryNthValue;

public class KPermutationLexOrderNthTest {

    @Test
    public void assertCount(){
        int increment = 3;
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = tools.permutations().of(input)
                        .k(k)
                        .lexOrderNth(increment)
                        .stream().count();

                double expected = Math.ceil(calculator.nPr(n,k).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = tools.permutations().of("A", "B", "C")
                .k(2)
                .lexOrderNth(2);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateNthKPermutations() {

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
    public void shouldSupportVeryLargeNthKPermutation() {

        //find every 10^29 th 20-Permutation of 40 elements(20P40)
        BigInteger increment = new BigInteger("100000000000000000000000000000");
        BigInteger n = BigInteger.ZERO;

        var permutations = tools.permutations().of(40)
                .k(20)
                .lexOrderNth(increment)
                .stream().toList();
        

        for(List<Integer> permutation: permutations) {

            int[] p = permutation.stream().mapToInt(Integer::intValue).toArray();

            BigInteger rank  = PermutadicAlgorithms.rank(40, p);
            Assert.assertEquals(n,rank);
            n = n.add(increment);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return tools.permutations().of(input)
                .k(k)
                .lexOrderNth(increment)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        var stream = tools.permutations().of(input)
                .k(k)
                .lexOrder().stream();

        return collectEveryNthValue(stream, increment).toString();
    }
}