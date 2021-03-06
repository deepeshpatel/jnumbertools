package io.github.deepeshpatel.jnumbertools.generator.permutation;


import io.github.deepeshpatel.jnumbertools.generator.JNumberTools;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.factorial;

public class MultisetPermutationTest {

    @Test
    public void assertCount() {
        Random random = new Random(System.currentTimeMillis());
        for(int n=1; n<=6; n++){
            List<String> input = Collections.nCopies(n, "A");
            int[] multisetFreqArray =getRandomMultisetFreqArray(random, input.size());
            long count = JNumberTools.permutationsOf(input)
                    .multiset(multisetFreqArray).stream().count();

            int sum = Arrays.stream(multisetFreqArray).reduce(0, Integer::sum);
            long numerator = factorial(sum).longValue();//.get(());
            long denominator = IntStream.of(multisetFreqArray).asLongStream().reduce(1, (a, b) -> (a * factorial((int)b).longValue()));
            long expected = numerator/denominator;
            //( ∑ ai.si)! / Π(si!)
            Assert.assertEquals(expected,count);
        }
    }

    @Test
    public void shouldReturnSameResultForDifferentIteratorObjects(){
        MultisetPermutation<String> iterable = JNumberTools.permutationsOf("A", "B", "C")
                .multiset(3, 2, 3);

        List<List<String>> lists1 = iterable.stream().collect(Collectors.toList());
        List<List<String>> lists2 = iterable.stream().collect(Collectors.toList());
        Assert.assertEquals(lists1, lists2);
    }

    private int[] getRandomMultisetFreqArray(Random random, int length) {
        int[] multisetFreqArray = new int[length];
        for(int i=0; i<multisetFreqArray.length; i++) {
            int value = random.nextInt(2)+1;
            multisetFreqArray[i] = value;
        }
        return multisetFreqArray;
    }

//    private List<Long> factorialList(){
//        int n = 20;
//        long p=1;
//        List<Long> factorials = new ArrayList<>(n+1);
//        factorials.add(1L);
//        for(long i=1; i<=n; i++){
//            p *=i;
//            factorials.add(p);
//        }
//        return factorials;
//    }

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


        String output = JNumberTools.permutationsOf("Red", "Green", "Blue")
                .multiset(new int[]{2,1,1})
                .stream()
                .collect(Collectors.toList()).toString();

        Assert.assertEquals(expected,output);
    }

    @Test
    public void shouldGenerateAllUniquePermutationsOfMultiset(){
        String expected = "[[A, A, B, C], [A, A, C, B], [A, B, A, C], " +
                "[A, B, C, A], [A, C, A, B], [A, C, B, A], [B, A, A, C], " +
                "[B, A, C, A], [B, C, A, A], [C, A, A, B], [C, A, B, A], [C, B, A, A]]";

        String output = JNumberTools.permutationsOf("A", "B", "C")
                .multiset(new int[]{2,1,1})
                .stream()
                .collect(Collectors.toList()).toString();

        Assert.assertEquals(expected,output);
    }
}