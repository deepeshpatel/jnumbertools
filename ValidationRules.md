# JNumberTools - Validation Rules

> **Language-agnostic contract.** This document defines *behavior*, not the API of any one
> language. "Throws" means "the operation signals an error" and nothing more. The categories
> used below (*null-input error*, *invalid-argument error*) are descriptive labels. Which
> concrete exception/error type, error code or message a given language binding uses is an
> implementation choice and is **not** part of this contract (for example, a Java binding may
> use `null-input error` / `invalid-argument error`, another language its own idiom).
> What *is* part of the contract is *when* an error is signaled and when it is not.

Null input (programmer error) signals a **null-input error**.  
Empty collections/maps are allowed and follow the rules below.  
Negative counts/ranges/frequencies signal an **invalid-argument error**.

## UNIQUE COMBINATION (ⁿCᵣ)

| n (set size) | r (selection) | Mathematical | Count | Iterator Returns          | Interpretation                          |
|--------------|---------------|--------------|-------|---------------------------|-----------------------------------------|
| n = 0        | r = 0         | ⁰C₀ = 1      | 1     | [[]]                      | One empty combination                   |
| n = 0        | r > 0         | ⁰Cᵣ = 0      | 0     | []                        | Cannot select from empty set            |
| n > 0        | r = 0         | ⁿC₀ = 1      | 1     | [[]]                      | One empty combination                   |
| n > 0        | 0 < r ≤ n     | ⁿCᵣ          | ⁿCᵣ   | combinations              | Normal case                             |
| n > 0        | r > n         | ⁿCᵣ = 0      | 0     | []                        | Selection exceeds set size              |
| **Error case** | **Condition** | **Throws** | | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | r < 0 | invalid-argument error | | | r must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

## REPETITIVE COMBINATION (ⁿ⁺ʳ⁻¹Cᵣ)

| n (set size) | r (selection) | Mathematical     | Count | Iterator Returns | Interpretation                          |
|--------------|---------------|------------------|-------|------------------|-----------------------------------------|
| n = 0        | r = 0         | (by convention)  | 1     | [[]]             | One empty combination                   |
| n = 0        | r > 0         | 0                | 0     | []               | Cannot select from empty set            |
| n > 0        | r = 0         | 1                | 1     | [[]]             | One empty combination                   |
| n > 0        | r > 0         | ⁿ⁺ʳ⁻¹Cᵣ          | ⁿ⁺ʳ⁻¹Cᵣ | combinations     | Normal case                             |
| **Error case** | **Condition** | **Throws** | | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | r < 0 | invalid-argument error | | | r must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

## UNIQUE PERMUTATION (n!)

| n (set size) | Mathematical | Count | Iterator Returns | Interpretation            |
|--------------|--------------|-------|------------------|---------------------------|
| n = 0        | 0! = 1       | 1     | [[]]             | One empty permutation     |
| n > 0        | n!           | n!    | permutations     | Normal case               |
| **Error case** | **Condition** | **Throws** | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

**Note**: Input elements are treated as distinct *by position*. Equal elements by
`equals()` are still treated as distinct positionally; the iterator may emit
visually identical permutations.

## K-PERMUTATION (ⁿPₖ)

| n (set size) | k (selection) | Mathematical | Count | Iterator Returns | Interpretation                          |
|--------------|---------------|--------------|-------|------------------|-----------------------------------------|
| n = 0        | k = 0         | ⁰P₀ = 1      | 1     | [[]]             | One empty permutation                   |
| n = 0        | k > 0         | ⁰Pₖ = 0      | 0     | []               | Cannot select from empty set            |
| n > 0        | k = 0         | ⁿP₀ = 1      | 1     | [[]]             | One empty permutation                   |
| n > 0        | 0 < k ≤ n     | ⁿPₖ          | ⁿPₖ   | permutations     | Normal case                             |
| n > 0        | k > n         | ⁿPₖ = 0      | 0     | []               | Selection exceeds set size              |
| **Error case** | **Condition** | **Throws** | | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | k < 0 | invalid-argument error | | | k must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

## REPETITIVE PERMUTATION (nʳ)

