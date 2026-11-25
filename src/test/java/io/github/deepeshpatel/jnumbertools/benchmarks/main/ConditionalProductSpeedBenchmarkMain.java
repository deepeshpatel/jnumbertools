
package io.github.deepeshpatel.jnumbertools.benchmarks.main;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Quick screening comparison of conditional Cartesian product implementations:
 *
 * 1. Pairwise when-then conditional product
 * 2. Compound conditional product built from Condition.is(...)
 * 3. Lambda-scoped conditional product
 * 4. Incrementally pruned Java Stream baseline
 *
 * <p>Each benchmark method builds its product once per size. Only generation
 * and traversal of the generated tuples are timed.</p>
 *
 * <p>No implementation materializes its results into a Set or another
 * collection. Every generated tuple is consumed by the checksum calculation.
 * This avoids Set insertion, duplicate detection, and collection allocation
 * while ensuring that the complete result is traversed.</p>
 *
 * <p>The checksum is order-independent, so implementations may generate
 * equivalent tuples in different orders.</p>
 */
public class ConditionalProductSpeedBenchmarkMain {

    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASUREMENT_ROUNDS = 9;

    private static final int[] SIZES = {
            100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000
    };

    private static final int[] EXPECTED_MATCHES = {
            616, 1216, 1816, 2416, 3016,
            6016, 12016, 18016, 24016, 30016
    };

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

    /*
     * Keeps the checksum observable without including the volatile write
     * in the measured generation time.
     */
    private static volatile long blackhole;

