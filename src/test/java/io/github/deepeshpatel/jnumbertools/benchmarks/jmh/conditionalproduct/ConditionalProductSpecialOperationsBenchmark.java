/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.support.SpecialOperationsCase;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * The library's special operations (count, isEmpty, lexOrderMth, byRanks, choice, sample) against an enumeration
 * baseline on bounded workloads, where enumerating every tuple is feasible. These operations are where the exact
 * counting tier pays off, so full-traversal results alone do not show them. Each call includes builder construction.
 * Trial setup verifies the library result before anything is measured. See
 * {@link ConditionalProductScalingBenchmark} for sizes that cannot be enumerated at all.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductSpecialOperationsBenchmark {

    @Param({"count", "isEmpty", "lexOrderMth", "byRanks", "choice", "sample"})
    public String operation;

    @Param({"CHAIN-5x25", "MANY_VALID-5x25", "DEAD_END-5x25", "DEEP_DEAD_END-5x25", "SMALL_DOMAIN-10x4"})
    public String scenario;

    @Param({"LIBRARY", "ENUMERATION"})
    public String implementation;

    private SpecialOperationsCase benchmark;

    @Setup(Level.Trial)
    public void setup() {
        benchmark = new SpecialOperationsCase(scenario);
        benchmark.verify(operation);
    }

    @Benchmark
    public long run() {
        return benchmark.run(operation, implementation);
    }
}
