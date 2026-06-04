package io.github.deepeshpatel.jnumbertools.numbersystem.derangadic.experiments;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

public final class DerangadicCarryCounterDPPersistent {

    private static final String DEFAULT_FILE = "resources/cached/derangadic-carry-counts.txt";
    private final Path filePath;
    private final DerangadicCarryCounterDP dp;
    private final TreeMap<Integer, BigInteger[]> cache = new TreeMap<>();

    public DerangadicCarryCounterDPPersistent() {
        this(Paths.get(DEFAULT_FILE));
    }

    public DerangadicCarryCounterDPPersistent(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath);
        this.dp = new DerangadicCarryCounterDP();
        loadFromFile();
    }

    public BigInteger[] carryDistribution(int n) {
        if (cache.containsKey(n)) return cache.get(n);

        int start = (n % 2 == 0) ? 2 : 3;
        for (int k = start; k <= n; k += 2) {
            if (!cache.containsKey(k)) {
                computeAndSave(k);
            }
        }
        return cache.get(n);
    }

    private void computeAndSave(int n) {
        System.out.printf("Computing n=%d ...%n", n);
        long t0 = System.currentTimeMillis();

        BigInteger[] dist = dp.carryDistribution(n);

        long ms = System.currentTimeMillis() - t0;
        System.out.printf("  done in %,d ms%n", ms);

        cache.put(n, dist);
        appendToFile(n, dist);
    }

    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            System.out.println("No snapshot file — starting fresh.");
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                int n = Integer.parseInt(parts[0]);
                BigInteger[] dist = new BigInteger[n + 1];
                Arrays.fill(dist, BigInteger.ZERO);
                for (int L = 2; L <= n; L++) {
                    dist[L] = new BigInteger(parts[L - 1]);
                }
                cache.put(n, dist);
            }
        } catch (Exception e) {
            System.err.println("Warning: " + e.getMessage());
        }
    }

    private void appendToFile(int n, BigInteger[] dist) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            StringBuilder sb = new StringBuilder();
            sb.append(n);
            for (int L = 2; L <= n; L++) {
                sb.append(' ').append(dist[L]);
            }
            bw.write(sb.toString());
            bw.newLine();
            System.out.println("  Saved n=" + n);
        } catch (IOException e) {
            System.err.println("Write warning: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int maxN = args.length > 0 ? Integer.parseInt(args[0]) : 25;
        Path file = args.length > 1 ? Paths.get(args[1]) : Paths.get(DEFAULT_FILE);

        var counter = new DerangadicCarryCounterDPPersistent(file);
        System.out.printf("Target up to n=%d | file: %s%n%n", maxN, file.toAbsolutePath());

        for (int p = 0; p <= 1; p++) {
            int start = (p == 0) ? 2 : 3;
            for (int n = start; n <= maxN; n += 2) {
                if (counter.cache.containsKey(n)) {
                    System.out.println("n=" + n + " already cached.");
                    continue;
                }
                try {
                    counter.computeAndSave(n);
                } catch (OutOfMemoryError e) {
                    System.err.println("OOM at n=" + n + ". Try -Xmx28g or higher.");
                    return;
                }
            }
        }
        System.out.println("Done up to n=" + maxN);
    }
}