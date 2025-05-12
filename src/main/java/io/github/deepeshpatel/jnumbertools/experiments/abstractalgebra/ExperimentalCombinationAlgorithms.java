/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */
package io.github.deepeshpatel.jnumbertools.experiments.abstractalgebra;

import io.github.deepeshpatel.jnumbertools.base.Calculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExperimentalCombinationAlgorithms {

    static class SuggestedByDeepeshAndAditya {


        //This algo can be optimized further as follows:
        public static List<Integer> unRank(int n, int r, BigInteger rank, Calculator calculator) {

            List<Integer> result = new ArrayList<>(r);
            int increment = 0;

            for(;r>=1; r--) {
                int i = 0;
                BigInteger lowerBound = BigInteger.ZERO;
                BigInteger upperBound = BigInteger.ZERO;

                for (int n1 = n - increment - 1; n1 >= r; n1--, i++) {
                    BigInteger count = calculator.nCr(n1, r - 1);
                    lowerBound = upperBound;
                    upperBound = upperBound.add(count);

                    if (upperBound.compareTo(rank) > 0) {
                        break;
                    }
                }

                int currentResult = increment + i;
                result.add(currentResult);
                increment = currentResult + 1;
                rank = rank.subtract(lowerBound);
            }
            return result;
        }

        /*
            Check id this algo can be optimized by finding rank of remaining items
            because selecting 8 out of 10 items is same as selecting remaining 2 items
            or mathematically nCr = nCn-r
        */
        public static int rank(int[] combination, int n,  Calculator calculator) {
            int prevValue = -1;
            n = n-1;
            int r = combination.length-1;
            int rank = 0;
            int increment;

            for(int i=0; i<combination.length; i++,n--,r--) {

                increment = combination[i] - prevValue - 1;
                prevValue = combination[i];

                for(int j = 0; j<increment; j++, n--) {
                    rank = rank + calculator.nCr(n,r).intValue();
                }
            }

            return rank;
        }
    }

    static class FromZbigniewKokosinskiPaper {

        /*
        Algo adapted from research paper -
        https://www.researchgate.net/publication/220800601_Algorithms_for_Unranking_Combinations_and_their_Applications
        */
        public static List<Integer> unRank(int n, int r, BigInteger rank, Calculator c){
            List<Integer> result = new ArrayList<>(r);
            int t = n-r;
            int m = r-1;

            BigInteger first = c.nCr(m+t,t-1);
            BigInteger second = c.nCr(t+m, t);
            BigInteger N = first.add(second).subtract(BigInteger.ONE).subtract(rank);

            while (m>=0) {
                BigInteger combination = c.nCr(t+m, t-1);
                if( combination.compareTo(N) <= 0) {
                    result.add(n-t-m-1);
                    N = N.subtract(combination);
                    m = m-1;
                } else {
                    t--;
                }
            }
            return result;
        }
    }
}
