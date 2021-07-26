package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KPermutationLexOrderNthTest {

    @Test
    public void shouldGenerateKPermutationsSkippingInBetween() {

        List<String> input = Arrays.asList("A","B","C","D","E","F","G","H","I","J");
        for(int k=1; k<= input.size()/2; k++) {
            for(int skip=1; skip<=32;skip++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,skip);
                String output   = getResultViaDirectSkipping(input,k,skip);
                Assert.assertEquals(expected,output);

            }
        }
    }

    private String getResultViaDirectSkipping(List<String> input, int k, int skip) {
        return JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrderNth(skip)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int skip) {
        Iterable<List<String>> iterable = JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrder();

        return TestUtil.collectSkippedValues(iterable, skip).toString();
    }

}