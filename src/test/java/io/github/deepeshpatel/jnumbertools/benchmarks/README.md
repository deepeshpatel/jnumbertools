# Benchmarks

JMH benchmarks for JNumberTools maintainers. These classes live under `src/test/java`; they are
development tools and are not included in the published library.

## Benchmark design rules

1. Compare each library API implementation with an equivalent Java Stream implementation whenever
   the operation can reasonably be expressed with streams.
2. Compare equivalent workloads and consume equivalent results. Validate result counts and a checksum
   before interpreting performance numbers.
3. Include setup and construction in an end-to-end benchmark when those costs are part of normal use.
   Add a separate traversal-only benchmark when isolating iteration is useful.
4. Record JDK, JVM, hardware, JMH configuration, and allocation data (`-prof gc`). Treat short or
   noisy runs as exploratory, not publication-quality evidence.
5. Keep benchmark classes small. Put reusable scenario generation, baselines, and setup logic in
   named subpackages. Do not put Java source files directly in the `benchmarks/` directory.

A Java Stream baseline is a standing comparison requirement for future benchmark families. The baseline
should be independent of the library implementation; for conditional products, it incrementally checks
constraints while recursively traversing dimensions rather than materializing the full Cartesian product
and filtering only at the end.

## Package layout

```text
benchmarks/
├── README.md
├── runner/
│   └── BenchmarkCommandGenerator.java
└── conditionalproduct/
    ├── ConditionalProductBenchmark.java
    ├── anyorder/
    ├── baseline/
    ├── scenario/
    └── support/
```

The topology benchmarks share one parameterized JMH class. The console command generator is kept
separate from the benchmark itself: it collects selections, validates them, expands valid
topology/dimension/domain-size combinations, and prints a command for the user to execute.
It does not launch JMH.

## Conditional-product benchmarks

The unified `conditionalproduct/ConditionalProductBenchmark` compares these implementations:

- `PRUNED_BITSET`
- `PRUNED_DFS`
- `DP`
- `ADAPTIVE`
- `NESTED_STREAM` — independent incrementally filtered nested Java Streams baseline

The standard topology matrix combines `CHAIN`, `MANY_VALID`, `COMPLETE`, and
`DEAD_END` with 3, 5, 7, or 10 dimensions and 10, 25, or 50 values per dimension.
`DEAD_END` deliberately excludes 10x50 because its estimated output exceeds the benchmark
budget. Two additional focused workload families extend that matrix:

- `DEEP_DEAD_END`: late conflicts designed to expose prefixes that appear viable until a
  later dimension; tested at 5x10, 5x25, 7x10, 7x25, and 10x10.
- `SMALL_DOMAIN`: many dimensions with small domains and a high proportion of valid tuples;
  tested at 8x4, 10x4, 12x3, 14x3, and 8x5.

The runner generates only valid scenario values, so JMH does not attempt invalid
topology/shape combinations.

The benchmark includes builder construction, rule registration, generator creation, and complete
tuple consumption. Trial setup checks each selected library strategy's tuple count and checksum
against the nested-stream baseline. Scenario definitions and rule data are in `scenario/`;
the independent nested-stream implementation is in `baseline/`; shared builder and verification
logic are in `support/`.

### Generating a benchmark command

First build the test classpath:

```bash
mvn test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt
```

