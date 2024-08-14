package io.github.deepeshpatel.jnumbertools.entrypoint;

import io.github.deepeshpatel.jnumbertools.numbersystem.CombinadicAlgorithms;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;

public class RankOf {

    private final Calculator calculator;

    public RankOf() {
        this(new Calculator());
    }

    public RankOf(Calculator calculator) {
        this.calculator = calculator;
    }

    public BigInteger kPermutation(int size, int... permutation) {
        return PermutadicAlgorithms.rank(size, permutation);
    }

    public BigInteger uniquePermutation(int... permutation) {
        return PermutadicAlgorithms.rank(permutation.length, permutation);
    }

    public BigInteger repeatedPermutation(int base, Integer... permutation) {

        BigInteger result = BigInteger.ZERO;
        long power = 1;
        for(int i=permutation.length-1; i >=0; i--) {
            long placeValue = permutation[i] * power;
            result = result.add(BigInteger.valueOf(placeValue));
            power *= base;
        }
        return result;
    }

    public BigInteger uniqueCombination(int n, int... combination) {
        return new CombinadicAlgorithms(calculator).rank(n, combination);
    }
}
