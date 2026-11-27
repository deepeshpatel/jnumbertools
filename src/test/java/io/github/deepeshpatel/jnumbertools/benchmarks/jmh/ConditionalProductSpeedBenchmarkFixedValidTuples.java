
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JMH comparison of three implementations of the same conditional Cartesian product:
 *
 * 1. Pairwise when-then conditional product
 * 2. Lambda conditional product
 * 3. Incrementally pruned Java Stream baseline
 *
 * Builder construction and rule registration happen once in @Setup(Level.Trial).
 *
 * Each benchmark invocation:
 * - obtains a fresh stream;
 * - traverses every generated tuple;
 * - passes every tuple to JMH Blackhole;
 * - returns the number of generated tuples.
 *
 * Correctness is verified separately during setup, outside measured execution.
 */
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(3)
@State(Scope.Thread)
public class ConditionalProductSpeedBenchmarkFixedValidTuples {

    @Param({
            "50", "100", "200", "300", "400", "500",
            "1000", "2000", "3000", "4000", "5000"
    })
    public int size;

    private static final int[] SIZES = {            50, 100,  200,  300,  400,  500, 1000, 2000,   3000, 4000,  5000};
    private static final int[] EXPECTED_MATCHES = {316, 616, 1216, 1816, 2416, 3016, 6016, 12016, 18016, 24016, 30016};

    private static final String LINUX = "Linux";
    private static final String WINDOWS = "Windows";

    private static final List<String> LINUX_BROWSERS =
            List.of("Firefox", "Safari");

    private static final List<String> LINUX_NETWORKS =
            List.of("4G", "WiFi");

    private static final List<String> LINUX_DATABASES =
            List.of("PostgreSQL", "MySQL");

    private static final List<String> LINUX_REGIONS =
            List.of("ap-south", "eu-west");

    private static final List<String> WINDOWS_BROWSERS =
            List.of("Firefox", "Safari", "Chrome");

    private static final List<String> WINDOWS_DATABASES =
            List.of("PostgreSQL");

    private static final List<String> WINDOWS_REGIONS =
            List.of("us-east", "eu-west");

    private static final String[] BROWSER_BASE = {
            "Firefox", "Safari", "Chrome", "Internet Explorer"
    };

    private static final String[] NETWORK_BASE = {
            "2G", "3G", "4G", "WiFi"
    };

    private static final String[] DATABASE_BASE = {
            "PostgreSQL", "MySQL", "Oracle"
    };

    private static final String[] REGION_BASE = {
            "ap-south", "eu-west", "us-east"
    };

    private Dimensions dimensions;

    private Supplier<Stream<List<Object>>> pairwise;
    private Supplier<Stream<List<Object>>> engineLambda;
    private Supplier<Stream<List<Object>>> prunedStream;

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @Setup(Level.Trial)
    public void setup() {

        dimensions = createDimensions(size);

        buildPairwise();
        buildEngineLambda();
        buildPrunedStream();

        verifyCorrectness();
    }

    // -------------------------------------------------------------------------
    // 1. Pairwise when-then conditional product
    // -------------------------------------------------------------------------

    private void buildPairwise() {

        var builder = Product.conditional(
                        dimensions.os,
                        dimensions.browser,
                        dimensions.network,
                        dimensions.database,
                        dimensions.region
                )

                // Linux rules
                .when(dimensions.os).is(LINUX)
                .then(dimensions.browser).isOneOf(LINUX_BROWSERS)

                .when(dimensions.os).is(LINUX)
                .then(dimensions.network).isOneOf(LINUX_NETWORKS)

                .when(dimensions.os).is(LINUX)
                .then(dimensions.database).isOneOf(LINUX_DATABASES)

                .when(dimensions.os).is(LINUX)
                .then(dimensions.region).isOneOf(LINUX_REGIONS)

                // Windows rules
                .when(dimensions.os).is(WINDOWS)
                .then(dimensions.browser).isOneOf(WINDOWS_BROWSERS)

                .when(dimensions.os).is(WINDOWS)
                .then(dimensions.database).isOneOf(WINDOWS_DATABASES)

                .when(dimensions.os).is(WINDOWS)
                .then(dimensions.region).isOneOf(WINDOWS_REGIONS);

        pairwise = () -> builder.anyOrder().stream();
    }

