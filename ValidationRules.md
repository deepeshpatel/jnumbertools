# JNumberTools - Validation Rules

All operations throw **NullPointerException** on null input (programmer error).  
Empty collections/maps are allowed and follow the rules below.  
Negative counts/ranges/frequencies throw **IllegalArgumentException**.

## UNIQUE COMBINATION (ⁿCᵣ)

| n (set size) | r (selection) | Mathematical | Count | Iterator Returns          | Interpretation                          |
|--------------|---------------|--------------|-------|---------------------------|-----------------------------------------|
| n = 0        | r = 0         | ⁰C₀ = 1      | 1     | [[]]                      | One empty combination                   |
| n = 0        | r > 0         | ⁰Cᵣ = 0      | 0     | []                        | Cannot select from empty set            |
| n > 0        | r = 0         | ⁿC₀ = 1      | 1     | [[]]                      | One empty combination                   |
| n > 0        | 0 < r ≤ n     | ⁿCᵣ          | ⁿCᵣ   | combinations              | Normal case                             |
| n > 0        | r > n         | ⁿCᵣ = 0      | 0     | []                        | Selection exceeds set size              |
| **Exception** | **Condition** | **Throws** | | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | r < 0 | IllegalArgumentException | | | r must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

## REPETITIVE COMBINATION (ⁿ⁺ʳ⁻¹Cᵣ)

| n (set size) | r (selection) | Mathematical     | Count | Iterator Returns | Interpretation                          |
|--------------|---------------|------------------|-------|------------------|-----------------------------------------|
| n = 0        | r = 0         | (by convention)  | 1     | [[]]             | One empty combination                   |
| n = 0        | r > 0         | 0                | 0     | []               | Cannot select from empty set            |
| n > 0        | r = 0         | 1                | 1     | [[]]             | One empty combination                   |
| n > 0        | r > 0         | ⁿ⁺ʳ⁻¹Cᵣ          | ⁿ⁺ʳ⁻¹Cᵣ | combinations     | Normal case                             |
| **Exception** | **Condition** | **Throws** | | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | r < 0 | IllegalArgumentException | | | r must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

## UNIQUE PERMUTATION (n!)

| n (set size) | Mathematical | Count | Iterator Returns | Interpretation            |
|--------------|--------------|-------|------------------|---------------------------|
| n = 0        | 0! = 1       | 1     | [[]]             | One empty permutation     |
| n > 0        | n!           | n!    | permutations     | Normal case               |
| **Exception** | **Condition** | **Throws** | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

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
| **Exception** | **Condition** | **Throws** | | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | k < 0 | IllegalArgumentException | | | k must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

## REPETITIVE PERMUTATION (nʳ)

| n (set size) | r (length) | Mathematical | Count | Iterator Returns | Interpretation                          |
|--------------|------------|--------------|-------|------------------|-----------------------------------------|
| n = 0        | r = 0      | 0⁰ = 1       | 1     | [[]]             | One empty permutation                   |
| n = 0        | r > 0      | 0ʳ = 0       | 0     | []               | Cannot form from empty set              |
| n > 0        | r = 0      | n⁰ = 1       | 1     | [[]]             | One empty permutation                   |
| n > 0        | r > 0      | nʳ           | nʳ    | permutations     | Normal case                             |
| **Exception** | **Condition** | **Throws** | | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | r < 0 | IllegalArgumentException | | | r must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

## DERANGEMENT (!n)

A derangement is a permutation with no fixed point — no element appears at its
original position. The count is the subfactorial `D_n = !n`
(1, 0, 1, 2, 9, 44, 265, …).

| n (set size) | Mathematical | Count | Iterator Returns | Interpretation                          |
|--------------|--------------|-------|------------------|-----------------------------------------|
| n = 0        | !0 = 1       | 1     | [[]]             | One empty derangement                   |
| n = 1        | !1 = 0       | 0     | []               | No fixed-point-free permutation exists  |
| n ≥ 2        | !n           | !n    | derangements     | Normal case                             |
| **Exception** | **Condition** | **Throws** | | |
| | n < 0 | IllegalArgumentException | | | n must be ≥ 0 |
| | null input | NullPointerException | | | Elements list cannot be null |

**Note**: As with unique permutations, elements are treated as distinct *by
position*. Duplicates do not throw; they simply produce visually-identical
derangements.

## MULTISET PERMUTATION

| Map State          | Mathematical          | Count         | Iterator Returns | Interpretation                          |
|--------------------|-----------------------|---------------|------------------|-----------------------------------------|
| Empty map          | 0! = 1                | 1             | [[]]             | One empty permutation                   |
| Non-empty          | n! / (Π fᵢ!)          | multinomial   | permutations     | Normal case                             |
| All frequencies = 0| treated as empty      | 1             | [[]]             | Treated as empty map                    |
| **Exception** | **Condition** | **Throws** | | | |
| | any frequency < 0 | IllegalArgumentException | | | Frequencies must be ≥ 0 |
| | null input | NullPointerException | | | Map cannot be null |

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
| **Exception** | **Condition** | **Throws** | | | |
| | any frequency < 0 | IllegalArgumentException | | | Frequencies must be ≥ 0 |
| | r < 0 | IllegalArgumentException | | | r must be ≥ 0 |
| | null input | NullPointerException | | | Map cannot be null |

