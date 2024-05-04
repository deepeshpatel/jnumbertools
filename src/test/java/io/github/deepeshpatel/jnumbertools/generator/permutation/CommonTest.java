package io.github.deepeshpatel.jnumbertools.generator.permutation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static io.github.deepeshpatel.jnumbertools.TestBase.tools;

public class CommonTest {

    @Test
    public void all3NthPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {
            for(int increment = 1; increment<=10; increment++) {

                String uniquePermutationValues = tools.permutations().of(size).
                        uniqueNth(increment).stream().toList().toString();

                String multisetPermutationValues = tools.permutations().of(size)
                        .multisetNth(increment, Collections.nCopies(size, 1))
                        .stream().toList().toString();

                String kPermutationValues = tools.permutations().of(size).k(size)
                        .lexOrderNth(increment)
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
            }
        }
    }

    @Test
    public void all3NextPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){

        for(int size=1; size<=5; size++) {

                String uniquePermutationValues = tools.permutations().of(size).
                        unique().stream().toList().toString();

                String multisetPermutationValues = tools.permutations().of(size)
                        .multiset(Collections.nCopies(size, 1))
                        .stream().toList().toString();

                String kPermutationValues = tools.permutations().of(size).k(size)
                        .lexOrder()
                        .stream().toList().toString();

                Assert.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assert.assertEquals(uniquePermutationValues, kPermutationValues);
        }
    }

    @Test
    public void all6LexOrderPermutationGeneratorsShouldGenerateSameOutputForSpecialCase(){
        for(int size=1; size<=5; size++) {
            int increment = 1;

            String uniquePermutationNth = tools.permutations().of(size).
                    uniqueNth(increment).stream().toList().toString();

            String multisetPermutationNth = tools.permutations().of(size)
                    .multisetNth(increment, Collections.nCopies(size, 1))
                    .stream().toList().toString();

            String kPermutationNth = tools.permutations().of(size).k(size)
                    .lexOrderNth(increment)
                    .stream().toList().toString();

            String uniquePermutation = tools.permutations().of(size).
                    unique().stream().toList().toString();

            String multisetPermutation = tools.permutations().of(size)
                    .multiset(Collections.nCopies(size, 1))
                    .stream().toList().toString();

            String kPermutation = tools.permutations().of(size).k(size)
                    .lexOrder()
                    .stream().toList().toString();

            Assert.assertEquals(uniquePermutation, uniquePermutationNth);
            Assert.assertEquals(uniquePermutation, kPermutation);
            Assert.assertEquals(uniquePermutation, kPermutationNth);
            Assert.assertEquals(uniquePermutation, multisetPermutation);
            Assert.assertEquals(uniquePermutation, multisetPermutationNth);
        }
    }
}