    // -------------------------------------------------------------------------
    // 2. Lambda conditional product
    // -------------------------------------------------------------------------

    private void buildEngineLambda() {

        var builder = Product.conditional(
                        dimensions.os,
                        dimensions.browser,
                        dimensions.network,
                        dimensions.database,
                        dimensions.region)
                .withLambda(
                        dimensions.os,
                        dimensions.browser,
                        dimensions.network,
                        dimensions.database,
                        dimensions.region
                )

                 .when((os, browser, network, database, region) ->
                        (LINUX.equals(os) ? LINUX_BROWSERS.contains(browser)
                                : WINDOWS.equals(os) ? WINDOWS_BROWSERS.contains(browser) : true)
                        && (!LINUX.equals(os) || LINUX_NETWORKS.contains(network))
                        && (LINUX.equals(os) ? LINUX_DATABASES.contains(database)
                                : WINDOWS.equals(os) ? WINDOWS_DATABASES.contains(database) : true)
                        && (LINUX.equals(os) ? LINUX_REGIONS.contains(region)
                                : WINDOWS.equals(os) ? WINDOWS_REGIONS.contains(region) : true));

        engineLambda = () -> builder.anyOrder().stream();
    }

    // -------------------------------------------------------------------------
    // 4. Incrementally pruned Java Stream baseline
    // -------------------------------------------------------------------------

    private void buildPrunedStream() {

        prunedStream = () -> prunedStream(dimensions);
    }

