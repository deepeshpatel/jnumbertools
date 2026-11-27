
package io.github.deepeshpatel.jnumbertools.benchmarks.product.jmh;

import io.github.deepeshpatel.jnumbertools.entrypoint.Product;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JMH comparison of conditional Cartesian product implementations
 * across different achievable validity ratios.
 *
 * Each benchmark invocation traverses the complete lexOrder() result
 * and consumes every generated tuple.
 *
 * Builder construction and rule registration happen in trial setup.
 * Correctness verification is performed outside measured execution.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(3)
@State(Scope.Thread)
public class ConditionalProductSpeedBenchmark {

    @Param({"10", "20", "30", "100"})
    public int size;

    @Param({"0.02", "0.05", "0.10", "0.25", "0.50", "0.75"})
    public double targetValidityRatio;

    private static final String LINUX = "Linux";
    private static final String WINDOWS = "Windows";

    private Dimensions dimensions;

    private Supplier<Stream<List<Object>>> compiled;
    private Supplier<Stream<List<Object>>> lambda;
    private Supplier<Stream<List<Object>>> prunedStream;

    @Setup(Level.Trial)
    public void setup() {

        dimensions = createDimensions(size, targetValidityRatio);

        buildCompiled();
        buildLambda();
        buildPrunedStream();

        verifyCorrectness();
    }

    // -------------------------------------------------------------------------
    // 1. Compiled pairwise conditional product
    // -------------------------------------------------------------------------

    private void buildCompiled() {

        var d = dimensions;

        var builder = Product.conditional(
                        d.os, d.browser, d.network, d.database, d.region)

                .when(d.os).is(LINUX)
                .then(d.browser).isOneOf(d.browserAccepted)

                .when(d.os).is(LINUX)
                .then(d.network).isOneOf(d.networkAccepted)

                .when(d.os).is(LINUX)
                .then(d.database).isOneOf(d.databaseAccepted)

                .when(d.os).is(LINUX)
                .then(d.region).isOneOf(d.regionAccepted)

                .when(d.os).is(WINDOWS)
                .then(d.browser).isOneOf(d.browserAccepted)

                .when(d.os).is(WINDOWS)
                .then(d.database).isOneOf(d.databaseAccepted)

                .when(d.os).is(WINDOWS)
                .then(d.region).isOneOf(d.regionAccepted);

        compiled = () -> builder.lexOrder().stream();
    }

    // -------------------------------------------------------------------------
    // 2. Lambda conditional product
    // -------------------------------------------------------------------------

    private void buildLambda() {

        var d = dimensions;

        var builder = Product.conditional(
                        d.os, d.browser, d.network, d.database, d.region)
                .withLambda(
                        d.os, d.browser, d.network, d.database, d.region)

                .when(d.os, d.browser).matches((os, browser) ->
                        d.browserAcceptedSet.contains(browser))

                .when(d.os, d.network).matches((os, network) ->
                        !LINUX.equals(os)
                                || d.networkAcceptedSet.contains(network))

                .when(d.os, d.database).matches((os, database) ->
                        d.databaseAcceptedSet.contains(database))

                .when(d.os, d.region).matches((os, region) ->
                        d.regionAcceptedSet.contains(region));

        lambda = () -> builder.lexOrder().stream();
    }

    // -------------------------------------------------------------------------
    // 3. Incrementally pruned Java Stream baseline
    // -------------------------------------------------------------------------

    private void buildPrunedStream() {
        prunedStream = () -> prunedStream(dimensions);
    }

