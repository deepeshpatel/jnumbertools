![JNumberTools](resources/JNumberTools_broad.webp)

# JNumberTools
**JNumberTools** is an open-source Java library designed to provide powerful tools for solving complex problems in combinatorics and number theory. Whether you're a researcher, developer, or student, this library offers a comprehensive set of APIs to efficiently handle a wide range of mathematical tasks, from basic functions to advanced combinatorial computations.

Optimized for performance, JNumberTools allows you to tackle challenging combinatorics problems—such as permutations, combinations, conditional-cartesian-products, ranking, partitions, and more—without the need to develop custom algorithms. Seamlessly integrate these capabilities into your projects to streamline both academic and practical applications in fields like mathematics, cryptography, data analysis, and algorithm design.

**Key Features:**

1. Versatile and efficient APIs for various combinatorics and number-theory operations.
2. Ideal for educational, research, and real-world problem-solving in fields such as cryptography, computer science, and optimization.
3. Optimized for both small-scale and large-scale problems, delivering high performance.


## latest version
The latest release of the library is [v3.0.0](https://github.com/deepeshpatel/jnumbertools/releases/tag/v3.0.0).
It is available through The Maven Central Repository [here](https://central.sonatype.com/search?q=jnumbertools&smo=true).
Add the following section into your `pom.xml` file.

```xml
<dependency>
    <groupId>io.github.deepeshpatel</groupId>
    <artifactId>jnumbertools</artifactId>
    <version>3.0.0</version>
</dependency>
```

**Currently Available Algorithms**

1. [Permutations: 10 different types of permutations](#1-permutations)
   1. [Unique permutation in lex order](#11-unique-permutation)
   2. [Every m<sup>th</sup> unique permutation in lex order](#12-m-sup-th-sup-unique-permutation)
   3. [Repetitive permutation in lex order](#13-repetitive-permutation)
   4. [Every m<sup>th</sup> repetitive permutation in lex order](#14-m-sup-th-sup-repetitive-permutation)
   5. [k-permutation in lex order](#15-k--permutation)
   6. [Every m<sup>th</sup> k-permutation in lex order](#16-m-sup-th-sup-k--permutation)
   7. [k-permutation in combination order](#17-k--permutation-in-combination-order)
   8. [Every m<sup>th</sup> permutation in combination order](#18-m-sup-th-sup-k--permutation-in-combination-order)
   9. [Multiset permutation in lex order](#19-multiset-permutation-in-lex-order)
   10. [Every m<sup>th</sup> multiset permutation in lex order](#110-m-sup-th-sup-multiset-permutation-in-lex-order)


2. [Combinations: 5 different types of combinations](#2-combinations)
   1. [Uniques combination in lex order](#21-unique-combination-in-lex-order)
   2. [Every m<sup>th</sup> unique combination in lex order](#22-m-sup-th-sup-unique-combination-in-lex-order)
   3. [Repetitive combination in lex order](#23-repetitive-combination-in-lex-order)
   4. [Every m<sup>th</sup> repetitive combination in lex order](#24-m-sup-th-sup-repetitive-combination-in-lex-order)
   5. [Multiset combination in lex order](#25-multiset-combination-in-lex-order)
   6. Every m<sup>th</sup> multiset combination in lex order: Coming soon


3. [Set/subset generations: 4 different types available](#3-subsets)
   1. [All subsets of a given set in lex order](#31-all-subsets-in-lex-order)
   2. [Every m<sup>th</sup> of all subsets in lex order](#32-mm-sup-th-sup-subsets-in-lex-order)
   3. [All subsets in a given size range in lex order](#33-all-subsets-of-given-size-range-in-lex-order)
   4. [Every m<sup>th</sup> of subsets in a given size range in lex order](#34-m-sup-th-sup-subsets-in-range-in-lex-order)


4. [Cartesian Product: 4 different types of product](#4-cartesian-product)
   1. [Simple Cartesian product in lex order](#41-simple-cartesian-product)
   2. [Every m<sup>th</sup> cartesian product in lex order](#42-m-sup-th-sup-simple-cartesian-product)
   3. [Complex Cartesian product in lex order](#43-complex-cartesian-product)
   4. [Every m<sup>th</sup> complex cartesian product in lex order](#44-m-sup-th-sup-complex-cartesian-product)


5. [Ranking of permutations & combinations](#5-ranking-of-permutations--combinations)
   1. [Ranking of unique permutation](#51-ranking-of-unique-permutation)
   2. [Ranking of k-permutation](#52-ranking-of-k-permutation)
   3. [Ranking of repetitive permutation](#53-ranking-of-repetitive-permutation)
   4. [Ranking of unique combination](#54-ranking-of-combination)
   5. Rank of repetitive combination : Coming soon
   6. Rank of multiset permutation : Coming soon
   7. Rank of multiset combination : Coming soon


6. Number system algorithms related to combinatorics
   1. Factorial Number System aka Factoradic
   2. Permutation Number System aka Permutadic
   3. Combinatorial Number System aka Combinadic

***
### 1. Permutations

#### 1.1 Unique permutation

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

#### 1.2 M<sup>th</sup> Unique permutation

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

#### 1.3 Repetitive permutation
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

#### 1.4 M<sup>th</sup> Repetitive permutation
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
#### 1.5 _k_-permutation
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

#### 1.6 m<sup>th</sup> _k_-permutation
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

#### 1.7 _k_-permutation in combination order
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

#### 1.8 m<sup>th</sup> _k_-permutation in combination order
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

#### 1.9 multiset permutation in lex order
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

#### 1.10 m<sup>th</sup> multiset permutation in lex order
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

### 2. Combinations

#### 2.1 Unique combination in lex order
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

#### 2.2 M<sup>th</sup> unique combination in lex order
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

#### 2.3 Repetitive combination in lex order
Generates combinations with repeated elements allowed in lexicographical order.
There are total <sup>(n+r-1)</sup>C<sub>r</sub> combinations with repetition.

```java
// all combination of 3 numbers with 
// repetition allowed in range [0,5) in lex order 
JNumberTools.combinations()
   .repetitive(5,3)
   .lexOrder().stream().toList();

JNumberTools.combinations()
   .repetitive(3,"A","B","C","D","E")
   .lexOrder().stream().toList();
```
#### 2.4 M<sup>th</sup> repetitive combination in lex order
Generates every m<sup>th</sup> combination with repeated elements in lex order

```java
// 5th, 7th , 9th.. combination of 3 numbers with 
// repetition allowed in range [0,5) in lex order 
JNumberTools.combinations()
   .repetitive(5,3)
   .lexOrderMth(5,2).stream().toList();

JNumberTools.combinations()
   .repetitive(3,"A","B","C","D","E")
   .lexOrderMth(5,2).stream().toList();
```

#### 2.5 Multiset combination in lex order
Special case of repetitive combination where every element has an associated frequency that denotes how many times an element can be repeated in a combination. For example, combinations of 3 apples and 2 oranges.

```java
//combinations of any 2 fruits out of 2 apples, 1 banana and 3 guavas in lex order
var elements = List.of("Apple", "Banana", "Guava");
int[] frequencies = {2,1,3};

JNumberTools.combinations()
    .multiset(elements,frequencies, 3)
    .lexOrder().stream().toList();
```

### 3. Subsets

#### 3.1 All subsets in lex order
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

#### 3.2 MM<sup>th</sup> subsets in lex order
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

#### 3.3 All subsets of given size range in lex order
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

#### 3.4 M<sup>th</sup> subsets in range in lex order
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

### 4. Cartesian Product

#### 4.1 Simple Cartesian product
A Cartesian Product of two sets A and B, denoted by A × B is the set of all 
ordered pairs (a, b) where a ∈ A and b ∈ B. 

This API supports the cartesian product of n different sets. That is for sets A, B, C, D...
you can find A × B × C × D X ... directly in single API call

```java
//every combination of 1 pizza-base, 1 pizza-crust and 1 cheese
var pizzaSize   =  List.of("Small", "Medium", "Large");
var pizzaCrust  =  List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese      =  List.of( "Ricotta ","Mozzarella","Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrder().stream().toList();
```

#### 4.2 M<sup>th</sup> Simple Cartesian product
Generates every m<sup>th</sup> cartesian product starting from any starting index of choice.
This API does not search for the m<sup>th</sup> product in a sorted list but
directly generates the desired product and hence it is very efficient.

```java
//every 10th combination of 1 pizza-base, 1-pizza crust and 1 cheese
//starting form 5th index in lex order. That is 5th, 10th,15th and 20th
var pizzaSize   =  List.of("Small", "Medium", "Large");
var pizzaCrust  =  List.of("Flatbread", "Neapolitan", "Thin Crust");
var cheese      =  List.of( "Ricotta ","Mozzarella","Cheddar");

JNumberTools.cartesianProduct()
   .simpleProductOf(pizzaSize)
   .and(pizzaCrust)
   .and(cheese)
   .lexOrderMth(5,10).stream().toList();
```

#### 4.3 Complex Cartesian product
Same as simple cartesian product but instead of having only one item from each list
we can select multiple, select repetitive elements and select in a given range.
```java
//Print all combination of -
// any 1 pizza base and
// any 2 distinct cheese
// and any 2 sauce (repeated allowed)
// and any toppings in range 1 to 5";

var pizzaBase = List.of("Small ","Medium", "Large");
var cheese = List.of( "Ricotta ","Mozzarella","Cheddar");
var sauce = List.of( "Tomato Ketchup","White Sauce","Green Chutney");
var toppings = List.of("Tomato","Capsicum", "Onion", "Corn", "Mushroom");

JNumberTools
   .cartesianProduct().complexProductOf(1, pizzaBase)
   .andDistinct(2, cheese)
   .andMultiSelect(2, sauce)
   .andInRange(1,5,toppings)
   .lexOrder().stream().toList();
```

#### 4.4 M<sup>th</sup> Complex Cartesian product
Generates m<sup>th</sup> complex cartesian product directly without calculating the values
preceding it.
For example below code prints every 10<sup>18</sup> th lexicographical combination
which is not feasible to calculate via one by one iteration. API supports BigInteger,
so it is even possible to execute it for very large value of m. 
Say every 10<sup>100</sup> th combination.
```java
//Every 10^18 th lexicographical combinations of -
// any 10 distinct small alphabets and
// any 12 distinct capital alphabets and
// any 3 symbols with repetition allowed and 
// all subsets in size range [10,20] of a set of numbers in [0,20)

var smallAlphabets = IntStream.rangeClosed('a', 'z').mapToObj(c -> (char) c).toList();
var capitalAlphabets = IntStream.rangeClosed('A', 'Z').mapToObj(c -> (char) c).toList();
var symbols = List.of('~','!','@','#','$','%','^','&','*','(',')');
var numbers = IntStream.rangeClosed(0, 20).boxed().toList();

JNumberTools.cartesianProduct()
   .complexProductOf(10, smallAlphabets) //distinct
   .andDistinct(12, capitalAlphabets) //distinct
   .andMultiSelect(3, symbols) // repetition allowed
   .andInRange(10, 20, numbers) // all subsets in size range
   .lexOrderMth(1000_000_000_000_000_000L, 0).stream().toList();
```

### 5. Ranking of permutations & combinations

#### 5.1 Ranking of unique permutation
Calculates the rank of a unique permutation starting from 0th
For example, there are total 4!=24 permutations of [0,1,2,3]
where the first permutation [0,1,2,3] has rank 0 and the last permutation
[3,2,1,0] has the rank of 23

```java
BigInteger rank = JNumberTools.rankOf().uniquePermutation(3,2,1,0);
```

#### 5.2 Ranking of k-permutation
Calculates the rank of a k=permutation. For example if we 
select 5 elements out of 8 [0,1,2,3,4,5,6,7] then there are total <sup>8</sup>C<sub>5</sub> 
permutations out of which 1000<sup>th</sup> permutation is [8,4,6,2,0]
```java
//will return 1000
BigInteger rank = JNumberTools.rankOf().kPermutation(8,4,6,2,0);
```

#### 5.3 Ranking of repetitive permutation
Calculates the rank of repetitive permutation of 

```java
//1,0,0,1 is the 9th repeated permutation of two digits 0 and 1  
int elementCount = 2;
BigInteger result = JNumberTools.rankOf()
    .repeatedPermutation(elementCount, new Integer[]{1,0,0,1});
```

#### 5.4 Ranking of combination
Calculates the rank of given combination. For example if we generate
combination of 4 elements out of 8  in lex order, then 35th combination
will be [1,2,3,4]. So given the input 8 and [1,2,3,4], the API returns 35,
the rank of combination 

```java
BigInteger rank = JNumberTools.rankOf().uniqueCombination(8, 1,2,3,4);
```