    private static Stream<List<Object>> prunedStream(Dimensions d) {

        return d.os.stream()

                .flatMap(os -> d.browser.stream()

                        .filter(browser -> {
                            if (os.equals(LINUX)) {
                                return LINUX_BROWSERS.contains(browser);
                            }
                            return WINDOWS_BROWSERS.contains(browser);
                        })

                        .flatMap(browser -> d.network.stream()

                                .filter(network -> {
                                    if (os.equals(LINUX)) {
                                        return LINUX_NETWORKS.contains(network);
                                    }
                                    return true;
                                })

                                .flatMap(network -> d.database.stream()

                                        .filter(database -> {
                                            if (os.equals(LINUX)) {
                                                return LINUX_DATABASES.contains(database);
                                            }
                                            return WINDOWS_DATABASES.contains(database);
                                        })

                                        .flatMap(database -> d.region.stream()

                                                .filter(region -> {
                                                    if (os.equals(LINUX)) {
                                                        return LINUX_REGIONS.contains(region);
                                                    }
                                                    return WINDOWS_REGIONS.contains(region);
                                                })

                                                .map(region -> List.<Object>of(
                                                        os,
                                                        browser,
                                                        network,
                                                        database,
                                                        region
                                                ))
                                        )
                                )
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Benchmark methods
    // -------------------------------------------------------------------------

    @Benchmark
    public long pairwiseWhenThen(Blackhole blackhole) {
        return consume(pairwise.get(), blackhole);
    }

    @Benchmark
    public long engineLambda(Blackhole blackhole) {
        return consume(engineLambda.get(), blackhole);
    }

    @Benchmark
    public long prunedJavaStream(Blackhole blackhole) {
        return consume(prunedStream.get(), blackhole);
    }

    // -------------------------------------------------------------------------
    // Common consumer
    // -------------------------------------------------------------------------

    /**
     * Traverses every tuple and makes every tuple observable to JMH.
     *
     * The Blackhole consumption prevents dead-code elimination of the generated
     * tuples. Returning the count also provides a second observable result.
     */
    private static long consume(
            Stream<List<Object>> tuples,
            Blackhole blackhole) {

        long count = 0;

        try (tuples) {
            var iterator = tuples.iterator();

            while (iterator.hasNext()) {
                List<Object> tuple = iterator.next();

                blackhole.consume(tuple);

                count++;
            }
        }

        return count;
    }

    // -------------------------------------------------------------------------
    // Correctness verification (outside measured benchmark execution)
    // -------------------------------------------------------------------------

    private void verifyCorrectness() {

        Set<List<Object>> expected = collect(pairwise.get());

        Set<List<Object>> lambdaResult = collect(engineLambda.get());
        Set<List<Object>> streamResult = collect(prunedStream.get());

        if (!expected.equals(lambdaResult)) {
            throw new AssertionError(
                    "Lambda result differs from pairwise result for size " + size
            );
        }

        if (!expected.equals(streamResult)) {
            throw new AssertionError(
                    "Pruned Stream result differs from pairwise result for size " + size
            );
        }

        int expectedMatches = expectedMatchesFor(size);

        if (expected.size() != expectedMatches) {
            throw new AssertionError(
                    "Unexpected match count for size " + size
                            + ". Expected " + expectedMatches
                            + ", got " + expected.size()
            );
        }
    }

    private static Set<List<Object>> collect(
            Stream<List<Object>> tuples) {

        try (tuples) {
            return tuples
                    .map(List::copyOf)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    private static int expectedMatchesFor(int size) {

        for (int i = 0; i < SIZES.length; i++) {
            if (SIZES[i] == size) {
                return EXPECTED_MATCHES[i];
            }
        }

        throw new IllegalArgumentException("Unsupported size: " + size);
    }

    // -------------------------------------------------------------------------
    // Input dimensions
    // -------------------------------------------------------------------------

    private static Dimensions createDimensions(int size) {

        List<String> os = List.of(LINUX, WINDOWS);

        List<String> browser =
                createDimension(BROWSER_BASE, size, "OtherBrowser-");

        List<String> network =
                createDimension(NETWORK_BASE, size, "OtherNetwork-");

        List<String> database =
                createDimension(DATABASE_BASE, size, "OtherDatabase-");

        List<String> region =
                createDimension(REGION_BASE, size, "OtherRegion-");

        return new Dimensions(os, browser, network, database, region);
    }

    private static List<String> createDimension(
            String[] base,
            int size,
            String prefix) {

        List<String> values = new ArrayList<>(size);
        values.addAll(Arrays.asList(base));

        for (int i = values.size(); i < size; i++) {
            values.add(prefix + i);
        }

        return Collections.unmodifiableList(values);
    }

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------

    private static final class Dimensions {

        private final List<String> os;
        private final List<String> browser;
        private final List<String> network;
        private final List<String> database;
        private final List<String> region;

        private Dimensions(
                List<String> os,
                List<String> browser,
                List<String> network,
                List<String> database,
                List<String> region) {

            this.os = os;
            this.browser = browser;
            this.network = network;
            this.database = database;
            this.region = region;
        }
    }
}

/*

sample command -
mvn test-compile exec:exec \
  -Dexec.executable="java" \
  -Dexec.classpathScope="test" \
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main ConditionalProductSpeedBenchmarkJMH -p size=1000 -wi 5 -w 1s -i 10 -r 1s -f 3 -tu ms -bm avgt -prof gc"

# JMH version: 1.37
# VM version: JDK 27.0.1.0.3, OpenJDK 64-Bit Server VM, 27.0.1.0.3
# VM invoker: /opt/homebrew/Cellar/openjdk/27.0.1.0.3/libexec/openjdk.jdk/Contents/Home/bin/java
# VM options: <none>

Benchmark                                                                (size)  Mode  Cnt       Score    Error   Units
ConditionalProductSpeedBenchmarkJMH.engineLambda                             50  avgt   30       0.010 ±  0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate               50  avgt   30    1510.936 ± 41.046  MB/sec
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate.norm          50  avgt   30   15472.062 ±  0.002    B/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.count                    50  avgt   30     597.000           counts
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.time                     50  avgt   30     109.000               ms
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen                         50  avgt   30       0.010 ±  0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate           50  avgt   30    1482.769 ± 46.137  MB/sec
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate.norm      50  avgt   30   15472.064 ±  0.002    B/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.count                50  avgt   30     603.000           counts
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.time                 50  avgt   30     107.000               ms
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream                         50  avgt   30       0.057 ±  0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate           50  avgt   30    1774.250 ± 14.355  MB/sec
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate.norm      50  avgt   30  105784.363 ±  0.003    B/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.count                50  avgt   30     626.000           counts
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.time                 50  avgt   30     118.000               ms

Benchmark                                                                (size)  Mode  Cnt       Score    Error   Units
ConditionalProductSpeedBenchmarkJMH.engineLambda                            100  avgt   30       0.019 ±  0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate              100  avgt   30    1535.093 ± 18.185  MB/sec
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate.norm         100  avgt   30   29872.118 ±  0.001    B/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.count                   100  avgt   30     627.000           counts
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.time                    100  avgt   30     107.000               ms
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen                        100  avgt   30       0.019 ±  0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate          100  avgt   30    1519.605 ± 29.794  MB/sec
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate.norm     100  avgt   30   29872.119 ±  0.002    B/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.count               100  avgt   30     655.000           counts
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.time                100  avgt   30     109.000               ms
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream                        100  avgt   30       0.206 ±  0.004   ms/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate          100  avgt   30     950.450 ± 16.589  MB/sec
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate.norm     100  avgt   30  205049.312 ±  0.024    B/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.count               100  avgt   30     743.000           counts
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.time                100  avgt   30     117.000               ms

Benchmark                                                                (size)  Mode  Cnt       Score     Error   Units
ConditionalProductSpeedBenchmarkJMH.engineLambda                            200  avgt   30       0.044 ±   0.005   ms/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate              200  avgt   30    1287.682 ± 123.719  MB/sec
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate.norm         200  avgt   30   58656.283 ±   7.706    B/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.count                   200  avgt   30     908.000            counts
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.time                    200  avgt   30     148.000                ms
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen                        200  avgt   30       0.037 ±   0.001   ms/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate          200  avgt   30    1499.825 ±  52.298  MB/sec
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate.norm     200  avgt   30   58664.239 ±   7.688    B/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.count               200  avgt   30     883.000            counts
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.time                200  avgt   30     140.000                ms
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream                        200  avgt   30       0.750 ±   0.016   ms/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate          200  avgt   30     513.718 ±  10.780  MB/sec
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate.norm     200  avgt   30  403564.778 ±   0.105    B/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.count               200  avgt   30     577.000            counts
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.time                200  avgt   30     101.000                ms

Benchmark                                                                (size)  Mode  Cnt        Score    Error   Units
ConditionalProductSpeedBenchmarkJMH.engineLambda                           1000  avgt   30        0.229 ±  0.016   ms/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate             1000  avgt   30     1217.621 ± 91.394  MB/sec
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate.norm        1000  avgt   30   289073.459 ±  0.102    B/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.count                  1000  avgt   30      800.000           counts
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.time                   1000  avgt   30      137.000               ms
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen                       1000  avgt   30        0.180 ±  0.005   ms/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate         1000  avgt   30     1535.226 ± 38.610  MB/sec
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate.norm    1000  avgt   30   289073.146 ±  0.029    B/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.count              1000  avgt   30      753.000           counts
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.time               1000  avgt   30      173.000               ms
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream                       1000  avgt   30       18.626 ±  1.741   ms/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate         1000  avgt   30      103.410 ±  9.212  MB/sec
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate.norm    1000  avgt   30  1983709.628 ± 10.906    B/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.count              1000  avgt   30      263.000           counts
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.time               1000  avgt   30       56.000               ms

Benchmark                                                                (size)  Mode  Cnt        Score    Error   Units
ConditionalProductSpeedBenchmarkJMH.engineLambda                           5000  avgt   30        0.848 ±   0.033   ms/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate             5000  avgt   30     1624.351 ±  53.944  MB/sec
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.alloc.rate.norm        5000  avgt   30  1441077.405 ±   0.215    B/op
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.count                  5000  avgt   30      367.000            counts
ConditionalProductSpeedBenchmarkJMH.engineLambda:gc.time                   5000  avgt   30      111.000                ms
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen                       5000  avgt   30        0.835 ±   0.022   ms/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate         5000  avgt   30     1647.185 ±  39.769  MB/sec
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.alloc.rate.norm    5000  avgt   30  1441077.322 ±   0.133    B/op
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.count              5000  avgt   30      378.000            counts
ConditionalProductSpeedBenchmarkJMH.pairwiseWhenThen:gc.time               5000  avgt   30      136.000                ms
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream                       5000  avgt   30      489.458 ±  35.156   ms/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate         5000  avgt   30       19.444 ±   1.501  MB/sec
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.alloc.rate.norm    5000  avgt   30  9860771.911 ± 341.441    B/op
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.count              5000  avgt   30       46.000            counts
ConditionalProductSpeedBenchmarkJMH.prunedJavaStream:gc.time               5000  avgt   30       22.000                ms

 */