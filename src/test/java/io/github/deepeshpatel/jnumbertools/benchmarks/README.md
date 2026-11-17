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

The logical scenario selection combines topology and shape. The supported topologies are
`CHAIN`, `MANY_VALID`, `COMPLETE`, and `DEAD_END`. Shapes use 3, 5, 7, or 10 dimensions
and 10, 25, or 50 values per dimension. `DEAD_END` deliberately excludes 10x50 because its
estimated output exceeds the benchmark budget. The runner generates only valid scenario values,
so JMH does not attempt invalid topology/shape combinations.

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

## Chain benchmark: first full JMH results

The first complete reported run of the `CHAIN` topology, captured before benchmark-class consolidation, used
`-wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc`. It includes end-to-end construction and
full tuple consumption. The table reports the fastest builder strategy for each shape against
the independent nested Java Stream baseline.

| Shape | Fastest builder strategy | Builder (ms/op) | Nested Stream (ms/op) | Speedup |
|---|---|---:|---:|---:|
| 3x10 | PRUNED_BITSET | 0.012 | 0.031 | 2.58x |
| 3x25 | PRUNED_BITSET | 0.165 | 0.423 | 2.56x |
| 3x50 | PRUNED_DFS | 0.427 | 1.052 | 2.46x |
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
`PRUNED_BITSET` was fastest or tied for fastest in 10 of 12 shapes; `PRUNED_DFS` led 3x50,
and `ADAPTIVE` led 7x10 (with a tie at 7x50). The observed speedup ranged from 2.46x to 21.23x.

The GC profiler's normalized allocation metric (`gc.alloc.rate.norm`) also shows a substantial
difference. For the fastest builder in each shape, allocation per operation was approximately
6.1x to 22.5x lower than nested streams (about 83.6% to 95.6% less allocated data). For example,
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
| 5x10 | PRUNED_BITSET | 1.165 | 2.413 | 2.07x |
| 5x25 | DP | 1.058 | 5.055 | 4.78x |
| 5x50 | PRUNED_BITSET | 0.900 | 9.746 | 10.83x |
| 7x10 | PRUNED_DFS | 1.061 | 3.218 | 3.03x |
| 7x25 | PRUNED_BITSET | 0.569 | 3.566 | 6.27x |
| 7x50 | PRUNED_BITSET | 0.962 | 6.249 | 6.50x |
| 10x10 | PRUNED_BITSET / ADAPTIVE | 0.141 | 0.998 | 7.08x |
| 10x25 | PRUNED_BITSET | 0.343 | 2.340 | 6.82x |
| 10x50 | PRUNED_BITSET | 0.903 | 7.353 | 8.14x |

In this run, the fastest measured builder strategy beat nested streams in all 12 shapes,
with observed speedups from 2.07x to 10.83x. `PRUNED_BITSET` was fastest or tied in
10 shapes; `DP` led 5x25 and `PRUNED_DFS` led 7x10. The 7x10 result is relatively
close to `PRUNED_BITSET` and has a wide error estimate, so the apparent winner should
not be treated as conclusive. At 5x25, the `DP` and `PRUNED_BITSET` scores are also
close relative to their reported errors. `ADAPTIVE` did not consistently select the
fastest measured strategy in this topology.

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

This is a decision summary, not a raw benchmark archive. Read it before proposing another
optimization. Keep only the conclusion, the evidence needed to understand it, the decision,
and the condition for reopening it. Detailed JMH output belongs in benchmark result files.

### Rules

1. Check this log before proposing or repeating an optimization.
2. Do not rerun an unchanged experiment unless the workload, implementation, environment, or
   measurement quality changes materially. State what changed and what question the rerun answers.
3. Record conclusions as exploratory or confirmed. Include benchmark configuration and environment
   only at the level needed to judge reliability.
4. Compare equivalent workloads; retain tuple-count and checksum validation and the independent,
   incrementally-pruned nested-stream baseline.
5. Do not change the default strategy based on one scenario or a short/noisy run.
6. Record "no change" decisions and the evidence required to reopen them. Never silently reverse
   an earlier decision; add a new entry explaining why the evidence changed.

### Focused strategy comparison: deep dead-end and small-domain scenarios

**Status: exploratory; no implementation or dispatch change justified yet.**

A focused JMH run (JMH 1.37, OpenJDK 26.0.1, one 200 ms warmup, one 200 ms measurement,
one fork, GC profiler) compared PRUNED_BITSET, PRUNED_DFS, DP, ADAPTIVE, and NESTED_STREAM
across 14 scenarios.

- **Deep dead-end:** DP was fastest in four of five cases; PRUNED_DFS led the remaining 5x10
  case. PRUNED_BITSET was close but did not establish a consistent lead. ADAPTIVE was steady
  but did not lead these cases.
- **Small-domain, chain, and complete cases:** the fastest builder strategy beat the nested-stream
  baseline in all tested scenarios. The observed advantage was about 3.7x–5.4x outside the
  extreme deep-dead-end cases.
- **Extreme deep-dead-end results:** builder speedups ranged from about 5.9x to over 2,000x.
  Treat these as workload-specific evidence of incremental pruning, not a general performance claim.
- **Allocation:** builder strategies allocated substantially less in the tested cases. For example,
  DEEP_DEAD_END-7x25 was about 59 KB/op for the best builder strategy versus about 247 MB/op
  for nested streams. This is allocated bytes per operation, not retained or peak live heap.

The run was too short to provide useful confidence intervals. Small differences between builder
strategies are not reliable rankings.

**Decision:** retain all four strategies; do not change strategy selection or defaults from this run.
DP is the leading candidate for further investigation on deep-dead-end workloads, not a confirmed
winner. The builder approach remains promising against the equivalent nested-stream baseline
for these tested scenarios.

**Next evidence required:** rerun the same scenario/implementation matrix with at least
`-wi 5 -i 8 -w 1s -r 1s -f 2 -tu ms -bm avgt -prof gc`. Review score errors, allocation,
correctness checks, and regressions before changing implementation or dispatch.

### Decision register

| Topic | Decision | Reopen when |
|---|---|---|
| DP vs PRUNED_BITSET on deep dead ends | No change; DP is only a provisional candidate. | A multi-fork run shows a repeatable advantage. |
| PRUNED_DFS | Retain; it led the exploratory 5x10 case. | Confirmatory results show it is consistently dominated or regresses elsewhere. |
| ADAPTIVE | Retain; no demonstrated lead in the focused deep-dead-end cases. | A defined policy wins robustly across representative scenarios. |
| Builder vs nested streams | Builder approach is promising on tested workloads; do not generalize extreme ratios. | Equivalent, verified workloads contradict the result. |
| Default strategy / dispatch | No change from this exploratory run. | Confirmatory performance evidence and correctness tests justify it. |

