package io.github.deepeshpatel.jnumbertools.becchmark.quick;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * End-to-end comparison of conditional Cartesian product implementations:
 *
 * 1. Compound conditional product built from Condition.is(...)
 * 2. Pruned Java Stream implementation
 * 3. Interpreted (pruned-DFS) lambda-scoped conditional product
 *
 * <h2>What is, and is not, inside the measured time</h2>
 * <p>Each {@link BenchmarkMethod} is split into a {@code build} step and a {@code run} step.
 * {@code build} runs exactly once per size, outside every timed round: it constructs the builder and
 * registers every rule, which for the lambda-scoped method means compiling its {@code matches(...)}
 * predicates there too. Only {@code run} — generation and materialization into the result {@code Set} —
 * is ever timed, in both the warmup and the measurement rounds. This keeps one-time compilation cost
 * out of the per-call numbers for every method alike, not only the lambda-scoped one, so the columns
 * compare the same thing: the cost of producing the tuples from an already-built product.</p>
 *
 * <p>The column order in the printed table follows the order of the
 * {@code BenchmarkMethod[]} array built by {@link #createMethods()}, so
 * implementations can be reordered or added there without touching the
 * header, separator, or data-row formatting.</p>
 */
public class ConditionalProductBenchmark {

    private static final int WARMUP_ROUNDS = 5;
    private static final int MEASUREMENT_ROUNDS = 9;

    private static final int[] SIZES = {100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000};

    private static final int[] EXPECTED_MATCHES = {
            616, 1216, 1816, 2416, 3016, 6016, 12016, 18016, 24016, 30016
    };

    private static final String LINUX = "Linux";
    private static final String WINDOWS = "Windows";

    private static final List<String> LINUX_BROWSERS = List.of("Firefox", "Safari");
    private static final List<String> LINUX_NETWORKS = List.of("4G", "WiFi");
    private static final List<String> LINUX_DATABASES = List.of("PostgreSQL", "MySQL");
    private static final List<String> LINUX_REGIONS = List.of("ap-south", "eu-west");

    private static final List<String> WINDOWS_BROWSERS = List.of("Firefox", "Safari", "Chrome");
    private static final List<String> WINDOWS_DATABASES = List.of("PostgreSQL");
    private static final List<String> WINDOWS_REGIONS = List.of("us-east", "eu-west");

    private static final String[] BROWSER_BASE = {"Firefox", "Safari", "Chrome", "Internet Explorer"};
    private static final String[] NETWORK_BASE = {"2G", "3G", "4G", "WiFi"};
    private static final String[] DATABASE_BASE = {"PostgreSQL", "MySQL", "Oracle"};

    private static final String[] REGION_BASE = {"ap-south", "eu-west", "us-east"};

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
    // Method registry — single source of truth for column order
    // -------------------------------------------------------------------------

    /**
     * Builds the ordered list of benchmarked implementations. Reorder this
     * array to reorder the table; add an entry to add a column.
     *
     * <p>Each entry's {@code build} function does all one-time work (builder construction, rule
     * registration, predicate compilation) and returns a {@link Supplier} that only generates and
     * materializes the result; {@code build} itself is never timed.</p>
     */
    private static BenchmarkMethod[] createMethods() {
        return new BenchmarkMethod[] {
                new BenchmarkMethod("Pairwise when-then", ConditionalProductBenchmark::whenThen),
                new BenchmarkMethod("Compound", ConditionalProductBenchmark::buildCompound),
                new BenchmarkMethod("engineLambda", ConditionalProductBenchmark::buildEngineLambda),
                new BenchmarkMethod("Pruned Stream", ConditionalProductBenchmark::buildPrunedStream),
        };
    }

    // -------------------------------------------------------------------------
    // Benchmark runner
    // -------------------------------------------------------------------------

    private static void runBenchmark(
            BenchmarkMethod[] methods, int size, int expectedMatches) {
        Dimensions dimensions = createDimensions(size);

        /*
         * Build once per method, outside every timed section. For a method whose rules are lambda
         * predicates, this is where compilation happens — it never runs again below.
         */
        Supplier<Set<List<Object>>>[] runnables = new Supplier[methods.length];
        for (int i = 0; i < methods.length; i++) {
            runnables[i] = methods[i].build.apply(dimensions);
        }

        /*
         * Correctness verification is deliberately outside the timed section.
         */
        Set<List<Object>> expected = null;

        for (int i = 0; i < methods.length; i++) {
            Set<List<Object>> result = runnables[i].get();

            if (expected == null) {
                expected = result;
            } else if (!expected.equals(result)) {
                throw new AssertionError("Incorrect result from " + methods[i].name + " for size " + size);
            }
        }

        if (expected.size() != expectedMatches) {
            throw new AssertionError("Unexpected match count for size " + size + ": expected " + expectedMatches + ", got " + expected.size());
        }

        /*
         * Warmup. Rotate execution order to reduce systematic ordering bias.
         */
        for (int round = 0; round < WARMUP_ROUNDS; round++) {
            executeRound(runnables, round);
        }

        /*
         * Measurement.
         */
        long[][] measurements = new long[methods.length][MEASUREMENT_ROUNDS];

        for (int round = 0; round < MEASUREMENT_ROUNDS; round++) {

            int offset = round % methods.length;

            for (int j = 0; j < methods.length; j++) {
                int methodIndex = (j + offset) % methods.length;
                long start = System.nanoTime();
                Set<List<Object>> result = runnables[methodIndex].get();
                long elapsed = System.nanoTime() - start;
                measurements[methodIndex][round] = elapsed;

                if (result.size() != expectedMatches) {
                    throw new AssertionError(methods[methodIndex].name + " produced an unexpected result count");
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

    private static void executeRound(Supplier<Set<List<Object>>>[] runnables, int round ) {
        int offset = round % runnables.length;
        for (int j = 0; j < runnables.length; j++) {
            int index = (j + offset) % runnables.length;
            runnables[index].get();
        }
    }

    // -------------------------------------------------------------------------
    // 1. Pairwise when-then conditional product
    // -------------------------------------------------------------------------
    private static Supplier<Set<List<Object>>> whenThen(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .conditionalProductOf(d.os, d.browser, d.network, d.database, d.region)

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

        return () -> builder.anyOrder()
                .stream()
                .map(tuple -> List.copyOf(tuple))
                .collect(Collectors.toSet());
    }


    // -------------------------------------------------------------------------
    // 2. Compound conditional product (triggers built from Condition.is)
    // -------------------------------------------------------------------------

    private static Supplier<Set<List<Object>>> buildCompound(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .compoundProductOf(d.os, d.browser, d.network, d.database, d.region)

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

        return () -> builder.generate()
                .stream()
                .map(tuple -> List.copyOf(tuple))
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // 3. Lambda Engine - Engine integrated withLambda (compiles into the same Relation tables as Compound)
    // -------------------------------------------------------------------------
    private static Supplier<Set<List<Object>>> buildEngineLambda(Dimensions d) {

        var builder = JNumberTools.cartesianProduct()
                .withLambda(d.os, d.browser, d.network, d.database, d.region)

                .when(d.os, d.browser).matches((os, browser) -> {
                    if (LINUX.equals(os)) return LINUX_BROWSERS.contains(browser);
                    if (WINDOWS.equals(os)) return WINDOWS_BROWSERS.contains(browser);
                    return true;
                })

                .andWhen(d.os, d.network).matches((os, network) ->
                        !LINUX.equals(os) || LINUX_NETWORKS.contains(network))

                .andWhen(d.os, d.database).matches((os, database) -> {
                    if (LINUX.equals(os)) return LINUX_DATABASES.contains(database);
                    if (WINDOWS.equals(os)) return WINDOWS_DATABASES.contains(database);
                    return true;
                })

                .andWhen(d.os, d.region).matches((os, region) -> {
                    if (LINUX.equals(os)) return LINUX_REGIONS.contains(region);
                    if (WINDOWS.equals(os)) return WINDOWS_REGIONS.contains(region);
                    return true;
                });

        return () -> builder.generate()
                .stream()
                .map(tuple -> List.copyOf(tuple))
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // 4. Pruned Java Stream baseline
    // -------------------------------------------------------------------------

    private static Supplier<Set<List<Object>>> buildPrunedStream(Dimensions d) {
        // No builder to construct; the closure itself is the whole implementation.
        return () -> prunedStream(d);
    }

    private static Set<List<Object>> prunedStream(Dimensions d) {

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
                )
                .collect(Collectors.toSet());
    }





    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Dimensions createDimensions(int size) {
        List<String> os = List.of(LINUX, WINDOWS);
        List<String> browser = createDimension(BROWSER_BASE, size, "OtherBrowser-");
        List<String> network = createDimension(NETWORK_BASE, size, "OtherNetwork-");
        List<String> database = createDimension(DATABASE_BASE, size, "OtherDatabase-");
        List<String> region = createDimension(REGION_BASE, size, "OtherRegion-");
        return new Dimensions(os, browser, network, database, region);
    }

    private static List<String> createDimension(String[] base, int size, String prefix) {
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

        private Dimensions(List<String> os, List<String> browser, List<String> network, List<String> database, List<String> region) {
            this.os = os;
            this.browser = browser;
            this.network = network;
            this.database = database;
            this.region = region;
        }
    }

    private static final class BenchmarkMethod {

        private final String name;
        private final Function<Dimensions, Supplier<Set<List<Object>>>> build;

        private BenchmarkMethod(String name, Function<Dimensions, Supplier<Set<List<Object>>>> build) {
            this.name = name;
            this.build = build;
        }
    }
    private static void printHeader(BenchmarkMethod[] methods) {
        StringBuilder fmt = new StringBuilder("%-8s %-12s");
        for (int i = 0; i < methods.length; i++) fmt.append(" %-17s");
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

    private static void printDataRow(BenchmarkMethod[] methods, int size, int expectedMatches, double[] medianMillis) {
        StringBuilder fmt = new StringBuilder("%-8d %-12d");
        for (int i = 0; i < methods.length; i++) fmt.append(" %-17.3f");
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
Benchmark result
Size     Matches      Pairwise when-then ms Compound ms       engineLambda ms   Pruned Stream ms
---------------------------------------------------------------------------------------------
100      616          0.110             0.105             0.117             0.643
200      1216         0.104             0.105             0.111             1.088
300      1816         0.128             0.132             0.135             1.847
400      2416         0.199             0.205             0.192             3.187
500      3016         0.275             0.276             0.262             5.027
1000     6016         0.404             0.388             0.411             17.284
2000     12016        0.812             0.847             0.829             69.063
3000     18016        1.355             1.350             1.322             153.291
4000     24016        1.764             1.732             1.744             270.358
5000     30016        2.409             2.336             2.347             419.092
 */