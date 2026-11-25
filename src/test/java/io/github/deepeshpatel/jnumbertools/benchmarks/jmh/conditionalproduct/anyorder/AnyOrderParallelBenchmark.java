/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.anyorder;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Compares complete sequential and parallel consumption of {@code anyOrder()}.
 *
 * <p>The cases deliberately vary how much work is exposed by the first dimension:
 * a wide root, a singleton root, a narrow root, and a skewed set of prefix subtrees.
 * Every benchmark consumes the complete result and returns an order-independent checksum.</p>
 *
 * <p>{@code traversalOnly} reuses a builder prepared at trial setup.
 * {@code endToEnd} includes dimension/rule construction, generator creation, and traversal.</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
public class AnyOrderParallelBenchmark {

    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param({"WIDE_ROOT", "SINGLETON_ROOT", "NARROW_ROOT", "SKEWED_ROOT"})
        public String scenarioName;

        ConditionalProductBuilder builder;
        long expectedChecksum;

        @Setup(Level.Trial)
        public void setup() {
            builder = buildScenario(scenarioName);
            expectedChecksum = checksum(builder.anyOrder().stream());
            long parallelChecksum = checksum(builder.anyOrder().stream().parallel());
            if (parallelChecksum != expectedChecksum) {
                throw new IllegalStateException(scenarioName
                        + ": sequential and parallel anyOrder checksums differ");
            }
        }
    }

    @Benchmark
    public long sequentialTraversal(BenchmarkState state) {
        return checksum(state.builder.anyOrder().stream());
    }

    @Benchmark
    public long parallelTraversal(BenchmarkState state) {
        return checksum(state.builder.anyOrder().stream().parallel());
    }

    @Benchmark
    public long sequentialEndToEnd(BenchmarkState state) {
        return checksum(buildScenario(state.scenarioName).anyOrder().stream());
    }

    @Benchmark
    public long parallelEndToEnd(BenchmarkState state) {
        return checksum(buildScenario(state.scenarioName).anyOrder().stream().parallel());
    }

    private static ConditionalProductBuilder buildScenario(String scenario) {
        List<Integer> first;
        List<Integer> second;
        List<Integer> third;

        switch (scenario) {
            case "WIDE_ROOT" -> {
                first = range(64);
                second = range(48);
                third = range(24);
                return JNumberTools.cartesianProduct().conditionalProductOf(first).and(second).and(third)
                        .when(first).isOneOf(range(32)).then(second).isOneOf(range(12))
                        .when(second).isOneOf(range(12)).then(third).isOneOf(range(6));
            }
            case "SINGLETON_ROOT" -> {
                first = range(1);
                second = range(96);
                third = range(32);
                return JNumberTools.cartesianProduct().conditionalProductOf(first).and(second).and(third)
                        .when(second).isOneOf(range(48)).then(third).isOneOf(range(8));
            }
            case "NARROW_ROOT" -> {
                first = range(2);
                second = range(96);
                third = range(32);
                return JNumberTools.cartesianProduct().conditionalProductOf(first).and(second).and(third)
                        .when(first).is(0).then(second).isOneOf(range(4))
                        .when(first).is(1).then(second).isOneOf(range(92, 96))
                        .when(second).isOneOf(range(4)).then(third).isOneOf(range(8))
                        .when(second).isOneOf(range(92, 96)).then(third).isOneOf(range(24, 32));
            }
            case "SKEWED_ROOT" -> {
                first = range(32);
                second = range(48);
                third = range(24);
                return JNumberTools.cartesianProduct().conditionalProductOf(first).and(second).and(third)
                        .when(first).isOneOf(range(28)).then(second).is(0)
                        .when(second).is(0).then(third).isOneOf(range(2));
            }
            default -> throw new IllegalArgumentException("Unknown benchmark scenario: " + scenario);
        }
    }

    private static List<Integer> range(int end) {
        return IntStream.range(0, end).boxed().toList();
    }

    private static List<Integer> range(int start, int end) {
        return IntStream.range(start, end).boxed().toList();
    }

    private static long checksum(java.util.stream.Stream<List<Object>> tuples) {
        try (tuples) {
            return tuples.mapToLong(AnyOrderParallelBenchmark::tupleHash).sum();
        }
    }

    private static long tupleHash(List<Object> tuple) {
        long hash = 1;
        for (Object value : tuple) hash = hash * 31 + (Integer) value;
        return hash;
    }
}
