/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.benchmarks.jmh.conditionalproduct.support.SpecialOperationsCase;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * {@code count} and {@code isEmpty} on a product that is empty, where the emptiness is only revealed by the last
 * dimension. A plain enumeration has to walk every dead prefix to learn that nothing is valid, while the library answers
 * from its feasibility tier ({@code isEmpty}) or its counts ({@code count}). The rank and sampling operations are not
 * meaningful on an empty product, so they are not parameters here. Each call includes builder construction.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductEmptyProductBenchmark {

    @Param({"count", "isEmpty"})
    public String operation;

    @Param({"EMPTY_DEEP-5x25"})
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
