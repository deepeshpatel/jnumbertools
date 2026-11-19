package io.github.deepeshpatel.jnumbertools.benchmarks.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Prints the environment a benchmark run executed in (commit, JDK, JVM arguments, hardware) as
 * {@code key=value} lines, so results can be compared later only when the setups are comparable.
 *
 * <p>Run with the same classpath as the benchmark. JMH's own JSON adds the JMH version, fork and
 * iteration settings. Forked benchmark JVMs use JMH's JVM arguments, which are recorded in that JSON.</p>
 */
public final class BenchmarkEnvironment {

    private BenchmarkEnvironment() {
    }

    public static void main(String[] args) {
        line("timestamp", OffsetDateTime.now().toString());
        line("git.commit", git("rev-parse", "HEAD"));
        line("git.branch", git("rev-parse", "--abbrev-ref", "HEAD"));
        String dirty = git("status", "--porcelain");
        line("git.uncommitted", dirty.isEmpty() ? "no" : "yes (" + dirty.lines().count() + " files)");
        line("java.version", System.getProperty("java.version"));
        line("java.vendor", System.getProperty("java.vendor"));
        line("jvm", System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
        line("jvm.args", String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
        line("os", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        line("cpu.model", cpuModel());
        line("cpu.logical", Integer.toString(Runtime.getRuntime().availableProcessors()));
        line("memory.physical.mb", physicalMemoryMb());
        line("heap.max.mb", Long.toString(Runtime.getRuntime().maxMemory() / (1024 * 1024)));
    }

    private static void line(String key, String value) {
        System.out.println(key + "=" + value);
    }

    private static String physicalMemoryMb() {
        try {
            var os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sun) {
                return Long.toString(sun.getTotalMemorySize() / (1024 * 1024));
            }
        } catch (RuntimeException | LinkageError ignored) {
            // fall through
        }
        return "unknown";
    }

    private static String cpuModel() {
        Path cpuinfo = Path.of("/proc/cpuinfo");
        try {
            if (Files.isReadable(cpuinfo)) {
                for (String l : Files.readAllLines(cpuinfo)) {
                    if (l.startsWith("model name")) {
                        return l.substring(l.indexOf(':') + 1).trim();
                    }
                }
            }
        } catch (IOException ignored) {
            // fall through
        }
        String mac = run(List.of("sysctl", "-n", "machdep.cpu.brand_string"));
        return mac.isEmpty() ? "unknown" : mac;
    }

    private static String git(String... args) {
        List<String> command = new java.util.ArrayList<>();
        command.add("git");
        command.addAll(List.of(args));
        String out = run(command);
        return out.isEmpty() && !args[0].equals("status") ? "unknown" : out;
    }

    private static String run(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String l;
                while ((l = r.readLine()) != null) {
                    if (sb.length() > 0) sb.append('\n');
                    sb.append(l);
                }
            }
            if (!process.waitFor(10, TimeUnit.SECONDS) || process.exitValue() != 0) {
                return "";
            }
            return sb.toString().trim();
        } catch (IOException e) {
            return "";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }
}
