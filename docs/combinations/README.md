[Home](../../README.md)  
    |    
[Permutation Generators](../permutations/README.md)  
    |    
[Combination Generators](../combinations/README.md)  
    |    
[Set/Subset Generators](../sets/sets.md)  
    |    
[Cartesian Product Generators](../products/README.md)  
    |    
[Math Functions](../calculator/README.md)  
    |    
[Ranking Algorithms](../ranking/README.md)

# Combination Generators

JNumberTools provides the following 15 combination generation APIs. All m<sup>th</sup> generators are BigInteger compatible, enabling rapid generation of combinations at very large indices, such as 10<sup>100</sup>.

**Currently Available Algorithms**

1. [Unique Combinations](#1-unique-combinations)
   1. [Unique combination in lex order](#11-unique-combination-in-lex-order)
   2. [Every m<sup>th</sup> unique combination in lex order](#12-every-m-th-unique-combination-in-lex-order)
   3. [Unique combination random choice](#13-unique-combination-random-choice)
   4. [Unique combination random sample](#14-unique-combination-random-sample)
   5. [Unique combination from sequence](#15-unique-combination-from-sequence)

2. [Multiset Combinations](#2-multiset-combinations)
   1. [Multiset combination in lex order](#21-multiset-combination-in-lex-order)
   2. [Every m<sup>th</sup> multiset combination](#22-every-m-th-multiset-combination)
   3. [Multiset combination random choice](#23-multiset-combination-random-choice)
   4. [Multiset combination random sample](#24-multiset-combination-random-sample)
   5. [Multiset combination from sequence](#25-multiset-combination-from-sequence)

3. [Repetitive Combinations](#3-repetitive-combinations)
   1. [Repetitive combination in lex order](#31-repetitive-combination-in-lex-order)
   2. [Every m<sup>th</sup> repetitive combination](#32-every-m-th-repetitive-combination)
   3. [Repetitive combination random choice](#33-repetitive-combination-random-choice)
   4. [Repetitive combination random sample](#34-repetitive-combination-random-sample)
   5. [Repetitive combination from sequence](#35-repetitive-combination-from-sequence)

---

### 1. Unique Combinations

#### 1.1 Unique combination in lex order
Selection of r distinct items out of n elements, also known as n-Choose-r. Generates all combinations in lexicographical order.

```java
// All possible combinations of 3 numbers in range [0,5) in lex order
JNumberTools.combinations()
    .unique(5, 3)
    .lexOrder()
    .stream().toList();

// All possible combinations of 3 elements from ["A","B","C","D","E"] in lex order
JNumberTools.combinations()
    .unique(3, "A", "B", "C", "D", "E")
    .lexOrder()
    .stream().toList();
```

#### 1.2 Every m-th unique combination in lex order
Generates every m<sup>th</sup> combination in lexicographical order, starting from a given index. This is efficient for large combination counts, such as generating the billionth combination of 34-Choose-17 without sequential iteration.

```java
// 5th, 7th, 9th... combinations of 3 numbers in range [0,5) in lex order
JNumberTools.combinations()
    .unique(5, 3)
    .lexOrderMth(2, 5)
    .stream().toList();

// 5th, 7th, 9th... combinations of 3 elements from ["A","B","C","D","E"] in lex order
JNumberTools.combinations()
    .unique(3, "A", "B", "C", "D", "E")
    .lexOrderMth(2, 5)
    .stream().toList();
```

#### 1.3 Unique combination random choice
Generates n random combinations with duplicates allowed.

```java
// Generate any 5 random combinations of 10 items [0,10) from 20 items [0,20) with duplicates
JNumberTools.combinations()
    .unique(20, 10)
    .choice(5)
    .stream().forEach(System.out::println);

// Generate any 5 random combinations of 5 items from ["A","B","C","D","E","F","G"] with duplicates
JNumberTools.combinations()
    .unique(5, "A", "B", "C", "D", "E", "F", "G")
    .choice(5)
    .stream().forEach(System.out::println);
```

#### 1.4 Unique combination random sample
Generates n random combinations without duplicates.

```java
// Generate any 5 random combinations of 10 items [0,10) from 20 items [0,20) without duplicates
JNumberTools.combinations()
    .unique(20, 10)
    .sample(5)
    .stream().forEach(System.out::println);

// Generate any 5 random combinations of 5 items from ["A","B","C","D","E","F","G"] without duplicates
JNumberTools.combinations()
    .unique(5, "A", "B", "C", "D", "E", "F", "G")
    .sample(5)
    .stream().forEach(System.out::println);
```

#### 1.5 Unique combination from sequence
Generates combinations at indices specified by a custom sequence.

```java
// Generates combinations of 100 items from 200 at specified indices
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.combinations()
    .unique(200, 100)
    .fromSequence(iterable)
    .stream().toList();
```

### 2. Multiset Combinations

#### 2.1 Multiset combination in lex order
Generates all combinations of selecting r items from a multiset, where each item has a specified frequency, in lexicographical order.

```java
// All combinations of 3 items from a multiset with 2 Apples and 1 Banana
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1));
JNumberTools.combinations()
    .multiset(elements, 3)
    .lexOrder()
    .stream().toList();
```

#### 2.2 Every m-th multiset combination
Generates every m<sup>th</sup> multiset combination in lexicographical order, starting from a given index, without computing preceding combinations.

```java
// Every 3rd combination of 3 items from a multiset with 2 Apples and 1 Banana, starting from 5th
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1));
JNumberTools.combinations()
    .multiset(elements, 3)
    .lexOrderMth(3, 5)
    .stream().toList();
```

#### 2.3 Multiset combination random choice
Generates n random multiset combinations with duplicates allowed.

```java
// Generate 5 random combinations of 3 items from a multiset with 2 Apples and 1 Banana, with duplicates
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1));
JNumberTools.combinations()
    .multiset(elements, 3)
    .choice(5)
    .stream().forEach(System.out::println);
```

#### 2.4 Multiset combination random sample
Generates n random multiset combinations without duplicates.

```java
// Generate 5 random combinations of 3 items from a multiset with 2 Apples and 1 Banana, without duplicates
var elements = new LinkedHashMap<>(Map.of("Apple", 2, "Banana", 1));
JNumberTools.combinations()
    .multiset(elements, 3)
    .sample(5)
    .stream().forEach(System.out::println);
```

#### 2.5 Multiset combination from sequence
Generates multiset combinations at indices specified by a custom sequence.

```java
// Generates combinations of 3 items from a multiset with 50 Apples and 50 Bananas at specified indices
var elements = new LinkedHashMap<>(Map.of("Apple", 50, "Banana", 50));
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.combinations()
    .multiset(elements, 3)
    .fromSequence(iterable)
    .stream().toList();
```

### 3. Repetitive Combinations

#### 3.1 Repetitive combination in lex order
Generates all combinations of r items from n elements with repetition allowed, also known as n+Choose-r, in lexicographical order.

```java
// All combinations of 3 numbers with repetition from [0,5) in lex order
JNumberTools.combinations()
    .repetitive(5, 3)
    .lexOrder()
    .stream().toList();

// All combinations of 3 elements with repetition from ["A","B","C","D","E"] in lex order
JNumberTools.combinations()
    .repetitive(3, "A", "B", "C", "D", "E")
    .lexOrder()
    .stream().toList();
```

#### 3.2 Every m-th repetitive combination
Generates every m<sup>th</sup> repetitive combination in lexicographical order, starting from a given index.

```java
// 5th, 7th, 9th... combinations of 3 numbers with repetition from [0,5) in lex order
JNumberTools.combinations()
    .repetitive(5, 3)
    .lexOrderMth(2, 5)
    .stream().toList();

// 5th, 7th, 9th... combinations of 3 elements with repetition from ["A","B","C","D","E"] in lex order
JNumberTools.combinations()
    .repetitive(3, "A", "B", "C", "D", "E")
    .lexOrderMth(2, 5)
    .stream().toList();
```

#### 3.3 Repetitive combination random choice
Generates n random repetitive combinations with duplicates allowed.

```java
// Generate 5 random combinations of 3 numbers with repetition from [0,5) with duplicates
JNumberTools.combinations()
    .repetitive(5, 3)
    .choice(5)
    .stream().forEach(System.out::println);
```

#### 3.4 Repetitive combination random sample
Generates n random repetitive combinations without duplicates.

```java
// Generate 5 random combinations of 3 numbers with repetition from [0,5) without duplicates
JNumberTools.combinations()
    .repetitive(5, 3)
    .sample(5)
    .stream().forEach(System.out::println);
```

#### 3.5 Repetitive combination from sequence
Generates repetitive combinations at indices specified by a custom sequence.

```java
// Generates combinations of 100 items with repetition from 200 at specified indices
var iterable = List.of(10, 20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.combinations()
    .repetitive(200, 100)
    .fromSequence(iterable)
    .stream().toList();
```

[Home](../../README.md)  
    |    
[Permutation Generators](../permutations/README.md)  
    |    
[Combination Generators](../combinations/README.md)  
    |    
[Set/Subset Generators](../sets/sets.md)  
    |    
[Cartesian Product Generators](../products/README.md)  
    |    
[Math Functions](../calculator/README.md)  
    |    
[Ranking Algorithms](../ranking/README.md)