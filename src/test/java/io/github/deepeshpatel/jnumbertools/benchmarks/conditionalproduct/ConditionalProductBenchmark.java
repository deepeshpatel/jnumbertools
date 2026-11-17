/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.support.ConditionalProductCaseSupport;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Unified end-to-end conditional-product benchmark.
 *
 * The scenario parameter represents a valid topology/shape combination. Keeping it as one
 * parameter allows topology-specific exclusions (currently DEAD_END at 10x50) without
 * asking JMH to execute invalid topology/shape combinations.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class ConditionalProductBenchmark {

    @Param({
            "CHAIN-3x10", "CHAIN-3x25", "CHAIN-3x50",
            "CHAIN-5x10", "CHAIN-5x25", "CHAIN-5x50",
            "CHAIN-7x10", "CHAIN-7x25", "CHAIN-7x50",
            "CHAIN-10x10", "CHAIN-10x25", "CHAIN-10x50",

            "MANY_VALID-3x10", "MANY_VALID-3x25", "MANY_VALID-3x50",
            "MANY_VALID-5x10", "MANY_VALID-5x25", "MANY_VALID-5x50",
            "MANY_VALID-7x10", "MANY_VALID-7x25", "MANY_VALID-7x50",
            "MANY_VALID-10x10", "MANY_VALID-10x25", "MANY_VALID-10x50",

            "COMPLETE-3x10", "COMPLETE-3x25", "COMPLETE-3x50",
            "COMPLETE-5x10", "COMPLETE-5x25", "COMPLETE-5x50",
            "COMPLETE-7x10", "COMPLETE-7x25", "COMPLETE-7x50",
            "COMPLETE-10x10", "COMPLETE-10x25", "COMPLETE-10x50",

            "DEAD_END-3x10", "DEAD_END-3x25", "DEAD_END-3x50",
            "DEAD_END-5x10", "DEAD_END-5x25", "DEAD_END-5x50",
            "DEAD_END-7x10", "DEAD_END-7x25", "DEAD_END-7x50",
            "DEAD_END-10x10", "DEAD_END-10x25"
    })
    public String scenario;

    @Param({"PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM"})
    public String implementation;

    private ConditionalProductCaseSupport benchmark;

    @Setup(Level.Trial)
    public void setup() {
        int separator = scenario.lastIndexOf('-');
        if (separator < 1) {
            throw new IllegalArgumentException("Invalid scenario parameter: " + scenario);
        }
        String topology = scenario.substring(0, separator);
        String shape = scenario.substring(separator + 1);
        if (topology.equals("DEAD_END")) {
            topology = "CONTROLLED_DEAD_END";
        }
        benchmark = new ConditionalProductCaseSupport(
                "phase-e/" + topology + "/" + shape, implementation);
    }

    @Benchmark
    public long endToEnd() {
        return benchmark.endToEnd(implementation);
    }
}
