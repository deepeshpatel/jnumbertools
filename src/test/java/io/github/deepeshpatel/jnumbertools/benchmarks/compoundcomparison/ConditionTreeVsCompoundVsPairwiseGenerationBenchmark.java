/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison.GenerationChecks.Summary;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;

/**
 * Compares three ways of expressing the same rules against the shared conditional-product engine:
 * the pairwise-only entry point ({@code conditionalProductOf(...).when(dim).is(v).then(...)}), the
 * flat compound entry point ({@code compoundProductOf(...).when(dim).is(v).and(dim2)...or(dim3)...then(...)}),
 * and the new {@link Condition}-tree entry point ({@code compoundProductOf(...).when(condition(dim).is(v)...)}).
 *
 * <p>Because {@code Condition.and(Condition)}/{@code or(Condition)} rewrite directly into the same
 * OR-of-AND-groups form the flat chain already compiles to (see {@link Condition}), a rule built with
 * {@code Condition} and the same rule built with the flat chain produce byte-for-byte identical
 * {@code MultiTriggerRule} groups once both reach the shared model; any difference measured here is
 * construction/wrapper overhead, not a difference in how the rule is evaluated or counted.</p>
 *
 * <h2>Part 1: a pairwise-collapsing chain, all three entry points</h2>
 * <p>The shared rule is the same chain used by {@link CompoundVsPairwiseGenerationBenchmark}: whenever
 * dimension i contains 0, dimension i+1 must not contain 0. Every rule links exactly two dimensions
 * with a single condition on each side, so it collapses to the pairwise engine under all three entry
 * points; this isolates per-entry-point wrapper overhead with the traversal algorithm held fixed.
 * The {@code @Param} scenarios are identical to {@link CompoundVsPairwiseGenerationBenchmark} so results
 * can be compared directly across the two benchmark classes.</p>
 *
 * <h2>Part 2: a genuine OR-of-AND multi-trigger rule, compound-flat vs Condition-tree</h2>
 * <p>Reuses the exact rule from {@link CompoundMultiTriggerGenerationBenchmark} (an AND/OR trigger over
 * several dimensions that the pairwise API cannot express at all, so there is no pairwise variant here),
 * built once with the flat {@code .and()/.or()} chain and once with an equivalent (not nested — see note
 * below) {@code Condition} tree, to check that routing a non-trivial OR-of-AND rule through
 * {@code Condition} costs nothing extra once the model is built. {@code Condition} is deliberately not
 * exercised with real nesting (AND inside OR, OR inside AND) here, because the flat chain has no
 * equivalent to compare against for that shape; nesting support is covered for correctness by
 * {@code CompoundConditionalProductBuilderTest}, not performance, in this change.</p>
 *
 * <p>Trial setup validates every builder against an independent brute-force oracle before any timed
 * method runs, exactly as the sibling benchmarks in this package do.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
@Threads(1)
public class ConditionTreeVsCompoundVsPairwiseGenerationBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15", "4x30", "5x18"})
    public String scenario;

    private ConditionalProductBuilder pairwiseBuilder;
    private CompoundConditionalProductBuilder compoundFlatBuilder;
    private CompoundConditionalProductBuilder conditionTreeBuilder;

    private CompoundConditionalProductBuilder multiTriggerFlatBuilder;
    private CompoundConditionalProductBuilder multiTriggerConditionBuilder;

    @Setup(Level.Trial)
    public void setup() {
        String[] parts = scenario.split("x");
        int dimensionsCount = Integer.parseInt(parts[0]);
        int domainSize = Integer.parseInt(parts[1]);

        List<List<Integer>> dimensions = newDimensions(dimensionsCount, domainSize);
        List<List<Integer>> dimensionsForCompound = newDimensions(dimensionsCount, domainSize);
        List<List<Integer>> dimensionsForCondition = newDimensions(dimensionsCount, domainSize);

        CartesianProduct products = new CartesianProduct();
        pairwiseBuilder = products.conditionalProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));
        compoundFlatBuilder = products.compoundProductOf(
                dimensionsForCompound.get(0), dimensionsForCompound.get(1),
                dimensionsForCompound.subList(2, dimensionsCount).toArray(List<?>[]::new));
        conditionTreeBuilder = products.compoundProductOf(
                dimensionsForCondition.get(0), dimensionsForCondition.get(1),
                dimensionsForCondition.subList(2, dimensionsCount).toArray(List<?>[]::new));

        for (int i = 0; i < dimensionsCount - 1; i++) {
            pairwiseBuilder = pairwiseBuilder.when(dimensions.get(i)).is(0)
                    .then(dimensions.get(i + 1)).isNot(0);
            compoundFlatBuilder = compoundFlatBuilder.when(dimensionsForCompound.get(i)).is(0)
                    .then(dimensionsForCompound.get(i + 1)).isNot(0);
            conditionTreeBuilder = conditionTreeBuilder
                    .when(condition(dimensionsForCondition.get(i)).is(0))
                    .then(dimensionsForCondition.get(i + 1)).isNot(0);
        }

        int[] sizes = new int[dimensionsCount];
        Arrays.fill(sizes, domainSize);
        Predicate<int[]> chain = tuple -> {
            for (int i = 0; i + 1 < tuple.length; i++) {
                if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
            }
            return true;
        };
        Summary expected = GenerationChecks.oracle(sizes, chain);
        long expectedHashSum = GenerationChecks.hashSum(sizes, chain);

        GenerationChecks.require(scenario, "pairwise count()", expected.count(), pairwiseBuilder.count().longValueExact());
        GenerationChecks.require(scenario, "compound-flat count()", expected.count(), compoundFlatBuilder.count().longValueExact());
        GenerationChecks.require(scenario, "condition-tree count()", expected.count(), conditionTreeBuilder.count().longValueExact());

        GenerationChecks.require(scenario, "pairwise anyOrder()", expected,
                GenerationChecks.summarize(pairwiseBuilder.anyOrder().stream()));
        GenerationChecks.require(scenario, "compound-flat generate()", expected,
                GenerationChecks.summarize(compoundFlatBuilder.generate().stream()));
        GenerationChecks.require(scenario, "condition-tree generate()", expected,
                GenerationChecks.summarize(conditionTreeBuilder.generate().stream()));

        GenerationChecks.require(scenario, "pairwiseAnyOrder()", expectedHashSum, pairwiseAnyOrder());
        GenerationChecks.require(scenario, "compoundFlatGenerate()", expectedHashSum, compoundFlatGenerate());
        GenerationChecks.require(scenario, "conditionTreeGenerate()", expectedHashSum, conditionTreeGenerate());
        GenerationChecks.require(scenario, "pairwiseAnyOrderParallel()", expectedHashSum, pairwiseAnyOrderParallel());
        GenerationChecks.require(scenario, "compoundFlatGenerateParallel()", expectedHashSum, compoundFlatGenerateParallel());
        GenerationChecks.require(scenario, "conditionTreeGenerateParallel()", expectedHashSum, conditionTreeGenerateParallel());

        // ── Part 2: genuine OR-of-AND multi-trigger rule, same shape as CompoundMultiTriggerGenerationBenchmark ──
        if (dimensionsCount >= 3 && domainSize >= 3) {
            List<List<Integer>> dimsFlat = newDimensions(dimensionsCount, domainSize);
            List<List<Integer>> dimsCondition = newDimensions(dimensionsCount, domainSize);

            multiTriggerFlatBuilder = new CartesianProduct().compoundProductOf(
                    dimsFlat.get(0), dimsFlat.get(1), dimsFlat.subList(2, dimensionsCount).toArray(List<?>[]::new));
            multiTriggerConditionBuilder = new CartesianProduct().compoundProductOf(
                    dimsCondition.get(0), dimsCondition.get(1), dimsCondition.subList(2, dimensionsCount).toArray(List<?>[]::new));

            for (int i = 0; i < dimensionsCount - 1; i++) {
                multiTriggerFlatBuilder = multiTriggerFlatBuilder.when(dimsFlat.get(i)).is(0)
                        .then(dimsFlat.get(i + 1)).isNot(0);
                multiTriggerConditionBuilder = multiTriggerConditionBuilder.when(dimsCondition.get(i)).is(0)
                        .then(dimsCondition.get(i + 1)).isNot(0);
            }

            List<Integer> lastFlat = dimsFlat.get(dimensionsCount - 1);
            List<Integer> lastCondition = dimsCondition.get(dimensionsCount - 1);
            boolean alternative = dimensionsCount >= 4;
            if (alternative) {
                multiTriggerFlatBuilder = multiTriggerFlatBuilder
                        .when(dimsFlat.get(0)).is(0).and(dimsFlat.get(1)).is(1)
                        .or(dimsFlat.get(1)).is(2).and(dimsFlat.get(2)).is(0)
                        .then(lastFlat).isNot(0);
                multiTriggerConditionBuilder = multiTriggerConditionBuilder
                        .when(
                                condition(dimsCondition.get(0)).is(0).and(condition(dimsCondition.get(1)).is(1))
                                        .or(condition(dimsCondition.get(1)).is(2).and(condition(dimsCondition.get(2)).is(0)))
                        )
                        .then(lastCondition).isNot(0);
            } else {
                multiTriggerFlatBuilder = multiTriggerFlatBuilder
                        .when(dimsFlat.get(0)).is(0).and(dimsFlat.get(1)).is(1)
                        .then(lastFlat).isNot(0);
                multiTriggerConditionBuilder = multiTriggerConditionBuilder
                        .when(condition(dimsCondition.get(0)).is(0).and(condition(dimsCondition.get(1)).is(1)))
                        .then(lastCondition).isNot(0);
            }

            int lastIndex = dimensionsCount - 1;
            Predicate<int[]> multiTriggerValid = tuple -> {
                for (int i = 0; i + 1 < tuple.length; i++) {
                    if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
                }
                boolean triggered = (tuple[0] == 0 && tuple[1] == 1)
                        || (alternative && tuple[1] == 2 && tuple[2] == 0);
                return !(triggered && tuple[lastIndex] == 0);
            };
            Summary multiExpected = GenerationChecks.oracle(sizes, multiTriggerValid);
            long multiExpectedHashSum = GenerationChecks.hashSum(sizes, multiTriggerValid);

            GenerationChecks.require(scenario, "multiTrigger flat count()",
                    multiExpected.count(), multiTriggerFlatBuilder.count().longValueExact());
            GenerationChecks.require(scenario, "multiTrigger condition count()",
                    multiExpected.count(), multiTriggerConditionBuilder.count().longValueExact());
            GenerationChecks.require(scenario, "multiTrigger flat generate()", multiExpected,
                    GenerationChecks.summarize(multiTriggerFlatBuilder.generate().stream()));
            GenerationChecks.require(scenario, "multiTrigger condition generate()", multiExpected,
                    GenerationChecks.summarize(multiTriggerConditionBuilder.generate().stream()));
            GenerationChecks.require(scenario, "multiTriggerFlatGenerate()", multiExpectedHashSum, multiTriggerFlatGenerate());
            GenerationChecks.require(scenario, "multiTriggerConditionGenerate()", multiExpectedHashSum,
                    multiTriggerConditionGenerate());
        }
    }

    private static List<List<Integer>> newDimensions(int dimensionsCount, int domainSize) {
        List<List<Integer>> dimensions = new ArrayList<>(dimensionsCount);
        for (int i = 0; i < dimensionsCount; i++) {
            List<Integer> values = new ArrayList<>(domainSize);
            for (int value = 0; value < domainSize; value++) {
                values.add(value);
            }
            dimensions.add(List.copyOf(values));
        }
        return dimensions;
    }

    // ── Part 1 ──────────────────────────────────────────────────────────────────────────────────────

    @Benchmark
    public long pairwiseAnyOrder() {
        return pairwiseBuilder.anyOrder().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long compoundFlatGenerate() {
        return compoundFlatBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionTreeGenerate() {
        return conditionTreeBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long pairwiseAnyOrderParallel() {
        return pairwiseBuilder.anyOrder().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long compoundFlatGenerateParallel() {
        return compoundFlatBuilder.generate().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionTreeGenerateParallel() {
        return conditionTreeBuilder.generate().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }

    // ── Part 2 (only meaningful for scenarios with >= 3 dimensions and domain size >= 3) ───────────

    @Benchmark
    public long multiTriggerFlatGenerate() {
        return multiTriggerFlatBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long multiTriggerConditionGenerate() {
        return multiTriggerConditionBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }
}
