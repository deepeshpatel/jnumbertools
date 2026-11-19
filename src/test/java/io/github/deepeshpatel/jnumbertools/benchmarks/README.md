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
- `IMPERATIVE_ALLOCATING` — hand-written recursive backtracking that allocates one `Object[]`
  (wrapped as a list) per result, like a library tuple
- `IMPERATIVE_REUSE` — the same backtracking over one reused index array; allocates nothing per
  result, so it bounds from below what the library's result objects cost

The standard topology matrix combines `CHAIN`, `MANY_VALID`, `COMPLETE`, and
`DEAD_END` with 3, 5, 7, or 10 dimensions and 10, 25, or 50 values per dimension.
`DEAD_END` deliberately excludes 10x50 because its estimated output exceeds the benchmark
budget. Two additional focused workload families extend that matrix:

- `DEEP_DEAD_END`: late conflicts designed to expose prefixes that appear viable until a
  later dimension; tested at 5x10, 5x25, 7x10, 7x25, and 10x10. Before the Phase F2 fix four of
  these five shapes produced an empty product, so results recorded before that fix do not
  describe partial dead ends and must not be reused.
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

### Special operations

Full traversal is only part of the library: `count`, `isEmpty`, `lexOrderMth`, `byRanks`, `choice`
and `sample` use the exact counting tier, which is where the largest gains over enumeration are
expected. Two classes measure them (preset `special-ops`, which runs both and writes two JSON files):

- `ConditionalProductSpecialOperationsBenchmark`: five bounded workloads (`CHAIN-5x25`,
  `MANY_VALID-5x25`, `DEAD_END-5x25`, `DEEP_DEAD_END-5x25`, `SMALL_DOMAIN-10x4`) with `LIBRARY`
  against `ENUMERATION`, the best a caller can do without the library: a non-allocating
  imperative enumeration, materialising every tuple first where sampling needs them.
- `ConditionalProductScalingBenchmark`: `LARGE-10x50` (about 1e17 tuples), `LARGE-20x50` (about
  5e33) and `LARGE-20x100` (about 2e39), library only, since no enumeration can finish.

Every measured call includes builder and rule construction. Trial setup verifies the library
result before anything is timed: counts against the enumeration (or the closed form
`size * (size-1)^(n-1)` for `LARGE`), `lexOrderMth` and `byRanks` against the enumeration on
bounded workloads, and for all workloads that every returned tuple satisfies the rules, rank
operations return ascending tuples, and `sample` returns distinct tuples.

#### Special operations: first results (screening)

JMH 1.37, OpenJDK 26.0.1, one fork, 3 warmup and 5 measurement iterations of 100 ms, GC profiler.
The environment file was not supplied, so hardware is not recorded. Single-fork short runs: many rows
had a score error above 10%, so read ratios of about 1.5x or less as ties.

Library speedup over the enumeration baseline (above 1 the library is faster, below 1 enumeration is):

| Workload | count | isEmpty | lexOrderMth | byRanks | choice | sample |
|---|---:|---:|---:|---:|---:|---:|
| CHAIN-5x25 | 25.5x | 0.03x | 12.6x | 13.1x | 16.2x | 16.7x |
| MANY_VALID-5x25 | 62.4x | 0.03x | 25.8x | 28.9x | 38.9x | 39.8x |
| DEAD_END-5x25 | 1.4x | 0.33x | 0.54x | 0.56x | 0.65x | 0.65x |
| DEEP_DEAD_END-5x25 | 78.3x | 0.24x | 58.2x | 55.7x | 52.0x | 48.4x |
| SMALL_DOMAIN-10x4 | 48.8x | 0.02x | 16.3x | 17.9x | 36.3x | 38.5x |

Library only, microseconds per call (builder construction included):

| Workload | count | isEmpty | lexOrderMth | byRanks | choice | sample |
|---|---:|---:|---:|---:|---:|---:|
| LARGE-10x50 (about 1e17 tuples) | 205 | 21 | 241 | 240 | 203 | 536 |
| LARGE-20x50 (about 5e33) | 431 | 52 | 702 | 600 | 414 | 6,029 |
| LARGE-20x100 (about 2e39) | 1,507 | 76 | 1,914 | 1,757 | 1,529 | 11,479 |

