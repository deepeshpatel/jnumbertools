/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.support;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.baseline.ImperativeBacktrackingBaseline;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.baseline.NestedStreamBaseline;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Rule;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario;
import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.scenario.Scenario.ScenarioData;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.LexOrderStrategy;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

/** Shared setup and end-to-end measurement logic for one conditional-product benchmark case. */
public final class ConditionalProductCaseSupport {
    private final ScenarioData scenario;
    private final List<PreparedRule> preparedRules;
    private final NestedStreamBaseline.Plan baselinePlan;
    private final long expectedCount;

    public ConditionalProductCaseSupport(String caseId, String implementation) {
        scenario = Scenario.create(caseId);
        preparedRules = prepareRules(scenario);
        baselinePlan = NestedStreamBaseline.compile(scenario);

        Summary expected = summarize(NestedStreamBaseline.stream(baselinePlan));
        expectedCount = expected.count();

        if (implementation.equals(IMPERATIVE_ALLOCATING) || implementation.equals(IMPERATIVE_REUSE)) {
            long[] actual = imperative(implementation, ImperativeBacktrackingBaseline.compile(scenario));
            if (actual[0] != expected.count() || actual[1] != expected.checksum()) {
                throw new IllegalStateException(caseId + "/" + implementation
                        + ": imperative baseline differs from the incrementally filtered nested-stream baseline");
            }
        } else if (!implementation.equals("NESTED_STREAM")) {
            Summary actual = summarize(createLibraryGenerator(
                    buildBuilder(scenario, preparedRules), implementation, expectedCount));
            if (!actual.equals(expected)) {
                throw new IllegalStateException(caseId + "/" + implementation
                        + ": library output differs from the incrementally filtered nested-stream baseline");
            }
        }
    }

    /** Imperative backtracking, one {@code Object[]} wrapped as a list per result (like a library tuple). */
    public static final String IMPERATIVE_ALLOCATING = "IMPERATIVE_ALLOCATING";
    /** Imperative backtracking over one reused index array; allocates nothing per result. */
    public static final String IMPERATIVE_REUSE = "IMPERATIVE_REUSE";

    private static long[] imperative(String implementation, ImperativeBacktrackingBaseline.Plan plan) {
        return implementation.equals(IMPERATIVE_ALLOCATING)
                ? ImperativeBacktrackingBaseline.allocating(plan)
                : ImperativeBacktrackingBaseline.reuse(plan);
    }

    /** Includes builder/rule construction, generator creation, and complete tuple consumption. */
    public long endToEnd(String implementation) {
        if (implementation.equals(IMPERATIVE_ALLOCATING) || implementation.equals(IMPERATIVE_REUSE)) {
            return imperative(implementation, ImperativeBacktrackingBaseline.compile(scenario))[1];
        }
        if (implementation.equals("NESTED_STREAM")) {
            return summarize(NestedStreamBaseline.stream(
                    NestedStreamBaseline.compile(scenario))).checksum();
        }
        ConditionalProductBuilder builder = buildBuilder(scenario, preparedRules);
        return summarize(createLibraryGenerator(builder, implementation, expectedCount)).checksum();
    }

    static ConditionalProductBuilder buildBuilder(
            ScenarioData scenario, List<PreparedRule> preparedRules) {
        List<List<Integer>> dimensions = scenario.dimensions();
        ConditionalProductBuilder builder =
                JNumberTools.cartesianProduct().conditional().ofList(dimensions.get(0));
        for (int d = 1; d < dimensions.size(); d++) {
            builder = builder.and(dimensions.get(d));
        }
        for (PreparedRule rule : preparedRules) {
            builder = builder.when(dimensions.get(rule.triggerDimension()))
                    .isOneOf(rule.triggerValues())
                    .then(dimensions.get(rule.targetDimension()))
                    .isOneOf(rule.allowedValues());
        }
        return builder;
    }

    static List<PreparedRule> prepareRules(ScenarioData scenario) {
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

    static Iterable<?> createLibraryGenerator(
            ConditionalProductBuilder builder, String implementation, long expectedCount) {
        if (implementation.equals("ADAPTIVE")) {
            return builder.lexOrder(LexOrderStrategy.ADAPTIVE, expectedCount);
        }
        return builder.lexOrder(LexOrderStrategy.valueOf(implementation));
    }

    private static Summary summarize(Iterable<?> tuples) {
        long count = 0;
        long hash = 1;
        for (Object item : tuples) {
            for (Object value : (List<?>) item) {
                hash = hash * 31 + (Integer) value;
            }
            hash = hash * 31 + 1;
            count++;
        }
        return new Summary(count, hash);
    }

    private static Summary summarize(Stream<List<Object>> tuples) {
        long count = 0;
        long hash = 1;
        try (Stream<List<Object>> stream = tuples) {
            var iterator = stream.iterator();
            while (iterator.hasNext()) {
                for (Object value : iterator.next()) {
                    hash = hash * 31 + (Integer) value;
                }
                hash = hash * 31 + 1;
                count++;
            }
        }
        return new Summary(count, hash);
    }

    private static List<Integer> values(BitSet indexes, int size) {
        List<Integer> result = new ArrayList<>(indexes.cardinality());
        for (int i = indexes.nextSetBit(0); i >= 0 && i < size; i = indexes.nextSetBit(i + 1)) {
            result.add(i);
        }
        return List.copyOf(result);
    }

    record PreparedRule(
            int triggerDimension,
            List<Integer> triggerValues,
            int targetDimension,
            List<Integer> allowedValues) { }

    private record Summary(long count, long checksum) { }
}
