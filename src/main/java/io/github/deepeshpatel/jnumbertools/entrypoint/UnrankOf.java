package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.FactoradicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

public final class UnrankOf {

    private final Calculator calculator;

    public UnrankOf() {
        this(new Calculator());
    }

    public UnrankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    public int[] uniquePermutation(BigInteger rank, int size) {
        return FactoradicAlgorithms.unRank(rank, size);
    }

    public int[] kPermutation(BigInteger rank, int n, int k) {
        return new PermutadicAlgorithms(calculator).unRankWithBoundCheck(rank, n, k);
    }

    public int[] uniqueCombination(BigInteger rank, int n, int r) {
        return new CombinadicAlgorithms(calculator).unRank(rank, calculator.nCr(n,r), n,r);
    }
}
