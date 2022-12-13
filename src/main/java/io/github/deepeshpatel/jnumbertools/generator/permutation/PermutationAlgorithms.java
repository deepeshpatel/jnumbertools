package io.github.deepeshpatel.jnumbertools.generator.permutation;

import io.github.deepeshpatel.jnumbertools.numbersystem.MathUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PermutationAlgorithms {

    public static List<List<Integer>> toCycleNotation(int[] permutation, boolean includeOneCycle) {
        List<Integer> p = Arrays.stream(permutation).boxed().collect(Collectors.toList());
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
        for(List cycle : permutationCycles) {
            lcm = MathUtil.LCM(lcm,cycle.size());
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
            throw new IllegalArgumentException("To compute the product, permutation1 and permutation2 must have same degree");
        }
        List<Integer> product = new ArrayList<>(permutation1.size());
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
        return Arrays.asList(inverse);
    }

    public static long orderOfPermutation(List<Integer> permutation) {
        return orderOfPermutationWithCycleNotation(toCycleNotation(permutation, true));
    }
}
