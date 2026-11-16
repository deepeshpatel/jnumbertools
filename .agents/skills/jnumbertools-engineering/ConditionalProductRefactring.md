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

Benchmark evidence (JMH, maintainer's machine, `ConditionalProductStrategyBenchmark`, 3 to 5 dimensions, lists of 25 to 100):

| Finding | Numbers |
|---|---|
| Walking tuples is already fast | library pruned traversal is 3.4 to 5.7x faster than a nested Java stream (e.g. CHAIN 10,000 rows: 137 us vs 786 us) |
| Setup dominates small jobs | builder construction is about 491 us (CHAIN), 76 us (STAR), 1.1 ms (CYCLE): 63 to 100% of the end-to-end time at 1,000 rows |
| End-to-end crossover | library beats a nested stream from about 10,000 rows (CHAIN tie, STAR 2.6x faster, CYCLE nested stream still 1.4x faster); below that the stream wins by 1.2 to 4.5x |
| `DP` loses on cycles | CYCLE end to end: 13 ms vs 1.1 ms (loop block enumeration, about 17 MB per run) |
| `ADAPTIVE` cutoff is unproven | from 10,000 tuples it picks `DP`, which is 12x worse on CYCLE |
| `PRUNED_DFS` has no winning region yet | traversal 2x slower than BitSet at 10,000 rows, 2.3x more allocation; kept until the many-small-lists shapes are measured |

Hypothesis for the setup cost (not yet profiled): every `withRule` copies the whole rule list, rebuilds a sorted set of linked pairs and re-runs the loop check, so building R rules costs O(R^2).

Goals: (1) cut setup cost for every strategy; (2) one shared constraint model, several interchangeable generators; (3) parallel traversal through the normal `stream().parallel()`.

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
- No stream-based generator: the nested stream wins at small sizes only because the benchmark hands it raw tables; a generator behind the public rule API cannot reproduce that, and it is 3 to 6x slower per tuple.
- Do not drop `PRUNED_DFS` early (a user may have thousands of tuples over small lists).
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

- [ ] 1.1 Run the loop check only when a rule adds a new linked pair
- [ ] 1.2 Replace the sorted-set rebuild per rule with a scan for "is this pair already linked"
- [ ] 1.3 Chain the rules (shared tail) so adding a rule is O(1) instead of copying the list
- Acceptance: `builderConstructionOnly` clearly lower on CHAIN and CYCLE; identical tuples on the differential check.

### WP2: Extract the shared model

- [ ] 2.1 Move `Rule`, `Relation` into package-private files
- [ ] 2.2 `ConstraintModel` (dimensions, rules, lazy `Rows`)
- [ ] 2.3 `ConstraintPlan` (feasibility) and `ConstraintCounts` (exact) as separate files
- [ ] 2.4 `ConditionalProductBuilder` becomes the facade only (target: well under 500 lines)

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
- [ ] 6.3 Decide `PRUNED_DFS` and `ADAPTIVE` from benchmark data (keep, change cutoff, or remove)

## 5. Progress summary

| Package | Done | Total |
|---|---|---|
| WP0 baseline | 0 (3 delivered, unconfirmed) | 3 |
| WP1 setup cost | 0 | 3 |
| WP2 shared model | 0 | 4 |
| WP3 generators | 0 | 5 |
| WP4 parallel | 0 | 3 |
| WP5 benchmarks | 0 | 3 |
| WP6 finish | 0 | 3 |

Update this table whenever an item changes status.

## 6. Open questions for the maintainer

- Keep the new generator classes package-private in `...generator.product.conditional`, or move them into a subpackage?
- Final default and the fate of `PRUNED_DFS` / `ADAPTIVE`: decided by WP5 data, not before.

## 7. Verification method

- Differential check: build the same rule sets with the old reference commit and with the new code and compare a digest of the full output (count, order, hash). Use the fixed cases plus thousands of seeded random rule sets (random dimensions, sizes, rules, including loops and dead ends).
- Brute-force oracle: for small random rule sets, enumerate the whole product and filter; compare with every generator, count, rank, and `lexOrderMth`.
- Parallel: compare `stream().parallel()` collected in order with the sequential list.
- Unit tests live next to the existing ones in `ConditionalProductBuilderTest`, `ConditionalProductRuleOrderTest`, and a new differential test per generator.

## 8. Progress log (newest first)

- 2026-11-16: Plan written. WP0 patch (`conditional-compact-rules.patch`) delivered against a4b57d2; benchmark with the plain Java stream row delivered. Maintainer's local tree has `LexOrderStrategy {PRUNED_BITSET, PRUNED_DFS, DP, ADAPTIVE}`; waiting for the push before rebasing WP0 and starting WP1.