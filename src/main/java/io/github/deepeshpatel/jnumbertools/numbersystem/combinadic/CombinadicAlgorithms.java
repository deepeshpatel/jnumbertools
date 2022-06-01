package io.github.deepeshpatel.jnumbertools.numbersystem.combinadic;

import java.math.BigInteger;

import static io.github.deepeshpatel.jnumbertools.generator.base.CombinatoricsUtil.getClone;
import static io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil.nCrBig;

public class CombinadicAlgorithms {

    public static BigInteger rank(int n, int[] nthCombination ) {
        int[] combinadic = new int[nthCombination.length];

        for(int i=0; i<combinadic.length; i++) {
            combinadic[i] = n-1-nthCombination[i];
        }

        BigInteger x = combinadicToDecimal(combinadic);
        return  nCrBig(n,nthCombination.length).subtract(x).subtract(BigInteger.ONE);
    }

    public static int[] unRank(BigInteger rank, int n, int r) {
        return unRank(rank,nCrBig(n, r), n, r);
    }
    public static int[] unRank(BigInteger rank, BigInteger nCr, int n, int r) {
        BigInteger x = nCr.subtract(rank).subtract(BigInteger.ONE);
        int[] a = decimalToCombinadic(x,r);
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
    public static int[] nextCombinadic(int[] combinadic) {

        int[] result = getClone(combinadic);

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

//    public static int[] nextNthCombinadic(int[] combinadic ) {
//        //TODO: For fun activity. Ask Aditya to find the algo for this.
//        return null;
//    }
}
