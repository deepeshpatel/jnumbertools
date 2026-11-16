# Benchmarks

JMH benchmarks for developers of JNumberTools. They are not part of the published library. Committed
`*BenchmarkOutput.txt` files hold selected historical results; new benchmark runs may write machine-readable
results under Maven's `target/` directory.

| Class | Output file | What it measures |
|---|---|---|
| `GeneratorSurveyBenchmark` | `GeneratorSurveyBenchmarkOutput.txt` | `lexOrder`, `lexOrderMth`, `choice`, unranking for every generator family |
| `ProductTupleBenchmark` | `ProductTupleBenchmarkOutput.txt` | Cartesian product tuple handling |
| `OptimisationCandidatesBenchmark` | `OptimisationCandidatesBenchmarkOutput.txt` | Comparisons of candidate optimisations (for example derangement lexOrder) |
| `ConditionalProductStrategyBenchmark` | `target/conditional-product-strategy.json` | Full traversal and end-to-end comparison of all four conditional-product strategies against a nested-stream baseline; cases are loaded from the hierarchical resource tree under `src/test/resources/benchmarks/conditionalproduct/cases` |

## Benchmark environment

The committed results were measured on the machine and JVM below, so they are comparable with each other.
They are not absolute speed claims for other hardware, JVMs or operating systems.

| | |
|---|---|
| Machine | MacBook Air (Mac17,4), Apple M5, 10 cores (4 performance, 6 efficiency), 16 GB RAM |
| OS | macOS 26.4 |
| JDK | OpenJDK 64-Bit Server VM 27.0.1.0.3 (Homebrew), no extra JVM flags |
| Build target | Java 21 |
| Power | AC power, Low Power Mode off |
| Library version | 4.1.0 (commit 6d1266f) |
| JMH | 1.37; `GeneratorSurveyBenchmark`: 2 forks, 5 warmup and 8 measurement iterations of 300 ms each, 1 thread, average time, `-prof gc` |
| Notes | Fanless laptop, so long runs can be slightly throttled. No other heavy work was running; a browser and a chat app were open but idle. In the survey most rows vary by less than ±3% between iterations; a few are noisier (up to about ±13%). |

## Running

Run one benchmark row:

```bash
mvn clean test-compile exec:exec -Dexec.executable=java -Dexec.classpathScope=test \
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main GeneratorSurveyBenchmark.lexOrder1000 -p generator=permutation-k -prof gc"
```

Run one conditional-product case, one strategy, and one measurement:

```bash
mvn clean test-compile exec:exec -Dexec.executable=java -Dexec.classpathScope=test \
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main ConditionalProductStrategyBenchmark.traversalOnly -p scenarioName=topology/star/Dim2-5_Size10-100/selectivity/high/STAR_FIRST_5x10_HIGH -p implementation=PRUNED_BITSET -prof gc"
```

Change the method to `endToEnd` to include builder construction, rule registration, generator creation,
and traversal. Change `implementation` to `PRUNED_DFS`, `DP`, `ADAPTIVE`, or `NESTED_STREAM`.

Run all five implementations for one case by omitting `-p implementation=...`. Run both measurement
methods by using `ConditionalProductStrategyBenchmark` instead of a method-qualified benchmark name.
Omit both parameter filters to run the complete matrix. To save the full result set as JSON:

```bash
mvn clean test-compile exec:exec -Dexec.executable=java -Dexec.classpathScope=test \
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main ConditionalProductStrategyBenchmark -prof gc -rf json -rff target/conditional-product-strategy.json"
```

### Conditional-product case hierarchy

Each case is a `.properties` file. Its path is also its JMH `scenarioName` parameter (without the
`.properties` suffix). The hierarchy is:

