package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.entrypoint.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.generator.combination.UniqueCombinationNthTest.collectEveryNthValue;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationCombinationOrderNthTest {

    @Test
    public void assertCount(){
        int increment=2;
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutations().of(input)
                        .k(k)
                        .combinationOrderNth(increment)
                        .stream().count();
                double expected = Math.ceil(nPr(n, k)/(double)increment);
                Assert.assertEquals((long)expected, size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = JNumberTools.permutations().of("A", "B", "C")
                .k(2)
                .combinationOrderNth(2);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        JNumberTools.permutations().of().k(-1).combinationOrderNth(3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        JNumberTools.permutations().of().k(1).combinationOrderNth(3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeIncrementValue() {
        JNumberTools.permutations().of().k(1).combinationOrderNth(0);
    }

    @Test
    public void shouldGenerateNthKPermutations() {

        var input = List.of("A","B","C","D","E","F");
        for(int k=1; k<= 3; k++) {
            for(int increment=1; increment<=32;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected, output);
            }
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int k, int increment) {
        return new KPermutationCombinationOrderNth<>(input, k, BigInteger.valueOf(increment))
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        var stream = JNumberTools.permutations().of(input).k(k).combinationOrder().stream();
        return collectEveryNthValue(stream, increment).toString();
    }
}