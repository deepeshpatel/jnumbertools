/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.compoundcomparison;

import io.github.deepeshpatel.jnumbertools.base.CartesianProduct;
import io.github.deepeshpatel.jnumbertools.generator.product.compound.CompoundConditionalProductBuilder;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Compares complete sequential generation by the independent compound and pairwise
 * implementations for the same data and equivalent pairwise rules.
 *
 * <p>Builder construction and rule registration happen in trial setup, outside the
 * timed methods. Each invocation creates the generator and consumes every tuple.
 * Encounter order is intentionally ignored.</p>
 *
 * <p>The shared rule shape is a chain: whenever dimension i contains 0, dimension
 * i+1 must not contain 0. Both APIs receive exactly these rules.</p>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
public class CompoundVsPairwiseGenerationBenchmark {

    @Param({"3x10", "5x10", "7x5", "10x3", "5x15"})
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
        compoundBuilder = products.compoundConditionalProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));
        conditionalBuilder = products.conditionalProductOf(
                dimensions.get(0), dimensions.get(1),
                dimensions.subList(2, dimensionsCount).toArray(List<?>[]::new));

        for (int i = 0; i < dimensionsCount - 1; i++) {
            List<Integer> trigger = dimensions.get(i);
            List<Integer> target = dimensions.get(i + 1);

            compoundBuilder = compoundBuilder.when(trigger).is(0)
                    .then(target).isNot(0);
            conditionalBuilder = conditionalBuilder.when(trigger).is(0)
                    .then(target).isNot(0);
        }

        // Correctness guard: both generators must produce the same tuples, irrespective
        // of their unspecified/different encounter orders.
        GenerationSummary compound = summarize(compoundBuilder.generate());
        GenerationSummary pairwise = summarize(conditionalBuilder.anyOrder());
        if (!compound.equals(pairwise)) {
            throw new IllegalStateException(
                    "Implementations produced different results for " + scenario
                            + ": compound=" + compound + ", pairwise=" + pairwise);
        }
    }

    @Benchmark
    public long compoundGenerate(Blackhole blackhole) {
        long count = 0;
        for (List<Object> tuple : compoundBuilder.generate()) {
            blackhole.consume(tuple);
            count++;
        }
        return count;
    }

    @Benchmark
    public long conditionalAnyOrder(Blackhole blackhole) {
        long count = 0;
        for (List<Object> tuple : conditionalBuilder.anyOrder()) {
            blackhole.consume(tuple);
            count++;
        }
        return count;
    }

    private static GenerationSummary summarize(Iterable<List<Object>> tuples) {
        long count = 0;
        long sum = 0;
        long xor = 0;

        for (List<Object> tuple : tuples) {
            long hash = mix64(tuple.hashCode());
            count++;
            sum += hash;
            xor ^= Long.rotateLeft(hash, (int) (hash & 63));
        }

        return new GenerationSummary(count, sum, xor);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private record GenerationSummary(long count, long sum, long xor) {
    }
}
