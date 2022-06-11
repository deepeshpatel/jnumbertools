package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNthTest.collectEveryNthValue;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationLexOrderNthTest {

    @Test
    public void assertCount(){
        int increment = 3;
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .k(k)
                        .lexOrderNth(increment)
                        .stream().count();

                double expected = Math.ceil(nPr(n,k)/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        KPermutationLexOrderNth<String> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .k(2)
                .lexOrderNth(2);
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void shouldGenerateNthKPermutations() {

        List<String> input = Arrays.asList("A","B","C","D","E","F","G");
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

        List<List<Integer>> permutations = JNumberTools.permutationsOf(40)
                .k(20)
                .lexOrderNth(increment)
                .stream().collect(Collectors.toList());

        for(List<Integer> permutation: permutations) {
            int[] p = permutation.stream().mapToInt(Integer::intValue).toArray();
            BigInteger rank  = PermutadicAlgorithms.rank(p,40);
            Assert.assertEquals(n,rank);
            n = n.add(increment);
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrderNth(increment)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        Stream<List<String>> stream = JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrder().stream();

        return collectEveryNthValue(stream, increment).toString();
    }
}