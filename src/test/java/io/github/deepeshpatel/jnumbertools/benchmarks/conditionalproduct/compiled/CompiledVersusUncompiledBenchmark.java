/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.compiled;

import io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.compiled.CompiledShapes.Shape;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.CompiledConditionalProductDefinition;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.ConditionalProductBuilder;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Does declaring the rules once and binding different lists later beat building the ordinary builder chain per call?
 * Each invocation processes the next of 100 pre-generated datasets (same values, different order), so no call can reuse
 * another's work. UNCOMPILED builds the chain every call; COMPILED defines and compiles once in setup and only binds per
 * call. Both include everything from the lists to the answer. "first" operations stop after 100 or 10 000 tuples.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class CompiledVersusUncompiledBenchmark {
    private static final int DATASETS = 100;

    @Param({"CHAIN-5x25", "DEAD_END-5x25", "SMALL_DOMAIN-10x4", "LARGE-20x50", "RULE_HEAVY-7x25"})
    public String shapeName;

    @Param({"isEmpty", "count", "first100", "first10000"})
    public String operation;

    @Param({"UNCOMPILED", "COMPILED"})
    public String implementation;

    private Shape shape;
    private List<List<List<Integer>>> datasets;
    private CompiledConditionalProductDefinition.CompiledConditionalProduct compiled;
    private int next;
    private int limit;
    private boolean skip;

    @Setup(Level.Trial)
    public void setup() {
        shape = CompiledShapes.shape(shapeName);
        datasets = CompiledShapes.datasets(shape, DATASETS);
        compiled = CompiledShapes.compile(shape);
        limit = operation.startsWith("first") ? Integer.parseInt(operation.substring(5)) : 0;
        // LARGE has far more tuples than any prefix needs, but a limit of 10 000 is still fine; nothing to skip
        skip = false;
    }

    @Benchmark
    public void run(Blackhole bh) {
        List<List<Integer>> dims = datasets.get(next);
        next = next + 1 == DATASETS ? 0 : next + 1;
        if (implementation.equals("UNCOMPILED")) {
            ConditionalProductBuilder b = CompiledShapes.uncompiled(shape, dims);
            switch (operation) {
                case "isEmpty" -> bh.consume(b.isEmpty());
                case "count" -> bh.consume(b.count());
                default -> consume(b.lexOrder().iterator(), bh);
            }
        } else {
            ConditionalProductBuilder b = CompiledShapes.bind(compiled, dims);
            switch (operation) {
                case "isEmpty" -> bh.consume(b.isEmpty());
                case "count" -> bh.consume(b.count());
                default -> consume(b.lexOrder(
                        io.github.deepeshpatel.jnumbertools.generator.product.conditional.LexOrderStrategy.PRUNED_BITSET)
                        .iterator(), bh);
            }
        }
    }

    private void consume(Iterator<?> it, Blackhole bh) {
        for (int i = 0; i < limit && it.hasNext(); i++) bh.consume(it.next());
    }
}
