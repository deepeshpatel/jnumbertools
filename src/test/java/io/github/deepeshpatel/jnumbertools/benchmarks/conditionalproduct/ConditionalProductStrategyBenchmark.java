/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.baseline.NestedStreamBaseline;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.LexOrderStrategy;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Full-result comparison of the four conditional-product strategies with a nested-stream baseline.
 *
 * <p>{@code traversalOnly} reuses a builder and generator prepared at trial setup.
 * {@code endToEnd} includes builder construction, rule registration, generator creation, and
 * complete traversal. Both methods consume every valid tuple and fold all values into a checksum.</p>
 *
 * <p>Run {@link ConditionalProductBenchmarkRunner} to save a separate final-results text file
 * for each benchmark method.</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductStrategyBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({
                "phase-e/DEAD_END/3x10",
                "phase-e/DEAD_END/3x25",
                "phase-e/DEAD_END/3x50",
                "phase-e/DEAD_END/5x10",
                "phase-e/DEAD_END/5x25",
                "phase-e/DEAD_END/5x50",
                "phase-e/DEAD_END/7x10",
                "phase-e/DEAD_END/7x25",
                "phase-e/DEAD_END/7x50",
                "phase-e/DEAD_END/10x10",
                "phase-e/DEAD_END/10x25",
                "phase-e/DEAD_END/10x50",
                "phase-e/MANY_VALID/3x10",
                "phase-e/MANY_VALID/3x25",
                "phase-e/MANY_VALID/3x50",
                "phase-e/MANY_VALID/5x10",
                "phase-e/MANY_VALID/5x25",
                "phase-e/MANY_VALID/5x50",
                "phase-e/MANY_VALID/7x10",
                "phase-e/MANY_VALID/7x25",
                "phase-e/MANY_VALID/7x50",
                "phase-e/MANY_VALID/10x10",
                "phase-e/MANY_VALID/10x25",
                "phase-e/MANY_VALID/10x50",
                "phase-e/COMPLETE/3x10",
                "phase-e/COMPLETE/3x25",
                "phase-e/COMPLETE/3x50",
                "phase-e/COMPLETE/5x10",
                "phase-e/COMPLETE/5x25",
                "phase-e/COMPLETE/5x50",
                "phase-e/COMPLETE/7x10",
                "phase-e/COMPLETE/7x25",
                "phase-e/COMPLETE/7x50",
                "phase-e/COMPLETE/10x10",
                "phase-e/COMPLETE/10x25",
                "phase-e/COMPLETE/10x50",
                "topology/star/Dim2-5_Size10-100/selectivity/high/STAR_FIRST_5x10_HIGH",
                "topology/star/Dim2-5_Size10-100/selectivity/high/STAR_LAST_5x10_HIGH",
                "topology/chain/Dim2-5_Size10-100/selectivity/medium/CHAIN_5x10_MEDIUM",
                "topology/chain/Dim2-5_Size10-100/selectivity/tight/CHAIN_5x10_TIGHT",
                "topology/chain/Dim2-5_Size10-100/selectivity/medium/CHAIN_3x25_MEDIUM",
                "topology/star/Dim2-5_Size10-100/selectivity/low/STAR_FIRST_3x50_LOW",
                "topology/chain/Dim6-10_Size10-100/selectivity/tight/CHAIN_10x10_TIGHT",
                "topology/chain/Dim11-20_Size10-100/selectivity/tight/CHAIN_15x10_TIGHT",
                "topology/chain/Dim2-5_Size101-250/selectivity/tight/CHAIN_3x101_TIGHT",
                "topology/chain/Dim6-10_Size101-250/selectivity/tight/CHAIN_6x101_TIGHT",
                "topology/chain/Dim11-20_Size101-250/selectivity/tight/CHAIN_11x101_TIGHT",
                "edge-cases/small-domain/SPARSE_7x4_MEDIUM",
                "edge-cases/small-domain/DENSE_7x4_LOW",
                "edge-cases/small-domain/CHAIN_10x2_LOW",
                "edge-cases/small-domain/CYCLE_5x6_MEDIUM",
                "edge-cases/dead-end/DEAD_END_5x10",
                "edge-cases/empty-result/EMPTY_3x10"
        })
        public String scenarioName;

        @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
        public String implementation;

        Scenario scenario;
        long expectedCount;
        NestedStreamBaseline.Plan baselinePlan;
        ConditionalProductBuilder builder;
        Iterable<?> preparedGenerator;
        List<PreparedRule> preparedRules;

        @Setup(Level.Trial)
        public void setup() {
            scenario = Scenario.create(scenarioName);
            preparedRules = prepareRules(scenario);
            baselinePlan = NestedStreamBaseline.compile(scenario);
            expectedCount = NestedStreamBaseline.stream(baselinePlan).count();
            long expectedChecksum = checksum(NestedStreamBaseline.stream(baselinePlan));

            if (!implementation.equals("NESTED_STREAM")) {
                long actualChecksum = checksum(createLibraryGenerator(
                        buildBuilder(scenario, preparedRules), implementation, expectedCount));
                if (actualChecksum != expectedChecksum) {
                    throw new IllegalStateException(scenarioName + "/" + implementation
                            + ": library output differs from the nested-stream baseline");
                }
            }

            builder = implementation.equals("NESTED_STREAM") ? null : buildBuilder(scenario, preparedRules);
            preparedGenerator = implementation.equals("NESTED_STREAM")
                    ? null : createLibraryGenerator(builder, implementation, expectedCount);
        }
    }

    @Benchmark
    public long traversalOnly(BenchmarkState state) {
        if (state.implementation.equals("NESTED_STREAM")) {
            return checksum(NestedStreamBaseline.stream(state.baselinePlan));
        }
        return checksum(state.preparedGenerator);
    }

    @Benchmark
    public long endToEnd(BenchmarkState state) {
        if (state.implementation.equals("NESTED_STREAM")) {
            return checksum(NestedStreamBaseline.stream(NestedStreamBaseline.compile(state.scenario)));
        }
        ConditionalProductBuilder builder = buildBuilder(state.scenario);
        return checksum(createLibraryGenerator(builder, state.implementation, state.expectedCount));
    }

    private static ConditionalProductBuilder buildBuilder(
            Scenario scenario, List<PreparedRule> preparedRules) {
        List<List<Integer>> dimensions = scenario.dimensions();
        ConditionalProductBuilder builder =
                JNumberTools.cartesianProduct().conditionalProductOf(dimensions.get(0));
        for (int d = 1; d < dimensions.size(); d++) builder = builder.and(dimensions.get(d));

        for (PreparedRule rule : preparedRules) {
            builder = builder.when(dimensions.get(rule.triggerDimension()))
                    .isOneOf(rule.triggerValues())
                    .then(dimensions.get(rule.targetDimension()))
                    .isOneOf(rule.allowedValues());
        }
        return builder;
    }

    private static List<PreparedRule> prepareRules(Scenario scenario) {
        List<List<Integer>> dimensions = scenario.dimensions();
        List<PreparedRule> prepared = new ArrayList<>(scenario.rules().size());
        for (Rule rule : scenario.rules()) {
            prepared.add(new PreparedRule(
                    rule.triggerDimension(),
                    values(rule.triggerValues(), dimensions.get(rule.triggerDimension()).size()),
                    rule.targetDimension(),
                    values(rule.allowedValues(), dimensions.get(rule.targetDimension()).size())));
        }
        return List.copyOf(prepared);
    }

    private record PreparedRule(
            int triggerDimension,
            List<Integer> triggerValues,
            int targetDimension,
            List<Integer> allowedValues) { }

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
            for (Object value : (List<?>) item) hash = hash * 31 + (Integer) value;
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

    private static List<Integer> values(BitSet indexes, int size) {
        List<Integer> result = new ArrayList<>(indexes.cardinality());
        for (int i = indexes.nextSetBit(0); i >= 0 && i < size; i = indexes.nextSetBit(i + 1)) {
            result.add(i);
        }
        return result;
    }
}
