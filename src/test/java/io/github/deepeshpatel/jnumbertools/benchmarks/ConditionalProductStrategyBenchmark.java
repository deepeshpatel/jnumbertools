/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.LexOrderStrategy;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Full-result comparison of the four conditional-product strategies with a nested-stream baseline.
 *
 * <p>Each scenario encodes a topology, dimension count, values per dimension, and constraint selectivity.
 * Both benchmark methods consume every valid tuple and fold every value into a checksum. The
 * traversal-only method reuses a builder and generator prepared at trial setup. The end-to-end method
 * includes builder construction, rule registration, generator creation, and complete traversal.</p>
 *
 * <p>The nested-stream baseline checks each constraint as soon as both participating dimensions have
 * been selected. It does not generate the complete Cartesian product and filter it afterwards.</p>
 *
 * <p>Run with {@code -prof gc} to record allocation. No benchmark method writes to the console.</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductStrategyBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {

        /**
         * Bounded full-traversal cases. The largest valid result set is 80,000 tuples.
         * STAR_FIRST and STAR_LAST expose the effect of constraint direction relative to dimension order.
         */
        @Param({
                "STAR_FIRST_5x10_HIGH",
                "STAR_LAST_5x10_HIGH",
                "CHAIN_5x10_MEDIUM",
                "SPARSE_7x4_MEDIUM",
                "DENSE_7x4_LOW",
                "DEAD_END_5x10",
                "EMPTY_3x10",
                "CHAIN_3x25_MEDIUM",
                "STAR_FIRST_3x50_LOW",
                "CHAIN_10x2_LOW",
                "CHAIN_5x10_TIGHT",
                "CYCLE_5x6_MEDIUM"
        })
        public String scenarioName;

        /** NESTED_STREAM is the reference baseline; the other values are public library strategies. */
        @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
        public String implementation;

        Scenario scenario;
        long expectedCount;
        long expectedChecksum;
        BaselinePlan baselinePlan;
        ConditionalProductBuilder builder;
        Iterable<?> preparedGenerator;

        @Setup(Level.Trial)
        public void setup() {
            scenario = Scenario.create(scenarioName);
            baselinePlan = buildBaselinePlan(scenario);
            // The exact expected traversal size is scenario metadata for ADAPTIVE, not timed work.
            expectedCount = nestedStream(baselinePlan).count();
            expectedChecksum = checksum(nestedStream(baselinePlan));

            // Verify tuple values and encounter order on a disposable builder so DP's lazy
            // counting state is not warmed for the measured traversal-only generator.
            if (!implementation.equals("NESTED_STREAM")) {
                long actualChecksum = checksum(createLibraryGenerator(
                        buildBuilder(scenario), implementation, expectedCount));
                if (actualChecksum != expectedChecksum) {
                    throw new IllegalStateException(scenarioName + "/" + implementation
                            + ": library output differs from the nested-stream baseline");
                }
            }

            // The measured traversal-only builder/generator is fresh and unconsumed.
            builder = implementation.equals("NESTED_STREAM") ? null : buildBuilder(scenario);
            preparedGenerator = implementation.equals("NESTED_STREAM")
                    ? null : createLibraryGenerator(builder, implementation, expectedCount);
        }
    }

    @Benchmark
    public long traversalOnly(BenchmarkState state) {
        if (state.implementation.equals("NESTED_STREAM")) {
            return checksum(nestedStream(state.baselinePlan));
        }
        return checksum(state.preparedGenerator);
    }

    @Benchmark
    public long endToEnd(BenchmarkState state) {
        if (state.implementation.equals("NESTED_STREAM")) {
            // Include compilation of the baseline's per-depth rule index in the timed operation.
            return checksum(nestedStream(buildBaselinePlan(state.scenario)));
        }
        ConditionalProductBuilder builder = buildBuilder(state.scenario);
        Iterable<?> generator = createLibraryGenerator(builder, state.implementation, state.expectedCount);
        return checksum(generator);
    }

    private static ConditionalProductBuilder buildBuilder(Scenario scenario) {
        ConditionalProductBuilder builder =
                JNumberTools.cartesianProduct().conditionalProductOf(scenario.dimensions.get(0));
        for (int d = 1; d < scenario.dimensions.size(); d++) {
            builder = builder.and(scenario.dimensions.get(d));
        }

        for (Rule rule : scenario.rules) {
            List<Integer> triggers = values(rule.triggerValues, scenario.dimensions.get(rule.triggerDimension).size());
            List<Integer> allowed = values(rule.allowedValues, scenario.dimensions.get(rule.targetDimension).size());
            builder = builder.when(scenario.dimensions.get(rule.triggerDimension))
                    .isOneOf(triggers)
                    .then(scenario.dimensions.get(rule.targetDimension))
                    .isOneOf(allowed);
        }
        return builder;
    }

    private static Iterable<?> createLibraryGenerator(
            ConditionalProductBuilder builder, String implementation, long expectedCount) {
        if (implementation.equals("ADAPTIVE")) {
            return builder.lexOrder(LexOrderStrategy.ADAPTIVE, expectedCount);
        }
        return builder.lexOrder(LexOrderStrategy.valueOf(implementation));
    }

    private static long checksum(Iterable<?> tuples) {
        long hash = 1;
        for (Object item : tuples) {
            List<?> tuple = (List<?>) item;
            for (Object value : tuple) hash = hash * 31 + (Integer) value;
            hash = hash * 31 + 1;
        }
        return hash;
    }

    private static long checksum(Stream<List<Object>> tuples) {
        long hash = 1;
        try (Stream<List<Object>> stream = tuples) {
            var iterator = stream.iterator();
            while (iterator.hasNext()) {
                for (Object value : iterator.next()) hash = hash * 31 + (Integer) value;
                hash = hash * 31 + 1;
            }
        }
        return hash;
    }

    // ---------------------------------------------------------------------
    // Independent nested-stream baseline
    // ---------------------------------------------------------------------

    private static BaselinePlan buildBaselinePlan(Scenario scenario) {
        List<Rule>[] byDepth = new List[scenario.dimensions.size()];
        for (int d = 0; d < byDepth.length; d++) byDepth[d] = new ArrayList<>();
        for (Rule rule : scenario.rules) {
            int laterDimension = Math.max(rule.triggerDimension, rule.targetDimension);
            byDepth[laterDimension].add(new Rule(rule.triggerDimension,
                    (BitSet) rule.triggerValues.clone(), rule.targetDimension,
                    (BitSet) rule.allowedValues.clone()));
        }
        return new BaselinePlan(scenario, byDepth);
    }

    private static Stream<List<Object>> nestedStream(BaselinePlan plan) {
        return nestedAt(0, new int[plan.scenario.dimensions.size()],
                new Object[plan.scenario.dimensions.size()], plan);
    }

    private static Stream<List<Object>> nestedAt(
            int depth, int[] indexes, Object[] values, BaselinePlan plan) {
        if (depth == plan.scenario.dimensions.size()) {
            return Stream.of(List.copyOf(Arrays.asList(values.clone())));
        }

        return IntStream.range(0, plan.scenario.dimensions.get(depth).size())
                .filter(candidate -> validCandidate(depth, candidate, indexes, plan))
                .boxed()
                .flatMap(candidate -> {
                    indexes[depth] = candidate;
                    values[depth] = plan.scenario.dimensions.get(depth).get(candidate);
                    return nestedAt(depth + 1, indexes, values, plan);
                });
    }

    private static boolean validCandidate(int depth, int candidate, int[] indexes, BaselinePlan plan) {
        for (Rule rule : plan.rulesByDepth[depth]) {
            int triggerIndex = rule.triggerDimension == depth
                    ? candidate : indexes[rule.triggerDimension];
            int targetIndex = rule.targetDimension == depth
                    ? candidate : indexes[rule.targetDimension];

            if (rule.triggerValues.get(triggerIndex) && !rule.allowedValues.get(targetIndex)) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------------
    // Scenario definitions
    // ---------------------------------------------------------------------

    private enum Topology { STAR_FIRST, STAR_LAST, CHAIN, CYCLE, SPARSE, DENSE, DEAD_END, EMPTY }
    private enum Selectivity { TIGHT, HIGH, MEDIUM, LOW, NONE }

    private record Rule(int triggerDimension, BitSet triggerValues,
                        int targetDimension, BitSet allowedValues) { }

    private static final class BaselinePlan {
        final Scenario scenario;
        final List<Rule>[] rulesByDepth;

        BaselinePlan(Scenario scenario, List<Rule>[] rulesByDepth) {
            this.scenario = scenario;
            this.rulesByDepth = rulesByDepth;
        }
    }

    private static final class Scenario {
        final Topology topology;
        final Selectivity selectivity;
        final List<List<Integer>> dimensions;
        final List<Rule> rules;

        private Scenario(Topology topology, Selectivity selectivity, int dimensionCount,
                         int listSize, boolean emptyDimension) {
            this.topology = topology;
            this.selectivity = selectivity;
            this.dimensions = new ArrayList<>(dimensionCount);
            for (int d = 0; d < dimensionCount; d++) {
                List<Integer> values = emptyDimension && d == 0
                        ? List.of()
                        : IntStream.range(0, listSize).boxed().toList();
                dimensions.add(values);
            }
            this.rules = createRules();
        }

        static Scenario create(String name) {
            return switch (name) {
                case "STAR_FIRST_5x10_HIGH" -> new Scenario(Topology.STAR_FIRST, Selectivity.HIGH, 5, 10, false);
                case "STAR_LAST_5x10_HIGH" -> new Scenario(Topology.STAR_LAST, Selectivity.HIGH, 5, 10, false);
                case "CHAIN_5x10_MEDIUM" -> new Scenario(Topology.CHAIN, Selectivity.MEDIUM, 5, 10, false);
                case "SPARSE_7x4_MEDIUM" -> new Scenario(Topology.SPARSE, Selectivity.MEDIUM, 7, 4, false);
                case "DENSE_7x4_LOW" -> new Scenario(Topology.DENSE, Selectivity.LOW, 7, 4, false);
                case "DEAD_END_5x10" -> new Scenario(Topology.DEAD_END, Selectivity.HIGH, 5, 10, false);
                case "EMPTY_3x10" -> new Scenario(Topology.EMPTY, Selectivity.NONE, 3, 10, true);
                case "CHAIN_3x25_MEDIUM" -> new Scenario(Topology.CHAIN, Selectivity.MEDIUM, 3, 25, false);
                case "STAR_FIRST_3x50_LOW" -> new Scenario(Topology.STAR_FIRST, Selectivity.LOW, 3, 50, false);
                case "CHAIN_10x2_LOW" -> new Scenario(Topology.CHAIN, Selectivity.LOW, 10, 2, false);
                case "CHAIN_5x10_TIGHT" -> new Scenario(Topology.CHAIN, Selectivity.TIGHT, 5, 10, false);
                case "CYCLE_5x6_MEDIUM" -> new Scenario(Topology.CYCLE, Selectivity.MEDIUM, 5, 6, false);
                default -> throw new IllegalArgumentException("Unknown scenario: " + name);
            };
        }

        private List<Rule> createRules() {
            List<Rule> result = new ArrayList<>();
            int n = dimensions.size();
            switch (topology) {
                case STAR_FIRST -> {
                    for (int d = 1; d < n; d++) addRule(result, 0, d, allowedSet(d, -1));
                }
                case STAR_LAST -> {
                    for (int d = 0; d < n - 1; d++) addRule(result, d, n - 1, allowedSet(n - 1, -1));
                }
                case CHAIN -> {
                    for (int d = 0; d < n - 1; d++) addRule(result, d, d + 1, allowedSet(d + 1, -1));
                }
                case CYCLE -> {
                    for (int d = 0; d < n; d++) addRule(result, d, (d + 1) % n, allowedSet((d + 1) % n, -1));
                }
                case SPARSE -> {
                    int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {5, 6}};
                    for (int[] edge : edges) addRule(result, edge[0], edge[1], allowedSet(edge[1], -1));
                }
                case DENSE -> {
                    for (int from = 0; from < n; from++) {
                        for (int to = from + 1; to < n; to++) {
                            addRule(result, from, to, allowedSet(to, -1));
                        }
                    }
                }
                case DEAD_END -> {
                    addRule(result, 0, n - 1, allowedSet(n - 1, 0));
                    addRule(result, 1, n - 1, allowedSet(n - 1, 1));
                    for (int d = 2; d < n - 1; d++) addRule(result, d, n - 1, allowedSet(n - 1, 0));
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

        private BitSet allowedSet(int dimension, int variant) {
            int size = dimensions.get(dimension).size();
            int allowedCount = switch (selectivity) {
                case TIGHT -> Math.max(1, (int) Math.floor(size * 0.10));
                case HIGH -> Math.max(1, (int) Math.floor(size * 0.20));
                case MEDIUM -> Math.max(1, (int) Math.floor(size * 0.50));
                case LOW -> Math.max(1, (int) Math.floor(size * 0.80));
                case NONE -> size;
            };
            BitSet allowed = new BitSet(size);
            if (variant == 1) {
                allowed.set(size - allowedCount, size);
            } else {
                allowed.set(0, allowedCount);
            }
            return allowed;
        }
    }

    private static List<Integer> values(BitSet indexes, int size) {
        List<Integer> result = new ArrayList<>(indexes.cardinality());
        for (int i = indexes.nextSetBit(0); i >= 0 && i < size; i = indexes.nextSetBit(i + 1)) {
            result.add(i);
        }
        return result;
    }
}
