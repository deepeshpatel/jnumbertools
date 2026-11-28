
package io.github.deepeshpatel.jnumbertools.benchmarks.product;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Quick screening comparison of JNumberTools SimpleProduct against
 * an equivalent nested Java Stream.flatMap() implementation.
 *
 * Four dimensions are used in both implementations.
 *
 * Builder construction and input preparation happen outside the
 * timed sections. Both implementations traverse every tuple and
 * calculate the same count and checksum.
 *
 * No additional result collection is materialized.
 */
public final class MainSimpleProductSpeedBenchmark {

    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASUREMENT_ROUNDS = 9;

    private static final int[] SIZES = {
            5, 10, 15, 20, 30, 40, 50, 60, 70, 80, 90, 100
    };

    private static volatile long blackhole;

    private MainSimpleProductSpeedBenchmark() {
    }

    public static void main(String[] args) {

        printBenchmarkConditions();

        System.out.printf(
                "%-8s %-14s %-22s %-22s %-12s%n",
                "Size",
                "Tuples",
                "JNumberTools ms",
                "Stream.flatMap ms",
                "Speedup"
        );

        System.out.println("-".repeat(86));

        for (int size : SIZES) {
            runBenchmark(size);
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Benchmark conditions
    // -------------------------------------------------------------------------

    private static void printBenchmarkConditions() {

        System.out.println("""
                Simple Cartesian Product Speed Benchmark
                =====================================================================
                Baseline:            Nested Java Stream.flatMap()
                Comparison:          JNumberTools SimpleProduct
                Dimensions:          4
                Warmup rounds:       5
                Measurement rounds:  9 (median)
                """);

        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JVM:          " + System.getProperty("java.vm.name"));
        System.out.println("OS:           " + System.getProperty("os.name"));
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Benchmark runner
    // -------------------------------------------------------------------------

    private static void runBenchmark(int size) {

        List<Integer> a = range(size, 0);
        List<Integer> b = range(size, size);
        List<Integer> c = range(size, size * 2);
        List<Integer> d = range(size, size * 3);

        /*
         * Build the JNumberTools product once.
         * Builder construction is outside the timed sections.
         */
        var builder = Product.simple(a,b,c,d);

        /*
         * Obtain a fresh stream for every traversal.
         */
        Supplier<Stream<List<Object>>> library =
                () -> builder.lexOrder().stream();

        Supplier<Stream<List<Integer>>> streams =
                () -> streamProduct(a, b, c, d);

        /*
         * Correctness verification outside timed sections.
         */
        Summary expected = summarize(streams.get());
        Summary actual = summarize(library.get());

        verifySameResult(size, expected, actual);

        long expectedCount =
                (long) size * size * size * size;

        if (actual.count() != expectedCount) {
            throw new AssertionError(
                    "Unexpected tuple count for size " + size
                            + ". Expected " + expectedCount
                            + ", got " + actual.count()
            );
        }

        /*
         * Warmup.
         * Alternate execution order to reduce systematic ordering bias.
         */
        for (int round = 0; round < WARMUP_ROUNDS; round++) {

            if ((round & 1) == 0) {
                execute(library, expected);
                execute(streams, expected);
            } else {
                execute(streams, expected);
                execute(library, expected);
            }
        }

        /*
         * Measurement.
         */
        long[] libraryTimes =
                new long[MEASUREMENT_ROUNDS];

        long[] streamTimes =
                new long[MEASUREMENT_ROUNDS];

        for (int round = 0; round < MEASUREMENT_ROUNDS; round++) {

            if ((round & 1) == 0) {
                libraryTimes[round] =
                        measure(library, expected);

                streamTimes[round] =
                        measure(streams, expected);
            } else {
                streamTimes[round] =
                        measure(streams, expected);

                libraryTimes[round] =
                        measure(library, expected);
            }
        }

        double libraryMedian =
                medianMillis(libraryTimes);

        double streamMedian =
                medianMillis(streamTimes);

        double speedup =
                libraryMedian == 0
                        ? Double.POSITIVE_INFINITY
                        : streamMedian / libraryMedian;

        printResult(
                size,
                expectedCount,
                libraryMedian,
                streamMedian,
                speedup
        );
    }

    private static void execute(
            Supplier<? extends Stream<? extends List<?>>> operation,
            Summary expected) {

        Summary result = summarize(operation.get());

        consume(result);

        if (!expected.equals(result)) {
            throw new AssertionError(
                    "Unexpected result during warmup: " + result
            );
        }
    }

    private static long measure(
            Supplier<? extends Stream<? extends List<?>>> operation,
            Summary expected) {

        long start = System.nanoTime();

        Summary result = summarize(operation.get());

        long elapsed = System.nanoTime() - start;

        /*
         * Keep checksum consumption and validation outside
         * the measured interval.
         */
        consume(result);

        if (!expected.equals(result)) {
            throw new AssertionError(
                    "Unexpected benchmark result: " + result
            );
        }

        return elapsed;
    }

    // -------------------------------------------------------------------------
    // Java Stream baseline
    // -------------------------------------------------------------------------

    private static Stream<List<Integer>> streamProduct(
            List<Integer> a,
            List<Integer> b,
            List<Integer> c,
            List<Integer> d) {

        return a.stream()
                .flatMap(x -> b.stream()
                        .flatMap(y -> c.stream()
                                .flatMap(z -> d.stream()
                                        .map(w -> List.of(x, y, z, w)))));
    }

    // -------------------------------------------------------------------------
    // Result traversal and checksum
    // -------------------------------------------------------------------------

    private static Summary summarize(
            Stream<? extends List<?>> tuples) {

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

    private static long append(
            long checksum,
            List<?> tuple) {

        for (Object value : tuple) {
            checksum = checksum * 31 + value.hashCode();
        }

        return checksum * 31 + 1;
    }

    private static void consume(Summary summary) {

        blackhole ^= summary.count()
                + summary.checksum();
    }

    private static void verifySameResult(
            int size,
            Summary expected,
            Summary actual) {

        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Different results for dimension size " + size
                            + ". Expected " + expected
                            + ", got " + actual
            );
        }
    }

    // -------------------------------------------------------------------------
    // Input generation
    // -------------------------------------------------------------------------

    private static List<Integer> range(
            int size,
            int offset) {

        List<Integer> values = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            values.add(offset + i);
        }

        return List.copyOf(values);
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    private static double medianMillis(long[] values) {

        long[] sorted = values.clone();

        Arrays.sort(sorted);

        return sorted[sorted.length / 2] / 1_000_000.0;
    }

    private static void printResult(
            int size,
            long tupleCount,
            double libraryMillis,
            double streamMillis,
            double speedup) {

        System.out.printf(
                "%-8d %-14d %-22.3f %-22.3f %.2fx%n",
                size,
                tupleCount,
                libraryMillis,
                streamMillis,
                speedup
        );
    }

    private record Summary(
            long count,
            long checksum) {
    }
}

/*
Size     Tuples         JNumberTools ms        Stream.flatMap ms      Speedup
--------------------------------------------------------------------------------------
5        625            0.097                  0.173                  1.79x
10       10000          0.304                  0.807                  2.65x
15       50625          0.950                  1.366                  1.44x
20       160000         3.352                  5.151                  1.54x
30       810000         9.359                  17.596                 1.88x
40       2560000        29.477                 54.945                 1.86x
50       6250000        71.898                 136.099                1.89x
60       12960000       148.985                283.398                1.90x
70       24010000       300.902                613.217                2.04x
80       40960000       583.383                1296.921               2.22x
90       65610000       883.213                2278.759               2.58x
100      100000000      1528.032               3559.027               2.33x
 */
