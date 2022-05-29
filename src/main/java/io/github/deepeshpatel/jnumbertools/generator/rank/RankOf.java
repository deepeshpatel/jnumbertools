package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.numbersystem.combinadic.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;

import java.math.BigInteger;

public class RankOf {

    public RankOf() {
    }

    public BigInteger kPermutation(int size, int... permutation) {
        return PermutadicAlgorithms.rank(permutation, size);
    }

    public BigInteger uniquePermutation(int... permutation) {
        return FactoradicAlgorithms.rank(permutation);
    }

    public BigInteger uniqueCombination(int n, int... combination) {
        return CombinadicAlgorithms.rank(n, combination);
    }
}
