/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

public class CombinatoricsUtil {

    public static int[] initIndicesForMultisetPermutation(int... frequencies){

        var multisetFreqList = new ArrayList<Integer>();
        for(int i=0; i<frequencies.length;i++) {
            multisetFreqList.addAll(Collections.nCopies(frequencies[i], i));
        }
        return multisetFreqList.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void checkParamMultisetFreqArray(int inputSize, int[] frequencies, String message) {

        if(frequencies == null) {
            throw new IllegalArgumentException("frequencies must be non null to generate " + message + " of multiset");
        }

        if(frequencies.length != inputSize ) {
            throw new IllegalArgumentException("Length of frequencies should be equal to input length to generate " + message + " of multiset");
        }
    }

    public static void checkParamIncrement(BigInteger increment, String message) {
        if(increment.compareTo(BigInteger.ZERO) <=0) {
            throw new IllegalArgumentException("increment value must be > 0 to generate every nth" + message );
        }
    }

    public static void  checkParamCombination(int inputSize, int r, String message){
        if(r > inputSize) {
            throw new IllegalArgumentException("r should be <= input length to generate " + message);
        }
        if(r < 0) {
            throw new IllegalArgumentException("r should be >= 0 to generate " + message );
        }
    }
}
