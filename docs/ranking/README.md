[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Ranking Algorithms

JNumberTools provides ranking APIs for permutations, combinations, and derangements. These APIs calculate the lexicographical rank of a given arrangement, supporting `BigInteger` for large indices and enabling efficient ranking even in massive combinatorial spaces.

**Currently Available Algorithms**

1. [Ranking of Unique Permutation](#1-ranking-of-unique-permutation)  
2. [Ranking of k-Permutation](#2-ranking-of-k-permutation)  
3. [Ranking of Repetitive Permutation](#3-ranking-of-repetitive-permutation)  
4. [Ranking of Unique Combination](#4-ranking-of-unique-combination)  
5. [Ranking of Derangement](#5-ranking-of-derangement)  
6. [Ranking of Repetitive Combination](#6-ranking-of-repetitive-combination) *(in development)*  
7. [Ranking of Multiset Permutation](#7-ranking-of-multiset-permutation) *(in development)*  
8. [Ranking of Multiset Combination](#8-ranking-of-multiset-combination) *(in development)*  

---

### 1. Ranking of Unique Permutation
Calculates the rank of a unique permutation starting from 0th. For example, there are a total of 4!=24 permutations of [0,1,2,3], where the first permutation [0,1,2,3] has rank 0 and the last permutation [3,2,1,0] has the rank of 23.

```java
BigInteger rank = JNumberTools.rankOf().uniquePermutation(3, 2, 1, 0);
```

### 2. Ranking of k-Permutation
Calculates the rank of a k-permutation. For example, if we select 4 elements out of 8 [0,1,2,3,4,5,6,7], there are a total of <sup>8</sup>P<sub>4</sub> permutations, out of which the 1000<sup>th</sup> permutation is [4,6,2,0].

```java
// Will return 1000
BigInteger rank = JNumberTools.rankOf().kPermutation(8, 4, 6, 2, 0);
```

### 3. Ranking of Repetitive Permutation
Calculates the rank of a repetitive permutation.

```java
// 1,0,0,1 is the 9th repeated permutation of two digits 0 and 1
int elementCount = 2;
BigInteger result = JNumberTools.rankOf()
    .repeatedPermutation(elementCount, new Integer[]{1, 0, 0, 1});
```

### 4. Ranking of Unique Combination
Calculates the rank of a given combination. For example, if we generate combinations of 4 elements out of 8 in lex order, the 35th combination will be [1,2,3,4]. Given the input 8 and [1,2,3,4], the API returns 35, the rank of the combination.

```java
BigInteger rank = JNumberTools.rankOf().uniqueCombination(8, 1, 2, 3, 4);
```

### 5. Ranking of Derangement
Calculates the lexicographical rank of a derangement (a permutation with no fixed points). The input must be a full permutation of `{0, 1, ..., n-1}` with no index `i` satisfying `p[i] == i`.

```java
// Derangement [1, 0, 3, 2] of size 4
BigInteger rank = JNumberTools.rankOf().derangement(1, 0, 3, 2);
```

### 6. Ranking of Repetitive Combination
This feature is currently in development and will be available in a future release.

### 7. Ranking of Multiset Permutation
This feature is currently in development and will be available in a future release.

### 8. Ranking of Multiset Combination
This feature is currently in development and will be available in a future release.

[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)