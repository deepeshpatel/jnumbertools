package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UniquePermutationInSizeRangeTest {

    @Test
    public void shouldGenerateAllPermutationsInSizeRange2To3() {

        int from = 0;
        int to = 3;
        List<Integer> input = IntStream.range(0,4).boxed().collect(Collectors.toList());

        List<List<Integer>> outputViaKPermutation = new ArrayList<>();
        for(int i=from; i<=to; i++) {
            List<List<Integer>> collect = JNumberTools.permutationsOf(input)
                    .k(i)
                    .stream().collect(Collectors.toList());
            outputViaKPermutation.addAll(collect);
        }

        List<List<Integer>> output = JNumberTools.permutationsOf(input)
                .uniqueInSizeRange(from, to)
                .stream().collect(Collectors.toList());

        Assert.assertEquals(outputViaKPermutation.toString(), output.toString());

    }
}