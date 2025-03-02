[Home](../../README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Permutation Generators](../permutations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Combination Generators](../combinations/README.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Set/subset Generators](../sets/sets.md)
&emsp;&emsp;&emsp; | &emsp;&emsp;&emsp;
[Cartesian Product Generators](../products/README.md)



# Set/subset Generators

JNumberTools provides following 7 generators for sets/subsets


1. [All subsets of a given set in lex order](#1-all-subsets-in-lex-order)
2. [Every mth of all subsets in lex order](#2-every-m-th-subsets-in-lex-order)
3. [All subsets of a given size range in lex order](#3-all-subsets-of-given-size-range-in-lex-order)
4. [Every mth of subsets in a given size range in lex order](#4-every-m-th-subsets-in-range-in-lex-order)
5. Random subset choice TODO
6. Random subset sample TODO
7. Subsets from sequence TODO


#### 1 All subsets in lex order
Generates all subset of a given set in lex order.
For example for all subsets of "A", "B", "C"
it will result

[ ],[A], [B], [C],[A, B], [A, C], [B, C], [A, B, C]

The first empty set represents φ set
```java
//all subsets of "Apple", "Banana", "Guava" in lex order
JNumberTools.subsets()
  .of("Apple", "Banana", "Guava")
  .all().lexOrder()
  .stream().toList();
```

#### 2 Every m-th subsets in lex order
Generates every m<sup>th</sup> subset of a given set in lex order. Starting from given index.
This API does not search for the m<sup>th</sup> subset in a sorted list but
directly generates the desired subset and hence it is very efficient.

```java
//every 1 billion-th subsets of numbers in range [0,40)
//starting from 0th index
JNumberTools.subsets()
  .of(40)
  .all().lexOrderMth(1000000000,0)
  .stream().toList();
```

#### 3 All subsets of given size range in lex order
Generates all subset of a given size range in lex order.
For example for all subsets of "A", "B", "C" of size range [2,3]
will result

[A, B], [A, C], [B, C], [A, B, C]

The first empty set represents φ set
```java
//all subsets of "Apple", "Banana", "Guava" in size range [2,3] in lex order
JNumberTools.subsets()
   .of("Apple", "Banana", "Guava")
   .inRange(2,3)
   .lexOrder()
   .stream().toList();
```

#### 4 Every m-th subsets in range in lex order
Generates every m<sup>th</sup> subset in a given size range in lex order. Starting from given index.
This API does not search for the m<sup>th</sup> subset in a sorted list but
directly generates the desired subset and hence it is very efficient.

```java
//every 1 billion-th subsets of numbers in range [0,40) with size range [10,39]
//starting from 0th index
JNumberTools.subsets()
   .of(40)
   .inRange(10,39).lexOrderMth(1000000000,0)
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



