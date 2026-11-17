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
        if (!List.of(3, 5, 7, 10).contains(n) || !List.of(10, 25, 50).contains(size))
            throw new IllegalArgumentException("Unsupported benchmark shape: " + parts[2]);
        if (!List.of("CHAIN", "MANY_VALID", "COMPLETE", "DEAD_END", "CONTROLLED_DEAD_END").contains(topology))
            throw new IllegalArgumentException("Unknown benchmark topology: " + topology);

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
