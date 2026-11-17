/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.baseline;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario.ScenarioData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Independent recursively nested-stream implementation. Constraints are evaluated as soon as
 * both participating dimensions have been selected; the full Cartesian product is never materialized.
 */
public final class NestedStreamBaseline {
    private NestedStreamBaseline() { }

    public static Plan compile(ScenarioData scenario) {
        @SuppressWarnings("unchecked")
        List<Rule>[] byDepth = new List[scenario.dimensions().size()];
        for (int d = 0; d < byDepth.length; d++) byDepth[d] = new ArrayList<>();
        for (Rule rule : scenario.rules()) {
            int laterDimension = Math.max(rule.triggerDimension(), rule.targetDimension());
            byDepth[laterDimension].add(new Rule(rule.triggerDimension(),
                    (BitSet) rule.triggerValues().clone(), rule.targetDimension(),
                    (BitSet) rule.allowedValues().clone()));
        }
        return new Plan(scenario, byDepth);
    }

    public static Stream<List<Object>> stream(Plan plan) {
        return nestedAt(0, new int[plan.scenario.dimensions().size()],
                new Object[plan.scenario.dimensions().size()], plan);
    }

    private static Stream<List<Object>> nestedAt(
            int depth, int[] indexes, Object[] values, Plan plan) {
        if (depth == plan.scenario.dimensions().size()) {
            return Stream.of(List.copyOf(Arrays.asList(values.clone())));
        }
        return IntStream.range(0, plan.scenario.dimensions().get(depth).size())
                .filter(candidate -> validCandidate(depth, candidate, indexes, plan))
                .boxed()
                .flatMap(candidate -> {
                    indexes[depth] = candidate;
                    values[depth] = plan.scenario.dimensions().get(depth).get(candidate);
                    return nestedAt(depth + 1, indexes, values, plan);
                });
    }

    private static boolean validCandidate(int depth, int candidate, int[] indexes, Plan plan) {
        for (Rule rule : plan.rulesByDepth[depth]) {
            int triggerIndex = rule.triggerDimension() == depth
                    ? candidate : indexes[rule.triggerDimension()];
            int targetIndex = rule.targetDimension() == depth
                    ? candidate : indexes[rule.targetDimension()];
            if (rule.triggerValues().get(triggerIndex) && !rule.allowedValues().get(targetIndex)) {
                return false;
            }
        }
        return true;
    }

    public static final class Plan {
        private final ScenarioData scenario;
        private final List<Rule>[] rulesByDepth;

        private Plan(ScenarioData scenario, List<Rule>[] rulesByDepth) {
            this.scenario = scenario;
            this.rulesByDepth = rulesByDepth;
        }
    }
}
