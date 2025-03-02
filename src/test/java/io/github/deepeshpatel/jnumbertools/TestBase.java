package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.base.*;
import org.junit.jupiter.api.Assertions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class TestBase {
    static { System.getProperties().setProperty("stress.testing","true"); }

    public static final Calculator calculator = new Calculator(100, 100, 100,2);
    public static final Permutations permutation = new Permutations(calculator);
    public static final Combinations combination = new Combinations(calculator);
    public static final CartesianProduct cartesianProduct = new CartesianProduct(calculator);
    public static final Subsets subsets = new Subsets(calculator);
    public static final NumberSystem numberSystem = new NumberSystem(calculator);

    public static final List<?> listOfEmptyList = List.of(Collections.emptyList());
    public static final List<?> listOfEmptyMap = List.of(Collections.emptyMap());

    public static final List<Character>  A_B = List.of('A', 'B');
    public static final List<Character> A_B_C = List.of('A', 'B', 'C');
    public static final List<Character> A_B_C_D = List.of('A', 'B', 'C', 'D');

    public static final List<Integer> num_1_2_3 = List.of(1, 2, 3);
    public static final List<Integer> num_1_to_4 = List.of(1, 2, 3, 4);
    public static final List<Integer> num_0_to_5 = List.of(0, 1, 2, 3, 4, 5);

    private static List<?> everyMthValue(Stream<?> stream, int start, long m) {
        final int[] j = {0};
        return stream.filter(e -> {
            int index = j[0]++;
            return index >= start && (index - start) % m == 0;
        }).toList();
    }

    public static void  assertEveryMthValue(Stream<?> completeStream, Stream<?> mthStream, int start, long m) {
        var main = everyMthValue(completeStream, start, m);
        var mth = mthStream.toList();
        Assertions.assertIterableEquals(main, mth);
    }

    public static int[] getRandomMultisetFreqArray(Random random, int length) {
        int[] frequencies = new int[length];
        for (int i = 0; i < frequencies.length; i++) {
            int value = random.nextInt(2) + 1;
            frequencies[i] = value;
        }
        return frequencies;
    }

    //TODO: temp method remove this
    public static <K, Integer> LinkedHashMap createMap(List<K> list, int[] freq) {
        var options = new LinkedHashMap<>();
        for(int i=0; i<freq.length; i++) {
            options.put(list.get(i), freq[i]);
        }
        return options;
    }
}
