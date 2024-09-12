package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.*;

public class KPermutationLexOrderMthTest {

    @Test
    public void assertCount(){
        int increment = 3;
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPk(k, input)
                        .lexOrderMth(increment, 0)
                        .stream().count();

                double expected = Math.ceil(calculator.nPr(n,k).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.nPk(2,"A", "B", "C")
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
        String expected = "[[11, 37, 6, 5, 26, 15, 0, 25, 9, 22, 27, 16, 12, 21, 24, 31, 33, 34, 17, 38], " +
                          "[23, 34, 12, 11, 10, 28, 1, 8, 18, 3, 9, 30, 24, 0, 6, 20, 26, 27, 37, 35], " +
                          "[35, 30, 18, 16, 38, 1, 2, 32, 23, 22, 33, 3, 29, 17, 26, 6, 12, 14, 11, 28]]";

        var permutations = permutation.nPk(40,20)
                .lexOrderMth(increment, increment)
                .stream().toList();
        Assert.assertEquals(expected, permutations.toString());
    }

    @Test
    public void test_start_parameter_greater_than_0() {
        var output = permutation.nPk(3, 'a','b','c','d','e')
                .lexOrderMth(20, 5)
                .stream().toList().toString();
        Assert.assertEquals("[[a, c, e], [c, a, d], [d, e, a]]", output);
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return permutation.nPk(k, input)
                .lexOrderMth(increment, 0)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        var stream = permutation.nPk(k, input)
                .lexOrder().stream();

        return everyMthValue(stream, increment).toString();
    }
}