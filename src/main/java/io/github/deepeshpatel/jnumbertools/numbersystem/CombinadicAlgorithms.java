package io.github.deepeshpatel.jnumbertools.numbersystem;

import java.math.BigInteger;
import java.util.List;

import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCrBig;

public class CombinadicAlgorithms {

    public static BigInteger rank(int n, int[] nthCombination ) {
        int[] combinadic = combinationToCombinadic(n, nthCombination);
        BigInteger x = combinadicToDecimal(combinadic);
        return  nCrBig(n,nthCombination.length).subtract(x).subtract(BigInteger.ONE);
    }

    public static int[] unRank(BigInteger rank, int n, int r) {
        return unRank(rank,nCrBig(n, r), n, r);
    }
    public static int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
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
        int a[] = new int[combinadic.length];
        System.arraycopy(combinadic,0, a,0, a.length);
        for(int i=0; i<a.length; i++) {
            a[i] = n-1-a[i];
        }
        return a;
    }

    public static BigInteger combinadicToDecimal(int[] combinadic) {

        BigInteger decimalValue = BigInteger.ZERO;
        int r= combinadic.length;

        for (int j : combinadic) {
            decimalValue = decimalValue.add(nCrBig(j, r));
            r--;
        }

        return decimalValue;
    }

    public static int[] decimalToCombinadic(BigInteger value, int degree) {

        int[] combinadic = new int[degree];

        int r = degree;
        BigInteger max = value;

        for(int i=0; i<combinadic.length; i++) {
            int n=r;
            BigInteger nCr = nCrBig(n,r);
            BigInteger result = BigInteger.ZERO;
            while(nCr.compareTo(max) <= 0 ) {
                result = nCr;
                n++;
                nCr =  nCr
                        .multiply(BigInteger.valueOf(n))
                        .divide(BigInteger.valueOf(n-r));
            }
            combinadic[i] = n-1;
            max = max.subtract(result);
            r--;
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
