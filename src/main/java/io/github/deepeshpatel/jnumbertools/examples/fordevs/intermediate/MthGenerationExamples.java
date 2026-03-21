package io.github.deepeshpatel.jnumbertools.examples.fordevs.intermediate;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

/**
 * Demonstrates efficient generation of every m-th element
 * Critical for large combinatorial spaces
 */
public class MthGenerationExamples {
    public static void main(String[] args) {

        System.out.println("=== M-th Element Generation ===");

        // Every 1,000,000-th lottery combination
        System.out.println("\nEvery millionth lottery combination (6/49):");
        JNumberTools.combinations()
                .unique(49, 6)
                .lexOrderMth(1_000_000, 0)
                .stream()
                .limit(3)
                .forEach(System.out::println);

        // Every 100-th permutation of 5 elements
        System.out.println("\nEvery 100th permutation of 5 elements:");
        JNumberTools.permutations()
                .unique(5)
                .lexOrderMth(100, 0)
                .stream()
                .limit(3)
                .forEach(System.out::println);

        // M-th generation with custom start
        System.out.println("\nEvery 50th combination starting from rank 200:");
        JNumberTools.combinations()
                .unique(20, 5)
                .lexOrderMth(50, 200)
                .stream()
                .limit(4)
                .forEach(System.out::println);
    }
}
