
package io.github.deepeshpatel.jnumbertools.benchmarks.runner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * Interactive console utility that generates a JMH command for selected
 * conditional-product benchmark scenarios.
 *
 * This class generates commands but does not execute them.
 */
public final class BenchmarkCommandGenerator {

    private static final String BENCHMARK_REGEX =
            "'.*ConditionalProductBenchmark.*'";

    /**
     * Valid shapes per topology.
     *
     * These must match the scenario values supported by
     * ConditionalProductBenchmark.
     */
    private static final Map<String, List<String>> SHAPES =
            new LinkedHashMap<>();

    static {
        List<String> grid = new ArrayList<>();

        for (int dimensions : List.of(3, 5, 7, 10)) {
            for (int domainSize : List.of(10, 25, 50)) {
                grid.add(dimensions + "x" + domainSize);
            }
        }

        SHAPES.put("CHAIN", grid);
        SHAPES.put("MANY_VALID", grid);
        SHAPES.put("COMPLETE", grid);

        SHAPES.put(
                "DEAD_END",
                grid.stream()
                        .filter(shape -> !shape.equals("10x50"))
                        .toList()
        );

        SHAPES.put(
                "DEEP_DEAD_END",
                List.of("5x10", "5x25", "7x10", "7x25", "10x10")
        );

        SHAPES.put(
                "SMALL_DOMAIN",
                List.of("8x4", "10x4", "12x3", "14x3", "8x5")
        );
    }

    private static final List<String> TOPOLOGIES =
            List.copyOf(SHAPES.keySet());

    private static final List<String> IMPLEMENTATIONS =
            List.of(
                    "PRUNED_BITSET",
                    "PRUNED_DFS",
                    "DP",
                    "ADAPTIVE",
                    "NESTED_STREAM",
                    "IMPERATIVE_ALLOCATING",
                    "IMPERATIVE_REUSE"
            );

    /** Directory (relative to the project root) for result files; it is git-ignored. */
    static final String RESULTS_DIR = "benchmark-results";

    /**
     * Quick preset: a 1-2 minute regression screen for uncommitted code. One fork and short iterations
     * cannot rank close strategies; it only exposes changes of roughly 20% or more.
     */
    private static final List<String> QUICK_SCENARIOS = List.of(
            "CHAIN-5x25",
            "MANY_VALID-5x25",
            "DEAD_END-5x25",
            "DEEP_DEAD_END-5x25",
            "SMALL_DOMAIN-10x4"
    );
    private static final List<String> QUICK_IMPLEMENTATIONS = List.of("PRUNED_BITSET", "DP");

    private BenchmarkCommandGenerator() {
    }

    /**
     * Standalone entry point.
     */
    public static void main(String[] args) {
        // Non-interactive form used by scripts: --print <quick|full> <run-name>
        if (args.length == 3 && args[0].equals("--print")) {
            System.out.println(presetCommand(args[1], args[2]));
            return;
        }
        try (Scanner scanner = new Scanner(System.in)) {
            String command = generateCommand(scanner);

            if (command == null) {
                return;
            }

            System.out.println();
            System.out.println("Generated command");
            System.out.println("-----------------");
            System.out.println(command);
            System.out.println();
            System.out.println(
                    "The command has not been executed. "
                            + "Copy and run it from the project root."
            );
        }
    }

