package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KPermutationLexOrderTest {

    @Test
    public void shouldGeneratedOutputInLexOrder(){
        int k= 4;
        List<String> input = Arrays.asList("A","B","C","D","E","F","G","H");
        String output = JNumberTools.permutationsOf(input)
                .k(k)
                .lexOrder()
                .stream()
                .collect(Collectors.toList()).toString();

        List<String> sorted = new ArrayList<>();
        JNumberTools.permutationsOf(input)
                .k(k)
                .combinationOrder()
                .stream()
                .forEach(e-> sorted.add(e.toString()));

        Collections.sort(sorted);
        String expected = sorted.toString();
        Assert.assertEquals(expected, output);



    }
}