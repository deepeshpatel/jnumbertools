package io.github.deepeshpatel.jnumbertools.generator.rank;

import io.github.deepeshpatel.jnumbertools.numbersystem.combinadic.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.factoradic.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.permutadic.PermutadicAlgorithms;

import java.math.BigInteger;

public class RankOf {

    public static long kPermutation(int size, int[] permutation) {
        return PermutadicAlgorithms.rank(permutation, size);
    }

    public static BigInteger uniquePermutation(int[] permutation) {
        return FactoradicAlgorithms.rank(permutation);
    }

    public static BigInteger uniqueCombination(int n, int r, int[] combination) {
        return CombinadicAlgorithms.rank(n,r, combination);
    }

//    public static long multisetPermutation(int[] permutation, int[] freqArray) {
//        //TODO: Implement this
//        return 0;
//    }

//    public static long repetitivePermutation(int size, int[] permutation) {
//        //TODO: Implement this
//        return 0;
//    }

}
