
package io.github.deepeshpatel.jnumbertools.benchmarks.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class BenchmarkCommandExecutor {

    private BenchmarkCommandExecutor() {
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String command = obtainCommand(scanner);

            if (command == null || command.isBlank()) {
                System.out.println("No command to execute.");
                return;
            }

            System.out.println();
            System.out.println("Executing command: " + command);
            System.out.println("----------------------------------------------");

            execute(command);

        } catch (IOException e) {
            System.err.println("Unable to execute benchmark: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Benchmark execution interrupted.");
        }
    }

    private static String obtainCommand(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("==============================================");
            System.out.println("          JMH Benchmark Command Executor");
            System.out.println("==============================================");
            System.out.println("1. Generate command interactively");
            System.out.println("2. Paste an existing command");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    return BenchmarkCommandGenerator.generateCommand(scanner);

                case "2":
                    return readCommand(scanner);

                case "0":
                    return null;

                default:
                    System.out.println("Invalid choice. Please select 0, 1, or 2.");
            }
        }
    }

    private static String readCommand(Scanner scanner) {
        System.out.println();
        System.out.println("Paste your JMH command below.");
        System.out.println("For a multiline command, press Enter on an empty line when finished.");
        System.out.println();

        List<String> lines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.isEmpty()) {
                break;
            }

            lines.add(line);
        }

        return String.join(System.lineSeparator(), lines).trim();
    }

    private static void execute(String command)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/bash", "-lc", command
        );

        // IntelliJ's working directory should be the project root.
        processBuilder.directory(
                new java.io.File(System.getProperty("user.dir"))
        );

        // Stream JMH output directly to the IntelliJ Run console.
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        System.out.println();
        System.out.println("----------------------------------------------");

        if (exitCode == 0) {
            System.out.println("JMH execution completed successfully.");
        } else {
            System.err.println("JMH execution failed. Exit code: " + exitCode);
        }
    }
}
