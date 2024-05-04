package io.github.deepeshpatel.jnumbertools.numbersystem;

import io.github.deepeshpatel.jnumbertools.entrypoint.Calculator;

import java.math.BigInteger;
import java.util.List;

public class CombinadicAlgorithms {

    private final Calculator calculator;

    public CombinadicAlgorithms(Calculator calculator) {
        this.calculator = calculator;
    }

    public BigInteger rank(int n, int[] nthCombination) {
        int[] combinadic = combinationToCombinadic(n, nthCombination);
        BigInteger x = combinadicToDecimal(combinadic);
        BigInteger nCr = calculator.nCr(n, nthCombination.length);
        return  nCr.subtract(x).subtract(BigInteger.ONE);
    }

    public int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
        BigInteger x = nCr.subtract(rank).subtract(BigInteger.ONE);
        int[] a = decimalToCombinadic(x,r);
        return combinadicToCombination(a, n);
    }

    public static int[] combinationToCombinadic(int n, int[] nthCombination){
        int[] combinadic = new int[nthCombination.length];
        for(int i=0; i<combinadic.length; i++) {
            combinadic[i] = n-1-nthCombination[i];
        }
        return combinadic;
    }

    public static int[] combinadicToCombination(int[] combinadic, int n) {
        int[] a = new int[combinadic.length];
        System.arraycopy(combinadic,0, a,0, a.length);
        for(int i=0; i<a.length; i++) {
            a[i] = n-1-a[i];
        }
        return a;
    }

    public BigInteger combinadicToDecimal(int[] combinadic) {

        BigInteger decimalValue = BigInteger.ZERO;
        int r = combinadic.length;

        for (int j : combinadic) {
            decimalValue = decimalValue.add(calculator.nCr(j, r));
            r--;
        }

        return decimalValue;
    }

    public int[] decimalToCombinadic(BigInteger value, int degree) {

        int[] combinadic = new int[degree];

        int r = degree;
        BigInteger max = value;

        for(int i=0; r>0; i++, r--) {
            int n=r;
            BigInteger nCr = calculator.nCr(n,r);
            BigInteger result = nCr;

            while(nCr.compareTo(max) <= 0 ) {
                result = nCr;
                nCr = calculator.nCr(++n,r);
            }
            combinadic[i] = n-1;
            max = max.subtract(result);
        }
        return combinadic;
    }

    //This is faster than nextKthCombinadic. So must be used for +1 while finding next Nth Combinadic
    public static int[] nextCombinadic(List<Integer> combinadic) {

        int[] result = combinadic.stream().mapToInt(Integer::intValue).toArray();

        int k=0;
        for(int i=result.length-1; i>0; i--) {

            result[i] = result[i] + 1;
            if(result[i] < result[i-1] ) {
                return result;
            }
            result[i] = k++;
        }

        result[0]++;
        return result;
    }

    //TODO: Add algo for nextNthCombinadic(int[] combinadic ) without converting to decimal.
}
