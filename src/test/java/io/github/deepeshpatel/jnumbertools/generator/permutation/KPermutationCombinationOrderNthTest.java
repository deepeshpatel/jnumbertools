package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationCombinationOrderNthTest {

    @Test
    public void assertCount(){
        int increment=2;
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
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
        KPermutationCombinationOrderNth<String> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .k(2)
                .combinationOrderNth(2);
        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        JNumberTools.permutationsOf().k(-1).combinationOrderNth(3);
        //new KPermutationCombinationOrderNth<>(Collections.emptyList(), -1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        JNumberTools.permutationsOf().k(1).combinationOrderNth(3);
        //new KPermutationCombinationOrderNth<>(Collections.emptyList(), 1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeIncrementValue() {
        JNumberTools.permutationsOf().k(1).combinationOrderNth(0);
        //new KPermutationCombinationOrderNth<>(Collections.emptyList(), 1, 0);
    }

    @Test
    public void shouldGenerateNthKPermutations() {

        List<String> input = Arrays.asList("A","B","C","D","E","F");
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
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        Stream<List<String>> stream = JNumberTools.permutationsOf(input).k(k).combinationOrder().stream();
        return TestUtil.collectEveryNthValue(stream, increment).toString();
    }
}