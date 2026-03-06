package io.github.deepeshpatel.jnumbertools.examples.fordevs.advanced;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;

import java.util.stream.Collectors;

/**
 * Demonstrates integration with Java Stream API
 * Shows filtering, mapping, collecting, and parallel processing
 */
public class StreamApiIntegration {

    public static void main(String[] args) {
        System.out.println("=== Stream API Integration ===\n");

        basicStreamOperations();
        filteringAndMapping();
        collectingResults();
        parallelProcessing();
        infiniteStreams();
    }

    private static void basicStreamOperations() {
        System.out.println("1. BASIC STREAM OPERATIONS\n");

        var permutations = JNumberTools.permutations()
                .unique("A", "B", "C", "D")
                .lexOrder()
                .stream();

        System.out.println("   First 5 permutations:");
        permutations
                .limit(5)
                .forEach(System.out::println);

        // Streams are one-time use, so recreate for each operation
        var count = JNumberTools.permutations()
                .unique("A", "B", "C", "D")
                .lexOrder()
                .stream()
                .count();
        System.out.println("\n   Total permutations: " + count);
    }

    private static void filteringAndMapping() {
        System.out.println("\n2. FILTERING AND MAPPING\n");

        var combinations = JNumberTools.combinations()
                .unique(10, 3)
                .lexOrder()
                .stream();

        // Find combinations where first element is 0 and sum > 10
        System.out.println("   Combinations starting with 0 where sum > 10:");
        combinations
                .filter(combo -> combo.get(0) == 0)
                .filter(combo -> combo.stream().mapToInt(Integer::intValue).sum() > 10)
                .limit(5)
                .forEach(System.out::println);

        // Map combinations to their sum
        var sums = JNumberTools.combinations()
                .unique(10, 3)
                .lexOrder()
                .stream()
                .map(combo -> combo.stream().mapToInt(Integer::intValue).sum())
                .limit(10)
                .toList();
        System.out.println("\n   Sums of first 10 combinations: " + sums);
    }

    private static void collectingResults() {
        System.out.println("\n3. COLLECTING RESULTS\n");

        // Collect to List
        var first10 = JNumberTools.permutations()
                .unique(4)
                .lexOrder()
                .stream()
                .limit(10)
                .toList();
        System.out.println("   First 10 permutations (as List): " + first10.size());

        // Collect to Map (using first element as key)
        var byFirstElement = JNumberTools.permutations()
                .unique(4)
                .lexOrder()
                .stream()
                .limit(20)
                .collect(Collectors.groupingBy(
                        perm -> perm.get(0),
                        Collectors.counting()
                ));
        System.out.println("\n   Count by first element:");
        byFirstElement.forEach((key, value) ->
                System.out.println("     " + key + ": " + value));

        // Join to String
        var joined = JNumberTools.combinations()
                .unique(5, 2)
                .lexOrder()
                .stream()
                .limit(5)
                .map(combo -> combo.stream().map(String::valueOf).collect(Collectors.joining("-")))
                .collect(Collectors.joining(", "));
        System.out.println("\n   First 5 combinations: " + joined);
    }

    private static void parallelProcessing() {
        System.out.println("\n4. PARALLEL PROCESSING\n");

        var startTime = System.currentTimeMillis();

        // Sequential processing
        long sequentialCount = JNumberTools.combinations()
                .unique(20, 8)
                .lexOrder()
                .stream()
                .parallel()  // Switch to parallel
                .filter(combo -> combo.stream().mapToInt(Integer::intValue).sum() > 50)
                .count();

        var endTime = System.currentTimeMillis();
        System.out.println("   Combinations with sum > 50: " + sequentialCount);
        System.out.println("   Processing time (parallel): " + (endTime - startTime) + "ms");

        // Note: For small datasets, sequential may be faster
        System.out.println("\n   Note: Parallel processing benefits large datasets only");
    }

    private static void infiniteStreams() {
        System.out.println("\n5. INFINITE STREAMS (with limits)\n");

        // Generate random samples as a stream
        System.out.println("   10 random lottery tickets:");
        JNumberTools.combinations()
                .unique(49, 6)
                .choice(Integer.MAX_VALUE)  // Effectively infinite
                .stream()
                .limit(10)
                .forEach(ticket -> {
                    var formatted = ticket.stream()
                            .map(n -> String.format("%02d", n + 1))
                            .collect(Collectors.joining("-"));
                    System.out.println("   " + formatted);
                });

        // Process every millionth combination indefinitely
        System.out.println("\n   Every millionth combination (first 3):");
        JNumberTools.combinations()
                .unique(49, 6)
                .lexOrderMth(1_000_000, 0)
                .stream()
                .limit(3)
                .forEach(System.out::println);
    }
}
