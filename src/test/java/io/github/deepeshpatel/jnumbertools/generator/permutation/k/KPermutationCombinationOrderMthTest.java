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
            var input = Collections.nCopies(n,'A');
            for (int k = 0; k < n; k++) {
                long size = permutation.nPr(k,input)
                        .combinationOrderMth(increment, 0)
                        .stream().count();
                double expected = Math.ceil(calculator.nPr(n, k).longValue()/(double)increment);
                Assert.assertEquals((long)expected, size);
            }
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        var iterable = permutation.nPr(2, "A", "B", "C")
                .combinationOrderMth(2, 0);
        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKLessThan0() {
        permutation.nPr(-1,1).combinationOrderMth(3, 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForKGreaterThanInputLength() {
        permutation.nPr(1, 5).combinationOrderMth(3, 0);
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForZeroAndNegativeIncrementValue() {
        permutation.nPr(1,1).combinationOrderMth(0, 0);
    }

    @Test
    public void shouldGenerateMthKPermutations() {

        var input = List.of('A','B','C','D');
        for(int k=1; k<= 3; k++) {
            for(int increment=1; increment<=10;increment++) {
                String expected = getExpectedResultViaOneByOneIteration(input, k,increment);
                String output   = getResultViaDirectIncrement(input,k,increment);
                Assert.assertEquals(expected, output);
            }
        }
    }

    @Test
    public void test_start_parameter_greater_than_0() {
        var output = permutation.nPr(3, 'a','b','c','d','e')
                .combinationOrderMth(20, 5)
                .stream().toList().toString();

        Assert.assertEquals("[[c, b, a], [a, e, c], [c, e, b]]", output);
    }

    @Test
    public void shouldHandleEmptyInput() {
        var output = permutation.nPr(0, Collections.emptyList())
                .combinationOrderMth(1, 0).stream().toList();
        Assert.assertEquals("[[]]", output.toString());
    }

    @Test
    public void shouldHandleLargeIncrementValues() {
        var input = List.of('A', 'B', 'C', 'D');
        var output = permutation.nPr(2, input)
                .combinationOrderMth(1, 12)
                .stream().toList();

        Assert.assertTrue(output.isEmpty() );
    }

    @Test
    public void shouldGenerateAllPermutationsWhenKEqualsInputSize() {
        var input = List.of('A', 'B', 'C');
        var output = permutation.nPr(3, input)
                .combinationOrderMth(1, 0)
                .stream().toList();

        Assert.assertEquals("[[A, B, C], [A, C, B], [B, A, C], [B, C, A], [C, A, B], [C, B, A]]",
                output.toString());
    }

    @Test
    public void shouldHandleLargeInputSize() {
        var input = List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J');
        var output = permutation.nPr(5, input)
                .combinationOrderMth(1, 0)
                .stream().toList();

        Assert.assertEquals(30240, output.size() );
    }

    private String getResultViaDirectIncrement(List<?> input, int k, int increment) {
        return permutation.nPr(k,input)
                .combinationOrderMth(increment, 0)
                .stream().toList().toString();
    }

    private String getExpectedResultViaOneByOneIteration(List<?> input, int k, int increment) {
        var stream = permutation.nPr(k, input)
                .combinationOrder().
                stream();
        return everyMthValue(stream, increment).toString();
    }
}