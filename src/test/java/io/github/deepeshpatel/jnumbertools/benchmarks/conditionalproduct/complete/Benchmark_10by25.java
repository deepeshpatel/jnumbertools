/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.complete;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductCaseSupport;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/** Fixed COMPLETE workload: 10 dimensions, 25 values per dimension. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class Benchmark_10by25 {
    @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
    public String implementation;

    private ConditionalProductCaseSupport benchmark;

    @Setup(Level.Trial)
    public void setup() {
        benchmark = new ConditionalProductCaseSupport("phase-e/COMPLETE/10x25", implementation);
    }

    @Benchmark
    public long endToEnd() {
        return benchmark.endToEnd(implementation);
    }
}
