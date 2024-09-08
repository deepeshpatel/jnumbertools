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

    @Test
    public void shouldReturnSingleElementForSingleInput() {
        var list = JNumberTools.permutations().unique(List.of('a')).singleSwap().stream().toList();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(List.of('a'), list.get(0));
    }

    @Test
    public void shouldReturnTwoPermutationsForTwoElements() {
        var list = JNumberTools.permutations().unique('a', 'b').singleSwap().stream().toList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(List.of('a', 'b'), list.get(0));
        Assert.assertEquals(List.of('b', 'a'), list.get(1));
    }

    @Test
    public void shouldHandleLargeInput() {
        var list = JNumberTools.permutations()
                .unique('a', 'b', 'c', 'd', 'e', 'f', 'g')
                .singleSwap().stream().toList();

        // The size should be factorial of the number of elements
        Assert.assertEquals(5040, list.size());
        // Perform a quick check to ensure swaps are correct
        for(int i=1; i<list.size(); i++) {
            Assert.assertEquals(2, diffCount(list.get(i-1), list.get(i)));
        }
    }

    @Test
    public void shouldReturnOneEmptyElementForEmptyInput() {
        var list = JNumberTools.permutations().unique().singleSwap().stream().toList();
        Assert.assertEquals("[[]]", list.toString());
    }

    @Test
    public void shouldHandleIdenticalElementsWithDifferentDataTypes() {
        var list = JNumberTools.permutations().unique('a', 1, 'a', 1).singleSwap().stream().toList();
        Assert.assertTrue(list.contains(List.of('a', 1, 'a', 1)));
        Assert.assertTrue(list.contains(List.of(1, 'a', 'a', 1)));
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