Run the console selector from the project root:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  io.github.deepeshpatel.jnumbertools.benchmarks.runner.BenchmarkCommandGenerator
```

At each selection prompt, enter one option (for example, `2`), several comma-separated options
(for example, `1,3` or `1, 3`), or `All` (or the displayed final option number). Duplicate,
out-of-range, and malformed selections are rejected and prompted again.

The selector asks for topology, dimensions, domain sizes, implementations, warmup iterations,
measurement iterations, forks, and profiler (`gc` or `none`). JMH defaults are 5 warmup
iterations, 8 measurement iterations, and 2 forks; the default profiler is `gc`. It prints the
resolved selections and generated command, but does not execute it. Copy the generated command
and run it from the project root.

The unified JMH class uses the `scenario` parameter to represent valid topology/shape pairs and
the `implementation` parameter for the strategy. This avoids invalid cross-products in JMH
while still letting the console selector present topology, dimensions, and domain size as
independent choices.

## Historical-result comparability

The initial `DEAD_END`, `CHAIN`, `MANY_VALID`, and `COMPLETE` results below were captured
during earlier benchmark work; the latter three were captured before benchmark-class
consolidation. Retain them as historical evidence for those workloads, but do not compare their
scores directly with newer focused runs unless the benchmark source, workload, JVM/hardware,
and measurement configuration are shown to be equivalent. The uploaded text outputs for
`CHAIN`, `MANY_VALID`, and `COMPLETE` use the earlier `shape` parameter, whereas the
consolidated benchmark uses `scenario`. Their results therefore describe the earlier benchmark
implementation, not necessarily the current strategy dispatch. The detailed output and metadata
for each run should remain with its corresponding result file.

## Dead-end benchmark: initial results

The first reported end-to-end run of the (now consolidated) `DEAD_END` topology shows the
following fastest implementation for each tested shape:

| Shape | Fastest implementation | Score (ms/op) | Nested Stream (ms/op) | Relative result |
|---|---|---:|---:|---:|
| 3x10 | PRUNED_BITSET | 0.005 | 0.004 | Stream 1.25x faster |
| 3x25 | ADAPTIVE | 0.008 | 0.026 | Builder 3.25x faster |
| 3x50 | PRUNED_BITSET | 0.031 | 0.149 | Builder 4.81x faster |
| 5x10 | PRUNED_DFS | 0.007 | 0.007 | Approximately tied |
| 5x25 | PRUNED_BITSET | 0.055 | 0.204 | Builder 3.71x faster |
| 5x50 | PRUNED_BITSET | 1.144 | 3.164 | Builder 2.77x faster |
| 7x10 | ADAPTIVE | 0.010 | 0.015 | Builder 1.50x faster |
| 7x25 | PRUNED_BITSET | 1.524 | 4.656 | Builder 3.05x faster |
| 7x50 | PRUNED_DFS | 105.160 | 453.121 | Builder 4.31x faster |
| 10x10 | PRUNED_BITSET | 0.031 | 0.103 | Builder 3.32x faster |
| 10x25 | PRUNED_DFS | 258.326 | 1061.780 | Builder 4.11x faster |

These are end-to-end scores: builder construction, rule registration, generator creation, and complete
tuple consumption are included. The results suggest that pruning overhead is not always worthwhile for
the smallest cases (3x10 and 5x10), while the conditional-product implementations outperform the
nested-stream baseline by roughly 2.8x–4.8x on the larger tested cases.

This is an initial, exploratory result set, not a publication-quality comparison. The pasted output
contained scores but not the JMH error estimates, allocation profiler results, exact JVM/hardware
metadata, or full command line. Re-run with `-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc` and retain
the complete output before drawing firm conclusions. The 10x50 dead-end shape is intentionally
omitted from this suite.

## Complete benchmark: first full JMH results

The uploaded `COMPLETE` run used the earlier shape-parameterized benchmark with
`-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`. It covered 12 shapes (3, 5, 7, and
10 dimensions; 10, 25, and 50 values per dimension). The fastest builder strategy beat
nested streams in all 12 shapes, with observed speedups from about 1.9x to 15.9x.
`ADAPTIVE` had the lowest recorded score in 11 shapes and `PRUNED_BITSET` in one,
but most of those strategy differences were small and often within the reported error
ranges. Treat this as historical, pre-consolidation evidence; it does not validate the
current `ADAPTIVE` dispatch policy. The text output does not include JDK/JVM metadata.

## Chain benchmark: first full JMH results

The first complete reported run of the `CHAIN` topology, captured before benchmark-class consolidation, used
`-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`. It includes end-to-end construction and
full tuple consumption. The table reports the fastest builder strategy for each shape against
the independent nested Java Stream baseline.

| Shape | Fastest builder strategy | Builder (ms/op) | Nested Stream (ms/op) | Speedup |
|---|---|---:|---:|---:|
| 3x10 | PRUNED_BITSET | 0.012 | 0.031 | 2.58x |
| 3x25 | PRUNED_BITSET | 0.165 | 0.423 | 2.56x |
| 3x50 | ADAPTIVE | 0.399 | 1.052 | 2.64x |
| 5x10 | PRUNED_BITSET | 0.325 | 1.026 | 3.16x |
| 5x25 | PRUNED_BITSET | 0.367 | 1.736 | 4.73x |
| 5x50 | PRUNED_BITSET | 0.192 | 2.750 | 14.32x |
| 7x10 | ADAPTIVE | 0.144 | 0.666 | 4.63x |
| 7x25 | PRUNED_BITSET | 0.460 | 3.500 | 7.61x |
| 7x50 | PRUNED_BITSET / ADAPTIVE | 0.078 | 1.656 | 21.23x |
| 10x10 | PRUNED_BITSET | 0.140 | 1.022 | 7.30x |
| 10x25 | PRUNED_BITSET | 0.341 | 2.347 | 6.88x |
| 10x50 | PRUNED_BITSET | 1.118 | 10.870 | 9.72x |

In this run, a builder strategy was faster than the nested-stream baseline in every tested shape.
`PRUNED_BITSET` was fastest or tied for fastest in 10 of 12 shapes; `ADAPTIVE` led 3x50
and 7x10 (with a tie at 7x50). The observed speedup ranged from 2.56x to 21.23x.

The GC profiler's normalized allocation metric (`gc.alloc.rate.norm`) also shows a substantial
difference. For the fastest builder in each shape, allocation per operation was approximately
5.0x to 22.5x lower than nested streams (about 80.1% to 95.6% less allocated data). For example,
at 10x50, `PRUNED_BITSET` allocated about 1.51 MB/op versus 30.90 MB/op for nested streams.
These are per-operation allocation measurements, not retained-heap measurements.

The scores are scenario-specific and should not be expected to increase monotonically with the
number of dimensions or values: the number and distribution of valid tuples and the pruning
opportunities also affect the amount of work. This is one topology only; do not generalize these
numbers to `manyvalid`, `complete`, or `deadend`. The reported output includes JMH error
estimates and GC metrics, but not the machine/JVM metadata, so retain that metadata with any
formal performance report.

## Many-valid benchmark: first full JMH results

The first reported end-to-end run of the `MANY_VALID` topology, captured before benchmark-class consolidation, used
`-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`. The table shows the fastest measured
library strategy for each shape against the independent nested Java Stream baseline.

| Shape | Fastest builder strategy | Builder (ms/op) | Nested Stream (ms/op) | Speedup |
|---|---|---:|---:|---:|
| 3x10 | PRUNED_BITSET / ADAPTIVE | 0.012 | 0.033 | 2.75x |
| 3x25 | PRUNED_BITSET | 0.165 | 0.426 | 2.58x |
| 3x50 | PRUNED_BITSET | 0.746 | 1.916 | 2.57x |
| 5x10 | PRUNED_DFS | 0.904 | 2.413 | 2.67x |
| 5x25 | DP | 1.058 | 5.055 | 4.78x |
| 5x50 | PRUNED_BITSET | 0.900 | 9.746 | 10.83x |
| 7x10 | PRUNED_DFS | 1.061 | 3.218 | 3.03x |
| 7x25 | PRUNED_BITSET | 0.569 | 3.566 | 6.27x |
| 7x50 | PRUNED_BITSET | 0.962 | 6.249 | 6.50x |
| 10x10 | PRUNED_BITSET / ADAPTIVE | 0.141 | 0.998 | 7.08x |
| 10x25 | PRUNED_BITSET | 0.343 | 2.340 | 6.82x |
| 10x50 | PRUNED_BITSET | 0.903 | 7.353 | 8.14x |

In this run, the fastest measured builder strategy beat nested streams in all 12 shapes,
with observed speedups from 2.57x to 10.83x. `PRUNED_BITSET` was fastest or tied in
8 shapes; `PRUNED_DFS` led 5x10 and 7x10, `DP` led 5x25, and `ADAPTIVE` led
10x50. Some apparent wins are close relative to their reported errors, so the per-shape
minimum should not be treated as a universal strategy ranking.

The GC profiler's normalized allocation metric (`gc.alloc.rate.norm`) shows that the
fastest measured builder strategy allocated approximately 6.1x to 15.1x less data per
operation than nested streams (about 83.6% to 93.4% less). For example, at 5x50,
`PRUNED_BITSET` allocated about 2.63 MB/op versus 37.88 MB/op for nested streams.
At 7x25, it allocated about 0.91 MB/op versus 13.73 MB/op. These are per-operation
allocation measurements, not retained-heap measurements.

This result is important because the builder advantage is not confined to the dead-end
or chain scenarios: it is also present in this many-valid scenario. However, the measured
advantage varies substantially by shape, and several builder scores have wide error
estimates. Treat this as an initial exploratory result, not a publication-quality
comparison. The pasted output includes JMH errors and GC metrics but not the exact
machine/JVM metadata. Preserve that metadata with any formal report, and avoid
generalizing these results to the `complete` topology.

## Any-order parallel benchmark

`anyorder/AnyOrderParallelBenchmark` compares sequential and parallel consumption of
`anyOrder().stream()`. It includes traversal-only and end-to-end methods and validates sequential
and parallel checksums during trial setup. This is a separate comparison: `anyOrder()` is not the
lexicographic conditional-product strategy parameterized by the topology benchmarks.

## Running benchmarks directly

Build the test classpath once:

```bash
mvn test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt
```

For conditional-product benchmarks, use the console selector described above. It generates the
JMH command with explicit `scenario` and `implementation` parameter values. To run the separate
any-order parallel comparison directly:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  org.openjdk.jmh.Main '.*AnyOrderParallelBenchmark.*' -wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc
```