What the numbers do and do not show:

- Counting and rank operations beat enumeration by roughly 13x to 78x on four of the five bounded
  workloads, and handle products of 1e39 tuples in milliseconds.
- `isEmpty` loses to enumeration everywhere, but the comparison is biased: every bounded workload is
  non-empty, so enumeration stops at its first tuple. A workload whose product is empty must be added
  before drawing a conclusion. `isEmpty` itself is cheap on `LARGE` (21 to 76 us), because it uses the
  feasibility tier and builds no counts.
- On the small `DEAD_END-5x25` shape enumeration wins every rank and sampling operation (library at
  0.54x to 0.65x) and `count` is only about 1.4x faster: setting up the counting tier costs more than
  enumerating a product this small.
- Allocation is the clearest cost. `count` allocates about 66 KB to 1.1 MB per call on the bounded
  workloads against about 1.5 KB for enumeration, and 1.5 MB to 13 MB on `LARGE`. `sample` allocates
  several times more than `byRanks` (24 MB against 4.5 MB at 20x50; 57 MB at 20x100) and is the slowest
  operation at large sizes.
- Status: screening. These support Phase H item 8 (counting speed) as the next target.

### Presets, result files and environment record

The selector first offers three presets:

- **quick** (about 1-2 minutes): `CHAIN-5x25`, `MANY_VALID-5x25`, `DEAD_END-5x25`,
  `DEEP_DEAD_END-5x25`, `SMALL_DOMAIN-10x4` with `PRUNED_BITSET` and `DP`, 2 warmup and
  3 measurement iterations, one fork. It is a regression screen for uncommitted code: it exposes
  changes of roughly 20% or more and cannot rank close strategies.
- **full-matrix**: every topology, shape and implementation in one benchmark generation, with
  5 warmup iterations, 8 measurement iterations and 2 forks. This is the confirmation run the
  decision log requires; it takes a long time.
- **special-ops**: the two special-operations benchmarks above.
- **custom**: the interactive selections above.

Every generated command also records the run. Files go to `benchmark-results/` (git-ignored; only
the final summary table is committed to this README):

- `<run>.env.txt`: commit, branch, whether the tree had uncommitted changes, JDK and JVM, JVM
  arguments, OS, CPU model, logical CPUs, physical memory and maximum heap
  (`BenchmarkEnvironment`).
- `<run>.json`: JMH JSON (`-rf json -rff`), which also carries the JMH version, forks and iteration
  settings, and the GC profiler's allocation per operation.
- `<run>.log`: the complete console output.

Scripts print the same command without prompting:
`... BenchmarkCommandGenerator --print quick <run-name>` (or `full`).

### Report tables and comparing against an earlier commit

`scripts/BenchmarkReport.java` is a single-file JDK program that needs no dependencies.

```bash
# README-ready table: fastest strategy, speedup vs the stream baseline and vs the next-best builder,
# allocation per operation vs the baseline, and a flag when the score error exceeds 10% of the score
java scripts/BenchmarkReport.java report benchmark-results/<run>.json benchmark-results/<run>.env.txt
```

`scripts/benchmark-compare.sh <SHA>` answers "did my uncommitted change help one case and hurt
another?". It builds the old commit in a temporary git worktree (your working tree is not
touched and no branch is switched), copies the current benchmark sources into it so both sides
run identical benchmark code, runs the quick preset on both, and prints a per-shape table with
verdicts: within 10% is "same", a larger change with overlapping error bars is "inconclusive",
otherwise "FASTER" or "SLOWER". It finishes by printing the branch and commit you are on.
Pass `full` as the second argument for the full matrix. A clean result means "no regression
found", not "proven faster".

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

### Default lexicographic strategy and work-triggered lookahead