```text
src/test/resources/benchmarks/conditionalproduct/cases/
├── topology/
│   ├── star/
│   │   └── Dim2-5_Size10-100/
│   │       └── selectivity/
│   │           ├── high/
│   │           └── low/
│   └── chain/
│       ├── Dim2-5_Size10-100/
│       │   └── selectivity/
│       │       ├── medium/
│       │       └── tight/
│       ├── Dim6-10_Size10-100/
│       │   └── selectivity/tight/
│       ├── Dim11-20_Size10-100/
│       │   └── selectivity/tight/
│       ├── Dim2-5_Size101-250/
│       │   └── selectivity/tight/
│       ├── Dim6-10_Size101-250/
│       │   └── selectivity/tight/
│       └── Dim11-20_Size101-250/
│           └── selectivity/tight/
└── edge-cases/
    ├── small-domain/
    ├── dead-end/
    └── empty-result/
```

The six numeric range names use inclusive, non-overlapping bounds:

| Folder component | Meaning |
|---|---|
| `Dim2-5` | 2 through 5 dimensions |
| `Dim6-10` | 6 through 10 dimensions |
| `Dim11-20` | 11 through 20 dimensions |
| `Size10-100` | 10 through 100 values per dimension |
| `Size101-250` | 101 through 250 values per dimension |

After the selectivity folder, the remaining two hierarchy levels are selected by JMH rather than
duplicated as resource directories for every workload:

- **Strategy:** the `implementation` parameter: `PRUNED_BITSET`, `PRUNED_DFS`, `DP`,
  `ADAPTIVE`, or `NESTED_STREAM`.
- **Measurement:** the benchmark method: `traversalOnly` or `endToEnd`.

This keeps the workload tree concrete while avoiding five copies of every case descriptor and two
copies of every strategy implementation under every leaf. The JMH selector combination identifies
the exact leaf workload → strategy → measurement to execute.

The size range describes input-domain size, not the number of valid output tuples. Current case
descriptors use a uniform size for all dimensions. The `edge-cases` group deliberately holds workloads
outside these ranges or workloads whose defining property is an empty result or dead end.

A case descriptor contains `topology`, `selectivity`, and `dimensions`. Use `size` as a
uniform-size shorthand, or `sizes` as a comma-separated list with one value per dimension; every value
must fall within the folder's declared size range. `emptyDimension` is optional. The benchmark loads
descriptors from the classpath, so adding a case does not require editing the JMH benchmark's scenario
factory; add the new resource path to the benchmark state's `@Param` list so JMH includes it in the
default matrix. The current descriptors use uniform sizes, but mixed-size workloads are supported. The
`TIGHT` profile permits approximately 1% of each target dimension (at least one value), keeping the
larger range cases bounded.

Important interpretation note: the current rule generator selects every trigger value, so
the `high`/`medium`/`low`/`tight` profile currently controls the allowed target-value fraction;
it does not yet vary trigger coverage. These are therefore target-restriction profiles, not a full
trigger-selectivity study. The separate trigger-coverage dimension should be added before drawing
conclusions about conditional-trigger selectivity.

The matrix uses one fork, one 500 ms warmup iteration, and two 500 ms measurement iterations. It fully
consumes each scenario and compares `PRUNED_BITSET`, `PRUNED_DFS`, `DP`, `ADAPTIVE`, and
`NESTED_STREAM`. Both traversal-only and end-to-end measurements are available. The largest existing
case is designed to remain bounded; verify setup's baseline count before adding larger workloads.

Keep JMH's normal progress output enabled: it reports run progress and an ETA. Benchmark methods do not
print. The JSON result file captures scores and `-prof gc` allocation metrics without per-scenario
console summaries.

## Updating the committed results

- Re-run a benchmark and replace its output file only when the code it measures has changed.
- Run it on the machine above, with nothing heavy open, and keep `-prof gc` so allocation (`gc.alloc.rate.norm`) is recorded.
- Allocation per operation is far more stable than time; compare it first when looking for a regression.
- A row whose error is close to or larger than its score is noise; do not quote it.
- Update the commit hash in the table above when results are replaced.