    public static void main(String[] args) {

        BenchmarkMethod[] methods = createMethods();

        printBenchmarkConditions();
        printProblemStatement();
        printHeader(methods);
        printSeparator(methods.length);

        for (int i = 0; i < SIZES.length; i++) {
            runBenchmark(methods, SIZES[i], EXPECTED_MATCHES[i]);
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Method registry
    // -------------------------------------------------------------------------

    private static BenchmarkMethod[] createMethods() {
        return new BenchmarkMethod[]{
                new BenchmarkMethod(
                        "Pairwise when-then",
                        ConditionalProductSpeedBenchmarkMain::whenThen),

                new BenchmarkMethod(
                        "Compound",
                        ConditionalProductSpeedBenchmarkMain::buildCompound),

                new BenchmarkMethod(
                        "engineLambda",
                        ConditionalProductSpeedBenchmarkMain::buildEngineLambda),

                new BenchmarkMethod(
                        "Pruned Stream",
                        ConditionalProductSpeedBenchmarkMain::buildPrunedStream)
        };
    }

    // -------------------------------------------------------------------------
    // Benchmark runner
    // -------------------------------------------------------------------------

    private static void runBenchmark(
            BenchmarkMethod[] methods,
            int size,
            int expectedMatches) {

        Dimensions dimensions = createDimensions(size);

        /*
         * Build each implementation once, outside the timed sections.
         */
        List<Supplier<Result>> runnables = new ArrayList<>(methods.length);

        for (BenchmarkMethod method : methods) {
            runnables.add(method.build.apply(dimensions));
        }

        /*
         * Correctness verification is outside the timed sections.
         *
         * The first implementation establishes the expected result.
         * Every other implementation must produce the same count and
         * checksum aggregates.
         */
        Result expected = runnables.getFirst().get();
        consume(expected);

        if (expected.count() != expectedMatches) {
            throw new AssertionError(
                    "Unexpected match count for size " + size
                            + ": expected " + expectedMatches
                            + ", got " + expected.count());
        }

        for (int i = 1; i < methods.length; i++) {
            Result actual = runnables.get(i).get();
            consume(actual);

            if (!expected.equals(actual)) {
                throw new AssertionError(
                        "Incorrect result from " + methods[i].name
                                + " for size " + size
                                + ". Expected " + expected
                                + ", got " + actual);
            }
        }

        /*
         * Warmup.
         * Rotate execution order to reduce systematic ordering bias.
         */
        for (int round = 0; round < WARMUP_ROUNDS; round++) {
            executeRound(runnables, round, expected);
        }

        /*
         * Measurement.
         */
        long[][] measurements =
                new long[methods.length][MEASUREMENT_ROUNDS];

        for (int round = 0; round < MEASUREMENT_ROUNDS; round++) {

            int offset = round % methods.length;

            for (int j = 0; j < methods.length; j++) {

                int methodIndex = (j + offset) % methods.length;

                long start = System.nanoTime();

                Result result = runnables.get(methodIndex).get();

                long elapsed = System.nanoTime() - start;

                measurements[methodIndex][round] = elapsed;

                /*
                 * Consume and validate outside the measured interval.
                 */
                consume(result);

                if (!expected.equals(result)) {
                    throw new AssertionError(
                            methods[methodIndex].name
                                    + " produced an unexpected result"
                                    + " for size " + size);
                }
            }
        }

        double[] medianMillis = new double[methods.length];

        for (int i = 0; i < methods.length; i++) {
            medianMillis[i] =
                    median(measurements[i]) / 1_000_000.0;
        }

        printDataRow(methods, size, expectedMatches, medianMillis);
    }

    private static void executeRound(
            List<Supplier<Result>> runnables,
            int round,
            Result expected) {

        int offset = round % runnables.size();

        for (int j = 0; j < runnables.size(); j++) {

            int index = (j + offset) % runnables.size();

            Result result = runnables.get(index).get();

            consume(result);

            if (!expected.equals(result)) {
                throw new AssertionError(
                        "Unexpected result during warmup");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Checksum
    // -------------------------------------------------------------------------

    /**
     * Traverses every tuple and calculates an order-independent checksum.
     *
     * <p>We maintain:</p>
     * <ul>
     *     <li>Number of tuples</li>
     *     <li>Sum of tuple hash codes</li>
     *     <li>Sum of squared tuple hash codes</li>
     *     <li>XOR of mixed tuple hash codes</li>
     * </ul>
     *
     * <p>This avoids storing the generated tuples while providing a
     * lightweight consistency check across implementations.</p>
     */
    private static Result checksum(Stream<List<Object>> tuples) {

        long count = 0;
        long hashSum = 0;
        long hashSquareSum = 0;
        long hashXor = 0;

        var iterator = tuples.iterator();

        while (iterator.hasNext()) {

            List<Object> tuple = iterator.next();

            int tupleHash = tuple.hashCode();

            count++;
            hashSum += tupleHash;
            hashSquareSum += (long) tupleHash * tupleHash;
            hashXor ^= mix(tupleHash);
        }

        return new Result(
                count,
                hashSum,
                hashSquareSum,
                hashXor);
    }

    /**
     * Mixes a tuple hash code to make the XOR aggregate more discriminating.
     */
    private static long mix(int hash) {

        long value = hash;

        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;

        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;

        value ^= value >>> 33;

        return value;
    }

    /**
     * Makes the checksum observable outside the timed section.
     */
    private static void consume(Result result) {

        blackhole ^= result.count()
                + result.hashSum()
                + Long.rotateLeft(result.hashSquareSum(), 17)
                + result.hashXor();
    }

    private record Result(
            long count,
            long hashSum,
            long hashSquareSum,
            long hashXor) {
    }

    // -------------------------------------------------------------------------
    // 1. Pairwise when-then conditional product
    // -------------------------------------------------------------------------

    private static Supplier<Result> whenThen(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .conditionalProductOf(
                        d.os, d.browser, d.network, d.database, d.region)

                // Linux rules
                .when(d.os).is(LINUX)
                .then(d.browser).isOneOf(LINUX_BROWSERS)

                .when(d.os).is(LINUX)
                .then(d.network).isOneOf(LINUX_NETWORKS)

                .when(d.os).is(LINUX)
                .then(d.database).isOneOf(LINUX_DATABASES)

                .when(d.os).is(LINUX)
                .then(d.region).isOneOf(LINUX_REGIONS)

                // Windows rules
                .when(d.os).is(WINDOWS)
                .then(d.browser).isOneOf(WINDOWS_BROWSERS)

                .when(d.os).is(WINDOWS)
                .then(d.database).isOneOf(WINDOWS_DATABASES)

                .when(d.os).is(WINDOWS)
                .then(d.region).isOneOf(WINDOWS_REGIONS);

        return () -> checksum(builder.anyOrder().stream());
    }

    // -------------------------------------------------------------------------
    // 2. Compound conditional product
    // -------------------------------------------------------------------------

    private static Supplier<Result> buildCompound(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .compoundProductOf(
                        d.os, d.browser, d.network, d.database, d.region)

                // Linux rules
                .when(Condition.condition(d.os).is(LINUX))
                .then(d.browser).isOneOf(LINUX_BROWSERS)

                .when(Condition.condition(d.os).is(LINUX))
                .then(d.network).isOneOf(LINUX_NETWORKS)

                .when(Condition.condition(d.os).is(LINUX))
                .then(d.database).isOneOf(LINUX_DATABASES)

                .when(Condition.condition(d.os).is(LINUX))
                .then(d.region).isOneOf(LINUX_REGIONS)

                // Windows rules
                .when(Condition.condition(d.os).is(WINDOWS))
                .then(d.browser).isOneOf(WINDOWS_BROWSERS)

                .when(Condition.condition(d.os).is(WINDOWS))
                .then(d.database).isOneOf(WINDOWS_DATABASES)

                .when(Condition.condition(d.os).is(WINDOWS))
                .then(d.region).isOneOf(WINDOWS_REGIONS);

        return () -> checksum(builder.generate().stream());
    }

    // -------------------------------------------------------------------------
    // 3. Lambda Engine
    // -------------------------------------------------------------------------

    private static Supplier<Result> buildEngineLambda(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .withLambda(
                        d.os, d.browser, d.network, d.database, d.region)

                 .when((os, browser, network, database, region) ->
                        (LINUX.equals(os) ? LINUX_BROWSERS.contains(browser)
                                : WINDOWS.equals(os) ? WINDOWS_BROWSERS.contains(browser) : true)
                        && (!LINUX.equals(os) || LINUX_NETWORKS.contains(network))
                        && (LINUX.equals(os) ? LINUX_DATABASES.contains(database)
                                : WINDOWS.equals(os) ? WINDOWS_DATABASES.contains(database) : true)
                        && (LINUX.equals(os) ? LINUX_REGIONS.contains(region)
                                : WINDOWS.equals(os) ? WINDOWS_REGIONS.contains(region) : true));

        return () -> checksum(builder.generate().stream());
    }

    // -------------------------------------------------------------------------
    // 4. Pruned Java Stream baseline
    // -------------------------------------------------------------------------

    private static Supplier<Result> buildPrunedStream(Dimensions d) {

        return () -> checksum(prunedStream(d));
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
    // Helpers
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

    private static long median(long[] values) {

        long[] sorted = values.clone();

        Arrays.sort(sorted);

        return sorted[sorted.length / 2];
    }

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

    private static final class BenchmarkMethod {

        private final String name;
        private final Function<Dimensions, Supplier<Result>> build;

        private BenchmarkMethod(
                String name,
                Function<Dimensions, Supplier<Result>> build) {

            this.name = name;
            this.build = build;
        }
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private static void printHeader(BenchmarkMethod[] methods) {

        StringBuilder fmt = new StringBuilder("%-8s %-12s");

        for (int i = 0; i < methods.length; i++) {
            fmt.append(" %-17s");
        }

        fmt.append("%n");

        Object[] args = new Object[2 + methods.length];

        args[0] = "Size";
        args[1] = "Matches";

        for (int i = 0; i < methods.length; i++) {
            args[2 + i] = methods[i].name + " ms";
        }

        System.out.printf(fmt.toString(), args);
    }

    private static void printSeparator(int methodCount) {

        int width = 8 + 1 + 12 + methodCount * (1 + 17);

        System.out.println("-".repeat(width));
    }

    private static void printDataRow(
            BenchmarkMethod[] methods,
            int size,
            int expectedMatches,
            double[] medianMillis) {

        StringBuilder fmt = new StringBuilder("%-8d %-12d");

        for (int i = 0; i < methods.length; i++) {
            fmt.append(" %-17.3f");
        }

        fmt.append("%n");

        Object[] args = new Object[2 + methods.length];

        args[0] = size;
        args[1] = expectedMatches;

        for (int i = 0; i < methods.length; i++) {
            args[2 + i] = medianMillis[i];
        }

        System.out.printf(fmt.toString(), args);
    }

    private static void printBenchmarkConditions() {

        System.out.println();
        System.out.println("Conditional Product Benchmark");
        System.out.println("==============================================================");
        System.out.println("Warmup rounds:      " + WARMUP_ROUNDS);
        System.out.println("Measurement rounds: " + MEASUREMENT_ROUNDS);
        System.out.println();
    }

    private static void printProblemStatement() {

        System.out.println("""
            Problem Statement
            ==============================================================
            Find all valid combinations of:

                Operating System × Browser × Network × Database × Region

            subject to the following rules:

            1. If Operating System is Linux:
               - Browser must be Firefox or Safari.
               - Network must be 4G or WiFi.
               - Database must be PostgreSQL or MySQL.
               - Region must be ap-south or eu-west.

            2. If Operating System is Windows:
               - Browser must be Firefox, Safari or Chrome.
               - Database must be PostgreSQL.
               - Region must be us-east or eu-west.

            3. Network has no restriction when Operating System is Windows.

            Objective:
            Generate all valid combinations and compare the performance
            of different conditional Cartesian product implementations.
            ==============================================================
            """);
    }
}


/*
Size     Matches      Pairwise when-then ms Compound ms       engineLambda ms   Pruned Stream ms
---------------------------------------------------------------------------------------------
100      616          0.163             0.163             0.161             0.594
200      1216         0.061             0.051             0.068             0.799
300      1816         0.058             0.056             0.057             1.626
400      2416         0.079             0.078             0.079             2.708
500      3016         0.105             0.094             0.094             4.343
1000     6016         0.214             0.208             0.203             15.071
2000     12016        0.359             0.360             0.376             57.271
3000     18016        0.571             0.531             0.524             126.792
4000     24016        0.710             0.715             0.723             221.740
5000     30016        0.878             0.865             0.865             349.215
 */