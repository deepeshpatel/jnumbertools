package io.github.deepeshpatel.jnumbertools.api.examples.fordevs.advanced;

import io.github.deepeshpatel.jnumbertools.api.JNumberTools;

import java.math.BigInteger;

/**
 * Demonstrates handling of extremely large combinatorial spaces
 * Shows memory-efficient stream processing
 */
public class LargeScaleGeneration {
        public static void main(String[] args) {

                System.out.println("=== Large-Scale Generation Techniques ===");

                // Generate only what you need
                System.out.println("\nFirst 100 permutations of 20 elements:");
                JNumberTools.permutations()
                                .unique(20)
                                .lexOrder()
                                .stream()
                                .limit(100)
                                .forEach(perm -> {
                                });

                // Process in chunks using m-th generation
                System.out.println("\nProcessing every 1,000,000-th permutation of 15 elements:");
                JNumberTools.permutations()
                                .unique(15)
                                .lexOrderMth(1_000_000, 0)
                                .stream()
                                .limit(5)
                                .forEach(System.out::println);

                // Memory-efficient counting
                System.out.println("\nCounting total permutations of 50 elements:");
                BigInteger count = JNumberTools.permutations()
                                .unique(50)
                                .count();
                System.out.println("50! = " + count);
                System.out.println("(calculated without generating any permutations)");
        }
}
