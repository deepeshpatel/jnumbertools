/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison.GenerationChecks.Summary;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;

/**
 * Compares complete generation through the compound and pairwise APIs backed by the shared conditional-product engine.
 *
 * <p>Both sequential and parallel stream execution are measured. The parallel cases explicitly
 * call {@code stream().parallel()}, because {@code StreamableIterable.stream()} creates a
 * sequential stream by default. JMH uses one benchmark thread; parallelism is provided by the
 * stream's common ForkJoinPool.</p>
 *
 * <p>Builder construction and rule registration happen in trial setup, outside the timed
 * methods. Each invocation creates the generator and consumes every tuple through the same
 * hash reduction.</p>
 *
 * <p>Every rule here links two dimensions, so the compound builder's model has no multi-trigger rule and
 * its generator takes the pairwise traversal. This benchmark therefore measures the overhead of the compound
 * API wrapper against the pairwise API. The compound traversal itself is measured by
 * {@link CompoundMultiTriggerGenerationBenchmark}.</p>
 *
 * <p>Trial setup checks the exact stream code that is timed. The sequential and the parallel stream of every
 * generator are summarised and compared with an independent brute-force result, so the parallel
 * spliterators are validated too. Each timed method must also return the expected hash sum. Encounter order
 * is not compared.</p>
 *
 * <p>The shared rule shape is a chain: whenever dimension i contains 0, dimension i+1 must
 * not contain 0. Both APIs receive exactly these rules. The larger scenarios increase the
 * domain size so the unordered spliterators have enough candidate work to expose splitting.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
@Threads(1)
public class CompoundVsPairwiseGenerationBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15", "4x30", "5x18"})
    public String scenario;

    private CompoundConditionalProductBuilder compoundBuilder;
    private ConditionalProductBuilder conditionalBuilder;

    @Setup(Level.Trial)
    public void setup() {
        String[] parts = scenario.split("x");
        int dimensionsCount = Integer.parseInt(parts[0]);
        int domainSize = Integer.parseInt(parts[1]);

        List<List<Integer>> dimensions = new ArrayList<>(dimensionsCount);
        for (int i = 0; i < dimensionsCount; i++) {
            List<Integer> values = new ArrayList<>(domainSize);
            for (int value = 0; value < domainSize; value++) {
                values.add(value);
            }
            dimensions.add(List.copyOf(values));
        }

        CartesianProduct products = new CartesianProduct();
        compoundBuilder = products.compoundProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));
        conditionalBuilder = products.conditionalProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));

        for (int i = 0; i < dimensionsCount - 1; i++) {
            List<Integer> trigger = dimensions.get(i);
            List<Integer> target = dimensions.get(i + 1);

            compoundBuilder = compoundBuilder.when(condition(trigger).is(0))
                    .then(target).isNot(0);
            conditionalBuilder = conditionalBuilder.when(trigger).is(0)
                    .then(target).isNot(0);
        }

        int[] sizes = new int[dimensionsCount];
        java.util.Arrays.fill(sizes, domainSize);
        Predicate<int[]> chain = tuple -> {
            for (int i = 0; i + 1 < tuple.length; i++) {
                if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
            }
            return true;
        };
        Summary expected = GenerationChecks.oracle(sizes, chain);
        long expectedHashSum = GenerationChecks.hashSum(sizes, chain);

        GenerationChecks.require(scenario, "compound generate()", expected,
                GenerationChecks.summarize(compoundBuilder.generate().stream()));
        GenerationChecks.require(scenario, "compound generate() parallel", expected,
                GenerationChecks.summarize(compoundBuilder.generate().stream().parallel()));
        GenerationChecks.require(scenario, "pairwise anyOrder()", expected,
                GenerationChecks.summarize(conditionalBuilder.anyOrder().stream()));
        GenerationChecks.require(scenario, "pairwise anyOrder() parallel", expected,
                GenerationChecks.summarize(conditionalBuilder.anyOrder().stream().parallel()));
        GenerationChecks.require(scenario, "pairwise lexOrder()", expected,
                GenerationChecks.summarize(conditionalBuilder.lexOrder().stream()));
        GenerationChecks.require(scenario, "pairwise lexOrder() parallel", expected,
                GenerationChecks.summarize(conditionalBuilder.lexOrder().stream().parallel()));

        GenerationChecks.require(scenario, "compoundGenerate()", expectedHashSum, compoundGenerate());
        GenerationChecks.require(scenario, "conditionalAnyOrder()", expectedHashSum, conditionalAnyOrder());
        GenerationChecks.require(scenario, "conditionalLexOrder()", expectedHashSum, conditionalLexOrder());
        GenerationChecks.require(scenario, "compoundGenerateParallel()", expectedHashSum, compoundGenerateParallel());
        GenerationChecks.require(scenario, "conditionalAnyOrderParallel()", expectedHashSum,
                conditionalAnyOrderParallel());
        GenerationChecks.require(scenario, "conditionalLexOrderParallel()", expectedHashSum,
                conditionalLexOrderParallel());
    }

    @Benchmark
    public long compoundGenerate() {
        return compoundBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionalAnyOrder() {
        return conditionalBuilder.anyOrder().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionalLexOrder() {
        return conditionalBuilder.lexOrder().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long compoundGenerateParallel() {
        return compoundBuilder.generate().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionalAnyOrderParallel() {
        return conditionalBuilder.anyOrder().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long conditionalLexOrderParallel() {
        return conditionalBuilder.lexOrder().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }
}
