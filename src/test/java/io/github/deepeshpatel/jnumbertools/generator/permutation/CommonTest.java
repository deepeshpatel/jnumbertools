package io.github.deepeshpatel.jnumbertools.generator.permutation;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.deepeshpatel.jnumbertools.TestBase.createMap;
import static io.github.deepeshpatel.jnumbertools.TestBase.permutation;

public class CommonTest {

    @Test
    void all3MthPermutationGeneratorsShouldGenerateSameOutputForSpecialCase() {
        for (int size = 1; size <= 5; size++) {
            for (int increment = 1; increment <= 4; increment++) {

                var elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
                int[] freq = new int[elements.size()];
                Arrays.fill(freq, 1);

                String uniquePermutationValues = permutation.unique(size)
                        .lexOrderMth(increment, 0).stream().toList().toString();

                String multisetPermutationValues = permutation.multiset(createMap(elements, freq))
                        .lexOrderMth(increment, 0)
                        .stream().toList().toString();

                String kPermutationValues = permutation.nPk(size, size)
                        .lexOrderMth(increment, 0)
                        .stream().toList().toString();

                Assertions.assertEquals(uniquePermutationValues, multisetPermutationValues);
                Assertions.assertEquals(uniquePermutationValues, kPermutationValues);
            }
        }
    }

    @Test
    void all3NextPermutationGeneratorsShouldGenerateSameOutputForSpecialCase() {
        for (int size = 1; size <= 5; size++) {

            var elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
            int[] freq = new int[elements.size()];
            Arrays.fill(freq, 1);

            String uniquePermutationValues = permutation.unique(size)
                    .lexOrder().stream().toList().toString();

            String multisetPermutationValues = permutation.multiset(createMap(elements, freq))
                    .lexOrder()
                    .stream().toList().toString();

            String kPermutationValues = permutation.nPk(size, size)
                    .lexOrder()
                    .stream().toList().toString();

            Assertions.assertEquals(uniquePermutationValues, multisetPermutationValues);
            Assertions.assertEquals(uniquePermutationValues, kPermutationValues);
        }
    }

    @Test
    void all6LexOrderPermutationGeneratorsShouldGenerateSameOutputForSpecialCase() {
        for (int size = 1; size <= 3; size++) {
            int increment = 1;

            var elements = IntStream.range(0, size).boxed().collect(Collectors.toList());
            int[] freq = new int[elements.size()];
            Arrays.fill(freq, 1);

            String uniquePermutationMth = permutation.unique(size)
                    .lexOrderMth(increment, 0).stream().toList().toString();

            String multisetPermutationMth = permutation.multiset(createMap(elements, freq))
                    .lexOrderMth(increment, 0)
                    .stream().toList().toString();

            String kPermutationMth = permutation.nPk(size, size)
                    .lexOrderMth(increment, 0)
                    .stream().toList().toString();

            String uniquePermutation = permutation.unique(size)
                    .lexOrderMth(increment, 0).stream().toList().toString();

            String multisetPermutation = permutation.multiset(createMap(elements, freq))
                    .lexOrder()
                    .stream().toList().toString();

            String kPermutation = permutation.nPk(size, size)
                    .lexOrder()
                    .stream().toList().toString();

            Assertions.assertEquals(uniquePermutation, uniquePermutationMth);
            Assertions.assertEquals(uniquePermutation, kPermutation);
            Assertions.assertEquals(uniquePermutation, kPermutationMth);
            Assertions.assertEquals(uniquePermutation, multisetPermutation);
            Assertions.assertEquals(uniquePermutation, multisetPermutationMth);
        }
    }
}
