[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Set/Subset Generators

JNumberTools provides the following 7 generators for sets and subsets. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of subsets at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [All subsets in lex order](#1-all-subsets-in-lex-order)
2. [Every m<sup>th</sup> subsets in lex order](#2-every-m-th-subsets-in-lex-order)
3. [All subsets of given size range in lex order](#3-all-subsets-of-given-size-range-in-lex-order)
4. [Every m<sup>th</sup> subsets in a given size range in lex order](#4-every-m-th-subsets-in-range-in-lex-order)
5. [Random subset choice](#5-random-subset-choice)
6. [Random subset sample](#6-random-subset-sample)
7. [Subsets of ranks](#7-subsets-of-ranks)

---

### 1. All subsets in lex order
Generates all subsets of a given set in lexicographical order, including the empty set (φ). For example, for the set ["A", "B", "C"], it generates: [], [A], [B], [C], [A, B], [A, C], [B, C], [A, B, C].

```java
// All subsets of ["Apple", "Banana", "Guava"] in lex order
JNumberTools.subsets()
    .of("Apple", "Banana", "Guava")
    .all()
    .lexOrder()
    .stream().toList();
```

### 2. Every m-th subsets in lex order
Generates every m<sup>th</sup> subset of a given set in lexicographical order, starting from a given index. This API directly generates the desired subset without iterating through preceding subsets, making it highly efficient.

```java
// Every 1 billionth subset of numbers in range [0,40) starting from 0th index
JNumberTools.subsets()
    .of(40)
    .all()
    .lexOrderMth(1_000_000_000, 0)
    .stream().toList();
```

### 3. All subsets of given size range in lex order
Generates all subsets within a specified size range in lexicographical order. For example, for the set ["A", "B", "C"] with size range [2,3], it generates: [A, B], [A, C], [B, C], [A, B, C].

```java
// All subsets of ["Apple", "Banana", "Guava"] in size range [2,3] in lex order
JNumberTools.subsets()
    .of("Apple", "Banana", "Guava")
    .inRange(2, 3)
    .lexOrder()
    .stream().toList();
```

### 4. Every m-th subsets in range in lex order
Generates every m<sup>th</sup> subset within a specified size range in lexicographical order, starting from a given index. This API directly generates the desired subset without computing preceding subsets.

```java
// Every 1 billionth subset of numbers in range [0,40) with size range [10,39] starting from 0th index
JNumberTools.subsets()
    .of(40)
    .inRange(10, 39)
    .lexOrderMth(1_000_000_000, 0)
    .stream().toList();
```

### 5. Random subset choice
Generates n random subsets with duplicates allowed.

```java
// Generate 5 random subsets of numbers in range [0,10) with duplicates
JNumberTools.subsets()
    .of(10)
    .all()
    .choice(5)
    .stream().forEach(System.out::println);

// Generate 5 random subsets of ["Apple", "Banana", "Guava"] with duplicates
JNumberTools.subsets()
    .of("Apple", "Banana", "Guava")
    .all()
    .choice(5)
    .stream().forEach(System.out::println);
```

### 6. Random subset sample
Generates n random subsets without duplicates.

```java
// Generate 5 random subsets of numbers in range [0,10) without duplicates
JNumberTools.subsets()
    .of(10)
    .all()
    .sample(5)
    .stream().forEach(System.out::println);

// Generate 5 random subsets of ["Apple", "Banana", "Guava"] without duplicates
JNumberTools.subsets()
    .of("Apple", "Banana", "Guava")
    .all()
    .sample(5)
    .stream().forEach(System.out::println);
```

### 7. Subsets of ranks
Generates subsets at indices specified by a custom sequence.

```java
// Generates subsets of numbers in range [0,100) at specified indices
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.subsets()
    .of(100)
    .all()
    .ofRanks(iterable)
    .stream().toList();
```

[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)