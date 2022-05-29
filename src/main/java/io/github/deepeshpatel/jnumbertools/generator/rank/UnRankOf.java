package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.numbersystem.combinadic.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;

import java.math.BigInteger;

public class UnRankOf {

    public UnRankOf() {
    }

    public int[] uniquePermutation(BigInteger rank, int size) {
        return FactoradicAlgorithms.unRank(rank, size);
    }

    public int[] kPermutation(BigInteger rank, int n, int k) {
        return PermutadicAlgorithms.unRankingWithBoundCheck(rank, n, k);
    }

    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        return CombinadicAlgorithms.unRank(rank, n, r);
    }
}