| n (set size) | r (length) | Mathematical | Count | Iterator Returns | Interpretation                          |
|--------------|------------|--------------|-------|------------------|-----------------------------------------|
| n = 0        | r = 0      | 0⁰ = 1       | 1     | [[]]             | One empty permutation                   |
| n = 0        | r > 0      | 0ʳ = 0       | 0     | []               | Cannot form from empty set              |
| n > 0        | r = 0      | n⁰ = 1       | 1     | [[]]             | One empty permutation                   |
| n > 0        | r > 0      | nʳ           | nʳ    | permutations     | Normal case                             |
| **Error case** | **Condition** | **Throws** | | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | r < 0 | invalid-argument error | | | r must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

## DERANGEMENT (!n)

A derangement is a permutation with no fixed point — no element appears at its
original position. The count is the subfactorial `D_n = !n`
(1, 0, 1, 2, 9, 44, 265, …).

| n (set size) | Mathematical | Count | Iterator Returns | Interpretation                          |
|--------------|--------------|-------|------------------|-----------------------------------------|
| n = 0        | !0 = 1       | 1     | [[]]             | One empty derangement                   |
| n = 1        | !1 = 0       | 0     | []               | No fixed-point-free permutation exists  |
| n ≥ 2        | !n           | !n    | derangements     | Normal case                             |
| **Error case** | **Condition** | **Throws** | | |
| | n < 0 | invalid-argument error | | | n must be ≥ 0 |
| | null input | null-input error | | | Elements list cannot be null |

**Note**: As with unique permutations, elements are treated as distinct *by
position*. Duplicates do not throw; they simply produce visually-identical
derangements.

## MULTISET PERMUTATION

| Map State          | Mathematical          | Count         | Iterator Returns | Interpretation                          |
|--------------------|-----------------------|---------------|------------------|-----------------------------------------|
| Empty map          | 0! = 1                | 1             | [[]]             | One empty permutation                   |
| Non-empty          | n! / (Π fᵢ!)          | multinomial   | permutations     | Normal case                             |
| All frequencies = 0| treated as empty      | 1             | [[]]             | Treated as empty map                    |
| **Error case** | **Condition** | **Throws** | | | |
| | any frequency < 0 | invalid-argument error | | | Frequencies must be ≥ 0 |
| | null input | null-input error | | | Map cannot be null |

**Note**: Frequencies must be non-negative integers; zero means element not available.
Zero-frequency entries are filtered out before counting, so `{A:0, B:2}` is processed
exactly like `{B:2}`.

## MULTISET COMBINATION

| Map State | r (selection) | Mathematical                     | Count | Iterator Returns | Interpretation                          |
|-----------|---------------|----------------------------------|-------|------------------|-----------------------------------------|
| Empty map | r = 0         | 1                                | 1     | [{}]             | One empty multiset                      |
| Empty map | r > 0         | 0                                | 0     | []               | Cannot select from empty multiset       |
| Non-empty | r = 0         | 1                                | 1     | [{}]             | One empty multiset                      |
| Non-empty | r > 0         | multisetCombinationsCount(r, freq) | calculated | combinations     | Normal case                             |
| Non-empty | r > ∑fᵢ       | 0                                | 0     | []               | Selection exceeds available elements    |
| **Error case** | **Condition** | **Throws** | | | |
| | any frequency < 0 | invalid-argument error | | | Frequencies must be ≥ 0 |
| | r < 0 | invalid-argument error | | | r must be ≥ 0 |
| | null input | null-input error | | | Map cannot be null |

## SUBSETS (Power Set)

| n (set size) | Range [from, to] | Mathematical     | Count | Iterator Returns | Interpretation                          |
|--------------|------------------|------------------|-------|------------------|-----------------------------------------|
| n = 0        | [0, 0]           | 2⁰ = 1           | 1     | [[]]             | One empty subset                        |
| n = 0        | [0, m] where m>0 | Σ2⁰ = 1          | 1     | [[]]             | Only empty subset exists                |
| n = 0        | [1, m] where m>0 | 0                | 0     | []               | No non-empty subsets                    |
| n > 0        | [0, 0]           | 1                | 1     | [[]]             | One empty subset                        |
| n > 0        | [0, n]           | 2ⁿ               | 2ⁿ    | all subsets      | Full power set                          |
| n > 0        | [a, b] where 0 ≤ a ≤ b ≤ n | Σ ⁿCᵢ for i=a..b | Σ ⁿCᵢ | subsets in range | Subsets of specific sizes |
| **Error case** | **Condition** | **Throws** | | | |
| | from < 0 | invalid-argument error | | | from must be ≥ 0 |
| | to < from | invalid-argument error | | | to must be ≥ from |
| | from > n | invalid-argument error | | | from cannot exceed n |
| | to > n | invalid-argument error | | | to cannot exceed n |
| | null input | null-input error | | | Elements list cannot be null |

