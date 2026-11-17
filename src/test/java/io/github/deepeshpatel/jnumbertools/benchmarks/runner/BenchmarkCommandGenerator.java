/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.benchmarks.runner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interactive console utility that generates a JMH command for selected conditional-product cases.
 * It prints the command but deliberately does not execute it.
 */
public final class BenchmarkCommandGenerator {

    private static final String BENCHMARK_REGEX = "'.*ConditionalProductBenchmark.*'";
    /** Valid shapes per topology; must match the {@code scenario} values of ConditionalProductBenchmark. */
    private static final Map<String, List<String>> SHAPES = new LinkedHashMap<>();
    static {
        List<String> grid = new ArrayList<>();
        for (int d : List.of(3, 5, 7, 10)) {
            for (int n : List.of(10, 25, 50)) {
                grid.add(d + "x" + n);
            }
        }
        SHAPES.put("CHAIN", grid);
        SHAPES.put("MANY_VALID", grid);
        SHAPES.put("COMPLETE", grid);
        SHAPES.put("DEAD_END", grid.stream().filter(x -> !x.equals("10x50")).toList());
        SHAPES.put("DEEP_DEAD_END", List.of("5x10", "5x25", "7x10", "7x25", "10x10"));
        SHAPES.put("SMALL_DOMAIN", List.of("8x4", "10x4", "12x3", "14x3", "8x5"));
    }
    private static final List<String> TOPOLOGIES = List.copyOf(SHAPES.keySet());
    private static final List<Integer> DIMENSIONS = distinct(0);
    private static final List<Integer> DOMAIN_SIZES = distinct(1);
    private static final List<String> IMPLEMENTATIONS =
            List.of("PRUNED_BITSET", "PRUNED_DFS", "DP", "ADAPTIVE", "NESTED_STREAM");

    private BenchmarkCommandGenerator() {
    }

    private static List<Integer> distinct(int part) {
        return SHAPES.values().stream().flatMap(List::stream)
                .map(shape -> Integer.parseInt(shape.split("x")[part]))
                .distinct().sorted().toList();
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("==============================================");
            System.out.println(" Conditional Product Benchmark Command Builder");
            System.out.println("==============================================");

            List<String> topologies = select(scanner, "topology", TOPOLOGIES,
                    Function.identity());
            List<Integer> dimensions = select(scanner, "dimensions", DIMENSIONS,
                    Object::toString);
            List<Integer> domainSizes = select(scanner, "domain size", DOMAIN_SIZES,
                    Object::toString);
            List<String> implementations = select(scanner, "implementation", IMPLEMENTATIONS,
                    Function.identity());

            int warmup = readPositiveInt(scanner, "Warmup iterations", 5);
            int measurement = readPositiveInt(scanner, "Measurement iterations", 8);
            int forks = readPositiveInt(scanner, "Forks", 2);
            String profiler = readProfiler(scanner);

            List<String> scenarios = buildScenarios(topologies, dimensions, domainSizes);
            if (scenarios.isEmpty()) {
                System.out.println("No valid scenario combinations were selected.");
                return;
            }

            System.out.println();
            System.out.println("Selected configuration");
            System.out.println("----------------------");
            System.out.println("Topologies      : " + String.join(", ", topologies));
            System.out.println("Dimensions      : " + join(dimensions));
            System.out.println("Domain sizes    : " + join(domainSizes));
            System.out.println("Implementations : " + String.join(", ", implementations));
            System.out.println("Valid scenarios : " + scenarios.size());
            System.out.println("Note            : only shapes defined for a topology are generated"
                    + " (e.g. DEAD_END has no 10x50).");

            String command = buildCommand(scenarios, implementations, warmup,
                    measurement, forks, profiler);
            System.out.println();
            System.out.println("Generated command");
            System.out.println("-----------------");
            System.out.println(command);
            System.out.println();
            System.out.println("The command has not been executed. Copy and run it from the project root.");
        }
    }

    private static <T> List<T> select(
            Scanner scanner, String label, List<T> options, Function<T, String> display) {
        while (true) {
            System.out.println();
            System.out.println("Select " + label + ":");
            for (int i = 0; i < options.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, display.apply(options.get(i)));
            }
            System.out.printf("  %d. All%n", options.size() + 1);
            System.out.print("Your choice (comma-separated, e.g. 1,3): ");

            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("all")
                    || input.equals(Integer.toString(options.size() + 1))) {
                return List.copyOf(options);
            }

            try {
                Set<Integer> selectedIndexes = parseIndexes(input, options.size());
                List<T> selected = new ArrayList<>(selectedIndexes.size());
                for (int index : selectedIndexes) {
                    selected.add(options.get(index - 1));
                }
                return List.copyOf(selected);
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid selection: " + exception.getMessage()
                        + " Please enter valid option numbers separated by commas.");
            }
        }
    }

    private static Set<Integer> parseIndexes(String input, int optionCount) {
        if (input.isBlank()) {
            throw new IllegalArgumentException("selection cannot be empty.");
        }
        Set<Integer> indexes = new LinkedHashSet<>();
        for (String token : input.split(",", -1)) {
            String value = token.trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("empty option between commas.");
            }
            final int index;
            try {
                index = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("'" + value + "' is not a number.");
            }
            if (index < 1 || index > optionCount) {
                throw new IllegalArgumentException("option " + index + " is out of range.");
            }
            if (!indexes.add(index)) {
                throw new IllegalArgumentException("option " + index + " was selected more than once.");
            }
        }
        return indexes;
    }

    private static List<String> buildScenarios(
            List<String> topologies, List<Integer> dimensions, List<Integer> domainSizes) {
        List<String> scenarios = new ArrayList<>();
        for (String topology : topologies) {
            for (String shape : SHAPES.get(topology)) {
                String[] parts = shape.split("x");
                if (dimensions.contains(Integer.parseInt(parts[0]))
                        && domainSizes.contains(Integer.parseInt(parts[1]))) {
                    scenarios.add(topology + "-" + shape);
                }
            }
        }
        return scenarios;
    }

    private static String buildCommand(
            List<String> scenarios,
            List<String> implementations,
            int warmup,
            int measurement,
            int forks,
            String profiler) {
        return "java -cp \"target/test-classes:target/classes:$(cat target/test-classpath.txt)\" \\\n"
                + "  org.openjdk.jmh.Main " + BENCHMARK_REGEX + " \\\n"
                + "  -p scenario=" + String.join(",", scenarios) + " \\\n"
                + "  -p implementation=" + String.join(",", implementations) + " \\\n"
                + "  -wi " + warmup + " -i " + measurement + " -f " + forks
                + " -tu ms -bm avgt" + (profiler.equals("none") ? "" : " -prof " + profiler);
    }

    private static int readPositiveInt(Scanner scanner, String label, int defaultValue) {
        while (true) {
            System.out.printf("%s [%d]: ", label, defaultValue);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Prompt again below.
            }
            System.out.println("Enter a positive integer.");
        }
    }

    private static String readProfiler(Scanner scanner) {
        while (true) {
            System.out.print("Profiler [gc] (gc/none): ");
            String value = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty() || value.equals("gc")) {
                return "gc";
            }
            if (value.equals("none")) {
                return "none";
            }
            System.out.println("Enter 'gc' or 'none'.");
        }
    }

    private static String join(List<?> values) {
        return values.stream().map(Object::toString).collect(Collectors.joining(", "));
    }
}
