# ConditionalProductBuilder: generator split, faster setup, parallel traversal

Working plan and progress tracker. **Read this file first when resuming. Update the Progress section after every step.**

## 0. How to work on this (rules for the AI assistant)

1. Read sections 1 to 6, then the Progress table. Continue from the first unchecked item. Do not redo checked items.
2. Work only when the maintainer says "go ahead". Each go-ahead delivers one or two small items (a patch plus tests), then updates the Progress table and log in this file as part of the same patch.
3. Do **not** run JMH, and do not run the full test suite unless asked. Compile with `javac`, and run only the narrow checks the maintainer allows. The maintainer runs `mvn test` and the benchmarks and shares the output.
4. Deliver changes as a patch against the maintainer's latest pushed commit. If the repository state is unknown or newer than the last log entry, ask for a push first. Do not build on a stale base.
5. Never change behaviour silently. Every generator must produce exactly the same tuples in the same order as the reference (see section 7).
6. Keep the number of releases low. Changes are batched into one release; do not tag.
7. Naming: strategies are called **generators** (`XGenerator`), not builders. The builder is the one class that collects rules.

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
| `ADAPTIVE` | Selects `PRUNED_BITSET` or `DP` from the caller's expected tuple consumption | Required implementation; selection heuristic and cutoff may be tuned using benchmark evidence |

**Retention rule: all four enum constants and their corresponding implementations must remain available throughout this work. No strategy is to be removed or deprecated as part of these work packages.** If a strategy performs poorly, improve its implementation and tests. Only after the complete flow is implemented and correctness-verified should the full benchmark suite be used to tune implementations, defaults, or the `ADAPTIVE` cutoff. Benchmark results may inform later maintainer decisions, but this plan does not authorize removing a strategy.

The current adaptive threshold (10,000 tuples) is provisional. The full-generation target is complete consumption, so benchmark coverage must include full traversal rather than infer the best strategy from first-N microbenchmarks.

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
- Parallel is opt-in through `stream().parallel()`; the default stays sequential. No automatic parallel selection (the real output size is unknown without counting).
- Parallel splitting: custom `Spliterator` whose `trySplit` cuts by prefix (contiguous ranges of the first dimension, then further dimensions if needed). Pieces are lexicographically ordered, so ordered parallel results stay correct; `.unordered()` drops the ordering cost. It stops splitting below a work threshold. `DpLexGenerator` can split by rank range with exact sizes (costs the counting plan).
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
- [x] 4.3 Added unordered parallel-stream tuple-set checks for PRUNED_BITSET, PRUNED_DFS, DP, and ADAPTIVE; maintainer confirmed the focused suite passed (36 tests). Ordered parallel equality remains covered for all explicit generators.

### WP5: Benchmarks (maintainer runs them)

- [~] 5.1 Delivered `ConditionalProductStrategyBenchmark` with case definitions externalized as classpath `.properties` resources. JMH's `scenarioName` parameter is the case's relative resource path, so a single case can be selected without editing Java source. All four library strategies are compared with the nested-stream baseline; traversal-only and end-to-end are separate benchmark methods.
- [~] 5.2 Delivered the hierarchical case tree: topology → numeric dimension/size range → selectivity → case. Numeric ranges are inclusive and non-overlapping: `Dim2-5`, `Dim6-10`, `Dim11-20`; `Size10-100`, `Size101-250`. STAR, CHAIN, SPARSE, DENSE, CYCLE, DEAD_END, and EMPTY coverage is retained. Workloads outside the numeric ranges are isolated under `edge-cases`.
- Benchmark limitation recorded: current rules select every trigger value. The selectivity profile therefore varies allowed target values, not trigger coverage; do not interpret these results as a trigger-selectivity study. Add trigger-coverage as a separate workload axis in a later benchmark slice.
- [~] 5.3 Added the initial large-size tier for all three dimension bands and both size bands. Large cases use the TIGHT profile (approximately 1% of target values, minimum one) to keep complete traversal bounded. These cases still require maintainer compilation and benchmark execution.
- Settings: one fork, one 500 ms warmup and two 500 ms measurement iterations; precompute dimensions/rule inputs outside timed methods; report traversal-only and end-to-end. Benchmark methods do not print; use JMH progress/ETA output and write detailed results as JSON.

### WP6: Finish — deferred until the very end

These three deliverables are intentionally postponed until the implementation, parallel work, and benchmark design/results are settled. Do not start them during intermediate work packages.

- [ ] FINAL TODO 1 — Javadoc update: document every strategy and give a plain statement of when to use it.
- [ ] FINAL TODO 2 — README update: update the main README, benchmarks README, and changelog to reflect the final API, behavior, and measured results.
- [ ] FINAL TODO 3 — Benchmark update: run the delivered full-traversal benchmark suite for all four retained strategies, then use the evidence to tune implementations, defaults, and the ADAPTIVE cutoff where justified. Add further scenarios or large-size cases only when the first results identify a gap. Never remove or deprecate a strategy.

## 5. Progress summary

| Package | Done | Total |
|---|---|---|
| WP0 baseline | 0 (3 delivered, unconfirmed) | 3 |
| WP1 setup cost | 3 verified | 3 |
| WP2 shared model | 4 verified | 4 |
| WP3 generators | 5 verified; all four strategies retained and dispatched | 5 |
| WP4 parallel | 3 verified | 3 |
| WP5 benchmarks | 3 delivered, unverified | 3 |
| WP6 finish | 0 | 3 |

Update this table whenever an item changes status.

## 6. Open questions for the maintainer

- Keep the new generator classes package-private in `...generator.product.conditional`, or move them into a subpackage?
- Final default and the `ADAPTIVE` cutoff may be tuned after WP5; all four strategies remain available.

## 7. Verification method

- Differential check: build the same rule sets with the old reference commit and with the new code and compare a digest of the full output (count, order, hash). Use the fixed cases plus thousands of seeded random rule sets (random dimensions, sizes, rules, including loops and dead ends).
- Brute-force oracle: for small random rule sets, enumerate the whole product and filter; compare with every generator, count, rank, and `lexOrderMth`.
- Parallel: compare `stream().parallel()` collected in order with the sequential list.
- Unit tests live next to the existing ones in `ConditionalProductBuilderTest`, `ConditionalProductRuleOrderTest`, and a new differential test per generator.

## 8. Progress log (newest first)

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

## 9. Maintainer clarifications (2026-11-17)

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