The topology benchmark annotations retain short smoke-test defaults (one fork, one 100 ms warmup,
one 100 ms measurement). The command selector defaults to 5 warmup iterations, 8 measurement
iterations, 2 forks, and the GC profiler. Always inspect score error and allocation per operation
before drawing conclusions, and do not compare timings from different machines or JVM
configurations as if they were directly equivalent.


## Optimization decision log

This is the concise engineering decision record, not a raw benchmark archive. Each entry connects
the change, benchmark configuration, finding, and decision. Detailed JMH output belongs in
benchmark result files. Read this section before proposing another optimization.

### Decision-record rules

1. State what changed and which benchmark configuration tested it.
2. Separate the measured finding from the engineering decision.
3. Label evidence as screening, provisional, or confirmed; a short run can identify candidates
   but cannot establish close performance rankings.
4. Validate equivalent tuple counts and checksums, and retain the independent,
   incrementally-pruned nested-stream baseline.
5. Do not silently reverse a decision. Record the new evidence and identify the decision it
   supersedes.

### Default lexicographic strategy and topology-aware lookahead

- **Change:** The current implementation makes `PRUNED_BITSET` the default for
  `lexOrder()`. It applies prefix pruning and uses topology-triggered feasibility lookahead
  when the constraint graph contains a loop block. `PRUNED_DFS` uses the corresponding
  lookahead with dense boolean relation rows.