    /**
     * Generates a JMH command using interactive selections.
     *
     * The caller owns the Scanner and is responsible for closing it.
     *
     * @param scanner scanner used for interactive input
     * @return generated JMH command, or null if no valid scenarios exist
     */
    public static String generateCommand(Scanner scanner) {

        System.out.println("==============================================");
        System.out.println(" Conditional Product Benchmark Command Builder");
        System.out.println("==============================================");

        String preset = readPreset(scanner);
        if (!preset.equals("custom")) {
            String command = presetCommand(preset, runName(preset));
            System.out.println();
            System.out.println("Preset: " + preset);
            return command;
        }

        // Select topology first.
        List<String> topologies = select(
                scanner,
                "topology",
                TOPOLOGIES,
                Function.identity()
        );

        // Only show dimensions supported by the selected topologies.
        List<Integer> dimensions = select(
                scanner,
                "dimensions",
                distinct(topologies, 0),
                Object::toString
        );

        // Only show domain sizes supported by the selected topologies.
        List<Integer> domainSizes = select(
                scanner,
                "domain size",
                distinct(topologies, 1),
                Object::toString
        );

        List<String> implementations = select(
                scanner,
                "implementation",
                IMPLEMENTATIONS,
                Function.identity()
        );

        int warmup = readPositiveInt(
                scanner, "Warmup iterations", 5
        );

        int measurement = readPositiveInt(
                scanner, "Measurement iterations", 8
        );

        int forks = readPositiveInt(
                scanner, "Forks", 2
        );

        String profiler = readProfiler(scanner);

        List<String> scenarios = buildScenarios(
                topologies,
                dimensions,
                domainSizes
        );

        if (scenarios.isEmpty()) {
            System.out.println();
            System.out.println(
                    "No valid scenario combinations were selected."
            );
            return null;
        }

        System.out.println();
        System.out.println("Selected configuration");
        System.out.println("----------------------");
        System.out.println(
                "Topologies      : " + String.join(", ", topologies)
        );
        System.out.println(
                "Dimensions      : " + join(dimensions)
        );
        System.out.println(
                "Domain sizes    : " + join(domainSizes)
        );
        System.out.println(
                "Implementations : " + String.join(", ", implementations)
        );
        System.out.println(
                "Valid scenarios : " + scenarios.size()
        );

        System.out.println();
        System.out.println("Scenarios");
        System.out.println("---------");

        scenarios.forEach(System.out::println);

        return buildCommand(
                runName("custom"),
                scenarios,
                implementations,
                warmup,
                measurement,
                forks,
                profiler
        );
    }

    private static String runName(String preset) {
        return preset + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private static String readPreset(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("Preset:");
            System.out.println("  1. quick       (about 1-2 minutes; 5 shapes, PRUNED_BITSET and DP; regression screen only)");
            System.out.println("  2. full-matrix (every topology, shape and implementation; 5 warmup, 8 measurement, 2 forks; long)");
            System.out.println("  3. special-ops (count, isEmpty, lexOrderMth, byRanks, choice, sample; bounded and LARGE workloads)");
            System.out.println("  4. custom      (choose topologies, shapes and implementations)");
            System.out.print("Your choice [4]: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return "quick";
                case "2":
                    return "full";
                case "3":
                    return "special";
                case "", "4":
                    return "custom";
                default:
                    System.out.println("Enter 1, 2, 3 or 4.");
            }
        }
    }

    /**
     * Command for a named preset. The full matrix lists ordinary and adversarial topologies in one
     * benchmark generation, as the decision log requires before any default is revisited.
     */
    static String presetCommand(String preset, String runName) {
        switch (preset) {
            case "quick":
                return buildCommand(runName, QUICK_SCENARIOS, QUICK_IMPLEMENTATIONS, 2, 3, 1, "gc");
            case "full":
                List<String> all = buildScenarios(
                        TOPOLOGIES,
                        distinct(TOPOLOGIES, 0),
                        distinct(TOPOLOGIES, 1));
                return buildCommand(runName, all, IMPLEMENTATIONS, 5, 8, 2, "gc");
            case "special":
                return specialOperationsCommand(runName);
            default:
                throw new IllegalArgumentException("Unknown preset: " + preset);
        }
    }

    /**
     * Special operations are measured by two classes with different parameter sets (bounded workloads with an
     * enumeration baseline, and LARGE library-only workloads), so this preset runs both and writes two result files.
     */
    private static String specialOperationsCommand(String runName) {
        String classpath = shellQuote(System.getProperty("java.class.path"));
        String base = RESULTS_DIR + "/" + runName;
        String options = " -wi 3 -i 5 -f 1 -tu us -bm avgt -prof gc";
        return "mkdir -p " + RESULTS_DIR + "\n"
                + "java -cp " + classpath + " "
                + BenchmarkEnvironment.class.getName() + " > " + base + ".env.txt\n"
                + "java -cp " + classpath + " org.openjdk.jmh.Main '.*ConditionalProductSpecialOperationsBenchmark.*'"
                + options + " -rf json -rff " + base + "-bounded.json 2>&1 | tee " + base + "-bounded.log\n"
                + "java -cp " + classpath + " org.openjdk.jmh.Main '.*ConditionalProductScalingBenchmark.*'"
                + options + " -rf json -rff " + base + "-scaling.json 2>&1 | tee " + base + "-scaling.log";
    }

