/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.support.SpecialOperationsCase;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Special operations on workloads far too large to enumerate: about 1e17 tuples (10x50), 5e33 (20x50) and 2e39
 * (20x100). There is no baseline here, because no enumeration can finish; the point is how the builder scales with the
 * number of dimensions and list sizes. Trial setup checks the count against its closed form and that every returned
 * tuple is valid (ascending for the rank operations, distinct for {@code sample}).
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductScalingBenchmark {

    @Param({"count", "isEmpty", "lexOrderMth", "byRanks", "choice", "sample"})
    public String operation;

    @Param({"LARGE-10x50", "LARGE-20x50", "LARGE-20x100"})
    public String scenario;

    private SpecialOperationsCase benchmark;

    @Setup(Level.Trial)
    public void setup() {
        benchmark = new SpecialOperationsCase(scenario);
        benchmark.verify(operation);
    }

    @Benchmark
    public long run() {
        return benchmark.run(operation, SpecialOperationsCase.LIBRARY);
    }
}