- **Change:** The current implementation makes `PRUNED_BITSET` the default for
  `lexOrder()`. It applies prefix pruning and lazily enables feasibility lookahead after traversal work
  indicates substantial dead-end exploration. Lookahead switches on once descents exceed
  1,024 + 4 x emitted tuples. The trigger is independent of graph topology, so it applies
  to looped and acyclic dead ends alike. Once on it stays on for that traversal and does not
  change the tuples returned or their order.
  `PRUNED_DFS` uses the same lookahead control flow with dense boolean relation rows when
  that representation fits the memory limit.
- **Benchmark:** The uploaded historical `CHAIN`, `MANY_VALID`, and `COMPLETE` runs
  used the earlier shape-parameterized benchmark. The later focused JSON run used JMH 1.37,
  OpenJDK 26.0.1, one 200 ms warmup, one 200 ms measurement, one fork, and 14 scenarios.
  A separate follow-up used five warmup iterations, eight measurement iterations, and two
  forks for five deep-dead-end and five small-domain scenarios; its text output does not
  include JDK/JVM metadata.
- **Finding:** No single strategy won across all topologies. (The earlier claim that DP won
  all five deep-dead-end cases came from the pre-F2 shapes, four of which were empty products;
  see the superseding entry below.) Small-domain winners varied. In the earlier chain and
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
  the incremental benefit and cost of work-triggered lookahead.

### DP on deep-dead-end workloads

- **Change:** Added the `DEEP_DEAD_END` workload family to expose late conflicts that can
  leave long prefixes apparently viable before they fail. Phase F2 corrected the shapes: the
  first version produced an empty product for four of five cases, so the earlier comparison
  measured how quickly each strategy discovers emptiness, not partial dead ends.
- **Benchmark:** Superseding screening run on the corrected shapes, all five implementations,
  `DEEP_DEAD_END-5x10`, `5x25`, `7x10`, `7x25`, `10x10`; command
  `-wi 2 -i 2 -f 2 -tu ms -bm avgt -prof gc` with the short annotation iteration times
  (screening quality; JDK/JVM metadata not recorded).
- **Finding:** With work-triggered lookahead, `PRUNED_BITSET` ran at 1.46x, 1.44x, 1.01x, 1.01x
  and 0.72x the DP time (5x10, 5x25, 7x10, 7x25, 10x10): DP was ahead by at most about 1.5x
  and pruned was ahead on 10x10. The fastest builder beat the nested-stream baseline in all
  five cases by roughly 4x to 12x. On ordinary `DEAD_END` and `SMALL_DOMAIN` shapes
  `PRUNED_BITSET` was faster than DP in every case (0.14x to 0.76x of the DP time).
  These are scenario-specific end-to-end results, not a universal speedup claim.
- **Decision:** Supersedes the earlier "DP is the leading candidate for deep dead ends" entry.
  Keep DP as an explicit strategy; do not change the default. Because pruned plus lookahead is
  within about 1.5x of DP on the corrected shapes, Phase G (dimension reordering) is not
  justified unless a multi-fork run contradicts this.
- **Status:** Screening only; short runs cannot establish close rankings.
- **Revisit when:** The confirmation run (below) with environment metadata contradicts the
  ratios above or includes shapes where pruned is more than 2x slower than DP.

### Builder traversal versus nested Java Streams

- **Change:** Retained an independent nested-stream baseline that checks constraints
  incrementally while traversing dimensions; it does not build the full Cartesian product
  and filter only at the end.
- **Benchmark:** The uploaded topology runs compare end-to-end construction and tuple
  consumption. The focused follow-up includes the GC profiler and correctness validation
  performed by the benchmark setup.
- **Finding:** The builder approach beat the independent nested-stream baseline in all
  12 historical COMPLETE, all 12 CHAIN, all 12 MANY_VALID, and, in the corrected
  screening run, all ten DEEP_DEAD_END/SMALL_DOMAIN scenarios. The magnitude varied
  substantially by workload. Allocation was also lower in the uploaded runs. Figures quoted
  before the Phase F2 fix for `DEEP_DEAD_END` are withdrawn because those shapes were empty.
  Allocation figures are per operation, not retained-heap measurements.
