package io.github.deepeshpatel.jnumbertools.experiments.permutadic;

import org.junit.jupiter.api.Disabled;
import io.github.deepeshpatel.jnumbertools.datastructure.FenwickTree;
import io.github.deepeshpatel.jnumbertools.numbersystem.PermutadicAlgorithms;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

/**
 * Screening benchmark for the k-permutation paths of {@link PermutadicAlgorithms}:
 * {@code rank}, {@code mthPermutationToPermutadic} and {@code unRank}.
 *
 * <p>For a grid of set sizes s and ratios k²/s it times the <b>library method as it is today</b> and three
 * reference algorithms kept in this file: sparse O(k²), Fenwick (O(s + k log s)) and the pool-based one
 * (ArrayList insertion/removal, O(s·k)). All variants are checked to give identical results before anything is
 * timed. The table shows where each approach wins and the summary lists, per operation and set size, the
 * largest k²/s for which sparse still beats the best alternative; that is the value to use for the switch
 * in {@code PermutadicAlgorithms.useSparse}.
 *
 * <p>Run from the project root:
 * <pre>
 *   mvn -DskipTests test-compile
 *   java -cp target/classes:target/test-classes \
 *     io.github.deepeshpatel.jnumbertools.experiments.permutadic.OptimizationBenchmark [--quick]
 * </pre>
 * Use ';' as the classpath separator on Windows. {@code --quick} skips the 1,000,000-element sets;
 * {@code --sizes=200,500,1000} runs exactly those set sizes.
 *
 * <p>This is a dependency-free screening harness (time-based warm-up, median of several batches), not a
 * substitute for JMH. Run it twice on an otherwise idle machine; results depend on the JVM and CPU, so
 * re-measure before changing the switch on a different platform.
 */
@Disabled("Experiment or benchmark harness, not a correctness test; run manually")
public final class OptimizationBenchmark {

    private static final double WARMUP_MILLIS = 120;
    private static final double BATCH_MILLIS = 12;
    private static final int BATCHES = 7;
    /** Skip the pool variant when s*k exceeds this: it is O(s*k) and would dominate the run time. */
    private static final long POOL_LIMIT = 150_000_000L;

    private static long blackHole;

    private OptimizationBenchmark() {
    }

    public static void main(String[] args) {
        boolean quick = Arrays.asList(args).contains("--quick");
        int[] sizes = quick ? new int[]{2_000, 8_000, 20_000, 100_000} : new int[]{2_000, 8_000, 20_000, 100_000, 1_000_000};
        for (String arg : args) {
            if (arg.startsWith("--sizes=")) {
                sizes = Arrays.stream(arg.substring("--sizes=".length()).split(",")).mapToInt(Integer::parseInt).toArray();
            }
        }
        double[] ratios = {0.25, 0.5, 1, 2, 4, 8, 16};

        System.out.println("PermutadicAlgorithms benchmark | Java " + System.getProperty("java.version")
                + " | median of " + BATCHES + " batches, microseconds per call\n");

        Map<String, Map<Integer, List<double[]>>> summary = new LinkedHashMap<>();
        for (String operation : new String[]{"rank", "toPermutadic (mthPermutationToPermutadic)", "unRank"}) {
            System.out.println("== " + operation);
            System.out.printf(Locale.ROOT, "%-9s %-6s %-7s %10s %10s %10s %10s   %s%n",
                    "s", "k", "k^2/s", "library", "sparse", "fenwick", "pool", "winner (library -> best)");
            for (int s : sizes) {
                for (double ratio : ratios) {
                    int k = (int) Math.min(s, Math.round(Math.sqrt(ratio * s)));
                    if (k < 1) {
                        continue;
                    }
                    double[] t = measure(operation, s, k);
                    double best = Math.min(t[1], Math.min(t[2], Double.isNaN(t[3]) ? Double.MAX_VALUE : t[3]));
                    String winner = t[1] == best ? "sparse" : t[2] == best ? "fenwick" : "pool";
                    System.out.printf(Locale.ROOT, "%-9d %-6d %-7.2f %10.2f %10.2f %10.2f %10s   %s (library %.2fx of best)%n",
                            s, k, (double) k * k / s, t[0], t[1], t[2],
                            Double.isNaN(t[3]) ? "-" : String.format(Locale.ROOT, "%.2f", t[3]), winner, t[0] / best);
                    double others = Math.min(t[2], Double.isNaN(t[3]) ? Double.MAX_VALUE : t[3]);
                    summary.computeIfAbsent(operation, o -> new LinkedHashMap<>())
                            .computeIfAbsent(s, x -> new ArrayList<>())
                            .add(new double[]{(double) k * k / s, t[1] <= others ? 1 : 0});
                }
                System.out.println();
            }
        }

        System.out.println("== Summary: largest k^2/s up to which sparse beats the best alternative at every smaller ratio");
        for (var operation : summary.entrySet()) {
            for (var size : operation.getValue().entrySet()) {
                double crossover = 0;
                for (double[] point : size.getValue()) {
                    if (point[1] == 1) {
                        crossover = point[0];
                    } else {
                        break;
                    }
                }
                System.out.printf(Locale.ROOT, "  %-44s s=%-8d sparse wins up to k^2/s = %s%n", operation.getKey(), size.getKey(),
                        crossover == 0 ? "(never at the tested ratios)" : String.format(Locale.ROOT, "%.2f", crossover));
            }
        }
        System.out.println("\nBlack hole: " + blackHole);
        System.out.println("Run at least twice on an otherwise idle machine; these are screening measurements, not JMH results.");
    }

