package io.github.deepeshpatel.jnumbertools.generator.permutation.unique;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UniquePermutationSingleSwapTest {

    @Test
    public void should_have_only_single_swap_when_compared_to_previous() {
        var list = JNumberTools.permutations().unique('a','b','c','d','e').singleSwap().stream().toList();
        Assert.assertEquals(120, list.size());
        for(int i=1; i<list.size(); i++) {
            int difCount = diffCount(list.get(i-1), list.get(i));
            Assert.assertEquals(2, difCount);
        }
    }

    private <T> int diffCount(List<T> first, List<T> second) {
        int sum = 0;
        for(int i=0; i< first.size(); i++) {
            if(!first.get(i).equals(second.get(i))) {
                sum++;
            }
        }
        return sum;
    }
}