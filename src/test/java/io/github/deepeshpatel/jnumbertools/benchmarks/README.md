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

This section is the durable record of conditional-product performance experiments and decisions.
Read it before proposing or implementing another optimization. Update it whenever a benchmark
result is analyzed or an implementation strategy is changed. The purpose is to prevent repeated
experiments and contradictory recommendations across contributors and AI assistants.

### Rules for future optimization work

1. **Read this log first.** Search for the proposed optimization, the affected topology, and any
   previous decision before suggesting code changes.
2. **Do not repeat a failed or inconclusive experiment unchanged.** A repeat is justified only when
   the benchmark, workload, implementation, JDK, or measurement quality has materially changed.
   State exactly what changed and what question the rerun answers.
3. **Separate evidence from hypotheses.** Label results as exploratory, confirmatory, or rejected.
   Record the exact benchmark command/configuration, JDK, JVM, hardware when known, and source
   revision. Never promote a single short run into a general performance claim.
4. **Preserve the baseline.** Compare equivalent complete workloads and verify tuple counts and
   checksums. Keep the independent incrementally-pruned nested-stream baseline; do not replace it
   with a less efficient full-product-then-filter baseline.
5. **Do not optimize from a single winner.** Compare all relevant scenarios, allocation per operation,
   score errors, and regressions. A strategy that wins one topology is not automatically the default.
6. **Record decisions, including "do not change".** For every investigated idea, record the question,
   evidence, conclusion, decision, and what new evidence would justify reopening it.
7. **No silent reversals.** If a later result contradicts a recorded decision, add a dated entry
   explaining the contradiction and superseding evidence. Keep the earlier entry for history.

### Focused strategy comparison: DEEP_DEAD_END and SMALL_DOMAIN

**Status: exploratory; not sufficient to select or change the default strategy.**

A focused run compared PRUNED_BITSET, PRUNED_DFS, DP, ADAPTIVE, and NESTED_STREAM
across 14 scenarios. Configuration:

- JMH 1.37; OpenJDK 26.0.1, 64-bit Server VM.
- One warmup iteration and one measurement iteration, each 200 ms; one fork.
- Average time in milliseconds; GC profiler enabled.
- Scenarios: DEEP_DEAD_END-5x10, DEEP_DEAD_END-5x25, DEEP_DEAD_END-7x10,
  DEEP_DEAD_END-7x25, DEEP_DEAD_END-10x10, SMALL_DOMAIN-8x4,
  SMALL_DOMAIN-10x4, SMALL_DOMAIN-12x3, SMALL_DOMAIN-14x3,
  SMALL_DOMAIN-8x5, CHAIN-5x10, CHAIN-7x10, COMPLETE-5x10,
  COMPLETE-7x10.
- All five implementations were measured.
- Because this was only one short warmup/measurement and one fork, JMH reported no useful
  confidence interval. Treat all rankings and ratios below as directional observations only.

#### Deep-dead-end results

| Scenario | PRUNED_BITSET (ms/op) | PRUNED_DFS (ms/op) | DP (ms/op) | ADAPTIVE (ms/op) | Fastest |
|---|---:|---:|---:|---:|---|
| 5x10 | 0.196 | 0.187 | 0.205 | 0.224 | PRUNED_DFS |
| 5x25 | 0.085 | 0.085 | 0.084 | 0.095 | DP |
| 7x10 | 0.014 | 0.017 | 0.013 | 0.014 | DP |
| 7x25 | 0.045 | 0.049 | 0.041 | 0.045 | DP |
| 10x10 | 0.017 | 0.017 | 0.014 | 0.015 | DP |

Observations:

- DP was fastest in four of the five measured deep-dead-end cases; PRUNED_DFS led 5x10.
- PRUNED_BITSET was close to DP, but did not win these cases except that its 5x10 result
  was close to the best. The measured gaps are too small and the run too weak to establish a
  reliable ranking.
