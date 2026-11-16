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
- [~] 2.4 Continued reducing `ConditionalProductBuilder` to the facade. Extracted the immutable persistent AVL relation map into package-private `PersistentRelationMap`, graph validation/decomposition into package-private `ConstraintGraph`, and rule dimension/value resolution into package-private `RuleCompiler`. Further extractions are required before this item is complete.

### WP3: Generators

- [ ] 3.1 `PrunedLexGenerator` (abstract control flow) and `BitSetPrunedGenerator`
- [ ] 3.2 `DenseDfsGenerator` (dense boolean rows, size cap, falls back to BitSet beyond the cap)
- [ ] 3.3 `DpLexGenerator`
- [ ] 3.4 Facade selects a generator from `LexOrderStrategy`; `ADAPTIVE` kept
- [ ] 3.5 Rename the enum constants (`PRUNED_BITSET`, `PRUNED_DFS`) if the maintainer's push has not already

### WP4: Parallel

- [ ] 4.1 Splittable spliterator for the pruned generators (prefix split, ordered, size threshold)
- [ ] 4.2 Rank-range split for `DpLexGenerator`
- [ ] 4.3 Tests: ordered parallel equals sequential for all generators; `unordered()` has the same set of tuples

### WP5: Benchmarks (maintainer runs them)

- [ ] 5.1 Tier 1: dimensions 3, 5, 7, 10 x list sizes 10, 25, 50, fully consumed up to about 100,000 rows; include many small lists
- [ ] 5.2 Tier 2: dead-end shapes (`STAR_LAST`, 5 to 10% density chain, empty product)
- [ ] 5.3 Tier 3: large sizes only if tiers 1 and 2 leave open questions
- Settings: one fork, fewer iterations; precompute rule inputs outside the timed section; report both traversal-only and end-to-end.

### WP6: Finish

- [ ] 6.1 Javadoc for every strategy, with a plain statement of when to use it
- [ ] 6.2 README and benchmarks README update; changelog line
- [ ] 6.3 Benchmark all four retained strategies after the complete flow is ready; tune implementations, defaults, and the `ADAPTIVE` cutoff where evidence supports a change. Do not remove or deprecate any strategy.

## 5. Progress summary

| Package | Done | Total |
|---|---|---|
| WP0 baseline | 0 (3 delivered, unconfirmed) | 3 |
| WP1 setup cost | 3 verified | 3 |
| WP2 shared model | 3 verified, 1 in progress | 4 |
| WP3 generators | 0 | 5 |
| WP4 parallel | 0 | 3 |
| WP5 benchmarks | 0 | 3 |
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

- 2026-11-17: Continued WP2.4: extracted caller-list identity resolution and value-to-index predicate compilation into `RuleCompiler`. The fluent builder delegates `when(...)`, `then(...)`, and trigger/target matching while preserving validation messages and BitSet semantics. Awaiting focused test verification.
