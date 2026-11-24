# Benchmark results

This directory contains selected, reproducible benchmark runs committed as supporting engineering evidence.

## What belongs here

- Keep exploratory, smoke, and regression-screening output local in the git-ignored `benchmark-results/` directory.
- Commit only completed runs that are useful for reviewing or comparing a specific implementation.
- Keep each run's raw JMH JSON and the matching environment record together. Include console logs when they add useful diagnostic information.
- Do not edit raw JMH JSON to create summary tables. Generate summaries from the raw result files and keep interpretation in the relevant documentation.
- Do not compare results across different benchmark implementations, workloads, JDK/JVM versions, hardware, or JMH settings without explicitly accounting for those differences.

## Directory convention

Use one directory per run date:

```text
benchmarks/results/
├── README.md
└── YYYY-MM-DD/
    ├── README.md                 # optional run notes / exact commands
    ├── environment.txt           # output from BenchmarkEnvironment
    ├── strategy.json             # unified strategy/topology JMH run
    ├── end-to-end.json           # API-level end-to-end JMH comparison
    ├── lambda-vs-compound.json    # equivalent multi-dimensional workload
    ├── special-operations-bounded.json
    ├── special-operations-scaling.json
    └── special-operations-empty.json
```

Only add files for benchmarks that were actually run. The names above are the suggested names for the corresponding result categories; use a distinct run/date directory if the same category is repeated under a different configuration.

## Required run metadata

The environment record should be generated from the same checkout used for the benchmark, using `BenchmarkEnvironment`. Preserve:

- Git commit SHA and branch; state whether the working tree was clean.
- Run timestamp, JDK vendor/version, JVM name/version and JVM arguments.
- OS/architecture, CPU model, logical processor count, physical memory and maximum heap.
- JMH version, warmup/measurement iterations, fork count, benchmark parameters and profiler. JMH JSON records most of the JMH-specific configuration; retain the exact command as well.

A benchmark result is not a clean comparison if the source commit, workload, or measurement configuration cannot be identified.

## Naming and interpretation

Prefer descriptive, stable file names. If multiple runs of the same category are needed on one date, add a short suffix (for example, `strategy-full-m5.json`). Keep the raw results unchanged. Label short runs as screening evidence and include score errors and allocation data when interpreting them.
