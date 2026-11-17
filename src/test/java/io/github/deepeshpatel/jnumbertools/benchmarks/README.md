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

## Conditional-product benchmarks

Conditional-product benchmarks are organized by topology and fixed input shape. Each class
fixes one dimension count and one uniform domain size; JMH's `implementation` parameter compares `PRUNED_BITSET`, `PRUNED_DFS`, `DP`, `ADAPTIVE`,
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
└── chain/
    └── Benchmark_<dimensions>by<size>.java
```

The fixed-case classes use one fork, one 100 ms warmup iteration, and one 100 ms measurement
iteration. Each class has one benchmark method. A `deadend` class has five implementation parameter
values; a `chain` class has two profile values (`MANY_VALID`, `COMPLETE`) crossed with the five
implementations. This keeps each supplied class run below one minute for the bounded workloads.
The `deadend/Benchmark_10by50.java` case is deliberately omitted: its estimated full output is too
large for the one-minute limit. The `chain` profiles cap full output at 75,000 and 1,000 tuples,
respectively, including the 10-by-50 case.

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
`deadend` or `chain`. JMH prints the score for each implementation (and, for `chain`, each
profile). The
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
- Incrementally filtered nested streams are slower in all six cases; the absolute time gap becomes
  substantially larger on the bigger workloads.
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
