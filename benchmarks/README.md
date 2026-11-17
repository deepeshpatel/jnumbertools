# JNumberTools Benchmarks

This is a standalone Maven module containing JMH benchmarks for JNumberTools maintainers. Benchmark
code and JMH dependencies are isolated from the library's production and test source sets; they are
not included in the published library artifact.

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
├── pom.xml
├── README.md
└── src/main/java/io/github/deepeshpatel/jnumbertools/benchmarks/
    └── conditionalproduct/
        ├── anyorder/
        ├── baseline/
        ├── chain/
        ├── complete/
        ├── deadend/
        ├── manyvalid/
        ├── scenario/
        └── support/
```

Future API families should get their own domain package (for example, `permutation/`,
`combination/`, `derangement/`, or `product/`) rather than adding unrelated benchmark classes
to the root.

## Conditional-product benchmarks

The parameterized JMH classes compare these implementations:

- `PRUNED_BITSET`
- `PRUNED_DFS`
- `DP`
- `ADAPTIVE`
- `NESTED_STREAM` — independent incrementally filtered nested Java Streams baseline

Each topology has one benchmark class. The `shape` parameter selects 3, 5, 7, or 10 dimensions
and 10, 25, or 50 values per dimension. The `implementation` parameter selects the library strategy
or Java Stream baseline. The benchmark includes builder construction, rule registration, generator
creation, and complete tuple consumption. Trial setup checks each selected library strategy's tuple
count and checksum against the nested-stream baseline.

| Folder | Scenario | Valid-output budget | Shapes |
|---|---|---:|---|
| `chain` | Chain-shaped restrictions | 30,000 | All 12 |
| `manyvalid` | Chain-shaped restrictions with a larger branching factor | 75,000 | All 12 |
| `complete` | Chain-shaped restrictions with a small output budget | 1,000 | All 12 |
| `deadend` | Controlled conflicting restrictions | 30,000 | 11 (omits 10×50) |

The `deadend` folder omits 10×50 because its estimated output exceeds the intended benchmark budget.
The scenario definitions and rule data are in `scenario/`; the independent nested-stream implementation
is in `baseline/`; shared builder and verification logic is in `support/`.

## Dead-end benchmark: initial results

The first reported end-to-end run of `deadend/ConditionalProductBenchmark` shows the
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

The first complete reported run of `chain/ConditionalProductBenchmark` used
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

## Any-order parallel benchmark

`anyorder/AnyOrderParallelBenchmark` compares sequential and parallel consumption of
`anyOrder().stream()`. It includes traversal-only and end-to-end methods and validates sequential
and parallel checksums during trial setup. This is a separate comparison: `anyOrder()` is not the
lexicographic conditional-product strategy parameterized by the topology benchmarks.

## Building and running benchmarks

The benchmark module depends on the matching JNumberTools artifact version. From the repository root,
install the library into the local Maven repository first:

```bash
mvn clean install -DskipTests
```

Then build the standalone JMH module:

```bash
mvn -f benchmarks/pom.xml clean package
```

This creates the executable JMH jar at `benchmarks/target/benchmarks.jar`. Run all shapes and
implementations for one topology:

```bash
java -jar benchmarks/target/benchmarks.jar '.*deadend.ConditionalProductBenchmark' -prof gc
```

Replace `deadend` with `chain`, `manyvalid`, or `complete` to select another topology. Run only
the any-order comparison with:

```bash
java -jar benchmarks/target/benchmarks.jar '.*AnyOrderParallelBenchmark.*' -prof gc
```

The topology benchmark annotations use short smoke-test defaults (one fork, one 100 ms warmup,
one 100 ms measurement). Override them for meaningful performance comparisons; for example:

```bash
java -jar benchmarks/target/benchmarks.jar '.*deadend.ConditionalProductBenchmark' \
  -wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc
```

The benchmark module is intentionally independent of the root Maven lifecycle. If the library version
changes, update `jnumbertools.version` in `benchmarks/pom.xml` to match the root project version,
then reinstall the library before rebuilding the benchmarks.

Do not compare timings from different machines or JVM configurations as if they were directly equivalent.
Always inspect score error and allocation per operation before drawing conclusions.
