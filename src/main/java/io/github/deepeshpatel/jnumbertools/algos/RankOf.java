package io.github.deepeshpatel.jnumbertools.algos;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

public class RankOf {

    public RankOf() {
    }

    public BigInteger kPermutation(int size, int... permutation) {
        return PermutadicAlgorithms.rank(size, permutation);
    }

    public BigInteger uniquePermutation(int... permutation) {
        return PermutadicAlgorithms.rank(permutation.length, permutation);
        //return FactoradicAlgorithms.rank(permutation);
    }

    public BigInteger uniqueCombination(int n, int... combination) {
        return CombinadicAlgorithms.rank(n, combination);
    }
}
