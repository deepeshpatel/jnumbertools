package io.github.deepeshpatel.jnumbertools.benchmarks.product.main;


import io.github.deepeshpatel.jnumbertools.entrypoint.Product;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Hand-rolled (no JMH/Maven) quick-check harness comparing the Compiled,
 *  builders against an incrementally pruned Java Stream baseline.
 *
 * Compares count(), byRanks(midrank) and a full lexOrder() walk across
 * configurable target validity ratios.
 *
 * - Chooses the closest achievable overall validity ratio.
 * - Verifies lexOrder tuple count and sequence using a rolling hash.
 * - Handles configurations with no usable reference API.
 * - Uses multiple warmup and measurement rounds.
 */
public class ConditionalProductSpeedBenchmark {

    // ---------------------------------------------------------------------------------------------
    // Configuration
    // ---------------------------------------------------------------------------------------------

    private enum Api {
        COMPILED("Compiled"),
        LAMBDA("Lambda Engine"),
        PRUNED_STREAM("Pruned-Stream");

        final String label;

        Api(String label) {
            this.label = label;
        }
    }

    private enum Op {
        COUNT("count()"),
        BY_RANKS("byRanks(midrank)"),
        LEX_ORDER("lexOrder()");

        final String label;

        Op(String label) {
            this.label = label;
        }
    }

    /** Add/remove a candidate API here. */
    private static final EnumSet<Api> ENABLED_APIS = EnumSet.of(
            Api.COMPILED, Api.LAMBDA, Api.PRUNED_STREAM
    );

    /** Add/remove an operation here. */
    private static final EnumSet<Op> ENABLED_OPS = EnumSet.of(
            Op.COUNT, Op.BY_RANKS, Op.LEX_ORDER
    );

    private static final int WARMUP_ROUNDS = 0;
    private static final int MEASUREMENT_ROUNDS = 1;

    private static final int[] SIZES = {
            10, 20, 30, 100
    };

    private static final double[] VALIDITY_RATIOS = {
            0.02, 0.05, 0.1, 0.25, 0.50, 0.75
    };

    /**
     * Pruned-Stream is skipped entirely once its expected match count exceeds
     * this limit. The limit applies to every enabled operation.
     */
    private static final long MAX_COUNT_VIA_LOOP = 1_000_000_000L;

    private static final BigInteger TWO = BigInteger.valueOf(2);

    private static final String LINUX = "Linux";
    private static final String WINDOWS = "Windows";

    private static final List<Api> CANONICAL_ORDER =
            List.of(Api.COMPILED, Api.LAMBDA, Api.PRUNED_STREAM);

    private static final int METHOD_COL_WIDTH = 20;
    private static final int SIZE_COL_WIDTH = 8;
    private static final int VALUE_COL_WIDTH = 18;

    private static volatile long blackhole;

    public static void main(String[] args) {

        validateConfiguration();
        printBenchmarkConditions();
        printProblemStatement();

        for (double validityRatio : VALIDITY_RATIOS) {
            runValidityRatio(validityRatio);
        }
    }

