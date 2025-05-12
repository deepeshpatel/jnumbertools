[Home](../../README.md)
</br>[Permutation Generators](../permutations/README.md)
</br>[Combination Generators](../combinations/README.md)
</br>[Set/Subset Generators](../sets/README.md)
</br>[Cartesian Product Generators](../products/README.md)
</br>[Math Functions](../calculator/README.md)
</br>[Ranking Algorithms](../ranking/README.md)
</br>[Number System Algorithms](../numbersystem/README.md)

# Permutation Generators

JNumberTools provides the following 23 permutation generation APIs. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of permutations at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [Unique Permutations: 6 different types of permutations](#1-unique-permutation-generators)
   1. [Unique permutation in lex order](#11-unique-permutation-in-lex-order)
   2. [Every m-th unique permutation in lex order](#12-every-m-th-unique-permutation)
   3. [Unique permutation with minimum change](#13-unique-permutation-with-minimum-change)
   4. [Unique permutation random choice](#14-unique-permutation-random-choice)
   5. [Unique permutation random sample](#15-unique-permutation-random-sample)
   6. [Unique permutation of ranks](#16-unique-permutation-of-ranks)

2. [Multiset Permutations: 5 types of multiset permutations](#2-multiset-permutation-generators)
   1. [Multiset permutation in lex order](#21-multiset-permutation-in-lex-order)
   2. [Every m-th multiset permutation](#22-every-m-th-multiset-permutation)
   3. [Multiset permutation random choice](#23-multiset-permutation-random-choice)
   4. [Multiset permutation random sample](#24-multiset-permutation-random-sample)
   5. [Multiset permutation of ranks](#25-multiset-permutation-of-ranks)

3. [k-Permutations: 7 different variations of k-permutations](#3-k-permutation-generators)
   1. [k-Permutation in lex order](#31-k-permutation-in-lex-order)
   2. [Every m-th k-permutation in lex order](#32-every-m-th-k-permutation)
   3. [k-Permutation in combination order](#33-k-permutation-in-combination-order)
   4. [Every m-th k-permutation in combination order](#34-every-m-th-k-permutation-in-combination-order)
   5. [k-Permutation random choice](#35-k-permutation-random-choice)
   6. [k-Permutation random sample](#36-k-permutation-random-sample)
   7. [k-Permutation of ranks](#37-k-permutation-of-ranks)

4. [Repetitive Permutations: 5 different variations of repetitive permutations](#4-repetitive-permutation-generators)
   1. [Repetitive permutation in lex order](#41-repetitive-permutation)
   2. [Every m-th repetitive permutation](#42-every-m-th-repetitive-permutation)
   3. [Repetitive permutation random choice](#43-repetitive-permutation-random-choice)
   4. [Repetitive permutation random sample](#44-repetitive-permutation-random-sample)
   5. [Repetitive permutation of ranks](#45-repetitive-permutation-of-ranks)

---

### 1. Unique Permutation Generators

#### 1.1 Unique permutation in lex order
Used to generate all n! unique permutations of n elements in lexicographical order.

```java
// Generates all permutations of integers in range [0,3)
JNumberTools.permutations()
    .unique(3)
    .lexOrder()
    .stream().toList();

// Generates all permutations of "Red", "Green", and "Blue" in lex order
JNumberTools.permutations()
    .unique("Red", "Green", "Blue")
    .lexOrder()
    .stream().toList();
```

#### 1.2 Every m-th unique permutation
Generates every m<sup>th</sup> permutation in lexicographical order of indices of input values, starting from a given start index. This is important because, for example, generating the next 100 trillionth permutation of 100 items would take months if computed sequentially due to the astronomical total number of permutations (100! ≈ 9.3326 × 10<sup>157</sup>).

```java
int m = 2;
int start = 5;

// Generates 5th, 7th, 9th... permutations of [0,5)
JNumberTools.permutations()
    .unique(5)
    .lexOrderMth(m, start)
    .stream().toList();

// Generates 5th, 7th, 9th... permutations of elements in lex order
JNumberTools.permutations()
    .unique("A", "B", "C", "D", "E")
    .lexOrderMth(m, start)
    .stream().toList();
```

#### 1.3 Unique permutation with minimum change
Generates all unique permutations such that each permutation is a single swap away from the previous permutation.

```java
// Generates all permutations of 5 digits [0-5) with minimum change
JNumberTools.permutations()
    .unique(5)
    .singleSwap()
    .stream().toList();

// Generates all permutations of input with minimum change
JNumberTools.permutations()
    .unique("A", "B", "C", "D", "E")
    .singleSwap()
    .stream().toList();
```

#### 1.4 Unique permutation random choice
Generates random unique permutations with duplicates allowed.

```java
// Generates exactly 10 permutations of 5 digits [0-5) which may contain duplicates
JNumberTools.permutations()
    .unique(5)
    .choice(10)
    .stream().toList();

// Generates exactly 10 permutations of input which may contain duplicates
JNumberTools.permutations()
    .unique("A", "B", "C", "D", "E")
    .choice(10)
    .stream().toList();
```

#### 1.5 Unique permutation random sample
Generates random unique permutations without duplicates.

```java
// Generates exactly 10 permutations of 5 digits [0-5) without duplicates
JNumberTools.permutations()
    .unique(5)
    .sample(10)
    .stream().toList();

// Generates exactly 10 permutations of input without duplicates
JNumberTools.permutations()
    .unique("A", "B", "C", "D", "E")
    .sample(10)
    .stream().toList();
```

#### 1.6 Unique permutation of ranks
Generates all permutations at indices specified by a custom sequence.

```java
// Generates all permutations of [0,100) at indices specified by Iterable<Number>
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.permutations()
    .unique(100)
    .ofRanks(iterable)
    .stream().toList();
```

### 2. Multiset Permutation Generators

#### 2.1 Multiset permutation in lex order
Permutations where every item has an associated frequency that denotes how many times an item can be repeated in a permutation. For example, permutations of 3 apples and 2 oranges.

```java
var elements = new LinkedHashMap<>(Map.of("Red", 2, "Green", 1, "Blue", 3));

// Permutation of 2 Red, 1 Green, and 3 Blue colors in lex order
JNumberTools.permutations()
    .multiset(elements)
    .lexOrder()
    .stream().toList();
```

#### 2.2 Every m-th multiset permutation
Generates every m<sup>th</sup> multiset permutation from a given start index. This API directly generates the desired permutation without searching a sorted list, making it very efficient. There are a total of (∑ ai × si)! / Π(si!) such permutations.

```java
// Every 3rd permutation of 2 Apple, 1 Banana, and 3 Guava, starting from 5th (i.e., 5th, 8th, 11th...)
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1, "Guava", 3));
JNumberTools.permutations()
    .multiset(elements)
    .lexOrderMth(3, 5)
    .stream()
    .toList();
```

#### 2.3 Multiset permutation random choice
Generates random permutations of a multiset with duplicates allowed.

```java
// Generates exactly 10 permutations of multiset which may contain duplicates
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1, "Guava", 3));
JNumberTools.permutations()
    .multiset(elements)
    .choice(10)
    .stream().toList();
```

#### 2.4 Multiset permutation random sample
Generates random permutations of a multiset without duplicates.

```java
// Generates exactly 10 permutations of multiset without duplicates
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1, "Guava", 3));
JNumberTools.permutations()
    .multiset(elements)
    .sample(10)
    .stream().toList();
```

#### 2.5 Multiset permutation of ranks
Generates all permutations at indices specified by a custom sequence.

```java
// Generates all permutations of a multiset at indices specified by Iterable<Number>
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
var elements = new LinkedHashMap<>(Map.of("Apple", 50, "Banana", 200, "Guava", 50));
JNumberTools.permutations()
    .multiset(elements)
    .ofRanks(iterable)
    .stream().forEach(System.out::println);
```

### 3. k-Permutation Generators

#### 3.1 k-Permutation in lex order
Generates all unique permutations of size k where 0 ≤ k ≤ n and n is the number of input elements, in lexicographical order. In number theory, this is also known as k-permutation. There are a total of <sup>n</sup>P<sub>k</sub> such permutations possible.

```java
// Generates all k-permutations of size 2 out of 5 numbers in range [0,5)
JNumberTools.permutations()
    .nPk(5, 2)
    .lexOrder()
    .stream().toList();

// Generates all permutations of 2 elements out of a list of elements
JNumberTools.permutations()
    .nPk(2, "A", "B", "C", "D", "E")
    .lexOrder()
    .stream().toList();
```

#### 3.2 Every m-th k-permutation
Generates every m<sup>th</sup> k-permutation in lexicographical order without computing preceding permutations. This is important because the total number of permutations can grow astronomically large. For instance, the number of permutations of 100 elements selected 50 at a time is <sup>100</sup>P<sub>50</sub> ≈ 3.068518756 × 10<sup>93</sup>, which is beyond practical limits for sequential generation.

To achieve this, a concept called Permutational Number System (Permutadic, a generalization of factoradic) and Deep-code (a generalization of Lehmer code) is used. Details can be found in the research paper: [Generating the nth Lexicographical Element of a Mathematical k-Permutation using Permutational Number System](https://papers.ssrn.com/sol3/papers.cfm?abstract_id=4174035).

```java
// Generates every 3rd permutation starting from 0th of size 3 out of 10 numbers in range [0,10) (i.e., 0th, 3rd, 6th, 9th...)
JNumberTools.permutations()
    .nPk(10, 5)
    .lexOrderMth(3, 0)
    .stream().toList();

// Generates every 3rd permutation starting from 0th of size 3 out of a list of n elements
JNumberTools.permutations()
    .nPk(2, "A", "B", "C", "D", "E")
    .lexOrder()
    .stream().toList();
```

#### 3.3 k-Permutation in combination order
Generates all k-permutations in the lexicographical order of combinations. For example, [C, A] comes before [B, C] because combination-wise [C, A] = [A, C]. Note that the API does not sort the output to achieve this, but generates the permutation in said order, making it very efficient.

```java
// Generates all k-permutations for n=5 and k=2 in combination-first order
JNumberTools.permutations()
    .nPk(5, 2)
    .combinationOrder()
    .stream().toList();

// Generates all k-permutations for a list of elements
JNumberTools.permutations()
    .nPk(2, "A", "B", "C", "D", "E")
    .combinationOrder()
    .stream().toList();
```

#### 3.4 Every m-th k-permutation in combination order
Generates every m<sup>th</sup> k-permutation in the lexicographical order of combinations. This API does not sort or search to achieve this but generates the desired permutation on the fly, making it very efficient.

```java
// Generates every 3rd k-permutation for n=5 and k=2 in combination-first order, starting from 0th (i.e., 0th, 3rd, 6th...)
JNumberTools.permutations()
    .nPk(5, 2)
    .combinationOrderMth(3, 0)
    .stream().toList();

// Generates every 3rd k-permutation for a list of elements
JNumberTools.permutations()
    .nPk(2, "A", "B", "C", "D", "E")
    .combinationOrderMth(3, 0)
    .stream().toList();
```

#### 3.5 k-Permutation random choice
Generates random k-permutations with duplicates allowed.

```java
// Generates 20 k-permutations for n=20 and k=5, which may contain duplicates
JNumberTools.permutations()
    .nPk(10, 5)
    .choice(20)
    .stream().forEach(System.out::println);
```

#### 3.6 k-Permutation random sample
Generates random k-permutations without duplicates.

```java
// Generates 20 k-permutations for n=20 and k=5, without duplicates
JNumberTools.permutations()
    .nPk(10, 5)
    .sample(20)
    .stream().forEach(System.out::println);
```

#### 3.7 k-Permutation of ranks
Generates all k-permutations at indices specified by a custom sequence.

```java
// Generates all k-permutations of n=200 and k=100 at indices specified by Iterable<Number>
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
var elements = new LinkedHashMap<>(Map.of("Apple", 50, "Banana", 200, "Guava", 50));
JNumberTools.permutations()
    .nPk(200, 100)
    .ofRanks(iterable)
    .stream().forEach(System.out::println);
```

### 4. Repetitive Permutation Generators

#### 4.1 Repetitive permutation
This is equivalent to generating base-n numbers of max size r-digits with given n symbols, starting from the 0th permutation in lexicographical order. There are a total of n<sup>r</sup> such permutations.

```java
// Generates all permutations of 3 digits with repetition allowed from [0,5)
JNumberTools.permutations()
    .repetitive(3, 5)
    .lexOrder()
    .stream().toList();

// Generates all permutations of 3 elements out of given elements with repetition allowed
JNumberTools.permutations()
    .repetitive(3, "A", "B", "C", "D", "E")
    .lexOrder()
    .stream().toList();
```

#### 4.2 Every m-th repetitive permutation
This is equivalent to generating an arithmetic progression of base-n numbers with given n symbols and a=start, d=m. If a=0, there are a total of n<sup>r</sup>/m such permutations.

```java
// Generates every 2nd repetitive permutation of 3 numbers in range [0,5), starting from 5th (i.e., 5th, 7th, 9th...)
int m = 2;
int start = 5;
JNumberTools.permutations()
    .repetitive(3, 5)
    .lexOrderMth(m, start)
    .stream().toList();

// Generates every 2nd repetitive permutation of 3 elements out of given elements, starting from 5th
JNumberTools.permutations()
    .repetitive(3, "A", "B", "C", "D", "E")
    .lexOrderMth(m, start)
    .stream().toList();
```

#### 4.3 Repetitive permutation random choice
Generates random repetitive permutations with duplicates allowed.

```java
// Generates any 4 repetitive permutations of 3 numbers in range [0,5) which may contain duplicates
JNumberTools.permutations()
    .repetitive(3, 5)
    .choice(4)
    .stream().toList();
```

#### 4.4 Repetitive permutation random sample
Generates random repetitive permutations without duplicates.

```java
// Generates any 4 repetitive permutations of 3 numbers in range [0,5) without duplicates
JNumberTools.permutations()
    .repetitive(3, 5)
    .sample(4)
    .stream().toList();
```

#### 4.5 Repetitive permutation of ranks
Generates all repetitive permutations at indices specified by a custom sequence.

```java
// Generates all repetitive permutations of n=500 and r=300 at indices specified by Iterable<Number>
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.permutations()
    .repetitive(300, 500)
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