    // ------------------------------------------------------------------ measurement

    /** Returns {library, sparse, fenwick, pool (NaN when skipped)} in microseconds per call. */
    private static double[] measure(String operation, int s, int k) {
        int[] permutation = makePermutation(s, k);
        BigInteger rank = PermutadicAlgorithms.rank(s, permutation);
        boolean withPool = (long) s * k <= POOL_LIMIT;

        Supplier<Object> library, sparse, fenwick, pool;
        switch (operation) {
            case "rank" -> {
                library = () -> PermutadicAlgorithms.rank(s, permutation);
                sparse = () -> rankSparse(permutation, s);
                fenwick = () -> rankFenwick(permutation, s);
                pool = () -> rankPool(permutation, s);
            }
            case "unRank" -> {
                library = () -> PermutadicAlgorithms.unRank(rank, s, k);
                sparse = () -> unRankSparse(rank, s, k);
                fenwick = () -> unRankFenwick(rank, s, k);
                pool = () -> unRankPool(rank, s, k);
            }
            default -> {
                library = () -> PermutadicAlgorithms.mthPermutationToPermutadic(permutation, s - k);
                sparse = () -> digitsSparse(permutation);
                fenwick = () -> digitsFenwick(permutation, s);
                pool = () -> digitsPool(permutation, s);
            }
        }

        Object expected = library.get();
        check(expected, sparse.get(), "sparse", s, k);
        check(expected, fenwick.get(), "fenwick", s, k);
        if (withPool) {
            check(expected, pool.get(), "pool", s, k);
        }
        return new double[]{micros(library), micros(sparse), micros(fenwick), withPool ? micros(pool) : Double.NaN};
    }

    private static void check(Object expected, Object actual, String name, int s, int k) {
        boolean equal = expected instanceof int[] a && actual instanceof int[] b ? Arrays.equals(a, b) : expected.equals(actual);
        if (!equal) {
            throw new AssertionError(name + " differs from the library for s=" + s + ", k=" + k);
        }
    }

