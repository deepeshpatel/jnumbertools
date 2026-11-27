/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.apicomparison;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Public API comparison: JNumberTools SimpleProduct versus nested Stream.flatMap().
 *
 * Builder construction and input creation are performed in trial setup. Both
 * benchmark methods traverse all tuples and compute the same checksum, avoiding
 * result-set allocation that is not required by either API.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(2)
public class SimpleProductApiBenchmark {

    @Param({"5", "10", "15", "20"})
    public int size;

    private List<Integer> a;
    private List<Integer> b;
    private List<Integer> c;
    private List<Integer> d;
    private Iterable<List<Object>> product;
    private long expectedCount;
    private long expectedChecksum;

    @Setup(Level.Trial)
    public void setup() {
        a = range(size, 0);
        b = range(size, size);
        c = range(size, size * 2);
        d = range(size, size * 3);

        product = Product.simple(a,b,c,d).lexOrder();
        Summary expected = summarize(streamProduct());
        Summary actual = summarize(product);
        if (!expected.equals(actual)) {
            throw new IllegalStateException("SimpleProduct differs from Stream.flatMap for size " + size
                    + ": expected " + expected + ", got " + actual);
        }
        expectedCount = expected.count();
        expectedChecksum = expected.checksum();
    }

    @Benchmark
    public long jnumbertoolsSimpleProduct() {
        Summary result = summarize(product);
        verify(result);
        return result.checksum();
    }

    @Benchmark
    public long javaStreamFlatMap() {
        Summary result = summarize(streamProduct());
        verify(result);
        return result.checksum();
    }

    private Stream<List<Integer>> streamProduct() {
        return a.stream()
                .flatMap(x -> b.stream()
                        .flatMap(y -> c.stream()
                                .flatMap(z -> d.stream()
                                        .map(w -> List.of(x, y, z, w)))));
    }

    private void verify(Summary result) {
        if (result.count() != expectedCount || result.checksum() != expectedChecksum) {
            throw new IllegalStateException("Unexpected result for size " + size + ": " + result);
        }
    }

    private static Summary summarize(Iterable<? extends List<?>> tuples) {
        long count = 0;
        long checksum = 1;
        for (List<?> tuple : tuples) {
            checksum = append(checksum, tuple);
            count++;
        }
        return new Summary(count, checksum);
    }

    private static Summary summarize(Stream<? extends List<?>> tuples) {
        long count = 0;
        long checksum = 1;
        try (Stream<? extends List<?>> stream = tuples) {
            var iterator = stream.iterator();
            while (iterator.hasNext()) {
                checksum = append(checksum, iterator.next());
                count++;
            }
        }
        return new Summary(count, checksum);
    }

    private static long append(long checksum, List<?> tuple) {
        for (Object value : tuple) {
            checksum = checksum * 31 + (Integer) value;
        }
        return checksum * 31 + 1;
    }

    private static List<Integer> range(int size, int offset) {
        List<Integer> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(offset + i);
        }
        return List.copyOf(values);
    }

    private record Summary(long count, long checksum) {
    }
}
