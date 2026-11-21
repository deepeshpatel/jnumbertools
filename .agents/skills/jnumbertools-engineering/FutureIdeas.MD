# Future Ideas

This file records potential future directions for JNumberTools. These are exploration notes, not approved features or final API contracts.

## SAT Solver and SAT Enumerator

Explore adding a Boolean satisfiability (SAT) capability to JNumberTools. A SAT formula consists of Boolean variables and logical constraints (typically represented in conjunctive normal form, CNF). The capability could expose three distinct operations: finding one satisfying assignment, enumerating every satisfying assignment, and counting satisfying assignments.

Illustrative API shape:

```java
// Boolean satisfiability: find one satisfying assignment
JNumberTools.SAT().solve(formula);

// Enumerate all satisfying assignments
JNumberTools.SAT().allSolutions(formula);

// Count satisfying assignments (#SAT)
JNumberTools.SAT().count(formula);
```

These names and signatures are illustrative only. They are not a proposed final API contract. The eventual design should follow JNumberTools' builder conventions and be informed by the formula model, solver architecture, result semantics, and performance requirements.

### Separate problem types

Initially consider these as separate operations because their objectives, algorithms, costs, and result types differ.

| API concept | Purpose | Expected result |
|---|---|---|
| `solve()` | Determine whether a formula is satisfiable | SAT/UNSAT status and, when satisfiable, possibly one satisfying assignment |
| `allSolutions()` | Generate every satisfying assignment | Lazy stream/iterable of assignments |
| `count()` | Count all satisfying assignments (#SAT) | `BigInteger` |

Important semantic points to settle during design:

- `solve()` should distinguish UNSAT from SAT and make the satisfying assignment available when one exists. Decide whether this is represented by a result object, an optional assignment, or another explicit type.
- `allSolutions()` should ideally be lazy, avoid retaining all assignments, and define a stable variable ordering and assignment representation. Enumeration may take exponential time because the output itself can be exponential.
- `count()` must use `BigInteger`; the number of satisfying assignments can exceed primitive integer ranges. Counting should not be implemented by materializing the stream from `allSolutions()`.
- Define behaviour for an empty formula, an empty clause, unused variables, duplicate clauses, contradictory constraints, and variables that do not occur in the formula.
- Establish whether the initial formula representation is restricted to CNF and how literals, clauses, and variable identifiers are modelled.

### Relationship to existing JNumberTools capabilities

The Boolean assignment space for (n) variables is a Cartesian product of (n) binary domains, with (2^n) possible assignments. Existing Cartesian-product generation provides a useful conceptual baseline for representing this search space.

Conditional Cartesian products may express some pairwise restrictions and can help explore small constrained instances. However, general SAT clauses can involve multiple variables simultaneously, and a practical SAT solver needs dedicated propagation, branching, conflict handling, and related search techniques. A general SAT implementation should therefore be designed as its own capability rather than treated as a thin wrapper around conditional-product generation.

Keep the responsibilities distinct:

1. **SAT solving:** search for one satisfying assignment or establish that none exists.
2. **SAT enumeration:** systematically generate all satisfying assignments without requiring the caller to construct the full Cartesian product.
3. **#SAT counting:** count satisfying assignments using a dedicated counting algorithm, not by assuming enumeration is affordable.

The theoretical breakthrough discussed in recent work on counting satisfying assignments is not itself a practical SAT solver implementation. JNumberTools should not claim to reproduce such a result merely by adding these APIs.

### Possible later specializations

After a general formula model and the three core operations have been evaluated, consider specialized entry points or algorithm choices for:

- **3-SAT:** formulas whose clauses contain at most three literals.
- **k-SAT:** formulas whose clauses have a bounded or specified number of literals.
- **Weighted Max-SAT:** find an assignment that maximizes the total weight of satisfied clauses (or otherwise satisfies a clearly defined optimization objective).

These should be treated as later extensions, not requirements for the first SAT capability. In particular, weighted Max-SAT is an optimization problem and should have result semantics distinct from SAT/UNSAT and #SAT.

### Suggested investigation sequence

1. Define a small, immutable Boolean formula model and its CNF semantics.
2. Specify assignment, SAT/UNSAT, enumeration, and counting result contracts.
3. Build correctness tests against exhaustive enumeration for small formulas.
4. Prototype `solve()` independently and compare it with a straightforward brute-force baseline.
5. Prototype lazy `allSolutions()`, verifying completeness, uniqueness, and deterministic ordering.
6. Prototype `count()` as a separate algorithm and cross-check its results against enumeration on small formulas.
7. Benchmark representative formula structures and sizes; record runtime and allocation, not just asymptotic expectations.
8. Evaluate whether specialized 3-SAT, k-SAT, or weighted Max-SAT APIs are justified by demonstrated use cases.

No implementation approach, final API naming, or release target is decided by this note.

## Conditional product: deferred work and rejected ideas (from the 4.1.0 optimisation work)

Notes carried over from the retired `ConditionalProductRefactring.md`. They are not approved features.

### Deferred

- **Compiled / reusable product API** (declare dimension names and rules once, bind different lists later;
  `.compile()` and `.bind(...)`, for the Simple, Constrained and Conditional products). A test-source prototype
  matched the ordinary builder exactly. One JMH run (100 datasets, setup-dominated operations) showed 25 to 50%
  less time on `isEmpty`, `count` and `first100`, and only 5 to 12% on long enumerations, but a later end-to-end
  test by the maintainer showed no gain, so it is parked. Reopen only if a representative repeated-dataset workload
  shows a measurable end-to-end gain including binding and consuming results. If it returns: rule values are
  fixed at compile time, a missing value fails at bind time, and the uncompiled path must not change.
- **Small-product setup cost.** Fixed cost on small products is plan construction (about 66 KB per call at 5x25),
  not counting. Unprofiled hypothesis: each `withRule` copies the rule list and re-runs the loop check.
- **Automatic parallel policy for `anyOrder()`.** Needs complete-consumption benchmarks across representative shapes
  and must not change `lexOrder()` parallel behaviour. `anyOrder()` dimension reordering is on hold.
- **`ADAPTIVE` and its `estimatedLimit` argument.** `ADAPTIVE` resolves to `PRUNED_BITSET` and the argument is
  validated but unused. Decide whether to give it a real cutoff (needs dead-end benchmark evidence) or remove it
  in a major version.
- **`Relation` row sharing** only covers up to 62 rules per pair and copies its rule array per added rule. Measure
  one-rule-per-value shapes at large sizes before changing it.
- **Optional confirmation:** a full multi-fork JMH matrix before making broad performance claims.
- **Library-wide leftovers** (the earlier "item 10"), to be done on a separate branch and verified with the shim suite.

### Tried and rejected (do not retry without new evidence)

- A cheap `estimateSize` in `DpLexSpliterator`: it makes the first split's children leaves and caps parallel DP at about 2.
- Enabling completion lookahead whenever the plan exists: it costs time on healthy shapes.
- Exact-count `anyOrder()` splitting: about 5x slower on small shapes in parallel (counting setup costs more than the split saves).
- Pivot by `cardinality()` (STAR_LAST experiment): no advantage when tested.
- Do not treat a bimodal allocation figure on JIT-sensitive paths (for example DEEP_DEAD_END, 9.6 vs 15.4 MB) as a regression.

## Conditional product: carried over from 4.2.0 (compound/pairwise engine unification)

Notes carried over from the retired `ENGINEERING-4.1.1.md`. They are not approved features.

- **Whether `compoundConditionalProductOf(...)` / `CompoundConditionalProductBuilder` should still exist.**
  The compound and pairwise engines were unified under the hood: both now share one `ConditionalProductBuilder`
  and one constraint model. But the public surfaces were not unified. `ConditionalProductBuilder.Trigger.and()`
  and `.or()` are package-private, so `conditionalProductOf(...)` only ever gives a caller single-condition
  pairwise rules (`.when(x).is(v).then(y).is(w)`); the `.and(...)/.or(...)` compound syntax is reachable only
  through `compoundConditionalProductOf(...)` / `CompoundConditionalProductBuilder`, which is itself limited to
  `generate()`, `count()` and `isEmpty()` and does not expose `lexOrder()`, `lexOrderMth()`, `byRanks()`,
  `choice()` or `sample()` even when a rule set happens to collapse to pairwise. So today there are two
  builders, each with a capability the other lacks, and neither is a strict subset of the other. Worth deciding
  deliberately: either (a) make `and()/or()` public on `ConditionalProductBuilder.Trigger` so one builder has
  the full set (compound syntax plus every traversal/rank/sample operation, throwing
  `UnsupportedOperationException` only when a rule doesn't collapse to pairwise) and retire the separate
  compound entry point; or (b) keep two deliberately different builders, but document the actual capability
  split precisely instead of describing one as a subset of the other.
- **A diagnostic/introspection method** (`.explain()` or similar) reporting the constraint plan's node
  structure: how many dimensions ended up in each node, and which rules caused dimensions to merge into
  one block. The block-merging bug that `0e47496` fixed is the kind of thing that's invisible from the
  public API until `count()` gets slow or throws; a cheap read-only report of the node/block structure
  would let a caller see it coming instead of discovering it empirically.
- **Validation gap:** a compound rule's AND-group isn't checked for referencing the same dimension twice
  with conflicting values (e.g. `.when(x).is(1).and(x).is(2)...`), which is unsatisfiable by construction
  and currently just silently compiles into a group that can never match. Worth a one-line check with a
  clear error message rather than a silent always-false rule.
