import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Turns JMH JSON output into README-ready tables. Single-file program: no dependencies, JDK 17+.
 *
 *   java scripts/BenchmarkReport.java report  results.json [results.env.txt]
 *   java scripts/BenchmarkReport.java compare base.json local.json [label-base label-local]
 *
 * report : per scenario the fastest implementation, speedup over the NESTED_STREAM baseline and over the next-best
 *          builder strategy, allocation per operation versus the baseline, and a flag where the score error exceeds
 *          10% of the score. Also prints the environment block.
 * compare: per scenario and implementation, base versus local with a verdict. Differences under 10% are "same";
 *          larger differences whose error bars overlap are "inconclusive".
 */
public final class BenchmarkReport {
    static final String BASELINE = "NESTED_STREAM";
    static final String IMPERATIVE_REUSE = "IMPERATIVE_REUSE";

    /** Hand-written baselines (not library strategies). */
    static boolean isBaseline(String impl) {
        return impl.equals(BASELINE) || impl.equals("ENUMERATION") || impl.startsWith("IMPERATIVE_");
    }
    static final double SAME = 0.10;
    static final double ERR_FLAG = 0.10;

    record Row(String scenario, String impl, double score, double err, String unit, double alloc) {
        boolean noisy() { return score > 0 && err / score > ERR_FLAG; }
    }

