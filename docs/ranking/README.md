[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Ranking and Unranking

JNumberTools provides ranking and unranking APIs for permutations, combinations, and derangements. Ranking calculates the zero-based lexicographical position of an arrangement. Unranking performs the inverse operation: it returns the arrangement at a specified rank. The APIs use `BigInteger` for ranks, so they can address combinatorial spaces larger than the range of primitive integer types.

The API is organised by mathematical structure. Choose `permutation()` or `combination()` after `rankOf()` or `unrankOf()`, then choose the required variant.

## Ranking

### 1. Ranking Unique permutation

Ranks a full permutation of the set `{0, 1, ..., n-1}`. The input must contain every index exactly once.

```java
// Rank of [3, 2, 1, 0] among the 4! permutations
BigInteger rank = JNumberTools.rankOf().permutation().unique(3, 2, 1, 0);
```

### 2. Ranking k-permutation

Ranks an ordered selection of `k` distinct elements from `{0, 1, ..., n-1}`. The input length determines `k`.

```java
// Rank of [4, 6, 2, 0] among the 4-permutations of 8 elements
BigInteger rank = JNumberTools.rankOf().permutation().k(8, 4, 6, 2, 0);
```

### 3. Ranking Repetitive permutation

Ranks a sequence of any length whose values are selected from `{0, 1, ..., n-1}`. Values may repeat. The first argument is the number of available element types.

```java
// [1, 0, 0, 1] has rank 9 among sequences of length 4 over {0, 1}
BigInteger rank = JNumberTools.rankOf().permutation().repetitive(2, 1, 0, 0, 1);
```

### 4. Ranking Multiset permutation

Ranks a permutation with prescribed multiplicities. `frequencies[i]` specifies how many times type index `i` must occur in the permutation. The permutation contains type indices, not the original element values.

```java
// Two occurrences of type 0 and one occurrence of type 1
BigInteger rank = JNumberTools.rankOf().permutation()
    .multiset(new int[]{2, 1}, new int[]{0, 1, 0});
```

### 5. Ranking Derangement

Ranks a derangement: a full permutation with no fixed points (`permutation[i] != i` for every index).

```java
// Rank of derangement [1, 0, 3, 2] of size 4
BigInteger rank = JNumberTools.rankOf().permutation().derangement(1, 0, 3, 2);
```

### 6. Ranking Unique combination

Ranks a combination of `r` distinct elements selected from `{0, 1, ..., n-1}`. The input must be in strictly ascending order.

```java
// Rank of [1, 2, 3, 4] among the 4-combinations of 8 elements
BigInteger rank = JNumberTools.rankOf().combination().unique(8, 1, 2, 3, 4);
```

### 7. Ranking Repetitive combination

Ranks a nondecreasing combination in which each of the `n` element types may be selected more than once.

```java
// Rank of [0, 1, 1] among combinations with repetition from 3 types
BigInteger rank = JNumberTools.rankOf().combination().repetitive(3, 0, 1, 1);
```

### 8. Ranking Multiset combination

Ranks a selection from a multiset with limited availability. Both arrays are count vectors aligned by type: `frequencies[i]` is the available count of type `i`, and `combination[i]` is the selected count.

```java
// Select one item each of types 0 and 1 from frequencies {2, 2, 1}
BigInteger rank = JNumberTools.rankOf().combination()
    .multiset(new int[]{2, 2, 1}, new int[]{1, 1, 0});
```

## Unranking

Unranking returns the arrangement at a zero-based lexicographical rank. For each operation, the rank must be within the range of arrangements for the supplied parameters.

### 1. Unranking Unique permutation

Provide the rank and permutation size. `uniqueMinimumSize(rank)` is also available when the smallest permutation size capable of containing the rank should be determined automatically.

```java
int[] permutation = JNumberTools.unrankOf().permutation()
    .unique(BigInteger.valueOf(5), 3);

int[] minimumSizePermutation = JNumberTools.unrankOf().permutation()
    .uniqueMinimumSize(BigInteger.valueOf(5));
```

### 2. Unranking k-permutation

Provide the rank, source-set size `n`, and selection size `k`.

```java
int[] kPermutation = JNumberTools.unrankOf().permutation()
    .k(BigInteger.valueOf(5), 8, 3);
```

### 3. Unranking Repetitive permutation

Provide the rank, number of available element types `n`, and output length `width`.

```java
int[] repetitivePermutation = JNumberTools.unrankOf().permutation()
    .repetitive(BigInteger.valueOf(5), 2, 4);
```

### 4. Unranking Multiset permutation

Provide the rank and frequency vector. The result contains type indices, with each type occurring exactly as many times as specified by its frequency.

```java
int[] multisetPermutation = JNumberTools.unrankOf().permutation()
    .multiset(BigInteger.valueOf(2), 2, 1);
```

### 5. Unranking Derangement

Provide the rank and the size `n` of the derangement. A `long` overload is also available.

```java
int[] derangement = JNumberTools.unrankOf().permutation()
    .derangement(BigInteger.valueOf(2), 4);
```

### 6. Unranking Unique combination

Provide the rank, source-set size `n`, and combination size `r`.

```java
int[] combination = JNumberTools.unrankOf().combination()
    .unique(BigInteger.valueOf(9), 5, 3);
```

### 7. Unranking Repetitive combination

Provide the rank, number of available element types `n`, and combination size `r`.

```java
int[] repetitiveCombination = JNumberTools.unrankOf().combination()
    .repetitive(BigInteger.valueOf(4), 3, 3);
```

### 8. Unranking Multiset combination

Provide the rank, available frequency vector, and number of items to select `r`. The result is a count vector aligned with the frequency vector.

```java
int[] multisetCombination = JNumberTools.unrankOf().combination()
    .multiset(BigInteger.ZERO, new int[]{2, 2, 1}, 2);
```

## Ranking and unranking together

For a valid arrangement, ranking and unranking are inverse operations: unranking the rank of an arrangement returns that arrangement, and ranking the result of unranking a valid rank returns the original rank.

```java
int[] permutation = {2, 0, 1};
BigInteger rank = JNumberTools.rankOf().permutation().unique(permutation);
int[] decoded = JNumberTools.unrankOf().permutation().unique(rank, permutation.length);
```

[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)
