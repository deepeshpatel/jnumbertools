[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Derangement Generators](../derangements/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Derangement Generators

JNumberTools provides derangement (fixed-point-free permutation) generators for
lists and integer ranges. All m<sup>th</sup> generators are BigInteger
compatible, enabling rapid access to very large ranks.

**Currently Available Algorithms**

1. [Derangement in lex order](#1-derangement-in-lex-order)
2. [Every m<sup>th</sup> derangement in lex order](#2-every-m-th-derangement-in-lex-order)
3. [Derangement random choice](#3-derangement-random-choice)
4. [Derangement random sample](#4-derangement-random-sample)
5. [Derangement of ranks](#5-derangement-of-ranks)

---

### 1. Derangement in lex order
Generates all derangements of the given elements in lexicographical order.
A derangement is a permutation where no element appears in its original position.

```java
// All derangements of [0,1,2,3] in lex order
JNumberTools.derangements()
    .of(4)
    .lexOrder()
    .stream().forEach(System.out::println);

// All derangements of ["A","B","C","D"] in lex order
JNumberTools.derangements()
    .of("A", "B", "C", "D")
    .lexOrder()
    .stream().forEach(System.out::println);
```

### 2. Every m-th derangement in lex order
Generates every m<sup>th</sup> derangement, starting from a given rank, without
iterating through all prior derangements.

```java
int m = 3;
int start = 0;

// Every 3rd derangement of [0,1,2,3,4] starting from rank 0
JNumberTools.derangements()
    .of(5)
    .lexOrderMth(m, start)
    .stream().forEach(System.out::println);

// Every 3rd derangement of ["A","B","C","D","E"] starting from rank 0
JNumberTools.derangements()
    .of("A", "B", "C", "D", "E")
    .lexOrderMth(m, start)
    .stream().forEach(System.out::println);
```

### 3. Derangement random choice
Generates random derangements with replacement (duplicates allowed).

```java
// Generate any 5 derangements of [0,1,2,3] with duplicates allowed
JNumberTools.derangements()
    .of(4)
    .choice(5)
    .stream().forEach(System.out::println);
```

### 4. Derangement random sample
Generates random derangements without replacement (unique output).

```java
// Generate 5 unique derangements of ["A","B","C","D","E"]
JNumberTools.derangements()
    .of("A", "B", "C", "D", "E")
    .sample(5)
    .stream().forEach(System.out::println);
```

### 5. Derangement of ranks
Generates derangements at indices specified by a custom rank sequence.

```java
var ranks = List.of(
        BigInteger.ZERO,
        BigInteger.valueOf(2),
        BigInteger.valueOf(10),
        new BigInteger("1000000000000000000"));

// Generate derangements of size 8 at specific ranks
JNumberTools.derangements()
    .of(8)
    .byRanks(ranks)
    .stream().forEach(System.out::println);
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

