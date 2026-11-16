# Conditional-product JMH benchmarks

Run the complete scenario/implementation matrix:

```bash
mvn test-compile exec:java \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductBenchmarkRunner
```

Run one method for one scenario and implementation:

```bash
mvn test-compile exec:java \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductBenchmarkRunner \
  -Dexec.args="--method=traversalOnly --scenario=topology/chain/Dim11-20_Size101-250/selectivity/tight/CHAIN_11x101_TIGHT --implementation=PRUNED_BITSET"
```

Supported selectors:

- `--method=traversalOnly|endToEnd`
- `--scenario=<case resource path without .properties>`
- `--implementation=PRUNED_BITSET|PRUNED_DFS|DP|ADAPTIVE|NESTED_STREAM`
- `--output-dir=<directory>` (default: `target/benchmarks`)
- `--no-gc` to disable the default GC profiler

The runner leaves JMH progress and diagnostics on the console. It writes the complete JSON result to
`target/benchmarks/ConditionalProductStrategyBenchmark_results.json` and writes one final summary
per executed method:

- `ConditionalProductStrategyBenchmark.traversalOnly_output.txt`
- `ConditionalProductStrategyBenchmark.endToEnd_output.txt`

The text files contain the final primary score, error, parameters, and secondary profiler metrics.
