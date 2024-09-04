package io.github.deepeshpatel.jnumbertools.generator.combination;

import io.github.deepeshpatel.jnumbertools.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.combination;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationMthTest {

    @Test
    public void assertCount(){

        for(int n=3; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                int combinationSize=input.size()/2;
                long size = combination.unique(combinationSize, input).lexOrderMth(increment, 0).stream().count();
                double expected = Math.ceil(calculator.nCr(n,combinationSize).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var input = List.of("A", "B", "C");
        var iterable = combination.unique(2, input).lexOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsWithIncrementingToEvery3rdCombination() {
        String expected = "[[0, 1, 2], [0, 2, 3], [1, 2, 3], [2, 3, 4]]";
        assertEquals(expected, output(List.of(0,1,2,3,4), 3, 3));
    }

    @Test
    public void  shouldSupportVeryLargeMthCombination() {

        String expected = "[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16], " +
                "[0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33], " +
                "[2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33]]";

        int n = 34;
        int r = 17;
        int increment = 1000_000_000; // jump to 1 billionth combination
        assertEquals(expected, output(n,r, increment));
    }

    @Test
    public void shouldGenerateMthCombinationIncrementingValuesInBetween() {

        List<String> input = List.of("A","B","C","D","E","F","G","H","I","J");
        for(int k=1; k<=input.size()/2; k++) {
            for(int increment=1; increment<=32;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected,output);
            }
        }
    }

    private String getResultViaDirectIncrement(List<String> input, int r, int increment) {
        return combination.unique(r, input)
                .lexOrderMth(increment, 0)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int r, int increment) {
        var stream = combination.unique(r, input).lexOrder().stream();
        return TestBase.everyMthValue(stream, increment).toString();
    }

    private <T> String output(List<T> elements, int size, int m) {
        return combination.unique(size, elements).lexOrderMth(m, 0).stream().toList().toString();
    }

    private String output(int n, int r, int m) {
        return combination.unique(n,r).lexOrderMth(m, 0).stream().toList().toString();
    }
}