    record Run(List<Row> rows, Map<String, String> meta) { }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) usage();
        switch (args[0]) {
            case "report" -> report(load(Path.of(args[1])), args.length > 2 ? Path.of(args[2]) : null);
            case "compare" -> {
                if (args.length < 3) usage();
                compare(load(Path.of(args[1])), load(Path.of(args[2])),
                        args.length > 3 ? args[3] : "base", args.length > 4 ? args[4] : "local");
            }
            default -> usage();
        }
    }

    static void usage() {
        System.err.println("usage: BenchmarkReport report <json> [env.txt] | compare <base.json> <local.json> [labelBase labelLocal]");
        System.exit(2);
    }

    // ---------------------------------------------------------------- loading

    @SuppressWarnings("unchecked")
    static Run load(Path file) throws Exception {
        Object root = new Json(Files.readString(file)).parse();
        List<Object> entries = (List<Object>) root;
        List<Row> rows = new ArrayList<>();
        Map<String, String> meta = new LinkedHashMap<>();
        for (Object o : entries) {
            Map<String, Object> e = (Map<String, Object>) o;
            Map<String, Object> params = (Map<String, Object>) e.getOrDefault("params", Map.of());
            Map<String, Object> pm = (Map<String, Object>) e.get("primaryMetric");
            if (pm == null) continue;
            meta.putIfAbsent("jmhVersion", str(e.get("jmhVersion")));
            meta.putIfAbsent("jdkVersion", str(e.get("jdkVersion")));
            meta.putIfAbsent("vm", str(e.get("vmName")) + " " + str(e.get("vmVersion")));
            meta.putIfAbsent("jvmArgs", str(e.get("jvmArgs")));
            meta.putIfAbsent("forks", str(e.get("forks")));
            meta.putIfAbsent("warmup", str(e.get("warmupIterations")) + " x " + str(e.get("warmupTime")));
            meta.putIfAbsent("measurement", str(e.get("measurementIterations")) + " x " + str(e.get("measurementTime")));
            double alloc = Double.NaN;
            Map<String, Object> sec = (Map<String, Object>) e.getOrDefault("secondaryMetrics", Map.of());
            for (var m : sec.entrySet()) {
                if (m.getKey().endsWith("gc.alloc.rate.norm")) {
                    alloc = ((Number) ((Map<String, Object>) m.getValue()).get("score")).doubleValue();
                }
            }
            double err = pm.get("scoreError") instanceof Number n && !Double.isNaN(n.doubleValue()) ? n.doubleValue() : 0;
            String scenarioLabel = str(params.get("scenario"))
                    + (params.get("operation") != null ? " / " + str(params.get("operation")) : "");
            String impl = params.get("implementation") != null ? str(params.get("implementation")) : "LIBRARY";
            rows.add(new Row(scenarioLabel, impl,
                    ((Number) pm.get("score")).doubleValue(), err, str(pm.get("scoreUnit")), alloc));
        }
        if (rows.isEmpty()) throw new IllegalStateException("No benchmark results in " + file);
        return new Run(rows, meta);
    }

    static String str(Object o) {
        if (o == null) return "?";
        if (o instanceof Double d && d == Math.rint(d) && !d.isInfinite()) return Long.toString(d.longValue());
        return String.valueOf(o);
    }

    static Map<String, List<Row>> byScenario(Run run) {
        Map<String, List<Row>> m = new LinkedHashMap<>();
        for (Row r : run.rows) m.computeIfAbsent(r.scenario, k -> new ArrayList<>()).add(r);
        return m;
    }

    // ----------------------------------------------------------------- report

    static void report(Run run, Path env) throws Exception {
        System.out.println("### Environment\n");
        run.meta.forEach((k, v) -> System.out.println("- " + k + ": " + v));
        if (env != null && Files.exists(env)) {
            for (String l : Files.readAllLines(env)) if (!l.isBlank()) System.out.println("- " + l.replace("=", ": "));
        }
        System.out.println("\n### Results\n");
        System.out.println("| Scenario | Fastest | Score | Baseline | Speedup vs baseline | Imperative (reuse) vs best builder | Next-best builder | Alloc B/op | Alloc vs baseline | Noisy (err > 10%) |");
        System.out.println("|---|---|---:|---:|---:|---:|---|---:|---:|---|");
        for (var e : byScenario(run).entrySet()) {
            List<Row> rows = e.getValue();
            List<Row> builders = rows.stream().filter(r -> !isBaseline(r.impl))
                    .sorted(Comparator.comparingDouble(Row::score)).toList();
            Row stream = rows.stream().filter(r -> r.impl.equals(BASELINE) || r.impl.equals("ENUMERATION")).findFirst().orElse(null);
            Row fastest = rows.stream().min(Comparator.comparingDouble(Row::score)).get();
            Row imperative = rows.stream().filter(r -> r.impl.equals(IMPERATIVE_REUSE)).findFirst().orElse(null);
            Row best = builders.isEmpty() ? null : builders.get(0);
            String next = builders.size() > 1
                    ? builders.get(1).impl + " (" + fmt(builders.get(1).score / best.score) + "x slower)" : "-";
            String noisy = String.join(", ", rows.stream().filter(Row::noisy).map(r -> r.impl).toList());
            System.out.printf("| %s | %s | %s %s | %s | %s | %s | %s | %s | %s | %s |%n",
                    e.getKey(), fastest.impl, fmt(fastest.score), fastest.unit,
                    stream == null ? "-" : fmt(stream.score),
                    stream == null || best == null ? "-" : fmt(stream.score / best.score) + "x",
                    imperative == null || best == null ? "-" : fmt(imperative.score / best.score) + "x",
                    next,
                    best == null || Double.isNaN(best.alloc) ? "-" : fmt(best.alloc),
                    stream == null || best == null || Double.isNaN(best.alloc) || Double.isNaN(stream.alloc) || stream.alloc == 0
                            ? "-" : fmt(best.alloc / stream.alloc) + "x",
                    noisy.isEmpty() ? "" : noisy);
        }
        System.out.println("\nSpeedup and allocation compare the fastest library strategy with the " + BASELINE
                + " (or, for special operations, ENUMERATION) baseline; the imperative column is IMPERATIVE_REUSE time divided by the best library time (above 1x the "
                + "library is faster, below 1x the hand-written loop wins). Scores are single-run averages; treat differences inside the error bars as ties.");
    }

    // ---------------------------------------------------------------- compare

    static void compare(Run base, Run local, String lb, String ll) {
        System.out.println("### Environment\n");
        System.out.println("- " + lb + ": JMH " + base.meta.get("jmhVersion") + ", JDK " + base.meta.get("jdkVersion")
                + ", forks " + base.meta.get("forks") + ", warmup " + base.meta.get("warmup")
                + ", measurement " + base.meta.get("measurement"));
        System.out.println("- " + ll + ": JMH " + local.meta.get("jmhVersion") + ", JDK " + local.meta.get("jdkVersion")
                + ", forks " + local.meta.get("forks") + ", warmup " + local.meta.get("warmup")
                + ", measurement " + local.meta.get("measurement"));
        System.out.println("\n### " + lb + " versus " + ll + "\n");
        System.out.println("| Scenario | Implementation | " + lb + " | " + ll + " | Ratio (" + ll + "/" + lb + ") | Verdict |");
        System.out.println("|---|---|---:|---:|---:|---|");
        Map<String, Row> baseRows = new LinkedHashMap<>();
        for (Row r : base.rows) baseRows.put(r.scenario + "|" + r.impl, r);
        int faster = 0, slower = 0, same = 0, inconclusive = 0;
        List<String> slowerList = new ArrayList<>();
        for (Row l : local.rows) {
            Row b = baseRows.get(l.scenario + "|" + l.impl);
            if (b == null) continue;
            if (!b.unit.equals(l.unit)) throw new IllegalStateException("Different units for " + l.scenario);
            double ratio = l.score / b.score;
            String verdict;
            if (Math.abs(ratio - 1) <= SAME) { verdict = "same (within 10%)"; same++; }
            else if (Math.abs(l.score - b.score) <= l.err + b.err) { verdict = "inconclusive (errors overlap)"; inconclusive++; }
            else if (ratio < 1) { verdict = "FASTER"; faster++; }
            else { verdict = "SLOWER"; slower++; slowerList.add(l.scenario + " " + l.impl); }
            System.out.printf("| %s | %s | %s %s | %s %s | %sx | %s |%n", l.scenario, l.impl,
                    fmt(b.score), b.unit, fmt(l.score), l.unit, fmt(ratio), verdict);
        }
        System.out.printf("%nSummary: %d faster, %d slower, %d same, %d inconclusive.%n", faster, slower, same, inconclusive);
        if (!slowerList.isEmpty()) System.out.println("Slower: " + String.join("; ", slowerList));
        System.out.println("A short run only exposes large changes. \"No regression found\" is not \"proven faster\"; "
                + "rerun a flagged or inconclusive shape alone with more iterations before deciding.");
    }

    static String fmt(double v) {
        if (Double.isNaN(v)) return "-";
        if (v >= 1000) return String.format(Locale.ROOT, "%,.0f", v);
        if (v >= 10) return String.format(Locale.ROOT, "%.1f", v);
        if (v >= 0.1) return String.format(Locale.ROOT, "%.2f", v);
        return String.format(Locale.ROOT, "%.4f", v);
    }

    // ------------------------------------------------------------ tiny JSON

    static final class Json {
        private final String s;
        private int i;
        Json(String s) { this.s = s; }

        Object parse() { Object v = value(); ws(); return v; }

        private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

        private Object value() {
            ws();
            char c = s.charAt(i);
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            return number();
        }

        private Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; ws();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                ws(); String k = string(); ws(); i++; // ':'
                m.put(k, value()); ws();
                if (s.charAt(i++) == '}') return m;
            }
        }

        private List<Object> array() {
            List<Object> l = new ArrayList<>();
            i++; ws();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                l.add(value()); ws();
                if (s.charAt(i++) == ']') return l;
            }
        }

        private String string() {
            StringBuilder sb = new StringBuilder();
            i++; // opening quote
            while (s.charAt(i) != '"') {
                char c = s.charAt(i++);
                if (c == '\\') {
                    char n = s.charAt(i++);
                    switch (n) {
                        case 'n' -> sb.append('\n'); case 't' -> sb.append('\t'); case 'r' -> sb.append('\r');
                        case 'b' -> sb.append('\b'); case 'f' -> sb.append('\f');
                        case 'u' -> { sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                        default -> sb.append(n);
                    }
                } else sb.append(c);
            }
            i++;
            return sb.toString();
        }

        private Number number() {
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String t = s.substring(st, i);
            if (t.equals("NaN")) return Double.NaN;
            return Double.parseDouble(t);
        }
    }
}
