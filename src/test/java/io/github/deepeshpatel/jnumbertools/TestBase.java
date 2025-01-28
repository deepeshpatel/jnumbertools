package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.base.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestBase {

    public static final Calculator calculator = new Calculator(100, 100, 100);
    public static final Permutations permutation = new Permutations(calculator);
    public static final Combinations combination = new Combinations(calculator);
    public static final CartesianProduct cartesianProduct = new CartesianProduct(calculator);
    public static final Subsets subsets = new Subsets(calculator);
    public static final NumberSystem numberSystem = new NumberSystem(calculator);

    public static final List<?> listOfEmptyList = List.of(Collections.emptyList());

    public static final List<Character>  A_B = List.of('A', 'B');
    public static final List<Character> A_B_C = List.of('A', 'B', 'C');
    public static final List<Character> A_B_C_D = List.of('A', 'B', 'C', 'D');

    public static final List<Integer> num_1_2_3 = List.of(1, 2, 3);
    public static final List<Integer> num_1_to_4 = List.of(1, 2, 3, 4);
    public static final List<Integer> num_0_to_5 = List.of(0, 1, 2, 3, 4, 5);

    public static List<?> everyMthValue(Stream<?> stream, int m) {
        final int[] j = {0};
        return stream.filter(e -> j[0]++ % m == 0).collect(Collectors.toList());
    }

    public static int[] getRandomMultisetFreqArray(Random random, int length) {
        int[] frequencies = new int[length];
        for (int i = 0; i < frequencies.length; i++) {
            int value = random.nextInt(2) + 1;
            frequencies[i] = value;
        }
        return frequencies;
    }
}