- **Benchmark:** The uploaded historical `CHAIN`, `MANY_VALID`, and `COMPLETE` runs
  used the earlier shape-parameterized benchmark. The later focused JSON run used JMH 1.37,
  OpenJDK 26.0.1, one 200 ms warmup, one 200 ms measurement, one fork, and 14 scenarios.
  A separate follow-up used five warmup iterations, eight measurement iterations, and two
  forks for five deep-dead-end and five small-domain scenarios; its text output does not
  include JDK/JVM metadata.
- **Finding:** No single strategy won across all topologies. DP won all five deep-dead-end
  cases in the two-fork follow-up; small-domain winners varied. In the earlier chain and
  many-valid runs, BitSet was generally competitive, but DFS, DP, and ADAPTIVE each won
  selected shapes. The historical COMPLETE run recorded ADAPTIVE as the lowest score in
  11 of 12 shapes, often with small differences and overlapping errors.
- **Decision:** Keep `PRUNED_BITSET` as the current default. This remains an engineering
  choice, not a benchmark-proven globally optimal dispatch policy. The older ADAPTIVE
  results must not be used to claim that the current ADAPTIVE implementation is independently
  selecting the best strategy: the current code maps ADAPTIVE to the BitSet-backed path.
- **Status:** Provisional; supported by multiple topology-specific runs, but not by a
  unified, current-source comparison across the full workload matrix.
- **Revisit when:** A full multi-fork run of the consolidated benchmark compares current
  implementations across representative topologies, and an isolated before/after run measures
  the incremental benefit and cost of topology-aware lookahead.

### DP on deep-dead-end workloads

- **Change:** Added the `DEEP_DEAD_END` workload family to expose late conflicts that can
  leave long prefixes apparently viable before they fail.
- **Benchmark:** The follow-up run compared all five implementations on
  `DEEP_DEAD_END-5x10`, `5x25`, `7x10`, `7x25`, and `10x10`, plus five
  `SMALL_DOMAIN` scenarios. Command configuration:
  `-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`.
- **Finding:** DP recorded the lowest time in all five deep-dead-end cases. Its advantage
  was modest at 5x10 and very large in the other four late-conflict cases. In the five
  small-domain cases, the lowest-time strategy varied: BitSet won two, DFS won two, and
  ADAPTIVE won one. The fastest builder beat nested streams in all ten scenarios; the
  measured speedup ranged from about 3.6x to over 2,000x. These are scenario-specific
  end-to-end results, not a universal speedup claim.
