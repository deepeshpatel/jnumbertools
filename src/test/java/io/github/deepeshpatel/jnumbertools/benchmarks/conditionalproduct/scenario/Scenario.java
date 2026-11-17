/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

/** Loads a benchmark case and expands its declarative properties into dimensions and rules. */
public final class Scenario {
    private enum Topology { STAR_FIRST, STAR_LAST, CHAIN, CYCLE, SPARSE, DENSE, DEAD_END, EMPTY }
    private enum Selectivity { TIGHT, HIGH, MEDIUM, LOW, NONE }

    private final List<List<Integer>> dimensions;
    private final List<Rule> rules;

    private Scenario(int[] dimensionSizes, boolean emptyDimension,
                     Topology topology, Selectivity selectivity) {
        dimensions = new ArrayList<>(dimensionSizes.length);
        for (int d = 0; d < dimensionSizes.length; d++) {
            List<Integer> values = emptyDimension && d == 0
                    ? List.of()
                    : IntStream.range(0, dimensionSizes[d]).boxed().toList();
            dimensions.add(values);
        }
        rules = createRules(topology, selectivity);
    }

    private Scenario(List<List<Integer>> dimensions, List<Rule> rules) {
        this.dimensions = List.copyOf(dimensions);
        this.rules = List.copyOf(rules);
    }

    public static Scenario create(String caseId) {
        if (caseId.startsWith("phase-e/")) return createPhaseE(caseId);
        String resource = "/benchmarks/conditionalproduct/cases/" + caseId + ".properties";
        Properties properties = new Properties();
        try (InputStream input = Scenario.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Unknown benchmark case: " + caseId
                        + " (resource not found: " + resource + ")");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load benchmark case: " + caseId, exception);
        }

