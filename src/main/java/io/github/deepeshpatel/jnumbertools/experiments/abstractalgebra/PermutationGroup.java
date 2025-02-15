/*
 * JNumberTools Library v3.0.1
 * Copyright (c) 2025 Deepesh Patel (patel.deepesh@gmail.com)
 */

package io.github.deepeshpatel.jnumbertools.experiments.abstractalgebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PermutationGroup {


    public static List<List<Integer>> toCycleNotation(int[] permutation, boolean includeOneCycle) {
        List<Integer> p = Arrays.stream(permutation).boxed().toList();
        return toCycleNotation(p, includeOneCycle);
    }

    public static List<List<Integer>> toCycleNotation(List<Integer> permutation, boolean includeOneCycle) {
        boolean[] visited = new boolean[permutation.size()];
        List<List<Integer>> cycles = new ArrayList<>();

        for(int i=0; i<visited.length; i++) {
            if(visited[i]) {
                continue;
            }

            List<Integer> cycle = new ArrayList<>();
            int element = i;//permutation.get(i);
            cycle.add(element);
            visited[element] = true;

            element = permutation.get(element);
            while(cycle.get(0) != element) {
                cycle.add(element);
                visited[element] = true;
                element = permutation.get(element);
            }

            if(cycle.size()==1 && !includeOneCycle) {
                continue;
            }
            cycles.add(new ArrayList<>(cycle));
        }
        return cycles;
    }

    public static long orderOfPermutationWithCycleNotation(List<List<Integer>> permutationCycles) {

        long lcm = 1;
        for(var cycle : permutationCycles) {
            lcm = LCM(lcm, cycle.size());
        }
        return lcm;
    }

    public static boolean isIdentity(List<Integer> permutation) {
        for(int i=0; i<permutation.size(); i++) {
            if(i != permutation.get(i)) {
                return false;
            }
        }
        return true;
    }

    public static List<Integer> productMatrixForm(List<Integer> permutation1, List<Integer> permutation2) {
        if(permutation1.size() != permutation2.size()) {
            throw new IllegalArgumentException("To compute the cartesianProduct, permutation1 and permutation2 must have same degree");
        }
        var product = new ArrayList<Integer>(permutation1.size());
        for(int i=0; i<permutation1.size(); i++) {
            product.add(permutation1.get(permutation2.get(i)));
        }
        return product;
    }

    public static List<Integer> inverse(List<Integer> permutation) {
        Integer[] inverse = new Integer[permutation.size()];
        for(int i=0; i< permutation.size(); i++) {
            inverse[permutation.get(i)] = i;
        }
        return List.of(inverse);
    }

    public static List<Integer> product(List<Integer> permutation1, List<Integer> permutation2) {
        if(permutation1.size() != permutation2.size()) {
            throw new IllegalArgumentException("size of permutation1 and permutation2 should be same.");
        }

        //cartesianProduct is not commutative
        Integer[] product = new Integer[permutation1.size()];
        for(int i=0; i<product.length; i++) {
            product[i] = permutation1.get(permutation2.get(i));
        }

        return List.of(product);
    }

    public static boolean isDerangement(List<Integer> permutation) {
        for(int i=0; i<permutation.size(); i++) {
            if(permutation.get(i) == i) {
                return false;
            }
        }
        return true;
    }

    public long orderOfPermutation(List<Integer> permutation) {
        return orderOfPermutationWithCycleNotation(toCycleNotation(permutation, true));
    }

    public static long GCD(long greater, long smaller) {
        long mod = greater % smaller;
        return mod == 0 ? smaller : GCD(smaller, mod);
    }

    public static long LCM(long a, long b) {
        return (a * b)/ GCD(a,b);
    }
}
