/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Launches the conditional-product JMH suite and writes one final summary file per benchmark method.
 *
 * <p>JMH's normal progress and diagnostic output remains on the console. The complete machine-readable
 * result is saved as JSON, while method-specific text files contain only completed benchmark results.</p>
 *
 * <p>Optional arguments: {@code --method=traversalOnly|endToEnd},
 * {@code --scenario=case-id}, {@code --implementation=PRUNED_BITSET}, and
 * {@code --output-dir=target/benchmarks}. GC profiling is enabled by default and can be disabled
 * with {@code --no-gc}.</p>
 */
public final class ConditionalProductBenchmarkRunner {
    private static final String BENCHMARK_CLASS = "ConditionalProductStrategyBenchmark";

    private ConditionalProductBenchmarkRunner() { }

    public static void main(String[] args) throws Exception {
        Map<String, String> arguments = parseArguments(args);
        Path outputDirectory = Path.of(arguments.getOrDefault("output-dir", "target/benchmarks"))
                .toAbsolutePath().normalize();
        Files.createDirectories(outputDirectory);

        Path jsonFile = outputDirectory.resolve(BENCHMARK_CLASS + "_results.json");
        OptionsBuilder options = new OptionsBuilder()
                .include(benchmarkPattern(arguments.get("method")))
                .resultFormat(ResultFormatType.JSON)
                .result(jsonFile.toString())
                .shouldFailOnError(true);

        if (arguments.containsKey("scenario")) {
            options.param("scenarioName", arguments.get("scenario"));
        }
        if (arguments.containsKey("implementation")) {
            options.param("implementation", arguments.get("implementation"));
        }
        if (!arguments.containsKey("no-gc")) {
            options.addProfiler(GCProfiler.class);
        }

        System.out.println("JMH JSON results: " + jsonFile);
        System.out.println("Per-method text results: " + outputDirectory);
        Collection<RunResult> results = new Runner(options.build()).run();
        writeMethodSummaries(results, outputDirectory);
    }

    private static String benchmarkPattern(String method) {
        if (method == null) {
            return ".*" + BENCHMARK_CLASS + "\\.(traversalOnly|endToEnd)";
        }
        if (!method.equals("traversalOnly") && !method.equals("endToEnd")) {
            throw new IllegalArgumentException("Unsupported benchmark method: " + method);
        }
        return ".*" + BENCHMARK_CLASS + "\\." + method;
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String arg : args) {
            if (arg.equals("--no-gc")) {
                values.put("no-gc", "true");
            } else if (arg.startsWith("--") && arg.contains("=")) {
                int separator = arg.indexOf('=');
                values.put(arg.substring(2, separator), arg.substring(separator + 1));
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        return values;
    }

    private static void writeMethodSummaries(Collection<RunResult> results, Path outputDirectory)
            throws IOException {
        Map<String, List<String>> summaries = new LinkedHashMap<>();
        for (RunResult run : results) {
            String benchmark = run.getBenchmark();
            String method = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            summaries.computeIfAbsent(method, ignored -> new ArrayList<>()).add(format(run));
        }

        for (Map.Entry<String, List<String>> entry : summaries.entrySet()) {
            Path output = outputDirectory.resolve(BENCHMARK_CLASS + "." + entry.getKey() + "_output.txt");
            String content = String.join(System.lineSeparator()
                    + System.lineSeparator(), entry.getValue()) + System.lineSeparator();
            Files.writeString(output, content, StandardCharsets.UTF_8);
            System.out.println("Wrote " + output);
        }
    }

    private static String format(RunResult run) {
        Result<?> primary = run.getPrimaryResult();
        StringBuilder text = new StringBuilder(512);
        text.append("Benchmark: ").append(run.getBenchmark()).append(System.lineSeparator());
        text.append("Primary score: ")
                .append(String.format(Locale.ROOT, "%.6f ± %.6f %s",
                        primary.getScore(), primary.getScoreError(), primary.getScoreUnit()))
                .append(System.lineSeparator());

        text.append("Parameters:").append(System.lineSeparator());
        for (String key : run.getParams().getParamsKeys()) {
            text.append("  ").append(key).append(" = ")
                    .append(run.getParams().getParam(key)).append(System.lineSeparator());
        }

        if (!run.getSecondaryResults().isEmpty()) {
            text.append("Secondary results:").append(System.lineSeparator());
            run.getSecondaryResults().forEach((name, result) -> text.append("  ")
                    .append(name).append(" = ")
                    .append(String.format(Locale.ROOT, "%.6f ± %.6f %s",
                            result.getScore(), result.getScoreError(), result.getScoreUnit()))
                    .append(System.lineSeparator()));
        }
        return text.toString();
    }
}