- ADAPTIVE was consistent but did not lead any of these five cases.
- Allocation also favoured DP in these cases. For example, at 7x25, DP allocated about
  59 KB/op, compared with about 64 KB/op for PRUNED_BITSET, 67 KB/op for PRUNED_DFS,
  and 64 KB/op for ADAPTIVE.

**Decision:** Do not change or remove any strategy based on this run. In particular, do not
assume that topology-aware lookahead makes PRUNED_BITSET the best deep-dead-end strategy.
The current evidence instead makes DP the candidate to investigate for these cases, but a
proper multi-fork run is required before any selection or dispatch change.

#### Builder strategies versus nested streams

The fastest of the four builder strategies in each scenario was faster than NESTED_STREAM
in this exploratory run:

| Scenario | Fastest builder time (ms/op) | Nested Stream (ms/op) | Approx. speedup |
|---|---:|---:|---:|
| DEEP_DEAD_END-5x10 | 0.187 | 1.092 | 5.9x |
| DEEP_DEAD_END-5x25 | 0.084 | 70.829 | 841.9x |
| DEEP_DEAD_END-7x10 | 0.013 | 30.444 | 2,388.8x |
| DEEP_DEAD_END-7x25 | 0.041 | 76.106 | 1,868.8x |
| DEEP_DEAD_END-10x10 | 0.014 | 10.962 | 775.3x |
| CHAIN-5x10 | 0.336 | 1.446 | 4.3x |
| CHAIN-7x10 | 0.150 | 0.677 | 4.5x |
| COMPLETE-5x10 | 0.016 | 0.086 | 5.4x |
| COMPLETE-7x10 | 0.019 | 0.100 | 5.3x |
| SMALL_DOMAIN-8x4 | 0.200 | 0.864 | 4.3x |
| SMALL_DOMAIN-10x4 | 1.877 | 9.108 | 4.9x |
| SMALL_DOMAIN-12x3 | 0.208 | 1.019 | 4.9x |
| SMALL_DOMAIN-14x3 | 0.840 | 3.670 | 4.4x |
| SMALL_DOMAIN-8x5 | 1.590 | 5.950 | 3.7x |

The large deep-dead-end ratios are especially sensitive to how much search the baseline performs.
They are evidence for the benefit of incremental pruning on these particular workloads, not a
general speedup promise for all conditional products. Confirm semantic equivalence and inspect
the scenario construction before using these ratios in external claims.

Normalized allocation (gc.alloc.rate.norm) also showed substantially lower allocation for
the builder strategies. At DEEP_DEAD_END-7x25, the best builder strategy allocated about
59 KB/op versus about 247 MB/op for NESTED_STREAM. This is per-operation allocated data,
not retained heap or peak live memory.

#### Follow-up experiment required

Repeat the same 14 scenarios and five implementations with a statistically stronger configuration
before making strategy-selection decisions:

    -wi 5 -i 8 -w 1s -r 1s -f 2 -tu ms -bm avgt -prof gc

Retain the complete JMH output, including score errors, allocation metrics, JDK/JVM metadata,
hardware, exact command, and source commit. Compare the same scenario/implementation pairs.
Do not change strategy selection until that run has been reviewed.

### Current decision register

| Topic | Current decision | Reopen only when |
|---|---|---|
| DP versus PRUNED_BITSET on DEEP_DEAD_END | No change; DP is the provisional candidate, not a confirmed winner. | A multi-fork run confirms a repeatable time/allocation advantage across the relevant cases. |
| PRUNED_DFS | Retain; it led the exploratory 5x10 case. | Stronger evidence shows it is consistently dominated or has unacceptable regressions. |
| ADAPTIVE | Retain pending stronger evidence; it was not the fastest in the focused deep-dead-end cases. | A defined adaptive policy demonstrates robust wins across a representative scenario matrix. |
| Builder versus NESTED_STREAM | Current focused run supports the builder approach for the tested cases; do not generalize the extreme ratios. | Equivalent-workload benchmarks on additional representative workloads or environments contradict this. |
| Default strategy / dispatch changes | No change based on the short focused run. | Confirmatory JMH evidence plus correctness tests support the change. |