- **Decision:** Keep DP as an explicit strategy and treat it as the leading choice for the
  tested deep-dead-end workload family. Do not change the global default: the small-domain
  results show that the deep-dead-end result does not generalize to every topology.
- **Status:** Stronger two-fork evidence for this workload family, but still provisional
  because the uploaded output lacks JDK/JVM metadata and the full consolidated matrix was
  not rerun in the same command.
- **Revisit when:** The same scenarios are rerun with environment metadata recorded and
  included alongside chain, many-valid, complete, and ordinary dead-end cases.

### Builder traversal versus nested Java Streams

- **Change:** Retained an independent nested-stream baseline that checks constraints
  incrementally while traversing dimensions; it does not build the full Cartesian product
  and filter only at the end.
- **Benchmark:** The uploaded topology runs compare end-to-end construction and tuple
  consumption. The focused follow-up includes the GC profiler and correctness validation
  performed by the benchmark setup.
- **Finding:** The builder approach beat the independent nested-stream baseline in all
  12 historical COMPLETE, all 12 CHAIN, all 12 MANY_VALID, and all ten newer
  DEEP_DEAD_END/SMALL_DOMAIN scenarios. The magnitude varied substantially by workload.
  Allocation was also lower in the uploaded runs; for example, in
  `DEEP_DEAD_END-7x25`, the best builder allocated about 57 KiB/op versus about
  114 MiB/op for nested streams. These are allocation-per-operation figures, not
  retained-heap measurements.
- **Decision:** Keep the incrementally-pruned nested-stream implementation as the baseline.
  The results support the builder approach across the tested workloads; do not generalize
  the extreme late-conflict speedups to ordinary products.
- **Status:** Repeated evidence across topology families; historical runs differ in benchmark
  generation, and the latest follow-up's environment metadata is incomplete.
- **Revisit when:** An equivalent, correctness-checked workload or a material baseline
  implementation change produces contrary results.

### ADAPTIVE behavior and API description

- **Change:** The current `ADAPTIVE` entry point resolves to `PRUNED_BITSET` with
  topology-triggered lookahead. Its `estimatedLimit` argument is validated but does not
  choose between DP and prefix pruning.
- **Benchmark:** Older shape-parameterized runs reported ADAPTIVE as the lowest-time
  implementation in selected CHAIN, MANY_VALID, and COMPLETE cases. The later focused
  run included ADAPTIVE, but it was a short one-fork screening run.
- **Finding:** Those older ADAPTIVE measurements came from a different benchmark generation
  and do not establish that the current ADAPTIVE entry point is an independent adaptive
  selector. The current implementation's behavior is determined by the source, not by
  historical benchmark labels.
- **Decision:** Keep the API behavior as currently implemented, but document clearly that
  `estimatedLimit` does not select DP versus prefix pruning. Do not claim that ADAPTIVE
  dynamically chooses the fastest strategy.
- **Status:** Implementation behavior is explicit; performance rationale is not confirmed.
- **Revisit when:** A genuinely adaptive policy is implemented and benchmarked against
  explicit BitSet and DP on the same current-source scenario matrix.

### Next confirmation run

Run the consolidated benchmark across the representative topology/shape matrix with at least
`-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`. Record the exact command, source commit,
JMH version, JDK/JVM version and arguments, hardware, score errors, allocation per operation,
and correctness checks. Include ordinary and adversarial topologies in the same benchmark
generation before changing default strategy selection.

### Decision register

| Topic | Current decision | Reason / evidence needed to reopen |
|---|---|---|
| Default lexicographic strategy | Keep `PRUNED_BITSET`; provisional, not proven globally optimal. | Unified current-source multi-fork comparison and isolated lookahead before/after results. |
| DP on deep dead ends | Keep as explicit strategy and leading candidate for this workload family. | Reproduce the five-of-five result with environment metadata and compare against ordinary topologies. |
| PRUNED_DFS | Retain as an explicit option; it won selected small-domain and historical many-valid cases. | Reopen if current-source runs show consistent domination or a material workload-specific advantage. |
| ADAPTIVE | Current implementation maps to BitSet-backed pruning; `estimatedLimit` does not choose DP. | Reopen with an implemented and measured adaptive policy. |
| Builder vs nested streams | Keep the independent incrementally-pruned stream baseline; builders won all uploaded tested cases. | Reopen with equivalent verified workloads or a material baseline change. |
| Historical topology results | Retain as topology-specific evidence; do not compare across benchmark generations without equivalent setup. | Re-run under the consolidated benchmark with environment metadata. |
