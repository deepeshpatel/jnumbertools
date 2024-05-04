package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

public class RankOf {

    private final Calculator calculator;

    public RankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    public BigInteger kPermutation(int size, int... permutation) {
        return PermutadicAlgorithms.rank(size, permutation);
    }

    public BigInteger uniquePermutation(int... permutation) {
        return PermutadicAlgorithms.rank(permutation.length, permutation);
        //return FactoradicAlgorithms.rank(permutation);
    }

    public BigInteger uniqueCombination(int n, int... combination) {
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }
}