    private static void validateConfiguration() {
        if (ENABLED_APIS.isEmpty()) {
            throw new IllegalArgumentException("At least one API must be enabled");
        }

        for (double ratio : VALIDITY_RATIOS) {
            if (ratio <= 0.0 || ratio > 1.0) {
                throw new IllegalArgumentException(
                        "Validity ratios must be greater than 0 and at most 1: " + ratio
                );
            }
        }

        for (int size : SIZES) {
            if (size <= 0) {
                throw new IllegalArgumentException("Sizes must be positive: " + size);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Benchmark execution
    // -------------------------------------------------------------------------

    private static void runValidityRatio(double targetRatio) {

        System.out.println();
        System.out.printf("Validity ratio = %.0f%%%n", targetRatio * 100.0);

        printHeader();

        for (int size : SIZES) {
            runSize(targetRatio, size);
        }
    }

    private static void runSize(double targetRatio, int size) {

        Dimensions d = createDimensions(size, targetRatio);

        BigInteger expectedCount = expectedMatches(d);
        BigInteger midrank = expectedCount.divide(TWO);

        double actualRatio =
                expectedCount.doubleValue() / totalProductSize(size).doubleValue();

        Map<Api, BenchmarkMethod> methods = createMethods(d);

        boolean skipPrunedStream = methods.containsKey(Api.PRUNED_STREAM)
                && expectedCount.compareTo(BigInteger.valueOf(MAX_COUNT_VIA_LOOP)) > 0;

        System.out.printf(
                "%n  Size %d | Matches: %s | Actual validity: %.3f%%%n",
                size, expectedCount, actualRatio * 100.0
        );

        verifyCorrectness(methods, expectedCount, midrank, skipPrunedStream, size);

        if (ENABLED_OPS.contains(Op.COUNT)) {
            Map<Api, Double> timings = new LinkedHashMap<>();

            for (Api api : methods.keySet()) {
                if (api == Api.PRUNED_STREAM && skipPrunedStream) continue;

                timings.put(api, measureCount(methods.get(api).count, expectedCount));
            }

            printRow(Op.COUNT, size, methods.keySet(), timings, skipPrunedStream);
        }

        if (ENABLED_OPS.contains(Op.BY_RANKS)) {
            Api reference = firstEnabledApi(methods, skipPrunedStream);

            Map<Api, Double> timings = new LinkedHashMap<>();

            if (reference != null) {
                Object expectedMiddle = methods.get(reference).byRank.apply(midrank);

                for (Api api : methods.keySet()) {
                    if (api == Api.PRUNED_STREAM && skipPrunedStream) continue;

                    timings.put(
                            api,
                            measureByRank(
                                    methods.get(api).byRank,
                                    midrank,
                                    expectedMiddle
                            )
                    );
                }
            }

            printRow(Op.BY_RANKS, size, methods.keySet(), timings, skipPrunedStream);
        }

        if (ENABLED_OPS.contains(Op.LEX_ORDER)) {
            Map<Api, Double> timings = new LinkedHashMap<>();

            for (Api api : methods.keySet()) {
                if (api == Api.PRUNED_STREAM && skipPrunedStream) continue;

                timings.put(
                        api,
                        measureLexOrder(
                                methods.get(api).lexOrderFullWalk,
                                expectedCount
                        )
                );
            }

            printRow(Op.LEX_ORDER, size, methods.keySet(), timings, skipPrunedStream);
        }
    }

    // -------------------------------------------------------------------------
    // Correctness gate
    // -------------------------------------------------------------------------

    private static void verifyCorrectness(
            Map<Api, BenchmarkMethod> methods,
            BigInteger expectedCount,
            BigInteger midrank,
            boolean skipPrunedStream,
            int size) {

        for (Map.Entry<Api, BenchmarkMethod> entry : methods.entrySet()) {
            if (entry.getKey() == Api.PRUNED_STREAM && skipPrunedStream) continue;

            BigInteger actual = entry.getValue().count.get();

            if (!expectedCount.equals(actual)) {
                throw new AssertionError(
                        entry.getKey() + " count() returned an incorrect count for size " + size
                                + ". Expected " + expectedCount + ", got " + actual
                );
            }
        }

        Api reference = firstEnabledApi(methods, skipPrunedStream);
        if (reference == null) return;

        Object expectedMiddle = methods.get(reference).byRank.apply(midrank);

        for (Map.Entry<Api, BenchmarkMethod> entry : methods.entrySet()) {
            if (entry.getKey() == reference) continue;
            if (entry.getKey() == Api.PRUNED_STREAM && skipPrunedStream) continue;

            Object actualMiddle = entry.getValue().byRank.apply(midrank);

            if (!expectedMiddle.equals(actualMiddle)) {
                throw new AssertionError(
                        entry.getKey() + " returned a different middle tuple than " + reference
                                + " for size " + size
                );
            }
        }

        if (!ENABLED_OPS.contains(Op.LEX_ORDER)) return;

        WalkResult expectedWalk = methods.get(reference).lexOrderFullWalk.get();

        if (!expectedCount.equals(expectedWalk.count)) {
            throw new AssertionError(
                    reference + " lexOrder() produced " + expectedWalk.count
                            + " tuples, expected " + expectedCount + " for size " + size
            );
        }

        for (Map.Entry<Api, BenchmarkMethod> entry : methods.entrySet()) {
            if (entry.getKey() == reference) continue;
            if (entry.getKey() == Api.PRUNED_STREAM && skipPrunedStream) continue;

            WalkResult actualWalk = entry.getValue().lexOrderFullWalk.get();

            if (!expectedCount.equals(actualWalk.count)) {
                throw new AssertionError(
                        entry.getKey() + " lexOrder() produced " + actualWalk.count
                                + " tuples, expected " + expectedCount + " for size " + size
                );
            }

            if (expectedWalk.hash != actualWalk.hash) {
                throw new AssertionError(
                        entry.getKey() + " produced a different lexOrder() sequence than "
                                + reference + " for size " + size
                );
            }
        }
    }

    private static Api firstEnabledApi(
            Map<Api, BenchmarkMethod> methods,
            boolean skipPrunedStream) {

        for (Api api : CANONICAL_ORDER) {
            if (!methods.containsKey(api)) continue;
            if (api == Api.PRUNED_STREAM && skipPrunedStream) continue;
            return api;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Timing
    // -------------------------------------------------------------------------

    private static double measureCount(
            Supplier<BigInteger> operation,
            BigInteger expected) {

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            consume(operation.get());
        }

        long[] measurements = new long[MEASUREMENT_ROUNDS];

        for (int i = 0; i < MEASUREMENT_ROUNDS; i++) {
            long start = System.nanoTime();
            BigInteger result = operation.get();
            long elapsed = System.nanoTime() - start;

            measurements[i] = elapsed;

            if (!expected.equals(result)) {
                throw new AssertionError("Unexpected count during measurement");
            }

            consume(result);
        }

        return median(measurements) / 1_000_000.0;
    }

    private static double measureByRank(
            Function<BigInteger, Object> operation,
            BigInteger rank,
            Object expected) {

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            consume(operation.apply(rank));
        }

        long[] measurements = new long[MEASUREMENT_ROUNDS];

        for (int i = 0; i < MEASUREMENT_ROUNDS; i++) {
            long start = System.nanoTime();
            Object result = operation.apply(rank);
            long elapsed = System.nanoTime() - start;

            measurements[i] = elapsed;

            if (!expected.equals(result)) {
                throw new AssertionError("Unexpected tuple during rank measurement");
            }

            consume(result);
        }

        return median(measurements) / 1_000_000.0;
    }

    private static double measureLexOrder(
            Supplier<WalkResult> fullWalk,
            BigInteger expected) {

        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            consume(fullWalk.get().count);
        }

        long[] measurements = new long[MEASUREMENT_ROUNDS];

        for (int i = 0; i < MEASUREMENT_ROUNDS; i++) {
            long start = System.nanoTime();
            WalkResult result = fullWalk.get();
            long elapsed = System.nanoTime() - start;

            measurements[i] = elapsed;

            if (!expected.equals(result.count)) {
                throw new AssertionError(
                        "lexOrder() produced " + result.count
                                + " tuples during measurement, expected " + expected
                );
            }

            consume(result.count);
        }

        return median(measurements) / 1_000_000.0;
    }

    private static long median(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    private static void consume(Object value) {
        blackhole ^= value.hashCode();
    }

    // -------------------------------------------------------------------------
    // Method registry
    // -------------------------------------------------------------------------

    private static Map<Api, BenchmarkMethod> createMethods(Dimensions d) {

        Map<Api, BenchmarkMethod> methods = new LinkedHashMap<>();

        if (ENABLED_APIS.contains(Api.COMPILED)) {
            methods.put(Api.COMPILED, buildCompiled(d));
        }

        if (ENABLED_APIS.contains(Api.LAMBDA)) {
            methods.put(Api.LAMBDA, buildLambdaEngine(d));
        }

        if (ENABLED_APIS.contains(Api.PRUNED_STREAM)) {
            methods.put(Api.PRUNED_STREAM, buildPrunedStream(d));
        }

        return methods;
    }

    // -------------------------------------------------------------------------
    // 1. Compiled: pairwise when-then API
    // -------------------------------------------------------------------------

    private static BenchmarkMethod buildCompiled(Dimensions d) {

        var builder = Product.conditional(d.os, d.browser, d.network, d.database, d.region)

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

        return new BenchmarkMethod(
                builder::count,
                rank -> builder.byRanks(List.of(rank)).stream().findFirst().orElseThrow(),
                () -> fullLexOrderWalk(builder.lexOrder().stream())
        );
    }

    // -------------------------------------------------------------------------
    // 3. Lambda Engine API
    // -------------------------------------------------------------------------

    private static BenchmarkMethod buildLambdaEngine(Dimensions d) {

        var builder = Product.conditional(d.os, d.browser, d.network, d.database, d.region)
                .withLambda(d.os, d.browser, d.network, d.database, d.region)

                .when(d.os, d.browser).matches((os, browser) ->
                        d.browserAccepted.contains(browser))

                .when(d.os, d.network).matches((os, network) ->
                        !LINUX.equals(os) || d.networkAccepted.contains(network))

                .when(d.os, d.database).matches((os, database) ->
                        d.databaseAccepted.contains(database))

                .when(d.os, d.region).matches((os, region) ->
                        d.regionAccepted.contains(region));

        return new BenchmarkMethod(
                builder::count,
                rank -> builder.byRanks(List.of(rank)).stream().findFirst().orElseThrow(),
                () -> fullLexOrderWalk(builder.lexOrder().stream())
        );
    }

    // -------------------------------------------------------------------------
    // 4. Incrementally pruned Java Stream
    // -------------------------------------------------------------------------

    private static BenchmarkMethod buildPrunedStream(Dimensions d) {

        return new BenchmarkMethod(
                () -> BigInteger.valueOf(prunedStream(d).count()),
                rank -> prunedStream(d)
                        .skip(rank.longValueExact())
                        .findFirst()
                        .orElseThrow(),
                () -> fullLexOrderWalk(prunedStream(d))
        );
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
                                                        os, browser, network, database, region
                                                ))
                                        )
                                )
                        )
                );
    }

    /**
     * Walks every element using forEach(), never iterator().
     *
     * The rolling hash is order-sensitive and allows the correctness gate
     * to compare the tuple sequences without retaining all tuples in memory.
     */
    private static WalkResult fullLexOrderWalk(Stream<?> stream) {

        long[] count = {0};
        long[] hash = {0};

        stream.forEach(tuple -> {
            count[0]++;
            hash[0] = 31 * hash[0] + tuple.hashCode();
            consume(tuple);
        });

        return new WalkResult(BigInteger.valueOf(count[0]), hash[0]);
    }

    // -------------------------------------------------------------------------
    // Dimension creation
    // -------------------------------------------------------------------------

    private static Dimensions createDimensions(int size, double targetRatio) {

        int commonCount = closestCommonCount(size, targetRatio);

        List<String> os = List.of(LINUX, WINDOWS);

        Dimension browser = createDimension("Browser", size, commonCount);
        Dimension network = createDimension("Network", size, commonCount);
        Dimension database = createDimension("Database", size, commonCount);
        Dimension region = createDimension("Region", size, commonCount);

        return new Dimensions(
                os,
                browser.values, network.values, database.values, region.values,
                browser.accepted, network.accepted, database.accepted, region.accepted,
                browser.acceptedSet, network.acceptedSet,
                database.acceptedSet, region.acceptedSet
        );
    }

    /**
     * Finds the accepted-value count whose resulting overall validity ratio
     * is closest to the requested ratio.
     *
     * Overall ratio:
     * (common^4 + common^3 * size) / (2 * size^4)
     *
     * The ratio is discrete because the accepted-value count must be an integer.
     */
    private static int closestCommonCount(int size, double targetRatio) {

        int bestCount = 1;
        double bestDifference = Double.MAX_VALUE;

        for (int common = 1; common <= size; common++) {

            double actualRatio = validityRatio(size, common);
            double difference = Math.abs(actualRatio - targetRatio);

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
        HashSet<String> acceptedSet = new HashSet<>(commonCount);

        int commonIndex = 0;
        int otherIndex = 0;

        /*
         * Interleave common and other values to avoid grouping all accepted
         * values at the beginning of the dimension.
         */
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

    private static BigInteger expectedMatches(Dimensions d) {

        BigInteger common = BigInteger.valueOf(d.browserAccepted.size());
        BigInteger size = BigInteger.valueOf(d.browser.size());

        /*
         * Linux:   common browser x common network x common database x common region
         * Windows: common browser x unrestricted network x common database x common region
         */
        BigInteger linux = common.pow(4);
        BigInteger windows = common.pow(3).multiply(size);

        return linux.add(windows);
    }

    private static BigInteger totalProductSize(int size) {
        BigInteger n = BigInteger.valueOf(size);
        return TWO.multiply(n.pow(4));
    }

    // -------------------------------------------------------------------------
    // Data holders
    // -------------------------------------------------------------------------

    private record Dimension(
            List<String> values,
            List<String> accepted,
            HashSet<String> acceptedSet) {
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
            HashSet<String> browserAcceptedSet,
            HashSet<String> networkAcceptedSet,
            HashSet<String> databaseAcceptedSet,
            HashSet<String> regionAcceptedSet) {
    }

    private record BenchmarkMethod(
            Supplier<BigInteger> count,
            Function<BigInteger, Object> byRank,
            Supplier<WalkResult> lexOrderFullWalk) {
    }

    private record WalkResult(
            BigInteger count,
            long hash) {
    }

    // -------------------------------------------------------------------------
    // Output: fixed-width table
    // -------------------------------------------------------------------------

    private static void printHeader() {

        StringBuilder header = new StringBuilder(
                String.format(
                        "%-" + METHOD_COL_WIDTH + "s %-" + SIZE_COL_WIDTH + "s",
                        "Method", "Size"
                )
        );

        for (Api api : CANONICAL_ORDER) {
            if (!ENABLED_APIS.contains(api)) continue;

            header.append(String.format(
                    " %" + VALUE_COL_WIDTH + "s",
                    api.label
            ));
        }

        System.out.println("-".repeat(header.length()));
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
    }

    private static void printRow(
            Op op,
            int size,
            java.util.Set<Api> presentApis,
            Map<Api, Double> timings,
            boolean skipPrunedStream) {

        StringBuilder row = new StringBuilder(
                String.format(
                        "%-" + METHOD_COL_WIDTH + "s %-" + SIZE_COL_WIDTH + "d",
                        op.label, size
                )
        );

        for (Api api : CANONICAL_ORDER) {
            if (!presentApis.contains(api)) continue;

            if (api == Api.PRUNED_STREAM && skipPrunedStream) {
                row.append(String.format(
                        " %" + VALUE_COL_WIDTH + "s",
                        "skipped"
                ));
                continue;
            }

            Double timing = timings.get(api);

            if (timing == null) {
                row.append(String.format(
                        " %" + VALUE_COL_WIDTH + "s",
                        "skipped"
                ));
            } else {
                row.append(String.format(
                        " %" + (VALUE_COL_WIDTH - 3) + ".3f ms",
                        timing
                ));
            }
        }

        System.out.println(row);
    }

    private static void printBenchmarkConditions() {

        System.out.println();
        System.out.println("Conditional Product Full API Comparison Benchmark (V4, tabular)");
        System.out.println("============================================================");
        System.out.println("Warmup rounds:       " + WARMUP_ROUNDS);
        System.out.println("Measurement rounds:  " + MEASUREMENT_ROUNDS);
        System.out.println("Pruned Stream limit: " + MAX_COUNT_VIA_LOOP);
        System.out.println("Sizes:               " + Arrays.toString(SIZES));
        System.out.println("Enabled APIs:        " + ENABLED_APIS);
        System.out.println("Enabled operations:  " + ENABLED_OPS);
        System.out.println();
    }

    private static void printProblemStatement() {

        System.out.println("""
                Problem Statement
                ============================================================

                Find valid configurations of:

                    Operating System x Browser x Network x Database x Region

                Operating System has two possible values: Linux, Windows.

                Rules:
                1. Linux:   Browser, Network, Database and Region must each be in their accepted values.
                2. Windows: Browser, Database and Region must each be in their accepted values;
                            Network has no restriction.

                The accepted values are generated according to the requested validity ratio.
                The integer accepted-value count is selected to produce the closest achievable
                overall validity ratio. The remaining values are rejected wherever a restriction applies.

                Operations compared (toggle in ENABLED_OPS):
                    count()
                    byRanks(midrank)
                    lexOrder() -- full walk via forEach(), counted and sequence-verified

                Implementations compared (toggle in ENABLED_APIS):
                    Compiled pairwise when-then API
                    Compound Condition API
                    Lambda Engine API
                    Incrementally pruned Java Stream

                ============================================================
                """);
    }
}