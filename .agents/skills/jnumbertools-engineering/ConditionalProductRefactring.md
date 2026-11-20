# ConditionalProductBuilder: generator split, faster setup, parallel traversal

Working plan and progress tracker. **Read this file first when resuming. Update the Progress section after every step.**

## 0. How to work on this (maintainer workflow)

1. Read `SKILL.md` and `ValidationRules.md` before changing public behavior, validation, builders, generators, or tests.
2. Continue from the current branch state and the latest progress entry. Do not revive completed work packages without a concrete reason.
3. Run tests or JMH only when the maintainer asks for them. Never claim a test, build, or benchmark passed unless it was actually run and its result observed.
4. Push only when explicitly authorized. Keep the pull request open and do not merge unless the maintainer separately authorizes a merge.
5. Preserve the public behavioral contract and exact tuple set/order unless an API change is explicitly approved. Do not make public API changes silently.
6. Batch related release work; do not create release tags as part of engineering changes.
7. Keep strategies as generators (`XGenerator`), not builders. The public builder owns the fluent rule DSL; implementation collaborators remain package-private unless a public API need is established.

## 1. Why

Historical benchmark observations (JMH, maintainer's machine, `ConditionalProductStrategyBenchmark`, 3 to 5 dimensions, lists of 25 to 100). Treat these as provisional until the revised, equivalent-setup benchmarks are rerun:

| Finding | Numbers |
|---|---|
| Traversal-only performance | Earlier measurements suggested library prefix-pruned traversal can outperform nested Java streams; the comparison setup was later found not fully equivalent. Re-measure before relying on the ratio. |
| Setup dominates small jobs | builder construction is about 491 us (CHAIN), 76 us (STAR), 1.1 ms (CYCLE): 63 to 100% of the end-to-end time at 1,000 rows |
| End-to-end crossover | Earlier first-N results suggested workload-dependent crossovers, but setup costs were not equivalent. Re-measure with builder/rule construction included for every implementation. |
| `DP` loses on cycles | CYCLE end to end: 13 ms vs 1.1 ms (loop block enumeration, about 17 MB per run) |
| `ADAPTIVE` cutoff is unproven | from 10,000 tuples it picks `DP`, which is 12x worse on CYCLE |
| `PRUNED_DFS` has no winning region yet | traversal 2x slower than BitSet at 10,000 rows, 2.3x more allocation; kept until the many-small-lists shapes are measured |

Hypothesis for the setup cost (not yet profiled): every `withRule` copies the whole rule list, rebuilds a sorted set of linked pairs and re-runs the loop check, so building R rules costs O(R^2).

Goals: (1) cut setup cost for every strategy; (2) one shared constraint model, several interchangeable generators; (3) parallel traversal through the normal `stream().parallel()`.

## 1A. Performance objectives and operation boundaries

The implementation has two distinct optimization targets. Do not optimize one by making the other slower without measuring both.

### Full-result generation

- `lexOrder()` and any public any-order/full-generation operation must be optimized primarily for consuming the complete result set, not just the first few tuples.
- Sequential full traversal should avoid constructing exact-counting/DP state unless that state is genuinely needed by the selected generator.
- Preserve laziness and re-iterability; full-generation optimization does not permit eager materialization of all tuples.
- Measure traversal-only cost separately from end-to-end cost. A traversal-only benchmark may construct the builder and immutable rule model in trial setup; an end-to-end benchmark must include builder/rule construction, generator creation, and complete consumption.
- The public API currently exposes lexicographic generation and rank/random operations. Do not invent an any-order API as part of documentation: if an any-order full-generation API is added or already exists elsewhere, give it the same full-consumption performance objective and document its semantics explicitly.

### Special operations

- `count()`, `isEmpty()`, `lexOrderMth(...)`, `byRanks(...)`, `choice(...)`, and `sample(...)` are special operations, not ordinary full traversal.
- These operations may build and use the exact feasibility/counting plan, including loop-block processing and BigInteger/primitive count tables.
- Their end-to-end performance measurement must include builder construction and rule registration as well as the special operation. Also report operation-only measurements where useful, but never substitute those for end-to-end results.
- `isEmpty()` should use the least expensive exact feasibility mechanism that preserves semantics; it need not calculate full counts if a separate feasibility tier is sufficient.

### Public lexicographic strategy enum

`LexOrderStrategy` controls only ordinary lexicographic traversal. It does not select the implementation of `count`, rank, choice, or sample operations.

| Enum constant | Intended implementation | Contract / status |
|---|---|---|
| `PRUNED_BITSET` | Prefix-pruned DFS using compact BitSet relation rows | Required implementation; default candidate; avoids exact counting plan for ordinary traversal |
| `PRUNED_DFS` | Prefix-pruned indexed DFS using dense boolean relation rows | Required implementation; optimize it for its workload rather than removing it based on an early benchmark |
| `DP` | Exact DP completion-count lookahead | Required implementation; optimize setup and traversal for its workload, including cyclic/loop-heavy constraints |
| `ADAPTIVE` | Implementation policy; documented with no guarantee of which algorithm runs when (changes are recorded in the changelog and decision log). At present it resolves to `PRUNED_BITSET`; the estimated-limit argument is validated but unused | Entry point retained; decide whether to keep the parameter or rename before release (item 7 of the 2026-11-18 plan) |

**Retention rule: all four enum constants and their corresponding implementations must remain available throughout this work. No strategy is to be removed or deprecated as part of these work packages.** If a strategy performs poorly, improve its implementation and tests. Only after the complete flow is implemented and correctness-verified should the full benchmark suite be used to tune implementations, defaults, or the `ADAPTIVE` cutoff. Benchmark results may inform later maintainer decisions, but this plan does not authorize removing a strategy.

The current adaptive threshold (10,000 tuples) is provisional. The full-generation target is complete consumption, so benchmark coverage must include full traversal rather than infer the best strategy from first-N microbenchmarks.

## 1B. Reusable compiled-product API: evaluated and deferred

A compile-once/bind-many API was explored as a possible way to amortize product-definition preparation across repeated datasets. The proposed API was only illustrative; it was not approved as a public contract.

The end-to-end JMH comparison of the compiled conditional-product prototype against the ordinary builder showed no demonstrated benefit: the compiled path was near a tie for the smallest cases and slower for dimensions 15–25, while allocations were nearly identical. The ordinary builder therefore remains the supported API, and the compiled-product proposal is parked.

Do not implement or expose a compiled-product API as part of this work. Reopen the idea only if a representative repeated-dataset workload demonstrates a measurable end-to-end benefit after including binding and result consumption. Do not infer an advantage from reduced setup alone.

## 2. Target design

```
ConditionalProductBuilder        immutable facade: when/then/and, count, lexOrder, byRanks, choice, sample
        |  owns
        v
ConstraintModel                  shared, lazy, read-only: dimensions + rules
   |- Rows    BitSet tables per linked pair             built on first use
   |- Dense   boolean tables (only for DenseDfsGenerator, size-capped)
   |- Plan    feasibility tables                        isEmpty(), DpLexGenerator
   '- Counts  exact counts                              count(), ranks, sampling, DpLexGenerator
        |  read by
        v
Lex generators (package-private)
   |- PrunedLexGenerator (abstract: depth-first control flow, one hook "next valid candidate")
   |     |- BitSetPrunedGenerator   default
   |     '- DenseDfsGenerator       PRUNED_DFS, kept until data says otherwise
   '- DpLexGenerator                exact lookahead; splits by rank ranges
```

Decisions already made:

- Public enum `LexOrderStrategy`: `PRUNED_BITSET` (default), `PRUNED_DFS`, `DP`, `ADAPTIVE`. `ADAPTIVE` stays until a benchmark with dead-end shapes decides its cutoff.
- No stream-based generator is currently planned. Nested-stream baselines are useful for comparison, but must be benchmarked with equivalent rule/setup costs; they do not define the library's public generator architecture.
- Keep `PRUNED_DFS` as a permanent member of the strategy set during this work; optimize it for workloads such as thousands of tuples over small lists.
- Existing `lexOrder()` behaviour is out of scope for the new unordered traversal work. Do not extend or redesign its parallel implementation as part of the `anyOrder()` work; revisit it only after `anyOrder()` is complete, tested, and benchmarked.
- `anyOrder()` is a separate full-generation API for optimization, not randomization. Its contract will yield every valid tuple exactly once without promising encounter order. `choice()` and `sample()` remain the random-selection APIs.
- The intended `anyOrder()` policy is automatic sequential-versus-parallel selection using a workload threshold. The threshold and workload estimate must be supported by benchmark evidence; do not assume that first-dimension size alone is a sufficient estimate.
- Its parallel splitting design must expose useful independent work even when the first dimension has too few values. Evaluate deeper prefix/frontier splitting or another disjoint partitioning strategy. Avoid eager materialization and preserve lazy, re-iterable traversal.
- Keep the existing `lexOrder()` and its current spliterators stable during this work. The existing ordered lexicographic parallel tests remain regression coverage, not a mandate to add further `lexOrder()` parallel features.
- Exact counting and rank operations (`count`, `lexOrderMth`, `byRanks`, `choice`, `sample`) always use the counting plan, independent of the traversal strategy.

## 3. Constraints to preserve

- Tuples and order must not change for any accepted rule set; rejected rule sets (loop blocks over 1,000,000 combinations, too many pair cells) keep the same errors.
- Builders stay immutable and thread-safe. Lazy tables are built under a lock and published safely.
- Primitive `long` fast paths with the 62-bit guard, `BigInteger` fallback.
- Recursion limits: no special handling; let a stack overflow happen (documented decision).
- Loop blocks are not checked exactly beyond the existing combination limit; documented as "may hang".

## 4. Work packages

Status legend: `[ ]` not started, `[~]` delivered as a patch but not yet confirmed merged, `[x]` merged.

### WP0: Baseline (compact rules, lazy tables)

- [~] 0.1 Rules stored as a plain list; per-pair tables built once, lazily, and shared (`Rows`, `Relation.fromRules`)
- [~] 0.2 `PrunedLexItr` reads `Rows`, never builds the plan
- [~] 0.3 `Plan` (feasibility) and `Counts` (exact) as separate lazy tiers; `isEmpty()` uses `Plan` only
- Patch file: `conditional-compact-rules.patch` (against a4b57d2). The maintainer has local changes (enum with `PRUNED_BITSET`/`PRUNED_DFS`) that this patch does not contain, so it must be rebased once they push.

### WP1: Setup cost

- [~] 1.1 Run the loop check only when a rule adds a new linked pair (verified by the maintainer's full test run; branch change not yet merged)
- [x] 1.2 Replace repeated edge-removal reachability searches in `blockOf` with one iterative Tarjan bridge traversal; union only non-bridge edges into loop blocks (verified by maintainer: all 24 ConditionalProductBuilderTest tests passed)
- [x] 1.3 Replace full relation-map copies with a persistent AVL map; updates path-copy O(log E) nodes while preserving immutable builder snapshots and sorted iteration (verified by maintainer)
- Acceptance: lower builder-construction cost on CHAIN and CYCLE; identical tuples and loop-limit decisions on differential checks.

### WP2: Extract the shared model

- [x] 2.1 Extract the immutable `Relation` into a package-private file. There is no standalone `Rule` type in the current implementation: each rule is immediately compiled into a combined pairwise `Relation`, so do not introduce a redundant `Rule` object without a concrete need (verified by maintainer)
- [x] 2.2 Extract `ConstraintModel` to own immutable dimensions and pairwise relations; materialize prefix-pruned `Rows` lazily and share them across generators from the same builder (verified: all 25 `ConditionalProductBuilderTest` tests passed)
- [x] 2.3 Extracted the former nested `Plan` into package-private `ConstraintPlan`. `ConstraintCounts` owns the derived BigInteger/long count tables, prefix-completion queries, primitive-long fast path, and unranking operations; `ConstraintPlan` retains topology, feasibility checks, and candidate selection (verified: all 25 `ConditionalProductBuilderTest` tests passed).
- [x] 2.4 Reduced `ConditionalProductBuilder` to the immutable fluent/API facade. Persistent relation storage, graph validation, rule compilation, immutable model transitions, lazy plan/tuple access, and generator mechanics are delegated to package-private collaborators. The builder is 460 lines, including its public fluent DSL and API documentation; maintainer verified the generator extraction with the focused test.

### WP3: Generators

- [x] 3.1 Extracted `PrunedLexGenerator` (shared depth-first control flow) and `BitSetPrunedGenerator` (BitSet candidate filtering); maintainer confirmed the focused test passed.
- [x] 3.2 Implemented `DenseDfsGenerator` using lazily materialized, model-shared boolean relation rows with a 4,000,000-cell aggregate cap; larger representations fall back to `BitSetPrunedGenerator`. Maintainer confirmed the focused test passed.
- [x] 3.3 Extracted `DpLexGenerator`; maintainer confirmed the focused strategy/differential suite passed.
- [x] 3.4 Strategy dispatch reviewed: `PRUNED_BITSET`, `PRUNED_DFS`, and `DP` select their dedicated iterators; `ADAPTIVE` is resolved by the builder and remains available.
- [x] 3.5 Enum constants are already `PRUNED_BITSET`, `PRUNED_DFS`, `DP`, and `ADAPTIVE`; no rename required.

### WP4: Parallel

- [x] 4.1 Ordered, lazy spliterator for PRUNED_BITSET and PRUNED_DFS; splits disjoint contiguous first-dimension ranges, preserves encounter order, and stops below the minimum range threshold. Maintainer confirmed the focused regression suite passed (35 tests). Deeper-dimension splitting remains a follow-up if benchmarks show first-dimension partitioning is insufficient.
- [x] 4.2 Added `DpRankSpliterator`: partitions the exact BigInteger valid-tuple rank interval into disjoint contiguous ranges, lazily unranks each tuple, and preserves ordered encounter semantics. Maintainer confirmed the focused regression suite passed (35 tests).
- [x] 4.3 Added unordered parallel-stream tuple-set checks for PRUNED_BITSET, PRUNED_DFS, DP, and ADAPTIVE; maintainer confirmed the focused suite passed (36 tests). Ordered encounter-order equality remains covered for all explicit generators.

### WP4 follow-up: separate unordered traversal

- [x] 4.4 Public `anyOrder()` contract: full valid tuple set exactly once, unspecified encounter order, lazy and re-iterable; no random-selection semantics.
- [x] 4.5 Public `anyOrder()` API and sequential iterator baseline delivered; focused tuple-set, uniqueness, re-iteration and empty-product checks were confirmed by the maintainer.
- [x] 4.6 Lazy prefix-frontier splitting delivered. Partitions are disjoint candidate ranges below a fixed prefix; splitting can descend below singleton/small leading dimensions without eagerly materializing tuples. Parallel execution remains opt-in through `stream().parallel()`.
- [~] 4.7 Split coverage, uniqueness, deeper splitting and parallel tuple-set tests delivered. Keep the concurrent/repeated-iteration verification status tied to the maintainer's test run; do not claim unrun tests passed.
- [ ] 4.8 Benchmark complete `anyOrder()` consumption against sequential traversal and representative baselines before considering an automatic parallel threshold.
- [ ] 4.9 Do not change `lexOrder()` parallel policy based on any-order measurements; consider it only with separate evidence.

### WP5: Benchmarks

- [x] 5.1 Consolidated the conditional-product JMH benchmark around a single scenario-parameterized benchmark class. It compares all four library strategies with independent nested-stream and imperative backtracking baselines, and separates traversal-only from end-to-end timing where applicable.
- [x] 5.2 Added the standard topology/shape matrix plus `DEEP_DEAD_END` and `SMALL_DOMAIN` cases. Trial setup validates tuple counts and checksums against independent baselines.
- [x] 5.3 Added special-operation and scaling benchmarks for `count`, `isEmpty`, `lexOrderMth`, `byRanks`, `choice`, and `sample`; bounded cases use an enumeration baseline and very large cases are library-only with closed-form validation.
- [x] 5.4 Added quick/full command presets, JSON output, environment capture, and README-ready report generation.
- [~] 5.5 Benchmark analysis is recorded in the benchmark README and decision register. A full multi-fork run across the consolidated matrix remains the evidence needed before making global strategy-ranking or automatic-dispatch claims.

Benchmark rules: include builder construction and rule registration in end-to-end measurements; validate counts/checksums; retain the independent incrementally-pruned nested-stream baseline; report score error and allocation per operation; record source SHA, JMH/JDK/JVM, hardware and arguments. Treat short runs as screening evidence, not publication-quality rankings.

### WP5 follow-up: benchmark maintainability

- [x] Split scenario generation, independent baselines, benchmark support and command tooling into named packages.
- [x] Added `BenchmarkCommandGenerator` (interactive and preset command generation), `BenchmarkCommandExecutor` (interactive or pasted command execution), and `BenchmarkEnvironment` (machine/JDK/JVM/git metadata).
- [x] Added `scripts/BenchmarkReport.java` for per-run reports and baseline/local comparisons.
- [x] Added `scripts/benchmark-compare.sh` to compare the local working tree, including uncommitted changes, with a supplied SHA using a temporary worktree and identical benchmark sources. Supported commands:
  `scripts/benchmark-compare.sh <GIVEN_SHA>` and `scripts/benchmark-compare.sh <GIVEN_SHA> full`.

### WP6: Documentation and release finish

- [x] 6.1 Updated conditional-product builder Javadocs for dimension ordering, traversal strategies, and the current `ADAPTIVE` behavior.
- [x] 6.2 Updated the public README dependency examples to 4.1.0, expanded the Cartesian-product user guide for `anyOrder()` and strategy selection, and documented `isNotIn(...)` as an alias of `isNoneOf(...)`.
- [x] 6.3 Updated the benchmark README with the current runner/tool layout and local-versus-SHA comparison workflow; added the 4.1.0 changelog section.
- [ ] Final release verification remains separate: maintainer test suite, Javadoc/source-jar release build, and any final benchmark confirmation requested before publishing. No release tag or publication is part of this plan.

## 4B. Phase roadmap (A to M)

The work is on the `conditional-product-optimization` branch and is tracked by PR #6. "Delivered" below means present on that branch; it does not mean merged to `main`.

| Phase | Content | Status |
|---|---|---|
| A | Compact relation rows and lazy shared row materialization | delivered on PR branch |
| B | Feasibility tier, lazy counts and `DpLexSpliterator` | delivered on PR branch |
| C | `anyOrder()` splitting threshold and upper-bound `estimateSize()` | delivered on PR branch |
| D | Setup-cost work: shared value-to-position indexes and chained relation updates | delivered on PR branch |
| E | Generated topology/shape matrix and benchmark setup improvements | delivered; maintainer ran representative DEAD_END, CHAIN, MANY_VALID and COMPLETE cases |
| F | Work-triggered completion lookahead and corrected DEEP_DEAD_END shapes | F1/F2 delivered and maintainer-verified; `ADAPTIVE` is documented as a policy that currently maps to BitSet |
| G | Optional `anyOrder()` dimension reordering | on hold; current evidence does not justify it |
| H | Counting speed and test hardening | H1/H3/H4 delivered; special-operation JMH results recorded; small-product plan-construction cost deferred |
| I | User documentation, engineering documentation and 4.1.0 version metadata | delivered on PR branch; release verification pending |
| J | Conditional-rule exclusion vocabulary | decision complete: retain `isNotIn(...)` as an alias of `isNoneOf(...)`; do not add another synonym or remove a public method |
| K | Benchmark presets, report generation, environment capture and SHA comparison script | delivered; maintainer has used the quick/full comparison workflow |
| L | Special-operation benchmarks and imperative traversal baselines | delivered; maintainer ran the special preset and recorded screening results |
| M | Independent shape oracle, generator strategy checks, parallel and counting-tier concurrency coverage | delivered; final test verification remains with the maintainer |

## 5. Progress summary

| Area | Current status |
|---|---|
| Setup and shared model extraction | Delivered; focused tests were confirmed by the maintainer during implementation |
| Lexicographic generators and strategies | Delivered; all four strategies remain available |
| `anyOrder()` and parallel splitting | Delivered; automatic parallel policy is intentionally not enabled |
| Benchmark infrastructure | Delivered; quick/full presets, JSON, environment, reports and SHA comparison are available |
| Benchmark analysis | Representative results recorded; a full consolidated multi-fork matrix remains optional confirmation, not a blocker to the documentation patch |
| Public documentation and Javadocs | Updated for the 4.1.0 target |
| Release verification | Pending maintainer test and release-build execution |



## 6. Open questions and deferred work

- A full consolidated multi-fork benchmark can provide stronger evidence before changing strategy defaults or making broad performance claims.
- Do not add an automatic parallel threshold for `anyOrder()` until complete-consumption benchmarks demonstrate a benefit across representative shapes.
- Small-product conditional-operation overhead is primarily plan-construction cost; it is deferred rather than addressed with a speculative shortcut.
- The compiled-product API is parked after the end-to-end prototype failed to show a performance advantage (see §1B).
- No unresolved public API naming decision remains for `isNotIn`: retain it as an alias of `isNoneOf`.

## 7. Verification method

- Differential check: build the same rule sets with the old reference commit and with the new code and compare a digest of the full output (count, order, hash). Use the fixed cases plus thousands of seeded random rule sets (random dimensions, sizes, rules, including loops and dead ends).
- Brute-force oracle: for small random rule sets, enumerate the whole product and filter; compare with every generator, count, rank, and `lexOrderMth`.
- Parallel: compare `stream().parallel()` collected in order with the sequential list.
- Unit tests live next to the existing ones in `ConditionalProductBuilderTest`, `ConditionalProductRuleOrderTest`, and a new differential test per generator.

## 8. Progress log (newest first)

- 2026-11-20: Release-documentation pass for the 4.1.0 target. Updated Maven and README version references, the public Cartesian-product guide (`anyOrder()`, lexicographic strategies, rank-order semantics and the `isNotIn` alias), conditional-product Javadocs, benchmark tooling instructions, and the 4.1.0 changelog. The PR remains open; no merge or release publication is authorized.

- 2026-11-20: Maintainer clarified that the naming question is specifically `isNotIn(...)` versus `isNoneOf(...)`. Keep both overload families; `isNotIn(...)` is an alias of `isNoneOf(...)`. Do not remove a public method, add `isNotOneOf(...)`, or make a major-version decision based on this alias.


- 2026-11-19: Recorded a future architectural direction for a reusable compiled-product abstraction. The Simple, Constrained, and Conditional product builders should eventually support `.compile()`; a compiled definition would be bound to different datasets for repeated executions, following the compile-once/apply-many model of Java regular expressions. Added an illustrative conditional-product API and separated compilation, binding, and execution responsibilities. This is a design hint for future AI-assisted work, not a finalized API or implementation mandate.

- 2026-11-19 (L results): Maintainer ran the `special` preset (JMH 1.37, OpenJDK 26.0.1, one fork, 3 x 100 ms warmup, 5 x 100 ms measurement; environment file not supplied). Library beat enumeration by about 13x to 78x on count, lexOrderMth, byRanks, choice and sample for CHAIN, MANY_VALID, SMALL_DOMAIN and DEEP_DEAD_END; handled 1e39 tuples in 0.2 to 1.9 ms (sample 0.5 to 11.5 ms). Enumeration won `isEmpty` everywhere (biased: all bounded workloads are non-empty, so enumeration stops at the first tuple; an empty-product workload is needed) and won every rank and sampling operation on DEAD_END-5x25 (library 0.54x to 0.65x). Allocation is the main cost: `count` allocates 66 KB to 1.1 MB per call on bounded workloads and 1.5 MB to 13 MB on LARGE; `sample` allocates 24 MB (20x50) and 57 MB (20x100). Findings feed item 8 (counting speed): table construction allocation, `sample`, and fixed setup cost on small products. Summary table added to the benchmarks README (screening quality).

- 2026-11-19 (L, M): Added the special-operations benchmarks (`ConditionalProductSpecialOperationsBenchmark`, `ConditionalProductScalingBenchmark`, `SpecialOperationsCase`, new `LARGE` scenario family), the two imperative backtracking baselines (`IMPERATIVE_ALLOCATING`, `IMPERATIVE_REUSE`, available as `implementation` values and in the full-matrix preset), a `special-ops` generator preset, and report support for them. Added `ConditionalProductShapeOracleTest` (seven structured families, about 6 s). The first version of the duplicate-values test chose rule values from a shared pool; the library correctly rejected values that are not in the named dimension, so the test now chooses from each dimension's own values. Sandbox timings (2 cores, no JMH, indicative only; builder construction included): the library beat enumeration by roughly 5x to 130x on `count`, `lexOrderMth`, `byRanks`, `choice` and `sample` for CHAIN, MANY_VALID, SMALL_DOMAIN and DEEP_DEAD_END (for example DEEP_DEAD_END-5x25 `count` about 0.33 ms against 41 ms), and handled `LARGE-20x100` (about 2e39 tuples) in about 3 ms for `count` and about 15 ms for `sample`. Enumeration was faster in two places: `isEmpty`, because it stops at the first tuple (0.2 to 0.6 us against 9 to 22 us, the library paying for plan construction), and the small `DEAD_END-5x25` shape (rank operations about 190 us against 108 us). Both point at plan and counting setup cost for small products, which is item 8 (counting speed). `sample` on `LARGE` is the slowest operation (about 6 ms at 20x50, 15 ms at 20x100) and the first thing to profile. These need a maintainer JMH run before anything is concluded.

- 2026-11-18 (docs, tooling): Corrected the documentation drift: `PrunedLexGenerator` and builder Javadoc and the decision log now state the exact rule (lookahead on when descents exceed 1,024 + 4 x emitted tuples, independent of topology, stays on for the traversal, never changes output or order); `LexOrderStrategy.ADAPTIVE` documents that no algorithm choice is guaranteed. Phase table updated (D merged, F1/F2 merged, G on hold, K/L/M added). The benchmarks README DEEP_DEAD_END entries are superseded: the earlier "DP wins five of five" and the nested-stream allocation figures came from the pre-F2 shapes, four of which were empty products. Screening run on corrected shapes (maintainer, smoke settings): `PRUNED_BITSET` is 1.46x, 1.44x, 1.01x, 1.01x, 0.72x the DP time on DEEP_DEAD_END 5x10, 5x25, 7x10, 7x25, 10x10, and 0.14x to 0.76x on DEAD_END and SMALL_DOMAIN. The maintainer's pivot-cardinality patch in `BitSetPrunedGenerator.nextValidCandidate` showed no regression (geomean 0.944x on `PRUNED_BITSET`, SMALL_DOMAIN-14x3 43% faster, dead-end allocation about 30% lower). Exact-count `anyOrder()` splitting was prototyped and rejected: it made small shapes about 5x slower in parallel (NARROW_ROOT 8.8 to 44.7 us end to end) because counting setup costs more than the split saves; nothing was delivered. `PrunedLookaheadTest` extended with a mid-iteration switch (output and order unchanged) and `anyOrder()` with lookahead. Tooling delivered in the same patch: `quick` (about 1-2 minutes) and `full` presets in `BenchmarkCommandGenerator`, `-rf json -rff` plus a console log and an environment record under git-ignored `benchmark-results/`, `scripts/BenchmarkReport.java` (report and compare modes) and `scripts/benchmark-compare.sh <SHA>` (temporary worktree, same benchmark source on both sides, verdicts within 10% / inconclusive / faster / slower; error paths tested with a stub, JMH not run). Next: special-operations benchmark and imperative baselines (L), then larger-shape oracles (M).

- 2026-11-18 (F1): Replaced the loop-topology completion lookahead with a single waste-triggered one in `PrunedLexGenerator`. Lookahead starts off for every graph; once descents exceed 1,024 + 4 x emitted tuples it builds the feasibility plan and checks every prefix, and an empty plan ends the traversal at once. Reasons: the topology rule missed acyclic dead ends (a rule between distant dimensions whose values conflict, found only after the free dimensions between them are enumerated; pruned was about 2x slower than DP there) and it built the plan for every loop graph even when nothing was dead (+1.3 KB, about 6.5% allocation on the 66-tuple practical benchmark, 19,632 -> 20,992 B/op). Removed `ConstraintModel.completionLookaheadUseful`, `ConstraintGraph.hasLoopBlock`, `ConstraintPlan.needsCompletionLookahead` and its test. Sandbox (not JMH): practical shape 19,616 B/op; acyclic dead end 7x10 35.8 -> 16.4 ms (DP 16.0); healthy shapes unchanged. Tests: `PrunedLookaheadTest` (brute-force oracle on acyclic and looped dead-end shapes, ordered and unordered parallel, empty products, healthy chains never switch on). The benchmark fairness question (stream-side `compile` timed, library rule conversion not) was measured: compile costs about 0.4 us, so the benchmark code is kept. Awaiting maintainer `mvn test` and JMH rerun of DEEP_DEAD_END, SMALL_DOMAIN, CHAIN, COMPLETE and the practical benchmark.
- 2026-11-17 (E follow-up): Reviewed the maintainer's four JMH outputs. PRUNED_BITSET is fastest or tied nearly everywhere and 3-11x faster than the nested stream with 10-20x less allocation; DP and PRUNED_DFS never beat it materially on CHAIN, MANY_VALID and COMPLETE; ADAPTIVE (cutoff 10,000) is slower than BITSET in the cells where it picks DP. The existing DEAD_END (CONTROLLED_DEAD_END) topology does not stress dead ends: it ignores the 30,000 budget (about 5 million tuples at 7x50) and its dead prefixes are pruned at depth 2, and it ran with the 100 ms smoke defaults. Added DEEP_DEAD_END (late conflict at the last dimension) and SMALL_DOMAIN (8-14 dimensions, 3-5 values) topologies. A sandbox smoke run (not JMH) showed DP 10-30x faster than both pruned strategies on DEEP_DEAD_END, so Phase F must not simply point ADAPTIVE at BITSET; the pruned generators likely need completion lookahead, or ADAPTIVE must detect dead-end-prone shapes. Decide after the maintainer's JMH run.
- 2026-11-17: Phase E benchmark expansion delivered to the branch. Added 36 generated cases across 3/5/7/10 dimensions and sizes 10/25/50 for DEAD_END, MANY_VALID (up to 75,000 tuples), and COMPLETE (up to 1,000 tuples); retained all 17 historical resource cases. Rule BitSet-to-list conversion is precomputed in trial setup so end-to-end timings include builder/rule registration but not repeated benchmark-input conversion. The maintainer must compile, smoke-check representative profiles, and run the JMH matrix before results are accepted.

- 2026-11-17: Phase D delivered, awaiting maintainer verification. Measured in a sandbox timing harness (not JMH; 3 dimensions, one rule per value with about 50% of the targets allowed, rule registration only, three runs after a warm-up): size 1000 took 150 ms and 788 MB before Phase A, 66 ms and 104 MB at head 222f5b6; size 5000 took 10 to 15 s and 70 GB before Phase A, 1.5 s and 2.4 GB at head. The remaining cost was `RuleCompiler.matching`, which built two hash sets per rule (about 240 KB each at size 5000), and `Relation.restrict`, which copied the rule array per rule (quadratic). Added `DimensionIndexes` (lazy value to positions map per dimension, shared by derived builders through `ConstraintModel`) so compiling a rule costs time in proportion to the values it names; `Relation` now links rules in a shared chain and builds an array only when its rows are built. After the change: size 1000 about 9 ms and 2.5 MB, size 5000 about 165 to 250 ms and 33 MB. The first traversal builds the rows lazily: about 4 ms at 1000 and 140 to 210 ms at 5000 (the rows hold 25 million cells, so this is close to the size of the data); `isEmpty()` takes 1 to 4 ms. Remaining cost is inherent to the API: the caller passes lists of about half the dimension per rule. New `RuleCompilerTest`: repeated values in a dimension, unknown and repeated values, rules after `and()` use the extended index, concurrent derivation from one base. 49 conditional-product tests passed in the sandbox stub runner (`RelationTest` excluded there).

- 2026-11-17: Phase C delivered, awaiting maintainer verification. `AnyOrderGenerator.AnyOrderSpliterator` now splits a partition only while its upper work bound (candidates below the prefix: remaining range size times the sizes of the deeper dimensions, saturating) exceeds `SPLIT_WORK_THRESHOLD` (1024); smaller partitions run sequentially, so a parallel stream no longer creates a task per tuple. `estimateSize()` reports that bound instead of `Long.MAX_VALUE`, so the JDK sizes its tasks from it. Two existing anyOrder tests were given larger shapes so they still reach the splitting path. New `AnyOrderSplittingTest`: full splitting stays near the threshold and covers every tuple once, a 125-candidate product is not split, and the estimate is an upper bound (0 for an empty dimension). 45 conditional-product tests passed in the sandbox stub runner (`RelationTest` excluded there). The threshold is a first value and must be tuned with `AnyOrderParallelBenchmark`.

- 2026-11-17: Phase B delivered (feasibility tier and lazy counts), awaiting maintainer verification. `ConstraintPlan` now builds `canComplete` (a yes/no table per node) with the topology; `isEmpty()`, `hasCompletion` and `firstValid` use it and keep no counts. The exact counting tier is built on first use through `ConstraintPlan.counts()` (`count`, ranks, `choice`, `sample`, `lexOrderMth`). The `DP` stream no longer unranks every tuple: `DpLexSpliterator` walks sequentially with `DpLexGenerator` (no counts built) and switches to rank-range splitting only when a parallel pipeline splits it; `DpRankSpliterator` unranks once per range and then steps incrementally. New `ConstraintPlanTierTest`: counts not built by iteration or `isEmpty()`, dead end detection, 400 seeded random rule sets against a brute-force oracle on every DP path (iterator, sequential stream, parallel ordered stream, ranks), and rank-split order. Open follow-ups from review: `anyOrder()` spliterator needs a work threshold (it reports `Long.MAX_VALUE` and splits to single candidates); `Relation` shares rows only for up to 62 rules per pair and copies its rule array per added rule, so measure one-rule-per-value shapes at large sizes.

- 2026-11-17: Started the separate `anyOrder()` API. Added its public contract to `ConditionalProductBuilder`: it returns every valid tuple once, is lazy/re-iterable, makes no encounter-order guarantee, and is not random selection (`choice`/`sample` retain that role). Added `AnyOrderGenerator` as an initial sequential implementation using the existing prefix-pruned BitSet iterator; its spliterator omits `ORDERED` and refuses splitting so this stage introduces no accidental parallel execution. Added a focused test for tuple-set equality, duplicate-free output, re-iteration, empty output, and the unordered/non-splitting spliterator contract. Awaiting maintainer compilation and test execution. Automatic parallel selection and splitting remain separate follow-up work; `lexOrder()` is unchanged.

- 2026-11-17: Delivered the first documentation slice ahead of the `anyOrder()` implementation. Added practical dimension-ordering guidance to `ConditionalProductBuilder` Javadocs, including constraint-evaluation dependencies, selective-dimension pruning, tuple-position/lexicographic-order consequences, and the SQL join-order analogy. Updated the target design to keep existing `lexOrder()` behavior out of scope and recorded the staged, separate `anyOrder()` work package: contract, sequential traversal, automatic parallel traversal, correctness tests, then benchmark-based threshold selection. No traversal code or behavior changed.

- 2026-11-17: Added explicit benchmark follow-ups to WP5: remove unintended intermediate console output while retaining JMH progress/results and useful errors; analyze the complete JSON matrix for latency, allocation, strategy crossover, and ADAPTIVE quality; use targeted reruns only for unresolved comparisons. Added a deferred benchmark-maintainability task to split the currently consolidated Java benchmark class into cohesive classes/packages after results analysis. The maintainer observed and is currently clearing intermediate console output; exact sources still need to be traced and addressed.
- 2026-11-17: Extended case descriptors to support heterogeneous dimension sizes through a comma-separated `sizes` property; `size` remains the uniform-size shorthand. Documented that new case paths must also be added to the JMH `@Param` list. The physical case hierarchy represents workload axes; strategy and measurement are selected through JMH's `implementation` parameter and benchmark method to avoid duplicating every workload directory for every strategy.
- 2026-11-17: Reorganized conditional-product benchmark cases into classpath resources under `src/test/resources/benchmarks/conditionalproduct/cases`. The physical case hierarchy is topology → numeric dimension/size range → selectivity; each resource path is directly usable as the JMH `scenarioName` parameter. Added all six agreed non-overlapping range groups across dimension bands 2–5, 6–10, 11–20 and size bands 10–100, 101–250, plus isolated edge cases. Added a 1%-minimum-one TIGHT profile for bounded large cases and documented exact single-case/strategy/measurement commands. Compilation and benchmark execution are pending maintainer verification.
- 2026-11-17: Removed the two legacy conditional-product JMH benchmark classes and their committed output snapshots. Delivered `ConditionalProductStrategyBenchmark` with bounded full-traversal scenarios, all four library strategies, a prefix-pruned nested-stream baseline, tuple/order verification, and separate traversal-only/end-to-end methods. Added the benchmark README entry and JSON output command. Timed methods contain no console output; JMH's normal progress/ETA display stays enabled and detailed results are written to JSON. WP5.1 and WP5.2 are delivered but await maintainer compilation and benchmark runs.
- 2026-11-17: Maintainer confirmed the focused suite passed after WP4.3: 36 tests. WP4 is complete. The new unordered parallel test passed for all four strategies, comparing tuple sets with sequential traversal; ordered encounter-order checks remain in place for the three explicit generators.
- 2026-11-17: Delivered WP4.3 test coverage. Added an unordered parallel-stream regression test that compares the tuple set against sequential default traversal for PRUNED_BITSET, PRUNED_DFS, DP, and ADAPTIVE. The existing ordered-parallel test continues to compare exact encounter order for the three explicit generators. Awaiting the maintainer's focused test run.
- 2026-11-17: Maintainer confirmed the focused suite passed after WP4.2: 35 tests. WP4.2 is verified. The test now exercises ordered parallel traversal for DP as well as both pruned generators. The maintainer also requested that three finishing deliverables remain explicit TODOs at the very end of the plan: Javadoc update, README/benchmarks README/changelog update, and benchmark update. Benchmark class design will be supplied by the maintainer; do not begin that work before receiving it.
- 2026-11-17: Started WP4.2. Added `DpRankSpliterator`, which partitions the exact valid-tuple rank interval `[0, counts.total)` using BigInteger midpoint splits. Each partition lazily unranks its next rank and materializes the tuple; lower ranges are returned while the current spliterator retains the upper range, preserving ordered parallel-stream semantics without materializing tuples. Wired DP lex-order streams to this spliterator and expanded the parallel-order/set regression test to include DP. Awaiting the focused test run.
- 2026-11-17: WP4.1 verified by maintainer. `mvn -Dtest=ConditionalProductBuilderTest,ConditionalProductGeneratorTest,ConditionalProductRuleOrderTest test` passed: 35 tests. Ordered parallel traversal test passed for PRUNED_BITSET and PRUNED_DFS. No DP parallel splitting was included in this step.
- 2026-11-17: Started WP4.1. Added PrunedLexSpliterator for PRUNED_BITSET and PRUNED_DFS. It lazily creates independent bounded iterators over disjoint contiguous first-dimension ranges; trySplit returns the lower range and retains the upper range, preserving ordered encounter semantics. Splitting stops once a range contains at most two first-dimension indices or traversal has begun. DP remains on its existing iterator spliterator path for WP4.2. Added a parallel-stream regression test that compares both order and tuple set with sequential traversal. Awaiting maintainer execution of the focused test classes.
- 2026-11-16: Plan written. WP0 patch (`conditional-compact-rules.patch`) delivered against a4b57d2; benchmark with the plain Java stream row delivered. Maintainer's local tree has `LexOrderStrategy {PRUNED_BITSET, PRUNED_DFS, DP, ADAPTIVE}`; waiting for the push before rebasing WP0 and starting WP1.
- 2026-11-17: Maintainer confirmed the WP1.3 persistent relation-map regression test passed. WP1.3 is now verified. Started WP2.1 by extracting the immutable pairwise `Relation` into package-private `Relation.java`. The current implementation does not retain individual `Rule` objects: each fluent rule is immediately intersected into its pairwise relation. Do not add a redundant rule representation just to match the original target-design wording. Builder behavior is unchanged; awaiting the maintainer's focused test run.
- 2026-11-17: Maintainer confirmed the WP2.1 `Relation` extraction passed `ConditionalProductBuilderTest`. WP2.1 is verified. Delivered WP2.2: introduced package-private `ConstraintModel` to own the immutable dimension snapshots and pairwise relation map; its prefix-pruned `Rows` are built lazily with safe publication and reused by generators from the same builder. `PrunedLexItr` now consumes those shared rows instead of rebuilding its adjacency arrays for every iterator. Awaiting focused test run.
- 2026-11-17: Maintainer confirmed all 25 `ConditionalProductBuilderTest` tests passed after WP2.2. WP2.2 is verified. Started WP2.3 by extracting the nested exact completion plan into package-private `ConstraintPlan`; this is intentionally a first mechanical step. Exact-count tables and operations remain in `ConstraintPlan` for now and must be moved into `ConstraintCounts` in the next stage before WP2.3 can be considered complete. Awaiting focused test run for the extraction.
- 2026-11-17: Maintainer confirmed all 25 `ConditionalProductBuilderTest` tests passed after the `ConstraintCounts` table extraction. The derived BigInteger/primitive-long tables and their construction are now verified in `ConstraintCounts`. WP2.3 remains partial: prefix-completion and unranking query methods still live in `ConstraintPlan`; the next slice will move those operations into `ConstraintCounts`, leaving topology and feasibility traversal in `ConstraintPlan`.
- 2026-11-17: Continued WP2.3: moved prefix-range lookup, subtree/root completion counting, primitive-long completion counting, and rank unranking from `ConstraintPlan` into `ConstraintCounts`. `ConstraintPlan` now retains the graph/tree topology and feasibility operations (`firstValid`, `isValid`, and the earlier-dimension relation checks); it delegates prefix feasibility lookahead to `ConstraintCounts`. The builder's adaptive-density and unranking call sites now use the count holder directly. Awaiting `ConditionalProductBuilderTest` verification.
- 2026-11-17: Maintainer confirmed all 25 `ConditionalProductBuilderTest` tests passed after the final WP2.3 query-method extraction and the subsequent `ConstraintCounts` reference fixes (`values`/`dimensions` now resolve through `plan`). WP2.3 is verified. The shared model extraction is complete through WP2.3; next is WP2.4, reducing `ConditionalProductBuilder` to the immutable public facade before starting generator extraction.
- 2026-11-17: Started WP2.4 by extracting the immutable AVL relation map and its tree nodes from `ConditionalProductBuilder` into package-private `PersistentRelationMap.java`. The builder retains only the map reference and update calls; balancing, path-copy insertion, lookup, and ordered iteration now live outside the facade. This is the first WP2.4 slice, not completion: graph validation, rule compilation helpers, and generator implementations still need to move out. Awaiting the maintainer's focused test run.
- 2026-11-17: Continued WP2.4: extracted constraint-graph validation into package-private `ConstraintGraph`. It owns the unchanged pair-cell and loop-block limits, iterative Tarjan bridge detection, non-bridge union-find grouping, and canonical dimension-pair key encoding. Updated `ConditionalProductBuilder` and `ConstraintPlan` to delegate to it. Awaiting the maintainer's focused test run.
- 2026-11-17: Continued WP2.4: extracted caller-list identity resolution and value-to-index predicate compilation into `RuleCompiler`. The fluent builder delegates `when(...)`, `then(...)`, and trigger/target matching while preserving validation messages and BitSet semantics. The maintainer confirmed the focused `ConditionalProductBuilderTest` passed.
- 2026-11-17: Continued WP2.4: moved immutable state transitions (`withDimension` and `withRule`) into `ConstraintModel`. The model now owns dimension snapshots, the persistent relation map, pair-cell validation, relation restriction, and new-edge loop-block validation. `ConditionalProductBuilder` no longer keeps duplicate `dims`/`relations` aliases; `and(...)` and completed rule chains delegate to the model and create a new facade. The maintainer confirmed the focused test passed.
- 2026-11-17: Continued WP2.4: moved lazy `ConstraintPlan` construction/publication and tuple materialization into `ConstraintModel`. Each immutable model snapshot now owns its own safely published lazy plan, while generators share the same model-level tuple conversion. The builder no longer owns a duplicate plan field, plan factory, or tuple-conversion helper. The maintainer confirmed the focused test passed.
- 2026-11-17: Continued WP2.4 / prepared WP3: extracted lexicographic, rejection-choice, and rank-based generator implementations into package-private `ConditionalProductGenerators`. `ConditionalProductBuilder` now delegates generator creation and is reduced to 460 lines. This is a mechanical extraction: strategy dispatch and iterator algorithms are intentionally unchanged; in particular, the dedicated `PRUNED_DFS` implementation remains a planned WP3 task, not silently removed or substituted in this change. Awaiting focused test verification.
- 2026-11-17: Maintainer confirmed `mvn -Dtest=ConditionalProductBuilderTest test` passed after the `ConditionalProductGenerators` extraction. The generator extraction slice is verified. WP2.4 remains in progress pending a facade review and any remaining cohesive extractions; WP3 strategy-specific implementations have not started.
- 2026-11-17: Maintainer confirmed the focused ConditionalProductBuilderTest passed after extracting ConditionalProductGenerators. Reviewed the remaining 460-line builder: the remaining code is the public fluent rule DSL, public builder operations, immutable facade state, and API documentation; no further extraction is justified without changing public API structure. WP2.4 is complete.
- 2026-11-17: Started WP3.1. Extracted the shared prefix-pruned depth-first iterator control flow into PrunedLexGenerator and its BitSet candidate-selection implementation into BitSetPrunedGenerator. ConditionalProductGenerators now dispatches PRUNED_BITSET to the extracted implementation. PRUNED_DFS is deliberately still pending WP3.2; for this intermediate commit it retains the previous non-BitSet fallback behavior. Awaiting ConditionalProductBuilderTest verification.
- 2026-11-17: Maintainer confirmed the focused ConditionalProductBuilderTest passed after WP3.1. WP3.1 is verified. Delivered WP3.2: PRUNED_DFS now uses DenseDfsGenerator with boolean relation rows cached lazily on ConstraintModel and shared by iterators. Dense materialization is capped at 4,000,000 aggregate relation cells; above the cap the strategy falls back to the BitSet implementation to bound memory. Added a small-case strategy-equivalence assertion and a 4.2-million-cell case that exercises the fallback without traversing the full result. Awaiting focused test verification.
- 2026-11-17: Maintainer confirmed `mvn -Dtest=ConditionalProductBuilderTest test` passed after WP3.2. WP3.2 is verified. Maintainer raised whether repeatedly running the same focused test class is sufficient after many structural commits. Agreed to strengthen verification deliberately: add dedicated generator-level tests and seeded brute-force differential coverage across all four strategies, including empty/dead-end cases, repeated iteration, and lazy-plan behavior where observable. Keep the focused builder suite as regression coverage; do not inflate test count with redundant examples. Decide the exact test split before/alongside WP3.3 rather than delaying it until the end.
- 2026-11-17: In response to the maintainer's request for stronger test coverage beyond repeatedly running the existing builder suite, added `ConditionalProductGeneratorTest`. It independently brute-forces small Cartesian products and compares ordered output for PRUNED_BITSET, PRUNED_DFS, DP, and ADAPTIVE across 300 fixed-seed random rule sets. It also covers unconstrained products, empty dimensions, a fully dead-ended product, repeated iteration, idempotent `hasNext`, exhaustion, and `next` after exhaustion. Existing `ConditionalProductBuilderTest` remains the public API and broader builder regression suite. Awaiting focused execution of both classes.
- 2026-11-17: Maintainer reported three failures in `ConditionalProductRuleOrderTest`, all caused by its reflection helper looking for the former `relations` field directly on `ConditionalProductBuilder`. Updated the test helper to follow the extracted ownership path (`builder.model.relations`); no production code changed. Maintainer confirmed 34 tests passed after the correction. The dedicated generator-level strategy/differential tests and the rule-order regression suite are now verified. WP3.3 remains the next implementation step; do not start it until the maintainer says to proceed.
- 2026-11-17: Delivered WP3.3: extracted the DP lexicographic iterator from the private nested `LexItr` in `ConditionalProductGenerators` into package-private `DpLexGenerator`. The iterator retains the same exact-count lookahead through `ConstraintPlan.firstValid`, lexicographic candidate order, lazy `hasNext` advancement, tuple materialization through `ConstraintModel`, and exhaustion behavior. Updated strategy dispatch to instantiate the extracted class. No counting, rank, or sampling code changed. Awaiting maintainer verification with the focused builder, generator differential, and rule-order suites.
- 2026-11-17: Maintainer confirmed all 34 tests passed after WP3.3. Reviewed the final strategy wiring for WP3.4: `PRUNED_BITSET`, `PRUNED_DFS`, and `DP` dispatch to their dedicated iterator implementations in `ConditionalProductGenerators`; `ADAPTIVE` is resolved by `ConditionalProductBuilder` using the existing 10,000-tuple threshold, and remains available. Verified WP3.5 requires no enum rename: all four required constants are already present. WP3 is complete. No strategy was removed or deprecated.

## 9. Maintainer clarifications (2026-11-17)

- 2026-11-20: `isNotIn(...)` and `isNoneOf(...)` are the compared methods. Retain both varargs and collection overloads; document `isNotIn` as an alias. Target version for this work is 4.1.0. Keep the replacement PR open and do not merge.


- Full-result `lexOrder()` and any-order full generation are optimized for consuming the complete result set. Do not choose an implementation solely because it wins a short-prefix benchmark.
- Special operations (`count`, `isEmpty`, `lexOrderMth`, `byRanks`, `choice`, `sample`, etc.) may pay the exact-plan/counting cost. Their end-to-end benchmarks include builder construction and rule registration.
- The complete lexicographic enum strategy set is `PRUNED_BITSET`, `PRUNED_DFS`, `DP`, and `ADAPTIVE`. All four strategies must remain implemented and available. If one is inefficient, optimize it; do not remove or deprecate it during implementation. Benchmark only after the complete flow is ready, then tune implementations/defaults/cutoffs without dropping a strategy.
- 2026-11-17: Created development branch `conditional-product-optimization` from commit `27c2066`. Corrected `ConditionalProductBuilder` references from the obsolete `PRUNED` enum constant to `PRUNED_BITSET`; the plan was expanded with the full-generation versus special-operation objectives and the full strategy table. Compile/tests have not yet been run.
- 2026-11-17: Maintainer explicitly confirmed that no strategy is to be removed. Updated the retention rule, WP3/WP6 expectations, and open questions accordingly: all four strategies remain implemented; benchmark evidence is for optimization/default/cutoff tuning after the full flow is complete, not for dropping algorithms.
- 2026-11-17: WP1.1 delivered and verified by the maintainer's full test run. `withRule` runs `checkLoopBlocks` only when the rule introduces a new dimension-pair edge; adding another restriction to an existing pair cannot change graph connectivity or loop blocks. A builder-style regression test checks exact count and lexicographic output after adding a further restriction to an edge in a three-dimension loop.
- 2026-11-17: WP1.2 delivered. The previous `blockOf` implementation removed each edge and reran graph reachability, doing repeated work for every new linked pair. Replaced it with iterative Tarjan bridge detection in one DFS traversal (avoiding graph-depth dependence on the Java call stack), then unioned only non-bridge edges to form loop blocks. Added a builder-style regression test proving that a 1,001-value dimension attached to a 1,000,000-combination cycle by a bridge is excluded from that cycle's loop-block product. The maintainer should run the test suite before this item is marked verified.
- 2026-11-17: Maintainer confirmed all 24 `ConditionalProductBuilderTest` tests passed, including the iterative Tarjan bridge regression. WP1.2 is now verified.
- 2026-11-17: WP1.3 delivered. Replaced the full `LinkedHashMap` copy in `withRule` with an immutable AVL relation map. Updating an existing dimension pair now path-copies O(log E) nodes instead of copying all E entries; iteration remains sorted by pair key, and old builder instances retain their original map root. Added a builder-style regression covering repeated same-pair updates, independent branches, exact counts, and lexicographic output. Awaiting maintainer test run.
- 2026-11-17: Continued WP2.3: extracted exact subtree/root count-table construction and BigInteger/primitive-long table storage into package-private `ConstraintCounts`. `ConstraintPlan` retains graph topology, loop-block assignments, prefix feasibility, and currently still hosts the prefix-completion/rank query methods, which are the next extraction slice. Updated builder count/rank consumers to read the count holder. Awaiting focused test run.
- 2026-11-17: Maintainer confirmed all 25 `ConditionalProductBuilderTest` tests passed after the final WP2.3 query-method extraction and the subsequent `ConstraintCounts` reference fixes (`values`/`dimensions` now resolve through `plan`). WP2.3 is verified. The shared model extraction is complete through WP2.3; next is WP2.4, reducing `ConditionalProductBuilder` to the immutable public facade before starting generator extraction.
- 2026-11-17: Started WP2.4 by extracting the immutable AVL relation map and its tree nodes from `ConditionalProductBuilder` into package-private `PersistentRelationMap.java`. The builder retains only the map reference and update calls; balancing, path-copy insertion, lookup, and ordered iteration now live outside the facade. This is the first WP2.4 slice, not completion: graph validation, rule compilation helpers, and generator implementations still need to move out. Awaiting the maintainer's focused test run.
- 2026-11-17: Continued WP2.4: extracted constraint-graph validation into package-private `ConstraintGraph`. It owns the unchanged pair-cell and loop-block limits, iterative Tarjan bridge detection, non-bridge union-find grouping, and canonical dimension-pair key encoding. Updated `ConditionalProductBuilder` and `ConstraintPlan` to delegate to it. Awaiting the maintainer's focused test run.
- 2026-11-17: Continued WP2.4: extracted caller-list identity resolution and value-to-index predicate compilation into `RuleCompiler`. The fluent builder delegates `when(...)`, `then(...)`, and trigger/target matching while preserving validation messages and BitSet semantics. The maintainer confirmed the focused `ConditionalProductBuilderTest` passed.
- 2026-11-17: Continued WP2.4: moved immutable state transitions (`withDimension` and `withRule`) into `ConstraintModel`. The model now owns dimension snapshots, the persistent relation map, pair-cell validation, relation restriction, and new-edge loop-block validation. `ConditionalProductBuilder` no longer keeps duplicate `dims`/`relations` aliases; `and(...)` and completed rule chains delegate to the model and create a new facade. The maintainer confirmed the focused test passed.
- 2026-11-17: Continued WP2.4: moved lazy `ConstraintPlan` construction/publication and tuple materialization into `ConstraintModel`. Each immutable model snapshot now owns its own safely published lazy plan, while generators share the same model-level tuple conversion. The builder no longer owns a duplicate plan field, plan factory, or tuple-conversion helper. The maintainer confirmed the focused test passed.
- 2026-11-17: Continued WP2.4 / prepared WP3: extracted lexicographic, rejection-choice, and rank-based generator implementations into package-private `ConditionalProductGenerators`. `ConditionalProductBuilder` now delegates generator creation and is reduced to 460 lines. This is a mechanical extraction: strategy dispatch and iterator algorithms are intentionally unchanged; in particular, the dedicated `PRUNED_DFS` implementation remains a planned WP3 task, not silently removed or substituted in this change. Awaiting focused test verification.
- 2026-11-17: Maintainer confirmed `mvn -Dtest=ConditionalProductBuilderTest test` passed after the `ConditionalProductGenerators` extraction. The generator extraction slice is verified. WP2.4 remains in progress pending a facade review and any remaining cohesive extractions; WP3 strategy-specific implementations have not started.
- 2026-11-17: Maintainer confirmed the focused ConditionalProductBuilderTest passed after extracting ConditionalProductGenerators. Reviewed the remaining 460-line builder: the remaining code is the public fluent rule DSL, public builder operations, immutable facade state, and API documentation; no further extraction is justified without changing public API structure. WP2.4 is complete.
- 2026-11-17: Started WP3.1. Extracted the shared prefix-pruned depth-first iterator control flow into PrunedLexGenerator and its BitSet candidate-selection implementation into BitSetPrunedGenerator. ConditionalProductGenerators now dispatches PRUNED_BITSET to the extracted implementation. PRUNED_DFS is deliberately still pending WP3.2; for this intermediate commit it retains the previous non-BitSet fallback behavior. Awaiting ConditionalProductBuilderTest verification.
- 2026-11-17: Maintainer confirmed the focused ConditionalProductBuilderTest passed after WP3.1. WP3.1 is verified. Delivered WP3.2: PRUNED_DFS now uses DenseDfsGenerator with boolean relation rows cached lazily on ConstraintModel and shared by iterators. Dense materialization is capped at 4,000,000 aggregate relation cells; above the cap the strategy falls back to the BitSet implementation to bound memory. Added a small-case strategy-equivalence assertion and a 4.2-million-cell case that exercises the fallback without traversing the full result. Awaiting focused test verification.
- 2026-11-17: Maintainer confirmed `mvn -Dtest=ConditionalProductBuilderTest test` passed after WP3.2. WP3.2 is verified. Maintainer raised whether repeatedly running the same focused test class is sufficient after many structural commits. Agreed to strengthen verification deliberately: add dedicated generator-level tests and seeded brute-force differential coverage across all four strategies, including empty/dead-end cases, repeated iteration, and lazy-plan behavior where observable. Keep the focused builder suite as regression coverage; do not inflate test count with redundant examples. Decide the exact test split before/alongside WP3.3 rather than delaying it until the end.
- 2026-11-17: In response to the maintainer's request for stronger test coverage beyond repeatedly running the existing builder suite, added `ConditionalProductGeneratorTest`. It independently brute-forces small Cartesian products and compares ordered output for PRUNED_BITSET, PRUNED_DFS, DP, and ADAPTIVE across 300 fixed-seed random rule sets. It also covers unconstrained products, empty dimensions, a fully dead-ended product, repeated iteration, idempotent `hasNext`, exhaustion, and `next` after exhaustion. Existing `ConditionalProductBuilderTest` remains the public API and broader builder regression suite. Awaiting focused execution of both classes.
- 2026-11-17: Maintainer reported three failures in `ConditionalProductRuleOrderTest`, all caused by its reflection helper looking for the former `relations` field directly on `ConditionalProductBuilder`. Updated the test helper to follow the extracted ownership path (`builder.model.relations`); no production code changed. Maintainer confirmed 34 tests passed after the correction. The dedicated generator-level strategy/differential tests and the rule-order regression suite are now verified. WP3.3 remains the next implementation step; do not start it until the maintainer says to proceed.
- 2026-11-17: Delivered WP3.3: extracted the DP lexicographic iterator from the private nested `LexItr` in `ConditionalProductGenerators` into package-private `DpLexGenerator`. The iterator retains the same exact-count lookahead through `ConstraintPlan.firstValid`, lexicographic candidate order, lazy `hasNext` advancement, tuple materialization through `ConstraintModel`, and exhaustion behavior. Updated strategy dispatch to instantiate the extracted class. No counting, rank, or sampling code changed. Awaiting maintainer verification with the focused builder, generator differential, and rule-order suites.
- 2026-11-17: Maintainer confirmed all 34 tests passed after WP3.3. Reviewed the final strategy wiring for WP3.4: `PRUNED_BITSET`, `PRUNED_DFS`, and `DP` dispatch to their dedicated iterator implementations in `ConditionalProductGenerators`; `ADAPTIVE` is resolved by `ConditionalProductBuilder` using the existing 10,000-tuple threshold, and remains available. Verified WP3.5 requires no enum rename: all four required constants are already present. WP3 is complete. No strategy was removed or deprecated.


- 2026-11-17: Refactored the conditional-product benchmark into `benchmarks.conditionalproduct`, with separate `scenario` and `baseline` packages. Moved declarative case loading/rule construction into `Scenario` and `Rule`, and the nested-stream reference implementation into `NestedStreamBaseline`; the JMH benchmark retains the same 17 case parameters, five implementations, checksum validation, and two timing boundaries. Added `ConditionalProductBenchmarkRunner`, which runs JMH programmatically, preserves console progress/errors, writes complete JSON to `target/benchmarks/ConditionalProductStrategyBenchmark_results.json`, and writes one final text summary per method (`ConditionalProductStrategyBenchmark.traversalOnly_output.txt` and `...endToEnd_output.txt`). Supports optional method, scenario, implementation, output-directory selectors; GC profiling is on by default. This is delivered but not yet compiled or run by the maintainer.

- 2026-11-17: Continued WP4 follow-up for `anyOrder()`: extended `PrunedLexGenerator`/`BitSetPrunedGenerator` to start from a fixed prefix and a candidate range at the next dimension. Replaced the deliberately unsplittable `AnyOrderGenerator` spliterator with a lazy prefix-frontier spliterator. It splits disjoint candidate ranges and, when the current range contains at most one candidate, fixes that candidate and descends to the next dimension before attempting another split. Added tests for split partition coverage/uniqueness, splitting beneath a singleton first dimension, and parallel stream tuple-set equality. No automatic parallel threshold or default-stream parallelism was enabled; those decisions remain benchmark-gated. `lexOrder()` was not changed. Awaiting maintainer compilation and focused test execution.

- 2026-11-17: Maintainer confirmed the focused `ConditionalProductGeneratorTest` passed after adding prefix-frontier splitting. Added a further concurrency regression: six independent parallel traversals share the same `anyOrder()` generator/model and each must match the complete expected tuple set. This specifically exercises safe lazy publication of shared relation rows under concurrent first use. Awaiting focused test execution before benchmark work.

- 2026-11-17: Added `AnyOrderParallelBenchmark` as a focused JMH comparison of complete sequential versus parallel `anyOrder()` traversal. It covers wide, singleton, narrow, and skewed prefix roots; validates an order-independent checksum between both modes during trial setup; and separates traversal-only timing from end-to-end builder/rule construction plus traversal. No automatic parallel threshold or production execution-policy change is included. Awaiting maintainer compilation and benchmark results.

- 2026-11-19 (H1): item 8 first step. `ConstraintCounts` long-first construction with BigInteger fallback, shared
  row sums, prefix sums for single-run rows. count() allocation on LARGE shapes 1.5-13 MB -> 0.1-0.6 MB (sandbox).
  Open: `sample` at counts above 2^62 (21-45 MB per call), small-product setup cost, empty-product isEmpty workload.
- 2026-11-19 (H1 confirmed, H3): H1 pushed as 73e253b. JMH special: LARGE count 5x-11x faster, choice/byRanks up to 10x/4x,
  sample only 0.7-0.8x. H3: BigInteger counting queries skip identity multiply/add; sandbox sample allocation at
  20x50 21.5 -> 2.0 MB, 20x100 44.7 -> 4.1 MB. Awaiting JMH confirmation. Open: small-product setup cost, empty-product isEmpty workload.
- 2026-11-19 (H1 confirmed, H3): H1 pushed as 73e253b. JMH special: LARGE count 5x-11x faster, choice/byRanks up to 10x/4x,
  sample only 0.7-0.8x. H3: BigInteger counting queries skip identity multiply/add; sandbox sample allocation at
  20x50 21.5 -> 2.0 MB, 20x100 44.7 -> 4.1 MB. Awaiting JMH confirmation. Open: small-product setup cost, empty-product isEmpty workload.
- 2026-11-19 (H4): item 8 leftovers. Added the long-query overflow-invariant comment, cached the full product in
  `mostTuplesValid`, documented the `AnyOrderGenerator.trySplit` invariant, added the concurrent first-use test of the
  counting tier and the empty-product benchmark (`EMPTY_DEEP-5x25`, third file in the `special` preset). Small-product
  setup cost traced to plan construction (about 66 KB per call at 5x25), not counting; deferred. H3 JMH: sample, byRanks,
  lexOrderMth about 2x faster at LARGE-20x50/20x100. Review items declined: cheap `estimateSize` (would cap parallel DP),
  lookahead when the plan exists (costs time on healthy shapes). Open: multi-relation shape to measure `cardinality()` in
  `BitSetPrunedGenerator` (review finding 4), then the item 7 confirmation run.
