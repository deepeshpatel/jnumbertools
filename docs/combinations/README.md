[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)



# Combination Generators

JNumberTools provides following 15 combination generation APIs. All m<sup>th</sup> generators are BigInteger compatible. That is, it can generate combinations at very large index rapidly. For example combination at index 10<sup>100</sup>

**Currently Available Algorithms**

1. [Unique Combinations: 5 different types of combinations](#1-unique-combinations)
   1. [Uniques combination in lex order](#11-unique-combination-in-lex-order)
   2. [Every m<sup>th</sup> unique combination in lex order](#12-every-m-th-unique-combination-in-lex-order)
   3. [Unique combination random choice](#13-unique-combination-random-choice)
   4. [Unique combination random sample](#14-unique-combination-random-sample)
   5. [Unique combination from sequence](#15-unique-combination-frm-sequence)


2. Multiset Combinations
   1. Multiset combination in lex order
   2. Every mth multiset combination
   3. Multiset combination random choice
   4. Multiset combination random sample
   5. Multiset combination from sequence


3. Repetitive Combinations
   1. Repetitive combination in lex order
   2. Every Mth repetitive combination
   3. Repetitive combination random choice
   4. Repetitive combination random sample
   5. Repetitive combination from sequence

***

### 1. Unique Combinations

#### 1.1 Unique combination in lex order
Selection of r distinct items out of n elements. In mathematics, this is also known as n-Choose-r. Generates all combinations in lex order.

```java
//all possible combination of 3 numbers in range [0,5) in lex order 
JNumberTools.combinations()
  .unique(5,3)
  .lexOrder().stream().toList();

JNumberTools.combinations()
   .unique(3,"A","B","C","D","E")
   .lexOrder().stream().toList();
```

#### 1.2 Every m-th unique combination in lex order
Same as n-Choose-r but generates every m<sup>th</sup> combination in lex order starting from given index.  This concept is important because the count of combinations can grow astronomically large. For example, to generate, say, the next 1 billionth combination of 34-Choose-17,  we need to wait for days to generate the desired billionth combination if we generate all combinations sequentially and then select the billionth combination.

```java
//5th, 7th , 9th.. combination of 3 numbers in range [0,5) in lex order 
JNumberTools.combinations()
 .unique(5,3)
 .lexOrderMth(5,2).stream().toList();

JNumberTools.combinations()
   .unique(3,"A","B","C","D","E")
   .lexOrderMth(5,2).stream().toList();
```

#### 1.3 Unique combination random choice
generates 'n' random combinations where duplicates are allowed
```java
//generate any 5 random combinations of 10 items [0,10) from 20 items [0,20) with possibility of duplicates
JNumberTools.combinations()
    .unique(20,10)
    .choice(5) //5 random combinations
    .stream().forEach(System.out::println);

//generate any 5 random combinations of 5 items from ["A","B","C","D","E","F","G"] with possibility of duplicates
JNumberTools.combinations()
    .unique(5,"A","B","C","D","E","F","G")
    .choice(5)
    .stream().forEach(System.out::println);
```

#### 1.4 Unique combination random sample
generates 'n' random combinations without any duplicates 
```java
//generate any 5 random combinations of 10 items [0,10) from 20 items [0,20) with no duplicates
JNumberTools.combinations()
    .unique(20,10)
    .sample(5) //5 random combinations
    .stream().forEach(System.out::println);

//generate any 5 random combinations of 5 items from ["A","B","C","D","E","F","G"] with no duplicates
JNumberTools.combinations()
    .unique(5,"A","B","C","D","E","F","G")
    .sample(5)
    .stream().forEach(System.out::println);
```

#### 1.5 Unique combination frm sequence
Generates all combinations at index specified by custom sequence
```java
//generates all mth combinations specified by Iterable<Number>
var iterable = List.of(10,20, 1_000_000_000L, new BigInteger("1000000000000000000000"));
JNumberTools.combinations()
   .unique(200,100)
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

