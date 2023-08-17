package io.github.deepeshpatel.jnumbertools.algos;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

public class UnRankOf {

    public UnRankOf() {
    }

    public int[] uniquePermutation(BigInteger rank, int size) {
        return FactoradicAlgorithms.unRank(rank, size);
    }

    public int[] kPermutation(BigInteger rank, int n, int k) {
        return PermutadicAlgorithms.unRank(rank, n, k);
//        return PermutadicAlgorithms.unRankingWithBoundCheck(rank, n, k);
    }

    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        return CombinadicAlgorithms.unRank(rank, n, r);
    }
}
