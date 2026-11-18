/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/** Creates bounded, reproducible conditional-product benchmark workloads. */
public final class Scenario {
    private Scenario() { }

    public static ScenarioData create(String caseId) {
        String[] parts = caseId.split("/");
        if (parts.length != 3 || !parts[0].equals("phase-e")) {
            throw new IllegalArgumentException("Expected phase-e/<TOPOLOGY>/<DIMENSIONS>x<SIZE>: " + caseId);
        }
        String topology = parts[1];
        String[] shape = parts[2].split("x");
        if (shape.length != 2) throw new IllegalArgumentException("Invalid benchmark shape: " + parts[2]);
        int n = Integer.parseInt(shape[0]), size = Integer.parseInt(shape[1]);
        if (!List.of("CHAIN", "MANY_VALID", "COMPLETE", "DEAD_END", "CONTROLLED_DEAD_END",
                "DEEP_DEAD_END", "SMALL_DOMAIN").contains(topology))
            throw new IllegalArgumentException("Unknown benchmark topology: " + topology);
        if (topology.equals("SMALL_DOMAIN")) {
            return smallDomain(n, size);
        }
        if (topology.equals("DEEP_DEAD_END")) {
            return deepDeadEnd(n, size);
        }
        if (!List.of(3, 5, 7, 10).contains(n) || !List.of(10, 25, 50).contains(size))
            throw new IllegalArgumentException("Unsupported benchmark shape: " + parts[2]);

        List<List<Integer>> dimensions = new ArrayList<>(n);
        for (int d = 0; d < n; d++) dimensions.add(IntStream.range(0, size).boxed().toList());

        long budget = switch (topology) {
            case "CHAIN", "DEAD_END", "CONTROLLED_DEAD_END" -> 30_000L;
            case "MANY_VALID" -> 75_000L;
            case "COMPLETE" -> 1_000L;
            default -> throw new IllegalStateException(topology);
        };
        boolean deadEnd = topology.equals("DEAD_END") || topology.equals("CONTROLLED_DEAD_END");
        int allowedCount = deadEnd && topology.equals("CONTROLLED_DEAD_END")
                ? Math.max(1, (int) Math.ceil(size * 0.20))
                : largestBranchingFactor(size, n, budget);
        if (topology.equals("DEAD_END")) allowedCount = Math.max(2, allowedCount);

        List<Rule> rules = new ArrayList<>();
        if (deadEnd) {
            rules.add(new Rule(0, range(0, size), 1, range(0, allowedCount)));
            rules.add(new Rule(1, range(0, size), 2, range(0, allowedCount)));
            for (int d = 3; d < n; d++)
                rules.add(new Rule(2, range(0, size), d, range(0, allowedCount)));
            int triggerCount = topology.equals("CONTROLLED_DEAD_END")
                    ? Math.max(1, Math.min(size - 1, (int) Math.ceil(size * 0.80)))
                    : Math.max(1, Math.min(allowedCount - 1, (int) Math.floor(allowedCount * 0.80)));
            int midpoint = allowedCount / 2;
            rules.add(new Rule(0, range(0, triggerCount), 2, range(0, midpoint)));
            rules.add(new Rule(1, range(0, triggerCount), 2, range(midpoint, allowedCount)));
        } else {
            for (int d = 0; d < n - 1; d++)
                rules.add(new Rule(d, range(0, size), d + 1, range(0, allowedCount)));
        }
        return new ScenarioData(List.copyOf(dimensions), List.copyOf(rules));
    }

    /**
     * Small lists, many dimensions: a chain where each dimension excludes only its last value, so the
     * output is large relative to the list size (size * (size-1)^(n-1) tuples).
     */
    private static ScenarioData smallDomain(int n, int size) {
        if (n < 8 || size < 3 || size > 5) throw new IllegalArgumentException("Unsupported SMALL_DOMAIN shape");
        List<List<Integer>> dimensions = new ArrayList<>(n);
        for (int d = 0; d < n; d++) dimensions.add(IntStream.range(0, size).boxed().toList());
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < n - 1; d++)
            rules.add(new Rule(d, range(0, size), d + 1, range(0, size - 1)));
        return new ScenarioData(List.copyOf(dimensions), List.copyOf(rules));
    }

    /**
     * Late conflict: dimension n-2 forces the last dimension into the lower half, while ~90% of the values
     * dimension n-3 can take force it into the upper half (the rest stay valid, so the product is not empty). Those prefixes are only found dead at the last dimension, so a
     * traversal without lookahead explores the whole chain prefix before failing.
     */
    private static ScenarioData deepDeadEnd(int n, int size) {
        if (n < 5 || size < 10) throw new IllegalArgumentException("Unsupported DEEP_DEAD_END shape");
        List<List<Integer>> dimensions = new ArrayList<>(n);
        for (int d = 0; d < n; d++) dimensions.add(IntStream.range(0, size).boxed().toList());
        int allowed = largestBranchingFactor(size, n - 1, 200_000L);
        int half = size / 2;
        // ~90% of the values dimension n-3 can actually take (the chain limits it to [0,allowed)) are dead
        int killed = Math.max(1, Math.min(allowed - 1, (int) Math.floor(allowed * 0.9)));
        List<Rule> rules = new ArrayList<>();
        for (int d = 0; d < n - 2; d++)
            rules.add(new Rule(d, range(0, size), d + 1, range(0, allowed)));
        rules.add(new Rule(n - 2, range(0, size), n - 1, range(0, half)));
        rules.add(new Rule(n - 3, range(0, killed), n - 1, range(half, size)));
        return new ScenarioData(List.copyOf(dimensions), List.copyOf(rules));
    }

    private static int largestBranchingFactor(int size, int dimensions, long budget) {
        int best = 1;
        for (int candidate = 1; candidate <= size; candidate++) {
            long count = size;
            for (int d = 1; d < dimensions && count <= budget; d++) {
                if (count > budget / candidate) { count = budget + 1; break; }
                count *= candidate;
            }
            if (count <= budget) best = candidate;
            else break;
        }
        return best;
    }

    private static BitSet range(int from, int to) {
        BitSet result = new BitSet(to);
        result.set(from, to);
        return result;
    }

    public record ScenarioData(List<List<Integer>> dimensions, List<Rule> rules) { }
}
