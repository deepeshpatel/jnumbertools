package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.base.*;
import org.junit.AssumptionViolatedException;

import java.util.List;
import java.util.stream.Stream;

public class TestBase {

    public static Calculator calculator = new Calculator(100,100,100);
    public static Permutations permutation = new Permutations(calculator);
    public static Combinations combination = new Combinations(calculator);
    public static CartesianProduct cartesianProduct = new CartesianProduct(calculator);
    public static Subsets subsets = new Subsets(calculator);

    public static List<?> everyMthValue(Stream<?> stream, int m) {
        final int[] j = {0};
        return stream.filter(e-> j[0]++ % m == 0).toList();
    }

    private static boolean stressTesting() {
        return false;
    }

    public static <T> void assumeStressTesting() {
        if (!stressTesting()) {
            throw new AssumptionViolatedException("stress testing disabled.");
        }
    }
}
