# Benchmarks

JMH benchmarks for developers of JNumberTools. They are not part of the published library. Committed
`*BenchmarkOutput.txt` files hold selected historical results; new benchmark runs may write machine-readable
results under Maven's `target/` directory.

| Class | Output file | What it measures |
|---|---|---|
| `GeneratorSurveyBenchmark` | `GeneratorSurveyBenchmarkOutput.txt` | `lexOrder`, `lexOrderMth`, `choice`, unranking for every generator family |
| `ProductTupleBenchmark` | `ProductTupleBenchmarkOutput.txt` | Cartesian product tuple handling |
| `OptimisationCandidatesBenchmark` | `OptimisationCandidatesBenchmarkOutput.txt` | Comparisons of candidate optimisations (for example derangement lexOrder) |

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

## Conditional-product benchmarks

Conditional-product benchmarks are organized by workload family and fixed input shape. Each class
runs one topology/profile, one dimension count, and one uniform domain size; JMH's
`implementation` parameter compares `PRUNED_BITSET`, `PRUNED_DFS`, `DP`, `ADAPTIVE`,
and an incrementally filtered nested-stream baseline.

The source layout is:

```text
conditionalproduct/
├── ConditionalProductCaseSupport.java
├── baseline/
├── scenario/
├── deadend/
│   ├── Benchmark_3by10.java
│   ├── Benchmark_3by25.java
│   └── ...
├── manyvalid/
│   └── Benchmark_<dimensions>by<size>.java
└── complete/
    └── Benchmark_<dimensions>by<size>.java
```

The fixed-case classes use one fork, one 100 ms warmup iteration, and one 100 ms measurement
iteration. Each class has one benchmark method and five implementation parameter values. This keeps
a single class run comfortably below one minute for the supplied bounded workloads. The
`deadend/Benchmark_10by50.java` case is deliberately omitted: its estimated full output is too
large for the one-minute limit. The bounded `manyvalid` and `complete` profiles include the
10-by-50 case.

Build the test classpath once:

```bash
mvn test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt
```

Run exactly one fixed case (all five implementations):

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  org.openjdk.jmh.Main '.*deadend.Benchmark_5by10'
```

Change the class selector to any other `Benchmark_<dimensions>by<size>` class under
`deadend`, `manyvalid`, or `complete`. JMH prints the score for each implementation. The
benchmark includes builder construction, rule registration, generator creation, and complete
consumption. Trial setup independently checks the selected implementation's tuple count and
checksum against the nested-stream baseline.

The nested-stream baseline applies constraints as soon as both participating dimensions have been
selected. It does **not** generate the full Cartesian product and apply one final filter.

### Current controlled DEAD_END measurements

These are the maintainer's initial end-to-end results for the controlled dead-end profile, measured
with one fork, one warmup iteration, and two 500 ms measurement iterations. Times are ms/op.
The results are quick-glance measurements, not publication-grade estimates.

| Scenario | PRUNED_BITSET | PRUNED_DFS | DP | ADAPTIVE | NESTED_STREAM |
|---|---:|---:|---:|---:|---:|
| 5×10 | 0.004 | 0.004 | 0.007 | 0.004 | 0.006 |
| 5×25 | 0.053 | **0.045** | 0.102 | 0.052 | 0.173 |
| 5×50 | **0.860** | 0.949 | 1.611 | 1.768 | 3.217 |
| 7×10 | 0.007 | 0.008 | 0.017 | 0.007 | 0.013 |
| 7×25 | **1.600** | 1.632 | 2.256 | 1.639 | 5.686 |
| 7×50 | 157.901 | **141.047** | 167.611 | 167.153 | 432.660 |

Initial observations:

- No pruning strategy wins consistently. BITSET leads at 5×50 and 7×25; DFS leads at 5×25
  and 7×50. At 5×10 and 7×10, the three pruning strategies are effectively tied at this precision.
- DP is slower than the pruning strategies in these six cases.
- Incrementally filtered nested streams are slower in all six cases, with the gap widening on
  larger workloads.
- ADAPTIVE does not consistently match the fastest explicit strategy. Its selection heuristic
  should be evaluated against the full topology matrix before changing it.
- Doubling domain size from 25 to 50 causes a large increase in work. The growth depends on the
  number of dimensions and the valid-prefix structure, not just the raw domain size.

These findings apply only to this controlled DEAD_END profile and this machine/JVM. Do not infer a
universal strategy ranking from six cases or from two measurement iterations. The other workload
families are included to test whether the pattern holds for many-valid and complete-result shapes.

## Updating the committed results

- Re-run a benchmark and replace its output file only when the code it measures has changed.
- Run it on the machine above, with nothing heavy open, and keep `-prof gc` so allocation (`gc.alloc.rate.norm`) is recorded.
- Allocation per operation is far more stable than time; compare it first when looking for a regression.
- A row whose error is close to or larger than its score is noise; do not quote it.
- Update the commit hash in the table above when results are replaced.
