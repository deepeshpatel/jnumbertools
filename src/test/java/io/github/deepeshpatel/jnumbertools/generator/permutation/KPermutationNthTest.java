package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nPr;

public class KPermutationNthTest {

    @Test
    public void assertCount(){
        int skip=2;
        for(int n=0; n<=4; n++) {
            List<String> input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = JNumberTools.permutationsOf(input)
                        .kNth(k,skip)
                        .stream().count();
                double expected = Math.ceil(nPr(n, k)/(double)skip);
                Assert.assertEquals((long)expected, size);
            }
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        new KPermutationNth<>(Collections.emptyList(), -1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        new KPermutationNth<>(Collections.emptyList(), 1, 3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNegativeSkipValue() {
        new KPermutationNth<>(Collections.emptyList(), 1, 0);
    }

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
        return new KPermutationNth<>(input, k, skip)
                .stream().collect(Collectors.toList()).toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int skip) {
        List<List<String>> allPermutations = JNumberTools.permutationsOf(input)
                .k(k)
                .stream().collect(Collectors.toList());

        List<List<String>> viaAllPermutations = new ArrayList<>();
        int i=0;
        for(List<String> l: allPermutations) {
            if(skip ==0 || i%skip==0) {
                viaAllPermutations.add(l);
            }
            i++;
        }
        return viaAllPermutations.toString();
    }
}