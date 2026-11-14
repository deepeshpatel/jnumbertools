/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.datastructure.Tuple;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for plain Cartesian-product traversal: the simple and constrained products against
 * hand-written loops that build the same tuples.
 *
 * <ul>
 *   <li>{@code simpleProduct}: {@code simpleProductOf(...).and(...).lexOrder()}</li>
 *   <li>{@code constrainedProduct}: {@code constrainedProductOfDistinct(1, ...).andDistinct(1, ...).lexOrder()},
 *       the same tuples through the constrained product</li>
 *   <li>{@code loopWithTuple}: an odometer loop that wraps each tuple with {@link Tuple} (what the
 *       generators do now)</li>
 *   <li>{@code loopWithUnmodifiableList}: the same loop wrapping each tuple with
 *       {@code Collections.unmodifiableList(Arrays.asList(...))} (what the generators did before), so the
 *       difference to {@code loopWithTuple} is the cost of the wrapper alone</li>
 *   <li>{@code loopWithoutLists}: an odometer over an {@code int[]} that creates no tuple objects, a lower
 *       bound for any API that returns lists</li>
 *   <li>{@code streamingLoopWithTuple} and {@code streamingLoopWithUnmodifiableList}: the same two wrappers,
 *       but each tuple is consumed as soon as it is built instead of being kept in a list, which is how the
 *       library variants are consumed (the {@code loopWith...} pair keeps all 1,000 tuples alive, which costs
 *       more and stops the JIT from removing the wrapper)</li>
 * </ul>
 *
 * Every variant produces the same tuples in the same order; this is verified in the trial setup, outside the
 * timed methods. Each operation traverses the first 1,000 tuples and folds every element into a checksum, so
 * element reads are part of the cost. Run with {@code -prof gc} to see allocation per operation
 * ({@code gc.alloc.rate.norm}).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class ProductTupleBenchmark {

    private static final int FIRST_N = 1_000;

    @State(Scope.Thread)
    public static class BenchmarkState {

        /** Tuple length x values per dimension: 3 x 10 = 1,000 tuples, 6 x 5 = 15,625, 10 x 3 = 59,049. */
        @Param({"3x10", "6x5", "10x3"})
        public String shape;

        List<List<Integer>> lists;

        io.github.deepeshpatel.jnumbertools.generator.product.simple.SimpleProductBuilder simple;
        io.github.deepeshpatel.jnumbertools.generator.product.constrained.ConstrainedProductBuilder constrained;

        @Setup(Level.Trial)
        public void setup() {
            String[] parts = shape.split("x");
            int dimensions = Integer.parseInt(parts[0]);
            int values = Integer.parseInt(parts[1]);

            lists = new ArrayList<>(dimensions);
            for (int d = 0; d < dimensions; d++) {
                List<Integer> list = new ArrayList<>(values);
                for (int v = 0; v < values; v++) list.add(v);
                lists.add(List.copyOf(list));
            }

            var simpleStart = JNumberTools.cartesianProduct().simpleProductOf(lists.get(0));
            var constrainedStart = JNumberTools.cartesianProduct().constrainedProductOfDistinct(1, lists.get(0));
            for (int d = 1; d < dimensions; d++) {
                simpleStart = simpleStart.and(lists.get(d));
                constrainedStart = constrainedStart.andDistinct(1, lists.get(d));
            }
            simple = simpleStart;
            constrained = constrainedStart;

            verify();
        }

        private void verify() {
            List<List<Object>> expected = firstN(simple.lexOrder());
            if (expected.size() != FIRST_N) {
                throw new IllegalStateException("Expected " + FIRST_N + " tuples for " + shape);
            }
            check("constrained", expected, firstN(constrained.lexOrder()));
            check("loopWithTuple", expected, loopTuples(lists, true));
            check("loopWithUnmodifiableList", expected, loopTuples(lists, false));
            if (loopChecksum(lists) != checksumOf(expected)) {
                throw new IllegalStateException("loopWithoutLists differs for " + shape);
            }
        }

        private static List<List<Object>> firstN(Iterable<List<Object>> tuples) {
            List<List<Object>> result = new ArrayList<>(FIRST_N);
            for (List<Object> tuple : tuples) {
                result.add(tuple);
                if (result.size() == FIRST_N) break;
            }
            return result;
        }

        private static void check(String name, List<List<Object>> expected, List<List<Object>> actual) {
            if (!expected.equals(actual)) {
                throw new IllegalStateException(name + " differs from the simple product");
            }
        }

        private static long checksumOf(List<List<Object>> tuples) {
            long hash = 1;
            for (List<Object> tuple : tuples) {
                for (Object value : tuple) hash = hash * 31 + (Integer) value;
                hash = hash * 31 + 1;
            }
            return hash;
        }
    }

    // -------------------------------------------------------------------------
    // The library
    // -------------------------------------------------------------------------

    @Benchmark
    public long simpleProduct(BenchmarkState state) {
        return traverse(state.simple.lexOrder());
    }

    @Benchmark
    public long constrainedProduct(BenchmarkState state) {
        return traverse(state.constrained.lexOrder());
    }

    // -------------------------------------------------------------------------
    // Hand-written loops
    // -------------------------------------------------------------------------

    @Benchmark
    public long loopWithTuple(BenchmarkState state) {
        return checksum(loopTuples(state.lists, true));
    }

    @Benchmark
    public long loopWithUnmodifiableList(BenchmarkState state) {
        return checksum(loopTuples(state.lists, false));
    }

    @Benchmark
    public long streamingLoopWithTuple(BenchmarkState state) {
        return streamingLoop(state.lists, true);
    }

    @Benchmark
    public long streamingLoopWithUnmodifiableList(BenchmarkState state) {
        return streamingLoop(state.lists, false);
    }

    @Benchmark
    public long loopWithoutLists(BenchmarkState state) {
        return loopChecksum(state.lists);
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private static long traverse(Iterable<List<Object>> tuples) {
        long hash = 1;
        int count = 0;
        for (List<Object> tuple : tuples) {
            for (Object value : tuple) hash = hash * 31 + (Integer) value;
            hash = hash * 31 + 1;
            if (++count == FIRST_N) break;
        }
        return hash;
    }

    private static long checksum(List<List<Object>> tuples) {
        long hash = 1;
        for (List<Object> tuple : tuples) {
            for (Object value : tuple) hash = hash * 31 + (Integer) value;
            hash = hash * 31 + 1;
        }
        return hash;
    }

    /** First FIRST_N tuples in lexicographic order, rightmost dimension fastest, each wrapped as requested. */
    private static List<List<Object>> loopTuples(List<List<Integer>> lists, boolean useTuple) {
        int n = lists.size();
        int[] indices = new int[n];
        List<List<Object>> result = new ArrayList<>(FIRST_N);
        while (result.size() < FIRST_N) {
            Object[] tuple = new Object[n];
            for (int i = 0; i < n; i++) tuple[i] = lists.get(i).get(indices[i]);
            result.add(useTuple ? Tuple.wrap(tuple) : Collections.unmodifiableList(Arrays.asList(tuple)));
            if (!advance(indices, lists)) break;
        }
        return result;
    }

    /** Same tuples and checksum as {@link #loopTuples}, but each tuple is read right after it is built. */
    private static long streamingLoop(List<List<Integer>> lists, boolean useTuple) {
        int n = lists.size();
        int[] indices = new int[n];
        long hash = 1;
        for (int count = 0; count < FIRST_N; count++) {
            Object[] array = new Object[n];
            for (int i = 0; i < n; i++) array[i] = lists.get(i).get(indices[i]);
            List<Object> tuple = useTuple ? Tuple.wrap(array) : Collections.unmodifiableList(Arrays.asList(array));
            for (Object value : tuple) hash = hash * 31 + (Integer) value;
            hash = hash * 31 + 1;
            if (!advance(indices, lists)) break;
        }
        return hash;
    }

    private static long loopChecksum(List<List<Integer>> lists) {
        int n = lists.size();
        int[] indices = new int[n];
        long hash = 1;
        for (int count = 0; count < FIRST_N; count++) {
            for (int i = 0; i < n; i++) hash = hash * 31 + lists.get(i).get(indices[i]);
            hash = hash * 31 + 1;
            if (!advance(indices, lists)) break;
        }
        return hash;
    }

    private static boolean advance(int[] indices, List<List<Integer>> lists) {
        for (int i = indices.length - 1; i >= 0; i--) {
            if (indices[i] < lists.get(i).size() - 1) {
                indices[i]++;
                return true;
            }
            indices[i] = 0;
        }
        return false;
    }
}