# Changelog

All notable changes to JNumberTools are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [4.2.0] - Released 2026-11-21

### Added

- Added a multi-list `conditionalProductOf(first, second, additional...)` overload alongside the
  existing single-list entry point and `.and(...)` chaining, and a matching
  `new ConditionalProductBuilder(first, second, additional...)` constructor.
- Added compound AND/OR trigger expressions through a separate `compoundProductOf(...)` entry
  point: `.when(dim).is(v).and(dim2).is(v2).or(dim3)...then(dim4).is(v4)`, interpreted as an OR of AND-groups,
  with AND binding more tightly than OR. Its `CompoundConditionalProductBuilder` exposes `generate()`,
  `count()` and `isEmpty()`, and now has a matching
  `new CompoundConditionalProductBuilder(first, second, additional...)` constructor alongside the
  single-dimension one. `conditionalProductOf(...)` remains the pairwise-only entry point and
  additionally exposes `lexOrder()`, `lexOrderMth()`, `byRanks()`, `choice()` and `sample()`.
- Added `Condition`, a composable trigger condition built with `condition(dim).is(v)` and combined
  with `.and(Condition)`/`.or(Condition)`, plus a matching `.when(Condition)` overload on
  `CompoundConditionalProductBuilder` (and on `ConditionalProductBuilder`, for internal resolution).
  Unlike the flat `.when(dim)...and(dim2)...or(dim3)...` chain, which can only express a single level
  of OR-of-AND-groups, a `Condition` can be nested arbitrarily (AND inside OR, OR inside AND), because
  grouping is expressed by nested method calls rather than by reading a chain left to right. `and(...)`
  distributes over `or(...)` into the same OR-of-AND-groups form the flat chain already compiles to, so
  evaluation and counting are unaffected by how a condition was built; combining conditions into more
  than 4096 AND-groups is rejected rather than silently accepted.

### Fixed

- Fixed compound rules whose scope overlaps a chain of pairwise rules. The chain was merged with the
  scope into one enumerated block, which made counting far slower than the same chain without the
  compound rule and rejected models that contain no loop. The chain between the scope's dimensions
  is now a separate node joined to the scope by the rules between them, so each node is enumerated and
  bounded on its own.
- Fixed a gap where a node-to-node relation table built between two multi-dimension nodes (a compound
  scope, or an ordinary pairwise loop of three or more dimensions) joined by a single rule was not
  size-checked at all; only tables built from two or more rules between the same pair of nodes were.
  The real cost of such a table depends on how many distinct combinations of values the linking rules'
  own dimensions actually take, which can be far smaller than either node's full size once rows that
  project to the same values are shared, so the check is applied as the table is actually being built,
  bounding the work by `MAX_PAIR_CELLS` directly rather than by an estimate from the nodes' sizes. An
  earlier version of this fix checked the nodes' full sizes instead and rejected some legitimate, cheap
  rule sets as a result; it was replaced with this one before release.

### Changed

- Unified the compound and pairwise conditional-product engines internally.
  `CompoundConditionalProductBuilder` (returned by `compoundProductOf(...)`) now delegates to the same
  `ConditionalProductBuilder`/model that `conditionalProductOf(...)` uses, instead of a separate,
  fully duplicated `compound` package, which is gone. The public API is unchanged by this: the two
  entry points and their two builders remain separate, and `conditionalProductOf(...)`'s own
  `.and(...)/.or(...)` trigger methods remain package-private (not reachable from
  `ConditionalProductBuilder` directly).
- Renamed `compoundConditionalProductOf(...)` to `compoundProductOf(...)` on `CartesianProduct`.


## [4.1.0] - Released 2026-11-20

### Added

- Added `anyOrder()` for lazy, re-iterable generation of every valid conditional-product tuple exactly once,
  without an encounter-order guarantee. Its spliterator can partition work for parallel streams.
- Added explicit conditional-product lexicographic traversal strategies: `PRUNED_BITSET` (the default),
  `PRUNED_DFS`, `DP`, and the implementation-policy entry point `ADAPTIVE`, which currently resolves to
  `PRUNED_BITSET` (no guarantee about which algorithm runs; the `estimatedLimit` argument is validated
  but does not change the choice).

