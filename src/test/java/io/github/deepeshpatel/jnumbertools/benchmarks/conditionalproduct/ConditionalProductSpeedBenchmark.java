
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct;

import io.github.deepeshpatel.jnumbertools.base.JNumberTools;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ConditionalProductSpeedBenchmark {

    private static final List<String> os =
            List.of("Windows", "macOS", "Linux");

    private static final List<String> browser =
            List.of("Chrome", "Safari", "Firefox");

    private static final List<String> database =
            List.of("PostgreSQL", "SQL Server", "Oracle");

    private static final List<String> deployment =
            List.of("Cloud", "On-Prem");

    private static final List<String> region =
            List.of("US", "EU", "APAC");

    @Setup(Level.Trial)
    public void verifyCorrectness() {

        var builderResult = JNumberTools.cartesianProduct()
                .conditional().ofList(os)
                .and(browser)
                .and(database)
                .and(deployment)
                .and(region)
                .when(browser).is("Safari").then(os).isOneOf("macOS")
                .when(os).is("Linux").then(database).isOneOf("PostgreSQL", "Oracle")
                .when(database).is("Oracle").then(deployment).isOneOf("On-Prem")
                .when(database).is("SQL Server").then(os).isOneOf("Windows")
                .when(deployment).is("Cloud").then(region).isOneOf("US", "EU")
                .lexOrder()
                .stream()
                .toList();

        var streamResult = streamFilter().toList();

        if (!builderResult.equals(streamResult)) {
            throw new IllegalStateException(
                    "Result mismatch: builder=" + builderResult.size()
                            + ", stream=" + streamResult.size()
            );
        }

        System.out.println("Correctness check PASSED");
        System.out.println("Valid tuples: " + builderResult.size());
    }

    // JNumberTools: construction + rule registration + traversal + consumption

    @Benchmark
    public long jnumberTools() {

        var builder = JNumberTools.cartesianProduct()
                .conditional().ofList(os)
                .and(browser)
                .and(database)
                .and(deployment)
                .and(region)
                .when(browser).is("Safari").then(os).isOneOf("macOS")
                .when(os).is("Linux").then(database).isOneOf("PostgreSQL", "Oracle")
                .when(database).is("Oracle").then(deployment).isOneOf("On-Prem")
                .when(database).is("SQL Server").then(os).isOneOf("Windows")
                .when(deployment).is("Cloud").then(region).isOneOf("US", "EU");

        return builder.lexOrder()
                .stream()
                .mapToLong(List::hashCode)
                .sum();
    }

    // Equivalent nested Java Streams: filter each level before expanding the next

    private static Stream<List<String>> streamFilter() {

        return os.stream()
                .flatMap(o -> browser.stream()
                        .filter(b ->
                                !b.equals("Safari") || o.equals("macOS"))
                        .flatMap(b -> database.stream()
                                .filter(d ->
                                        !o.equals("Linux")
                                                || d.equals("PostgreSQL")
                                                || d.equals("Oracle"))
                                .filter(d ->
                                        !d.equals("SQL Server")
                                                || o.equals("Windows"))
                                .flatMap(d -> deployment.stream()
                                        .filter(dep ->
                                                !d.equals("Oracle")
                                                        || dep.equals("On-Prem"))
                                        .flatMap(dep -> region.stream()
                                                .filter(r ->
                                                        !dep.equals("Cloud")
                                                                || r.equals("US")
                                                                || r.equals("EU"))
                                                .map(r -> List.of(o, b, d, dep, r))
                                        )
                                )
                        )
                );
    }

    @Benchmark
    public long nestedJavaStreams() {

        return streamFilter()
                .mapToLong(List::hashCode)
                .sum();
    }
}