        Topology topology = Topology.valueOf(required(properties, "topology"));
        Selectivity selectivity = Selectivity.valueOf(required(properties, "selectivity"));
        int dimensionCount = Integer.parseInt(required(properties, "dimensions"));
        int[] dimensionSizes = parseDimensionSizes(properties, dimensionCount);
        boolean emptyDimension = Boolean.parseBoolean(properties.getProperty("emptyDimension", "false"));
        return new Scenario(dimensionSizes, emptyDimension, topology, selectivity);
    }


    /**
     * Builds bounded Phase E workloads. MANY_VALID targets at most 75,000 tuples;
     * COMPLETE targets at most 1,000. DEAD_END adds conflicting conditional
     * restrictions on the third dimension while preserving valid combinations.
     */
    private static Scenario createPhaseE(String caseId) {
        String[] parts = caseId.split("/");
        if (parts.length != 3) throw new IllegalArgumentException(
                "Expected phase-e/<PROFILE>/<DIMENSIONS>x<SIZE>: " + caseId);
        String profile = parts[1];
        String[] shape = parts[2].split("x");
        if (shape.length != 2) throw new IllegalArgumentException("Invalid Phase E shape: " + parts[2]);
        int n = Integer.parseInt(shape[0]);
        int size = Integer.parseInt(shape[1]);
        if (!List.of(3, 5, 7, 10).contains(n) || !List.of(10, 25, 50).contains(size))
            throw new IllegalArgumentException("Unsupported Phase E shape: " + parts[2]);
        if (!List.of("DEAD_END", "MANY_VALID", "COMPLETE").contains(profile))
            throw new IllegalArgumentException("Unknown Phase E profile: " + profile);

        List<List<Integer>> dimensions = new ArrayList<>(n);
        for (int d = 0; d < n; d++) dimensions.add(IntStream.range(0, size).boxed().toList());

        long budget = switch (profile) {
            case "DEAD_END" -> 30_000L;
            case "MANY_VALID" -> 75_000L;
            case "COMPLETE" -> 1_000L;
            default -> throw new IllegalStateException(profile);
        };
        int allowedCount = largestBranchingFactor(size, n, budget);
        if (profile.equals("DEAD_END")) allowedCount = Math.max(2, allowedCount);

        List<Rule> rules = new ArrayList<>();
        if (profile.equals("DEAD_END")) {
            // Keep the only cycle in dimensions 0, 1 and 2. Remaining dimensions
            // are attached as bridge edges so loop-block validation stays bounded.
            rules.add(new Rule(0, range(0, size), 1, range(0, allowedCount)));
            rules.add(new Rule(1, range(0, size), 2, range(0, allowedCount)));
            for (int d = 3; d < n; d++)
                rules.add(new Rule(2, range(0, size), d, range(0, allowedCount)));

            int triggerCount = Math.max(1, Math.min(allowedCount - 1,
                    (int) Math.floor(allowedCount * 0.80)));
            int midpoint = allowedCount / 2;
            rules.add(new Rule(0, range(0, triggerCount), 2, range(0, midpoint)));
            rules.add(new Rule(1, range(0, triggerCount), 2, range(midpoint, allowedCount)));
        } else {
            for (int d = 0; d < n - 1; d++)
                rules.add(new Rule(d, range(0, size), d + 1, range(0, allowedCount)));
        }
        return new Scenario(dimensions, rules);
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

    public List<List<Integer>> dimensions() { return dimensions; }
    public List<Rule> rules() { return rules; }

    private static int[] parseDimensionSizes(Properties properties, int dimensionCount) {
        String sizesProperty = properties.getProperty("sizes");
        int[] sizes = new int[dimensionCount];
        if (sizesProperty == null || sizesProperty.isBlank()) {
            int uniformSize = Integer.parseInt(required(properties, "size"));
            Arrays.fill(sizes, uniformSize);
        } else {
            String[] values = sizesProperty.split(",");
            if (values.length != dimensionCount) {
                throw new IllegalArgumentException("Expected " + dimensionCount
                        + " entries in sizes, found " + values.length);
            }
            for (int i = 0; i < values.length; i++) sizes[i] = Integer.parseInt(values[i].trim());
        }
        for (int size : sizes) {
            if (size <= 0) throw new IllegalArgumentException("Dimension sizes must be positive");
        }
        return sizes;
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required benchmark case property: " + key);
        }
        return value.trim();
    }

    private List<Rule> createRules(Topology topology, Selectivity selectivity) {
        List<Rule> result = new ArrayList<>();
        int n = dimensions.size();
        switch (topology) {
            case STAR_FIRST -> {
                for (int d = 1; d < n; d++) addRule(result, 0, d, allowedSet(d, -1, selectivity));
            }
            case STAR_LAST -> {
                for (int d = 0; d < n - 1; d++) addRule(result, d, n - 1, allowedSet(n - 1, -1, selectivity));
            }
            case CHAIN -> {
                for (int d = 0; d < n - 1; d++) addRule(result, d, d + 1, allowedSet(d + 1, -1, selectivity));
            }
            case CYCLE -> {
                for (int d = 0; d < n; d++) {
                    int target = (d + 1) % n;
                    addRule(result, d, target, allowedSet(target, -1, selectivity));
                }
            }
            case SPARSE -> {
                int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {5, 6}};
                for (int[] edge : edges) addRule(result, edge[0], edge[1],
                        allowedSet(edge[1], -1, selectivity));
            }
            case DENSE -> {
                for (int from = 0; from < n; from++)
                    for (int to = from + 1; to < n; to++)
                        addRule(result, from, to, allowedSet(to, -1, selectivity));
            }
            case DEAD_END -> {
                addRule(result, 0, n - 1, allowedSet(n - 1, 0, selectivity));
                addRule(result, 1, n - 1, allowedSet(n - 1, 1, selectivity));
                for (int d = 2; d < n - 1; d++)
                    addRule(result, d, n - 1, allowedSet(n - 1, 0, selectivity));
            }
            case EMPTY -> { }
        }
        return List.copyOf(result);
    }

    private void addRule(List<Rule> result, int triggerDimension, int targetDimension, BitSet allowed) {
        BitSet triggers = new BitSet(dimensions.get(triggerDimension).size());
        triggers.set(0, dimensions.get(triggerDimension).size());
        result.add(new Rule(triggerDimension, triggers, targetDimension, allowed));
    }

    private BitSet allowedSet(int dimension, int variant, Selectivity selectivity) {
        int size = dimensions.get(dimension).size();
        int allowedCount = switch (selectivity) {
            case TIGHT -> Math.max(1, (int) Math.floor(size * 0.01));
            case HIGH -> Math.max(1, (int) Math.floor(size * 0.20));
            case MEDIUM -> Math.max(1, (int) Math.floor(size * 0.50));
            case LOW -> Math.max(1, (int) Math.floor(size * 0.80));
            case NONE -> size;
        };
        BitSet allowed = new BitSet(size);
        if (variant == 1) allowed.set(size - allowedCount, size);
        else allowed.set(0, allowedCount);
        return allowed;
    }
}
