package io.github.deepeshpatel.jnumbertools;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;
import io.github.deepeshpatel.jnumbertools.entrypoint.Combinations;
import io.github.deepeshpatel.jnumbertools.entrypoint.Permutations;

public class TestBase {

    public static Calculator calculator = new Calculator(100,100,100);
    public static Permutations permutation = new Permutations(calculator);
    public static Combinations combination = new Combinations(calculator);



}
