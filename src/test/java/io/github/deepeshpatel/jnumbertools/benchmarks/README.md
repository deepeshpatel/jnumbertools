# Benchmarks

JMH benchmarks for developers of JNumberTools. They are not part of the published library. Committed
`*BenchmarkOutput.txt` files hold selected historical results; new benchmark runs may write machine-readable
results under Maven's `target/` directory.

| Class | Output file | What it measures |
|---|---|---|
| `GeneratorSurveyBenchmark` | `GeneratorSurveyBenchmarkOutput.txt` | `lexOrder`, `lexOrderMth`, `choice`, unranking for every generator family |
| `ProductTupleBenchmark` | `ProductTupleBenchmarkOutput.txt` | Cartesian product tuple handling |
| `OptimisationCandidatesBenchmark` | `OptimisationCandidatesBenchmarkOutput.txt` | Comparisons of candidate optimisations (for example derangement lexOrder) |
| `ConditionalProductStrategyBenchmark` | `target/conditional-product-strategy.json` | Full traversal and end-to-end comparison of all four conditional-product strategies against a nested-stream baseline |

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

Run the complete conditional-product strategy matrix and save machine-readable results:

```bash
mvn clean test-compile exec:exec -Dexec.executable=java -Dexec.classpathScope=test \\
  -Dexec.args="-cp %classpath org.openjdk.jmh.Main ConditionalProductStrategyBenchmark -prof gc -rf json -rff target/conditional-product-strategy.json"
```

The conditional-product matrix uses one fork, one 500 ms warmup iteration, and two 500 ms measurement
iterations. It fully consumes each scenario and compares `PRUNED_BITSET`, `PRUNED_DFS`, `DP`,
`ADAPTIVE`, and `NESTED_STREAM`. It reports traversal-only and end-to-end measurements. Scenarios cover
STAR_FIRST, STAR_LAST, CHAIN, CYCLE, SPARSE, DENSE, DEAD_END, and EMPTY topologies, with bounded
dimension/list-size combinations.

Keep JMH's normal progress output enabled: it reports run progress and an ETA. Benchmark methods themselves
do not print. The JSON result file captures scores and `-prof gc` allocation metrics without requiring
per-scenario console summaries.

Replace the class and method names to run another benchmark.

## Updating the committed results

- Re-run a benchmark and replace its output file only when the code it measures has changed.
- Run it on the machine above, with nothing heavy open, and keep `-prof gc` so allocation (`gc.alloc.rate.norm`) is recorded.
- Allocation per operation is far more stable than time; compare it first when looking for a regression.
- A row whose error is close to or larger than its score is noise; do not quote it.
- Update the commit hash in the table above when results are replaced.
