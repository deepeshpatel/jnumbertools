/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.chain;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductCaseSupport;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/** Fixed chain topology: 7 dimensions, 10 values per dimension. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class Benchmark_7by10 {
    @Param({"MANY_VALID", "COMPLETE"})
    public String profile;

    @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
    public String implementation;

    private ConditionalProductCaseSupport benchmark;

    @Setup(Level.Trial)
    public void setup() {
        benchmark = new ConditionalProductCaseSupport(
                "phase-e/" + profile + "/7x10", implementation);
    }

    @Benchmark
    public long endToEnd() {
        return benchmark.endToEnd(implementation);
    }
}
