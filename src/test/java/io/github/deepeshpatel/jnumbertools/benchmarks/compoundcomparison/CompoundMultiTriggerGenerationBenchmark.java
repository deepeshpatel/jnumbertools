/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison.GenerationChecks.Summary;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompoundConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Measures complete generation through the compound traversal, which a genuine multi-trigger rule selects.
 *
 * <p>Besides the pairwise chain used by {@link CompoundVsPairwiseGenerationBenchmark} (whenever dimension i
 * contains 0, dimension i+1 must not contain 0), the product has an AND/OR rule over several dimensions:</p>
 *
 * <pre>
 * when d0 = 0 and d1 = 1, or d1 = 2 and d2 = 0 (only with four or more dimensions)
 * then the last dimension must not be 0
 * </pre>
 *
 * <p>The pairwise API cannot express this rule, so there is no pairwise counterpart to compare with; the result
 * is validated against an independent brute-force result instead. Trial setup summarises the sequential and the
 * parallel stream and checks that {@code count()} and each timed method agree with that result.</p>
 *
 * <p>Both sequential and parallel stream execution are measured. The parallel case explicitly calls
 * {@code stream().parallel()}, because {@code StreamableIterable.stream()} is sequential by default. Builder
 * construction and rule registration happen in trial setup, outside the timed methods.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
@Threads(1)
public class CompoundMultiTriggerGenerationBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15", "4x30", "5x18"})
    public String scenario;

    private CompoundConditionalProductBuilder compoundBuilder;

    @Setup(Level.Trial)
    public void setup() {
        String[] parts = scenario.split("x");
        int dimensionsCount = Integer.parseInt(parts[0]);
        int domainSize = Integer.parseInt(parts[1]);
        if (dimensionsCount < 3 || domainSize < 3) {
            throw new IllegalArgumentException("The rule needs at least 3 dimensions and the values 0, 1 and 2");
        }

        List<List<Integer>> dimensions = new ArrayList<>(dimensionsCount);
        for (int i = 0; i < dimensionsCount; i++) {
            List<Integer> values = new ArrayList<>(domainSize);
            for (int value = 0; value < domainSize; value++) {
                values.add(value);
            }
            dimensions.add(List.copyOf(values));
        }

        compoundBuilder = new CartesianProduct().compoundConditionalProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));
        for (int i = 0; i < dimensionsCount - 1; i++) {
            compoundBuilder = compoundBuilder.when(dimensions.get(i)).is(0)
                    .then(dimensions.get(i + 1)).isNot(0);
        }

        List<Integer> last = dimensions.get(dimensionsCount - 1);
        boolean alternative = dimensionsCount >= 4;
        if (alternative) {
            compoundBuilder = compoundBuilder
                    .when(dimensions.get(0)).is(0).and(dimensions.get(1)).is(1)
                    .or(dimensions.get(1)).is(2).and(dimensions.get(2)).is(0)
                    .then(last).isNot(0);
        } else {
            compoundBuilder = compoundBuilder
                    .when(dimensions.get(0)).is(0).and(dimensions.get(1)).is(1)
                    .then(last).isNot(0);
        }

        int[] sizes = new int[dimensionsCount];
        Arrays.fill(sizes, domainSize);
        int lastIndex = dimensionsCount - 1;
        Predicate<int[]> valid = tuple -> {
            for (int i = 0; i + 1 < tuple.length; i++) {
                if (tuple[i] == 0 && tuple[i + 1] == 0) return false;
            }
            boolean triggered = (tuple[0] == 0 && tuple[1] == 1)
                    || (alternative && tuple[1] == 2 && tuple[2] == 0);
            return !(triggered && tuple[lastIndex] == 0);
        };
        Summary expected = GenerationChecks.oracle(sizes, valid);
        long expectedHashSum = GenerationChecks.hashSum(sizes, valid);

        GenerationChecks.require(scenario, "count()", expected.count(), compoundBuilder.count().longValueExact());
        GenerationChecks.require(scenario, "generate()", expected,
                GenerationChecks.summarize(compoundBuilder.generate().stream()));
        GenerationChecks.require(scenario, "generate() parallel", expected,
                GenerationChecks.summarize(compoundBuilder.generate().stream().parallel()));
        GenerationChecks.require(scenario, "generate() benchmark method", expectedHashSum, generate());
        GenerationChecks.require(scenario, "generateParallel() benchmark method", expectedHashSum,
                generateParallel());
    }

    @Benchmark
    public long generate() {
        return compoundBuilder.generate().stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    @Benchmark
    public long generateParallel() {
        return compoundBuilder.generate().stream().parallel()
                .mapToLong(List::hashCode)
                .sum();
    }
}
