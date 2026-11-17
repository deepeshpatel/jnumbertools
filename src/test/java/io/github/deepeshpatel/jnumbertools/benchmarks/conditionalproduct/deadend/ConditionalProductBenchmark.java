/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.deadend;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.support.ConditionalProductCaseSupport;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/** End-to-end CONTROLLED_DEAD_END conditional-product benchmarks across the supported workload shapes. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductBenchmark {
    @Param({"3x10", "3x25", "3x50", "5x10", "5x25", "5x50", "7x10", "7x25", "7x50", "10x10", "10x25"})
    public String shape;

    @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
    public String implementation;

    private ConditionalProductCaseSupport benchmark;

    @Setup(Level.Trial)
    public void setup() {
        benchmark = new ConditionalProductCaseSupport(
                "phase-e/CONTROLLED_DEAD_END/" + shape, implementation);
    }

    @Benchmark
    public long endToEnd() {
        return benchmark.endToEnd(implementation);
    }
}
