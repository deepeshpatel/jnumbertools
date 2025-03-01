**Permutation Algorithms in JNumberTools**

Current version provides 10 different types of permutations
 1. [Unique permutation in lex order](#1-unique-permutation)
 2. [Every m<sup>th</sup> unique permutation in lex order](#2-m-th-unique-permutation)
 3. [Repetitive permutation in lex order](#3-repetitive-permutation)
 4. [Every m<sup>th</sup> repetitive permutation in lex order](#4-m-th-repetitive-permutation)
 5. [k-permutation in lex order](#5-k-permutation)
 6. [Every m<sup>th</sup> k-permutation in lex order](#6-m-th-k-permutation)
 7. [k-permutation in combination order](#7-k-permutation-in-combination-order)
 8. [Every m<sup>th</sup> permutation in combination order](#8-m-th-k-permutation-in-combination-order)
 9. [Multiset permutation in lex order](#9-multiset-permutation-in-lex-order)
 10. [Every m<sup>th</sup> multiset permutation in lex order](#10-m-th-multiset-permutation-in-lex-order)

***

### 

1 Unique permutation

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

### 2 m-th Unique permutation

Generate every m<sup>th</sup> permutation in lexicographical order of indices of input
values starting from given start index

This is important because say, if we need to generate next 100 trillionth permutation
of 100 items then it will take months to compute if we go sequentially and then increment
to the desired permutation because the total # of permutations is astronomical
(100!= 9.3326 x 10m<sup>157</sup>)

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

### 3 Repetitive permutation
This is same as generating base-n numbers of max size r-digits with given n symbols, starting
from 0<sup>th</sup> permutation in lex order. There are total n<sup>r</sup> such permutations

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

### 4 m-th Repetitive permutation
This is same as generating AP series of base-n numbers with given n-symbols and
a=start, d=m and max-digits = r. There are total n<sup>r</sup>/m such permutations

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
### 5 k-permutation
Generates all unique permutations of size k where _0 ≤ k ≤ n_ and _n_ is the number of
input elements in lex order. In number theory, this is also known as _k_-permutation.
There are total <sup>n</sup>P<sub>k</sub> such permutations possible

```java
//generates all permutations of size '2' out of 5 numbers in range [0,5)
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

### 6 m-th k-permutation
Generates every  m<sup>th</sup> _k_-permutation in lex order without computing permutations
preceding it. This concept is important because the total number of permutations can grow
astronomically large. For instance, the number of permutations of 100 elements selected 50
at a time is 100P50 = 3.068518756 x 10<sup>93</sup>, which is way beyond the practical limit
to be generated sequentially to reach the desired permutation.

To achieve this, a concept called Permutational Number System(Permutadic) and Deep-code(a generalization of Lehmer code)
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

### 7 k-permutation in combination order
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

### 8 m-th k-permutation in combination order
Generates every m<sup>th</sup> k-permutation in the lexicographical order of combination.
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

### 9 multiset permutation in lex order
Permutation, where every item has an associated frequency that denotes how many
times an item can be repeated in a permutation.
For example, permutations of 3 apples and 2 oranges.

```java
var elements = List.of("Red", "Green", "Blue");
int[] frequencies = {2,1,3};

//permutation of 2 Red, 1-Green and 3 Blue colors in lex order
JNumberTools.permutations().multiset(elements, frequencies)
   .lexOrder().stream().toList();
```

### 10 m-th multiset permutation in lex order
Generates every m<sup>th</sup> multiset-permutation from a given start index.
This API does not search for the m<sup>th</sup> permutation in a sorted list but
directly generates the desired permutation and hence it is very efficient.
There are total ( ∑ ai . si )! / Π(si!) such permutations.

```java
//every 3 permutation of 2 Apple, 1 Banana and 3 Guava
//starting from 5th. That is 5th , 8th, 11th ... 
JNumberTools.permutations().multiset(elements, frequencies)
  .lexOrderMth(3,5)
  .stream()
  .toList();
```
