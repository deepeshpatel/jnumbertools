package io.github.deepeshpatel.jnumbertools.generator.permutation.multiset;


import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.calculator;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;

public class MultisetPermutationTest {

    @Test
    public void assertCount() {
        Random random = new Random(System.currentTimeMillis());
        for(int n=1; n<=6; n++){
            var input = Collections.nCopies(n, 'A');
            int[] frequency = getRandomMultisetFreqArray(random, input.size());
            long count = permutation.multiset(input, frequency)
                    .lexOrder().stream().count();

            int sum = Arrays.stream(frequency).reduce(0, Integer::sum);
            long numerator = calculator.factorial(sum).longValue();
            long denominator = IntStream.of(frequency).asLongStream()
                    .reduce(1, (a, b) -> (a * calculator.factorial((int)b).longValue()));
            long expected = numerator/denominator;
            //( ∑ ai.si)! / Π(si!)
            Assert.assertEquals(expected,count);
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){

        var elements = List.of("A", "B", "C");
        int[] frequencies = {3,2,2};

        MultisetPermutation<String> iterable = permutation.multiset(elements, frequencies)
                .lexOrder();

        var lists1 = iterable.stream().toList();
        var lists2 = iterable.stream().toList();
        Assert.assertEquals(lists1, lists2);
    }

    private int[] getRandomMultisetFreqArray(Random random, int length) {
        int[] frequencies = new int[length];
        for(int i=0; i<frequencies.length; i++) {
            int value = random.nextInt(2)+1;
            frequencies[i] = value;
        }
        return frequencies;
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOfMultiset2(){
        String expected = "[[Red, Red, Green, Blue], " +
                "[Red, Red, Blue, Green], " +
                "[Red, Green, Red, Blue], " +
                "[Red, Green, Blue, Red], " +
                "[Red, Blue, Red, Green], " +
                "[Red, Blue, Green, Red], " +
                "[Green, Red, Red, Blue], " +
                "[Green, Red, Blue, Red], " +
                "[Green, Blue, Red, Red], " +
                "[Blue, Red, Red, Green], " +
                "[Blue, Red, Green, Red], " +
                "[Blue, Green, Red, Red]]";

        var elements = List.of("Red", "Green", "Blue");
        int[] frequencies = {2,1,1};
        String output = permutation.multiset(elements, frequencies)
                .lexOrder()
                .stream()
                .toList().toString();

        Assert.assertEquals(expected,output);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOfMultiset(){
        String expected = "[[A, A, B, C], [A, A, C, B], [A, B, A, C], " +
                "[A, B, C, A], [A, C, A, B], [A, C, B, A], [B, A, A, C], " +
                "[B, A, C, A], [B, C, A, A], [C, A, A, B], [C, A, B, A], [C, B, A, A]]";

        var elements = List.of("A", "B", "C");
        int[] frequencies = {2,1,1};

        String output = permutation.multiset(elements,frequencies)
                .lexOrder()
                .stream()
                .toList().toString();

        Assert.assertEquals(expected,output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenFrequenciesIsNull() {
        var elements = List.of("A", "B", "C");

        String output = permutation.multiset(elements, null)
                .lexOrder()
                .stream()
                .toList().toString();
    }
}