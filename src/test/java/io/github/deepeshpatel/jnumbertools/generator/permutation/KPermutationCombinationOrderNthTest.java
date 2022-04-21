package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationCombinationOrderNthTest {

    @Test
    public void assertCount(){
        int skip=2;
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .k(k)
                        .combinationOrderNth(skip)
                        .stream().count();
                double expected = Math.ceil(nPr(n, k)/(double)skip);
                Assert.assertEquals((long)expected, size);
            }
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        new KPermutationCombinationOrderNth<>(Collections.emptyList(), -1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        new KPermutationCombinationOrderNth<>(Collections.emptyList(), 1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeSkipValue() {
        new KPermutationCombinationOrderNth<>(Collections.emptyList(), 1, 0);
    }

    @Test
    public void shouldGenerateKPermutationsSkippingInBetween() {

        List<String> input = Arrays.asList("A","B","C","D","E","F");
        for(int k=1; k<= input.size()/2; k++) {
            for(int skip=1; skip<=32;skip++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,skip);
                String output   = getResultViaDirectSkipping(input,k,skip);
                Assert.assertEquals(expected,output);
            }
        }
    }

    private String getResultViaDirectSkipping(List<String> input, int k, int skip) {
        return new KPermutationCombinationOrderNth<>(input, k, skip)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int skip) {
        Iterable<List<String>> iterable = JNumberTools.permutationsOf(input)
                .k(k)
                .combinationOrder();

        return TestUtil.collectSkippedValues(iterable, skip).toString();
    }
}