- Conditional Cartesian product, started with `cartesianProduct().conditionalProductOf(list)` and
  extended with `.and(list)` for more dimensions and `.when(list).is(...).then(list).isOneOf(...)` for
  rules between two dimensions (`is`, `isNot`, `isOneOf`, `isNoneOf`, `isNotIn`). All rules must hold.
  `count()`, `isEmpty()`, `lexOrder()`, `lexOrderMth(...)`, `byRanks(...)`, `choice(...)` and `sample(...)`
  work on the valid tuples only, without enumerating the full product, and ranks are `BigInteger`.
  Rules are evaluated on pairs of dimensions; dimensions linked in a loop are solved together and the
  size of such a group is limited. See `docs/products/README.md` and `ValidationRules.md`.

### Changed

- Conditional-product lexicographic traversal now uses shared, lazy work-triggered completion lookahead
  in the two prefix-pruned strategies. It does not change the tuples or their lexicographic order.
- The conditional-product implementation is split into a fluent public builder and package-private
  model, relation, planning, and generator collaborators.

- The simple, constrained and conditional products now return each tuple as one small immutable list
  object (`io.github.deepeshpatel.jnumbertools.datastructure.Tuple`) instead of
  `Collections.unmodifiableList(Arrays.asList(...))`, which creates two objects per tuple. Tuples still
  follow the `List` contract (`equals`, `hashCode`, `toString`, immutability, serialization).

### Performance

- Conditional-product counting tables use primitive `long` storage when intermediate counts fit within
  the supported bound, with an exact `BigInteger` fallback. Counting queries also avoid identity
  `BigInteger` operations.
- The benchmark suite now covers full traversal, any-order sequential/parallel traversal, and special
  operations, with reusable quick/full presets and comparison tooling.

No change in results or order; these changes only reduce time and allocation.

- k-permutations in lexicographic order (`nPk(k, list).lexOrder()`) advance a primitive index array
  in place instead of using a boxed `LinkedList`.
- Multiset combinations: `lexOrder()` builds each result map once instead of copying it, and
  `byRanks(...)` (also used by `lexOrderMth`, `choice` and `sample`) uses `long` tables when all counts
  fit in a `long`, falling back to `BigInteger` otherwise.
- Factoradic unranking (used by permutation `byRanks`, `lexOrderMth`, `choice` and `sample`) uses
  primitive arithmetic for ranks that fit in a `long`, and for larger ranks extracts several digits
  per pass over a single mutable word array instead of creating `BigInteger`s for each digit.
- Cartesian product by ranks reuses one by-rank generator per dimension and uses `long` arithmetic
  when the whole product fits in 62 bits.
- The conditional product builds its internal plan in the same order on every run, so its
  performance is reproducible from one JVM run to the next.
- Unique and repetitive combinations in `lexOrder()`, and unique permutations in `lexOrder()`, advance
  one index array in place instead of copying it for every result.
- Derangements in `lexOrder()` detect the last result from the successor engine instead of counting
  and comparing a `BigInteger` rank, and the Derangadic engine tracks available values with a
  lighter bit-set-style structure instead of a Fenwick tree.
- Multiset permutations `byRanks(...)` and `lexOrderMth(...)` share one unranker: a linear scan over
  the element types for up to 8 types when the count fits in a `long`, and a Fenwick tree otherwise.
  `lexOrderMth` previously always scanned linearly, which was slow for many types.
- Multiset combinations return each result as a compact immutable map (`FrequencyMap`, backed by
  two arrays) instead of a `LinkedHashMap` wrapped in `Collections.unmodifiableMap`. Iteration order,
  `equals`/`hashCode`/`toString` and immutability are unchanged.
- k-permutations `combinationOrderMth(...)` unrank the combination and its arrangement directly
  instead of creating builders and iterators for every result.
- Repetitive permutations (`byRanks`, `lexOrderMth`), simple Cartesian product by rank and
  Permutadic conversion use primitive arithmetic when the values fit in a `long`.
- Unranking a unique combination whose count does not fit in a `long` (used by unique combinations
  `byRanks`, `lexOrderMth`, `choice` and `sample`, by `unrankOf().combination().unique(...)` and by
  k-permutations `combinationOrderMth`) estimates each position from a table of log-factorials and
  confirms it with one or two exact binomials, instead of a binary search over exact binomials.
  Applies for n ≤ 4096 when the large-r walk is not used (r < 256 in practice); measured 3–5× faster.

