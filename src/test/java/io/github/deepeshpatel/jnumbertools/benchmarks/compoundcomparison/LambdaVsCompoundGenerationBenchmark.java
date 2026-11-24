/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison.GenerationChecks.Summary;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.LambdaConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition.condition;

/**
 * Compares complete generation through the pairwise, compound and lambda-scoped entry points, on the
 * exact same chain rule and {@code @Param} scenarios as {@link CompoundVsPairwiseGenerationBenchmark}:
 * whenever dimension i contains 0, dimension i+1 must not contain 0, expressed as a {@code BiPredicate}
 * over each adjacent pair rather than an equality/membership trigger.
 *
 * <p>Every lambda rule here links exactly two dimensions, so each one is compiled by
 * {@code LambdaConditionalProductBuilder} into the same lightweight pairwise {@code Relation} table the
 * pairwise and compound APIs use for this rule shape (see {@code ConditionalProductBuilder.withLambdaRule}'s
 * class Javadoc) — not a multi-trigger block. All three builders should therefore show the same generation
 * cost; this benchmark exists to confirm that empirically rather than only by the earlier correctness-only
 * {@code ChkLambda} driver, and to give a baseline for a future benchmark over a lambda rule that genuinely
 * cannot be pairwise (three or more dimensions named in one {@code matches(...)} call), which belongs
 * alongside {@link CompoundMultiTriggerGenerationBenchmark} instead of here.</p>
 *
 * <p>Builder construction and rule registration — including compiling every lambda rule into its relation
 * table — happen in trial setup, outside the timed methods, matching every other benchmark in this
 * package. Trial setup validates every generator's sequential and parallel stream against an independent
 * brute-force result before any timed method runs.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
@Threads(1)
public class LambdaVsCompoundGenerationBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15", "4x30", "5x18"})
    public String scenario;

    private CompoundConditionalProductBuilder compoundBuilder;
    private ConditionalProductBuilder conditionalBuilder;
    private LambdaConditionalProductBuilder lambdaBuilder;

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
        lambdaBuilder = products.withLambda(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));

        for (int i = 0; i < dimensionsCount - 1; i++) {
            List<Integer> trigger = dimensions.get(i);
            List<Integer> target = dimensions.get(i + 1);

            compoundBuilder = compoundBuilder.when(condition(trigger).is(0))
                    .then(target).isNot(0);
            conditionalBuilder = conditionalBuilder.when(trigger).is(0)
                    .then(target).isNot(0);
            lambdaBuilder = lambdaBuilder.when(trigger, target)
                    .matches((t, v) -> !(t == 0 && v == 0));
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
        GenerationChecks.require(scenario, "pairwise anyOrder()", expected,
                GenerationChecks.summarize(conditionalBuilder.anyOrder().stream()));
        GenerationChecks.require(scenario, "lambda generate()", expected,
                GenerationChecks.summarize(lambdaBuilder.generate().stream()));
        GenerationChecks.require(scenario, "lambda generate() parallel", expected,
                GenerationChecks.summarize(lambdaBuilder.generate().stream().parallel()));
        GenerationChecks.require(scenario, "lambda count()", expected.count(),
                lambdaBuilder.count().longValueExact());

        GenerationChecks.require(scenario, "compoundGenerate()", expectedHashSum, compoundGenerate());
        GenerationChecks.require(scenario, "conditionalAnyOrder()", expectedHashSum, conditionalAnyOrder());
        GenerationChecks.require(scenario, "lambdaGenerate()", expectedHashSum, lambdaGenerate());
        GenerationChecks.require(scenario, "lambdaGenerateParallel()", expectedHashSum, lambdaGenerateParallel());
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
    public long lambdaGenerate() {
        return lambdaBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long lambdaGenerateParallel() {
        return lambdaBuilder.generate().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }
}
