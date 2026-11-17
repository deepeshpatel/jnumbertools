# Conditional-product JMH benchmarks

## Run one small benchmark at a time

Build the test classpath once:

```bash
mvn test-compile dependency:build-classpath -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt
```

Run one scenario across all five implementations:

```bash
java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
  io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductBenchmarkRunner \
  --method=endToEnd \
  --scenario=phase-e/CONTROLLED_DEAD_END/7x25 \
  --no-gc
```

The benchmark annotations configure one warmup iteration, two measurement
iterations, and one fork. This is intended as a quick comparison, not a final
publication-quality measurement. Remove `--no-gc` when allocation and GC
profiling are wanted.

Each invocation writes its own JSON and text result files under
`target/benchmarks`, named with the selected scenario and method. Running
different scenarios therefore does not overwrite earlier scenario results.

## Controlled DEAD_END profile

The `phase-e/CONTROLLED_DEAD_END/<dimensions>x<size>` cases isolate domain-size and
dimension-count effects more cleanly than the original bounded `DEAD_END` profile.

The controlled profile holds these ratios constant:

- Allowed values on each constrained dimension: 20% of the domain (rounded up).
- Trigger values for each conditional restriction: 80% of the domain (rounded up).
- The two conditional restrictions on dimension 2 allow disjoint halves of the
  allowed range, preserving the conflicting/dead-end behavior.
- Dimensions 3 onward remain attached to dimension 2, keeping the same topology
  across the tested dimension counts.

The original `DEAD_END` profile is retained unchanged. It uses a tuple budget to
choose its allowed branching factor, so its selectivity varies by shape. It is a
practical bounded-workload profile, not a controlled scaling experiment.

## Run the full controlled matrix as separate small runs

```bash
for dimensions in 3 5 7 10; do
  for size in 10 25 50; do
    scenario="phase-e/CONTROLLED_DEAD_END/${dimensions}x${size}"
    java -cp "target/test-classes:target/classes:$(cat target/test-classpath.txt)" \
      io.github.deepeshpatel.jnumbertools.benchmarks.conditionalproduct.ConditionalProductBenchmarkRunner \
      --method=endToEnd \
      --scenario="$scenario" \
      --no-gc || exit 1
  done
done
```

This produces 12 independent JSON files and 12 corresponding text files. Each
scenario is validated against the nested-stream baseline during JMH trial setup.