### Fixed

- Multiset combinations now return their result maps in the order of the options they were created with,
  on every path (`lexOrder`, `lexOrderMth`, `byRanks`, `choice`, `sample`). Previously the order could
  differ from one JVM run to the next, so seeded output was not reproducible.

### Compatibility

- `isNotIn(...)` remains available as an alias of `isNoneOf(...)`; both varargs and collection overloads
  are retained. No breaking API change is introduced by this alias decision.

### Changed (breaking)

- `JNumberTools.subsets().of(...)` now returns a `SubsetSelector`, which only offers `all()` and
  `inRange(from, to)`. Those return the `SubsetBuilder` with `count()`, `lexOrder()` and the other
  operations. Previously `of(...)` returned a `SubsetBuilder` that had no size range yet: `count()` was
  `0`, `isEmpty()` was `true` and `lexOrder()` threw, so a forgotten `all()` looked like an empty result.
  Now forgetting the range does not compile. Code that always called `all()` or `inRange(...)` right after
  `of(...)`, as the documentation shows, needs no change. The public `SubsetBuilder(List, Calculator)`
  constructor is replaced by `SubsetSelector(List, Calculator)`, and `SubsetBuilder` no longer has
  `all()` and `inRange(...)`.

## [4.0.1] - 2026-11-09

Maintenance release. There are no changes to the supported public API or to library behavior.

### Changed

- Relicensed under the Apache License 2.0. This resolves a conflict in earlier releases, where the
  `LICENSE` file stated GPL-3.0 while the published `pom.xml` stated LGPL-3.0. Earlier releases
  (4.0.0 and before) remain available under the license stated in their published artifacts.
- Source file headers now carry an `SPDX-License-Identifier` and no longer contain a version number.

### Removed