## SIMPLE CARTESIAN PRODUCT

| Condition                             | Count | Iterator Returns | Interpretation                          |
|---------------------------------------|-------|------------------|-----------------------------------------|
| Any dimension is empty (positive multiplicity) | 0     | []               | Empty product                           |
| All dimensions contribute 1 tuple     | 1     | [[]]             | Single empty tuple                      |
| Normal case (no empty dimensions)     | Π sizes | products         | Normal product                          |
| **Error case** | **Condition** | **Throws** | | |
| | null input for any dimension | null-input error | | | Elements list cannot be null |

## CONSTRAINED CARTESIAN PRODUCT

| Condition                             | Count | Iterator Returns | Interpretation                          |
|---------------------------------------|-------|------------------|-----------------------------------------|
| Any dimension has count = 0           | 0     | []               | Empty product (impossible)              |
| All dimensions have count = 1         | 1     | [[]]             | Single empty tuple                      |
| Normal case                           | Π counts | products         | Normal constrained product              |
| **Error case** | **Condition** | **Throws** | | |
| | quantity < 0 (for distinct/multi-select) | invalid-argument error | | | quantity must be ≥ 0 |
| | invalid range (from < 0, to < from, from > n, to > n) | invalid-argument error | | | Range must be valid |
| | null input for any dimension | null-input error | | | Elements list cannot be null |

**Note**: Multiplicity 0 in a dimension (min = max = 0) contributes 1 empty tuple (as if dimension is absent).

## GENERAL RULES

1. Null input always throws a **null-input error** (programmer error). This covers a null
   collection/map, a null element inside a list, a null `ranks` iterable and a null random source.
2. Empty list (∅) is allowed and follows the rules above.
3. Negative counts, ranges, frequencies, or invalid parameters throw **invalid-argument error**.
4. Zero frequency in multiset means element is not available (treated as absent).
5. For zero selection (r=0, k=0, quantity=0): always count = 1, returns single empty result ([[]], [{}]).
6. For positive selection with empty input: always count = 0, empty iterator ([]).
7. **Null elements are not supported.** A list that a builder copies (rule 8) is checked for
   `null` elements *during that copy*, so the check costs no extra pass and the error is raised
   when the builder is created, not later during iteration. This rule does **not** apply to
   user-supplied sequences such as `ranks` (see the `byRanks` rows below).
8. **Builders snapshot their input.** Builders copy the list/map they are given. Changing the
   caller's collection after the builder has been created has no effect on `count()` or on any
   generator the builder returns.
9. **Generators are immutable and re-iterable.** Every call to `iterator()` starts a fresh,
   independent traversal. Every returned list/map is immutable.

## TRAVERSAL / SAMPLING RULES (shared by all builders)

These apply uniformly to every `Builder<T>` (`Permutations`, `Combinations`,
`Subsets`, `Derangements`, `CartesianProduct`, …). Validation is fail-fast at
the builder call unless noted otherwise.

| Method | Condition | Behavior |
|---|---|---|
| `lexOrderMth(m, start)` | `m ≤ 0` | invalid-argument error |
| `lexOrderMth(m, start)` | `start < 0` | invalid-argument error |
| `lexOrderMth(m, start)` | `start ≥ count` **and** `count > 0` | invalid-argument error |
| `lexOrderMth(m, start)` | `count = 0` (e.g. derangement of n=1) and `start ≥ 0` | Accepted; iteration yields nothing (`[]`) |
| `byRanks(ranks)` | `ranks` is null | error (null input) |
| `byRanks(ranks)` | any user-supplied `ranks` sequence | Consumed **lazily**, one rank at a time. It may be infinite or single-use, so it is never scanned up front: the only up-front check is for null. The caller may stop iterating at any point |
| `byRanks(ranks)` | individual rank `< 0` or `≥ count` | invalid-argument error, **raised lazily during iteration** when that rank is reached |
| `byRanks(ranks)` | domain whose only element is the empty list (`count = 1`: n = 0, r/k/width = 0, empty multiset) | Every rank must be `0`; yields one `[]` **per rank supplied** (the rank source is never ignored) |
| `sample(s, random)` | `s ≤ 0` | invalid-argument error |
| `sample(s, random)` | `s > count` (including `count = 0`) | invalid-argument error (sampling is **without replacement**) |
| `choice(s, random)` | `s ≤ 0` | invalid-argument error |
| `choice(s, random)` | `count = 0` | invalid-argument error (nothing can be drawn from an empty domain) |
| `choice(s, random)` | `count > 0` and `s > count` | Accepted (sampling is **with replacement**, duplicates allowed) |
| `choice(s, random)` | `count = 1` and the only element is the empty list | Yields `s` empty results |
| `sample` / `choice` | random source is null | error (null input) |
| `count()` | — | Always returns a non-negative integer of unbounded size; `0` is a valid count (empty domain) |
| `isEmpty()` | — | `true` **if and only if** `count() == 0` (the builder yields no results at all). A builder whose only result is the empty list (`[[]]`) is **not** empty. |