- **Decision:** Keep the incrementally-pruned nested-stream implementation as the baseline.
  The results support the builder approach across the tested workloads; do not generalize
  the extreme late-conflict speedups to ordinary products.
- **Status:** Repeated evidence across topology families; historical runs differ in benchmark
  generation, and the latest follow-up's environment metadata is incomplete.
- **Revisit when:** An equivalent, correctness-checked workload or a material baseline
  implementation change produces contrary results.

### ADAPTIVE behavior and API description

- **Change:** The current `ADAPTIVE` entry point resolves to `PRUNED_BITSET`. The shared
  pruned traversal lazily enables work-triggered feasibility lookahead; this trigger is not
  based on graph topology. Its `estimatedLimit` argument is validated but does not affect
  strategy selection.
- **Benchmark:** Older shape-parameterized runs reported ADAPTIVE as the lowest-time
  implementation in selected CHAIN, MANY_VALID, and COMPLETE cases. The later focused
  run included ADAPTIVE, but it was a short one-fork screening run.
- **Finding:** Those older ADAPTIVE measurements came from a different benchmark generation
  and do not establish that the current ADAPTIVE entry point is an independent adaptive
  selector. The current implementation's behavior is determined by the source, not by
  historical benchmark labels.
- **Decision:** Treat ADAPTIVE as an implementation policy, not a promise that a particular
  algorithm is used for a particular workload or condition. Its internal selection policy may
  change between versions. Record the actual dispatch in this maintainer decision log; at this
  revision it maps to `PRUNED_BITSET`, and `estimatedLimit` is validated but ignored for selection.
  Do not claim that ADAPTIVE dynamically chooses the fastest strategy.
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
| DP on deep dead ends | Keep as explicit strategy; no longer the leading candidate. Pruned plus lookahead is within about 1.5x of DP on corrected shapes (screening). | Multi-fork run showing pruned more than 2x slower than DP; then consider Phase G. |
| PRUNED_DFS | Retain as an explicit option; it won selected small-domain and historical many-valid cases. | Reopen if current-source runs show consistent domination or a material workload-specific advantage. |
| ADAPTIVE | Documented as an implementation policy with no guarantee of which algorithm runs when; at this revision it maps to BitSet-backed pruning and `estimatedLimit` does not choose DP. | Reopen with an implemented and measured adaptive policy; decide name/parameter before release. |
| Builder vs nested streams | Keep the independent incrementally-pruned stream baseline; builders won all uploaded tested cases. | Reopen with equivalent verified workloads or a material baseline change. |
| Hand-written baselines | Added `IMPERATIVE_ALLOCATING` and `IMPERATIVE_REUSE` beside `NESTED_STREAM` (additive; nothing replaced). They separate the cost of the algorithm from the cost of result objects and streams. | Reopen if either baseline stops being a fair hand-written equivalent, for example a clearly faster hand-written formulation. |
| Special operations | Measured separately from traversal: bounded workloads against enumeration, `LARGE` workloads library-only. | Reopen with a run recorded on a fixed environment; sandbox timings are indicative only. |
| Historical topology results | Retain as topology-specific evidence; do not compare across benchmark generations without equivalent setup. | Re-run under the consolidated benchmark with environment metadata. |


## Counting tier construction (patch h1)

`ConstraintCounts` now builds its tables in primitive longs first (checked against 2^62) and falls back to the
BigInteger tables only when some count does not fit. Equal relation rows share one sum per child, and a row that is a
single run of values is summed from prefix sums. Results are identical; the BigInteger path is covered by
`ConstraintPlanTierTest` (12 dimensions of 60 values, count above 2^62, rank checked against the mixed-radix closed form).

Sandbox allocation per `count()` call (indicative, not JMH): LARGE-10x50 111 KB, LARGE-20x50 422 KB,
LARGE-20x100 635 KB (before: 1.5 MB to 13 MB). `sample` on LARGE-20x50 and 20x100 is still 21 MB and 45 MB per call
because those counts exceed a long and each of the 50 unranks walks BigInteger completions; that is the next target.
Please confirm with `scripts/benchmark-compare.sh <SHA> quick` or the `special` preset.