- The `io.github.deepeshpatel.jnumbertools.examples` package, including `AllExamples`. It was not
  part of the public API and was excluded from the javadoc. Examples now live in
  [JNumberToolsExamples](https://github.com/deepeshpatel/JNumberToolsExamples).
- The `io.github.deepeshpatel.jnumbertools.experiments` package (`abstractalgebra`:
  `ExperimentalCombinationAlgorithms`, `PermutationGroup`, `InfiniteSupplyPartition` and
  `WeightedRandomGenerator`). This was research code that 4.0.0 documented as experimental, with
  no stability guarantee.

## [4.0.0] - 2026-11-09

Compared with the last tagged release, 3.0.0. This release reshapes much of the public API around
builders, so **it contains breaking changes**. See the migration table below before upgrading.

### Breaking changes

**Java 21 or newer is required** (3.0.0 targeted Java 17).

**API migration**

| Before (3.0.0) | Now |
|---|---|
| `JNumberTools.unRankingOf()` | `JNumberTools.unrankOf()` |
| `rankOf().uniquePermutation(...)` | `rankOf().permutation().unique(...)` |
| `rankOf().kPermutation(n, ...)` | `rankOf().permutation().k(n, ...)` |
| `rankOf().repeatedPermutation(n, ...)` | `rankOf().permutation().repetitive(n, ...)` |
| `rankOf().uniqueCombination(n, ...)` | `rankOf().combination().unique(n, ...)` |
| `unrankOf().uniquePermutation(rank, n)` | `unrankOf().permutation().unique(rank, n)` |
| `unrankOf().kPermutation(rank, n, k)` | `unrankOf().permutation().k(rank, n, k)` |
| `unrankOf().uniqueCombination(rank, n, r)` | `unrankOf().combination().unique(rank, n, r)` |
| `Permutations.multiset(List<T>, int[])` | `Permutations.multiset(LinkedHashMap<T, Integer>)` (element to frequency) |
| `Combinations.multiset(List<T>, int[], int)` | `Combinations.multiset(LinkedHashMap<T, Integer>, int)` |
| `CartesianProduct.complexProductOf(...)` | `constrainedProductOfDistinct(...)`, `constrainedProductOfMultiSelect(...)` or `constrainedProductOfInRange(...)` |
| `CartesianProduct.simpleProductOf(E...)` and `SimpleProductBuilder.and(E...)` | pass a `List` to `simpleProductOf(List)` / `and(List)` |
| `Calculator.pow(...)` | `Calculator.power(...)` |
| `UniquePermutationBuilder.fast()` | removed |

**Other breaking changes**

- Builders now return `StreamableIterable<E>` (an `Iterable` that also offers `stream()`). The
  intermediate result classes `UniqueCombinationMth`, `RepetitiveCombinationMth`,
  `RepetitiveCombinationMultiset`, `UniquePermutationsMth`, `KPermutationLexOrderMth`,
  `SimpleProductMth`, `ComplexProduct*`, `MthElementGenerator`, `CartesianProductUtils` and the
  `UniquePermItr*` iterators were removed. Code that held these types should use `Iterable<List<T>>`
  or `StreamableIterable<List<T>>`.
- Cartesian products are no longer generic: elements are produced as `List<Object>`.
- The `Calculator(int, int, int)` constructor was removed.
- `PermutadicAlgorithms` no longer has an instance API (`unRankWithBoundCheck` and the constructor
  taking a `Calculator` were removed); the static `unRank` performs no bound check. Prefer
  `unrankOf()` and `rankOf()`.
- `PermutationGroup` (package `experiments.abstractalgebra`) methods are now static.
- **Input validation is now defined by [ValidationRules.md](ValidationRules.md) and is performed by
  the builders and the `rankOf()` / `unrankOf()` helpers.** Null inputs, negative sizes, counts or
  frequencies, and out-of-range ranks signal an error, and so do arguments that were previously
  accepted or failed in a different way. Empty domains are valid and behave as the document
  describes (for example, `count()` is `0` and iteration yields nothing).

### Added

- Every builder offers the same operations: `lexOrder()`, `lexOrderMth(m, start)`, `byRanks(ranks)`,
  `choice(n, random)`, `sample(n, random)`, `count()` and `isEmpty()`. `byRanks` consumes its ranks
  lazily.
- `JNumberTools.derangements()`: derangement generators (lexicographic order, every m-th, ranks,
  random choice and sample).
- `JNumberTools.numberSystem()`: Factoradic, Permutadic, Combinadic and Derangadic.
  `Derangadic` encodes derangements and can walk consecutive ranks incrementally.
- Ranking and unranking through `rankOf().permutation()` / `.combination()` and
  `unrankOf().permutation()` / `.combination()`, covering unique, k-, repetitive, multiset and
  derangement variants.
- Constrained Cartesian products with three entry points (`constrainedProductOfDistinct`,
  `constrainedProductOfMultiSelect`, `constrainedProductOfInRange`) and `byRanks`, `choice` and
  `sample` for simple and constrained products.
- `Calculator`: `subFactorial`, `multinomial`, `rencontresNumber`, `restrictedDerangementCount`,
  `telephoneNumber`, `telephoneTable`, `factorialUpperBound`, `nCrUpperBound`, `gcd`, `lcm`, the
  multiset counting helpers and `clearCaches()`.
- Public data structures in `datastructure`: `FenwickTree`, `FreqVector`, `BinomialCoefficientMap`
  and `TwoLevelMap`.
- Documentation: a restructured README with one page per generator family, `ValidationRules.md`,
  and a javadoc overview with package documentation.

### Changed

- Faster ranking, unranking and counting across the library:
  - Fenwick-tree algorithms for k-permutation and derangement ranking and unranking.
  - A sparse O(k²) path for k-permutations of large sets, so cost no longer depends on the set size
    when k is small. The switch between sparse, Fenwick and list-based algorithms was tuned with the
    `OptimizationBenchmark` harness, and the digit conversions between a permutation and its
    Permutadic form now use the same selection.
  - Combination unranking that walks the digits for large r, and multiset combination counting and
    unranking by dynamic programming.
  - Multiset permutation unranking using a Fenwick tree.
  - Hybrid factorial and subfactorial caches.
- Test suite reorganised so that tests go through the public builders; experiments and benchmark
  harnesses live under `src/test/.../experiments` and are disabled by default.
- Build: compiles with `--release 21`; source, javadoc and signing steps moved into a `release`
  profile; publishing configured for the Maven Central Portal; GitHub Actions workflow added.

### Deprecated

- `CartesianProduct.constrainedProductOf(int, List)`, scheduled for removal. Use
  `constrainedProductOfDistinct`, which behaves identically.

### Experimental

- The `numbersystem.involutadic` classes and the `experiments` packages are research code. They are
  public but have no stability guarantee and may change or be removed without a major version bump.