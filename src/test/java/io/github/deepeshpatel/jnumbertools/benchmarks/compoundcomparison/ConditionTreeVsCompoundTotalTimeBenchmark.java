/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison.GenerationChecks.Summary;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;

/**
 * Measures total end-user time: builder construction, rule registration, <em>and</em> generation,
 * all inside the timed method — unlike {@link ConditionTreeVsCompoundVsPairwiseGenerationBenchmark},
 * where the builder and its rules are built once in {@code @Setup(Level.Trial)} and only
 * {@code generate()}/traversal is timed.
 *
 * <p>That earlier benchmark deliberately excludes rule construction from the timed region, so it
 * cannot show whether building a {@link Condition} tree (the {@code and()}/{@code or()}
 * cross-product/union calls) costs anything extra. This benchmark puts construction back inside the
 * timed method for both the flat {@code .and()/.or()} chain and the equivalent {@code Condition} tree,
 * on the same two rule shapes used throughout this package: a pairwise-collapsing chain, and the
 * genuine OR-of-AND multi-trigger rule from {@link CompoundMultiTriggerGenerationBenchmark}. The
 * dimension lists themselves are prepared once in {@code @Setup} (preparing input data is not part of
 * what is being compared); everything from {@code compoundProductOf(...)} onward is timed.</p>
 *
 * <p>If the flat and {@code Condition}-tree variants are statistically indistinguishable here too,
 * end users pay nothing extra for using {@code Condition}, including construction — which is the basis
 * for deciding whether the flat {@code .and()/.or()} API can be retired in favor of {@code Condition}
 * alone.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
@Threads(1)
public class ConditionTreeVsCompoundTotalTimeBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15", "4x30", "5x18"})
    public String scenario;

    private int dimensionsCount;
    private int domainSize;
    private List<List<Integer>> dims;
    private int[] sizes;

    private boolean alternative;

    @Setup(Level.Trial)
    public void setup() {
        String[] parts = scenario.split("x");
        dimensionsCount = Integer.parseInt(parts[0]);
        domainSize = Integer.parseInt(parts[1]);
        alternative = dimensionsCount >= 4;

        dims = newDimensions();
        sizes = new int[dimensionsCount];
        Arrays.fill(sizes, domainSize);

        // Correctness: build each variant once with the exact code the timed methods use, and check
        // it against an independent brute-force oracle before any timed method runs.
        Predicate<int[]> chain = tuple -> {
            for (int i = 0; i + 1 < tuple.length; i++) {
                if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
            }
            return true;
        };
        Summary chainExpected = GenerationChecks.oracle(sizes, chain);
        GenerationChecks.require(scenario, "compoundFlatTotal()", chainExpected,
                GenerationChecks.summarize(runCompoundFlatChain().generate().stream()));
        GenerationChecks.require(scenario, "conditionTreeTotal()", chainExpected,
                GenerationChecks.summarize(runConditionTreeChain().generate().stream()));

        if (dimensionsCount >= 3 && domainSize >= 3) {
            int lastIndex = dimensionsCount - 1;
            Predicate<int[]> multiTrigger = tuple -> {
                for (int i = 0; i + 1 < tuple.length; i++) {
                    if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
                }
                boolean triggered = (tuple[0] == 0 && tuple[1] == 1)
                        || (alternative && tuple[1] == 2 && tuple[2] == 0);
                return !(triggered && tuple[lastIndex] == 0);
            };
            Summary multiExpected = GenerationChecks.oracle(sizes, multiTrigger);
            GenerationChecks.require(scenario, "multiTriggerFlatTotal()", multiExpected,
                    GenerationChecks.summarize(runMultiTriggerFlat().generate().stream()));
            GenerationChecks.require(scenario, "multiTriggerConditionTotal()", multiExpected,
                    GenerationChecks.summarize(runMultiTriggerCondition().generate().stream()));
        }
    }

    private List<List<Integer>> newDimensions() {
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

    // ── Part 1: pairwise-collapsing chain, construction included ────────────────────────────────────

    private io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder
    runCompoundFlatChain() {
        var builder = new CartesianProduct().compoundProductOf(
                dims.get(0), dims.get(1), dims.subList(2, dimensionsCount).toArray(List<?>[]::new));
        for (int i = 0; i < dimensionsCount - 1; i++) {
            builder = builder.when(dims.get(i)).is(0).then(dims.get(i + 1)).isNot(0);
        }
        return builder;
    }

    private io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder
    runConditionTreeChain() {
        var builder = new CartesianProduct().compoundProductOf(
                dims.get(0), dims.get(1), dims.subList(2, dimensionsCount).toArray(List<?>[]::new));
        for (int i = 0; i < dimensionsCount - 1; i++) {
            builder = builder.when(condition(dims.get(i)).is(0)).then(dims.get(i + 1)).isNot(0);
        }
        return builder;
    }

    @Benchmark
    public long compoundFlatTotal() {
        return runCompoundFlatChain().generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionTreeTotal() {
        return runConditionTreeChain().generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    // ── Part 2: genuine OR-of-AND multi-trigger rule, construction included ─────────────────────────

    private io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder
    runMultiTriggerFlat() {
        var builder = new CartesianProduct().compoundProductOf(
                dims.get(0), dims.get(1), dims.subList(2, dimensionsCount).toArray(List<?>[]::new));
        for (int i = 0; i < dimensionsCount - 1; i++) {
            builder = builder.when(dims.get(i)).is(0).then(dims.get(i + 1)).isNot(0);
        }
        List<Integer> last = dims.get(dimensionsCount - 1);
        if (alternative) {
            builder = builder
                    .when(dims.get(0)).is(0).and(dims.get(1)).is(1)
                    .or(dims.get(1)).is(2).and(dims.get(2)).is(0)
                    .then(last).isNot(0);
        } else {
            builder = builder
                    .when(dims.get(0)).is(0).and(dims.get(1)).is(1)
                    .then(last).isNot(0);
        }
        return builder;
    }

    private io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder
    runMultiTriggerCondition() {
        var builder = new CartesianProduct().compoundProductOf(
                dims.get(0), dims.get(1), dims.subList(2, dimensionsCount).toArray(List<?>[]::new));
        for (int i = 0; i < dimensionsCount - 1; i++) {
            builder = builder.when(condition(dims.get(i)).is(0)).then(dims.get(i + 1)).isNot(0);
        }
        List<Integer> last = dims.get(dimensionsCount - 1);
        if (alternative) {
            builder = builder
                    .when(
                            condition(dims.get(0)).is(0).and(condition(dims.get(1)).is(1))
                                    .or(condition(dims.get(1)).is(2).and(condition(dims.get(2)).is(0)))
                    )
                    .then(last).isNot(0);
        } else {
            builder = builder
                    .when(condition(dims.get(0)).is(0).and(condition(dims.get(1)).is(1)))
                    .then(last).isNot(0);
        }
        return builder;
    }

    @Benchmark
    public long multiTriggerFlatTotal() {
        return runMultiTriggerFlat().generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long multiTriggerConditionTotal() {
        return runMultiTriggerCondition().generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }
}
