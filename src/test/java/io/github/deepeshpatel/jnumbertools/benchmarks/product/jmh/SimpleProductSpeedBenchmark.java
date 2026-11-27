
package io.github.deepeshpatel.jnumbertools.benchmarks.product.jmh;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.function.Supplier;

/**
 * JMH comparison of two implementations of the same four-dimensional
 * Cartesian product:
 *
 * 1. JNumberTools SimpleProduct
 * 2. Nested Java Stream.flatMap()
 *
 * Input preparation and JNumberTools builder construction happen once
 * in @Setup(Level.Trial).
 *
 * Each benchmark invocation:
 * - obtains a fresh stream;
 * - traverses every generated tuple;
 * - passes every tuple to JMH Blackhole;
 * - returns the number of generated tuples.
 *
 * Correctness is verified separately during setup, outside measured
 * benchmark execution.
 */
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(3)
@State(Scope.Thread)
public class SimpleProductSpeedBenchmark {

    @Param({
            "5", "10", "15", "20", "30", "40"
    })
    public int size;

    private List<Integer> a;
    private List<Integer> b;
    private List<Integer> c;
    private List<Integer> d;

    private Supplier<Stream<List<Object>>> libraryProduct;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @Setup(Level.Trial)
    public void setup() {

        a = range(size, 0);
        b = range(size, size);
        c = range(size, size * 2);
        d = range(size, size * 3);

        /*
         * Build the JNumberTools product once.
         * Builder construction is not part of the measured operation.
         */
        var builder = Product.simple(a,b,c,d);
        libraryProduct = () -> builder.lexOrder().stream();

        verifyCorrectness();
    }

    // -------------------------------------------------------------------------
    // Benchmark methods
    // -------------------------------------------------------------------------

    @Benchmark
    public long jnumberToolsSimpleProduct(Blackhole blackhole) {

        return consume(libraryProduct.get(), blackhole);
    }

    @Benchmark
    public long nestedJavaStream(Blackhole blackhole) {

        return consume(
                streamProduct(a, b, c, d),
                blackhole
        );
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
    // Common consumers
    // -------------------------------------------------------------------------

    private static long consume(
            Stream<? extends List<?>> tuples,
            Blackhole blackhole) {

        long count = 0;

        try (Stream<? extends List<?>> stream = tuples) {

            var iterator = stream.iterator();

            while (iterator.hasNext()) {
                blackhole.consume(iterator.next());
                count++;
            }
        }

        return count;
    }

    // -------------------------------------------------------------------------
    // Correctness verification (outside measured execution)
    // -------------------------------------------------------------------------

    private void verifyCorrectness() {

        Summary libraryResult = summarize(libraryProduct.get());

        Summary streamResult =
                summarize(streamProduct(a, b, c, d));

        if (!libraryResult.equals(streamResult)) {
            throw new AssertionError(
                    "JNumberTools and Java Stream results differ for size "
                            + size
                            + ". JNumberTools: " + libraryResult
                            + ", Java Stream: " + streamResult
            );
        }

        long expectedCount =
                (long) size * size * size * size;

        if (libraryResult.count() != expectedCount) {
            throw new AssertionError(
                    "Unexpected tuple count for size " + size
                            + ". Expected " + expectedCount
                            + ", got " + libraryResult.count()
            );
        }
    }

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

    private record Summary(
            long count,
            long checksum) {
    }
}

/*
sample command

mvn test-compile exec:exec \
  -Dexec.executable="java" \
  -Dexec.classpathScope="test" \
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main SimpleProductSpeedBenchmarkJMH -p size=5,10,15 -wi 5 -w 1s -i 10 -r 1s -f 3 -tu ms -bm avgt"

# JMH version: 1.37
# VM version: JDK 27.0.1.0.3, OpenJDK 64-Bit Server VM, 27.0.1.0.3
# VM invoker: /opt/homebrew/Cellar/openjdk/27.0.1.0.3/libexec/openjdk.jdk/Contents/Home/bin/java
# VM options: <none>

Benchmark                                                 (size)  Mode  Cnt  Score    Error  Units
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct       5  avgt   30  0.005 ±  0.001  ms/op
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct      10  avgt   30  0.066 ±  0.003  ms/op
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct      15  avgt   30  0.342 ±  0.007  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream                5  avgt   30  0.012 ±  0.001  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream               10  avgt   30  0.144 ±  0.004  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream               15  avgt   30  0.758 ±  0.006  ms/op

Benchmark                                                 (size)  Mode  Cnt   Score   Error  Units
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct      20  avgt   30   1.086 ± 0.026  ms/op
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct      30  avgt   30   5.856 ± 0.846  ms/op
SimpleProductSpeedBenchmarkJMH.jnumberToolsSimpleProduct      40  avgt   30  17.229 ± 0.412  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream               20  avgt   30   2.322 ± 0.025  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream               30  avgt   30  12.276 ± 0.341  ms/op
SimpleProductSpeedBenchmarkJMH.nestedJavaStream               40  avgt   30  37.117 ± 0.256  ms/op

 */