## SUBSETS (Power Set)

| n (set size) | Range [from, to] | Mathematical     | Count | Iterator Returns | Interpretation                          |
|--------------|------------------|------------------|-------|------------------|-----------------------------------------|
| n = 0        | [0, 0]           | 2⁰ = 1           | 1     | [[]]             | One empty subset                        |
| n = 0        | [0, m] where m>0 | Σ2⁰ = 1          | 1     | [[]]             | Only empty subset exists                |
| n = 0        | [1, m] where m>0 | 0                | 0     | []               | No non-empty subsets                    |
| n > 0        | [0, 0]           | 1                | 1     | [[]]             | One empty subset                        |
| n > 0        | [0, n]           | 2ⁿ               | 2ⁿ    | all subsets      | Full power set                          |
| n > 0        | [a, b] where 0 ≤ a ≤ b ≤ n | Σ ⁿCᵢ for i=a..b | Σ ⁿCᵢ | subsets in range | Subsets of specific sizes |
| **Exception** | **Condition** | **Throws** | | | |
| | from < 0 | IllegalArgumentException | | | from must be ≥ 0 |
| | to < from | IllegalArgumentException | | | to must be ≥ from |
| | from > n | IllegalArgumentException | | | from cannot exceed n |
| | to > n | IllegalArgumentException | | | to cannot exceed n |
| | null input | NullPointerException | | | Elements list cannot be null |

## SIMPLE CARTESIAN PRODUCT

| Condition                             | Count | Iterator Returns | Interpretation                          |
|---------------------------------------|-------|------------------|-----------------------------------------|
| Any dimension is empty (positive multiplicity) | 0     | []               | Empty product                           |
| All dimensions contribute 1 tuple     | 1     | [[]]             | Single empty tuple                      |
| Normal case (no empty dimensions)     | Π sizes | products         | Normal product                          |
| **Exception** | **Condition** | **Throws** | | |
| | null input for any dimension | NullPointerException | | | Elements list cannot be null |

## CONSTRAINED CARTESIAN PRODUCT

| Condition                             | Count | Iterator Returns | Interpretation                          |
|---------------------------------------|-------|------------------|-----------------------------------------|
| Any dimension has count = 0           | 0     | []               | Empty product (impossible)              |
| All dimensions have count = 1         | 1     | [[]]             | Single empty tuple                      |
| Normal case                           | Π counts | products         | Normal constrained product              |
| **Exception** | **Condition** | **Throws** | | |
| | quantity < 0 (for distinct/multi-select) | IllegalArgumentException | | | quantity must be ≥ 0 |
| | invalid range (from < 0, to < from, from > n, to > n) | IllegalArgumentException | | | Range must be valid |
| | null input for any dimension | NullPointerException | | | Elements list cannot be null |

**Note**: Multiplicity 0 in a dimension (min = max = 0) contributes 1 empty tuple (as if dimension is absent).

## GENERAL RULES

1. Null input (collections, maps) always throws **NullPointerException** (programmer error).
2. Empty list (∅) is allowed and follows the rules above.
3. Negative counts, ranges, frequencies, or invalid parameters throw **IllegalArgumentException**.
4. Zero frequency in multiset means element is not available (treated as absent).
5. For zero selection (r=0, k=0, quantity=0): always count = 1, returns single empty result ([[]], [{}]).
6. For positive selection with empty input: always count = 0, empty iterator ([]).

## TRAVERSAL / SAMPLING RULES (shared by all builders)

These apply uniformly to every `Builder<T>` (`Permutations`, `Combinations`,
`Subsets`, `Derangements`, `CartesianProduct`, …). Validation is fail-fast at
the builder call unless noted otherwise.

| Method | Condition | Behavior |
|---|---|---|
| `lexOrderMth(m, start)` | `m ≤ 0` | IllegalArgumentException |
| `lexOrderMth(m, start)` | `start < 0` | IllegalArgumentException |
| `lexOrderMth(m, start)` | `start ≥ count` **and** `count > 0` | IllegalArgumentException |
| `lexOrderMth(m, start)` | `count = 0` (e.g. derangement of n=1) and `start = 0` | Accepted; iteration yields nothing (`[]`) |
| `byRanks(ranks)` | `ranks == null` | IllegalArgumentException |
| `byRanks(ranks)` | individual rank `< 0` or `≥ count` | IllegalArgumentException, **thrown lazily during iteration** |
| `sample(s, random)` | `s ≤ 0` | IllegalArgumentException |
| `sample(s, random)` | `s > count` | IllegalArgumentException (sampling is **without replacement**) |
| `choice(s, random)` | `s ≤ 0` | IllegalArgumentException |
| `choice(s, random)` | `s > count` | Accepted (sampling is **with replacement**, duplicates allowed) |
| `random == null` for `sample` / `choice` | — | IllegalArgumentException |
| `count()` | — | Always returns a non-negative `BigInteger`; `0` is a valid count (empty domain) |