    /**
     * Returns the distinct sorted dimensions or domain sizes supported
     * by the selected topologies.
     *
     * @param topologies selected topologies
     * @param part 0 for dimensions, 1 for domain sizes
     */
    private static List<Integer> distinct(
            List<String> topologies,
            int part) {

        return topologies.stream()
                .map(SHAPES::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .map(shape -> Integer.parseInt(shape.split("x")[part]))
                .distinct()
                .sorted()
                .toList();
    }

    private static <T> List<T> select(
            Scanner scanner,
            String label,
            List<T> options,
            Function<T, String> display) {

        while (true) {
            System.out.println();
            System.out.println("Select " + label + ":");

            for (int i = 0; i < options.size(); i++) {
                System.out.printf(
                        "  %d. %s%n",
                        i + 1,
                        display.apply(options.get(i))
                );
            }

            System.out.printf(
                    "  %d. All%n",
                    options.size() + 1
            );

            System.out.print(
                    "Your choice (comma-separated, e.g. 1,3): "
            );

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("all")
                    || input.equals(Integer.toString(options.size() + 1))) {
                return List.copyOf(options);
            }

            try {
                Set<Integer> selectedIndexes =
                        parseIndexes(input, options.size());

                List<T> selected =
                        new ArrayList<>(selectedIndexes.size());

                for (int index : selectedIndexes) {
                    selected.add(options.get(index - 1));
                }

                return List.copyOf(selected);

            } catch (IllegalArgumentException exception) {
                System.out.println(
                        "Invalid selection: " + exception.getMessage()
                                + " Please enter valid option numbers separated by commas."
                );
            }
        }
    }

    private static Set<Integer> parseIndexes(
            String input,
            int optionCount) {

        if (input.isBlank()) {
            throw new IllegalArgumentException(
                    "selection cannot be empty."
            );
        }

        Set<Integer> indexes = new LinkedHashSet<>();

        for (String token : input.split(",", -1)) {
            String value = token.trim();

            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "empty option between commas."
                );
            }

            final int index;

            try {
                index = Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "'" + value + "' is not a number."
                );
            }

            if (index < 1 || index > optionCount) {
                throw new IllegalArgumentException(
                        "option " + index + " is out of range."
                );
            }

            if (!indexes.add(index)) {
                throw new IllegalArgumentException(
                        "option " + index + " was selected more than once."
                );
            }
        }

        return indexes;
    }

    private static List<String> buildScenarios(
            List<String> topologies,
            List<Integer> dimensions,
            List<Integer> domainSizes) {

        List<String> scenarios = new ArrayList<>();

        for (String topology : topologies) {
            for (String shape : SHAPES.get(topology)) {

                String[] parts = shape.split("x");

                int dimension = Integer.parseInt(parts[0]);
                int domainSize = Integer.parseInt(parts[1]);

                if (dimensions.contains(dimension)
                        && domainSizes.contains(domainSize)) {

                    scenarios.add(topology + "-" + shape);
                }
            }
        }

        return scenarios;
    }

    private static String buildCommand(
            String runName,
            List<String> scenarios,
            List<String> implementations,
            int warmup,
            int measurement,
            int forks,
            String profiler) {

        // Reuse the classpath of the JVM running this generator.
        // This supports both terminal and IntelliJ execution.
        String classpath = shellQuote(System.getProperty("java.class.path"));
        String base = RESULTS_DIR + "/" + runName;

        // 1) environment record, 2) JMH with JSON output, full console output kept in a log.
        // Result files are git-ignored; only the final summary table is committed.
        return "mkdir -p " + RESULTS_DIR + "\n"
                + "java -cp " + classpath + " "
                + BenchmarkEnvironment.class.getName() + " > " + base + ".env.txt\n"
                + "java -cp " + classpath + " \\\n"
                + "  org.openjdk.jmh.Main " + BENCHMARK_REGEX + " \\\n"
                + "  -p scenario=" + String.join(",", scenarios) + " \\\n"
                + "  -p implementation="
                + String.join(",", implementations) + " \\\n"
                + "  -wi " + warmup
                + " -i " + measurement
                + " -f " + forks
                + " -tu ms -bm avgt"
                + (profiler.equals("none") ? "" : " -prof " + profiler)
                + " \\\n  -rf json -rff " + base + ".json"
                + " 2>&1 | tee " + base + ".log";
    }

    private static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private static int readPositiveInt(
            Scanner scanner,
            String label,
            int defaultValue) {

        while (true) {
            System.out.printf(
                    "%s [%d]: ",
                    label,
                    defaultValue
            );

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

            String value = scanner.nextLine()
                    .trim()
                    .toLowerCase(Locale.ROOT);

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
        return values.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