    /** Median microseconds per call: time-based warm-up, then batches sized to about BATCH_MILLIS. */
    private static double micros(Supplier<Object> task) {
        long warmupEnd = System.nanoTime() + (long) (WARMUP_MILLIS * 1e6);
        long calls = 0;
        long start = System.nanoTime();
        while (System.nanoTime() < warmupEnd || calls < 20) {
            consume(task.get());
            calls++;
        }
        double perCallNanos = (System.nanoTime() - start) / (double) calls;
        int batchCalls = (int) Math.max(1, Math.min(1_000_000, BATCH_MILLIS * 1e6 / perCallNanos));

        double[] samples = new double[BATCHES];
        for (int b = 0; b < BATCHES; b++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < batchCalls; i++) {
                consume(task.get());
            }
            samples[b] = (System.nanoTime() - t0) / 1e3 / batchCalls;
        }
        Arrays.sort(samples);
        return samples[BATCHES / 2];
    }

    private static void consume(Object result) {
        if (result instanceof int[] a) {
            blackHole += a.length == 0 ? 0 : a[a.length - 1];
        } else if (result instanceof List<?> l) {
            blackHole += l.size();
        } else {
            blackHole += result.hashCode();
        }
    }

    /** A deterministic valid k-permutation of [0, s). */
    private static int[] makePermutation(int s, int k) {
        int[] values = new int[s];
        for (int i = 0; i < s; i++) {
            values[i] = i;
        }
        Random random = new Random(123456789L + s + k);
        for (int i = 0; i < k; i++) {
            int j = i + random.nextInt(s - i);
            int t = values[i];
            values[i] = values[j];
            values[j] = t;
        }
        return Arrays.copyOf(values, k);
    }

    // ------------------------------------------------------------------ reference algorithms: rank

    private static BigInteger accumulate(int[] digitsFromLast, int degree) {
        BigInteger rank = BigInteger.ZERO;
        BigInteger place = BigInteger.ONE;
        int radix = degree;
        for (int digit : digitsFromLast) {
            rank = rank.add(place.multiply(BigInteger.valueOf(digit)));
            radix++;
            place = place.multiply(BigInteger.valueOf(radix));
        }
        return rank;
    }

    private static BigInteger rankSparse(int[] p, int s) {
        return accumulate(digitsSparseArray(p), s - p.length);
    }

    private static BigInteger rankFenwick(int[] p, int s) {
        return accumulate(digitsFenwickArray(p, s), s - p.length);
    }

    private static BigInteger rankPool(int[] p, int s) {
        return accumulate(digitsPoolArray(p, s), s - p.length);
    }

    // ------------------------------------------------------------------ reference algorithms: digits of a permutation

    private static int[] digitsSparseArray(int[] p) {
        int[] digits = new int[p.length];
        int at = 0;
        for (int i = p.length - 1; i >= 0; i--) {
            int smaller = 0;
            for (int x = 0; x < i; x++) {
                if (p[x] < p[i]) {
                    smaller++;
                }
            }
            digits[at++] = p[i] - smaller;
        }
        return digits;
    }

    private static int[] digitsFenwickArray(int[] p, int s) {
        FenwickTree available = FenwickTree.ofOnes(s);
        for (int value : p) {
            available.update(value + 1, -1);
        }
        int[] digits = new int[p.length];
        int at = 0;
        for (int i = p.length - 1; i >= 0; i--) {
            digits[at++] = available.rsq(p[i]);
            available.update(p[i] + 1, 1);
        }
        return digits;
    }

    private static int[] digitsPoolArray(int[] p, int s) {
        Set<Integer> chosen = new HashSet<>(p.length);
        for (int v : p) {
            chosen.add(v);
        }
        List<Integer> pool = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            if (!chosen.contains(i)) {
                pool.add(i);
            }
        }
        int[] digits = new int[p.length];
        int at = 0;
        for (int i = p.length - 1; i >= 0; i--) {
            int index = -(Collections.binarySearch(pool, p[i]) + 1);
            pool.add(index, p[i]);
            digits[at++] = index;
        }
        return digits;
    }

    private static List<Integer> asList(int[] digits) {
        List<Integer> list = new ArrayList<>(digits.length);
        for (int d : digits) {
            list.add(d);
        }
        return List.copyOf(list);
    }

    private static List<Integer> digitsSparse(int[] p) {
        return asList(digitsSparseArray(p));
    }

    private static List<Integer> digitsFenwick(int[] p, int s) {
        return asList(digitsFenwickArray(p, s));
    }

    private static List<Integer> digitsPool(int[] p, int s) {
        return asList(digitsPoolArray(p, s));
    }

    // ------------------------------------------------------------------ reference algorithms: unrank

    private static int[] permutadicDigits(BigInteger rank, int s, int k) {
        int degree = s - k;
        int[] digit = new int[k];
        BigInteger quotient = rank;
        for (int j = 0; j < k && quotient.signum() > 0; j++) {
            BigInteger[] dr = quotient.divideAndRemainder(BigInteger.valueOf(degree + 1L + j));
            digit[j] = dr[1].intValue();
            quotient = dr[0];
        }
        return digit;
    }

    private static int[] unRankSparse(BigInteger rank, int s, int k) {
        int[] digit = permutadicDigits(rank, s, k);
        int[] result = new int[k];
        int[] chosen = new int[k];
        int m = 0;
        for (int position = 0; position < k; position++) {
            int value = digit[k - 1 - position];
            for (int i = 0; i < m && chosen[i] <= value; i++) {
                value++;
            }
            result[position] = value;
            int at = m;
            while (at > 0 && chosen[at - 1] > value) {
                chosen[at] = chosen[at - 1];
                at--;
            }
            chosen[at] = value;
            m++;
        }
        return result;
    }

    private static int[] unRankFenwick(BigInteger rank, int s, int k) {
        int[] digit = permutadicDigits(rank, s, k);
        FenwickTree available = FenwickTree.ofOnes(s);
        int[] result = new int[k];
        for (int position = 0; position < k; position++) {
            int at = available.findKth(digit[k - 1 - position] + 1);
            result[position] = at - 1;
            available.update(at, -1);
        }
        return result;
    }

    private static int[] unRankPool(BigInteger rank, int s, int k) {
        int[] digit = permutadicDigits(rank, s, k);
        List<Integer> pool = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            pool.add(i);
        }
        int[] result = new int[k];
        for (int position = 0; position < k; position++) {
            result[position] = pool.remove(digit[k - 1 - position]);
        }
        return result;
    }
}
