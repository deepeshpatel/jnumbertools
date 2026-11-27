[Home](../../README.md)

# Ranking and Unranking

The `Rank` and `Unrank` static entry points are in `io.github.deepeshpatel.jnumbertools.entrypoint`. Ranking returns the zero-based lexicographical position of a combinatorial object. Unranking reconstructs the object at a specified `BigInteger` rank.

## Ranking

### Permutations and derangements

```java
// Unique permutation of {0, 1, 2, 3}
BigInteger rank = Rank.permutation().unique(3, 2, 1, 0);

// 4-permutation selected from {0, ..., 7}
BigInteger kRank = Rank.permutation().k(8, 4, 6, 2, 0);

// Repetitive sequence over two types
BigInteger repetitiveRank = Rank.permutation().repetitive(2, 1, 0, 0, 1);

// Derangement
BigInteger derangementRank = Rank.permutation().derangement(1, 0, 3, 2);
```

Multiset permutations use frequency and type-index arrays:

```java
BigInteger multisetRank = Rank.permutation()
    .multiset(new int[]{2, 1}, new int[]{0, 1, 0});
```

### Combinations

```java
BigInteger uniqueRank = Rank.combination().unique(8, 1, 2, 3, 4);
BigInteger repetitiveRank = Rank.combination().repetitive(3, 0, 1, 1);
BigInteger multisetRank = Rank.combination()
    .multiset(new int[]{2, 2, 1}, new int[]{1, 1, 0});
```

Unique combinations must be supplied in strictly ascending order. Repetitive combinations must be nondecreasing. Multiset combinations are represented by selected-count vectors aligned with the frequency vector.

## Unranking

```java
// Unique permutation of size 3 at rank 5
int[] permutation = Unrank.permutation().unique(BigInteger.valueOf(5), 3);

// k-permutation of 3 elements selected from 8
int[] kPermutation = Unrank.permutation().k(BigInteger.valueOf(5), 8, 3);

// Derangement of size 4
int[] derangement = Unrank.permutation().derangement(BigInteger.valueOf(2), 4);

// Unique combination: choose 3 from 5
int[] combination = Unrank.combination().unique(BigInteger.valueOf(9), 5, 3);
```

The permutation unrank builder also provides `uniqueMinimumSize`, `repetitive` and `multiset`. The combination unrank builder provides `repetitive` and `multiset`.

## Sharing calculation caches

The no-argument entry points create a fresh `Calculator`. Pass a shared calculator when repeated ranking or unranking operations should reuse combinatorial caches.

```java
Calculator calculator = new Calculator();
var ranker = Rank.permutation(calculator);
var unranker = Unrank.permutation(calculator);

BigInteger rank = ranker.unique(2, 0, 1);
int[] decoded = unranker.unique(rank, 3);
```

Ranking and unranking validate their inputs. See [ValidationRules.md](../../ValidationRules.md) for the exact validation contract.