**Randomness**: `choice` and `sample` draw uniformly from the whole range `[0, count)`, however
large `count` is, including counts beyond 64-bit range. For a given seed the sequence is
reproducible.

## RANK / UNRANK HELPERS

Helpers that convert between an arrangement and its rank are called directly by users, so they
validate their own input.

**Where the validation lives (Java reference implementation).** In the user-facing `RankOf` and
`UnrankOf` classes. The number-system classes they delegate to (`PermutadicAlgorithms`,
`CombinadicAlgorithms`, ...) are implementation classes: they assume valid input, so the builders, which
only pass values they have already checked, pay nothing for validation. Checks that are intrinsic to a
conversion (for example a rank that cannot be encoded) remain where the conversion detects them.

| Operation | Condition | Throws |
|---|---|---|
| any `unrank` | rank `< 0` | invalid-argument error |
| any `unrank` | rank `≥` number of arrangements (e.g. `size!`, `ⁿPₖ`, `!n`) | invalid-argument error |
| any `unrank` | size / n / k negative | invalid-argument error |
| any `rank` | array is not a valid arrangement (duplicate value, or value outside `[0, n)`) | invalid-argument error |
| any `rank` / `unrank` | null array / null rank | null-input error |

**Round trip**: for every valid input, `rank(unrank(r)) == r` and `unrank(rank(p)) == p`.

## IMPLEMENTATION STATUS (Java reference implementation)

This section tracks the current Java implementation against the behavioral contract above. The
rules above are unchanged and remain the source of truth. Concrete Java exception classes are
implementation details; the status below records them only where they currently differ from the
required error category.

### Builder validation and generation

The combination (unique, repetitive, multiset), permutation (unique, k, repetitive, multiset),
derangement, subset, constrained Cartesian product and simple Cartesian product builders implement
the documented count, emptiness, snapshot, immutability, re-iterability, traversal and sampling
behaviors in the current source.

The six simple Cartesian product issues previously listed here are resolved:

| Rule | Current implementation | Verification |
|---|---|---|
| `isEmpty()` iff `count() == 0` | `SimpleProductBuilder.isEmpty()` checks `cachedCount.signum() == 0`. | `SimpleProductValidationRulesTest.isEmptyIsTrueExactlyWhenCountIsZero` |
| Reject null elements while copying each dimension | The constructor and `and()` call `snapshot()`, which uses `Util.copyOfNoNulls`. | `SimpleProductValidationRulesTest.nullElementsAreRejectedWhenBuilderIsCreated` |
| Snapshot caller-owned dimensions | Each dimension is copied before being stored; `and()` returns a new builder. | `SimpleProductValidationRulesTest.builderSnapshotsItsInput`; `andDoesNotChangeTheBuilderItWasCalledOn` |
| Immutable tuples from every generator | `lexOrder()` delegates to `SimpleProduct`, and rank-based generation wraps each tuple as an unmodifiable list. | `SimpleProductValidationRulesTest.resultsAreImmutable` |
| Re-iterable generators | `lexOrder()`, `lexOrderMth()`, and `byRanks()` create fresh iterators for each traversal. | `SimpleProductValidationRulesTest.generatorsAreReIterable` |
| Reject `choice()` on an empty domain | `BigIntegerChoice` validation rejects a non-positive domain count. | `SimpleProductValidationRulesTest.choiceFromAnEmptyProductIsRejected` |

### Remaining discrepancies

None. The current Java implementation matches the behavioral contract in `ValidationRules.md` for
all shared validation cases. The contract is language-agnostic about concrete exception types
(see header): signaling *any* error for null input or invalid arguments satisfies the rule.
