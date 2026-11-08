# Changelog

All notable changes to JNumberTools are recorded here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [4.0.0] - Unreleased

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