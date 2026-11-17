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

## Any-order parallel benchmark

`anyorder/AnyOrderParallelBenchmark` compares sequential and parallel consumption of
`anyOrder().stream()`. It includes traversal-only and end-to-end methods and validates sequential
and parallel checksums during trial setup. This is a separate comparison: `anyOrder()` is not the
lexicographic conditional-product strategy parameterized by the topology benchmarks.

## Running conditional-product benchmarks

Build the test classpath once:

```bash
mvn test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt
```

Run all shapes and implementations for one topology:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  org.openjdk.jmh.Main '.*deadend.Benchmark' -prof gc
```

Replace `deadend` with `chain`, `manyvalid`, or `complete` to select another topology. Run only the
any-order comparison with:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  org.openjdk.jmh.Main '.*AnyOrderParallelBenchmark.*' -prof gc
```

The topology benchmark annotations use short smoke-test defaults (one fork, one 100 ms warmup,
one 100 ms measurement). Override them for meaningful performance comparisons; for example:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  org.openjdk.jmh.Main '.*deadend.Benchmark' -wi 5 -i 8 -f 2 -tu ms -bm avgt -prof gc
```

Do not compare timings from different machines or JVM configurations as if they were directly equivalent.
Always inspect score error and allocation per operation before drawing conclusions.
