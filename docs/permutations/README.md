[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)



# Permutation Generators
JNumberTools provides following 23 permutation generation APIs. All m<sup>th</sup> generators are BigInteger compatible. That is, it can generate permutation at very large index rapidly. For example permutation at index 10<sup>100</sup> 

**Currently Available Algorithms**

1. [Unique Permutations: 6 different types of permutations](#1-unique-permutation-generators)
   1. [Unique permutation in lex order](#11-unique-permutation-in-lex-order)
   2. [Every m<sup>th</sup> unique permutation in lex order](#12-m-th-unique-permutation)
   3. [Unique permutation with minimum change](#13-unique-permutation-with-minimum-change)
   4. [Unique permutation random choice](#14-unique-permutation-random-choice)
   5. [Unique permutation random sample](#15-unique-permutation-random-sample)
   6. [Unique permutation from sequence](#16-unique-permutation-from-sequence)


2. [Multiset permutations: 5 types of multiset permutation ](#2-multiset-permutation-generators)
   1. [Multiset permutation in lex order](#21-multiset-permutation-in-lex-order)
   2. [Every m<sup>th</sup> multiset permutation](#22-m-th-multiset-permutation-in-lex-order)
   3. [Multiset permutation random choice](#23-multiset-permutation-random-choice)
   4. [Multiset permutation random sample](#24-multiset-permutation-random-sample)
   5. [Multiset permutation from sequence](#25-multiset-permutation-from-sequence)


3. [_k_-Permutations: 7 different variations of _k_-permutations](#3-k-permutation-generators)
   1. [_k_-permutation in lex order](#31-k-permutation-in-lex-order)
   2. [Every m<sup>th</sup> _k_-permutation in lex order](#32-m-th-k-permutation)
   3. [_k_-permutation in combination order](#33-k-permutation-in-combination-order)
   4. [Every m<sup>th</sup> _k_-permutation in combination order](#34-m-th-k-permutation-in-combination-order)
   5. [_k_-permutation random choice](#35-k-permutation-random-choice)
   6. [_k_-permutation random sample](#36-k-permutation-random-sample)
   7. [_k_-permutation from sequence](#37-k-permutation-from-sequence)


4. [Repetitive Permutations: 5 different variations of repetitive permutation](#4-repetitive-permutation-generators)
   1. [Repetitive permutation in lex order](#41-repetitive-permutation)
   2. [Every m<sup>th</sup> repetitive permutation](#42-m-th-repetitive-permutation)
   3. [Repetitive permutation random choice](#43--repetitive-permutation-random-choice)
   4. [Repetitive permutation random sample](#44--repetitive-permutation-random-sample)
   5. [Repetitive permutation from sequence](#45-repetitive-permutation-from-sequence)


***
### 1. Unique Permutation Generators

#### 1.1 Unique permutation in lex order

Used to generate all n! unique permutations of n elements in lex order

```java
//generates all permutations of integers in range [0,3)
JNumberTools.permutations()
   .unique(3)
   .lexOrder().stream().toList();

//generates all permutations of "Red", "Green" and "Blue" in lex order
JNumberTools.permutations()
    .unique( "Red", "Green", "Blue")
    .lexOrder().stream().toList();
```

#### 1.2 M-th Unique permutation

Generate every m<sup>th</sup> permutation in lexicographical order of indices of input
values starting from given start index

This is important because say, if we need to generate next 100 trillionth permutation
of 100 items then it will take months to compute if we go sequentially and then increment
to the desired permutation because the total # of permutations is astronomical
(100!= 9.3326 x 10<sup>157</sup>)

```java
int m = 2;
int start = 5;

//generates 5th, 7th, 9th.. permutations of [0,5)
JNumberTools.permutations()
    .unique(5)
    .lexOrderMth(m,start)
    .stream().toList();

//generates 5th, 7th, 9th.. permutations of elements in lex order
JNumberTools.permutations()
    .unique("A","B","C","D","E")
    .lexOrderMth(m,start)
    .stream().toList();
```

#### 1.3 Unique permutation with minimum change
Generates all unique permutations such that each permutation is single swap away from previous permutation.

```java
//generates all permutations of 5 digits [0-5) with minimum change 
JNumberTools.permutations()
    .unique(5)
    .singleSwap()
    .stream().toList();

//generates all permutations of input with minimum change 
JNumberTools.permutations()
    .unique("A","B","C","D","E")
    .singleSwap()
    .stream().toList();
```

#### 1.4 Unique permutation random choice
Generates random unique permutations with duplicates allowed

```java
//generates exactly 10 permutations of 5 digits [0-5) which may contains duplicates  
JNumberTools.permutations()
    .unique(5)
    .choice(10)
    .stream().toList();

//generates exactly 10 permutations of input which may contains duplicates
JNumberTools.permutations()
    .unique("A","B","C","D","E")
    .choice(10)
    .stream().toList();
```

#### 1.5 Unique permutation random sample
Generates random unique permutations without duplicates

```java
//generates exactly 10 permutations of 5 digits [0-5) without duplicates  
JNumberTools.permutations()
    .unique(5)
    .sample(10)
    .stream().toList();

//generates exactly 10 permutations of input without duplicates
JNumberTools.permutations()
    .unique("A","B","C","D","E")
    .sample(10)
    .stream().toList();
```

#### 1.6 Unique permutation from sequence
Generates all permutations at index specified by custom sequence

```java
//generates all permutations of [0,100) at index specified by Iterable<Number>
var iterable = List.of(10,20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.permutations()
   .unique(100)
   .fromSequence(iterable) //generates 10-th, 20-th, billion-th and sextillion-th permutation
   .stream().toList();
```
### 2. Multiset Permutation Generators

#### 2.1 Multiset permutation in lex order

Permutation, where every item has an associated frequency that denotes how many
times an item can be repeated in a permutation.
For example, permutations of 3 apples and 2 oranges.

```java
var elements = new LinkedHashMap(Map.of("Red",2, "Green",1, "Blue",3));

//permutation of 2 Red, 1 Green and 3 Blue colors in lex order
JNumberTools.permutations().multiset(elements)
   .lexOrder().stream().toList();
```

#### 2.2 M-th multiset permutation in lex order
Generates every m<sup>th</sup> multiset-permutation from a given start index.
This API does not search for the m<sup>th</sup> permutation in a sorted list but
directly generates the desired permutation and hence it is very efficient.
There are total ( ∑ ai × si )! / Π(si!) such permutations.

```java
//every 3rd permutation of 2 Apple, 1 Banana and 3 Guava
//starting from 5th. That is 5th , 8th, 11th ... 
var elements = new LinkedHashMap(Map.of("Apple",2, "Banana",1, "Guava",3));
JNumberTools.permutations().multiset(elements)
  .lexOrderMth(3,5)
  .stream()
  .toList();
```

#### 2.3 Multiset permutation random choice
Generates random permutations of multiset with duplicates allowed

```java
//generates exactly 10 permutations of multiset which may contains duplicates  
var elements = new LinkedHashMap(Map.of("Apple",2, "Banana",1, "Guava",3));

JNumberTools.permutations()
    .multiset(elements)
    .choice(10)
    .stream().toList();
```

#### 2.4 Multiset permutation random sample
Generates random permutations of multiset without duplicates

```java
//generates exactly 10 permutations of multiset without duplicates
var elements = new LinkedHashMap(Map.of("Apple",2, "Banana",1, "Guava",3));

JNumberTools.permutations()
    .multiset(elements)
    .sample(10)
    .stream().toList();
```

#### 2.5 Multiset permutation from sequence
Generates all permutations at index specified by custom sequence

```java
//generates all permutations of [0,100) at index specified by Iterable<Number>
var iterable = List.of(10,20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
var elements = new LinkedHashMap<>(Map.of("Apple",50, "Banana",200, "Guava",50));

JNumberTools.permutations()
    .multiset(elements) //generates 10-th, 20-th, billion-th and sextillion-th permutation
    .fromSequence(iterable) //generates 10-th, 20-th, billion-th and sextillion-th permutation
    .stream().forEach(System.out::println);
```
### 3. _k_-Permutation Generators

#### 3.1 _k_-permutation in lex order
Generates all unique permutations of size _k_ where _0 ≤ k ≤ n_ and _n_ is the number of
input elements in lex order. In number theory, this is also known as _k_-permutation.
There are total <sup>n</sup>P<sub>k</sub> such permutations possible

```java
//generates all k-permutations of size '2' out of 5 numbers in range [0,5)
JNumberTools.permutations()
     .nPk(5,2)
     .lexOrder()
     .stream().toList();

//generates all permutations of '2' elements out of list of elements
     JNumberTools.permutations()
     .nPk(2,"A","B","C","D","E")
     .lexOrder()
     .stream().toList();
```

#### 3.2 m-th k-permutation in lex order
Generates every  m<sup>th</sup> _k_-permutation in lex order without computing permutations
preceding it. This concept is important because the total number of permutations can grow
astronomically large. For instance, the number of permutations of 100 elements selected 50
at a time is 100P50 = 3.068518756 x 10<sup>93</sup>, which is way beyond the practical limit
to be generated sequentially to reach the desired permutation.

To achieve this, a concept called Permutational-Number-System aka Permutadic (Generalization of factoradic) and Deep-code(a generalization of Lehmer code)
is used. Details can be found in the research paper - [Generating the nth Lexicographical
Element of a Mathematical k-Permutation using Permutational Number System](https://papers.ssrn.com/sol3/papers.cfm?abstract_id=4174035)

```java
//generates every 3rd permutation starting from 0th of size 3 out of 10 
// numbers in range [0,10]. that is 0th, 3rd, 6th, 9th and so on
JNumberTools.permutations()
   .nPk(10,5)
   .lexOrderMth(3, 0)
   .stream().toList();

//generates every 3rd permutation starting from 0th of size 3 out of list of n elements
//that is 0th, 3rd, 6th, 9th and so on
JNumberTools.permutations()
   .nPk(2,"A","B","C","D","E")
   .lexOrder()
   .stream().toList();
```

#### 3.3 _k_-permutation in combination order
Generates all _k_-permutations in the lexicographical order of combination. For example, [C,A] comes before [B,C]
because combination-wise [C,A] = [A,C]. Note that the API does not sort the output to achieve this,
but it generates the permutation in said order, so it is very efficient.

```java
//generates all k-permutations for n=5 and k=2 in combination first order
JNumberTools.permutations()
   .nPk(5,2)
   .combinationOrder()
   .stream().toList();

JNumberTools.permutations()
   .nPk(2,"A","B","C","D","E")
   .combinationOrder()
   .stream().toList();
```

#### 3.4 m-th _k_-permutation in combination order
Generates every m<sup>th</sup> _k_-permutation in the lexicographical order of combination.
This API does not sort or search to achieve this but generates the desired permutation on the fly,
so it is very efficient.

```java
//generates every 3rd, k-permutations for n=5 and k=2 in combination first order
//starting from 0th. That is 0th, 3rd, 6th and so on
JNumberTools.permutations()
   .nPk(5,2)
   .combinationOrderMth(3,0)
   .stream().toList();

JNumberTools.permutations()
   .nPk(2,"A","B","C","D","E")
   .combinationOrderMth(3,0)
   .stream().toList();
```

#### 3.5 _k_-permutation random choice
Generates random _k_-permutations with duplicates allowed

```java
//generates 20 _k_-permutations for n=20 and k=5, which may contains duplicates  
JNumberTools.permutations()
        .nPk(10,5)
        .choice(20)
        .stream().forEach(System.out::println);
```

#### 3.6  _k_-permutation random sample
Generates random _k_-permutations with duplicates allowed

```java
//generates 20 _k_-permutations for n=20 and k=5, which may contains duplicates  
JNumberTools.permutations()
        .nPk(10,5)
        .sample(20)
        .stream().forEach(System.out::println);
```

#### 3.7 _k_-permutation from sequence
Generates all _k_-permutations at index specified by custom sequence

```java
//generates all permutations of n=200 and k=100 at index specified by Iterable<Number>
var iterable = List.of(10,20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
var elements = new LinkedHashMap<>(Map.of("Apple",50, "Banana",200, "Guava",50));

JNumberTools.permutations()
    .nPk(200,100) //generates 10-th, 20-th, billion-th and sextillion-th permutation
    .fromSequence(iterable) //generates 10-th, 20-th, billion-th and sextillion-th permutation
    .stream().forEach(System.out::println);
```

### 4. Repetitive Permutation Generators

#### 4.1 Repetitive permutation
This is same as generating base-_n_ numbers of max size _r_-digits with given _n_ symbols, starting
from 0<sup>th</sup> permutation in lex order. There are total n<sup>r</sup> such permutations.

```java
//generates al permutations of 3 digits with repetition allowed from [0,5)
JNumberTools.permutations()
    .repetitive(3, 5)
    .lexOrder()
    .stream().toList();

//generates all permutations of 3 elements out of given elements with repetition allowed
JNumberTools.permutations()
    .repetitive(3,"A","B","C","D","E")
    .lexOrder()
    .stream().toList();
```

#### 4.2 m-th Repetitive permutation
This is same as generating AP series of base-_n_ numbers with given _n_ symbols and
a=start, d=m. If a=0, there are total n<sup>r</sup>/m such permutations

```java
//generates every 2nd repetitive permutations of 3 numbers in range [0,5)
//starting from 5th. That is, 5th, 7th, 9th, and so on 
int m = 2;
int start = 5;
JNumberTools.permutations()
        .repetitive(3, 5)
        .lexOrderMth(m,start)
        .stream().toList();

//generates every 2nd repetitive permutations of 3 elements out of given elements
//starting from 5th. That is, 5th, 7th, 9th, and so on 
        JNumberTools.permutations()
        .repetitive(3,"A","B","C","D","E")
        .lexOrderMth(m,start)
        .stream().toList();
```

#### 4.3  Repetitive permutation random choice
Generates random repetitive permutations with duplicates allowed

```java
//generates any 4 repetitive permutations of 3 numbers in range [0,5) which may contains duplicates  
JNumberTools.permutations()
   .repetitive(3, 5)
   .choice(4)
   .stream().toList();
```

#### 4.4  Repetitive permutation random sample
Generates random repetitive permutations without any duplicates

```java
//generates any 4 repetitive permutations of 3 numbers in range [0,5) without any duplicates  
JNumberTools.permutations()
   .repetitive(3, 5)
   .sample(4)
   .stream().toList();
```

#### 4.5 Repetitive permutation from sequence
Generates all _k_-permutations at index specified by custom sequence

```java
//generates all repetitive permutations of n=500 and r=300 at index specified by Iterable<Number>
var iterable = List.of(10,20, 1_000_000_000L, new BigInteger("1000000000000000000000"));

JNumberTools.permutations()
   .repetitive(300, 500)
    .fromSequence(iterable) //generates 10-th, 20-th, billion-th and sextillion-th permutation
   .stream().toList();
```
[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)


