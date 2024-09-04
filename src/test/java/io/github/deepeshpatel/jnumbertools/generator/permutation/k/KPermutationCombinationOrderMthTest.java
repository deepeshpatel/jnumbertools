package io.github.deepeshpatel.jnumbertools.generator.permutation.k;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;
import static io.github.deepeshpatel.jnumbertools.TestBase.everyMthValue;

public class KPermutationCombinationOrderMthTest {

    @Test
    public void assertCount(){
        int increment=2;
        for(int n=0; n<=4; n++) {
            var input = Collections.nCopies(n,"A");
            for (int k = 0; k < n; k++) {
                long size = permutation.nPr(k,input)
                        .combinationOrderMth(increment)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n, k).longValue()/(double)increment);
                Assert.assertEquals((long)expected, size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.nPr(2, "A", "B", "C")
                .combinationOrderMth(2);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        permutation.nPr(-1,1).combinationOrderMth(3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        permutation.nPr(1, 5).combinationOrderMth(3);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForZeroAndNegativeIncrementValue() {
        permutation.nPr(1,1).combinationOrderMth(0);
    }

    @Test
    public void shouldGenerateMthKPermutations() {

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
        return permutation.nPr(k,input)
                .combinationOrderMth(increment)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int k, int increment) {
        var stream = permutation.nPr(k, input)
                .combinationOrder().
                stream();
        return everyMthValue(stream, increment).toString();
    }
}