    private static Stream<List<Object>> prunedStream(Dimensions d) {

        return d.os.stream()
                .flatMap(os -> d.browser.stream()
                        .filter(d.browserAcceptedSet::contains)
                        .flatMap(browser -> d.network.stream()
                                .filter(network -> !LINUX.equals(os)
                                        || d.networkAcceptedSet.contains(network))
                                .flatMap(network -> d.database.stream()
                                        .filter(d.databaseAcceptedSet::contains)
                                        .flatMap(database -> d.region.stream()
                                                .filter(d.regionAcceptedSet::contains)
                                                .map(region -> List.<Object>of(
                                                        os, browser, network,
                                                        database, region
                                                ))
                                        )
                                )
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Benchmark methods: full lexOrder traversal
    // -------------------------------------------------------------------------

    @Benchmark
    public long compiled(Blackhole blackhole) {
        return consume(compiled.get(), blackhole);
    }

    @Benchmark
    public long lambda(Blackhole blackhole) {
        return consume(lambda.get(), blackhole);
    }

    @Benchmark
    public long prunedJavaStream(Blackhole blackhole) {
        return consume(prunedStream.get(), blackhole);
    }

    private static long consume(
            Stream<List<Object>> tuples,
            Blackhole blackhole) {

        long count = 0;

        try (tuples) {
            var iterator = tuples.iterator();

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

        WalkResult expected = walk(compiled.get());

        verifyResult("Lambda", walk(lambda.get()), expected);
        verifyResult("Pruned Stream", walk(prunedStream.get()), expected);

        long expectedCount = expectedMatches(dimensions).longValueExact();

        if (expected.count != expectedCount) {
            throw new AssertionError(
                    "Unexpected match count. Expected " + expectedCount
                            + ", got " + expected.count
            );
        }
    }

    private static void verifyResult(
            String implementation,
            WalkResult actual,
            WalkResult expected) {

        if (actual.count != expected.count || actual.hash != expected.hash) {
            throw new AssertionError(
                    implementation + " produced a different lexOrder() result. "
                            + "Expected count/hash: " + expected
                            + ", actual: " + actual
            );
        }
    }

    private static WalkResult walk(Stream<List<Object>> tuples) {

        long count = 0;
        long hash = 0;

        try (tuples) {
            var iterator = tuples.iterator();

            while (iterator.hasNext()) {
                List<Object> tuple = iterator.next();
                count++;
                hash = 31 * hash + tuple.hashCode();
            }
        }

        return new WalkResult(count, hash);
    }

    // -------------------------------------------------------------------------
    // Dimension creation
    // -------------------------------------------------------------------------

    private static Dimensions createDimensions(
            int size,
            double targetRatio) {

        int commonCount = closestCommonCount(size, targetRatio);

        List<String> os = List.of(LINUX, WINDOWS);

        Dimension browser = createDimension("Browser", size, commonCount);
        Dimension network = createDimension("Network", size, commonCount);
        Dimension database = createDimension("Database", size, commonCount);
        Dimension region = createDimension("Region", size, commonCount);

        return new Dimensions(
                os,
                browser.values, network.values,
                database.values, region.values,
                browser.accepted, network.accepted,
                database.accepted, region.accepted,
                browser.acceptedSet, network.acceptedSet,
                database.acceptedSet, region.acceptedSet
        );
    }

    private static int closestCommonCount(int size, double targetRatio) {

        int bestCount = 1;
        double bestDifference = Double.MAX_VALUE;

        for (int common = 1; common <= size; common++) {

            double difference = Math.abs(
                    validityRatio(size, common) - targetRatio
            );

            if (difference < bestDifference) {
                bestDifference = difference;
                bestCount = common;
            }
        }

        return bestCount;
    }

    private static double validityRatio(int size, int commonCount) {

        double n = size;
        double common = commonCount;

        return (Math.pow(common, 4) + Math.pow(common, 3) * n)
                / (2.0 * Math.pow(n, 4));
    }

    private static Dimension createDimension(
            String prefix,
            int size,
            int commonCount) {

        List<String> values = new ArrayList<>(size);
        List<String> accepted = new ArrayList<>(commonCount);
        Set<String> acceptedSet = new HashSet<>(commonCount);

        int commonIndex = 0;
        int otherIndex = 0;

        // Interleave accepted and rejected values.
        for (int i = 0; i < size; i++) {

            boolean useCommon = (i % 2 == 0 && commonIndex < commonCount)
                    || otherIndex >= size - commonCount;

            if (useCommon) {
                String value = prefix + "-Common-" + commonIndex++;
                values.add(value);
                accepted.add(value);
                acceptedSet.add(value);
            } else {
                values.add(prefix + "-Other-" + otherIndex++);
            }
        }

        return new Dimension(
                List.copyOf(values),
                List.copyOf(accepted),
                acceptedSet
        );
    }

    // -------------------------------------------------------------------------
    // Expected result calculations
    // -------------------------------------------------------------------------

    private static java.math.BigInteger expectedMatches(Dimensions d) {

        java.math.BigInteger common =
                java.math.BigInteger.valueOf(d.browserAccepted.size());

        java.math.BigInteger size =
                java.math.BigInteger.valueOf(d.browser.size());

        // Linux: common^4
        // Windows: common^3 * size (network is unrestricted)
        return common.pow(4).add(common.pow(3).multiply(size));
    }

    // -------------------------------------------------------------------------
    // Data holders
    // -------------------------------------------------------------------------

    private record Dimension(
            List<String> values,
            List<String> accepted,
            Set<String> acceptedSet) {
    }

    private record Dimensions(
            List<String> os,
            List<String> browser,
            List<String> network,
            List<String> database,
            List<String> region,
            List<String> browserAccepted,
            List<String> networkAccepted,
            List<String> databaseAccepted,
            List<String> regionAccepted,
            Set<String> browserAcceptedSet,
            Set<String> networkAcceptedSet,
            Set<String> databaseAcceptedSet,
            Set<String> regionAcceptedSet) {
    }

    private record WalkResult(long count, long hash) {
    }
}
