package io.github.deepeshpatel.jnumbertools.generator.combination;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.tools;
import static org.junit.Assert.assertEquals;

public class UniqueCombinationNthTest {

    @Test
    public void assertCount(){

        for(int n=3; n<6; n++) {
            var input = Collections.nCopies(n, "A");
            for(int increment=1; increment<=4; increment++) {
                int combinationSize=input.size()/2;
                long size = tools.combinations().of(input,combinationSize).uniqueNth(increment).stream().count();
                double expected = Math.ceil(calculator.nCr(n,combinationSize).longValue()/(double)increment);
                Assert.assertEquals((long)expected,size );
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var input = List.of("A", "B", "C");
        var iterable = tools.combinations().of(input,2).uniqueNth(2);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test
    public void  shouldGenerateCombinationsWithIncrementingToEvery3rdCombination() {

        String expected = "[[0, 1, 2], [0, 2, 3], [1, 2, 3], [2, 3, 4]]";

        String output = tools.combinations()
                .of(3,0,1,2,3,4)
                .uniqueNth(3)
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void  shouldSupportVeryLargeNthCombination() {

        String expected = "[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16], " +
                "[0, 3, 6, 9, 10, 11, 13, 15, 16, 18, 20, 23, 24, 25, 27, 31, 33], " +
                "[2, 4, 10, 13, 14, 17, 18, 20, 22, 23, 26, 27, 29, 30, 31, 32, 33]]";

        String output = tools.combinations()
                .ofnCr(34,17)
                .uniqueNth(1000_000_000)// jump to 1 billionth combination
                .stream()
                .toList().toString();

        assertEquals(expected, output);
    }

    @Test
    public void shouldGenerateNthCombinationIncrementingValuesInBetween() {

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
        return tools.combinations().of(input,r)
                .uniqueNth(increment)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<String> input, int r, int increment) {
        var stream = tools.combinations().of(input,r).unique().stream();
        return collectEveryNthValue(stream, increment).toString();
    }

    public static <T> List<List<T>> collectEveryNthValue(Stream<List<T>> stream, int n) {
        final int[] j = {0};
        return stream.filter(e-> j[0]++ % n == 0).toList();
    }
}