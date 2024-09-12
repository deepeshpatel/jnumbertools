package io.github.deepeshpatel.jnumbertools.generator.permutation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;

public class CommonTest {

    @Test
    public void all3MthPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {
            for(int increment = 1; increment<=4; increment++) {

                var elements = IntStream.range(0, size).boxed().toList();
                int[] freq = new int[elements.size()];
                Arrays.fill(freq,1);

                String uniquePermutationValues = permutation.unique(size).
                        lexOrderMth(increment, 0).stream().toList().toString();

                String multisetPermutationValues = permutation.multiset(elements, freq)
                        .lexOrderMth(increment, 0)
                        .stream().toList().toString();

                String kPermutationValues = permutation.nPk(size,size)
                        .lexOrderMth(increment, 0)
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
            }
        }
    }

    @Test
    public void all3NextPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {

            var elements = IntStream.range(0, size).boxed().toList();
            int[] freq = new int[elements.size()];
            Arrays.fill(freq,1);

                String uniquePermutationValues = permutation.unique(size).
                        lexOrder().stream().toList().toString();

                String multisetPermutationValues = permutation.multiset(elements, freq)
                        .lexOrder()
                        .stream().toList().toString();

                String kPermutationValues = permutation.nPk(size, size)
                        .lexOrder()
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
        }
    }

    @Test
    public void all6LexOrderPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){
        for(int size=1; size<=3; size++) {
            int increment = 1;

            var elements = IntStream.range(0, size).boxed().toList();
            int[] freq = new int[elements.size()];
            Arrays.fill(freq,1);

            String uniquePermutationMth = permutation.unique(size).
                    lexOrderMth(increment, 0).stream().toList().toString();

            String multisetPermutationMth = permutation.multiset(elements, freq)
                    .lexOrderMth(increment, 0)
                    .stream().toList().toString();

            String kPermutationMth = permutation.nPk(size, size)
                    .lexOrderMth(increment, 0)
                    .stream().toList().toString();

            String uniquePermutation = permutation.unique(size).
                    lexOrderMth(increment, 0).stream().toList().toString();

            String multisetPermutation = permutation.multiset(elements, freq)
                    .lexOrder()
                    .stream().toList().toString();

            String kPermutation = permutation.nPk(size, size)
                    .lexOrder()
                    .stream().toList().toString();

            Assert.assertEquals(uniquePermutation, uniquePermutationMth);
            Assert.assertEquals(uniquePermutation, kPermutation);
            Assert.assertEquals(uniquePermutation, kPermutationMth);
            Assert.assertEquals(uniquePermutation, multisetPermutation);
            Assert.assertEquals(uniquePermutation, multisetPermutationMth);
        }
    }
}
