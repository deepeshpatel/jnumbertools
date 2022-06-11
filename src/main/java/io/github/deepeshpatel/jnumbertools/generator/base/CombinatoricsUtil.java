/*
 * JNumberTools Library v1.0.3
 * Copyright (c) 2022 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.generator.base;

import java.math.BigInteger;
import java.util.*;

public class CombinatoricsUtil {

    public static int[] initIndicesForMultisetPermutation(int... multisetFreqArray){

        List<Integer> a = new ArrayList<>();
        for(int i=0; i<multisetFreqArray.length;i++) {
            a.addAll(Collections.nCopies(multisetFreqArray[i], i));
        }
        return a.stream().mapToInt(Integer::intValue).toArray();
    }

    public static void checkParamMultisetFreqArray(int inputSize, int[] multisetFreqArray, String message) {

        if(multisetFreqArray == null) {
            throw new IllegalArgumentException("multisetFreqArray must be non null to generate " + message + " of multiset");
        }

        if(multisetFreqArray.length != inputSize ) {
            throw new IllegalArgumentException("Length of multisetFreqArray should be equal to input length to generate " + message + " of multiset");
        }
    }

    public static void checkParamIncrement(BigInteger increment, String message) {
        if(increment.compareTo(BigInteger.ZERO) <=0) {
            throw new IllegalArgumentException("increment value must be >0 to generate every nth" + message );
        }
    }

    public static void  checkParamCombination(int inputSize, int r, String message){
        if(r > inputSize) {
            throw new IllegalArgumentException("r should be <= input length to generate " + message);
        }
        if(r < 0) {
            throw new IllegalArgumentException("r should be >=0 to generate " + message );
        }
    }

    public static void checkParamKPermutation(int inputSize, int k, String message) {
        if(k > inputSize) {
            throw new IllegalArgumentException("k should be <= input length to generate " + message);
        }
        if(k < 0) {
            throw new IllegalArgumentException("k should be >=0 to generate " + message );
        }
    }

    //making it mathematically correct as set should contain null/empty set by definition.
    // for r=0, nPr = nCr = 1 and hence should contain one null(empty) value
    public static<T> Iterator<List<T>> newEmptyIterator(){
        List<List<T>> list = new ArrayList<>();
        list.add(Collections.emptyList());
        return list.iterator();
    }
}
