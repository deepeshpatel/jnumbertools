/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.jmh.apicomparison;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import io.github.deepeshpatel.jnumbertools.generator.product.conditional.Condition;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Public API comparison of the three JNumberTools conditional-product builders
 * against an independently written, incrementally pruned Java Stream baseline.
 *
 * Builder construction and rule registration happen in trial setup, matching
 * the quick-screening benchmark. Timed invocations generate and materialize
 * the complete tuple set for the selected implementation.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 8)
@Fork(2)
public class ConditionalProductApiBenchmark {

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

    @Param({"100", "200", "300", "400", "500", "1000", "2000", "3000", "4000", "5000"})
    public int size;

    @Param({"WHEN_THEN", "COMPOUND", "LAMBDA", "PRUNED_STREAM"})
    public String implementation;

    private Dimensions dimensions;
    private Supplier<Set<List<Object>>> operation;
    private int expectedCount;
    private long expectedChecksum;

    @Setup(Level.Trial)
    public void setup() {
        dimensions = createDimensions(size);
        operation = createOperation(dimensions, implementation);

        Set<List<Object>> expected = prunedStream(dimensions);
        Set<List<Object>> actual = operation.get();
        if (!expected.equals(actual)) {
            throw new IllegalStateException(implementation + " differs from the pruned-stream baseline"
                    + " for size " + size);
        }

        expectedCount = expected.size();
        expectedChecksum = checksum(expected);
        int formulaCount = 6 * size + 16;
        if (expectedCount != formulaCount) {
            throw new IllegalStateException("Unexpected match count: expected " + formulaCount
                    + ", got " + expectedCount);
        }
    }

    @Benchmark
    public long generateAndMaterialize() {
        Set<List<Object>> result = operation.get();
        long actualChecksum = checksum(result);
        if (result.size() != expectedCount || actualChecksum != expectedChecksum) {
            throw new IllegalStateException("Unexpected result: count=" + result.size()
                    + ", checksum=" + actualChecksum);
        }
        return actualChecksum;
    }

    private static Supplier<Set<List<Object>>> createOperation(Dimensions d, String implementation) {
        return switch (implementation) {
            case "WHEN_THEN" -> {
                var builder = JNumberTools.cartesianProduct()
                        .conditionalProductOf(d.os, d.browser, d.network, d.database, d.region)
                        .when(d.os).is(LINUX).then(d.browser).isOneOf(LINUX_BROWSERS)
                        .when(d.os).is(LINUX).then(d.network).isOneOf(LINUX_NETWORKS)
                        .when(d.os).is(LINUX).then(d.database).isOneOf(LINUX_DATABASES)
                        .when(d.os).is(LINUX).then(d.region).isOneOf(LINUX_REGIONS)
                        .when(d.os).is(WINDOWS).then(d.browser).isOneOf(WINDOWS_BROWSERS)
                        .when(d.os).is(WINDOWS).then(d.database).isOneOf(WINDOWS_DATABASES)
                        .when(d.os).is(WINDOWS).then(d.region).isOneOf(WINDOWS_REGIONS);
                yield () -> builder.anyOrder().stream()
                        .map(tuple -> List.copyOf(tuple))
                        .collect(Collectors.toSet());
            }
            case "COMPOUND" -> {
                var builder = JNumberTools.cartesianProduct()
                        .compoundProductOf(d.os, d.browser, d.network, d.database, d.region)
                        .when(Condition.condition(d.os).is(LINUX)).then(d.browser).isOneOf(LINUX_BROWSERS)
                        .when(Condition.condition(d.os).is(LINUX)).then(d.network).isOneOf(LINUX_NETWORKS)
                        .when(Condition.condition(d.os).is(LINUX)).then(d.database).isOneOf(LINUX_DATABASES)
                        .when(Condition.condition(d.os).is(LINUX)).then(d.region).isOneOf(LINUX_REGIONS)
                        .when(Condition.condition(d.os).is(WINDOWS)).then(d.browser).isOneOf(WINDOWS_BROWSERS)
                        .when(Condition.condition(d.os).is(WINDOWS)).then(d.database).isOneOf(WINDOWS_DATABASES)
                        .when(Condition.condition(d.os).is(WINDOWS)).then(d.region).isOneOf(WINDOWS_REGIONS);
                yield () -> builder.generate().stream()
                        .map(tuple -> List.copyOf(tuple))
                        .collect(Collectors.toSet());
            }
            case "LAMBDA" -> {
                var builder = JNumberTools.cartesianProduct()
                        .withLambda(d.os, d.browser, d.network, d.database, d.region)
                         .when((os, browser, network, database, region) ->
        (LINUX.equals(os) ? LINUX_BROWSERS.contains(browser)
                : WINDOWS.equals(os) ? WINDOWS_BROWSERS.contains(browser) : true)
        && (!LINUX.equals(os) || LINUX_NETWORKS.contains(network))
        && (LINUX.equals(os) ? LINUX_DATABASES.contains(database)
                : WINDOWS.equals(os) ? WINDOWS_DATABASES.contains(database) : true)
        && (LINUX.equals(os) ? LINUX_REGIONS.contains(region)
                : WINDOWS.equals(os) ? WINDOWS_REGIONS.contains(region) : true));
                            if (WINDOWS.equals(os)) return WINDOWS_REGIONS.contains(region);
                            return true;
                        });
                yield () -> builder.generate().stream()
                        .map(tuple -> List.copyOf(tuple))
                        .collect(Collectors.toSet());
            }
            case "PRUNED_STREAM" -> () -> prunedStream(d);
            default -> throw new IllegalArgumentException("Unknown implementation: " + implementation);
        };
    }

    private static Set<List<Object>> prunedStream(Dimensions d) {
        return d.os.stream()
                .flatMap(os -> d.browser.stream()
                        .filter(browser -> os.equals(LINUX)
                                ? LINUX_BROWSERS.contains(browser)
                                : WINDOWS_BROWSERS.contains(browser))
                        .flatMap(browser -> d.network.stream()
                                .filter(network -> !os.equals(LINUX)
                                        || LINUX_NETWORKS.contains(network))
                                .flatMap(network -> d.database.stream()
                                        .filter(database -> os.equals(LINUX)
                                                ? LINUX_DATABASES.contains(database)
                                                : WINDOWS_DATABASES.contains(database))
                                        .flatMap(database -> d.region.stream()
                                                .filter(region -> os.equals(LINUX)
                                                        ? LINUX_REGIONS.contains(region)
                                                        : WINDOWS_REGIONS.contains(region))
                                                .map(region -> List.<Object>of(
                                                        os, browser, network, database, region))
                                        )
                                )
                        )
                )
                .collect(Collectors.toSet());
    }

    private static long checksum(Set<List<Object>> tuples) {
        long checksum = 0;
        for (List<Object> tuple : tuples) {
            checksum += tuple.hashCode();
        }
        return checksum;
    }

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

    private record Dimensions(
            List<String> os,
            List<String> browser,
            List<String> network,
            List<String> database,
            List<String> region) {
    }
}
