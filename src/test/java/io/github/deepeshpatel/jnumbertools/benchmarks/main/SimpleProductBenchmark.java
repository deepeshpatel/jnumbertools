package io.github.deepeshpatel.jnumbertools.benchmarks.main;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Quick screening of JNumberTools SimpleProduct against the equivalent
 * nested Java Stream.flatMap() implementation.
 *
 * Input and builder construction are outside the timed section. Both
 * implementations traverse every tuple and compute the same count/checksum;
 * neither materializes an additional result collection.
 */
public final class SimpleProductBenchmark {

    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASUREMENT_ROUNDS = 9;
    private static final int[] SIZES = {5, 10, 15, 20};

    private SimpleProductBenchmark() {
    }

    public static void main(String[] args) {
        System.out.println("""
                Simple Cartesian Product Benchmark
                ==============================================================
                Baseline: nested Java Stream.flatMap()
                Comparison: JNumberTools SimpleProduct
                Dimensions: 4
                Warmup rounds:      5
                Measurement rounds: 9 (median)
                """);
        System.out.printf("%-8s %-14s %-18s %-18s%n",
                "Size", "Tuples", "JNumberTools ms", "Stream.flatMap ms");
        System.out.println("-".repeat(64));

        for (int size : SIZES) {
            run(size);
        }
    }

    private static void run(int size) {
        List<Integer> a = range(size, 0);
        List<Integer> b = range(size, size);
        List<Integer> c = range(size, size * 2);
        List<Integer> d = range(size, size * 3);

        var builder = Product.simple(a,b,c,d);

        Supplier<Summary> library = () -> summarize(builder.lexOrder());
        Supplier<Summary> streams = () -> summarize(streamProduct(a, b, c, d));

        Summary expected = streams.get();
        Summary actual = library.get();
        if (!expected.equals(actual)) {
            throw new AssertionError("Different results for dimension size " + size
                    + ": expected " + expected + ", got " + actual);
        }

        long expectedCount = (long) size * size * size * size;
        if (actual.count() != expectedCount) {
            throw new AssertionError("Expected " + expectedCount + " tuples, got " + actual.count());
        }

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            if ((i & 1) == 0) {
                library.get();
                streams.get();
            } else {
                streams.get();
                library.get();
            }
        }

        long[] libraryTimes = new long[MEASUREMENT_ROUNDS];
        long[] streamTimes = new long[MEASUREMENT_ROUNDS];

        for (int i = 0; i < MEASUREMENT_ROUNDS; i++) {
            if ((i & 1) == 0) {
                libraryTimes[i] = measure(library, expected);
                streamTimes[i] = measure(streams, expected);
            } else {
                streamTimes[i] = measure(streams, expected);
                libraryTimes[i] = measure(library, expected);
            }
        }

        System.out.printf("%-8d %-14d %-18.3f %-18.3f%n",
                size, expectedCount, medianMillis(libraryTimes), medianMillis(streamTimes));
    }

    private static long measure(Supplier<Summary> operation, Summary expected) {
        long start = System.nanoTime();
        Summary result = operation.get();
        long elapsed = System.nanoTime() - start;
        if (!result.equals(expected)) {
            throw new AssertionError("Unexpected benchmark result: " + result);
        }
        return elapsed;
    }

    private static Stream<List<Integer>> streamProduct(
            List<Integer> a, List<Integer> b, List<Integer> c, List<Integer> d) {
        return a.stream()
                .flatMap(x -> b.stream()
                        .flatMap(y -> c.stream()
                                .flatMap(z -> d.stream()
                                        .map(w -> List.of(x, y, z, w)))));
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

    private static double medianMillis(long[] values) {
        long[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        return sorted[sorted.length / 2] / 1_000_000.0;
    }

    private record Summary(long count, long checksum) {
    }
}
